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
import services.controller.CustomGroup;

/**
 *
 * @author The Boss
 */
public class CustomGroupModel extends AbstractTableModel implements ListModel<Object>{

    List<ListDataListener> listDataListeners ;
    List<CustomGroup> listCustomGroup;

    public CustomGroupModel() {
        listDataListeners = new ArrayList<>();
        listCustomGroup = new ArrayList<>();
    }
    
    public boolean addCustomGroup(CustomGroup customGroup){
        for(CustomGroup cg : listCustomGroup){
            if(cg.getGroup().getPeerGroupName().equals(customGroup.getGroup().getPeerGroupName())){
                //change
                listCustomGroup.remove(cg);
                listCustomGroup.add(customGroup);
                fireAllDataChanged();
                return false;
            }
        }
        listCustomGroup.add(customGroup);
        fireAllDataChanged();
        return true;
    }
    
     @Override
    public String getColumnName(int index) {
        //we can add started services later
        if (index >= 0 && index < getColumnCount()) {
            return new String[]{"Group Name", "Role"}[index];
        }
        return "Unknown";
    }
    
    @Override
    public int getRowCount() {
        return listCustomGroup.size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        String value = null;
        if(rowIndex < getRowCount()){
            CustomGroup cg = listCustomGroup.get(rowIndex);
            switch(columnIndex){
                case 0:
                    value = cg.getGroup().getPeerGroupName();
                    break;
                case 1:
                    value = cg.getRole();
                    break;
            }
        }
        return value;
    }

    @Override
    public int getSize() {
        return listCustomGroup.size();
    }

    @Override
    public Object getElementAt(int index) {
        if(index < getSize()){
            return listCustomGroup.get(index).getGroup().getPeerGroupName();
        }
        return null;
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
        ListDataEvent e = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, listCustomGroup.size());
        listDataListeners.stream().forEach((listener) -> {
            listener.contentsChanged(e);
        });
        fireTableDataChanged();

    }
}
