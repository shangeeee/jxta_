package service.chat;

import java.io.IOException;
import net.jxta.document.Advertisement;
import net.jxta.peergroup.PeerGroup;
import net.jxta.service.Service;
import service.chat.impl.message.ChatMessageSend;

public interface IChatService extends Service {

    public void init(String serverName, PeerGroup peerGroup) throws IOException;

    public boolean send(String serverName, ChatMessageSend message) throws
            IOException;

    public void sendInGroup(ChatMessageSend message) throws IOException;

    public Advertisement getUnicastAdv();

    public Advertisement buildPropagatePipe();
}
