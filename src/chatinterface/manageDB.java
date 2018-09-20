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
public class manageDB {
    public static void main(String[] args) {
        DBConnection db = new DBConnection();
//        db.createTable();
//        db.insertIntoTable("Hello");
//        db.insertIntoTable("How are you?");
        db.printAll();
//        db.emptyTable();
//        db.dropTable();
    }
}
