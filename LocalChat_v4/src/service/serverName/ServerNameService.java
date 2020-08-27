/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service.serverName;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.jxta.document.Advertisement;
import net.jxta.document.Document;
import net.jxta.document.Element;
import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocumentFactory;
import net.jxta.document.StructuredTextDocument;
import net.jxta.document.TextElement;
import net.jxta.exception.PeerGroupException;
import net.jxta.id.ID;
import net.jxta.id.IDFactory;
import net.jxta.impl.protocol.ResolverQuery;
import net.jxta.impl.protocol.ResolverResponse;
import net.jxta.peer.PeerID;
import net.jxta.peergroup.PeerGroup;
import net.jxta.protocol.ResolverQueryMsg;
import net.jxta.protocol.ResolverResponseMsg;
import net.jxta.resolver.QueryHandler;
import net.jxta.service.Service;

/**
 *
 * @author The Boss
 */
public class ServerNameService extends Thread implements QueryHandler, Service {

    private final String handlerName = "ServerNameService";
    private final Map<String, PeerID> serverNameToPeerID;
    private final Map<PeerID, String> peerIDToServerName;

    private PeerGroup group;
    private String myServerName;
    private boolean serviceIsRunning;
    private long refreshFrequency = 1000 * 60 * 1;//1min
    
    private Advertisement implAdv;

    public ServerNameService() {
        serverNameToPeerID = Collections.synchronizedMap(new HashMap<>());
        peerIDToServerName = Collections.synchronizedMap(new HashMap<>());
    }

    private void addEntry(String serverName, PeerID peerID) {
        serverNameToPeerID.put(serverName, peerID);
        peerIDToServerName.put(peerID, serverName);
    }

    public PeerID get(String serverName) {
        return serverNameToPeerID.get(serverName);
    }

    public String get(PeerID peerID) {
        return peerIDToServerName.get(peerID);
    }
    

    @Override
    public int processQuery(ResolverQueryMsg query) {
        ResolverData resDatas;
        try {

            resDatas = new ResolverData(new ByteArrayInputStream(query.getQuery().getBytes()));
            //System.out.println("New res data query  \n" + resDatas);
            PeerID peerID = (PeerID) IDFactory.fromURI(new URI(resDatas.getPeerID()));

            addEntry(resDatas.getServerName(), peerID);

            ResolverResponse res = new ResolverResponse();
            res.setHandlerName(handlerName);

            res.setResponse(new ResolverData(group.getPeerID().toString(), myServerName).toString());

            //System.out.println("Entry len : " + serverNameToPeerID.keySet().size() + "  New res data response \n" + resDatas);

            group.getResolverService().sendResponse(resDatas.getPeerID(), res);
            return 0;

        } catch (URISyntaxException ex) {
            Logger.getLogger(ServerNameService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ServerNameService.class.getName()).log(Level.SEVERE, null, ex);
        }

        return 1;
    }

    @Override
    public void processResponse(ResolverResponseMsg response) {
        ResolverData resDatas;
        try {

            resDatas = new ResolverData(new ByteArrayInputStream(response.getResponse().getBytes()));

            PeerID peerID = (PeerID) IDFactory.fromURI(new URI(resDatas.getPeerID()));

            addEntry(resDatas.getServerName(), peerID);

            //System.out.println("Entry len : " + serverNameToPeerID.keySet().size() + "  New res data response \n" + resDatas);

        } catch (URISyntaxException ex) {
            Logger.getLogger(ServerNameService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ServerNameService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String getMyServerName() {
        return myServerName;
    }

    public void setMyServerName(String myServerName) {
        this.myServerName = myServerName;
    }

    @Override
    public Advertisement getImplAdvertisement() {
        return implAdv;
    }

    @Override
    public void init(PeerGroup group, ID assignedID, Advertisement implAdv) throws PeerGroupException {
        this.group = group;
        this.implAdv = implAdv;
    }

    @Override
    public int startApp(String[] args) {
        if (myServerName == null) {
            throw new IllegalStateException(this.getClass().getSimpleName() + " myServerName is not set");
        }
        group.getResolverService().registerHandler(handlerName, this);
        serviceIsRunning = true;
        start();
        return START_OK;
    }

    @Override
    public void run() {
        while (serviceIsRunning) {
            try {
                ResolverQuery getDatas = new ResolverQuery();
                ResolverData myData = new ResolverData(group.getPeerID().toString(), myServerName);
                //System.out.println("Name Service : sending query " + myData);
                getDatas.setQuery(myData.toString());
                getDatas.setHandlerName(handlerName);
                getDatas.setSrcPeer(group.getPeerID());
                group.getResolverService().sendQuery(null, getDatas);
                sleep(refreshFrequency);
            } catch (InterruptedException ex) {
                //
            }
        }
    }

    @Override
    public void stopApp() {
        serviceIsRunning = false;
        group.getResolverService().unregisterHandler(handlerName);

    }

    private class ResolverData {

        private String doctype = "ServerNameService";
        private String peerID;
        private String serverName;

        public ResolverData(String peerID, String serverName) {
            this.peerID = peerID;
            this.serverName = serverName;
        }

        public ResolverData(InputStream stream) throws IOException {
            StructuredTextDocument doc = (StructuredTextDocument) StructuredDocumentFactory.
                    newStructuredDocument(MimeMediaType.XMLUTF8, stream);
            Enumeration<?> elements = doc.getChildren();
            TextElement element;
            while (elements.hasMoreElements()) {
                element = (TextElement) elements.nextElement();
                if (element.getKey().equals("PeerID")) {
                    peerID = element.getValue();
                    continue;
                }
                if (element.getKey().equals("ServerName")) {
                    serverName = element.getValue();
                }
            }
        }

        public Document getDocument() {

            StructuredTextDocument doc = (StructuredTextDocument) StructuredDocumentFactory.
                    newStructuredDocument(MimeMediaType.XMLUTF8, doctype);
            Element element = doc.createElement("PeerID", peerID);
            doc.appendChild(element);

            element = doc.createElement("ServerName", serverName);
            doc.appendChild(element);

            return doc;
        }

        public String getPeerID() {
            return peerID;
        }

        public String getServerName() {
            return serverName;
        }

        @Override
        public String toString() {
            return getDocument().toString();
        }
    }
    
    public long getNbPeerResolved(){
        return serverNameToPeerID.keySet().size();
    }
    
    @Override
    public  String toString(){
        return "ServerName : " + serverNameToPeerID.keySet().size() + "Server name resolved";
    }
}
