/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package organisation.management.impl;

import java.util.logging.Logger;
import organisation.management.Member;

/**
 *
 * @author The Boss
 */
public class SimpleParticipant extends Member {
private static final Logger Log = Logger.getLogger(SimpleParticipant.class.getName());

    public static String role = "Participant";

    public SimpleParticipant() {
          super();
       
      
    }

    
    @Override
    public boolean startWorking() {
        if (super.startWorking()) {
             Log.info("Starting SimpleParticipant role");
            return true;
        }
        return false;
    }

    @Override
    public void stopWorking() {
        Log.info("Stopping SimpleParticipant role");
        super.stopWorking();

        System.gc();
    }
    @Override
    public String getRole() {
        return role;
    }

}
