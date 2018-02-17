package com.example.avtansh.trafficpredictor;

import java.util.Date;

/**
 * Created by Avtansh Gupta on 14-12-2017.
 */

public class User {
    public String username;
    public String password;
    public String email;
    public String name;
    public String dob;
    public String phone;
    public Boolean verified;

    public User(String u, String p, String n, String e, String d, String ph, Boolean v){
        this.username = u;
        this.password = p;
        this.email = e;
        this.verified = v;
        this.phone = ph;
        this.dob = d;
        this.name = n;
    }
}
