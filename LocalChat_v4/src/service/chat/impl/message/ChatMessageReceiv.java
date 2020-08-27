package service.chat.impl.message;

import java.util.Date;

import service.chat.message.IChatMessage;
import net.jxta.document.MimeMediaType;
import net.jxta.endpoint.Message;
import net.jxta.endpoint.Message.ElementIterator;
import net.jxta.endpoint.MessageElement;
import net.jxta.pipe.PipeMsgEvent;

public class ChatMessageReceiv  implements IChatMessage {

    private String messageContent;
    private String sendDate;
    private final String receivDate;
    private final Message message;

    private String sender;

    public ChatMessageReceiv(PipeMsgEvent ev) {
        super();
        receivDate = new Date(System.currentTimeMillis()).toString();
        message = ev.getMessage();
       
        if (message != null) {
            ElementIterator elements = message.getMessageElements(
                    MimeMediaType.XMLUTF8);
            while (elements.hasNext()) {
                MessageElement element = elements.next();
                System.out.println(new String(element.getBytes(true)));
            }
            // System.out.println(message.);
            sender = message.getMessageElement(SENDER_INFO).toString();
            // addMessageElement(message.getMessageElement(SENDER_INFO));

            sendDate = message.getMessageElement(SEND_DATE).toString();
            // addMessageElement(message.getMessageElement(SEND_DATE));

            messageContent = message.getMessageElement(CONTENT).toString();
            // addMessageElement(message.getMessageElement(CONTENT));

            message.getMessageElement(RECEIV_DATE).setElementProperty(
                    RECEIV_DATE, receivDate);
            // addMessageElement(getMessageElement(RECEIV_DATE));

        } else {
            System.out.println("Incorrect message form ...");
        }

    }

    public String getMessageContent() {
        return messageContent;
    }

    public String getSendDate() {
        return sendDate;
    }

    public String getReceivDate() {
        return receivDate;
    }

    public String getSender() {
        return sender;
    }

    @Override
    public String toString() {
        return message.toString();
    }

}
