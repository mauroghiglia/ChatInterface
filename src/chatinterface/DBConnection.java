/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatinterface;

import backgroundchatserver.ChatMessage;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author MGhigl
 */
public class DBConnection {
//    private static final String DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
    private static final String JDBC_URL = "jdbc:derby:chatinterface;create=true";
    
    Connection conn;

    public DBConnection() {
        try {
            this.conn = DriverManager.getConnection(JDBC_URL);
            if(this.conn != null) {
                System.out.println("Connected to Database!");
            }
        } catch (SQLException ex) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void createTable() {
        try {
            conn.createStatement().execute("CREATE TABLE messages (FromIP varchar(15), ToIP varchar(15), Msg varchar(50))");
        } catch (SQLException ex) {
//            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Table messages already existing");
        }
    }
    
    public void insertIntoTable(String fromIP, String toIP, String msg) {
        try {
            conn.createStatement().execute("INSERT INTO messages VALUES ('"+fromIP+"', '"+toIP+"', '"+msg+"')");
        } catch (SQLException ex) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void printAll() {
        try {
            Statement statement = this.conn.createStatement();
            ResultSet res = statement.executeQuery("SELECT * FROM messages");
            System.out.println("Content of messages table");
            while(res.next()) {
                System.out.println(res.getString("FromIP") 
                    + " " + res.getString("ToIP") 
                    + " " + res.getString("Msg"));
            }
                    } catch (SQLException ex) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public ArrayList printSelToArrayList(String ip) {
        String myMsg;
        ArrayList<String> messages = new ArrayList<>();
        try {
            Statement statement = this.conn.createStatement();
            ResultSet res = statement.executeQuery("SELECT * FROM messages");
            System.out.println("Content of messages table");
            while(res.next()) {
                String fromIP = res.getString("FromIP");
                String toIP = res.getString("ToIP");
                String msg = res.getString("Msg");
                if(fromIP.equals(ip)) {
                    myMsg = "From: you\n\t" + msg;
                    messages.add(myMsg);
                } else if(toIP.equals(ip)) {
                    myMsg = "From: " + fromIP + "\n" + msg;
                    messages.add(myMsg);
                }
            }
                    } catch (SQLException ex) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
        return messages;
    }
    
    public void emptyTable(){
        try {
            conn.createStatement().execute("DELETE FROM messages");
        } catch (SQLException ex) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
