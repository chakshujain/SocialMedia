package com.example.socialmedia.Models;

public class Friends {
    String date;
    String fullname;
    String profileimage;

    public Friends(String fullname, String profileimage) {
        this.fullname = fullname;
        this.profileimage = profileimage;
        this.date = date;
    }

    public Friends(){

    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getProfileimage() {
        return profileimage;
    }

    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
