/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatinterface;

/**
 *
 * @author MGhigl
 */
import java.net.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import backgroundchatserver.ChatMessage;

public class ChatClientThread extends Thread {
    private Socket socket   = null;
    private FXChatClient client   = null;
    private String ipAddress;
    private DataInputStream  streamIn = null;
    private String strMessage = "";
    ChatMessage chatMessage;
    ObjectInputStream streamInObj;

    public ChatClientThread(FXChatClient client, String ipAddress, Socket socket) {
        this.client = client;
        this.socket = socket;
        this.ipAddress = ipAddress;
        open();  
        start();
    }
    
    public void open() {
        try {
//            streamIn  = new DataInputStream(socket.getInputStream());
            streamInObj = new ObjectInputStream(socket.getInputStream());
        } catch(IOException ioe) {
            System.out.println("Error getting input stream: " + ioe);
            try {
                client.stop();
            } catch (Exception ex) {
                Logger.getLogger(ChatClientThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void close() {
        try {
            if (streamIn != null) streamIn.close();
        } catch(IOException ioe) {
            System.out.println("Error closing input stream: " + ioe);
        }
    }
    
    //Ricezione messaggi
    public void run() {
        while (true) {
           try {

                
                chatMessage = (ChatMessage) streamInObj.readObject();
                client.handle(chatMessage);
            } catch(IOException ioe) {
                System.out.println("Listening error: " + ioe.getMessage());
                client.handle(chatMessage);
               try {
                   client.stop();
               } catch (Exception ex) {
                   Logger.getLogger(ChatClientThread.class.getName()).log(Level.SEVERE, null, ex);
               } 
            } catch (ClassNotFoundException cnf) {
                cnf.printStackTrace();
            }
//            chatMessage = new ChatMessage(ipAddress, strMessage);
            
        }
        
    }
    
    public String getIPAddress() {
        String clientAddress = "";
        
        try {
            InetAddress inetAddress;
            inetAddress = InetAddress.getLocalHost();
            clientAddress = (String) inetAddress.getHostAddress();
        } catch (UnknownHostException ex) {
//            Logger.getLogger(ChatClient.class.getName()).log(Level.SEVERE, null, ex);
             ex.printStackTrace();
        }
        
        return clientAddress;
    }
}
