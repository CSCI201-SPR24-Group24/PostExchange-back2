/*
 * Author: jianqing
 * Date: Mar 26, 2024
 * Description: This document is created for main servlet of our application.
 */
package com.postexchange.controller;

import cn.hutool.core.net.multipart.MultipartFormData;
import cn.hutool.core.net.multipart.UploadFile;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.postexchange.model.Postcard;
import com.postexchange.model.User;
import com.postexchange.network.OSSAccessor;
import com.postexchange.network.SQLAccessor;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import static com.postexchange.model.ResponseHelper.*;

/**
 * @author jianqing
 */
@WebServlet(name = "MainServlet", urlPatterns =

        {
                "/getPostcard", "/doLogin", "/doRegisterUser", "/getRecentPostcardsWithImage", "/getHomepageData", "/getRecentActivities", "/getUser",
                "/createPostcard", "/getRandUser", "/updatePostcardImage", "/doLogout", "/doTest", "/deleteUser", "/markReceived", "/getpostcardNotReceived",
                "/searchUser", "/getLoggedInUser", "/uploadFile", "/getPersonalGallery", "/saveProfile", "/getGlobalGallery"

        }, loadOnStartup = 1)
public class MainServlet extends HttpServlet {

    private ExecutorService executor;
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">

    @Override
    public void init() {
        executor = Executors.newCachedThreadPool();
    }

    @Override
    public void destroy() {
        executor.shutdownNow();
    }


    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();//What's the slashtag client is requesting
        String origin = request.getHeader("Origin");
        // Check if the origin is from localhost with any port
        // if (origin != null && origin.matches("^http://localhost(:\\d+)?$")) {
        // Set CORS headers

        // }
        switch (path) {
            case "/getPostcard":
                processGetPostcardGET(request, response);
                break;
            case "/getUser":
                System.out.println("Get user!!");
                break;
            case "/getRecentPostcardsWithImage":
                processGetRecentPostcardsWithImageGET(request, response);
                break;
            case "/getRecentActivities":
                // get recent sitewise postcard transaction
                processGetRecentActivitiesGET(request, response);
                break;
            case "/getHomepageData":
                processGetHomepageDataGET(request, response);
                break;
            case "/getRandUser":
                processGetRandomUser(request, response);
                break;
            case "/updatePostcardImage":
                processGetUpdatePostcardImage(request, response);
                break;
            case "/doTest":
                writeOK("This is a test", response, request);
                break;
            case "/doLogout":
                request.getSession().removeAttribute("user");
                writeOK("You have been logged out!", response, request);
                break;
            case "/searchUser":
                processSearchUserGET(request, response);
                break;
            case "/getpostcardNotReceived":
                processpostcardNotReceivedPOST(request, response);
                break;
            case "/getLoggedInUser":
                writeOK(request.getSession().getAttribute("user"), response, request);
                break;
            case "/getPersonalGallery":
                processPersonalGalleryGET(request, response);
                break;
            case "/getGlobalGallery":
                processGlobalGalleryGET(request, response);
                break;
            //Handle other endpoints...
            default:
                writeResponse("Wrong method! You should try POST method instead for this endpoint.", "SYSERR", 405, request, response);
                break;

        }
        //processRequest(request, response);
    }


    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //Determine which endpoint the user is trying to request
        String endpoint = request.getServletPath();

        String origin = request.getHeader("Origin");
        // Check if the origin is from localhost with any port
        //if (origin != null && origin.matches("^http:\\/\\/localhost(:\\d+)?$")) {
        // Set CORS headers
        response.addHeader("Access-Control-Allow-Origin", origin);
        response.addHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PUT");
        response.addHeader("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With, remember-me");
        response.addHeader("Access-Control-Allow-Credentials", "true");
        //}
        //Handle the endpoints
        switch (endpoint) {
            case "/doLogin":
                processdoLoginPOST(request, response);
                break;
            case "/doRegisterUser":
                processDoRegisterPOST(request, response);
                break;
            case "/createPostcard":
                processCreatePostcardPOST(request, response);
                break;
            case "/deleteUser":
                processDeleteUser(request, response);
                break;
            case "/markReceived":
                processMarkReceivedPOST(request, response);
                break;
            case "/uploadFile":
                processUploadFilePOST(request, response);
                break;
            case "/saveProfile":
                processSaveProfilePOST(request, response);
                break;

            //Handle other endpoints...
            default:
                writeResponse("Wrong method! You should try GET method instead for this endpoint.", "SYSERR", 405, request, response);
                break;
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "This is the main servlet of this website. ";
    }// </editor-fold>

    //////// *** HANDLE EACH ENDPOINTS BELOW *** ////////
    protected void processSaveProfilePOST(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        if (user == null) {
            writeNotLoggedIn(request, response);
            return;
        }

        String userName = request.getParameter("userName");
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String userBio = request.getParameter("userBio");
        String userCountry = request.getParameter("userCountry");
        String userImage = request.getParameter("userImage");

        if (!StrUtil.isAllNotEmpty(userName, firstName, lastName, userCountry)) {
            writeMissingParameter("some of the required params are missing!", request, response);
            return;
        }

        if (userBio.length() > 6000) {
            writeInvalidParameter("User bio should be 6000 characrers or shorter.", request, response);
            return;
        }

        try (SQLAccessor sql = SQLAccessor.getDefaultInstance()) {
            user.setUserName(userName);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setUserBio(userBio);
            user.setUserCountry(userCountry);
            user.setProfilePicture(userImage);
            sql.updateUserProfile(user);
            writeOK(user, response, request);
        } catch (SQLException | ClassNotFoundException e) {
            writeError(e, request, response);
        }
    }

    protected void processUploadFilePOST(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        MultipartFormData body = ServletUtil.getMultipart(request);
        UploadFile file = body.getFile("file");
        String name = file.getFileName(); // get the file name
        String[] frga = name.split(Pattern.quote(".")); // get the main "name" and the extension
        String postfix = frga[frga.length - 1]; // this is the extension  "myjpg.me.png"
        String realName = RandomUtil.randomString(10) + "." + postfix; // the "id" for the file, add this to database
        String path = "img/" + realName;// file path relative to the root of the bucket
        JSONObject jsonr = JSONUtil.createObj();

        try {
            OSSAccessor.uploadStream(path, file.getFileInputStream());
            //Upload was ok
            jsonr.set("tag", realName);
            writeOK(jsonr, response, request);
        } catch (Exception ex) {
            writeError(ex, request, response);
        }

    }

    protected void processGlobalGalleryGET(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String limPara = request.getParameter("limit");

        HttpSession session = request.getSession();
        try (SQLAccessor sql = SQLAccessor.getDefaultInstance()) {
            int limit = 0;
            try {
                limit = Integer.parseInt(limPara);
            } catch (NumberFormatException nfe) {
                limit = 10;
            }
            Postcard[] globalPostcards = sql.getGlobalGallery(limit);
            writeOK(globalPostcards, response, request);
        } catch (SQLException | ClassNotFoundException e) {
            writeError(e, request, response);
        }
    }

    protected void processPersonalGalleryGET(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        if (user == null) {
            writeNotLoggedIn(request, response);
            return;
        }

        try (SQLAccessor sql = SQLAccessor.getDefaultInstance()) {
            JSONArray postcards = sql.getPostcardsByUserId(user.getUserId());
            writeOK(postcards, response, request);
        } catch (SQLException | ClassNotFoundException e) {
            writeError(e, request, response);
        }
    }

    protected void processSearchUserGET(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //Get the session
        /*HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");*/

        //Get the search query from the request
        String searchQuery = request.getParameter("q");

        //Check if the search query is missing
        if (searchQuery == null) {
            writeMissingParameter("Search query is missing", request, response);
            return;
        }

        //Connect to the database
        try (SQLAccessor sql = SQLAccessor.getDefaultInstance()) {
            //Search for the user
            ArrayList<User> users = sql.searchUser(searchQuery);
            writeOK(users, response, request);
        } catch (SQLException | ClassNotFoundException e) {
            writeError(e, request, response);
        }
    }

    protected void processMarkReceivedPOST(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //Get the session
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        //Check if the user is logged in
        if (user == null) {
            writeNotLoggedIn(request, response);
            return;
        }

        //Get the postcard data from the request, update timeRecieved and add to users numRecieved
        try (SQLAccessor sql = SQLAccessor.getDefaultInstance()) {
            sql.updateNumSent(user);
            int postcardId = Integer.parseInt(request.getParameter("postcardID"));
            Postcard cardToUpdate = sql.getPostcardById(postcardId);
            cardToUpdate.setTimeReceived(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            sql.updatepostcardtimeRecieved(cardToUpdate);

            //In the websocket, tell everybody that someone received a postcard!
            //executor.execute(() -> { // There should not be a new thread for sql.
            //try {
            //User toUser = sql.getUserById(Integer.parseInt(userTo));
            User fromUser = sql.getUserById(cardToUpdate.getUserIDSent());
            JSONObject activity = JSONUtil.createObj();
            activity.set("postcardId", postcardId);
            activity.set("fromUserName", fromUser.getUserName());
            activity.set("fromUserId", cardToUpdate.getUserIDSent());
            activity.set("fromUserCountry", fromUser.getUserCountry());
            activity.set("toUserName", user.getUserName());
            activity.set("toUserId", user.getUserId());
            activity.set("toUserCountry", user.getUserCountry());
            JSONObject jsonr = JSONUtil.createObj();
            jsonr.set("data", activity);
            jsonr.set("type", "RECEIVE");
            ActivityWebSocket.broadCast(jsonr.toString());
            writeOK("OK", response, request);
        } catch (SQLException | ClassNotFoundException e) {
            writeError(e, request, response);
        }catch(NumberFormatException e){
            writeInvalidParameter("Invalid postcard ID", request, response);
        }
    }

    protected void processpostcardNotReceivedPOST(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        if (user == null) {
            writeNotLoggedIn(request, response);
            return;
        }

        try (SQLAccessor sql = SQLAccessor.getDefaultInstance()) {
            JSONArray postCards = sql.getPostCardNotreceived(user);
            writeOK(postCards, response, request);
        } catch (SQLException | ClassNotFoundException e) {
            writeError(e, request, response);
        }


    }

    protected void processDeleteUser(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //Get the session
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        //Check if the user is logged in
        if (user == null) {
            writeNotLoggedIn(request, response);
            return;
        }

        //Connect to the database
        try (SQLAccessor sql = SQLAccessor.getDefaultInstance()) {
            //Delete the user
            sql.deleteUser(user);
            writeOK("OK deleteUser", response, request);
        } catch (SQLException | ClassNotFoundException e) {
            writeError(e, request, response);
        }
    }

    protected void processGetUpdatePostcardImage(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //Get the session
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        //Check if the user is logged in
        if (user == null) {
            writeNotLoggedIn(request, response);
            return;
        }

        //get the postcardid and image tag as parameters
        String postcardId = request.getParameter("postcardId");
        String imageTag = request.getParameter("imageTag");

        //Check if the parameters are missing
        if (postcardId == null || imageTag == null) {
            writeMissingParameter(request, response);
            return;
        }

        //Check if the postcardId is an integer
        if (!NumberUtil.isInteger(postcardId)) {
            writeInvalidParameter("Postcard ID must be an integer", request, response);
            return;
        }

        //Connect to the database
        try (SQLAccessor sql = SQLAccessor.getDefaultInstance()) {
            //Update the postcard image
            sql.updatePostcardImage(Integer.parseInt(postcardId), imageTag);
            writeOK("We have updated the postcard image!", response, request);
        } catch (SQLException | ClassNotFoundException e) {
            writeError(e, request, response);
        }


    }


    protected void processGetRandomUser(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        if (user == null) {
            writeResponse("You are not logged in!", "NOLOGIN", 401, request, response);
        }

        try (SQLAccessor sql = SQLAccessor.getDefaultInstance()) {
            User randomUser = sql.getRandomUser(user);
            writeOK(randomUser, response, request);
        } catch (SQLException | ClassNotFoundException e) {
            writeError(e, request, response);
        }

    }


    protected void processCreatePostcardPOST(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //Get the session
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        //Check if the user is logged in
        if (user == null) {
            writeNotLoggedIn(request, response);
            return;
        }

        //Get the postcard data from the request, parameters are userFrom, userTo, imageTag
        String userFrom = user.getUserId();

        String userTo = request.getParameter("userTo");
        String imageTag = request.getParameter("imageTag");

        //Check if the parameters are missing
        if (userTo == null) {
            writeMissingParameter(request, response);
            return;
        }

//Create a new postcard object
        try (SQLAccessor sql = SQLAccessor.getDefaultInstance()) {
            Postcard postcard = new Postcard();
            postcard.setUserIDSent(Integer.parseInt(userFrom));
            postcard.setUserIDReceived(Integer.parseInt(userTo));
            postcard.setPostcardImage(imageTag);

            //Connect to the database
            // try  {
            //Insert the postcard into the database
            postcard.setTimeSent(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            int postcardId = sql.insertPostcard(postcard);
            //Set the current time for the postcard YYYY-MM-DD

            postcard.setPostcardID(postcardId);
            // executor.execute(()->{
            //try {
            User toUser = sql.getUserById(Integer.parseInt(userTo));
            JSONObject activity = JSONUtil.createObj();
            activity.set("postcardId", postcardId);
            activity.set("fromUserName", user.getUserName());
            activity.set("fromUserId", user.getUserId());
            activity.set("fromUserCountry", user.getUserCountry());
            activity.set("toUserName", toUser.getUserName());
            activity.set("toUserId", Integer.parseInt(userTo));
            activity.set("toUserCountry", toUser.getUserCountry());
            JSONObject jsonr = JSONUtil.createObj(); // JSON Object to be sent to the socket
            jsonr.set("data", activity);
            jsonr.set("type", "SEND");
            ActivityWebSocket.broadCast(jsonr.toString());
            writeOK(postcard, response, request);

            // });
            //write the response back

            //do another sql here - ??

            //send activities to the other users, if failed does not impact send back
            //}

        } catch (NumberFormatException e) {
            writeInvalidParameter("Invalid user ID", request, response);
        } catch (SQLException | ClassNotFoundException e) {
            writeError(e, request, response);
        }

        //writeResponse(null, "SYSERR", 500, request, response);

    }


    /**
     * Example: Handle an endpoint called GetHomepageData.  <br>
     *
     * @param request  The HTTPServletRequest Object directly taken from servlet
     * @param response The response object.
     * @throws ServletException If there is a servlet error.
     * @throws IOException      If there is an I/O error.
     */
    protected void processGetHomepageDataGET(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //Connect to the database
        try (SQLAccessor sql = SQLAccessor.getDefaultInstance()) {
            // Get the data from the database
            JSONArray activities = sql.getRecentActivities(10);
            Postcard[] postcards = sql.getRecentPostcardsWithImage(10);
            int numMembers = sql.getNumMembers();
            int numPostcardReceived = sql.getNumPostcardReceived();
            int numPostcardTravelling = sql.getNumPostcardTravelling();
            int numPostcardReceived6Months = sql.getNumPostcardReceived6Months();
            int numDonatedLast6Months = sql.getNumDonatedLast6Months();

            // Prepare the data to send back to the client
            JSONObject data = JSONUtil.createObj();
            data.set("activities", activities);
            data.set("postcards", postcards);
            data.set("numMembers", numMembers);
            data.set("numPostcardReceived", numPostcardReceived);
            data.set("numPostcardTravelling", numPostcardTravelling);
            data.set("numPostcardReceived6Months", numPostcardReceived6Months);
            data.set("numDonatedLast6Months", numDonatedLast6Months);
            writeOK(data, response, request);
        } catch (SQLException | ClassNotFoundException e) {
            writeError(e, request, response);
        }
    }

    protected void processGetRecentActivitiesGET(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String limitStr = request.getParameter("limit");
        int limit;
        try {
            limit = Integer.parseInt(limitStr);
        } catch (NumberFormatException nfe) {
            limit = 10;
            return;
        }

        //Connect to the database
        try (SQLAccessor sql = SQLAccessor.getDefaultInstance()) {
            JSONArray activities = sql.getRecentActivities(limit);
            writeOK(activities, response, request);
        } catch (SQLException | ClassNotFoundException e) {
            writeError(e, request, response);
        }
    }

    protected void processGetRecentPostcardsWithImageGET(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String limitStr = request.getParameter("limit");
        int limit;
        try {
            limit = Integer.parseInt(limitStr);
        } catch (NumberFormatException nfe) {
            limit = 10;
            return;
        }

        //Connect to the database
        try (SQLAccessor sql = SQLAccessor.getDefaultInstance()) {
            Postcard[] postcards = sql.getRecentPostcardsWithImage(limit);
            writeOK(postcards, response, request);

        } catch (SQLException | ClassNotFoundException e) {
            writeError(e, request, response);
        }
    }

    protected void processDoRegisterPOST(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //Get creds from front end
        final String userName = request.getParameter("userName");
        final String email = request.getParameter("email");
        final String password = request.getParameter("password");
        final String firstName = request.getParameter("firstName");
        final String lastName = request.getParameter("lastName");
        final String country = request.getParameter("country");
        final String userBio = request.getParameter("userBio");
        int USERBIOMAX = 6000;

        //Validation
        if (password.length() != 32) {
            writeInvalidParameter("Password must be MD5 hashed. Got you:) ", request, response);
            return;
        }

        if (!isEmail(email)) {
            writeInvalidParameter("The email is in invalid format.", request, response);
            return;
        }

        if (userBio.length() > 6000) {
            writeInvalidParameter("User bio should be 6000 characrers or shorter.", request, response);
            return;
        }

        //Prepare obj
        final User user = new User();
        user.setUserName(userName);
        user.setEmail(email);
        user.setPassword(password);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUserCountry(country);
        user.setUserBio(userBio);


        //Try to register this user to db
        try (SQLAccessor sql = SQLAccessor.getDefaultInstance()) {
            final JSONObject dataObj = JSONUtil.createObj();
            final int id = sql.registerNewUserInDb(user);
            //dataObj.set("userId", id);
            user.setUserId(Integer.toString(id));
            writeOK(user, response, request);
        } catch (SQLException sqle) {
            switch (sqle.getErrorCode()) {
                case 1062:
                    //duplicate entry for unique index 1062
                    writeResponse("The email is already registered", "USER_EXISTS", 401, request, response);
                    break;
                case 1406:
                    //Length constraint violation 1406
                    writeInvalidParameter("One of the fields is too long! Validate before submit:)", request, response);
                    break;
                default:
                    //Other unknown error
                    writeError(sqle, request, response);
                    break;
            }
        } catch (Exception nx) {
            writeError(nx, request, response);
        }

    }


    /**
     * Example: Handle an endpoint called GetPostcard.  <br>
     * This endpoint accepts the postcard ID as the parameter and returns the
     * postcard object.<br>
     * This endpoint does not require authentication. <br>
     *
     * @param request  The HTTPServletRequest Object directly taken from servlet
     *                 parameter.
     * @param response The response object.
     * @throws ServletException
     * @throws IOException
     */
    protected void processGetPostcardGET(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        //1. Start by getting the parameter of the postcard. 
        String postcardId = request.getParameter("id");

        //2. Validate the parameter. Is it empty? Is it an integer?
        if (!NumberUtil.isInteger(postcardId))//In case of invalid input
        {
            //2.1 Indicate that we can't find a valid parameter, also write the response back to the client.
            writeMissingParameter("Postcard ID must be an integer!", request, response);
            return; // Terminate the function.
        }

        //3. Database work here to retrieve postcard information
        try (SQLAccessor sql = SQLAccessor.getDefaultInstance()) {
            Postcard pc = sql.getPostcardById(Integer.parseInt(postcardId)); //Retrieve postcard from the database
            writeOK(pc, response, request);//Write the response to the frontend.
        } catch (SQLException | ClassNotFoundException e) {
            writeError(e, request, response);//tell the frontend we are having an error.
        }

    }


    protected void processdoLoginPOST(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //Retrieve the HTTPSession and parameters.


        HttpSession session = request.getSession();
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        final String emailPattern = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$"; // RegEx for validating email

        //is password hashed?
        if (password.length() != 32) {
            //return to frondend error
            writeMissingParameter("The password does not appear to be MD5 hashed. Please submit MD5 hashed password", request, response);
            return;
        }

        //is email valid?
        if (!ReUtil.isMatch(emailPattern, email)) {
            writeMissingParameter("Missing valid email!", request, response);
            return;
        }

        //Connect to the database
        try (SQLAccessor sql = SQLAccessor.getDefaultInstance()) {
            //Try to retrieve this pair from the database
            User user = sql.getUserByEmailPassword(email, password);

            //If the email/password combination does not exist.
            if (user == null) {
                writeResponse("Email/password combination does not exist.", "USER_DNE", 401, request, response);
                return;
            }

            //Validate the user into our session
            session.setAttribute("user", user);


            response.reset();
            Cookie ckk = new Cookie("JSESSIONID", session.getId());
            ckk.setSecure(true);
            ckk.setPath("/");
            response.addCookie(ckk);
            //Send the data back to the client.
            writeOK(user, response, request);

        } catch (SQLException | ClassNotFoundException sqle) {
            //Tell the client that we have an error.
            writeError(sqle, request, response);
        }

    }

}
