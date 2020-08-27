package service.chat;

import java.util.EventListener;
import service.chat.event.ChatMessageEvent;

public interface IChatServiceListener extends EventListener{

	public void processNewMessage(ChatMessageEvent chatEvent);

}
