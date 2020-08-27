/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.jxta.exception.PeerGroupException;
import net.jxta.peergroup.PeerGroup;
import organisation.management.data.container.CustomGroupModel;
import organisation.management.data.container.PermissionsAsk;
import organisation.management.impl.Responsable;
import service.groupJoin.IGroupJoinService;
import service.groupJoin.event.GroupJoinEvent;
import service.groupJoin.impl.GroupJoinService;
import service.groupJoin.impl.message.PermissionAsk;
import service.groupJoin.impl.message.PermissionResponse;
import service.groupJoin.listener.IGroupJoinListener;
import service.groupJoin.permission.listener.IPermNotifier;
import service.groupJoin.security.LocalKeyStore;
import service.presence.PresenceService;

/**
 *
 * @author The Boss
 */
public class CustomGroupManager {

    private final static Logger Log = Logger.getLogger(CustomGroupManager.class.getName());
    //use GroupJoin service to create multiples custom group
    //no management service
    private final PeerGroup rootPeerGroup;
    private final GroupJoinService peerGroupCreator;
    private final Map<String, CustomGroup> controllers;

    private final LocalKeyStore keyStore;

    private final String serviceName;

    private final GroupJoinListener peerGroupCreatorListener;

    private CustomGroup.Listener customGroupListener;

    private final PermissionsAsk permissionsAskDataModel;
    
    private CustomGroupListenerImpl customGroupListenerImpl;

    private CustomGroupModel customGroupDataModel;
    
    public CustomGroupManager(PeerGroup rootPeerGroup, LocalKeyStore keyStore) {

        this.rootPeerGroup = rootPeerGroup;
        peerGroupCreator = new GroupJoinService();
        controllers = new HashMap<>();

        peerGroupCreator.setLocalKeyStore(keyStore);

        peerGroupCreatorListener = new GroupJoinListener();

        permissionsAskDataModel = new PermissionsAsk();

        serviceName = rootPeerGroup.getPeerName();
        this.keyStore = keyStore;
        
        customGroupDataModel = new CustomGroupModel();
        
        customGroupListenerImpl = new CustomGroupListenerImpl();
    }

    public boolean start() {

        try {
            peerGroupCreator.init(rootPeerGroup, rootPeerGroup.getPeerGroupID(), null);
            peerGroupCreator.startApp(new String[]{serviceName});

            peerGroupCreator.addListener(peerGroupCreatorListener);
            peerGroupCreator.addPermNotifier(new PermNotifier());

            return true;
        } catch (PeerGroupException ex) {
            Logger.getLogger(CustomGroupManager.class.getName()).log(Level.SEVERE, "Unable to start CGM", ex);
        }
        return false;
    }

    public void stop() {
        peerGroupCreator.stopApp();
        controllers.values().stream().forEach((controller) -> {
            Log.log(Level.INFO, "Stopping controller : {0}", controller.getID());
            controller.stopWorking();
        });
        //Il n'y a pas encore la persistence.
        Logger.getLogger(CustomGroupManager.class.getName()).log(Level.INFO, "No group is stocked in the memory");
        controllers.clear();
    }

    /**
     * Create a custom group with the Responsable role
     *
     * @param customGroupName
     * @param description
     * @return
     * @throws PeerGroupException
     * @throws IOException
     */
    public CustomGroup createCustomGroup(String customGroupName, String description) throws PeerGroupException, IOException {
        return createCustomGroup(customGroupName, description, Responsable.role);
    }

    /**
     * Create a new peer group and then create the custom group
     *
     * @param customGroupName
     * @param description
     * @param role
     * @return
     * @throws PeerGroupException
     * @throws IOException
     */
    private CustomGroup createCustomGroup(String customGroupName, String description, String role) throws PeerGroupException, IOException {
        PeerGroup newPeerGroup = peerGroupCreator.newPeerGroup(customGroupName, description);

        return createCustomGroup(newPeerGroup, role);
    }

    /**
     * Create a custom group using a existing group
     *
     * @param group
     * @param role
     * @return
     * @throws PeerGroupException
     * @throws IOException
     */
    private CustomGroup createCustomGroup(PeerGroup group, String role) throws PeerGroupException, IOException {
        String customGroupName = group.getPeerName();
        String description = group.getPeerGroupAdvertisement().getDescription();

        String msg;

        CustomGroup cg = controllers.get(group.getPeerGroupID().toString());

        if (cg != null) {
            msg = customGroupName + " A custom group with this name already exists. Return it";
            Log.info(msg);
            return cg;
        }

        msg = "Creating a custom group \n"
                + "\tID  : " + group.getPeerGroupID() + "\n"
                + "\tName : " + customGroupName + "\n"
                + "\tDescription : " + description + "\n"
                + "\tManagement role : " + role;
        Log.info(msg);

        cg = new CustomGroup(group, customGroupName, role);
        cg.setKeyStore(keyStore);

        if (customGroupListener != null) {
            cg.regiterListener(customGroupListener);
        }
        cg.regiterListener(customGroupListenerImpl);
        addController(cg);
        peerGroupCreator.joinGroup(group, role);
        cg.startWorking();
        customGroupDataModel.addCustomGroup(cg);
        msg = "Controller -- " + customGroupName + " created and started with " + role + " as management role";
        Log.info(msg);

        return cg;
    }

    public void addPeer(String groupName, String peerName, String identity) {
        peerGroupCreator.sendPermission(groupName, peerName, IGroupJoinService.QUERY_TYPE_ADD, identity);
    }

    public void acceptGroupJoinPerm(PermissionAsk perm) {
        PeerGroup childPeerGroup = peerGroupCreator.getChildPeerGroup(perm.getGroupID());
        if (childPeerGroup == null) {
            peerGroupCreator.joinGroup(perm);
            permissionsAskDataModel.setState(perm, true);
        }

    }

    public void setCustomGroupListener(CustomGroup.Listener customGroupListener) {
        this.customGroupListener = customGroupListener;

    }

    public void setMyPresence(String newPresence) {
        controllers.values().stream().forEach((controller) -> {
            controller.setMyPresence(newPresence);
        });
    }

    private void addController(CustomGroup cg) {
        System.out.println("Adding a new controller : " + cg.getID());
        //controllers.put(cg.getID(), cg);
        CustomGroup r = controllers.put(cg.getGroup().getPeerGroupID().toString(), cg);
    }

    public CustomGroup getController(String groupName) {
        for (CustomGroup c : controllers.values()) {
            if (c.getGroup().getPeerGroupName().equals(groupName)) {
                return c;
            }
        }
        return null;
    }

    private class PermNotifier implements IPermNotifier {

        @Override
        public void newPerm(GroupJoinService source, PermissionAsk newPerm) {

            System.out.println("A new ADD me !!");
            permissionsAskDataModel.newPermMessage(newPerm);

            acceptGroupJoinPerm(newPerm);
        }
    }

    private class GroupJoinListener implements IGroupJoinListener {

        @Override
        public void joined(PeerGroup peerGroup, String identity) {
            String logMsg;
            logMsg = "Joined : " + peerGroup.getPeerGroupName() + " iden " + identity;

            CustomGroup cg;

            Log.info(logMsg);
            try {
                cg = controllers.get(peerGroup.getPeerGroupID().toString());

                if (cg == null) {
                    //A brand new
                    logMsg = "A brand new custom group";
                    Log.info(logMsg);
                    cg = createCustomGroup(peerGroup, identity);
                    cg.startManagementService();
                }

            } catch (IOException ex) {
                Logger.getLogger(CustomGroupManager.class.getName()).log(Level.SEVERE, "Unable to create the custom group", ex);
            } catch (PeerGroupException ex) {
                Logger.getLogger(CustomGroupManager.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

        @Override
        public void addPeerResult(GroupJoinEvent event) {
            String remontePeerServerName = event.get().getPermissionAsk().getReceiverPeerServerName();
            System.out.println(remontePeerServerName + " accept or delete the permission");
            CustomGroup cg = controllers.get(event.get().getPermissionAsk().getGroupID());
            if (event.get().getResponseType().equals(PermissionResponse.JOINED)) {
                //As he accept i will have his presence notification
                System.out.println(remontePeerServerName + " accept the permission");

            }

        }

    }

    public PermissionsAsk getPermissionsAskDataModel() {
        return permissionsAskDataModel;
    }

    public CustomGroupModel getCustomGroupDataModel() {
        return customGroupDataModel;
    }
    
    
    
    private class CustomGroupListenerImpl implements CustomGroup.Listener {

        @Override
        public void started(CustomGroup source) {}

        @Override
        public void startedManagementService(CustomGroup source, String newRole) {
            //May be the role changed
            customGroupDataModel.addCustomGroup(source);
        }

        @Override
        public void stoppedManagementService(CustomGroup source, String newRole) {}

        @Override
        public void stopped(CustomGroup source) {}

        @Override
        public void presenceStateChanged(CustomGroup source, PresenceService.Presence presence) {}
        
    }
}
