/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service.groupJoin.message;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import net.jxta.document.Document;
import net.jxta.document.MimeMediaType;

/**
 *
 * @author The Boss
 */
public abstract class PermissionMessage {

    protected String id;

    public PermissionMessage() {
        id = UUID.nameUUIDFromBytes(LocalDate.now().format(DateTimeFormatter.ISO_DATE).getBytes()).toString();
    }

    public abstract Document getDocument(MimeMediaType asMimeMediaType);

    public abstract String getPermissionType();
    
}
