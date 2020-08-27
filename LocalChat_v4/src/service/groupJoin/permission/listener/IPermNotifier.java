/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service.groupJoin.permission.listener;

import service.groupJoin.impl.GroupJoinService;
import service.groupJoin.impl.message.PermissionAsk;

/**
 *
 * @author The Boss
 */
public interface IPermNotifier {
     public void newPerm(GroupJoinService source ,PermissionAsk newPerm);
}
