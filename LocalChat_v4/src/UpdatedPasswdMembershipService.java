
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownServiceException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.jxta.credential.AuthenticationCredential;
import net.jxta.credential.Credential;
import net.jxta.document.Advertisement;
import net.jxta.document.Element;
import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocument;
import net.jxta.document.StructuredDocumentFactory;
import net.jxta.document.StructuredTextDocument;
import net.jxta.document.TextElement;
import net.jxta.exception.JxtaError;
import net.jxta.exception.PeerGroupException;
import net.jxta.exception.ProtocolNotSupportedException;
import net.jxta.id.ID;
import net.jxta.id.IDFactory;
import net.jxta.membership.Authenticator;
import net.jxta.membership.MembershipService;
import net.jxta.peer.PeerID;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.platform.ModuleSpecID;
import net.jxta.protocol.ModuleImplAdvertisement;
import net.jxta.protocol.PeerGroupAdvertisement;
import net.jxta.service.Service;

public class UpdatedPasswdMembershipService implements MembershipService {

    private static final Logger LOG = Logger.getLogger(UpdatedPasswdMembershipService.class.getName());

    @Override
    public Credential getDefaultCredential() throws PeerGroupException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private class PasswdCredential implements Credential {

        UpdatedPasswdMembershipService source;
        ID peerid;
        String signedPeerID;
        String whoami;
        protected PasswdCredential(
                UpdatedPasswdMembershipService source,
                String whoami, String signedPeerID) {
            this.source = (UpdatedPasswdMembershipService) source;
            this.whoami = whoami;
            this.peerid = source.getPeerGroup().getPeerID();
            this.signedPeerID = signedPeerID;
        }

        protected PasswdCredential(
                UpdatedPasswdMembershipService source, PeerGroupID peergroup, PeerID peer, String whoami, String signedPeerID) throws PeerGroupException {
            this.source = (UpdatedPasswdMembershipService) source;
            if (!source.getPeerGroup().getPeerGroupID().equals(
                    peergroup)) {
                throw new PeerGroupException("Cannot credential for a different peer group.");
            }
            this.whoami = whoami;
            this.peerid = peer;
            this.signedPeerID = signedPeerID;
        }

        public MembershipService getSourceService() {
            return source;
        }

        public ID getPeerGroupID() {
            return source.getPeerGroup().getPeerGroupID();
        }

        public ID getPeerID() {
            return peerid;
        }

        public StructuredDocument getDocument(MimeMediaType as)
                throws Exception {
            StructuredDocument doc
                    = StructuredDocumentFactory.newStructuredDocument(as,
                            "PasswdCredential");
            Element e = doc.createElement("PeerGroupID", peergroup.getPeerGroupID().toString());
            doc.appendChild(e);
            e = doc.createElement("PeerID",
                    peergroup.getPeerID().toString());
            doc.appendChild(e);
            e = doc.createElement("Identity", whoami);
            doc.appendChild(e);
            e = doc.createElement("ReallyInsecureSignature",
                    signedPeerID);
            doc.appendChild(e);
            return doc;
        }

        public String getIdentity() {
            return whoami;
        }

        @Override
        public boolean isExpired() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean isValid() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Object getSubject() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }

    public class PasswdAuthenticator implements Authenticator {

        MembershipService source;
        AuthenticationCredential application;
        String whoami = null;
        String password = null;

        PasswdAuthenticator(MembershipService source,
                            AuthenticationCredential application) {
            this.source = source;
            this.application = application;
            try {
                StructuredTextDocument credentialsDoc
                        = (StructuredTextDocument) application.getDocument(new MimeMediaType("text/xml"));
                Enumeration elements
                        = credentialsDoc.getChildren("IdentityInfo");
                elements = ((TextElement) elements.nextElement()).getChildren();
                elements = ((TextElement) elements.nextElement()).getChildren();
                while (elements.hasMoreElements()) {
                    TextElement elem = (TextElement) elements.nextElement();
                    String nm = elem.getName();
                    if (nm.equals("login")) {
                        whoami = elem.getTextValue();
                        continue;
                    }
                    if (nm.equals("password")) {
                        password = elem.getTextValue();
                        continue;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(-1);
            }
        }

        public MembershipService getSourceService() {
            return source;
        }

        synchronized public boolean isReadyForJoin() {
            return ((null != password) && (null != whoami));
        }

        public String getMethodName() {
            return "PasswdAuthentication";
        }

        public void setAuth1Identity(String who) {
            whoami = who;
        }

        public String getAuth1Identity() {
            return whoami;
        }

        public void setAuth2_Password(String secret) {
            password = secret;
        }

        private String getAuth2_Password() {
            return password;
        }

        public AuthenticationCredential
                getAuthenticationCredential() {
            return application;
        }
    }

    static class IdMaker {

        static ID mkID(String s) {

            try {
                return IDFactory.fromURI(new URI("urn", "", "jxta:uuid-" + s));
            } catch (URISyntaxException ex) {
                throw new JxtaError("Hardcoded Spec and Class IDs are malformed.");
            }
        }
    }
    public static final ModuleSpecID passwordMembershipSpecID
            = (ModuleSpecID) IdMaker.mkID("DeadBeefDeafBabaFeedBabe00000005"
                    + "02"
                    + "06");
    private PeerGroup peergroup = null;

    private Vector principals = null;
    private Vector authCredentials = null;
    private ModuleImplAdvertisement implAdvertisement = null;
    private Hashtable logins = null;

    public void init(PeerGroup group, ID assignedID,
                     Advertisement impl)
            throws PeerGroupException {
        peergroup = group;
        implAdvertisement = (ModuleImplAdvertisement) impl;
        PeerGroupAdvertisement configAdv
                = (PeerGroupAdvertisement) group.getPeerGroupAdvertisement();

        resign();
    }

    public Service getInterface() {
        return this;
    }

    public Advertisement getImplAdvertisement() {
        return implAdvertisement;
    }

    public int startApp(String[] arg) {
        return 0;
    }

    public void stopApp() {
    }

    public PeerGroup getPeerGroup() {
        return peergroup;
    }

    public Authenticator apply(AuthenticationCredential application) throws PeerGroupException,
                                                                            ProtocolNotSupportedException {
        String method = application.getMethod();
        if ((null != method)
                && !"UpdatedPasswdAuthentication".equals(method)) {
            throw new ProtocolNotSupportedException("Authentication method not recognized");
        }
        return new PasswdAuthenticator(this, application);
    }

    public synchronized Enumeration getCurrentCredentials()
            throws PeerGroupException {
        return principals.elements();
    }

    public synchronized Enumeration getAuthCredentials() throws
            PeerGroupException {
        return authCredentials.elements();
    }

    public synchronized Credential join(Authenticator authenticated) throws PeerGroupException {
        if (!(authenticated instanceof PasswdAuthenticator)) {
            throw new ClassCastException("This is not my authenticator!");
        }
        if (!authenticated.isReadyForJoin()) {
            throw new PeerGroupException("Not Ready to join!");
        }
        if (!checkPasswd(
                ((PasswdAuthenticator) authenticated).getAuth1Identity(),
                ((PasswdAuthenticator) authenticated).getAuth2_Password())) {
            throw new PeerGroupException("Incorrect Password!");
        }
        Credential newCred = new PasswdCredential(this,
                ((PasswdAuthenticator) authenticated).getAuth1Identity(),
                "blah");
        principals.addElement(newCred);
        authCredentials.addElement(
                authenticated.getAuthenticationCredential());
        return newCred;
    }

    public synchronized void resign() throws PeerGroupException {
        principals = new Vector();
        authCredentials = new Vector();
        principals.addElement(new PasswdCredential(this, "nobody", "blah"));
    }

    public Credential makeCredential(Element element) throws
            PeerGroupException, Exception {
        Object rootIs = element.getKey();

        if (!"PasswdCredential".equals(rootIs)) {
            throw new PeerGroupException("Element does not contain a recognized credential format");
        }
        Enumeration children = element.getChildren(
                "PeerGroupID");
        if (!children.hasMoreElements()) {
            throw new RuntimeException("Missing PeerGroupID Element");
        }
        PeerGroupID peergroup = (PeerGroupID) IDFactory.fromURI(
                new URI((String) ((Element) children.nextElement()).getValue()));
        if (children.hasMoreElements()) {
            throw new RuntimeException("Extra PeerGroupID Elements");
        }
        children = element.getChildren("PeerID");
        if (!children.hasMoreElements()) {
            throw new RuntimeException("Missing PeerID Element");
        }
        PeerID peer = (PeerID) IDFactory.fromURI(new URI(
                (String) ((Element) children.nextElement()).getValue())
        );
        if (children.hasMoreElements()) {
            throw new RuntimeException("Extra PeerID Elements");
        }
        children = element.getChildren("Identity");
        if (!children.hasMoreElements()) {
            throw new RuntimeException("Missing PeerID Element");
        }
        String whoami = (String) ((Element) children.nextElement()).getValue();
        if (children.hasMoreElements()) {
            throw new RuntimeException("Extra Identity Elements");
        }
        children = element.getChildren("ReallyInsecureSignature");
        if (!children.hasMoreElements()) {
            throw new RuntimeException("Missing 'ReallyInsecureSignature' Element");
        }
        String signedPeerID = (String) ((Element) children.nextElement()).getValue();
        if (children.hasMoreElements()) {
            throw new RuntimeException("Extra 'ReallyInsecureSignature' Elements");
        }
        return new PasswdCredential(this, peergroup, peer,
                whoami, signedPeerID);
    }

    private boolean checkPasswd(String identity, String passwd) {
        boolean result;
        result = passwd.equals(makePsswd("password"));
        return result;
    }

    public static String makePsswd(String source) {
        final String xlateTable = "DQKWHRTENOGXCVYSFJPILZABMU";

        StringBuffer work = new StringBuffer(source);
        for (int eachChar = work.length() - 1; eachChar >= 0;
                eachChar--) {
            char aChar = Character.toUpperCase(
                    work.charAt(eachChar));
            int replaceIdx = xlateTable.indexOf(aChar);
            if (-1 != replaceIdx) {
                work.setCharAt(eachChar, (char) ('A' + replaceIdx));
            }
        }
        return work.toString();
    }
}
