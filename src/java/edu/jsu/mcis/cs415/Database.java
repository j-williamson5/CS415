/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.jsu.mcis.cs415;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
/**
 *
 * @author Josh
 */
public class Database {
    
    private Connection conn;
    
    public Database(){
        this.conn = getConnection();
    }
    
     public Connection getConnection() {
        
        conn = null;
        
        try {
            Context envContext = new InitialContext();
            Context initContext  = (Context)envContext.lookup("java:/comp/env");
            DataSource ds = (DataSource)initContext.lookup("jdbc/db_pool");
            conn = ds.getConnection();
            System.out.println("*** CONNECTION SUCCESSFUL");
        }        
        catch (Exception e) {
            e.printStackTrace();
        }
        if(conn == null){
            System.out.println("NULL CONNECTION");
        }
        return conn;

    }
     
    private ResultSet getResultSet(String query) throws SQLException{

       //Get connection and make prepared statment
       PreparedStatement pstmt = conn.prepareStatement(query);
       pstmt.execute();

       ResultSet results = pstmt.getResultSet();


       //Execute Query and Return ResultSet
       return results;
   }
    
    public JSONObject getAllLightInfo() throws SQLException{
        

        
        //Get light table results
        String query = "SELECT * FROM homeauto_db.light;";
        ResultSet rs = getResultSet(query);
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnsNumber = rsmd.getColumnCount();
        JSONObject json = new JSONObject();
        
        
        //Loop through all of the "rows" in the resultset
        while (rs.next()) {
            
            //This will hold the innermost json
            JSONObject currentJSON = new JSONObject();
            String id = rs.getString("id");
            
            //Loop through all of the "columns" in the resultset
            for (int i = 1; i <= columnsNumber; i++) {

                //Get info (column name and data)
                String columnValue = rs.getString(i);
                String columnHeading = rsmd.getColumnName(i);
                
                //We ignore the ID as it is the key for the entire object
                if(!columnHeading.equals("id")){
                    
                    //Append data to the current array
                    currentJSON.put(columnHeading, columnValue);
                    
                }
            }
            
            currentJSON.put("state",getStateInfo(rs.getString("typeid"),id));
            json.put(id, currentJSON);
        }
        
        System.out.println(json);
        
        return json;
    }

    private HashMap<String,String> getStateInfo(String type, String id) throws SQLException{
        
        // This method accepts a type and an id. This allows the knowledge of which table to look in and which light to look for
        String stateTable = getStateTable(type);
        
        //Determine which table to search in

        
        //Get light state table results
        String stateQuery = "SELECT * FROM homeauto_db." + stateTable + " WHERE homeauto_db." + stateTable + ".lightid = " + id + ";";
        ResultSet rs = getResultSet(stateQuery);
        ResultSetMetaData rsmd = rs.getMetaData();
        int stateColumnsNumber = rsmd.getColumnCount();
        HashMap<String,String> state = new HashMap();
        
        
        //Add state info
        while(rs.next()){
            
                //Loop through all of the "columns" in the resultset
                for (int i = 1; i <= stateColumnsNumber; i++) {
                    //Get info (column name and data)
                    String columnValue = rs.getString(i);
                    String columnHeading = rsmd.getColumnName(i);
                    
                    //Only add if it is not the lightid
                    if(!columnHeading.equals("lightid")){
                        state.put(columnHeading,columnValue);
                    }
                }
            }

        return state;
    }
    
    private String getStateTable(String type){
        String stateTable = "";
        
        switch(type){
                case "1":
                    stateTable = "state_traditional_light";
                    break;
        }
        
        return stateTable;
    }
    
    public void deleteLight(String id) throws SQLException{
        
        //Get info about light (for finding out type)
        String rsQuery = "SELECT * FROM homeauto_db.light WHERE homeauto_db.light.id = " + id;
        ResultSet rs = getResultSet(rsQuery);
        
        //Move the resultset forward
        rs.next();
        String type = rs.getString("typeid");
        
        
        
        //Delete the info from the database
        String stateTable = getStateTable(type);
        JSONObject json = new JSONObject();
        String query = "DELETE FROM homeauto_db." + stateTable + " WHERE homeauto_db." + stateTable + ".lightid = ?";
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setString(1,id);
        pstmt.execute();
        
        String query2 = "DELETE FROM homeauto_db.light WHERE homeauto_db.light.id = ?";
        PreparedStatement pstmt2 = conn.prepareStatement(query2);
        pstmt2.setString(1, id);
        pstmt2.execute();
    }
    
    public void addLight(JSONObject json) throws SQLException{
        
        //Get query to add to DB
        String query = "INSERT INTO homeauto_db.light (name,manufacturer,model,hardwareid,version,typeid) VALUES (?,?,?,?,?,?)";
        PreparedStatement pstmt = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
        ResultSet keys;

        //Add info to statement from JSON
        pstmt.setString(1,json.get("name").toString());
                
        //System.out.println(json);
        pstmt.setString(2,json.get("manufacturer").toString());
        pstmt.setString(3,json.get("model").toString());
        pstmt.setString(4,json.get("hardwareid").toString());
        pstmt.setString(5,json.get("version").toString());
        pstmt.setString(6,json.get("typeid").toString());
        
        
        //Add lights to table and get autogenerated ID
        int result = pstmt.executeUpdate();
        int key = 0;
        
        if(result == 1){
            keys = pstmt.getGeneratedKeys();
            if(keys.next()){
                key = keys.getInt(1);
            }
        }
        
        //Find out the light type and add state info
        initState(json.get("typeid").toString(),String.valueOf(key));
    }
    
    private void initState(String type,String id) throws SQLException{
       
        //Perform query based on the type
        String query = "";
        
        switch(type){
                case "1":
                    query = "INSERT INTO homeauto_db.state_traditional_light VALUES (?,?)";
                    PreparedStatement pstmt = conn.prepareStatement(query);
                    pstmt.setString(1, id);
                    pstmt.setString(2,"0");
                    
                    System.out.println(pstmt.toString());
                    pstmt.execute();
        }
        
        
    }
    
    public void changeLightConfig(JSONObject json) throws SQLException{
        
        //Get id from JSON
        String id = (String) json.get("id");
        
        //Container for light info
        JSONObject lightInfo = new JSONObject();
        HashMap<String,String> stateInfo = new HashMap();
        
        //Get all existing info of a light
        String query = "SELECT * FROM homeauto_db.light WHERE homeauto_db.light.id = " + id;
        ResultSet rs = getResultSet(query);
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnsNumber = rsmd.getColumnCount();
        
        
        // Get info from the result set
        rs.next();
            
        //Loop through all of the "columns" in the resultset
        for (int i = 1; i <= columnsNumber; i++) {

            //Get info (column name and data)
            String columnValue = rs.getString(i);
            String columnHeading = rsmd.getColumnName(i);

            //We ignore the ID as it is given already
            if(!columnHeading.equals("id")){

                //Append data to the array
                lightInfo.put(columnHeading, columnValue);

            }
        }
        
        //Add the info to the JSON Object
        lightInfo.put("state",getStateInfo(rs.getString("typeid"),id));
        
        //Delete the current light info from the database
        deleteLight(id);
        
        //Change the info depending on the input
        for(Object key: json.keySet()){
            System.out.println("KEY: " + key);
            //Loop through info fetched from DB
            for(Object infoColumn : lightInfo.keySet()){
                System.out.println("infoColumn: " + infoColumn);
                
                //Check if column matches current key
                if(key.equals(infoColumn)){
                    //Update the info in lightInfo
                    lightInfo.put(key,json.get(key));
                    }

                }
            }
        
        //Update the light info
        addLight(lightInfo);
    }
    
}


