/*
 * Author: jianqing
 * Date: Mar 26, 2024
 * Description: This document is created for
 */
package com.postexchange.network;

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

    public Postcard getPostcardById(int postcardId) throws SQLException
    {
        //<img src="http://img.postexchange.com/pc/[slash tag from database]"
        Statement s = dbConn.createStatement();
        ResultSet rs = s.executeQuery("SELECT * FROM postcards WHERE postcardID=" + postcardId);
        if (rs.next())
        {
            Postcard postcard = new Postcard(rs.getInt("postcardID"),
                    rs.getDate("timeSent").toString(),
                    rs.getDate("timeReceived").toString(),
                    rs.getInt("userIDSent"),
                    rs.getInt("userIDReceived"),
                    rs.getString("postcardImage"),
                    rs.getString("postcardMessage")
            );
            return postcard;
        }
        return null;
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
            System.out.println(sql.getUserByUsernamePassword("hayubdhbs@gmail.com", "hahshsh"));
            System.out.println("Yay!");
        } catch (SQLException | ClassNotFoundException ex)
        {
            Logger.getLogger(SQLAccessor.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }

}
