/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package organisation.management;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.jxta.document.AdvertisementFactory;
import net.jxta.endpoint.Message;
import net.jxta.endpoint.MessageElement;
import net.jxta.exception.PeerGroupException;
import net.jxta.id.ID;
import net.jxta.id.IDFactory;
import net.jxta.peergroup.PeerGroup;
import net.jxta.pipe.InputPipe;
import net.jxta.pipe.PipeID;
import net.jxta.pipe.PipeMsgEvent;
import net.jxta.pipe.PipeMsgListener;
import net.jxta.pipe.PipeService;
import net.jxta.protocol.PipeAdvertisement;
import organisation.management.data.container.CalendarActions;
import organisation.management.data.container.PermissionsAsk;
import organisation.management.impl.Responsable;
import organisation.management.impl.Secretary;
import organisation.management.impl.SimpleParticipant;
import organisation.management.protocol.calendar.CalendarAction;
import service.groupJoin.event.GroupJoinEvent;
import service.groupJoin.impl.GroupJoinService;
import service.groupJoin.impl.message.PermissionAsk;
import service.groupJoin.impl.message.PermissionResponse;
import service.groupJoin.listener.IGroupJoinListener;
import service.groupJoin.permission.listener.IPermNotifier;
import services.controller.CustomGroup;

/**
 *
 * @author The Boss
 */
public abstract class Member {

    public static int nbMember = 0;
    private static final Logger Log = Logger.getLogger(Member.class.getName());

    protected static final long OUTPUT_PIPE_LIFE_TIME = 10;
    private static final List<String> allRoles = new ArrayList<>();

    static {
        allRoles.add(SimpleParticipant.role);
        allRoles.add(Secretary.role);
        allRoles.add(Responsable.role);
    }
    /**
     * input pipe to receive all group messages
     */
    private InputPipe calendarMessageInputPipe;
    /**
     * Controller to have communicate with other services in the same group
     */
    protected CustomGroup controller;

    //There many member in the administration so let put all of them in a group
    protected GroupJoinService adminGroupManagement;

    private CalendarActions calendarDatasModel;

    protected PermissionsAsk permissionAskDatasModel;

    public Member() {
        nbMember++;
        adminGroupManagement = new GroupJoinService();
    }

    public boolean startWorking() {
        boolean working = false;
        //Well know id for the admin group management service
        Log.info("Starting Member");
        ID assignedID = IDFactory.newPeerGroupID(controller.getGroup().getPeerGroupID(), "ADMINISTRATION-GROUPJOIN".getBytes());
        try {

            System.out.println("Admin group management with servername id  " + controller.getMyservername());
            adminGroupManagement.init(controller.getGroup(), assignedID, null);
            adminGroupManagement.startApp(new String[]{controller.getMyservername()});
            adminGroupManagement.addPermNotifier(new PermNotifier());
            adminGroupManagement.setLocalKeyStore(controller.getKeyStore());

            if (controller == null) {
                throw new IllegalStateException("This member does not have a controller");
            }

            if (calendarDatasModel == null) {
                calendarDatasModel = new CalendarActions(controller.getGroup().getPeerGroupName());
            }
            if (permissionAskDatasModel == null) {
                permissionAskDatasModel = new PermissionsAsk();
            }
            buildCalendarInputPipeMessagePipe();
            working = true;
        } catch (PeerGroupException ex) {
            Log.log(Level.SEVERE, "Init admin group management "
                    + "finish with a PeerGroup exception", ex);
        } catch (IOException ex) {
            Log.log(Level.SEVERE, "Unable to build Calendar Input message pipe", ex);
        }

        return working;
    }

    public void stopWorking() {
        Log.info("Stopping The member ---");
        adminGroupManagement.stopApp();
        calendarMessageInputPipe.close();
        calendarMessageInputPipe = null;

        //Remove from the memory all unreferenced object (:
        System.gc();
    }

    protected void buildCalendarInputPipeMessagePipe() throws IOException {
        PeerGroup group = controller.getGroup();
        PipeAdvertisement pipeAdv = (PipeAdvertisement) AdvertisementFactory.
                newAdvertisement(PipeAdvertisement.getAdvertisementType());
        //Build a well know pipeID using the peer id and the class name
        PipeID pipeID = IDFactory.newPipeID(group.getPeerGroupID(), Member.class.getName().getBytes());
        pipeAdv.setPipeID(pipeID);
        pipeAdv.setType(PipeService.PropagateType);
        pipeAdv.setName("CalendarMessage");
        pipeAdv.setDescription("PropagatePipe for all Member to receive shared calendar information");

        calendarMessageInputPipe = group.getPipeService().createInputPipe(pipeAdv, new CalendarMessageInputPipeListener());
        System.out.println(pipeAdv.getID());
    }

    protected final class CalendarMessageInputPipeListener implements PipeMsgListener {

        @Override
        public void pipeMsgEvent(PipeMsgEvent event) {
            //System.out.println("New Calendar action message !! ");
            Message msg = event.getMessage();
            MessageElement element;
            element = msg.getMessageElement(CalendarAction.stringAction(CalendarAction.ActionType.ADD));
            if (element == null) {
                element = msg.getMessageElement(CalendarAction.stringAction(CalendarAction.ActionType.MODIFY));
            }
            if (element == null) {
                element = msg.getMessageElement(CalendarAction.stringAction(CalendarAction.ActionType.REMOVE));
            }
            if (element == null) {
                element = msg.getMessageElement(CalendarAction.stringAction(CalendarAction.ActionType.DO));
            }
            if (element == null) {
                System.out.println("null message");
            }

            try {
                if (element != null) {
                    CalendarAction action;
                    action = new CalendarAction(new ByteArrayInputStream(element.getBytes(true)));
                    calendarDatasModel.registerAction(action);
                }

            } catch (IOException ex) {
                Log.log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                System.out.println(ex.toString());
            }
        }
    }

    public void setController(CustomGroup controller) {
        this.controller = controller;
    }

    public CalendarActions getCalendarDatasModel() {
        return calendarDatasModel;
    }

    public PermissionsAsk getPermissionsAskDatasModel() {
        return permissionAskDatasModel;
    }

    protected class PermNotifier implements IPermNotifier {

        @Override
        public void newPerm(GroupJoinService source, PermissionAsk newPerm) {
            String msg;

            permissionAskDatasModel.newPermMessage(newPerm);

            //Auto accept. Becauce it is a great offer
            String role = newPerm.getIdentity();
            msg = "Joinning the admin group with '" + role + "' role";

            Log.info(msg);

            if (controller.setRole(role)) {
                adminGroupManagement.joinGroup(newPerm);
                permissionAskDatasModel.setState(newPerm, true);
                Log.info("This member joinned the admin group");
                try {
                    controller.restartManagementService();

                } catch (IOException ex) {
                    Log.log(Level.SEVERE, "Cannot restart the management services", ex);
                }
            } else {
                msg = "Cannot join the group with '" + newPerm.getIdentity() + "' role";
                Log.warning(msg);
            }
        }
    }

    protected class GroupJoinListener implements IGroupJoinListener {

        @Override
        public void addPeerResult(GroupJoinEvent event) {
            PermissionResponse rep = event.get();
            switch (rep.getResponseType()) {
                case PermissionResponse.JOINED:
                    permissionAskDatasModel.setState(rep.getPermissionAsk(), true);
                    break;
                case PermissionResponse.DISCARD:
                    permissionAskDatasModel.setState(rep.getPermissionAsk(), false);
                    break;
            }
        }

        @Override
        public void joined(PeerGroup peerGroup, String identity ) {
            Log.info("Joined the admin group");
        }

    }


    public static boolean isRole(String role) {
        return allRoles.stream().anyMatch((validRole)
                -> (role.equals(validRole)));
    }

    public abstract String getRole();

}
