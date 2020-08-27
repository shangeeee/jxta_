/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TestApp;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayInputStream;
import java.util.Enumeration;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import net.jxta.credential.AuthenticationCredential;
import net.jxta.discovery.DiscoveryEvent;
import net.jxta.discovery.DiscoveryListener;
import net.jxta.discovery.DiscoveryService;
import net.jxta.document.AdvertisementFactory;
import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocument;
import net.jxta.exception.PeerGroupException;
import net.jxta.exception.ProtocolNotSupportedException;
import net.jxta.membership.MembershipService;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.protocol.DiscoveryResponseMsg;
import net.jxta.protocol.PeerGroupAdvertisement;

public class FindAndJoinGroup extends JFrame {

    private static PeerGroup netPeerGroup = null,
            wileyHowGroup = null,
            discoveredWileyHowGroup = null;
    private static PeerGroupID wileyHowGroupID;
    private DiscoveryService myDiscoveryService = null;
    private JTextArea displayArea;
    private final static MimeMediaType XMLMIMETYPE = new MimeMediaType("text/xml");

    public static void main(String args[]) {
        FindAndJoinGroup myapp = new FindAndJoinGroup();

        myapp.addWindowListener(
                new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        System.exit(0);
                    }
                }
        );
        myapp.run();
    }

    public FindAndJoinGroup() {
        super("User");
        Container c = getContentPane();
        displayArea = new JTextArea();
        c.add(new JScrollPane(displayArea), BorderLayout.CENTER);
        setSize(300, 150);
        show();
        launchJXTA();
        getServices();
        findAdvertisement("Name", "wileyHowGroup");
    }

    public void run() {
    }

    private void launchJXTA() {
        displayArea.append("Launching Peer into JXTA Network...\n");
        //netPeerGroup = PeerGroupFactory.newNetPeerGroup();
    }

    private void getServices() {
        displayArea.append("Obtaining Discovery Service....\n");
        myDiscoveryService = netPeerGroup.getDiscoveryService();
    }

    private void findAdvertisement(String searchKey, String searchValue) {
        Enumeration myLocalEnum = null;
        PeerGroupAdvertisement localWileyHowGroupAdv = null;
        displayArea.append("Trying to find advertisement...\n");
        try {
            myLocalEnum = myDiscoveryService.getLocalAdvertisements(DiscoveryService.GROUP, searchKey, searchValue);
            if ((myLocalEnum != null) && myLocalEnum.hasMoreElements()) {
                displayArea.append("Found Local Advertisement...\n");
                PeerGroupAdvertisement myFoundPGA = null;
                while (myLocalEnum.hasMoreElements()) {
                    myFoundPGA = (PeerGroupAdvertisement) myLocalEnum.nextElement();
                    if (myFoundPGA.getName().equals(searchValue)) {
                        localWileyHowGroupAdv = myFoundPGA;
                        break;
                    }
                }
                if (localWileyHowGroupAdv != null) {
                    displayArea.append("Creating new group variable...\n");
                    wileyHowGroup = netPeerGroup.newGroup(localWileyHowGroupAdv);
                    joinGroup(wileyHowGroup);
                }
            } else {
                DiscoveryListener myDiscoveryListener = new DiscoveryListener() {
                    public void discoveryEvent(DiscoveryEvent e) {
                        Enumeration enume;
                        String str;
                        displayArea.append("Found Remote Advertisement...\n");
                        DiscoveryResponseMsg myMessage = e.getResponse();
                        enume = myMessage.getResponses();
                        str = (String) enume.nextElement();
                        try {
                            //PeerGroupAdvertisement myPeerGroupAdv
////                                    = (PeerGroupAdvertisement) AdvertisementFactory.
////                                    newAdvertisement(XMLMIMETYPE, new ByteArrayInputStream(str.getBytes()));
//                            displayArea.append("Creating new group variable...\n");
//                            wileyHowGroup = netPeerGroup.newGroup(myPeerGroupAdv);
//                            joinGroup(wileyHowGroup);
                        } catch (Exception ee) {
                            ee.printStackTrace();
                            System.exit(-1);
                        }
                    }
                };
                displayArea.append("Launching Remote Discovery Service...\n");
                myDiscoveryService.getRemoteAdvertisements(null,
                        DiscoveryService.GROUP, searchKey, searchValue, 1, myDiscoveryListener);
            }
        } catch (Exception e) {
            System.out.println("Error during advertisement search");
            System.exit(-1);
        }
    }

    void joinGroup(PeerGroup myLocalGroup) {
        StructuredDocument myCredentials = null;
        try {
            AuthenticationCredential myAuthenticationCredential
                    = new AuthenticationCredential(myLocalGroup, null, myCredentials);
            MembershipService myMembershipService
                    = myLocalGroup.getMembershipService();
            net.jxta.membership.Authenticator myAuthenticator
                    = myMembershipService.apply(myAuthenticationCredential);
            if (!myAuthenticator.isReadyForJoin()) {
                displayArea.append("Authenticator is not complete\n");
                return;
            }
            myMembershipService.join(myAuthenticator);
            displayArea.append("Group has been joined\n");
        } catch (PeerGroupException | ProtocolNotSupportedException e) {
            displayArea.append("Authentication failed - group not joined\n");
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
