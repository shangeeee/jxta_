/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service.groupJoin.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.UnknownServiceException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.jxta.credential.AuthenticationCredential;
import net.jxta.credential.Credential;
import net.jxta.discovery.DiscoveryEvent;
import net.jxta.discovery.DiscoveryListener;
import net.jxta.discovery.DiscoveryService;
import net.jxta.document.Advertisement;
import net.jxta.document.AdvertisementFactory;
import net.jxta.document.Element;
import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocument;
import net.jxta.document.StructuredDocumentFactory;
import net.jxta.exception.PeerGroupException;
import net.jxta.exception.ProtocolNotSupportedException;
import net.jxta.id.ID;
import net.jxta.id.IDFactory;
import net.jxta.impl.membership.passwd.PasswdMembershipService;
import net.jxta.impl.membership.pse.PSEMembershipService;
import net.jxta.impl.membership.pse.StringAuthenticator;
import net.jxta.impl.protocol.ResolverQuery;
import net.jxta.impl.protocol.ResolverResponse;
import net.jxta.peergroup.PeerGroup;
import net.jxta.platform.ModuleSpecID;
import net.jxta.protocol.ModuleImplAdvertisement;
import net.jxta.protocol.ModuleSpecAdvertisement;
import net.jxta.protocol.PeerGroupAdvertisement;
import net.jxta.protocol.ResolverQueryMsg;
import net.jxta.protocol.ResolverResponseMsg;
import net.jxta.resolver.ResolverService;
import service.groupJoin.IGroupJoinService;
import service.groupJoin.event.GroupJoinEvent;
import service.groupJoin.impl.message.PermissionAsk;
import service.groupJoin.impl.message.PermissionResponse;
import service.groupJoin.listener.IGroupJoinListener;
import service.groupJoin.permission.listener.IPermNotifier;
import service.groupJoin.security.LocalKeyStore;

/**
 *
 * @author The Boss
 */
public class GroupJoinService implements IGroupJoinService {

    /**
     * The group in which this service will be used it.
     */
    private PeerGroup parentPeerGroup;
    /**
     * The id of this service within his parentPeerGroup
     */
    private ID assingnedID;

    /**
     * The implementation advertisement of this service
     */
    private Advertisement implAdv;

    /**
     * Map of peer group that this peer is managing
     * group name => group
     */
    private final Map<String, PeerGroup> childPeerGroups;

    /**
     * A name associate to the service by the peer
     */
    private String serviceName;

    /**
     * List of addMe permission
     */
    private final List<PermissionAsk> listAddMe;

    private final List<IPermNotifier> listPermNotifiers;

    private final List<IGroupJoinListener> registeredJoinListener;

    private LocalKeyStore localKeyStore = null;

    public GroupJoinService() {
        childPeerGroups = Collections.synchronizedMap(new HashMap());
        listAddMe = Collections.synchronizedList(new ArrayList());
        listPermNotifiers = Collections.synchronizedList(new ArrayList());
        registeredJoinListener = Collections.synchronizedList(new ArrayList());
    }

    public void setLocalKeyStore(LocalKeyStore localKeyStore) {
        this.localKeyStore = localKeyStore;
    }

    @Override
    public void init(PeerGroup group, ID assignedID, Advertisement implAdv)
            throws PeerGroupException {
        parentPeerGroup = group;
        this.assingnedID = assignedID;

        this.implAdv = implAdv;
    }

    @Override
    public int startApp(String[] args) {
        ResolverService resolver = parentPeerGroup.getResolverService();
        resolver.registerHandler(assingnedID.toString(), this);
        serviceName = args[0];
        return 0;
    }

    @Override
    public void stopApp() {
        ResolverService resolver = parentPeerGroup.getResolverService();
        resolver.unregisterHandler(assingnedID.toString());
        registeredJoinListener.clear();
        listPermNotifiers.clear();
    }

    @Override
    public void loadExistingGroups() {
//        Enumeration<Advertisement> listGroupExistingGroupAdv = null;
//        try {
//            DiscoveryService discovery = parentPeerGroup.getDiscoveryService();
//            //Get all local peer group advertisement
//            listGroupExistingGroupAdv = discovery.getLocalAdvertisements(
//                    DiscoveryService.GROUP, "GID", null);
//            //
//            //recreate this peer group
//            PeerGroupID id;
//            PeerGroupAdvertisement implAdv = null;
//            PeerGroup existingGroup;
//            while (listGroupExistingGroupAdv.hasMoreElements()) {
//                implAdv = (PeerGroupAdvertisement) listGroupExistingGroupAdv
//                        .nextElement();
//                id = IDFactory.newPeerGroupID(implAdv.getPeerGroupID()
//                        .toString());
//               // parentPeerGroup
//                existingGroup = parentPeerGroup.newGroup(id);
//                existingGroup.startApp(null);
//            }
//
//        } catch (IOException ex) {
//            Logger.getLogger(GroupJoinService.class.getName())
//                    .log(Level.SEVERE, null, ex);
//        } catch (PeerGroupException ex) {
//            Logger.getLogger(GroupJoinService.class.getName())
//                    .log(Level.SEVERE, null, ex);
//        }
    }

    @Override
    public PeerGroup newPeerGroup(String peerGroupName, String description) {
        PeerGroup newPeerGroup = null;
        PeerGroupAdvertisement adv;
        try {
            System.out.println("Creating a new group advertisement");

            if (!childPeerGroups.containsKey(peerGroupName)) {
                System.out.println("Good news : the new group name does not exist");

                ModuleSpecAdvertisement pgmsa = newPeerGroupModuleSpecAdv();
                ModuleImplAdvertisement pgmia = newPeerGroupImplAdv(pgmsa.getModuleSpecID());
                PeerGroupAdvertisement pga = newPeerGroupAdv(pgmsa.getModuleSpecID(), "username", "password");

                System.out.println("Creating ...");

                newPeerGroup = parentPeerGroup.newGroup(pga.getPeerGroupID(), pgmia, peerGroupName, description, true);

                childPeerGroups.put(peerGroupName, newPeerGroup);

                // publish this advertisement
                //(send out to other peers and rendezvous peer)
                //discoSvc.remotePublish(adv);
                //System.out.println("Group published successfully.");
            } else {
                System.out.println("The peer group name " + peerGroupName + " already exist");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(0);
        }

        return newPeerGroup;
    }

    /**
     * Join a group using a received permission
     *
     * @param perm a permission received recently
     */
    public void joinGroup(PermissionAsk perm) {
        if (listAddMe.contains(perm)) {
            DiscoveryListener listener;
            listener = new DiscoveryListener() {
                int nbFound = 0;

                @Override
                public void discoveryEvent(DiscoveryEvent event) {
                    nbFound++;
                    Enumeration<Advertisement> results = event.getSearchResults();
                    Advertisement adv = results.nextElement();
                    //We use only the first answer
                    if (adv != null && nbFound == 1) {

                        try {
                            PeerGroupAdvertisement groupAdv = (PeerGroupAdvertisement) adv;
                            System.out.println("The adv is found  group name " + groupAdv.getName());
                            if (childPeerGroups.containsKey(groupAdv.getName())) {
                                System.out.println("A recent has been found");
                                return;
                            }
                            PeerGroup toJoin = parentPeerGroup.newGroup(groupAdv);

                            joinGroup(toJoin, perm.getIdentity());

                            childPeerGroups.put(groupAdv.getName(), toJoin);

                            sendPermissionResponse(perm, PermissionResponse.JOINED);

                            listAddMe.remove(perm);
                        } catch (PeerGroupException ex) {
                            Logger.getLogger(GroupJoinService.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            };
            DiscoveryService discoveryService = parentPeerGroup.getDiscoveryService();
            System.out.println("Searching the group adv  from  all peer");
            //discoveryService.getRemoteAdvertisements(perm.getSenderID(), DiscoveryService.GROUP, "GID", perm.getGroupID(), 1, listener);
            discoveryService.getRemoteAdvertisements(null, DiscoveryService.GROUP, "GID", perm.getGroupID(), 1, listener);
        }
    }

    public void removePermReceiv(PermissionAsk perm) {
        listAddMe.remove(perm);
        sendPermissionResponse(perm, PermissionResponse.DISCARD);
    }

    public void joinGroup(PeerGroup toJoin, String identity) {
        System.out.println(serviceName + " => Joinning " + toJoin.getPeerGroupName() + " identity ; " + identity);
        StructuredDocument identityInfo = StructuredDocumentFactory.newStructuredDocument(MimeMediaType.XMLUTF8, "Identity");
        identityInfo.appendChild(identityInfo.createElement(identity));
        try {
            //retriving membership service
            PSEMembershipService psems = (PSEMembershipService) toJoin.getMembershipService();

            AuthenticationCredential myAuthenticationCredential
                    = new AuthenticationCredential(parentPeerGroup, "StringAuthentication", identityInfo);

            //System.out.println("auth cred " + myAuthenticationCredential.getDocument(MimeMediaType.TEXTUTF8).toString());
            StringAuthenticator sa = (StringAuthenticator) psems.apply(myAuthenticationCredential);

            if (localKeyStore == null) {
                throw new IllegalStateException("Local key store is mising (null) - Cannot join the new group");
            }

            sa.setAuth1_KeyStorePassword(localKeyStore.getKeyStorePassword());

            sa.setAuth2Identity(localKeyStore.getPeerID());

            sa.setAuth3_IdentityPassword(localKeyStore.getPrivateKeyPassword());

//            while (!sa.isReadyForJoin()) {
//                System.out.println("Authenticator is not complete");
//                Thread.sleep(2000);
//            }
            Credential join = psems.join(sa);

            registeredJoinListener.stream().forEach((listener) -> {

                listener.joined(toJoin, identity);
            });

        } catch (ProtocolNotSupportedException | IllegalStateException | PeerGroupException ex) {
            Logger.getLogger(GroupJoinService.class.getName()).log(Level.SEVERE, "Authentication failed - group not joined", ex);

        }
    }

    @Override
    public PermissionAsk sendPermission(String groupName, String peerServerName, String perType, String identity) {
        System.out.println("Preparing to send a permission : " + perType + " from " + serviceName + " to " + peerServerName);

        PermissionAsk query = null;
        if (childPeerGroups.containsKey(groupName)) {
            ResolverService resolver = parentPeerGroup.getResolverService();

            PeerGroup concerned = childPeerGroups.get(groupName);
            if (concerned.getPeerGroupAdvertisement() == null) {
                System.out.println("Null adv");
                return null;
            }

            if (identity != null) {
                query = new PermissionAsk(serviceName, peerServerName,
                        perType, concerned.getPeerGroupID().toString(), identity);
            } else {
                query = new PermissionAsk(serviceName, peerServerName,
                        perType, concerned.getPeerGroupID().toString());
            }
            query.setSenderID(parentPeerGroup.getPeerID().toString());

            ResolverQueryMsg rQuery = new ResolverQuery();
            rQuery.setHandlerName(assingnedID.toString());
            rQuery.setQuery(query.toString());
            rQuery.setSrcPeer(parentPeerGroup.getPeerID());

            //Ugly but work
            resolver.sendQuery(null, rQuery);

        } else {
            System.out.println("List of group does not contains " + groupName);
        }
        return query;
    }

    private void sendPermissionResponse(PermissionAsk perMsg, String reponseType) {
        PermissionResponse rep = new PermissionResponse(perMsg, reponseType);

        ResolverResponseMsg rRep = new ResolverResponse();
        rRep.setHandlerName(assingnedID.toString());
        rRep.setResponse(rep.toString());

        parentPeerGroup.getResolverService().sendResponse(perMsg.getSenderID(), rRep);
    }

    @Override
    public void addListener(IGroupJoinListener listener) {
        Logger.getLogger(GroupJoinService.class.getName()).log(Level.INFO, "Registering a group join listener ");

        registeredJoinListener.add(listener);
    }

    public void addPermNotifier(IPermNotifier notifier) {
        listPermNotifiers.add(notifier);
    }

    public void removePermNotifier(IPermNotifier notifier) {
        listPermNotifiers.remove(notifier);
    }

    @Override
    public Advertisement getImplAdvertisement() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int processQuery(ResolverQueryMsg query) {
        try {
            //two types of query join me -  remove me
            System.out.println("A permission has been received !!!");
            if (query.getQuery() == null) {
                System.out.println("Invalid query");
                return 1;
            }
            //System.out.println("QUERY : \n" + query.getQuery());
            PermissionAsk newPerm = new PermissionAsk(new ByteArrayInputStream(query.getQuery().getBytes()));
            if (newPerm.getReceiverPeerServerName().equals(serviceName)) {
                System.out.println("My service name ");
            } else {
                System.out.println("Not my service name " + newPerm.getReceiverPeerServerName() + " mine is " + serviceName);
                return 1;
            }
            System.out.println("Perm type " + newPerm.getPermissionType());
            switch (newPerm.getPermissionType()) {
                case QUERY_TYPE_ADD: {
                    listAddMe.add(newPerm);
                    break;
                }
                case QUERY_TYPE_LEAVE: {
                    System.out.println("A leaveMe permission ask. Must leave automatically "
                            + "every out input pipe, handler must be .... to completely leave");
                    break;
                }
            }
            //Inform all registered listener
            listPermNotifiers.stream().forEach((listener) -> {
                        listener.newPerm(this, newPerm);
            });


        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    @Override
    public void processResponse(ResolverResponseMsg response) {
        try {

            //two types of response i join you  - i leave you
            PermissionResponse rep = new PermissionResponse(new ByteArrayInputStream(response.getResponse().getBytes()));

            GroupJoinEvent event = new GroupJoinEvent(this, rep);

            registeredJoinListener.stream().forEach((listener)
                    -> {
                listener.addPeerResult(event);
            });

        } catch (IOException ex) {
            Logger.getLogger(GroupJoinService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public PeerGroup getParentPeerGroup() {
        return parentPeerGroup;
    }

    public PeerGroup getChildPeerGroup(String nameOrId) {
        PeerGroup group = childPeerGroups.get(nameOrId);
        if (group != null) {
            return group;
        }
        for (PeerGroup p : childPeerGroups.values()) {
            if (p.getPeerGroupID().toString().equals(nameOrId)) {
                return p;
            }
        }
        return null;
    }

    private PeerGroupAdvertisement newPeerGroupAdv(ModuleSpecID moduleSpecID, String username, String passwd) throws IOException {

        PeerGroupAdvertisement adv = (PeerGroupAdvertisement) AdvertisementFactory
                .newAdvertisement(PeerGroupAdvertisement.getAdvertisementType());

        adv.setPeerGroupID(IDFactory.newPeerGroupID());
        adv.setModuleSpecID(moduleSpecID);

        String loginString = username + ":" + PasswdMembershipService.makePsswd(passwd) + ":";
        StructuredDocument loginInfo = StructuredDocumentFactory.newStructuredDocument(MimeMediaType.XMLUTF8,
                "Param");
        Element loginElement = loginInfo.createElement("login", loginString);
        //loginInfo.appendChild(loginElement);

        return adv;
    }

    private ModuleImplAdvertisement newPeerGroupImplAdv(ModuleSpecID moduleSpecID) throws Exception {
        ModuleImplAdvertisement peerGroupImplAdv = (ModuleImplAdvertisement) AdvertisementFactory
                .newAdvertisement(ModuleImplAdvertisement.getAdvertisementType());

        peerGroupImplAdv.setModuleSpecID(moduleSpecID);
        peerGroupImplAdv.setDescription("Group created by " + serviceName + " " + moduleSpecID.toString());
        peerGroupImplAdv.setCompat(parentPeerGroup.getAllPurposePeerGroupImplAdvertisement().getCompat());
        peerGroupImplAdv.setCode(parentPeerGroup.getAllPurposePeerGroupImplAdvertisement().getCode());
        peerGroupImplAdv.setUri(parentPeerGroup.getAllPurposePeerGroupImplAdvertisement().getUri());
        peerGroupImplAdv.setParam(parentPeerGroup.getAllPurposePeerGroupImplAdvertisement().getParam());

        parentPeerGroup.getDiscoveryService().publish(peerGroupImplAdv);

        return peerGroupImplAdv;
    }

    private ModuleSpecAdvertisement newPeerGroupModuleSpecAdv() throws UnknownServiceException, URISyntaxException, Exception {
        ModuleSpecID specID = (ModuleSpecID) IDFactory.newModuleSpecID(parentPeerGroup.getAllPurposePeerGroupImplAdvertisement().getModuleSpecID().getBaseClass());
        // Create the Module Specification Advertisement.
        ModuleSpecAdvertisement moduleSpecAdv = (ModuleSpecAdvertisement) AdvertisementFactory
                .newAdvertisement(ModuleSpecAdvertisement.getAdvertisementType());
        // Configure the Module Specification Advertisement.
        moduleSpecAdv.setModuleSpecID(specID);
        moduleSpecAdv.setCreator(parentPeerGroup.getPeerName());
        moduleSpecAdv.setDescription("A specification for an custom peer goup.");
        moduleSpecAdv.setName("JXTASPEC:CustomPeerGroupSpec");
        moduleSpecAdv.setSpecURI("http://www.four.com/projects/LocalChat");
        moduleSpecAdv.setVersion("1.0");

        parentPeerGroup.getDiscoveryService().publish(moduleSpecAdv);

        // Return the advertisement to the caller.
        return moduleSpecAdv;
    }

//    private ModuleImplAdvertisement newPasswdServiceModuleImplAdvertisement() throws IOException, Exception {
//        ModuleImplAdvertisement passMembershipServiceMIA = (ModuleImplAdvertisement) AdvertisementFactory
//                .newAdvertisement(ModuleImplAdvertisement.getAdvertisementType());
//        passMembershipServiceMIA.setModuleSpecID(PasswdMembershipService.passwordMembershipSpecID);
//        passMembershipServiceMIA.setCode(PasswdMembershipService.class.getName());
//        passMembershipServiceMIA.setDescription("Module Impl Advertisement for PasswdMembership Service");
//        passMembershipServiceMIA.setCompat(parentPeerGroup.getAllPurposePeerGroupImplAdvertisement().getCompat());
//        passMembershipServiceMIA.setUri(parentPeerGroup.getAllPurposePeerGroupImplAdvertisement().getUri());
//        passMembershipServiceMIA.setProvider(parentPeerGroup.getAllPurposePeerGroupImplAdvertisement().getProvider());
//
//        parentPeerGroup.getDiscoveryService().publish(passMembershipServiceMIA);
//
//        return passMembershipServiceMIA;
//    }
//
//    private ModuleClassAdvertisement getPasswdMembershipServiceMCA() throws IOException {
//        if (passwdMembershipServiceMCA == null) {
//            passwdMembershipServiceMCA = (ModuleClassAdvertisement) AdvertisementFactory
//                    .newAdvertisement(ModuleClassAdvertisement.getAdvertisementType());
//            passwdMembershipServiceMCA.setName("JXTAMOD:"+PasswdMembershipService.class.getName());
//            passwdMembershipServiceMCA.setDescription("Module class adv for the password membership service");
//            passwdMembershipServiceMCA.setModuleClassID(IDFactory.newModuleClassID(PeerGroup.membershipClassID));
//        }
//        
//        System.out.println(passwdMembershipServiceMCA.getDocument(MimeMediaType.TEXTUTF8));
//        return passwdMembershipServiceMCA;
//    }
}
