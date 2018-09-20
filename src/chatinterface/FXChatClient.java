/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatinterface;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.layout.HBox;
import backgroundchatserver.ChatMessage;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 *
 * @author MGhigl
 */
public class FXChatClient extends Application {
    
    private static final Pattern PATTERN = Pattern.compile(
        "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
    private DataOutputStream streamOut = null;
    private ChatClientThread client    = null;
    private Socket socket = null;
    private ObservableList<String> chatLines = FXCollections.observableArrayList();
    
    
    
    private TextField msgTextField = new TextField();
    private TextField toIPTextField = new TextField();
    private String IPAddress = getIPAddress();
    ObjectOutputStream objectOutputStream;
    DBConnection db;
    ArrayList<String> dbMessages = new ArrayList<>();
    
    //Form controls that need to be accessed externally
    private ListView chatControl = new ListView();
    
    public FXChatClient() throws IOException {
        
    }
    
    @Override
    public void start(Stage primaryStage) throws UnknownHostException {
        //Opening Derby Embedded Database Connection
        db = new DBConnection();
        
        dbMessages = db.printSelToArrayList(getIPAddress());
        //Creating messages table if necessary
//        db.emptyTable();
        db.createTable();
        
        StackPane root = new StackPane();
        Scene scene = new Scene(root, 400, 400);
        primaryStage.setTitle("My Chat App");
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));
        scene = new Scene(grid, 400, 400);
        Text scenetitle = new Text("My Chat App");
        scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        grid.add(scenetitle, 0, 0, 2, 1);
        addChatLine("Chat Client v 1.0.1");
        
        //Form controls
        
        //Main messages text area
        grid.add(chatControl, 1, 1);  
        Label myIPLabel = new Label("My IP:");
        grid.add(myIPLabel, 0, 2);
        chatControl.setEditable(false);
        chatControl.minHeight(300);
        
        TextField myIPTextField = new TextField();
        grid.add(myIPTextField, 1, 2);
        Label toIPLabel = new Label("To IP:");
        grid.add(toIPLabel, 0, 3);
        grid.add(toIPTextField, 1, 3);
        //Only for testing purposes
        toIPTextField.setText("192.168.0.3");
//        toIPTextField.setText("No IP yet...");
        grid.add(msgTextField, 1, 4);
        myIPTextField.setText(getIPAddress());
        myIPTextField.setDisable(true);
        msgTextField.requestFocus();
        Button btn = new Button("Send");
        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
        hbBtn.getChildren().add(btn);
        grid.add(hbBtn, 1, 5);
        btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                if (toIPTextField.getText().equals("No IP yet...")) {
                    System.out.println("Please enter destination IP...");
                } else if(!IPV4validate(toIPTextField.getText())) {
                    System.out.println("Please insert a valid IP...");
                } else if(msgTextField.getText().equals("")) {
                    System.out.println("Please enter a message...");
                } else {
                    sendChatMessage(new ChatMessage(msgTextField.getText(), IPAddress, toIPTextField.getText()));
                }
            }
        });
        //END of form controls
        
        primaryStage.setScene(scene);
        primaryStage.show();
        connect("127.0.0.1", 4444);
        
    }

    @Override
    public void init() throws Exception {
        super.init();
        chatControl.setItems(chatLines);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
        
    }
    
    public void connect(String serverName, int serverPort) {
        addChatLine("Establishing connection. Please wait ...");
        try {
            socket = new Socket(serverName, serverPort);
            addChatLine("Connected..." + socket);
            
            open();
        } catch(UnknownHostException uhe) {
          System.out.println("Host unknown: " + uhe.getMessage()); 
        } catch(IOException ioe) {
          System.out.println("Unexpected exception: " + ioe.getMessage()); 
        } 
    }
    
    private void sendChatMessage(ChatMessage msg) {
        if(toIPTextField.getText().equals("No IP yet...")) {
            System.out.println("Please enter destination IP...");
        } else {
            try {
                objectOutputStream.writeObject(msg);
                objectOutputStream.flush(); 
                msgTextField.setText(""); 
                if(msg.getMsgType() == 0) {
                    db.insertIntoTable(msg.getIpAddress(), msg.getToIPAddress(), msg.getMsg());
//                    db.printAll();
                }
                
                
            } catch(IOException ioe) {
                addChatLine("Sending error: " + ioe.getMessage()); 
                close(); 
            }
        }
    }
    
    private String getIPAddress() {
        /**
         * FOR TESTING PURPOSES
         * Assign a fake IP
         */
        return "192.168.0.2";
//        InetAddress inetAddress = null;
//        try {
//            inetAddress = InetAddress.getLocalHost();
//        } catch (UnknownHostException ex) {
//            Logger.getLogger(FXChatClient.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return (String) inetAddress.getHostAddress();
    }
    
    private void addChatLine(String line) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                chatLines.add(line);
            }
        });
        chatScroll();
    }
    
    public void handle(ChatMessage msg) {
        int type = msg.getMsgType();
        String message = msg.getMsg();
        
//        String dbMessage = "";
        if(type == 0) {
            System.out.println("Normal message: " + message);
            db.insertIntoTable(msg.getIpAddress(), msg.getToIPAddress(), msg.getMsg());
            db.printAll();
            addChatLine(message);
        } else if(type == 1) {
            System.out.println("System message" + message);
            switch(message) {
                case "HELLO":
                    System.out.println("Starting handshake...");
                    sendChatMessage(new ChatMessage(1, "HELLO"));
                    break;
                    
                case "YOURIP":
                    System.out.println("Server asking for IP...");
                    System.out.println("Sending IP...");
                    sendChatMessage(new ChatMessage(1, "MYIP", getIPAddress()));
                    System.out.println("Requesting clients list...");
                    sendChatMessage(new ChatMessage(1, "LIST"));
                    break;
                    
                case "IPLIST":
                    System.out.println("Received IP list...");
            {
                try {
                    Thread.sleep(2000);
                    
                } catch (InterruptedException ex) {
                    Logger.getLogger(FXChatClient.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
                    chatClear();
//                    addChatLine("Test line");
                    
                    dbMessages.forEach(dbMessage -> addChatLine(dbMessage));
                    addChatLine(msg.getIpAddress());
                    
            }
        }
    }
    
    public void open() {
        try {
            client = new ChatClientThread(this, IPAddress, socket);
            streamOut = new DataOutputStream(socket.getOutputStream());
            objectOutputStream = new ObjectOutputStream(streamOut);
            
        } catch(IOException ioe) {
            addChatLine("Error opening output stream: " + ioe);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
   public void close() { 
       try {
            if (streamOut != null)  streamOut.close();
            if (socket    != null)  socket.close(); 
        } catch(IOException ioe) {
            addChatLine("Error closing ...");
        }
      client.close();
      client.stop(); 
    }
   
    private void chatScroll(){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                chatControl.scrollTo(chatLines.size());
            }
        });
    }
    
    private void chatClear() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                chatLines.clear();
            }
        });
        
    }
    
    public static boolean IPV4validate(final String ip) {
        return PATTERN.matcher(ip).matches();
    }
}
