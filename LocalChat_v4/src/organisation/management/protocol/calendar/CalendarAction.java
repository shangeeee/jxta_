package organisation.management.protocol.calendar;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Enumeration;
import java.util.UUID;
import net.jxta.document.Element;
import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocumentFactory;
import net.jxta.document.StructuredTextDocument;

/**
 *
 * @author The Boss
 */
public class CalendarAction {

    public enum ActionType {

        ADD, MODIFY, REMOVE, DO
    }

    public static final String docRoot = "Calendar-Action";
    private static final String ID = "Id";
    private static final String DATE = "Date";
    private static final String ACTION_TEXT = "ActionText";
    private static final String DONE = "Done";

    private String id ;
    private LocalDateTime date;
    private String actionText;
    private boolean done;
    private ActionType type;

    public CalendarAction(ActionType type) {
        super();
        id = UUID.randomUUID().toString();
        this.type = type;
        date = LocalDateTime.now();
        
        if(type == ActionType.DO) done = true;
        if(type == ActionType.ADD) done = false;
    }

    public CalendarAction(InputStream stream) throws IOException {
        this(ActionType.ADD); //Must changes later
        StructuredTextDocument doc = (StructuredTextDocument) StructuredDocumentFactory.newStructuredDocument(MimeMediaType.XMLUTF8, stream);
        Enumeration<?> elements = doc.getChildren();
        Element element;
        while (elements.hasMoreElements()) {
            element = (Element) elements.nextElement();

            if (element.getKey().equals(ID)) {
                id =  (String) element.getValue();
                continue;
            }
            if (element.getKey().equals(DATE)) {
                String creationDateTime = (String) element.getValue();
                
                date = LocalDateTime.parse(creationDateTime);
                continue;
            }
            if (element.getKey().equals(ACTION_TEXT)) {
                actionText = (String) element.getValue();
                continue;
            }
            if (element.getKey().equals(DONE)) {
                done = Boolean.valueOf((String) element.getValue());
            }
            
            String actionType = doc.getKey().substring(doc.getKey().lastIndexOf("-")+1);
            type = stringToAction(actionType);
     
        }
    }

    public StructuredTextDocument getDocument(MimeMediaType asMediaType) {
        StructuredTextDocument  doc = (StructuredTextDocument) StructuredDocumentFactory.
                    newStructuredDocument(asMediaType, docRoot+"-" + stringAction(type));
        
        Element element;

        element = doc.createElement(ID, String.valueOf(id));
        doc.appendChild(element);

        element = doc.createElement(DATE, String.valueOf(date.format(DateTimeFormatter.ISO_DATE_TIME)));
        doc.appendChild(element);

        element = doc.createElement(ACTION_TEXT, actionText);
        doc.appendChild(element);

        element = doc.createElement(DONE, String.valueOf(done));
        doc.appendChild(element);

        
        return doc;
    }

    @Override
    public String toString() {
        return getDocument(MimeMediaType.XMLUTF8).toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getActionText() {
        return actionText;
    }

    public void setActionText(String actionText) {
        this.actionText = actionText;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public void setType(ActionType type) {
        this.type = type;
    }

    
    public String getType() {
        return stringAction(type);
    }
    
    public String getStringDate(){
        return  date.format(DateTimeFormatter.ISO_DATE_TIME);
    }
    

    public static String stringAction(ActionType type){
        String s = null;
        if (type == ActionType.ADD) {
          s =  "ADD" ;
        }
        else if(type == ActionType.MODIFY){
            s = "MODIFY";
        }
        else if(type == ActionType.REMOVE){
            s="REMOVE";
        }
        else if (type == ActionType.DO){
            s =  "DO";
        }
        return s;
    }
    
    public static ActionType stringToAction(String action){
        switch (action) {
            case "ADD":
                return ActionType.ADD;
            case "MODIFY":
                return ActionType.MODIFY;
            case "REMOVE":
                return ActionType.REMOVE;
            case "DO":
                return ActionType.DO;
        }
        
        return  null;
    }
}
