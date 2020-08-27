/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service.groupJoin.impl.message;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import net.jxta.document.Document;
import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocumentFactory;
import net.jxta.document.StructuredTextDocument;
import net.jxta.document.TextElement;
import static service.groupJoin.IGroupJoinService.GROUP_ID_NAMESPACE;
import static service.groupJoin.IGroupJoinService.QUERY_TYPE_NAMESPACE;
import static service.groupJoin.IGroupJoinService.RECEIVER_NAMESPACE;
import static service.groupJoin.IGroupJoinService.SENDER_ID_NAMESPACE;
import static service.groupJoin.IGroupJoinService.SENDER_NAMESPACE;
import service.groupJoin.message.PermissionMessage;

/**
 *
 * @author The Boss
 */
public class PermissionAsk extends PermissionMessage{

    private static final String documentRoot = "PERMISSION-QUERY";
    private String senderPeerName;
    private String receiverPeerServerName;
    private String permissionType;
    private String groupID;
    private String senderID = "";
    private String identity ;

    public PermissionAsk(String senderPeerName, String receiverPeerServerName, String permissionType, String groupID , String desiredIDentity) {

        this.senderPeerName = senderPeerName;
        this.receiverPeerServerName = receiverPeerServerName;
        this.permissionType = permissionType;
        this.groupID = groupID;
        this.identity = desiredIDentity;
    }
    
        public PermissionAsk(String senderPeerName, String receiverPeerServerName, String permissionType, String groupID ) {
            this(senderPeerName, receiverPeerServerName, permissionType, groupID, "null");
        }


    public PermissionAsk(InputStream stream) throws IOException {
        StructuredTextDocument document = (StructuredTextDocument) StructuredDocumentFactory
                .newStructuredDocument(new MimeMediaType("text/xml"), stream);
        Enumeration<?> elements = document.getChildren();
        while (elements.hasMoreElements()) {
            TextElement element = (TextElement) elements.nextElement();
            //l'id generer est ecrasser
            if (element.getName().equals("ID")) {
                id = element.getTextValue();
                continue;
            }
            if (element.getName().equals(SENDER_NAMESPACE)) {
                senderPeerName = element.getTextValue();
                continue;
            }
            if (element.getName().equals(RECEIVER_NAMESPACE)) {
                receiverPeerServerName = element.getTextValue();
                continue;
            }
            if (element.getName().equals(QUERY_TYPE_NAMESPACE)) {
                permissionType = element.getTextValue();
                continue;
            }
            if (element.getName().equals(GROUP_ID_NAMESPACE)) {
                groupID = element.getTextValue();
                continue;
            }
            if (element.getName().equals(SENDER_ID_NAMESPACE)) {
                senderID = element.getTextValue();
                continue;
            }
            if (element.getName().equals("IDENTITY")) {
                identity = element.getTextValue();
            }
        }

    }

    @Override
    public Document getDocument(MimeMediaType asMimeType) {

        StructuredTextDocument document = (StructuredTextDocument) StructuredDocumentFactory
                .newStructuredDocument(asMimeType, documentRoot);
        TextElement element;
        
        element = document.createElement("ID", String.valueOf(id));
        document.appendChild(element);
        
        element = document.createElement(SENDER_NAMESPACE, senderPeerName);
        document.appendChild(element);

        element = document.createElement(RECEIVER_NAMESPACE, receiverPeerServerName);
        document.appendChild(element);

        element = document.createElement(QUERY_TYPE_NAMESPACE, permissionType);
        document.appendChild(element);

        element = document.createElement(GROUP_ID_NAMESPACE, groupID);
        document.appendChild(element);
        
        element = document.createElement(SENDER_ID_NAMESPACE, senderID);
        document.appendChild(element);
        
        element = document.createElement("IDENTITY", identity);
        document.appendChild(element);


        return document;
    }

    public String getSenderPeerName() {
        return senderPeerName;
    }

    public String getReceiverPeerServerName() {
        return receiverPeerServerName;
    }

    @Override
    public String getPermissionType() {
        return permissionType;
    }

    public String getGroupID() {
        return groupID;
    }

    public String getSenderID() {
        return senderID;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

    public String getIdentity() {
        return identity;
    }
    
  
    public String getPermID(){
        return id;
    }

    @Override
    public String toString() {
        return getDocument(MimeMediaType.XMLUTF8).toString();
    }
}
