/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service.presence;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.jxta.document.Advertisement;
import net.jxta.exception.PeerGroupException;
import net.jxta.id.ID;
import net.jxta.id.IDFactory;
import net.jxta.impl.protocol.ResolverQuery;
import net.jxta.impl.protocol.ResolverResponse;
import net.jxta.peer.PeerID;
import net.jxta.peergroup.PeerGroup;
import net.jxta.protocol.ResolverQueryMsg;
import net.jxta.protocol.ResolverResponseMsg;
import net.jxta.resolver.QueryHandler;
import net.jxta.resolver.ResolverService;
import net.jxta.service.Service;
import service.presence.listerner.PresenceStateListener;

/**
 *
 * @author The Boss
 */
public class PresenceService implements Service, QueryHandler {

    public final static String GROUP_MODE = "group";
    public final static String INDIVIDUAL_MODE = "indiv";

    private String handlerName;
    private PeerGroup group;
    /**
     * my own presence
     */
    private Presence myPresence;
    /**
     * To listen to remonte precense states
     */
    private final Map<String, Presence> myRemotePeerPresences;
    /**
     * To call when a specific peer presence state has changed
     */
    private final Map<String, PresenceStateListener> myRemotePeerPresencesListener;
    /**
     * Lists of peer that wants to get my presence states
     */
    private final Map<String, Presence> peersListening;

    private String presenceServiceMode;

    public PresenceService() {
        myRemotePeerPresences = Collections.synchronizedMap(new HashMap<>());
        myRemotePeerPresencesListener = Collections.synchronizedMap(new HashMap<>());
        peersListening = Collections.synchronizedMap(new HashMap<>());
    }

    @Override
    public Advertisement getImplAdvertisement() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void init(PeerGroup group, ID assignedID, Advertisement implAdv) throws PeerGroupException {

        this.group = group;
        handlerName = assignedID.toString() + this.getClass().getSimpleName();
        myPresence = new Presence();

    }

    /**
     *
     * @param args
     * arg[0] the mode of the service. PresenceService.INDIVIDUAL_MODE (default) or PresenceService.GROUP_MODE
     * @return
     */
    @Override
    public int startApp(String[] args) {
        if (group == null) {
            try {
                throw new IllegalStateException("The group of this presence service is not set.");
            } catch (IllegalStateException e) {
                Logger.getLogger(PresenceService.class.getName()).warning(e.getMessage());
                return START_AGAIN_PROGRESS;
            }
        }
        if (group != null) {
            String logMsg = "Starting Presence service in ";
            presenceServiceMode = INDIVIDUAL_MODE;
            if (args == null || args[0] == null) {
                presenceServiceMode = INDIVIDUAL_MODE;
                logMsg += " Individual mode";
                handlerName += INDIVIDUAL_MODE;
            } else {
                String mode = args[0];
                if (mode.equals(GROUP_MODE)) {
                    logMsg += " group mode";
                    presenceServiceMode = GROUP_MODE;
                    handlerName += GROUP_MODE;
                } else {
                    logMsg += " Individual mode";
                    presenceServiceMode = INDIVIDUAL_MODE;
                    handlerName += INDIVIDUAL_MODE;
                }
            }
            Logger.getLogger(PresenceService.class.getName()).info(logMsg);
            group.getResolverService().registerHandler(handlerName, this);

        }
        setMyPresence(Presence.ONLINE);

        return START_OK;
    }

    @Override
    public void stopApp() {

        Logger.getLogger(PresenceService.class.getName()).log(Level.INFO, "Stopping  group {0} Presence service", group.getPeerGroupName());
        group.getResolverService().unregisterHandler(handlerName);
        setMyPresence(PresenceService.Presence.OFFLINE);
    }

    @Override
    public int processQuery(ResolverQueryMsg query) {
        String peerID = query.getSrcPeer().toString();
        Presence concerned = myRemotePeerPresences.get(peerID);
        try {

            if (presenceServiceMode.equals(INDIVIDUAL_MODE)) {
                PresenceStateListener listener = myRemotePeerPresencesListener.get(peerID);
                if (listener != null) {
                    concerned.setPresence(query.getQuery());
                    listener.stateChanged(this, concerned);
                }
            } else {
                Presence p = new Presence();
                p.setPeerID(peerID);
                p.setPresence(query.getQuery());
                myRemotePeerPresencesListener.values().stream().forEach((listener)
                        -> {
                    listener.stateChanged(this, p);
                });
            }
            ResolverResponse rep = new ResolverResponse();
            rep.setResponse(group.getPeerID().toString() + "|" + myPresence.getPresence());
            rep.setHandlerName(handlerName);

            group.getResolverService().sendResponse(peerID, rep);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (URISyntaxException ex) {
            String msg = "A received peer id is malformed : " + peerID;
            Logger.getLogger(PresenceService.class.getName()).log(Level.SEVERE, msg, ex);
        }
        return 0;
    }

    @Override
    public void processResponse(ResolverResponseMsg response) {
        String rep = response.getResponse();
        int separatorIndex = rep.indexOf("|");
        String senderPID = rep.substring(0, separatorIndex);
        String hisPresence = rep.substring(separatorIndex + 1, rep.length());
        try {
            Presence p = new Presence();
            p.setPeerID(senderPID);
            p.presence = hisPresence;
            myRemotePeerPresencesListener.values().stream().forEach((listener)
                    -> {
                listener.stateChanged(this, p);
            });
        } catch (URISyntaxException ex) {
            String msg = "A received peer id is malformed : " + senderPID;
            Logger.getLogger(PresenceService.class.getName()).log(Level.SEVERE, msg, ex);
        }
    }

    public void setMyPresence(String newPresence) {
        System.out.println("My new persence : " + newPresence);

        this.myPresence.setPresence(newPresence);

        ResolverService resolver = group.getResolverService();

        ResolverQueryMsg updateRemote = new ResolverQuery();
        updateRemote.setHandlerName(handlerName);
        updateRemote.setQuery(String.valueOf(newPresence));
        updateRemote.setSrcPeer(group.getPeerID());

        if (presenceServiceMode.equals(INDIVIDUAL_MODE)) {
            peersListening.keySet().stream().forEach((to) -> {
                resolver.sendQuery(to, updateRemote);
            });
        } else {
            resolver.sendQuery(null, updateRemote);
        }
    }

    /**
     * I remontely copy their presence state
     *
     * @param peerID
     * @param presence
     */
    public void addMyRemotePeerPresence(PeerID peerID, Presence presence) {

        myRemotePeerPresences.put(peerID.toString(), presence);
    }

    /**
     * Their wants to be informed whenever my presence state change
     *
     * @param peerID
     * @param presence
     */
    public void addPeerListening(PeerID peerID, Presence presence) {

        peersListening.put(peerID.toString(), presence);
    }

    /**
     *
     * @param peerID
     * @param listener contains the methods to call when a remonte peer presence has changed
     */
    public void addMyRemotePeerPresencesListener(PeerID peerID, PresenceStateListener listener) {
        myRemotePeerPresencesListener.put(peerID.toString(), listener);
    }

    public static class Presence {

        /**
         * A status value indicating that a user is currently online but
         * is temporarily away from the device.
         */
        public static final String AWAY = "AWAY";
        /**
         * A status value indicating that a user is currently online but is busy and
         * does not want to be disturbed.
         */
        public static final String BUSY = "BUSY";
        /**
         * A status value indicating that a user is currently offline.
         */
        public static final String OFFLINE = "OFFLINE";
        /**
         * A status value indicating that a user is currently online.
         */
        public static final String ONLINE = "ONLINE";

        private String presence;
        private String serverName;
        private PeerID peerId;

        public Presence() {
        }

        public Presence(String serverName) {
            this.serverName = serverName;
        }

        public String getServerName() {
            return serverName;
        }

        public void setServerName(String serverName) {
            this.serverName = serverName;
        }

        public String getPresence() {
            return presence;
        }

        public void setPresence(String presence) {
            switch (presence) {
                case "AWAY":
                    this.presence = AWAY;
                    break;
                case "BUSY":
                    this.presence = BUSY;
                    break;
                case "OFFLINE":
                    this.presence = OFFLINE;
                    break;
                case "ONLINE":
                    this.presence = ONLINE;
                    break;
                default:
                    throw new IllegalArgumentException("Not a valid presence state : " + presence);
            }
        }

        public PeerID getPeerID() {
            return peerId;
        }

        public void setPeerID(PeerID peerId) {
            this.peerId = peerId;
        }

        public void setPeerID(String peerID) throws URISyntaxException {
            this.peerId = (PeerID) IDFactory.fromURI(new URI(peerID));
        }

    }

    public void setPresenceServiceMode(String presenceServiceMode) {
        this.presenceServiceMode = presenceServiceMode;
    }

    public PeerGroup getGroup() {
        return group;
    }

}
