package TestApp;

import java.io.File;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchProviderException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.jxta.exception.PeerGroupException;
import net.jxta.id.IDFactory;
import net.jxta.logging.Logging;
import net.jxta.peer.PeerID;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.platform.NetworkConfigurator;
import net.jxta.platform.NetworkManager;
import net.jxta.platform.NetworkManager.ConfigMode;
import organisation.management.impl.Responsable;
import organisation.management.impl.Secretary;
import organisation.management.impl.SimpleParticipant;
import organisation.management.protocol.calendar.CalendarAction;
import service.groupJoin.security.LocalKeyStore;
import service.presence.PresenceService;
import services.controller.CustomGroup;
import services.controller.CustomGroupManager;

public class AppPeer2 {

    // Static attributes
    public static String Name;
    public static final int TcpPort = 9711;
    public static PeerID PID;
    public static File ConfigurationFile;

    public static void main(String[] args) throws IOException, PeerGroupException, InterruptedException, NoSuchProviderException, KeyStoreException, Exception {
        System.setProperty(Logging.JXTA_LOGGING_PROPERTY, Level.OFF.toString());
        NetworkManager netManager;
        NetworkConfigurator netConfigurator;
        PeerGroup netPeerGroup;
        boolean isRDV = true;

        Name = "APP_PEER2";
        PID = IDFactory.newPeerID(PeerGroupID.defaultNetPeerGroupID, Name
                .getBytes());
        ConfigurationFile = new File("." + System.getProperty("file.separator")
                + Name);

        // Removing all existing conf file
        NetworkManager.RecursiveDelete(ConfigurationFile);
        // Create the network manager
        netManager = new NetworkManager(ConfigMode.EDGE, Name,
                ConfigurationFile.toURI());

        // Retrieving the network configurator
        netConfigurator = netManager.getConfigurator();

        // Setting more configuration
        netConfigurator.setTcpPort(TcpPort);
        netConfigurator.setTcpEnabled(true);
        netConfigurator.setTcpIncoming(true);
        netConfigurator.setTcpOutgoing(true);

        LocalKeyStore lks = new LocalKeyStore(PID, Name, "passpass", "pass");
        netConfigurator.setKeyStoreLocation(lks.getFileLocation());
        netConfigurator.setPassword(lks.getKeyStorePassword());
        // Starting the network
        netPeerGroup = netManager.startNetwork();
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
        CustomGroupManager cgm = new CustomGroupManager(netPeerGroup, lks);
      
        cgm.setCustomGroupListener(new CustomGroup.Listener() {

            @Override
            public void started(CustomGroup source) {
            }

            @Override
            public void startedManagementService(CustomGroup source, String newRole) {
                if (newRole.equals(Secretary.role)) {
                    System.out.println("restartedManagementService ------------------------------");
                    Secretary s = (Secretary) source.getManagementService();
                    CalendarAction a = new CalendarAction(CalendarAction.ActionType.ADD);
                    a.setActionText(Name + "  :   I am the new secratary");
                    a.setDone(true);
                    try {
                        System.out.println("Sending a do action");
                        s.sendNewCalendarAction(a);
                    } catch (IOException ex) {
                        Logger.getLogger(AppPeer2.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

            @Override
            public void stoppedManagementService(CustomGroup source, String newRole) {
            }

            @Override
            public void stopped(CustomGroup source) {
            }

            @Override
            public void presenceStateChanged(CustomGroup source , PresenceService.Presence presence) {
                System.out.println("PEER NAME " + presence.getServerName()+ " new Presence " + presence.getPresence());
            }
        });
          cgm.start();

        System.out.println("Peername " + netPeerGroup.getPeerName());
       // Thread.sleep(10000);

        CustomGroup cg = cgm.createCustomGroup("customGroup2", "cg 2 peer2");
        cg.startManagementService();
////            
////            
        Responsable rep = (Responsable) cg.getManagementService();
////   
        cgm.addPeer("customGroup2", "APP_PEER3", SimpleParticipant.role);
        Thread.sleep(10000);
        cgm.addPeer("customGroup2", "APP_PEER1", SimpleParticipant.role);
        int m = 0;
        while (m < 1000 * 60 * 2) {
            Thread.sleep(1);
//            cgm.setMyPresence(PresenceService.Presence.ONLINE);
//            Thread.sleep(500);
//            cgm.setMyPresence(PresenceService.Presence.AWAY);
//            Thread.sleep(500);
//            cgm.setMyPresence(PresenceService.Presence.OFFLINE);
//            Thread.sleep(500);
            m++;
        }

        System.out.println("Exiting ..");
        cgm.stop();
        System.exit(0);
    }
}
