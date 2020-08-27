/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service.groupJoin;

import net.jxta.peergroup.PeerGroup;
import net.jxta.resolver.QueryHandler;
import net.jxta.service.Service;
import service.groupJoin.impl.message.PermissionAsk;
import service.groupJoin.listener.IGroupJoinListener;

/**
 *
 * @author The Boss
 */
public interface IGroupJoinService extends Service, QueryHandler {

    public static String DEFAULT_HANDLER = "group join";
    public static String SENDER_NAMESPACE = "sender";
    public static String RECEIVER_NAMESPACE = "receiver";
    public static String QUERY_TYPE_NAMESPACE = "type";
    public static String QUERY_TYPE_ADD = "addMe";
    public static String QUERY_TYPE_LEAVE = "leaveMe";
    public static String GROUP_ID_NAMESPACE = "groupID";
    public static String SENDER_ID_NAMESPACE = "SenderPeerID";

    /**
     * Load all existing peerGroups using the local group advertisement
     * and update a map of peerGroupName => peerGroup
     */
    public void loadExistingGroups();

    /**
     * Create a new peer group in the current peer group ,publish it
     * and update a map of peerGroupName => peerGroup
     *
     * @param peerGroupName The name of the peer group
     * @param description Brief description of the group
     * @return a new peer group
     */
    public PeerGroup newPeerGroup(String peerGroupName, String description);

     public PermissionAsk sendPermission(String groupName, String peerServerName , String perType , String identity) ;

    /**
     * Add a listener to have a notification of success or failure
     *
     * @param listener
     */
    public void addListener( IGroupJoinListener listener);

}
