/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service.groupJoin.impl.message;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import net.jxta.document.Document;
import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocumentFactory;
import net.jxta.document.StructuredTextDocument;
import net.jxta.document.TextElement;
import service.groupJoin.message.PermissionMessage;

/**
 *
 * @author The Boss
 */
public class PermissionResponse extends PermissionMessage{
    public static  final String JOINED="joined";
    public static  final String DISCARD="discard";
    private static final String documentRoot = "PermmissionResponse";
    private PermissionAsk permissionAsk;
    private String responseType;

    public PermissionResponse(PermissionAsk permissionAsk, String responseType) {
        this.permissionAsk = permissionAsk;
        this.responseType = responseType;
    }

    public  PermissionResponse(InputStream stream) throws IOException{
        StructuredTextDocument document = (StructuredTextDocument) StructuredDocumentFactory.
                newStructuredDocument(MimeMediaType.XMLUTF8, stream);
        Enumeration<?> elements = document.getChildren();
        while(elements.hasMoreElements()){
            TextElement element = (TextElement) elements.nextElement();
            if(element.getName().equals("PermissionMessage")){
                permissionAsk = new PermissionAsk(new ByteArrayInputStream(element.getTextValue().getBytes()));
                continue;
            }
            if(element.getName().equals("ResponseType")){
                responseType = element.getTextValue();
            }
        }
    }
    
    public  Document getDocument(MimeMediaType asMimeMediaType){
        StructuredTextDocument document = (StructuredTextDocument) StructuredDocumentFactory.newStructuredDocument(asMimeMediaType, documentRoot);
        TextElement element;
        
        element = document.createElement("PermissionMessage" , permissionAsk.toString());
        document.appendChild(element);
        
        element = document.createElement("ResponseType", responseType);
        document.appendChild(element);
        
        return document;
    }
    
    public PermissionAsk getPermissionAsk() {
        return permissionAsk;
    }

    public String getResponseType() {
        return responseType;
    }
    
    
    @Override
    public  String toString(){
        return getDocument(MimeMediaType.XMLUTF8).toString();
    }

    @Override
    public String getPermissionType() {
        return permissionAsk.getPermissionType();
    }
}
