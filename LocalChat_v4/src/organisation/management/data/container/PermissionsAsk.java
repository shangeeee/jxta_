/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package organisation.management.data.container;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.table.AbstractTableModel;
import service.groupJoin.impl.message.PermissionAsk;

/**
 *
 * @author The Boss
 */
public class PermissionsAsk extends AbstractTableModel implements ListModel<String> {

    private final List<PermissionAsk> permissionList;
    private final Map<String, Boolean> permissionState;
    private final List<ListDataListener> listDataListeners;

    public PermissionsAsk() {
        permissionList = new ArrayList<>();
        listDataListeners = new ArrayList<>();
        permissionState = new HashMap<>();
    }

    public void newPermMessage(PermissionAsk per) {
        permissionList.add(per);

        permissionState.put(per.getPermID(), Boolean.FALSE);
        fireAllDataChanged();
    }

    public void removePermMessage(PermissionAsk per) {
        permissionList.remove(per);
        permissionState.remove(per.getPermID());
        fireAllDataChanged();
    }

    public void setState(PermissionAsk perm, boolean state) {
        permissionState.replace(perm.getPermID(), state);

        fireAllDataChanged();
    }

    @Override
    public int getRowCount() {
        return permissionList.size();
    }

    @Override
    public int getColumnCount() {
        return 5;
    }

    @Override
    public String getColumnName(int index) {

        if (index >= 0 && index < getColumnCount()) {
            return new String[]{"Perm Type", "Sender", "Receiver", "Identity", "Accepted"}[index];
        }
        return "Unknown";
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        String value = null;
        PermissionAsk per = (PermissionAsk) permissionList.get(rowIndex);
        switch (columnIndex) {
            case 0:
                value = per.getPermissionType();
                break;
            case 1:
                value = per.getSenderPeerName();
                break;
            case 2:
                value = per.getReceiverPeerServerName();
                break;
            case 3:
                value = per.getIdentity();
                break;
            case 4:
                value = String.valueOf(permissionState.get(per.getPermID()));
        }
        return value;
    }

    //Start list model
    @Override
    public int getSize() {
        return permissionList.size();
    }

    @Override
    public String getElementAt(int index) {
        String element;
        PermissionAsk per = (PermissionAsk) permissionList.get(index);
        boolean state = permissionState.get(per.getPermID());
        element = per.getPermissionType() + "  " + per.getIdentity() + "  " + state;

        return element;
    }

    @Override
    public void addListDataListener(ListDataListener l) {
        listDataListeners.add(l);
    }

    @Override
    public void removeListDataListener(ListDataListener l) {
        listDataListeners.remove(l);
    }

    public void fireAllDataChanged() {
        ListDataEvent e = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, permissionList.size());
        listDataListeners.stream().forEach((listener) -> {
            listener.contentsChanged(e);
        });
        fireTableDataChanged();

    }

    public PermissionAsk getPermAt(int index) {
        if (index < getSize()) {
            return permissionList.get(index);
        }
        return null;
    }
}
