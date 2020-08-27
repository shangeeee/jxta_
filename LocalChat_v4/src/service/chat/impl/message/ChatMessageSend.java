package service.chat.impl.message;

import java.util.Date;

import service.chat.message.IChatMessage;
import net.jxta.content.ContentID;
import net.jxta.endpoint.Message;
import net.jxta.endpoint.StringMessageElement;
import net.jxta.id.IDFactory;
import net.jxta.peergroup.PeerGroupID;

public class ChatMessageSend extends Message implements IChatMessage {
	private String sender;
	private String messageContent;
	private String sendDate;
	private String receivDate;
	private String id;

	public ChatMessageSend(PeerGroupID peerGroupID,String sender, String messageContent) {
		super();
		this.sender = sender;
		this.messageContent = messageContent;
		sendDate = new Date(System.currentTimeMillis()).toString();
		receivDate = null;

		StringMessageElement element;
		// The message id
		ContentID msgID = IDFactory.newContentID(peerGroupID, true);
		element = new StringMessageElement(ID, msgID.toString(), null);
		addMessageElement(element);

		// The sender info
		element = new StringMessageElement(SENDER_INFO, sender, null);
		addMessageElement(element);

		// Send time
		element = new StringMessageElement(SEND_DATE, sendDate, null);
		addMessageElement(element);

		// Receive time -- unknown
		element = new StringMessageElement(RECEIV_DATE, "sending ...", null);
		addMessageElement(element);

		// Message content
		element = new StringMessageElement(CONTENT, messageContent, null);
		addMessageElement(element);
	}

	public String getMessageContent() {
		return messageContent;
	}

	public String getSendDate() {
		return sendDate;
	}
        

}
