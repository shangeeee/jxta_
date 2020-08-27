/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service.groupJoin.listener;

import java.util.EventListener;
import net.jxta.peergroup.PeerGroup;
import service.groupJoin.event.GroupJoinEvent;

/**
 *
 * @author The Boss
 */
public interface IGroupJoinListener extends EventListener{
    public void addPeerResult(GroupJoinEvent event);
    
    public void joined(PeerGroup peerGroup , String identity );
}
