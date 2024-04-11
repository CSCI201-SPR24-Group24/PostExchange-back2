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
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/** 
 *
 * @author jianqing
 */
public class SQLAccessor implements AutoCloseable
{

    private java.sql.Connection dbConn;




    /**
     * Get recent activities from the database.
     * @param limit The number of activities to get. Types of activities include postcards sent, postcards received. (include donation?)
     * @return An array of JSON objects representing the activities.
     * @throws SQLException If there is an error in the SQL query.
     */
    public JSONArray getRecentActivities(int limit) throws SQLException
    {
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
                "LIMIT "+limit);
        //postcardId	postcardImage
        //	                            userIDSent	userName	userCountry	userIDReceived	userName	userCountry
        while (rs.next())
        {
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

    public int getNumMembers() throws SQLException
    {
        Statement s = dbConn.createStatement();
        ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM users");
        if (rs.next())
        {
            return rs.getInt(1);
        }
        return 0;
    }

    public int getNumPostcardReceived() throws SQLException
    {
        Statement s = dbConn.createStatement();
        ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM postcards WHERE timeReceived IS NOT NULL");
        if (rs.next())
        {
            return rs.getInt(1);
        }
        return 0;
    }

    public int getNumPostcardTravelling() throws SQLException
    {
        Statement s = dbConn.createStatement();
        ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM postcards WHERE timeReceived IS NULL");
        if (rs.next())
        {
            return rs.getInt(1);
        }
        return 0;
    }

    public int getNumPostcardReceived6Months() throws SQLException
    {
        Statement s = dbConn.createStatement();
        ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM postcards WHERE timeReceived IS NOT NULL AND timeReceived > DATE_SUB(NOW(), INTERVAL 6 MONTH)");
        if (rs.next())
        {
            return rs.getInt(1);
        }
        return 0;
    }

    public int getNumDonatedLast6Months() throws SQLException
    {
        Statement s = dbConn.createStatement();
        ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM users WHERE lastDonated > DATE_SUB(NOW(), INTERVAL 6 MONTH)");
        if (rs.next())
        {
            return rs.getInt(1);
        }
        return 0;
    }



    /**
     * Get the most recent postcards with image. The number of postcards is limited by the limit parameter.
     * @param limit The number of postcards to get.
     * @return An array of postcards.
     * @throws SQLException  If there is an error in the SQL query.
     */
    public Postcard[] getRecentPostcardsWithImage(int limit) throws SQLException
    {
        Statement s = dbConn.createStatement();
        ResultSet rs = s.executeQuery("SELECT * FROM postcards WHERE postcardImage IS NOT NULL ORDER BY timeSent DESC LIMIT " + limit);
        Postcard[] postcards = new Postcard[limit];
        int i = 0;
        while (rs.next())
        {
            postcards[i++] = getPostcardFromResultSet(rs);
        }
        return postcards;
    }

    /**
     * Get a postcard from the result set. Application should call next() before calling this method. Only works if selecting all columns.
     * @param rs The result set from the database.
     * @return The postcard object for onw row.
     */
    private Postcard getPostcardFromResultSet(ResultSet rs) throws SQLException
    {
        return new Postcard(rs.getInt("postcardID"),
                rs.getDate("timeSent").toString(),
                rs.getDate("timeReceived").toString(),
                rs.getInt("userIDSent"),
                rs.getInt("userIDReceived"),
                rs.getString("postcardImage"),
                rs.getString("postcardMessage"));
    }

    public Postcard getPostcardById(int postcardId) throws SQLException
    {
        //<img src="http://img.postexchange.com/pc/[slash tag from database]"
        Statement s = dbConn.createStatement();
        ResultSet rs = s.executeQuery("SELECT * FROM postcards WHERE postcardID=" + postcardId);
        if (rs.next())
        {
            return getPostcardFromResultSet(rs);
        }
        return null;
    }

    public int getNumberofUsers() throws SQLException
    {
        Statement s = dbConn.createStatement();
        ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM users");
        int count = 0;

        if(rs.next()){
            count = rs.getInt(1);
        }

        return count;
    }

  

    public User getUserByUsernamePassword(String username, String password) throws SQLException
    {
        PreparedStatement ps = dbConn.prepareStatement("SELECT * FROM users WHERE email=? AND password=?");
        ps.setString(1, username);
        ps.setString(2, password);
        ResultSet rs = ps.executeQuery();
        if (rs.next())
        {
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
        return null; // or throw an exception if user not found
    }
    
    /**
     * Try to register this user into database.
     * @param user
     * @return Returns the user id registered. -1 if failed to generate a key
     * @throws SQLException 
     */
    public int registerNewUserInDb(User user) throws SQLException
    {
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
        if(rs.next())
        {
            return rs.getInt(1);
        }
        return -1;
    }
    

    public void connect(String url, String dbUsername, String dbPassword, boolean useSSL) throws SQLException, ClassNotFoundException
    {
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

    public static SQLAccessor getDefaultInstance() throws SQLException, ClassNotFoundException
    {
        SQLAccessor d = new SQLAccessor();
        Setting s = new Setting("db.setting");
        d.connect(s.get("url"), s.get("user"), s.get("pass"), true);
        return d;
    }

    @Override
    public void close() throws SQLException
    {
        dbConn.close();
    }

    public static void main(String[] args)
    {
        //try-with-resources
        try(SQLAccessor sql = getDefaultInstance())
        {

            User user = new User();
            user.setUserName("baba");
            user.setEmail("m@g.c");
            user.setPassword("e10");
            user.setFirstName("John2");
            user.setLastName("Doe2");
            user.setUserCountry("US");
            user.setUserBio("Hahahahaha I am not crazy!");
            System.out.println(sql.registerNewUserInDb(user));

            System.out.println(sql.getUserByUsernamePassword("johndoe@example.com", "482c811da5d5b4bc6d497ffa98491e38"));
            System.out.println(sql.getPostcardById(1));
            System.out.println(sql.getNumberofUsers());

            System.out.println("Yay!");
        } catch (SQLException  ex)
        {
            if(ex.getErrorCode()==1062)
                System.out.println("Duplicate email!");
            else
                Logger.getLogger(SQLAccessor.class.getName()).log(Level.SEVERE, null, ex);
            
        } catch(ClassNotFoundException nx)
        {
            Logger.getLogger(SQLAccessor.class.getName()).log(Level.SEVERE, null, nx);
        }
    }

}
