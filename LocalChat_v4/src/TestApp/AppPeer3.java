/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
import organisation.management.impl.Secretary;
import organisation.management.protocol.calendar.CalendarAction;
import service.groupJoin.security.LocalKeyStore;
import service.presence.PresenceService;
import services.controller.CustomGroup;
import services.controller.CustomGroupManager;

/**
 *
 * @author The Boss
 */
public class AppPeer3 {

    // Static attributes
    public static String Name;
    public static final int TcpPort = 9713;
    public static PeerID PID;
    public static File ConfigurationFile;

    public static void main(String[] args) throws IOException, PeerGroupException, InterruptedException, NoSuchProviderException, KeyStoreException, Exception {
        System.setProperty(Logging.JXTA_LOGGING_PROPERTY, Level.OFF.toString());
        NetworkManager netManager;
        NetworkConfigurator netConfigurator;
        PeerGroup netPeerGroup;
        boolean isRDV = true;

        Name = "APP_PEER3";
        PID = IDFactory.newPeerID(PeerGroupID.defaultNetPeerGroupID, Name
                .getBytes());
        ConfigurationFile = new File("." + System.getProperty("file.separator")
                + Name);

        // Removing all existing conf file
        NetworkManager.RecursiveDelete(ConfigurationFile);
        // Create the network manager
        netManager = new NetworkManager(NetworkManager.ConfigMode.EDGE, Name,
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
                    Secretary s = (Secretary) source.getManagementService();
                    CalendarAction a = new CalendarAction(CalendarAction.ActionType.ADD);
                    a.setActionText(Name + "   :  I am the new secratary");
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
            public void presenceStateChanged(CustomGroup source , PresenceService.Presence presence){
                System.out.println("PEER NAME " + presence.getServerName()+ " new Presence " + presence.getPresence());
            }
        });
        cgm.start();

        System.out.println("Peername " + netPeerGroup.getPeerName());

        int m = 0;
        while (m < 1000 * 60 * 2) {
            Thread.sleep(1);
            m++;
            
        }
        cgm.getController("customGroup1").sendIN("salut le group");
        System.out.println("Exiting ..");
        cgm.stop();
        System.exit(0);
    }
}
