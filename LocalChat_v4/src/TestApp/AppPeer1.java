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
import organisation.management.impl.SimpleParticipant;
import service.groupJoin.security.LocalKeyStore;
import service.presence.PresenceService;
import services.controller.CustomGroup;
import services.controller.CustomGroupManager;

public class AppPeer1 {

    // Static attributes
    public static String Name;
    public static final int TcpPort = 9710;
    public static PeerID PID;
    public static File ConfigurationFile;

    public static void main(String[] args) throws InterruptedException {
        System.setProperty(Logging.JXTA_LOGGING_PROPERTY, Level.OFF.toString());

        NetworkManager netManager = null;
        NetworkConfigurator netConfigurator = null;
        PeerGroup netPeerGroup;

        Name = "APP_PEER1";

        PID = IDFactory.newPeerID(PeerGroupID.defaultNetPeerGroupID, Name.getBytes());
        ConfigurationFile = new File("." + System.getProperty("file.separator") + Name);

        try {
            // Removing all existing conf file
            NetworkManager.RecursiveDelete(ConfigurationFile);
            // Create the network manager
            netManager = new NetworkManager(ConfigMode.EDGE, Name, ConfigurationFile.toURI());

            // Retrieving the network configurator
            netConfigurator = netManager.getConfigurator();

            // Setting more configuration
            netConfigurator.setTcpPort(TcpPort);
            netConfigurator.setTcpEnabled(true);
            netConfigurator.setTcpIncoming(true);
            netConfigurator.setTcpOutgoing(true);

            LocalKeyStore lks = new LocalKeyStore(PID, Name, "pass", "pass");
            netConfigurator.setKeyStoreLocation(lks.getFileLocation());
            netConfigurator.setPassword(lks.getKeyStorePassword());

            // Starting the network
            netPeerGroup = netManager.startNetwork();

            //////////////////////////////////////////////////////////////////////////////////////
            CustomGroupManager cgm = new CustomGroupManager(netPeerGroup, lks);
            cgm.setCustomGroupListener(new CustomGroup.Listener() {

                @Override 
                public void started(CustomGroup source) {
                    if(source.getGroup().getPeerGroupName().equals("customGroup2")){
                        source.sendTO("APP_PEER2" , "salut le group");
                    }
                }

                @Override
                public void startedManagementService(CustomGroup source, String newRole) {
                    
                }

                @Override
                public void stoppedManagementService(CustomGroup source, String newRole) {
                }

                @Override
                public void stopped(CustomGroup source) {
                }

                @Override
                public void presenceStateChanged(CustomGroup source , PresenceService.Presence presence){
                  
                  
                    System.out.println("PEER NAME " + presence.getServerName()+ " new Presence " + presence.getPresence());
                }
            });
            cgm.start();
            CustomGroup cg = cgm.createCustomGroup("customGroup1", "cg 1 peer1");
            cg.startManagementService();
//            
//            
            Responsable rep = (Responsable) cg.getManagementService();
//   
            cgm.addPeer("customGroup1", "APP_PEER2", SimpleParticipant.role);
            Thread.sleep(10000);
            cgm.addPeer("customGroup1", "APP_PEER3", SimpleParticipant.role);
//            

           //rep.addToAdminGroup("customGroup1", Secretary.role);
          // rep.addToAdminGroup("APP_PEER2", Secretary.role);
            int m = 0;
            while (m < 1000 * 60 * 2) {
                Thread.sleep(1);
                //cgm.setMyPresence(PresenceService.Presence.ONLINE);
                m++;
            }

            System.out.println("Nb peeer " + cg.getServerNameService().getNbPeerResolved());
            System.out.println("Exiting ..");
            cgm.stop();
            System.exit(0);
            //
        } catch (IOException | PeerGroupException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchProviderException | KeyStoreException | IllegalStateException ex) {
            Logger.getLogger(AppPeer1.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
