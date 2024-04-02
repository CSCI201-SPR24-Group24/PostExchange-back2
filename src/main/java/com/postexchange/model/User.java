/*
 * Author: jianqing
 * Date: Mar 26, 2024
 * Description: This document is created for
 */
package com.postexchange.model;

/**
 *
 * @author jianqing
 */
public class User
{
    private String userId;
    private String emailId;
    private String password;
    private String firstName;
    private String lastName;
    private String lastLoginTime;
    private int numberSent;
    private int numberReceived; 
    private String userCountry;
    private String userBio;
    private String profilePicture;

    public String getUserId() {
        return userId;
    }

    public String getEmailId() {
        return emailId;
    }

    public String getPassword() {
        return password;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getLastLoginTime() {
        return lastLoginTime;
    }

    public int getNumberSent() {
        return numberSent;
    }

    public int getNumberReceived() {
        return numberReceived;
    }

    public String getUserCountry() {
        return userCountry;
    }

    public String getUserBio() {
        return userBio;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    // Setters
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setLastLoginTime(String lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public void setNumberSent(int numberSent) {
        this.numberSent = numberSent;
    }

    public void setNumberReceived(int numberReceived) {
        this.numberReceived = numberReceived;
    }

    public void setUserCountry(String userCountry) {
        this.userCountry = userCountry;
    }

    public void setUserBio(String userBio) {
        this.userBio = userBio;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }


    
}
