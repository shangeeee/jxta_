/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package organisation.management.impl;

import java.util.logging.Logger;
import organisation.management.Administration;
import service.groupJoin.IGroupJoinService;
import service.groupJoin.impl.message.PermissionAsk;

/**
 *
 * @author The Boss
 */
public class Responsable extends Administration {

    private static final Logger Log = Logger.getLogger(Responsable.class.getName());
    public static String role = "Responsable";

    public Responsable() {
        super();
    }

    @Override
    public boolean startWorking() {
        if (super.startWorking()) {
            adminGroupManagement.joinGroup(adminGroupController.getGroup(), role );
            return true;
        }
        return false;
    }

    @Override
    public void stopWorking() {
        Log.info("Stopping Responsable role ---");
        super.stopWorking();

        System.gc();
    }

    @Override
    public String getRole() {
        return role;
    }

    public void addToAdminGroup(String peerServername, String role) {
      
        String logMsg = "Sending a permission to " + peerServername + " to add him to the admin group with role " + role;
        Log.info(logMsg);
        PermissionAsk perm = adminGroupManagement.sendPermission(adminGroupServerName, peerServername,
                IGroupJoinService.QUERY_TYPE_ADD, role);
        permissionAskDatasModel.newPermMessage(perm);
    }

}
