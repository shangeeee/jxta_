package service.chat.event;

import service.chat.impl.message.ChatMessageReceiv;
import java.util.Date;
import java.util.EventObject;

public class ChatMessageEvent extends EventObject {
	private static final long serialVersionUID = 1L;

	private ChatMessageReceiv message;
	private Date receivedDate;

        /**
         * 
         * @param source Pipe msg event
         * @param message 
         */
	public ChatMessageEvent(Object source, ChatMessageReceiv message) {
		super(source);
		this.message = message;
               
		receivedDate = new Date(System.currentTimeMillis());
                
	}

	public ChatMessageReceiv getMessage() {
		return message;
	}

	public Date getReceivedDate() {
		return receivedDate;
	}
}
