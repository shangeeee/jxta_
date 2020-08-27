
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package AppDemo;

/**
 *
 * @author The Boss
 */
public class IndexPanel extends javax.swing.JPanel {

    private boolean started = false;
    /**
     * Creates new form IndexPanel
     */
    public IndexPanel() {
        initComponents();
    }
    
    private void start(){
        if(started) return; 
        String name = fldMyName.getText();
        if(name.isEmpty()){
            return;
            
        }
        started = true;
        startStop.setText("STOP");
    }
    private void stop(){
        if(started){
            started = false;
            startStop.setText("START");
        }
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel4 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        lblBaseGroupName = new javax.swing.JLabel();
        startStop = new javax.swing.JToggleButton();
        jLabel6 = new javax.swing.JLabel();
        cBoxStatus = new javax.swing.JComboBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        lstPermReceived = new javax.swing.JList();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        btnRefusePerm = new javax.swing.JButton();
        btnJoinGroup = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        tableGroups = new javax.swing.JTable();
        jButton1 = new javax.swing.JButton();
        fldMyName = new javax.swing.JTextField();

        FormListener formListener = new FormListener();

        jLabel4.setBackground(new java.awt.Color(255, 255, 255));
        jLabel4.setOpaque(true);

        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setText("Base group name");
        add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 10, 100, 20));

        jLabel3.setText("My Name");
        add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 10, 50, 20));

        lblBaseGroupName.setBackground(new java.awt.Color(255, 255, 255));
        lblBaseGroupName.setOpaque(true);
        add(lblBaseGroupName, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 10, 90, 20));

        startStop.setText("START");
        startStop.addActionListener(formListener);
        add(startStop, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 10, 100, -1));

        jLabel6.setText("Status");
        add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 10, 40, 20));

        cBoxStatus.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cBoxStatus.setEnabled(false);
        add(cBoxStatus, new org.netbeans.lib.awtextra.AbsoluteConstraints(566, 10, 120, -1));

        jScrollPane1.setViewportView(lstPermReceived);

        add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 110, 310, 220));

        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setText("Groups");
        add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 60, 310, 30));

        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel8.setText("Permission received");
        add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 70, 310, 30));

        btnRefusePerm.setText("Refuse");
        add(btnRefusePerm, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 340, -1, -1));

        btnJoinGroup.setText("Join");
        add(btnJoinGroup, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 340, 70, -1));

        tableGroups.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        tableGroups.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        tableGroups.setToolTipText("");
        tableGroups.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        tableGroups.setFillsViewportHeight(true);
        tableGroups.setGridColor(new java.awt.Color(204, 204, 204));
        tableGroups.getTableHeader().setReorderingAllowed(false);
        tableGroups.addMouseListener(formListener);
        tableGroups.addKeyListener(formListener);
        jScrollPane2.setViewportView(tableGroups);

        add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 110, 370, 220));

        jButton1.setText("Open");
        add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(490, 340, -1, -1));
        add(fldMyName, new org.netbeans.lib.awtextra.AbsoluteConstraints(380, 10, 100, -1));
    }

    // Code for dispatching events from components to event handlers.

    private class FormListener implements java.awt.event.ActionListener, java.awt.event.KeyListener, java.awt.event.MouseListener {
        FormListener() {}
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (evt.getSource() == startStop) {
                IndexPanel.this.startStopActionPerformed(evt);
            }
        }

        public void keyPressed(java.awt.event.KeyEvent evt) {
        }

        public void keyReleased(java.awt.event.KeyEvent evt) {
            if (evt.getSource() == tableGroups) {
                IndexPanel.this.tableGroupsKeyReleased(evt);
            }
        }

        public void keyTyped(java.awt.event.KeyEvent evt) {
        }

        public void mouseClicked(java.awt.event.MouseEvent evt) {
            if (evt.getSource() == tableGroups) {
                IndexPanel.this.tableGroupsMouseClicked(evt);
            }
        }

        public void mouseEntered(java.awt.event.MouseEvent evt) {
        }

        public void mouseExited(java.awt.event.MouseEvent evt) {
        }

        public void mousePressed(java.awt.event.MouseEvent evt) {
        }

        public void mouseReleased(java.awt.event.MouseEvent evt) {
        }
    }// </editor-fold>//GEN-END:initComponents

    private void tableGroupsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableGroupsMouseClicked
       
    }//GEN-LAST:event_tableGroupsMouseClicked

    private void tableGroupsKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tableGroupsKeyReleased
       
    }//GEN-LAST:event_tableGroupsKeyReleased

    private void startStopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startStopActionPerformed
        if(!started) start();
        else stop();
    }//GEN-LAST:event_startStopActionPerformed


    
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnJoinGroup;
    private javax.swing.JButton btnRefusePerm;
    private javax.swing.JComboBox cBoxStatus;
    private javax.swing.JTextField fldMyName;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblBaseGroupName;
    private javax.swing.JList lstPermReceived;
    private javax.swing.JToggleButton startStop;
    private javax.swing.JTable tableGroups;
    // End of variables declaration//GEN-END:variables
}
