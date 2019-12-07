package edu.jsu.mcis.cs415;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Lights extends HttpServlet {
    
    Database db = new Database();

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet Lights</title>");            
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet Lights at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }
*/
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            PrintWriter out = response.getWriter();
            out.println(db.getAllLightInfo());               
            //processRequest(request, response);
        } catch (SQLException ex) {
            Logger.getLogger(Lights.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        /*
        JSONObject params = new JSONObject();
        PrintWriter out = response.getWriter();
        System.out.println(request.getParameter("name"));
        try {
            //processRequest(request, response);
            Enumeration<String> parameterNames = request.getParameterNames();
            
            while(parameterNames.hasMoreElements()){
                System.out.println("LOOP");
                String parameterName = parameterNames.nextElement();
                String parameterValue = request.getParameter(parameterName);
                
                System.out.println(parameterName + ":" + parameterValue);
                
                params.put(parameterName, parameterValue);
                
            }
            db.addLight(params);
        } catch (SQLException ex) {
            Logger.getLogger(Lights.class.getName()).log(Level.SEVERE, null, ex);
        }
        */
        StringBuffer jb = new StringBuffer();
        String line = null;
        try {
          BufferedReader reader = request.getReader();
          while ((line = reader.readLine()) != null){
            jb.append(line);
          }
          JSONParser parser = new JSONParser();
          JSONObject jsonObject =  (JSONObject) parser.parse(jb.toString());
            
            db.addLight(jsonObject);
        } catch (Exception e) {
          System.err.print(e);
        }
    }
    
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
           throws ServletException, IOException {
        //processRequest(request, response);
        
        StringBuffer jb = new StringBuffer();
        String line = null;
        try {
          BufferedReader reader = request.getReader();
          while ((line = reader.readLine()) != null){
            jb.append(line);
          }
         
          JSONParser parser = new JSONParser();
          System.out.println("Past parser");
          JSONObject jsonObject =  (JSONObject) parser.parse(jb.toString());
          System.out.println("Past parse");
          
          
          db.changeLightConfig(jsonObject);
        } catch (Exception e) {
          System.err.print(e);
        }
    }
    
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //processRequest(request, response);
        String id = request.getParameter("id");
        try {
            db.deleteLight(id);
        } catch (SQLException ex) {
            Logger.getLogger(Lights.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
