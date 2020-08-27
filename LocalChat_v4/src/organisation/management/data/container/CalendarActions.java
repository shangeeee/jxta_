/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package organisation.management.data.container;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.table.AbstractTableModel;
import net.jxta.document.Element;
import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocumentFactory;
import net.jxta.document.StructuredTextDocument;
import organisation.management.protocol.calendar.CalendarAction;

/**
 *
 * @author The Boss
 */
public class CalendarActions extends AbstractTableModel implements ListModel<String> {

    public static File saveFile;
    private String savePathDir = System.getProperty("file.separator") + ".jxta" + File.separator + "calendar";
    private String saveFileName = "data";
    private static long id = 0;
    protected List<CalendarAction> datas;

    protected List<ListDataListener> listDataListeners;

    public CalendarActions(String groupName) {
        datas = new ArrayList<>();
        savePathDir = "." + groupName + savePathDir;
        saveFile = new File(savePathDir, saveFileName);

        listDataListeners = new ArrayList<>();
        //loadDatasFromFile();
    }

    private void loadDatasFromFile() {
        InputStream stream = null;
        try {
            stream = new FileInputStream(saveFile.toString());

            StructuredTextDocument doc = (StructuredTextDocument) StructuredDocumentFactory
                    .newStructuredDocument(MimeMediaType.XMLUTF8, stream);

            Enumeration<?> elements = doc.getChildren();
            Element nextElement;
            while (elements.hasMoreElements()) {
                nextElement = (Element) elements.nextElement();
                System.out.println(nextElement.toString());
                datas.add(new CalendarAction(new ByteArrayInputStream(nextElement.toString().getBytes())));
            }
        } catch (IllegalArgumentException e) {
            //May be the file is empty . ignore
        } catch (FileNotFoundException ex) {
            System.out.println("File not found ");
            try {
                stream.close();
            } catch (IOException ex1) {
                Logger.getLogger(CalendarActions.class.getName()).log(Level.SEVERE, null, ex1);
            }
        } catch (IOException ex) {
            System.out.println(ex.toString());

        }
    }

    /**
     *
     */
    public void saveDataToFile() {
        FileOutputStream file;
        try {
            System.out.println(saveFile.isFile());
            file = new FileOutputStream(saveFile.toString());
            for (CalendarAction data : datas) {
                file.write(data.getDocument(MimeMediaType.XMLUTF8).getValue().getBytes());
            }
        } catch (FileNotFoundException ex) {
            System.out.println("save datas : File not found");
        } catch (IOException ex) {
            Logger.getLogger(CalendarActions.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    

    public static long getID() {
        return id;
    }

    protected CalendarAction findData(String id) {
        for (CalendarAction data : datas) {
            if (data.getId().compareTo(id) == 0) {
                return data;
            }
        }
        return null;
    }

    public void registerAction(CalendarAction action) throws IOException, Exception {

        System.out.println("new action : " + action.getActionText());
        CalendarAction.ActionType type = CalendarAction.stringToAction(action.getType());
        if (type == CalendarAction.ActionType.ADD) {
            addAction(action);
        } else if (type == CalendarAction.ActionType.DO) {
            doAction(action);
        } else if (type == CalendarAction.ActionType.MODIFY) {
            modifyAction(action);
        } else if (type == CalendarAction.ActionType.REMOVE) {
            removeAction(action);
        } else {
            throw new Exception("Unable to register action");
        }

        fireAllDataChanged();

//        System.out.println("Datas upddated ............................START.");
//        datas.stream().forEach((a) -> {
//            System.out.println(a.getId() + "  " + a.getActionText() + "  " + a.isDone() + "     ==> " + a.getStringDate());
//        });
//        System.out.println("Datas upddated .............................END.");
    }

    /**
     * Add a new action to the calendar
     *
     * @param action
     * @throws java.io.IOException
     */
    private void addAction(CalendarAction action) throws IOException {
       
        CalendarAction data = new CalendarAction(CalendarAction.ActionType.ADD);

        data.setId(action.getId());
        data.setDone(false);
        data.setActionText(action.getActionText());

        datas.add(data);
    }

    /**
     * Modify an action from the calendar
     *
     * @param action
     * @return
     */
    private void modifyAction(CalendarAction action) {
        CalendarAction data = findData(action.getId());
        if (data == null) {
            return;
        }

        data.setActionText(action.getActionText());
        data.setDate(action.getDate());
        data.setDone(action.isDone());

    }

    /**
     * Remove a planed action from the calendar
     *
     * @param action
     */
    private void removeAction(CalendarAction action) {
        CalendarAction data = findData(action.getId());
        if (data != null) {
            datas.remove(data);
        }
    }

    /**
     * Changed the action status to done
     *
     * @param action
     */
    private void doAction(CalendarAction action) {

        CalendarAction data = findData(action.getId());
        if (data != null) {
            data.setDone(action.isDone());
        }
    }

    //Start table model
    @Override
    public int getRowCount() {
        return datas.size();
    }

    @Override
    public int getColumnCount() {
        return 4;
    }

    @Override
    public String getColumnName(int index) {

        if (index >= 0 && index < getColumnCount()) {
            return new String[]{"ID", "Date", "Plan", "Done"}[index];
        }
        return "Unknown";
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        CalendarAction action = datas.get(rowIndex);
        Object value = null;
        if (action != null) {
            if (columnIndex == 0) {
                value = String.valueOf(action.getId());
            } else if (columnIndex == 1) {
                value = String.valueOf(action.getStringDate());
            } else if (columnIndex == 2) {
                value = action.getActionText();
            } else if (columnIndex == 3) {
                value = action.isDone();
            }
        }
        return value;
    }

    ///Start List model
    @Override
    public int getSize() {
        return datas.size();
    }

    @Override
    public String getElementAt(int index) {
        CalendarAction action = datas.get(index);
        String element;
        element = action.getDate() + "  \n";
        element += action.getActionText() + " ==> IS DONE : ";
        element += action.isDone();

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
        ListDataEvent e = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, datas.size());
        listDataListeners.stream().forEach((listener) -> {
            listener.contentsChanged(e);
        });
        fireTableDataChanged();

    }
    
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return CalendarAction.class;
    }
    
    public CalendarAction getColumn(int columnIndex) {
        return datas.get(columnIndex);
    }
}
