package service.chat.bind;

import net.jxta.peergroup.PeerGroup;
import net.jxta.protocol.ResolverQueryMsg;
import net.jxta.protocol.ResolverResponseMsg;
import net.jxta.resolver.QueryHandler;

public abstract class BindQueryHandler implements QueryHandler {

	public static final String DEFAULT_HANDLER_NAME = "bind-handler";

	@Override
	public abstract int processQuery(ResolverQueryMsg query);

	@Override
	public abstract void processResponse(ResolverResponseMsg response);

}
