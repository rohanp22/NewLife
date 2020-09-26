package com.example.newlife;

public class User {

    private String Bid;
    private String Macid;

    public User(String bid, String macid) {
        Bid = bid;
        Macid = macid;
    }

    public String getBid() {
        return Bid;
    }

    public String getMacid() {
        return Macid;
    }
}
