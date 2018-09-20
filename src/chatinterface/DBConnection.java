/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatinterface;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
            conn.createStatement().execute("CREATE TABLE msgLines (Line varchar(50))");
        } catch (SQLException ex) {
//            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Table msgLines already existing");
        }
    }
    
    public void insertIntoTable(String line) {
        try {
            conn.createStatement().execute("INSERT INTO msgLines VALUES ('"+line+"')");
        } catch (SQLException ex) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void printAll() {
        try {
            Statement statement = this.conn.createStatement();
            ResultSet res = statement.executeQuery("SELECT * FROM msgLines");
            while(res.next()) {
                System.out.println(res.getString("Line"));
            }
                    } catch (SQLException ex) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void emptyTable(){
        try {
            conn.createStatement().execute("DELETE FROM msgLines");
        } catch (SQLException ex) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void dropTable(){
        try {
            conn.createStatement().execute("DROP TABLE msgLines");
        } catch (SQLException ex) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}