/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service.presence.listerner;

import service.presence.PresenceService;

/**
 *
 * @author The Boss
 */
public interface PresenceStateListener {

    public void stateChanged(PresenceService presenceService, PresenceService.Presence presence) ;
   
}
