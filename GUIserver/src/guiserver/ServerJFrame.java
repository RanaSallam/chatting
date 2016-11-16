                /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package guiserver;

/**
 *
 * @author Ronion
 */

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerJFrame extends javax.swing.JFrame {
    /**
     * Creates new form ServerJFrame
     */
    
    // list of clients Sockets
    ArrayList <Socket> clients = new ArrayList();
    // hash map to have the clients dosses with names 
    HashMap <String,DataOutputStream> Clients= new HashMap();
    // represent the online users dosess
    ArrayList <DataOutputStream> out = new ArrayList();
    Vector<String> onlineUsers = new Vector();
    Vector<String> offlineUsers = new Vector();
    Vector<Group> Groups = new Vector();
    Vector<String> ports = new Vector();
    
   public class Group{
        private String GN;
        int index;
        private int nMembers;
        private ArrayList <String> Names;
        private ArrayList <DataOutputStream> dosses = new ArrayList();
        
       public Group(String groupname, int numbers, ArrayList <String> names)
        {
            GN = groupname;
            nMembers = numbers;
            Names = names;
           
            // taking dosses from the [hashmap: clients] using the list names
            for(String name: names)
                dosses.add(Clients.get(name));
            
        }
       public String getName(){
            return GN;
        }
       public ArrayList<DataOutputStream> getDosses()
       {
           return dosses;
       }
       
       public ArrayList <String> getMembers()
       {
           return Names;
       }
       // DEH 3SHAAAA1AN L ENROLLING
       public void addMember(String n, String d){
        Names.add(n);
        int x = Integer.parseInt(d);
                dosses.add(out.get(x)); 
       }
       
        public void removeMember(DataOutputStream d){
            // remove from the member names+ dosses list
            // names arent unique till now
            int ind = dosses.indexOf(d);
            dosses.remove(ind);
            Names.remove(ind);       
       }
     
    }
    public class ClientHandler extends Thread {
        public DataOutputStream dos;
        public DataInputStream dis;
        Socket client;
        String ingroup = "";
        

        public ClientHandler(Socket clientSocket) 
        {
                client = clientSocket;
        }
        
        public void run() {
            try {
                dos = new DataOutputStream(client.getOutputStream());
                dis = new DataInputStream(client.getInputStream());
                
                // adding the name and the client Socket
                String user="" ;
            
               dos.writeUTF("Connection is made\n");
               
                while (true) {
                    String request = dis.readUTF();
                    if(request.equals("1111")){//connected, online
                        String port = dis.readUTF();
                        ports.add(port);
                        Clients.put(user,dos);
                        out.add(dos);
                        onlineUsers.add(user);
                        offlineUsers.remove(user);
                        ShowClients();
                        tellEveryone(user+ " is connected\n");
                        tellEveryone("1111");
              
                    }
                    // to check if the name is unique
                     else if(request.equals("5555")){
                        user=dis.readUTF();
                        
                        boolean flag =Clients.containsKey(user);
                        if (flag)
                            dos.writeUTF("5555");
                        else
                            dos.writeUTF("4444");
                        
                        
                    }
                    //disconnected client and removing him from everything
                    else if(request.equals("0000"))
                    {
                    
                        Disconnect(user, dos);
                     
                    }
                    //online
                    else if(request.equals("1112")){
                      //  String user = dis.readUTF();
                        onlineUsers.add(user);
                        offlineUsers.remove(user);
                        out.add(dos);
                        tellEveryone("1111");
                        outputPane.append(user+" is online\n");
                    } 
                    //offline
                    else if(request.equals("1113"))
                    {
                      //  String user = dis.readUTF();
                        out.remove(dos);
                        onlineUsers.remove(user);
                        offlineUsers.add(user);
                        tellEveryone("1111");
                        outputPane.append(user+" is offline\n");
                    }
                    // create group
                    else if(request.equals("2222"))
                    {
                        String Groupname = dis.readUTF();
                        String no = dis.readUTF();
                        int Number = Integer.parseInt( no );
                        ArrayList <String> names = new ArrayList <String>();
                       
                        //taking names of the members
                        for(int i =0 ; i<Number ; i++)
                        {
                            names.add(dis.readUTF());
                           
                        }
                        creatingGroup(Groupname, no, names);
                    }
                    
                    else if(request.equals("3333")){//EnterGroup button
                        String ChatGroup = dis.readUTF();
                        String username = dis.readUTF();
                        if(InGroup(ChatGroup,username))
                        {
                            dos.writeUTF("3331");//msg=your are now in a group chat
                            Group W = Groups.get(0);
                            for(Group g:Groups)
                            {
                                if(g.getName().equals(ChatGroup))
                                {
                                    W=g;
                                    break;
                                }
                            }
                            GroupTell("\nYou can now start chating in group ("+
                                    ChatGroup +") \n", W.getDosses());
                       
                        } else
                            dos.writeUTF("3330");//You are not allowed to chat in this group
                    }
                    else if(request.equals("3332")){//Chat Group button
                        String ChatGroup = dis.readUTF();
                        String username = dis.readUTF();
                        String msg=dis.readUTF();
                        
                        if(InGroup(ChatGroup,username))
                        {
                        dos.writeUTF("3337");//respond from chat group button
                       // dos.writeUTF(msg);
                            Group W = Groups.get(0);
                            for(Group g:Groups)
                            {
                                if(g.getName().equals(ChatGroup))
                                {
                                    W=g;
                                    break;
                                }
                            }
                           
                            GroupTell("[Group "+W.getName()+"]"+username+" : "+msg+"\n", W.getDosses());                                 
                            outputPane.append("[Group "+W.getName()+"]"+username+" : "+msg+"\n");
                            
                        }
                        else{
                            dos.writeUTF("3338");//You are not allowed to chat in this group
                        }
                    }
                    ////////////ENROLLING//////////
                    else if(request.equals("3335")){//if enroll button is pressed
                        String ChatGroup = dis.readUTF();
                        String username = dis.readUTF();
                        String index = dis.readUTF();
                
                            Group W = Groups.get(0);
                         
                            for(Group g:Groups)
                            {
                                if(g.getName().equals(ChatGroup))
                                {
                                    W=g;
                                    break;
                                }
                            }
                            W.addMember(username, index);
                            outputPane.append(username+" is enrolled to "+ChatGroup+" group\n");
                    }
                    
                    
                    ////////////UNENROLLING//////////
                    else if(request.equals("3336")){//if unenroll button is pressed
                        String ChatGroup = dis.readUTF();
                        String username = dis.readUTF();
                
                            Group W = Groups.get(0);
                         
                            for(Group g:Groups)
                            {
                                if(g.getName().equals(ChatGroup))
                                {
                                    W=g;
                                    break;
                                }
                            }
                            W.removeMember(dos);
                            outputPane.append(username+" is unenrolled from "+ChatGroup+" group\n");
                    }
                    
                    else if(request.equals("7777"))
                    {
                        dos.writeUTF("7777");
                        for(String port : ports)
                        {
                            dos.writeUTF(port);
                        }
                        dos.writeUTF("7779");
                    }
                    
                    else{
                        if(ingroup != ""){
                            Group W = Groups.get(0);
                            for(Group g:Groups)
                            {
                                if(g.getName().equals(ingroup))
                                {
                                    W=g;
                                    break;
                                }
                            }
                            GroupTell(request, W.getDosses());
                        }
                        else
                            tellEveryone(request);
                    }
                } 
            } 
            catch (Exception ex) {
                outputPane.append("Omission Failure. \n");
            } 
        } 
    } 
    
    public class Server extends Thread {
        public void run() {
            try {
                ServerSocket serversocket = new ServerSocket(5000);
                while (true) {
                    Socket clientsocket = serversocket.accept();
                    ClientHandler listener = new ClientHandler(clientsocket);
                    listener.start();
                    outputPane.append("Got a connection. \n");
                    clients.add(clientsocket);
                }
            } 
            catch (Exception ex)
            {
                outputPane.append("Error making a connection. \n");
            }
        }
    }
    public void creatingGroup(String GN, String N,  ArrayList <String> Names)
    {
        try 
        {
            int Number = Integer.parseInt(N);
            outputPane.append("Group is created now \n");
            outputPane.append("Groupname: " + GN + "\n");
            outputPane.append("Number of the group: " + N + "\n");
            outputPane.append("Group Members: ");
            for(String name: Names)
            {
                outputPane.append(name+ "\n");
            }
            Group G = new Group(GN, Number, Names);
            Groups.add(G);
            String Message = "Group (" + GN + ") is created \n";
            tellEveryone(Message);
            tellEveryone("1111");
            String Newmessage = "You joined group ("+ GN +")\n";
            GroupTell(Newmessage,G.getDosses());
        } 
        catch (Exception ex)
            {
                outputPane.append("Error creating a group. \n");
            }
    }

    // Telling EVERYONE even OFFlines one :D
    public void tellEveryone(String message) {
        if(!message.equals("1111")){
            outputPane.append("To all clients: "+message);
        }
       
        // set of names of the clients
        Set<String>keys= Clients.keySet();
        DataOutputStream output;
        // foreach to get dos by dos for every client
        for(String k:keys)
        {
            try {
                
                output= Clients.get(k); // dos of the client name =k
                if(message.equals("1111"))
                {
                    output.writeUTF("1111");
                    for(String onlineusers : onlineUsers)
                        output.writeUTF(onlineusers);
                    output.writeUTF("1234");
                    if(offlineUsers.size()==0)
                        output.writeUTF("EMPTYOFF");
                    else
                        for(String offlineusers : offlineUsers)
                            output.writeUTF(offlineusers);
                    output.writeUTF("4321");
                    for(Group g : Groups)
                        output.writeUTF(g.getName());
                    output.writeUTF("4231");
                }
                else
                {
                    output.writeUTF("[Public Message] "+message);
                   
                }
            } catch (Exception ex)
                {
                    outputPane.append("Error sending message to clients. \n");
                }
        }
    }
    
    public void GroupTell(String message, ArrayList <DataOutputStream> dosses)
    {
        for(DataOutputStream output : dosses)
        {
            try {
                output.writeUTF(message);
            } catch (Exception ex)
                {
                    outputPane.append("Error sending message to the group "
                            + "clients. \n");
                }
        }
        
    }
    
    
    public boolean InGroup(String GN, String username)
    {
        Group W = Groups.get(0);
        for(Group g:Groups)
        {
            if(g.getName().equals(GN))
            {
                W=g;
                break;
            }
        }
         ArrayList <String> members = W.getMembers();
        for(String name: members)
        {
            if(name.equals(username))
                return true;
        }
        return false;
    }
    
    
    ///// Disconnecting function
    public void Disconnect(String name, DataOutputStream Udos)
    {
        // removing from all the lists
        int ind;
        ind=out.indexOf(Udos);
        if (ind !=-1){
        out.remove(ind);
        onlineUsers.remove(name);
        }
        else{
           
          offlineUsers.remove(name);  
        }
        Clients.remove(name);
        
        //removing from all the groups
        for(Group g : Groups){
            
                ind = g.getDosses().indexOf(Udos);
                if (ind != -1) {
                    g.getMembers().remove(ind); 
                    g.getDosses().remove(ind);
                    
                }
            
        }
        ShowClients();
        tellEveryone("1111");
      /*  try {
            Udos.close();
        } catch (IOException ex) {
            Logger.getLogger(ServerJFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        */
    }
    
    // to show names of the clients 
    public void ShowClients(){
        
      // clients.forEach(clients.keySet());
      Vector<String> v = new Vector();
      for( String c: Clients.keySet()){
          v.add(c);
         
      }
        ConnClientsList.setListData(v);
    }
    
    public void kick_off( String clientName) throws IOException{
       
        
        DataOutputStream dos = Clients.get(clientName);
      
        // to let the client know about the kicking off
        dos.writeUTF("9696");
        // the same as the disconnecting funtion 
        Disconnect(clientName, dos);
        tellEveryone(clientName +" : has been kicked off \n");
        //outputPane.append(clientName + " has been kicked off \n");
        //c.close();
        ShowClients();
        
    }
    
    
    public ServerJFrame() {
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        outputPane = new javax.swing.JTextArea();
        startButton = new javax.swing.JButton();
        stopButton = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        ConnClientsList = new javax.swing.JList<>();
        KickOffBut = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        outputPane.setColumns(20);
        outputPane.setRows(5);
        jScrollPane1.setViewportView(outputPane);

        startButton.setText("START");
        startButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startButtonActionPerformed(evt);
            }
        });

        stopButton.setText("STOP");
        stopButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopButtonActionPerformed(evt);
            }
        });

        ConnClientsList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        jScrollPane2.setViewportView(ConnClientsList);

        KickOffBut.setText("Kick Off");
        KickOffBut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                KickOffButActionPerformed(evt);
            }
        });

        jLabel1.setText("     Connected Clients");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 487, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 30, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(KickOffBut)
                        .addGap(45, 45, 45))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 136, Short.MAX_VALUE)
                        .addContainerGap())))
            .addGroup(layout.createSequentialGroup()
                .addGap(126, 126, 126)
                .addComponent(startButton)
                .addGap(100, 100, 100)
                .addComponent(stopButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 229, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(KickOffBut, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 304, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, 13, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(stopButton)
                    .addComponent(startButton))
                .addContainerGap(35, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void startButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startButtonActionPerformed
        // TODO add your handling code here:
        Server start = new Server();
        start.start();

        outputPane.append("Server started. \n");
    }//GEN-LAST:event_startButtonActionPerformed

    private void stopButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopButtonActionPerformed
        // TODO add your handling code here:
        tellEveryone("Server has stopped. All clients will be disconnected. \n ");
        // to close all sockets 
       /* for( Socket c: clients){
            try {
                c.close();
            } catch (IOException ex) {
                Logger.getLogger(ServerJFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }*/
        outputPane.append("Server stopped \n");
       
    }//GEN-LAST:event_stopButtonActionPerformed

    private void KickOffButActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_KickOffButActionPerformed
        try {
            
            String name= ConnClientsList.getSelectedValue();
            kick_off(name);
        } catch (IOException ex) {
            Logger.getLogger(ServerJFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_KickOffButActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ServerJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ServerJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ServerJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ServerJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ServerJFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList<String> ConnClientsList;
    private javax.swing.JButton KickOffBut;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea outputPane;
    private javax.swing.JButton startButton;
    private javax.swing.JButton stopButton;
    // End of variables declaration//GEN-END:variables
}
