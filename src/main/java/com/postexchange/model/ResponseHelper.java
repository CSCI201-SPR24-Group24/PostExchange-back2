/*
 * Author: jianqing
 * Date: Apr 6, 2024
 * Description: This document is created for writing response back to the client.
 */
package com.postexchange.model;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

/**
 * These methods include creating the JSON object and writing response data back
 * to the client according to the status.
 *
 * @author jianqing
 */
public class ResponseHelper
{

    /**
     * This method writes the API data and response code to the response JSON
     * object according to our standard format. Then, flush the data to the
     * client.<br>
     * Example:<br>
     * <code>
     * protected void processGetUser(HttpRequest request, HttpResponse response)<br>
     *
     * {<br>
     * \\retrieve user from database<br>
     * try{<br>
     * User user = db.getUser(id);<br>
     * JSONObject jsonObj = JSONUtil.createObj();<br>
     * writeResponse(jsonObj, user, "OK", 200, response); <br>
     * }catch(Exception ex)<br>
     * {<br>
     * writeResponse(jsonObj, ex.toString(), "XXXERROR", 500, response);<br>
     * }<br>
     * jsonObj.write(response.getWriter());<br>
     * </code>
     *
     * @param data The data that should be returned to the client if the
     * response was success. Otherwise, write the message here.
     * @param statusString "OK" for success. Otherwise, write a error code
     * reflecting what happened. e.g. NOLOGIN, NOPARAM, etc.
     * @param httpCode The standard HTTP status code to return to the .
     * @param response The HttpServletResponse object obtained from the servlet.
     * @throws ServletException
     * @throws IOException
     */
    public static void writeResponse(Object data, String statusString, int httpCode, HttpServletResponse response) throws ServletException, IOException
    {
        JSONObject jsonr = JSONUtil.createObj();
        jsonr.set("status", statusString);
        jsonr.set("data", data);
        response.setContentType("application/json;charset=utf-8");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(httpCode);
        try (PrintWriter out = response.getWriter())
        {
            jsonr.write(out);
        }
    }

    ////Below are some shortcut methods that's commonly used.
    /**
     * Indicate that we are missing required parameter from the client. Write
     * the needed response to the JSON.
     *
     * @param resp The response object from servlet.
     * @throws ServletException
     * @throws IOException
     */
    public static void writeMissingParameter(HttpServletResponse resp) throws ServletException, IOException
    {
        writeResponse("Required parameters are not found!", "NOPARAM", 400, resp);
    }

    /**
     * Indicate that we are missing required parameter from the client with
     * custom message. Write the needed response to the JSON.
     *
     * @param message The custom message to send back.
     * @param resp The response object from servlet.
     * @throws ServletException
     * @throws IOException
     */
    public static void writeMissingParameter(String message, HttpServletResponse resp) throws ServletException, IOException
    {
        writeResponse(message, "NOPARAM", 400, resp);
    }

    /**
     * Indicate that the user tried to access an login required endpoint without
     * logging in.
     *
     * @param resp The <code>HttpServletResponse</code> object obtained from the
     * servlet.
     * @throws ServletException
     * @throws IOException
     */
    public static void writeNotLoggedIn(HttpServletResponse resp) throws ServletException, IOException
    {
        writeResponse("You need to login first to use this function. Please login!", "NOLOGIN", 400, resp);
    }

    /**
     * Indicate that the request was processed OK. Return the requested data
     * from here.
     *
     * @param data The "data" the user is requesting.
     * @param response The <code>HttpServletResponse</code> object obtained from
     * the servlet.
     * @throws ServletException
     * @throws IOException
     */
    public static void writeOK(Object data, HttpServletResponse response) throws ServletException, IOException
    {
        writeResponse(data, "OK", 200, response);
    }

    /**
     * Indicate that the there was an exception thrown.
     *
     * @param err The exception object in the catch statement.
     * @param response The <code>HttpServletResponse</code> object obtained from
     * the servlet.
     * @throws ServletException
     * @throws IOException
     */
    public static void writeError(Throwable err, HttpServletResponse response) throws ServletException, IOException
    {
        //logError(err);
        writeResponse(err, "SYSERR", 500, response);
    }

    /*public static void logError(Throwable dddd)
    {
        //TODO: connect to oss service
        
        dddd.printStackTrace();
    }*/
}
