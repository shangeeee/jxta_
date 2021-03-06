/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package App.gui;

import App.SimpleChatRDV;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import net.jxta.exception.PeerGroupException;
import net.jxta.id.IDFactory;
import net.jxta.peer.PeerID;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.platform.NetworkConfigurator;
import net.jxta.platform.NetworkManager;
import service.chat.IChatServiceListener;
import service.chat.event.ChatMessageEvent;
import service.chat.impl.ChatService;
import service.chat.impl.message.ChatMessageReceiv;
import service.chat.impl.message.ChatMessageSend;

import javax.swing.JFrame;
import net.jxta.logging.Logging;

/**
 *
 * @author The Boss
 */


public class SimpleChatNoRDV extends JFrame{
    PeerGroup netPeerGroup = null;
    ChatService chatService;
    String name;
    NetworkManager netManager;
    static int tcpPort = 9712;

    /**
     * Creates new form SimpleChat
     */
    public SimpleChatNoRDV() {
        
        System.out.println("loading class  SimpleChatNoRDV");
        initComponents();
        lstCible.setModel(new DefaultListModel());
        try {
            startJxta();
        } catch (IOException ex) {
            Logger.getLogger(SimpleChatRDV.class.getName()).log(Level.SEVERE, null, ex);
        }
        addWindowListener(new WindowListener() {

            @Override
            public void windowOpened(WindowEvent e) {
            }

            @Override
            public void windowClosing(WindowEvent e) {
                netManager.stopNetwork();
                System.exit(0);
            }

            @Override
            public void windowClosed(WindowEvent e) {
            }

            @Override
            public void windowIconified(WindowEvent e) {
            }

            @Override
            public void windowDeiconified(WindowEvent e) {
            }

            @Override
            public void windowActivated(WindowEvent e) {
            }

            @Override
            public void windowDeactivated(WindowEvent e) {
            }
        });

    }

    private void startJxta() throws IOException {
       // name = JOptionPane.showInputDialog(this, "Enter peer name");
        name = "peer2";
        lblPeerName.setText(name);
        System.out.println("Name = " + name);
        File ConfigurationFile;
        PeerID PID;
        PID = IDFactory.newPeerID(PeerGroupID.defaultNetPeerGroupID, name.getBytes());
        ConfigurationFile = new File("." + System.getProperty("file.separator") + name);
        netManager = new NetworkManager(NetworkManager.ConfigMode.EDGE, name, ConfigurationFile.toURI());

        // Retrieving the network configurator
        NetworkConfigurator netConfigurator = netManager.getConfigurator();

        // Setting more configuration
        netConfigurator.setTcpPort(tcpPort);
        netConfigurator.setTcpEnabled(true);
        netConfigurator.setTcpIncoming(true);
        netConfigurator.setTcpOutgoing(true);

        //Connect to a rdv seed in using the local address
        //String seed = "tcp://" + InetAddress.getLocalHost().getHostAddress() + ":" + App.SimpleChatRDV.tcpPort;
        String seed = "tcp://" + "0.0.0.0" + ":" + App.SimpleChatRDV.tcpPort;

        URI LocalSeedingRendezVousURI = URI.create(seed);
        netConfigurator.addSeedRendezvous(LocalSeedingRendezVousURI);

        try {
            // Starting the network
            netPeerGroup = netManager.startNetwork();
            System.out.println("Je suis RDV " + netPeerGroup.isRendezvous());
            if (netPeerGroup.isRendezvous() == false) {
                System.out.println("Waiting for rendezvous connection ...");
                if (netManager.waitForRendezvousConnection(10000)) {
                    System.out.println("Connected to a rendezvous");
                } else {
                    System.out.println("Unable to find a rendez vous connection");
                    // Disabling any rendezvous autostart
                    netPeerGroup.getRendezVousService().setAutoStart(false);
                }
            }
        } catch (PeerGroupException ex) {
            System.out.println("Cannot instanciate peer group. check tcp port");
            System.exit(1);
        }
        netConfigurator.setPeerID(PID);
        chatService = new ChatService();
        chatService.init(name, netPeerGroup);
        chatService.startApp(null);
        chatService.addChatPropagateListener((ChatMessageEvent chatEvent) -> {
            ChatMessageReceiv chatMessageReceiv = chatEvent.getMessage();
            String receivedDate = chatEvent.getReceivedDate().toString();
            String source = chatMessageReceiv.getSender();
            areaMsgReceived.append("Group message from " + source + " at " + receivedDate + "\n");
            areaMsgReceived.append("Msg content : " + chatMessageReceiv.getMessageContent() + "\n");
        });

        chatService.addChatUnicastListener(ChatService.UNKNOWN_PEER, (ChatMessageEvent chatEvent) ->{
            ChatMessageReceiv chatMessageReceiv = chatEvent.getMessage();
            String receivedDate = chatEvent.getReceivedDate().toString();
            String source = chatMessageReceiv.getSender();
            areaMsgReceived.append("SPAM message from " + source + " at " + receivedDate + "\n");
            areaMsgReceived.append("Msg content : " + chatMessageReceiv.getMessageContent() + "\n");
        });
    }

    private void ajouterCible() {
        String cible = fldAddCible.getText();
        if (cible.isEmpty()) {
            return;
        }
        DefaultListModel model = (DefaultListModel) lstCible.getModel();
        model.addElement(cible);
        chatService.addChatUnicastListener(cible, new IChatServiceListener() {

            @Override
            public void processNewMessage(ChatMessageEvent chatEvent) {
                ChatMessageReceiv chatMessageReceiv = chatEvent.getMessage();
                String receivedDate = chatEvent.getReceivedDate().toString();
                String source = chatMessageReceiv.getSender();
                areaMsgReceived.append("New message from " + source + " at " + receivedDate + "\n");
                areaMsgReceived.append("Msg content : " + chatMessageReceiv.getMessageContent() + "\n");
            }
        });

    }

    private void sendMsg() {
        String target = (String) lstCible.getSelectedValue();
        String msg = areaMsgToSend.getText();
        if (target == null) {
            ChatMessageSend toSend = new ChatMessageSend(netPeerGroup.getPeerGroupID(), name, msg);
            try {
                chatService.sendInGroup(toSend);
            } catch (IOException ex) {
                System.out.println("Cannot send message restart the application");
            }
            return;
        }
        if ((target.isEmpty() || msg.isEmpty())) {
            System.out.println("empty target or msg ");
            return;
        }

        ChatMessageSend toSend = new ChatMessageSend(netPeerGroup.getPeerGroupID(), name, msg);
        try {
            chatService.send(target, toSend);
        } catch (IOException ex) {
            System.out.println("Cannot send message restart the application");
        }
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private void initComponents() {

        jScrollPane5 = new javax.swing.JScrollPane();
        lstCible = new javax.swing.JList();
        fldAddCible = new javax.swing.JTextField();
        btnAddCible = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        areaMsgToSend = new javax.swing.JTextArea();
        btnSendMsg = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        areaMsgReceived = new javax.swing.JTextArea();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        lblPeerName = new javax.swing.JLabel();

        FormListener formListener = new FormListener();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lstCible.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane5.setViewportView(lstCible);

        getContentPane().add(jScrollPane5, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 110, 410, 100));

        fldAddCible.addActionListener(formListener);
        getContentPane().add(fldAddCible, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 40, 130, 30));

        btnAddCible.setText("Ajouter");
        btnAddCible.addActionListener(formListener);
        getContentPane().add(btnAddCible, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 40, 120, 30));

        areaMsgToSend.setColumns(20);
        areaMsgToSend.setRows(5);
        jScrollPane1.setViewportView(areaMsgToSend);

        getContentPane().add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 240, 410, -1));

        btnSendMsg.setText("Envoyer");
        btnSendMsg.addActionListener(formListener);
        getContentPane().add(btnSendMsg, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 350, -1, 40));

        areaMsgReceived.setColumns(20);
        areaMsgReceived.setRows(5);
        jScrollPane2.setViewportView(areaMsgReceived);

        getContentPane().add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(436, 60, 300, 330));

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Message recu");
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(526, 20, 130, 30));

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Selectionner la cible");
        getContentPane().add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 80, 400, 20));

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("Entrez votre message");
        getContentPane().add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(14, 220, 410, -1));

        jLabel4.setText("Ajoiuter une cible");
        getContentPane().add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 40, -1, 30));
        getContentPane().add(lblPeerName, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 4, 130, 20));

        pack();
    }

    // Code for dispatching events from components to event handlers.
    private class FormListener implements java.awt.event.ActionListener {

        FormListener() {
        }

        public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (evt.getSource() == fldAddCible) {
                SimpleChatNoRDV.this.fldAddCibleActionPerformed(evt);
            } else if (evt.getSource() == btnAddCible) {
                SimpleChatNoRDV.this.btnAddCibleActionPerformed(evt);
            } else if (evt.getSource() == btnSendMsg) {
                SimpleChatNoRDV.this.btnSendMsgActionPerformed(evt);
            }
        }
    }// </editor-fold>                        

    private void fldAddCibleActionPerformed(java.awt.event.ActionEvent evt) {
        ajouterCible();
    }

    private void btnAddCibleActionPerformed(java.awt.event.ActionEvent evt) {
        ajouterCible();
    }

    private void btnSendMsgActionPerformed(java.awt.event.ActionEvent evt) {
        sendMsg();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        System.setProperty(Logging.JXTA_LOGGING_PROPERTY , Level.OFF.toString());
        /*
         * Set the Nimbus look and feel
         */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info
                    : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(SimpleChatRDV.class.getName()).log(
                    java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(SimpleChatRDV.class.getName()).log(
                    java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(SimpleChatRDV.class.getName()).log(
                    java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(SimpleChatRDV.class.getName()).log(
                    java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /*
         * Create and display the form
         */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new SimpleChatNoRDV().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify                     
    private javax.swing.JTextArea areaMsgReceived;
    private javax.swing.JTextArea areaMsgToSend;
    private javax.swing.JButton btnAddCible;
    private javax.swing.JButton btnSendMsg;
    private javax.swing.JTextField fldAddCible;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JLabel lblPeerName;
    private javax.swing.JList lstCible;
    // End of variables declaration  
}
