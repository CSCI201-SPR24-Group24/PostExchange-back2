/*
 * Author: jianqing
 * Date: Apr 6, 2024
 * Description: This document is created for
 */
package com.postexchange.model;

import cn.hutool.json.JSONObject;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

/**
 * @deprecated @author jianqing
 */
public class ResponseHelperOld
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
     * processResponse(jsonObj, user, "OK", 200, response); <br>
     * }catch(Exception ex)<br>
     * {<br>
     * processResponse(jsonObj, ex.toString(), "XXXERROR", 500, response);<br>
     * }<br>
     * jsonObj.write(response.getWriter());<br>
     * </code>
     *
     * @param jsonr the response JSON to write to.
     * @param data The data that should be returned to the client if the
     * response was success. Otherwise, write the message here.
     * @param statusString "OK" for success. Otherwise, write a error code
     * reflecting what happened. e.g. NOLOGIN, NOPARAM, etc.
     * @param httpCode The standard HTTP status code to return to the .
     * @param response The HttpServletResponse object obtained from the servlet.
     * @throws ServletException
     * @throws IOException
     */
    public static void processResponse(JSONObject jsonr, Object data, String statusString, int httpCode, HttpServletResponse response) throws ServletException, IOException
    {
        jsonr.set("status", statusString);
        jsonr.set("data", data);
        response.setContentType("application/json;charset=utf-8");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(httpCode);
    }

    ////Below are some shortcut methods that's commonly used.
    /**
     * Indicate that we are missing required parameter from the client. Write
     * the needed response to the JSON.
     *
     * @param jsonr An empty JSON object to write to.
     * @param resp The response object from servlet.
     * @throws ServletException
     * @throws IOException
     */
    public static void processNoParam(JSONObject jsonr, HttpServletResponse resp) throws ServletException, IOException
    {
        processResponse(jsonr, "Required parameters are not found!", "NOPARAM", 400, resp);
    }

    /**
     * Indicate that we are missing required parameter from the client with
     * custom message. Write the needed response to the JSON.
     *
     * @param jsonr An empty JSON object to write to.
     * @param message The custom message to send back.
     * @param resp The response object from servlet.
     * @throws ServletException
     * @throws IOException
     */
    public static void processNoParam(JSONObject jsonr, String message, HttpServletResponse resp) throws ServletException, IOException
    {
        processResponse(jsonr, message, "NOPARAM", 400, resp);
    }

    /**
     * Indicate that the user tried to access an login required endpoint without
     * logging in.
     *
     * @param jsonr The response JSON object to write to.
     * @param resp The <code>HttpServletResponse</code> object obtained from the
     * servlet.
     * @throws ServletException
     * @throws IOException
     */
    public static void processNoLogin(JSONObject jsonr, HttpServletResponse resp) throws ServletException, IOException
    {
        processResponse(jsonr, "You need to login first to use this function. Please login!", "NOLOGIN", 400, resp);
    }

    /**
     * Indicate that the request was processed OK. Return the requested data
     * from here.
     *
     * @param jsonr The response JSON object to write to.
     * @param data The "data" the user is requesting.
     * @param response The <code>HttpServletResponse</code> object obtained from
     * the servlet.
     * @throws ServletException
     * @throws IOException
     */
    public static void processOK(JSONObject jsonr, Object data, HttpServletResponse response) throws ServletException, IOException
    {
        processResponse(jsonr, data, "OK", 200, response);
    }

    /**
     * Indicate that the there was an exception thrown.
     *
     * @param jsonr The response JSON object to write to.
     * @param err The exception object in the catch statement.
     * @param response The <code>HttpServletResponse</code> object obtained from
     * the servlet.
     * @throws ServletException
     * @throws IOException
     */
    public static void processSystemError(JSONObject jsonr, Throwable err, HttpServletResponse response) throws ServletException, IOException
    {
        logError(err);
        processResponse(jsonr, err, "SYSERR", 500, response);
    }

    public static void logError(Throwable dddd)
    {
        //TODO: connect to oss service

        dddd.printStackTrace();
    }

}
