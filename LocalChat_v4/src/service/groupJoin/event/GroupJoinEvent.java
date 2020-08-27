/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service.groupJoin.event;

import java.util.EventObject;
import service.groupJoin.impl.message.PermissionResponse;

/**
 *
 * @author The Boss
 */
public class GroupJoinEvent extends EventObject{


    PermissionResponse event;
    public GroupJoinEvent(Object source ,PermissionResponse event) {
        super(source);
        this.event = event;
    }
    
    public PermissionResponse get(){
        return event;
    }
}
