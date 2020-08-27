package service.chat.reslover;

import net.jxta.impl.protocol.ResolverQuery;
import net.jxta.impl.protocol.ResolverResponse;
import net.jxta.peer.PeerID;
import net.jxta.peergroup.PeerGroup;
import net.jxta.resolver.ResolverService;
import service.chat.bind.message.BindMessageQuery;
import service.chat.bind.message.BindMessageResponse;

public class BindResolver {

    public static int id = 0;

    public static void sendQuery(PeerGroup peerGroup, PeerID peerID, BindMessageQuery msg, String handlerName) {
        ResolverService rService = peerGroup.getResolverService();

        ResolverQuery query = new ResolverQuery();
        query.setHandlerName(handlerName);
        query.setCredential(null);
        query.setSrcPeer(peerGroup.getPeerID());

        query.setQuery(msg.toString());

        query.setQueryId(id++);

//                System.out.println("Query to send \n "+ query.getDocument(
//                        MimeMediaType.TEXTUTF8));
        // Query send to all peer
        if (peerID == null) {
            rService.sendQuery(null, query);
        }
        else{
            rService.sendQuery(peerID.toString(), query);
        }
    }

    public static void sendResponse(PeerGroup peerGroup, String peerID, BindMessageResponse msg, String handlerName) {
        ResolverService rService = peerGroup.getResolverService();
		// ResolverResponse response = new ResolverResponse(HandlerName, Credential,
        // QueryId, Response)
        msg.setSrcPeerID(peerGroup.getPeerID().toString());
        msg.setSrcPeerName(peerGroup.getPeerName());

        ResolverResponse response = new ResolverResponse();
        response.setHandlerName(handlerName);
        response.setCredential(null);
        response.setQueryId(id++);
        response.setResponse(msg.toString());

        rService.sendResponse(peerID, response);
    }
}
