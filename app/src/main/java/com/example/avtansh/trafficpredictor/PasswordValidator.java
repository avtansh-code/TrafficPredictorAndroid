package com.example.avtansh.trafficpredictor;

import android.content.Context;
import android.widget.EditText;

/**
 * Created by Avtansh Gupta on 16-12-2017.
 */

public class PasswordValidator
{

    EditText pwd;
    Context ctx;
    public PasswordValidator(EditText pwd, Context ctx)
    {
        this.pwd = pwd;
        this.ctx = ctx;
    }

    public boolean validate(String userName, String password)
    {
        String upperCaseChars = "(.*[A-Z].*)";
        String lowerCaseChars = "(.*[a-z].*)";
        String numbers = "(.*[0-9].*)";

        if (password.length() > 15 || password.length() < 8)
        {
            pwd.setError("Password should be less than 15 and more than 8 characters in length.");
        }
        else if (password.indexOf(userName) > -1)
        {
            pwd.setError("Password Should not be same as user name");
        }
        else if (!password.matches(upperCaseChars ))
        {
            pwd.setError("Password should contain atleast one upper case alphabet");
        }
        else if (!password.matches(lowerCaseChars ))
        {
            pwd.setError("Password should contain atleast one lower case alphabet");
        }
        else if (!password.matches(numbers ))
        {
            pwd.setError("Password should contain atleast one number.");
        }
        else{
            return true;
        }
        return false;
    }
}
