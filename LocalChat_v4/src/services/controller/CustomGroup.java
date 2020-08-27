/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.jxta.exception.PeerGroupException;
import net.jxta.peergroup.PeerGroup;
import organisation.management.Member;
import organisation.management.data.container.Peers;
import organisation.management.impl.Responsable;
import organisation.management.impl.Secretary;
import organisation.management.impl.SimpleParticipant;
import service.chat.IChatServiceListener;
import service.chat.event.ChatMessageEvent;
import service.chat.impl.ChatService;
import service.chat.impl.message.ChatMessageSend;
import service.groupJoin.security.LocalKeyStore;
import service.presence.PresenceService;
import service.presence.listerner.PresenceStateListener;
import service.serverName.ServerNameService;

/**
 * This class manages services and provides communications between them
 *
 * @author The Boss
 */
public class CustomGroup {

    private final String customGroupID;

    private static final Logger Log = Logger.getLogger(CustomGroup.class.getName());

    private final PresenceService presenceService;
    private final ChatService chatMessaging;
    private final ServerNameService serverNameService;

    private LocalKeyStore keyStore;

    private final List<Listener> registeredListener;

    private boolean restartManagementService = false;

    private Peers peersDataModel;
    /**
     * Useful for organisation management
     */
    private String role;

    private final PeerGroup group;
    private final String myservername;
    private Member managementService = null;

    private boolean forManagement;

    private PresenceStateListener presenceStateListener;

    public CustomGroup(PeerGroup group, String myservername, String role, boolean forManagement) throws PeerGroupException, IOException {
        this(group, myservername, role);
        this.forManagement = forManagement;

    }

    public CustomGroup(PeerGroup group, String myservername, String role) throws PeerGroupException, IOException {
        //customGroupID = UUID.nameUUIDFromBytes(group.toString().getBytes()).toString();
        this.forManagement = false;
        customGroupID = group.getPeerGroupID().toString();
        registeredListener = new ArrayList<>();
        presenceStateListener = new PresenceStateListenerImpl();
        peersDataModel = new Peers();

        this.group = group;
        this.myservername = myservername;
        this.role = role;

        serverNameService = new ServerNameService();

        presenceService = new PresenceService();
        chatMessaging = new ChatService();

        serverNameService.init(group, null, null);
        serverNameService.setMyServerName(myservername);
        presenceService.init(group, group.getPeerGroupID(), null);
        chatMessaging.init(myservername, group);
        chatMessaging.addChatPropagateListener(new GroupChatMessageListener());
    }

    public void startWorking() {

        String logMsg = "Starting custom group in '" + getGroup().getPeerGroupName()
                + "' group , only management service : " + forManagement;
        Log.log(Level.INFO, logMsg);
        serverNameService.startApp(null);
        if (!forManagement) {
            presenceService.startApp(new String[]{PresenceService.GROUP_MODE});
            chatMessaging.setController(this);

            chatMessaging.startApp(null);

        }
        registeredListener.stream().forEach((listener) -> {
            listener.started(this);
        });
    }

    public void stopWorking() {

        if (!forManagement) {
            presenceService.stopApp();
            serverNameService.stopApp();
            chatMessaging.stopApp();
        }
        if (managementService != null) {
            stopManagementService();
        }
        registeredListener.stream().forEach((listener) -> {
            listener.stopped(this);
        });
    }

    public void startManagementService() throws IOException {
        Log.info("Starting management service  ");
        if (role == null) {
            throw new IllegalStateException("Cannot start management service --- role is not set");
        } else if (role.equals(Responsable.role)) {
            managementService = new Responsable();
        } else if (role.equals(Secretary.role)) {
            managementService = new Secretary();
        } else if (role.equals(SimpleParticipant.role)) {
            managementService = new SimpleParticipant();
        } else {
            throw new UnsupportedClassVersionError("Managemenent service with role '" + role + "' is not supported yet");
        }

        managementService.setController(this);

        managementService.startWorking();

        if (!restartManagementService) {
            registeredListener.stream().forEach((listener) -> {
                listener.startedManagementService(this, role);
            });
        }
    }

    public void stopManagementService() {
        managementService.stopWorking();
        managementService = null;
        if (!restartManagementService) {
            registeredListener.stream().forEach((listener) -> {
                listener.stoppedManagementService(this, role);
            });
        }
        System.gc();
    }

    public void restartManagementService() throws IOException {
        restartManagementService = true;
        stopManagementService();
        startManagementService();
        registeredListener.stream().forEach((listener) -> {
            listener.startedManagementService(this, role);
        });
        restartManagementService = false;
    }

    public Member getManagementService() {
        return managementService;
    }

    public PresenceService getPresenceService() {
        return presenceService;
    }

    public ChatService getChatMessaging() {
        return chatMessaging;
    }

    public ServerNameService getServerNameService() {
        return serverNameService;
    }

    public String getMyservername() {
        return myservername;
    }

    public String getRole() {
        return role;
    }

    /**
     * Set the role
     *
     * @param role
     * @return
     */
    public boolean setRole(String role) {
        if (Member.isRole(role)) {
            this.role = role;
            return true;
        }
        return false;
    }

    public PeerGroup getGroup() {
        return group;
    }

    public LocalKeyStore getKeyStore() {
        return keyStore;
    }

    public void setKeyStore(LocalKeyStore keyStore) {
        this.keyStore = keyStore;
    }

    public void regiterListener(Listener listener) {
        if (!registeredListener.contains(listener)) {
            registeredListener.add(listener);
            //the peer id will not be used
            presenceService.addMyRemotePeerPresencesListener(group.getPeerID(), presenceStateListener);
        }
    }

    public void unRegiterListener(Listener listener) {
        registeredListener.remove(listener);
        // presenceService.remove..............
    }

    public String getID() {
        return customGroupID;
    }

    public void setMyPresence(String newP) {
        presenceService.setPresenceServiceMode(PresenceService.GROUP_MODE);
        presenceService.setMyPresence(newP);
    }
    
    public boolean sendTO(String name , String message){
        
        ChatMessageSend chatMessageSend = new ChatMessageSend(group.getPeerGroupID(), myservername, message);
        try {
            chatMessaging.send(name, chatMessageSend);
        } catch (IOException ex) {
            return false;
        }
        return  true;
    }

    public boolean sendIN(String message){
        ChatMessageSend chatMessageSend = new ChatMessageSend(group.getPeerGroupID(), myservername, message);
        try {
            chatMessaging.sendInGroup(chatMessageSend);
        } catch (IOException ex) {
            return false;
        }
        return  true;
    }
    
    private class PresenceStateListenerImpl implements PresenceStateListener {

        @Override
        public void stateChanged(PresenceService presenceService, PresenceService.Presence presence) {
            String hisName = null;
            do {
                try {
                    Thread.sleep(500);
                    hisName = serverNameService.get(presence.getPeerID());
                } catch (InterruptedException ex) {
                    //ignore
                }
            } while (hisName == null);
            presence.setServerName(hisName);
            if (peersDataModel.add(presence)) {
                Log.info("It is really a new peer -- ".concat(  group.getPeerGroupName()));
                chatMessaging.addChatUnicastListener(hisName, new IndivChatMessageListener(hisName));
            }
            registeredListener.stream().forEach((listener) -> {
                listener.presenceStateChanged(CustomGroup.this, presence);
            });
        }

    }

    private class GroupChatMessageListener implements IChatServiceListener {

        @Override
        public void processNewMessage(ChatMessageEvent chatEvent) {
            //put the new message in the chat group msg model

        }

    }

    private class IndivChatMessageListener implements IChatServiceListener {

        String name;

        public IndivChatMessageListener(String name) {
            this.name = name;
        }

        @Override
        public void processNewMessage(ChatMessageEvent chatEvent) {
            //put the new message in the individual chat msg model
        }

    }

    public interface Listener {

        public void started(CustomGroup source);

        public void startedManagementService(CustomGroup source, String newRole);

        public void stoppedManagementService(CustomGroup source, String newRole);

        public void stopped(CustomGroup source);

        public void presenceStateChanged(CustomGroup source, PresenceService.Presence presence);
    }
}
