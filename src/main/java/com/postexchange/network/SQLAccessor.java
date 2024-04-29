/*
 * Author: jianqing
 * Date: Mar 26, 2024
 * Description: This document is created for
 */
package com.postexchange.network;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.setting.Setting;
import com.postexchange.model.Postcard;
import com.postexchange.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * @author jianqing
 */
public class SQLAccessor implements AutoCloseable {

    private java.sql.Connection dbConn;
    //add to users mark as recieved

    public static SQLAccessor getDefaultInstance() throws SQLException, ClassNotFoundException {
        SQLAccessor d = new SQLAccessor();
        Setting s = new Setting("db.setting");
        d.connect(s.get("url"), s.get("user"), s.get("pass"), true);
        return d;
    }

    public User getUserById(int id) throws SQLException
    {
        String sql = "SELECT * FROM users WHERE userId = ?";
        PreparedStatement ps = dbConn.prepareStatement(sql);
        ps.setInt(1,id);
        ResultSet rs = ps.executeQuery();
        if(rs.next())
            return getUserFromResultSet(rs);
        return null;
    }

    public static void main(String[] args) {
        //try-with-resources
        try (SQLAccessor sql = getDefaultInstance()) {

           /*User user = new User();
           user.setUserId("1");
            user.setUserName("baba");
            user.setEmail("m@g.c");
            user.setPassword("e10");
            user.setFirstName("John2");
            user.setLastName("Doe2");
            user.setUserCountry("US");
            user.setUserBio("Hahahahaha I am not crazy!");
            sql.updateUserProfile(user);

            //User user =  new User();
            System.out.println(sql.searchUser("john"));*/
            User u=sql.getgetgetget("' OR 1=1; -- ","1234567");
            System.out.println(u);
            //user.setUserId("3");
            //System.out.println(sql.getPostCardNotreceived(user));
            //System.out.println(sql.getUserByUsernamePassword("johndoe@example.com", "482c811da5d5b4bc6d497ffa98491e38"));
            //System.out.println(sql.getPostcardById(1));
            //System.out.println(sql.getNumberofUsers());

            //System.out.println(sql.getRandomUser());
            //sql.deleteUser(user);
            //sql.updatePostcardImage(1, "xxx");




            System.out.println("Yay!");
        } catch (SQLException ex) {
            if (ex.getErrorCode() == 1062)
                System.out.println("Duplicate email!");
            else
                Logger.getLogger(SQLAccessor.class.getName()).log(Level.SEVERE, null, ex);

        } catch (ClassNotFoundException nx) {
            Logger.getLogger(SQLAccessor.class.getName()).log(Level.SEVERE, null, nx);
        }
    }

    public ArrayList<User> searchUser(String keyword) throws SQLException {
        PreparedStatement ps = dbConn.prepareStatement("SELECT * FROM users WHERE userName LIKE ? OR email LIKE ?");
        ps.setString(1, "%" + keyword + "%");
        ps.setString(2, "%" + keyword + "%");
        ResultSet rs = ps.executeQuery();
        ArrayList<User> users = new ArrayList<>();
        while (rs.next())
            users.add(getUserFromResultSet(rs));
        return users;
    }

    public void updateUserReceived(User user) throws SQLException {
        PreparedStatement ps = dbConn.prepareStatement("UPDATE users SET numReceived=? WHERE userId=?");

        ps.setInt(1, user.getNumberReceived() + 1);
        ps.setInt(2, Integer.parseInt(user.getUserId()));
        ps.executeUpdate();
    }

    //updateUserProfile

    //update postcard time recieved
    public void updatepostcardtimeRecieved(Postcard postcard) throws SQLException {
        PreparedStatement ps = dbConn.prepareStatement("UPDATE postcards SET timeReceived=? WHERE postcardID=?");
        ps.setString(1, postcard.getTimeReceived());
        ps.setInt(2, postcard.getPostcardID());
        ps.executeUpdate();
    }

    public void updateNumSent(User user) throws SQLException {
        PreparedStatement ps = dbConn.prepareStatement("UPDATE users SET numSent=? WHERE userId=?");
        ps.setInt(1, user.getNumberSent() + 1);
        ps.setInt(2, Integer.parseInt(user.getUserId()));
        ps.executeUpdate();
    }

    public void updateUserProfile(User user) throws SQLException {
        PreparedStatement ps = dbConn.prepareStatement("UPDATE users SET email=?, firstName=?, lastName=?, userCountry=?, userBio=?, profilePicture=?,userName=? WHERE userId=?");
        ps.setString(1, user.getEmail());
        ps.setString(2, user.getFirstName());
        ps.setString(3, user.getLastName());
        ps.setString(4, user.getUserCountry());
        ps.setString(5, user.getUserBio());
        ps.setString(6, user.getProfilePicture());
        ps.setString(7,user.getUserName());
        ps.setInt(8, Integer.parseInt(user.getUserId()));
        ps.executeUpdate();
    }

    //get all send and recieve postcards for this userid
    public JSONArray getPostcardsByUserId(String userId) throws SQLException{

        Statement s = dbConn.createStatement();
        ResultSet rs = s.executeQuery("SELECT * FROM postcards WHERE userIDSent=" + userId + " OR userIDReceived=" + userId);

        JSONArray result = new JSONArray();

        while(rs.next()){
            JSONObject jsonObject = new JSONObject();
            jsonObject.set("postcardId", rs.getInt(1));
            jsonObject.set("timeSent", rs.getString(2));
            jsonObject.set("timeReceived", rs.getString(3));
            jsonObject.set("userIDSent", rs.getInt(4));
            jsonObject.set("userIDReceived", rs.getInt(5));
            jsonObject.set("postcardImage", rs.getString(6));
            jsonObject.set("postcardMessage", rs.getString(7));
            result.add(jsonObject);
        }

        return result;

    }

    //random postcard

    public void deleteUser(User user) throws SQLException {
        PreparedStatement ps = dbConn.prepareStatement("DELETE FROM users WHERE userId=?");
        ps.setInt(1, Integer.parseInt(user.getUserId()));
        ps.executeUpdate();
    }

    //insert postcard

    //updateUserProfile

    public void updateUserProfile(User user) throws SQLException
    {
        PreparedStatement ps = dbConn.prepareStatement("UPDATE users SET email=?, firstName=?, lastName=?, userCountry=?, userBio=?, profilePicture=? WHERE userId=?");
        ps.setString(1, user.getEmail());
        ps.setString(2, user.getFirstName());
        ps.setString(3, user.getLastName());
        ps.setString(4, user.getUserCountry());
        ps.setString(5, user.getUserBio());
        ps.setString(6, user.getProfilePicture());
        ps.setInt(7, Integer.parseInt(user.getUserId()));
        ps.executeUpdate();
    }

    //updatePostcardImage
    public void updatePostcardImage(int postcardId, String postcardImage) throws SQLException {
        PreparedStatement ps = dbConn.prepareStatement("UPDATE postcards SET postcardImage=? WHERE postcardID=?");
        ps.setString(1, postcardImage);
        ps.setInt(2, postcardId);
        ps.executeUpdate();
    }

    public User getRandomUser(User userCalling) throws SQLException {
        String sql = "SELECT * FROM users WHERE userId != ? ORDER BY RAND() LIMIT 1";

        // Use PreparedStatement for safer parameter insertion
        PreparedStatement ps = dbConn.prepareStatement(sql);

        // Set the currentUserId as the parameter
        ps.setInt(1, Integer.parseInt(userCalling.getUserId()));

        ResultSet rs = ps.executeQuery();
        return rs.next() ? getUserFromResultSet(rs) : null;

        //

    }

    /**
     * Insert a postcard into the database.
     *
     * @param postcard to be created
     * @return the id for the new postcard
     * @throws SQLException
     */
    public int insertPostcard(Postcard postcard) throws SQLException {
        PreparedStatement ps = dbConn.prepareStatement("INSERT INTO postcards (timeSent, timeReceived, userIDSent, userIDReceived, postcardImage, postcardMessage) VALUES(?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, postcard.getTimeSent());
        ps.setString(2, postcard.getTimeReceived());
        ps.setInt(3, postcard.getUserIDSent());
        ps.setInt(4, postcard.getUserIDReceived());
        ps.setString(5, postcard.getPostcardImage());
        ps.setString(6, postcard.getPostcardMessage());
        ps.executeUpdate();
        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            return rs.getInt(1);
        }
        return -1;
    }

    public JSONArray getPostCardNotreceived(User user) throws SQLException {
        PreparedStatement ps = dbConn.prepareStatement("SELECT postcardId,postcardImage, userIDSent, u1.userName AS userNameSent, u1.userCountry AS userIDSent, userIDReceived, u2.userName AS userNameSent, u2.userCountry AS userCountryReceived FROM `postcards`\n" +
                "LEFT JOIN users u1\n" +
                "ON postcards.userIDSent = u1.userId\n" +
                "LEFT JOIN users u2\n" +
                "ON postcards.userIDReceived = u2.userId \n" +
                "WHERE postcards.userIDReceived=? AND postcards.timeReceived IS NULL;");
        ps.setInt(1, Integer.parseInt(user.getUserId()));
        ResultSet rs = ps.executeQuery();

        JSONArray result = new JSONArray();
        while (rs.next()) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.set("postcardId", rs.getInt(1));
            jsonObject.set("postcardImage", rs.getString(2));
            jsonObject.set("userIDSent", rs.getInt(3));
            jsonObject.set("userNameSent", rs.getString(4));
            jsonObject.set("userCountrySent", rs.getString(5));
            jsonObject.set("userIDReceived", rs.getInt(6));
            jsonObject.set("userNameReceived", rs.getString(7));
            jsonObject.set("userCountryReceived", rs.getString(8));
            result.add(jsonObject);
        }
        return result;


    }

    /**
     * Get recent activities from the database.
     *
     * @param limit The number of activities to get. Types of activities include postcards sent, postcards received. (include donation?)
     * @return An array of JSON objects representing the activities.
     * @throws SQLException If there is an error in the SQL query.
     */
    public JSONArray getRecentActivities(int limit) throws SQLException {
        //Order postcard by timeSent AND timeReceived DESC, then limit the number of postcards to the limit parameter.
        //SELECT * FROM postcards ORDER BY timeSent DESC, timeReceived DESC LIMIT 5;
        Statement s = dbConn.createStatement();
        JSONArray result = new JSONArray();
        ResultSet rs = s.executeQuery("SELECT postcardId,postcardImage, userIDSent, u1.userName AS userNameSent, u1.userCountry AS userIDSent, userIDReceived, u2.userName AS userNameSent, u2.userCountry AS userCountryReceived FROM `postcards`\n" +
                "LEFT JOIN users u1\n" +
                "ON postcards.userIDSent = u1.userId\n" +
                "LEFT JOIN users u2\n" +
                "ON postcards.userIDReceived = u2.userId\n" +
                "ORDER BY timeSent DESC, timeReceived DESC\n" +
                "LIMIT " + limit);
        //postcardId	postcardImage
        //	                            userIDSent	userName	userCountry	userIDReceived	userName	userCountry
        while (rs.next()) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.set("postcardId", rs.getInt(1));
            jsonObject.set("postcardImage", rs.getString(2));
            jsonObject.set("userIDSent", rs.getInt(3));
            jsonObject.set("userNameSent", rs.getString(4));
            jsonObject.set("userCountrySent", rs.getString(5));
            jsonObject.set("userIDReceived", rs.getInt(6));
            jsonObject.set("userNameReceived", rs.getString(7));
            jsonObject.set("userCountryReceived", rs.getString(8));
            result.add(jsonObject);
        }
        return result;
    }

    public int getNumMembers() throws SQLException {
        Statement s = dbConn.createStatement();
        ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM users");
        if (rs.next()) {
            return rs.getInt(1);
        }
        return 0;
    }

    public int getNumPostcardReceived() throws SQLException {
        Statement s = dbConn.createStatement();
        ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM postcards WHERE timeReceived IS NOT NULL");
        if (rs.next()) {
            return rs.getInt(1);
        }
        return 0;
    }

    public int getNumPostcardTravelling() throws SQLException {
        Statement s = dbConn.createStatement();
        ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM postcards WHERE timeReceived IS NULL");
        if (rs.next()) {
            return rs.getInt(1);
        }
        return 0;
    }

    public int getNumPostcardReceived6Months() throws SQLException {
        Statement s = dbConn.createStatement();
        ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM postcards WHERE timeReceived IS NOT NULL AND timeReceived > DATE_SUB(NOW(), INTERVAL 6 MONTH)");
        if (rs.next()) {
            return rs.getInt(1);
        }
        return 0;
    }

    public int getNumDonatedLast6Months() throws SQLException {
        Statement s = dbConn.createStatement();
        ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM users WHERE lastDonated > DATE_SUB(NOW(), INTERVAL 6 MONTH)");
        if (rs.next()) {
            return rs.getInt(1);
        }
        return 0;
    }

    /**
     * Get the most recent postcards with image. The number of postcards is limited by the limit parameter.
     *
     * @param limit The number of postcards to get.
     * @return An array of postcards.
     * @throws SQLException If there is an error in the SQL query.
     */
    public Postcard[] getRecentPostcardsWithImage(int limit) throws SQLException {
        Statement s = dbConn.createStatement();
        ResultSet rs = s.executeQuery("SELECT * FROM postcards WHERE postcardImage IS NOT NULL ORDER BY timeSent DESC LIMIT " + limit);
        Postcard[] postcards = new Postcard[limit];
        int i = 0;
        while (rs.next()) {
            postcards[i++] = getPostcardFromResultSet(rs);
        }
        return postcards;
    }

    /**
     * Get the global gallery which are the most recent postcards with image. The number of postcards is limited by the limit parameter.
     *
     * @param limit The number of postcards to get.
     * @return An array of postcards.
     * @throws SQLException If there is an error in the SQL query.
     */
    public Postcard[] getGlobalGallery(int limit) throws SQLException{
        Statement s = dbConn.createStatement();
        ResultSet rs = s.executeQuery("SELECT * FROM postcards WHERE postcardImage IS NOT NULL ORDER BY timeSent DESC LIMIT " + limit);
        Postcard[] postcards = new Postcard[limit];
        int i = 0;
        while (rs.next()) {
            postcards[i++] = getPostcardFromResultSet(rs);
        }
        return postcards;
    }


    /**
     * Get a postcard from the result set. Application should call next() before calling this method. Only works if selecting all columns.
     *
     * @param rs The result set from the database.
     * @return The postcard object for onw row.
     */
    private Postcard getPostcardFromResultSet(ResultSet rs) throws SQLException {
        return new Postcard(rs.getInt("postcardID"),
                rs.getDate("timeSent").toString(),
                rs.getDate("timeReceived") == null ? null : rs.getDate("timeReceived").toString(),
                rs.getInt("userIDSent"),
                rs.getInt("userIDReceived"),
                rs.getString("postcardImage"),
                rs.getString("postcardMessage"));
    }

    public Postcard getPostcardById(int postcardId) throws SQLException {
        //<img src="http://img.postexchange.com/pc/[slash tag from database]"
        Statement s = dbConn.createStatement();
        ResultSet rs = s.executeQuery("SELECT * FROM postcards WHERE postcardID=" + postcardId);
        if (rs.next()) {
            return getPostcardFromResultSet(rs);
        }
        return null;
    }

    public int getNumberofUsers() throws SQLException {
        Statement s = dbConn.createStatement();
        ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM users");
        int count = 0;

        if (rs.next()) {
            count = rs.getInt(1);
        }

        return count;
    }

    public User getUserByEmailPassword(String username, String password) throws SQLException {
        PreparedStatement ps = dbConn.prepareStatement("SELECT * FROM users WHERE email=? AND password=?");
        ps.setString(1, username);
        ps.setString(2, password);
        ResultSet rs = ps.executeQuery();
        if (rs.next())
            return getUserFromResultSet(rs);
        return null;
    }

    public User getgetgetget(String username, String pass) throws SQLException
    {
        Statement s = dbConn.createStatement();
        String sql = "SELECT * FROM users WHERE email='"+username+"' AND password='"+pass+"'";
        System.out.println(sql);
        ResultSet rs = s.executeQuery(sql);
        if (rs.next())
            return getUserFromResultSet(rs);
        return null;
    }

    private User getUserFromResultSet(ResultSet rs) throws SQLException {
        User user = new User(); // Assuming there is a default constructor.
        user.setUserId(rs.getString("userId"));
        user.setEmail(rs.getString("email"));
        // password is transient, you usually don't set it from the database.
        user.setFirstName(rs.getString("firstName"));
        user.setLastName(rs.getString("lastName"));
        user.setLastLoginTime(rs.getString("lastLoginTime"));
        user.setNumberSent(rs.getInt("numSent"));
        user.setNumberReceived(rs.getInt("numReceived"));
        user.setUserCountry(rs.getString("userCountry"));
        user.setUserBio(rs.getString("userBio"));
        user.setProfilePicture(rs.getString("profilePicture"));
        user.setLastDonated(rs.getString("lastDonated"));
        return user;
    }

    /**
     * Try to register this user into database.
     *
     * @param user
     * @return Returns the user id registered. -1 if failed to generate a key
     * @throws SQLException
     */
    public int registerNewUserInDb(User user) throws SQLException {
        PreparedStatement ps = dbConn.prepareStatement("INSERT INTO `users` (`userName`, `email`, `password`, `firstName`, `lastName`, `userCountry`, `userBio`) VALUES(?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, user.getUserName());
        ps.setString(2, user.getEmail());
        ps.setString(3, user.getPassword());
        ps.setString(4, user.getFirstName());
        ps.setString(5, user.getLastName());
        ps.setString(6, user.getUserCountry());
        ps.setString(7, user.getUserBio());
        ps.executeUpdate();

        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            return rs.getInt(1);
        }
        return -1;
    }

    public void connect(String url, String dbUsername, String dbPassword, boolean useSSL) throws SQLException, ClassNotFoundException {
        //String connectionURL = "jdbc:mysql://" + host + "/" + dbName;
        this.dbConn = null;
        Class.forName("com.mysql.cj.jdbc.Driver");
        //URL for new version jdbc connector.
        Properties properties = new Properties(); //connection system property
        properties.setProperty("user", dbUsername);
        properties.setProperty("password", dbPassword);
        properties.setProperty("useSSL", Boolean.toString(useSSL));//set this true if domain suppotes SSL
        //"-u root -p mysql1 -useSSL false"
        this.dbConn = DriverManager.getConnection(url, properties);
    }

    @Override
    public void close() throws SQLException {
        dbConn.close();
    }

}
