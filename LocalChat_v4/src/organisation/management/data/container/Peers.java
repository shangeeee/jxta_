/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package organisation.management.data.container;

import java.util.ArrayList;
import java.util.List;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.table.AbstractTableModel;
import service.presence.PresenceService;

/**
 *
 * @author The Boss
 */
public class Peers extends  AbstractTableModel implements ListModel<Object>{
    private final List<ListDataListener>  listDataListeners;
    private final List<PresenceService.Presence> listPeers;

    public Peers() {
        listDataListeners = new ArrayList<>();
        listPeers = new ArrayList<>();
    }
    
    public boolean add(PresenceService.Presence presence){
        //Update only the status if always in the list
        for(PresenceService.Presence p : listPeers){
            if(p.getPeerID().equals(presence.getPeerID())){
                p.setPresence(presence.getPresence());
                fireAllDataChanged();
                return false;
            }
        }
        //Otherwise add it in the list
        listPeers.add(presence);
        fireAllDataChanged();
        return true;
    }
    
    @Override
    public int getRowCount() {
        return listPeers.size();
    }

    @Override
    public int getColumnCount() {
        return 2;//Name + presence
    }

    @Override
    public String getColumnName(int index) {

        if (index >= 0 && index < getColumnCount()) {
            return new String[]{"Name", "Status"}[index];
        }
        return "Unknown";
    }
    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        String value = null;
        PresenceService.Presence p = listPeers.get(rowIndex);
        switch(columnIndex){
            case 0: 
                value = p.getServerName();
                break;
            case 1:
                value = p.getPresence();
                break;
        }
        return value;
    }

    @Override
    public int getSize() {
        return  listPeers.size();
    }

    @Override
    public Object getElementAt(int index) {
        String ele = listPeers.get(index).getServerName();
        ele += " -- " + listPeers.get(index).getPresence();
        return ele;
    }

    @Override
    public void addListDataListener(ListDataListener l) {
        listDataListeners.add(l);
    }

    @Override
    public void removeListDataListener(ListDataListener l) {
        listDataListeners.remove(l);
    }
    
    public void fireAllDataChanged(){
        ListDataEvent e = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, listPeers.size());
        listDataListeners.stream().forEach((listener)->{listener.contentsChanged(e);});
        fireTableDataChanged();
        
    }
}
