/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatinterface;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.image.*;
import static javafx.scene.input.DataFormat.IMAGE;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import backgroundchatserver.ChatMessage;

/**
 *
 * @author MGhigl
 */
public class FXChatClient extends Application {
    
    private DataOutputStream streamOut = null;
    private ChatClientThread client    = null;
    private Socket socket = null;
    private ObservableList<String> chatMessages = FXCollections.observableArrayList();
    private ListView chatTextField = new ListView();
    private TextField msgTextField = new TextField();
    private TextField toIPTextField = new TextField();
    private String IPAddress = getIPAddress();
    
    @Override
    public void start(Stage primaryStage) throws UnknownHostException {
        StackPane root = new StackPane();
        Scene scene = new Scene(root, 300, 250);
        primaryStage.setTitle("My Chat App");
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));
        scene = new Scene(grid, 300, 275);
        Text scenetitle = new Text("My Chat App");
        scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        grid.add(scenetitle, 0, 0, 2, 1);
        addChatLine("Chat Client v 1.0.1");
        
        //Form controls
        grid.add(chatTextField, 1, 1);  
        Label myIPLabel = new Label("My IP:");
        grid.add(myIPLabel, 0, 2);
        chatTextField.setEditable(false);
        TextField myIPTextField = new TextField();
        grid.add(myIPTextField, 1, 2);
        Label toIPLabel = new Label("To IP:");
        grid.add(toIPLabel, 0, 3);
        grid.add(toIPTextField, 1, 3);
        toIPTextField.setText("No IP yet...");
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
                } else if(msgTextField.getText().equals("")) {
                    System.out.println("Please enter a message...");
                } else {
                    sendChatMessage(msgTextField.getText(), IPAddress, toIPTextField.getText());
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
        chatTextField.setItems(chatMessages);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
        System.out.println("test");
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
    
    private void sendChatMessage(String text, String myIPAddress, String toIPAddress) {
        if(toIPTextField.getText().equals("No IP yet...")) {
            System.out.println("Please enter destination IP...");
        } else {
            try {
                new ObjectOutputStream(streamOut).writeObject(new ChatMessage(text, myIPAddress, toIPAddress));
                streamOut.flush(); 
                msgTextField.setText(""); 
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
        chatMessages.add(line);
        chatScroll();
    }
    
    public void handle(ChatMessage msg) {
        if (msg.getMsg().equals(".bye")) {
            addChatLine("Good bye. Press RETURN to exit ...");  
            close(); 
        } else {
            addChatLine(msg.getMsg());
        }
    }
    
    public void open() {
        try {
            client = new ChatClientThread(this, IPAddress, socket);
            streamOut = new DataOutputStream(socket.getOutputStream());
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
                chatTextField.scrollTo(chatMessages.size());
            }
        });
    }
    
}
