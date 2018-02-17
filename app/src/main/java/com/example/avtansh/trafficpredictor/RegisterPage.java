package com.example.avtansh.trafficpredictor;

import java.security.*;

import android.content.Intent;
import android.widget.*;
import android.os.Bundle;
import android.view.View;
import android.graphics.Color;

import com.google.firebase.database.*;
import android.support.v7.app.AppCompatActivity;

public class RegisterPage extends AppCompatActivity{

    EditText username, password, confirmPass, email, name, dob, phone;
    private DatabaseReference database = FirebaseDatabase.getInstance().getReference();

    Button register;
    TextView prompt;

    private static final String md5(final String password) {
        try {

            MessageDigest digest = java.security.MessageDigest
                    .getInstance("MD5");
            digest.update(password.getBytes());
            byte messageDigest[] = digest.digest();

            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++) {
                String h = Integer.toHexString(0xFF & messageDigest[i]);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_page);

        username = (EditText)findViewById(R.id.username);
        password = (EditText)findViewById(R.id.password);
        confirmPass = (EditText)findViewById(R.id.cnfmpassword);
        name = (EditText)findViewById(R.id.name);
        phone = (EditText)findViewById(R.id.phoneNo);
        dob = (EditText)findViewById(R.id.dateOfBirth);
        email = (EditText)findViewById(R.id.email);
        register = (Button)findViewById(R.id.registerUser);
        prompt = (TextView)findViewById(R.id.prompt);
        prompt.setVisibility(View.GONE);

        setDate d = new setDate(dob, this);

    }

    public void register(View v){
        User u;
        String user = username.getText().toString();
        String pass = password.getText().toString();
        String cpass = confirmPass.getText().toString();
        String mail = email.getText().toString();
        String ph = phone.getText().toString();
        String uname = name.getText().toString();
        String udob = dob.getText().toString();
        EmailValidator ev = new EmailValidator();
        PasswordValidator pv = new PasswordValidator(password, this);

        if(user.trim().equals("")){
            username.setError("Enter Username");
        }
        else if(pass.trim().equals("")){
            password.setError("Enter Password");
        }
        else if(!pv.validate(user,pass)){
            return;
        }
        else if(cpass.trim().equals("")){
            confirmPass.setError("Enter Confirm Password");
        }
        else if(mail.trim().equals("")){
            email.setError("Enter Email");
        }
        else if(!ev.validate(mail)){
            email.setError("Enter Valid Email");
        }
        else if(ph.trim().equals("")){
            phone.setError("Enter Phone Number");
        }
        else if(ph.length() != 10){
            phone.setError("Enter Valid Phone Number");
        }
        else if(uname.trim().equals("")){
            name.setError("Enter Name");
        }
        else if(udob.trim().equals("")){
            dob.setError("Enter Date of Birth");
        }
        else{
            prompt.setVisibility(View.GONE);
            register.setVisibility(View.GONE);
            if(pass.equalsIgnoreCase(cpass)){
                String md5Pass = md5(pass);
                u = new User(user,md5Pass, uname, mail, udob, ph, false);
                database.child("users").child(user).setValue(u);
                Intent intent = new Intent(this, LoginPage.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
            else {
                prompt.setText("Passwords do not match");
                prompt.setVisibility(View.VISIBLE);
                register.setVisibility(View.VISIBLE);
                prompt.setTextColor(Color.RED);
            }
        }
    }
}
