/*
 * Author: jianqing
 * Date: Mar 26, 2024
 * Description: This document is created for main servlet of our application.
 */
package com.postexchange.controller;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ReUtil;
import com.postexchange.model.Postcard;
import com.postexchange.model.User;
import com.postexchange.network.SQLAccessor;
import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static com.postexchange.model.ResponseHelper.*;

/**
 *
 * @author jianqing
 */
@WebServlet(name = "MainServlet", urlPatterns =
{
    "/getPostcard", "/doLogin"
}, loadOnStartup = 1)
public class MainServlet extends HttpServlet
{

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
            throws ServletException, IOException
    {
        String path = request.getServletPath();//What's the slashtag client is requesting
        switch (path)
        {
            case "/getPostcard":
                processGetPostcardGET(request, response);
                break;
            case "/getUser":
                System.out.println("Get user!");
                break;
            //Handle other endpoints...
            default:
                writeResponse("Wrong method! You should try POST method instead for this endpoint.", "SYSERR", 405, response);
                break;

        }
        //processRequest(request, response);
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
            throws ServletException, IOException
    {
        //Determine which endpoint the user is trying to request
        String endpoint = request.getServletPath();

        //Handle the endpoints
        switch (endpoint)
        {
            case "/doLogin":
                processdoLoginPOST(request, response);
                break;

            //Handle other endpoints...
            default:
                writeResponse("Wrong method! You should try GET method instead for this endpoint.", "SYSERR", 405, response);
                break;
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo()
    {
        return "This is the main servlet of this website.";
    }// </editor-fold>

    //////// *** HANDLE EACH ENDPOINTS BELOW *** ////////
    /**
     * Example: Handle an endpoint called GetPostcard.  <br>
     * This endpoint accepts the postcard ID as the parameter and returns the
     * postcard object.<br>
     * This endpoint does not require authentication. <br>
     *
     * @param request The HTTPServletRequest Object directly taken from servlet
     * parameter.
     * @param response The response object.
     * @throws ServletException
     * @throws IOException
     */
    protected void processGetPostcardGET(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {

        //1. Start by getting the parameter of the postcard. 
        String postcardId = request.getParameter("id");

        //2. Validate the parameter. Is it empty? Is it an integer?
        if (!NumberUtil.isInteger(postcardId))//In case of invalid input
        {
            //2.1 Indicate that we can't find a valid parameter, also write the response back to the client.
            writeMissingParameter("Postcard ID must be an integer!", response);
            return; // Terminate the function.
        }

        //3. Database work here to retrieve postcard information
        try (SQLAccessor sql = SQLAccessor.getDefaultInstance())
        {
            Postcard pc = sql.getPostcardById(Integer.parseInt(postcardId)); //Retrieve postcard from the database
            writeOK(pc, response);//Write the response to the frontend.
        } catch (SQLException | ClassNotFoundException e)
        {
            writeError(e, response);//tell the frontend we are having an error.
        }

    }

    protected void processdoLoginPOST(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        //Retrieve the HTTPSession and parameters.
        HttpSession session = request.getSession();
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        final String emailPattern = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$"; // RegEx for validating email

        //is password hashed?
        if (password.length() != 32)
        {
            //return to frondend error
            writeMissingParameter("The password does not appear to be MD5 hashed. Please submit MD5 hashed password", response);
            return;
        }

        //is email valid?
        if (!ReUtil.isMatch(emailPattern, email))
        {
            writeMissingParameter("Missing valid email!", response);
            return;
        }

        //Connect to the database
        try (SQLAccessor sql = SQLAccessor.getDefaultInstance())
        {
            //Try to retrieve this pair from the database
            User user = sql.getUserByUsernamePassword(email, password);

            //If the email/password combination does not exist.
            if (user == null)
            {
                writeResponse("Email/password combination does not exist.", "USER_DNE", 401, response);
                return;
            }

            //Validate the user into our session
            session.setAttribute("user", user);

            //Send the data back to the client.
            writeOK(user, response);

        } catch (SQLException | ClassNotFoundException sqle)
        {
            //Tell the client that we have an error.
            writeError(sqle, response);
        }

    }

}
