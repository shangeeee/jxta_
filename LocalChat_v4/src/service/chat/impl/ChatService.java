package service.chat.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.jxta.document.Advertisement;
import net.jxta.document.AdvertisementFactory;
import net.jxta.exception.PeerGroupException;
import net.jxta.id.ID;
import net.jxta.id.IDFactory;
import net.jxta.peer.PeerID;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.pipe.OutputPipe;
import net.jxta.pipe.PipeID;
import net.jxta.pipe.PipeMsgEvent;
import net.jxta.pipe.PipeMsgListener;
import net.jxta.pipe.PipeService;
import net.jxta.protocol.PipeAdvertisement;
import net.jxta.protocol.ResolverQueryMsg;
import net.jxta.protocol.ResolverResponseMsg;
import net.jxta.resolver.ResolverService;
import net.jxta.util.JxtaBiDiPipe;
import net.jxta.util.JxtaServerPipe;
import net.jxta.util.PipeEventListener;
import net.jxta.util.PipeStateListener;
import net.jxta.util.ServerPipeAcceptListener;
import service.chat.IChatService;
import service.chat.IChatServiceListener;
import service.chat.bind.BindQueryEvent;
import service.chat.bind.BindQueryHandler;
import service.chat.bind.BindQueryListener;
import service.chat.bind.message.BindMessageQuery;
import service.chat.bind.message.BindMessageResponse;
import service.chat.event.ChatMessageEvent;
import service.chat.impl.message.ChatMessageReceiv;
import service.chat.impl.message.ChatMessageSend;
import service.chat.reslover.BindResolver;
import services.controller.CustomGroup;

public class ChatService extends BindQueryHandler implements IChatService {

    private final int DEFAULT_PIPE_TIMEOUT = 3000;
    public static String UNKNOWN_PEER = "UNKNOWN_PEER";

    private CustomGroup controller;

    private final String username = "ange";
    private final String password = "ange";
    private String serviceName;
    private PeerGroup peerGroup;
    private PipeAdvertisement propagateAdv;
    private PipeService pipeService;

    private final PropagatePipeListener servicePropagateListener;

    private OutputPipe propagateOutputPipe = null;
    /**
     * serviceName => bidiPipe
     */
    private final Map<String, JxtaBiDiPipe> approvedConnections;
    /**
     * ServiceName => list of chat service listener
     */
    private final Map<String, List<IChatServiceListener>> approvedConnectionListeners;

    /**
     * Server to bind name => Bind listener
     */
    private final Map<String, BindQueryListener> remainingBindQueryListener;

    /**
     * Listener registered for receiving a propagate / group meassage
     */
    private final List<IChatServiceListener> registeredPropagateListener;

    public ChatService() {
        super();
        approvedConnections = Collections.synchronizedMap(new HashMap<>());
        approvedConnectionListeners = Collections.synchronizedMap(
                new HashMap<>());
        remainingBindQueryListener = Collections.synchronizedMap(
                new HashMap<>());
        registeredPropagateListener = Collections.synchronizedList(
                new ArrayList<>());
        servicePropagateListener = new PropagatePipeListener();
    }

    public CustomGroup getController() {
        return controller;
    }

    public void setController(CustomGroup controller) {
        this.controller = controller;
    }

    @Override
    public void init(String serviceName, PeerGroup peerGroup) throws IOException {
        this.peerGroup = peerGroup;
        this.serviceName = serviceName;
        pipeService = peerGroup.getPipeService();
    }

    @Override
    public int startApp(String[] strings) {
        System.out.println("Starting chat Service ...");
        ResolverService resolver = peerGroup.getResolverService();
        resolver.registerHandler(DEFAULT_HANDLER_NAME, this);
        buildPropagatePipe();

        return 0;
    }

    @Override
    public void stopApp() {
        System.out.println("Stoping chat Service ...");
        ResolverService resolver = peerGroup.getResolverService();
        resolver.unregisterHandler(DEFAULT_HANDLER_NAME);
    }

    /**
     *
     * @return always the same id for the pipe adv basing on the peer group id
     * and it name so not need to get a remonte or local pipe adv
     */
    @Override
    public Advertisement buildPropagatePipe() {
        if (propagateAdv == null) {
            PipeID MyPipeID = IDFactory.newPipeID(
                    peerGroup.getPeerGroupID(), peerGroup.getPeerGroupName()
                    .getBytes());
            propagateAdv = (PipeAdvertisement) AdvertisementFactory
                    .newAdvertisement(PipeAdvertisement.getAdvertisementType());
            propagateAdv.setPipeID(MyPipeID);
            propagateAdv.setType(PipeService.PropagateType);

            try {
                //Create an input pipe for receiving all propagate message
                System.out.println(
                        "Creating an input pipe to receive propagate message ...");
                pipeService.createInputPipe(propagateAdv,
                        servicePropagateListener);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return propagateAdv;
    }

    @Override
    public Advertisement getUnicastAdv() {
        // Creating a Pipe Advertisement
        PipeAdvertisement unicastAdv = (PipeAdvertisement) AdvertisementFactory
                .newAdvertisement(PipeAdvertisement.getAdvertisementType());
        PipeID MyPipeID = IDFactory.newPipeID(PeerGroupID.defaultNetPeerGroupID);

        unicastAdv.setPipeID(MyPipeID);
        unicastAdv.setType(PipeService.UnicastType);
        unicastAdv.setName("BidiPipe");
        unicastAdv.setDescription("Created by " + serviceName);

        return unicastAdv;
    }

    public synchronized void addChatUnicastListener(String serviceName,
                                                    IChatServiceListener chatListener) {
        List<IChatServiceListener> chatListeners = approvedConnectionListeners
                .get(serviceName);
        if (chatListeners != null) {
            chatListeners.add(chatListener);
        } else {
            chatListeners = new ArrayList<>();
            chatListeners.add(chatListener);
            approvedConnectionListeners.put(serviceName, chatListeners);
        }
    }

    public synchronized void addChatPropagateListener(
            IChatServiceListener chatListener) {
        registeredPropagateListener.add(chatListener);
    }

    @Override
    public boolean send(String serviceName, ChatMessageSend message) throws
            IOException {

        System.out.println("'" + this.serviceName
                + "' wants to send a message to '" + serviceName + "'");
        //Check whether a recent connection has been done to this server
        JxtaBiDiPipe recentBidiPipe = approvedConnections.get(serviceName);

        if (recentBidiPipe != null) {
            System.out.println(
                    "A recent connection exists to this server.\nUse it Instead");
            System.out.println("Sending the message");
            recentBidiPipe.sendMessage(message);
            return true;
        }
        System.out.println( "A recent connection does not exists to this server.\nSending a bing query");
        //send bind qurery 
        BindMessageQuery bindQuery = new BindMessageQuery(serviceName,
                this.serviceName,
                username, password, BindMessageQuery.unicast);
        PeerID peerID;
        int nbTry = 0;
        do {
            if(nbTry == 100) return false;
            peerID = controller.getServerNameService().get(serviceName);
            System.out.println("Id associate to the servername is " + peerID);
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
             nbTry++;
            }
        }while(peerID == null);

        BindResolver.sendQuery(peerGroup, peerID, bindQuery, DEFAULT_HANDLER_NAME);
        //wait for the answer (in a listener)
        BindQueryListener queryListener = new BindQueryListener() {
            @Override
            public void bindQueryAccepted(BindQueryEvent bindEvent) {
                try {
                    System.out.println("Bind query accepted ");
                    System.out.println("Prepararing to send the message");
                    BindMessageResponse bindResponse = bindEvent
                            .getbindResponse();
                    if (bindResponse == null) {
                        return;
                    }
                    PipeAdvertisement pipeAdv = (PipeAdvertisement) bindResponse
                            .getPipeAdv();
                    String pipeID = pipeAdv.getPipeID().toString();

                    JxtaBiDiPipe bidiPipe = null;
                    do {
                        System.out.println(
                                "The bidipipe associated is not approved yet \nRetrying ... => "
                                + serviceName);
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException ex) {

                        }
                        bidiPipe = approvedConnections.get(serviceName);
                    } while (bidiPipe == null);
                    System.out.println(
                            "The bidipipe associated is now approved");
                    approvedConnections.put(serviceName, bidiPipe);
                    System.out.println(
                            "Sending the message to the server : "
                            + serviceName);
                    bidiPipe.sendMessage(message);

                } catch (IOException ex) {
                    Logger.getLogger(ChatService.class
                            .getName())
                            .log(Level.SEVERE, null, ex);
                }
            }
        };

        remainingBindQueryListener.put(serviceName, queryListener);
        return true;
    }

    @Override
    public void sendInGroup(ChatMessageSend message) throws IOException {
        pipeService = peerGroup.getPipeService();
        if (propagateOutputPipe == null || propagateOutputPipe.isClosed()) {
            System.out.println("Creating a new propagate output pipe ");
            propagateOutputPipe = pipeService.createOutputPipe(propagateAdv,
                    JxtaServerPipe.DEFAULT_TIMEOUT);
        }
        System.out.println("Ready to send the message\n");

        if (!propagateOutputPipe.send(message)) {
            System.out.println("An error moccur while sending the message !!!");
        }
    }

    @Override
    public int processQuery(ResolverQueryMsg query) {

        try {
            BindMessageQuery bindQuery = new BindMessageQuery(
                    new ByteArrayInputStream(query.getQuery().getBytes()));
            BindMessageResponse bindResponse = null;
            System.out.println("Bind query received from " + bindQuery
                    .getsenderName());
            //check server name
            if (!bindQuery.getServiceName().equals(this.serviceName)) {
                System.out.println("Incorrect server name : " + bindQuery
                        .getServiceName());
                System.out.println("The correct one : " + this.serviceName);

                return 1;
            }
            System.out.println("Correct server name : " + this.serviceName);
            //Create a pipe adv

            PipeAdvertisement pipeAdv = null;
            String pipeID = null;
            //create bidi pipe
            if (bindQuery.getBindType().equals(BindMessageQuery.propagate)) {
                System.out
                        .println("Why he didn't build his own propagate avd ?");
                //pipeAdv = propagateAdv;

            } else {
                System.out.println("It is an unicast advertisment");
                UnicastListener unicastListener = new UnicastListener(bindQuery
                        .getsenderName());
                pipeAdv = (PipeAdvertisement) getUnicastAdv();
                System.out.println("Create the bidi pipe to communicate with "
                        + bindQuery.getsenderName());

                // send the adv
                System.out.println("Send the unicast pipe advertisement");
                bindResponse = new BindMessageResponse(serviceName, pipeAdv,
                        BindMessageQuery.unicast);
                BindResolver.sendResponse(peerGroup, query.getSrcPeer()
                        .toString(), bindResponse, DEFAULT_HANDLER_NAME);

                new JxtaServerPipe(peerGroup, pipeAdv, unicastListener);
            }
        } catch (IOException ex) {

        }

        return 0;
    }

    @Override
    public void processResponse(ResolverResponseMsg response) {
        System.out.println("Bind query response received ");
        System.out.println("Processing ...");

        try {
            BindMessageResponse bindResponse = new BindMessageResponse(
                    new ByteArrayInputStream(response.getResponse().getBytes()));
            //get the avd
            PipeAdvertisement pipeAdv = (PipeAdvertisement) bindResponse
                    .getPipeAdv();
            PeerID peerID = (PeerID) IDFactory.fromURI(new URI(bindResponse
                    .getSrcPeerID()));
            //create bidi pipe
            UnicastListener unicastListener = new UnicastListener(bindResponse
                    .getServiceName());
            //pipeService.createOutputPipe(pipeAdv, unicastListener);
            JxtaBiDiPipe bidiPipe = new JxtaBiDiPipe();
            bidiPipe.setReliable(true);
            bidiPipe.setPipeEventListener(unicastListener);
            bidiPipe.setMessageListener(unicastListener);

            System.out.println("Connection .....");
            bidiPipe.connect(peerGroup, peerID, pipeAdv, JxtaServerPipe.DEFAULT_TIMEOUT,
                    unicastListener);
            while (!bidiPipe.isBound()) {
                Thread.sleep(50);
            }
            System.out.println("new Approved bidi pipe     => " + bindResponse
                    .getServiceName());
            // A new bind query accepted event
            approvedConnections.put(bindResponse.getServiceName(), bidiPipe);

            String serverRespondingName = bindResponse.getServiceName();
            BindQueryEvent event = new BindQueryEvent(serverRespondingName,
                    bindResponse);

            BindQueryListener bindListerner = remainingBindQueryListener
                    .remove(serverRespondingName);
            if (bindListerner != null) {
                bindListerner.bindQueryAccepted(event);
            }

        } catch (IOException ex) {
            Logger.getLogger(ChatService.class
                    .getName())
                    .log(Level.SEVERE, null,
                            ex);
        } catch (URISyntaxException ex) {
            Logger.getLogger(ChatService.class
                    .getName())
                    .log(Level.SEVERE, null,
                            ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(ChatService.class
                    .getName())
                    .log(Level.SEVERE, null,
                            ex);
        }

    }

    private class UnicastListener implements PipeMsgListener,
            PipeEventListener, EventListener, ServerPipeAcceptListener {

        String serviceName;

        public UnicastListener(String serviceName) {
            this.serviceName = serviceName;
        }

        @Override
        public void pipeMsgEvent(PipeMsgEvent event) {
            //Message received
            ChatMessageReceiv receivedMsg = new ChatMessageReceiv(event);
            PipeID pipeID = event.getPipeID();
            JxtaBiDiPipe pipeSource = approvedConnections.get(receivedMsg
                    .getSender());
            System.out.println("New Message  received sender is " + receivedMsg
                    .getSender());

            ChatMessageEvent chatEvent = new ChatMessageEvent(event,
                    receivedMsg);
            List<IChatServiceListener> chatListeners = approvedConnectionListeners
                    .get(receivedMsg.getSender());
            //Prevent IChat listeners
            System.out.println("Trying to prevent all listener associate to '"
                    + receivedMsg.getSender() + "'");
            if (chatListeners != null) {
                System.out.println("Preventing all chat listener");
                chatListeners.stream().forEach((chatListener) -> {
                    chatListener.processNewMessage(chatEvent);
                });
            } else {
                System.out.println("No chat listener is associate to this sender ...");
                List<IChatServiceListener> unknownPeerChatListeners = approvedConnectionListeners.get(UNKNOWN_PEER);
                if (unknownPeerChatListeners != null) {
                    unknownPeerChatListeners.stream().forEach((listener) -> {
                        listener.processNewMessage(chatEvent);
                    });
                }
            }

        }

        @Override
        public void pipeEvent(int event) {
            System.out.println("a new  pipe pipe event : " + event);
            if (event == PipeStateListener.PIPE_CLOSED_EVENT) {
                System.out.println("Pipe closed .........................");

            }
            if (event == PipeStateListener.PIPE_OPENED_EVENT) {
                System.out.println("Pipe open ......................");

            }
        }

        @Override
        public void serverPipeClosed() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void pipeAccepted(JxtaBiDiPipe bidipipe) {

            bidipipe.setPipeEventListener(this);
            bidipipe.setMessageListener(this);

            System.out.println(
                    "Bidi pipe can now to receive messages ...................................."
                    + serviceName);
            approvedConnections.put(serviceName, bidipipe);
        }

    }

    private class PropagatePipeListener implements PipeMsgListener {

        @Override
        public void pipeMsgEvent(PipeMsgEvent event) {
            System.out.println("A new group message !!!");
            ChatMessageReceiv message = new ChatMessageReceiv(event);
            ChatMessageEvent chatEvent = new ChatMessageEvent(event, message);
            registeredPropagateListener.stream().forEach((groupListener) -> {
                groupListener.processNewMessage(chatEvent);
            });
        }
    }

    @Override
    @Deprecated
    public Advertisement getImplAdvertisement() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    @Deprecated
    public void init(PeerGroup pg, ID id, Advertisement a) throws
            PeerGroupException {
        throw new UnsupportedClassVersionError("Use int(PeerGroup) instead.");
    }
}
