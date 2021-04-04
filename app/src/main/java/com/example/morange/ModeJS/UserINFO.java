package com.example.morange.ModeJS;

public class UserINFO {

    private String id;
    private String login;
    private String ImageURL;
    private String status;
    private String username;
    private String email;
    private long lastseen;

    public UserINFO(String id,String login, String imageURL, String status,String username,String email, long lastseen)
    {
        this.id=id;
        this.login = login;
        this.ImageURL = imageURL;
        this.status = status;
        this.username = username;
        this.email = email;
        this.lastseen = lastseen;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public long getLastseen() {
        return lastseen;
    }

    public void setLastseen(long lastseen) {
        this.lastseen = lastseen;
    }

    public UserINFO() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getImageURL() {
        return ImageURL;
    }

    public void setImageURL(String imageURL) {
        this.ImageURL = imageURL;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
