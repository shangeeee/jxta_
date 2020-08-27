/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package organisation.management;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.jxta.exception.PeerGroupException;
import net.jxta.peergroup.PeerGroup;
import services.controller.CustomGroup;

/**
 *
 * @author The Boss
 */
public abstract class Administration extends Member {

    protected final String adminGroupSuffixe = "AdminGroup";

    protected String adminGroupServerName;
    /**
     * The group for admin member
     */
    protected CustomGroup adminGroupController;

    public Administration() {
        super();

    }

    @Override
    public boolean startWorking() {

        if (controller == null) {
            throw new IllegalStateException("\"Cannot start the admin group management --- "
                    + "The controller is not set");
        }
        adminGroupServerName = controller.getGroup().getPeerGroupName() + "--" + adminGroupSuffixe;
        if (super.startWorking()) {
            try {
                if (controller.getKeyStore() == null) {
                    throw new IllegalStateException("Cannot start the admin group management --- "
                            + "Controller key store is not set");
                }

                createAdminGroup();
                adminGroupController.startWorking();
                return true;
            } catch (PeerGroupException ex) {
                Logger.getLogger(Administration.class.getName()).log(Level.SEVERE, "Unable to start the administration "
                        + "group join service ", ex);
            } catch (IOException ex) {
                Logger.getLogger(Administration.class.getName()).log(Level.SEVERE, "Unable to create  the adminstration "
                        + "custom group", ex);
            }
        }

        return false;
    }


    private void createAdminGroup() throws PeerGroupException, IOException {

        PeerGroup adminGroup = adminGroupManagement.newPeerGroup(adminGroupServerName, "A group for all member of the admin staff");
        adminGroupManagement.addListener(new GroupJoinListener());
        adminGroupController = new CustomGroup(adminGroup, adminGroupServerName, null,true);
    }

}
