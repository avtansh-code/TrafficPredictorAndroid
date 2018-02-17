package com.example.avtansh.trafficpredictor;

import java.io.File;
import java.security.*;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.*;
import android.os.Bundle;
import android.view.View;
import android.graphics.Color;
import android.Manifest;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.*;
import com.google.firebase.storage.*;

import android.support.v7.app.AppCompatActivity;


public class LoginPage extends AppCompatActivity {

    EditText username, password;
    private DatabaseReference database = FirebaseDatabase.getInstance().getReference();
    Button login;
    TextView prompt;
    ProgressBar bar;
    int counter = 3;
    String key;
    String user, pass;
    Bundle bundle;
    private static final int PERMISSION_REQUEST_CODE = 1;
    String wantPermission = Manifest.permission.WRITE_EXTERNAL_STORAGE;

    private static final String md5(final String password) {
        try {

            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
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

        if( getIntent().getBooleanExtra("Exit me", false)){
            finish();
        }


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        username = (EditText)findViewById(R.id.username);
        password = (EditText)findViewById(R.id.password);
        login = (Button)findViewById(R.id.loginUser);
        prompt = (TextView)findViewById(R.id.prompt);
        bar = (ProgressBar)findViewById(R.id.loginProgress);

        prompt.setVisibility(View.GONE);
        bar.setVisibility(View.GONE);

        key = "output.csv";

        int permission = ActivityCompat.checkSelfPermission(LoginPage.this,  wantPermission);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    LoginPage.this,
                    new String[]{wantPermission},
                    PERMISSION_REQUEST_CODE
            );
        }
    }


    @Override
    public void onBackPressed(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setMessage("Exit App");
        builder.setPositiveButton("Yes",new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
                System.exit(0);
            }
        });
        builder.setNegativeButton("No",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alert=builder.create();
        alert.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.register, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.registerUser) {
            Intent intent = new Intent(this, RegisterPage.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }



    public void login(View v){
        user = username.getText().toString();
        pass = password.getText().toString();

        if(user.trim().equals("")){
            username.setError("Enter Username");
        }
        else if(pass.trim().equals("")){
            password.setError("Enter Password");
        }
        else{
            bar.setVisibility(View.VISIBLE);
            bar.setIndeterminate(true);
            prompt.setVisibility(View.VISIBLE);
            prompt.setText("Logging In...");
            login.setVisibility(View.GONE);
            prompt.setTextColor(Color.GREEN);
            database.child("users").child(user).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot){
                    if(dataSnapshot.getValue() != null){
                        String un = dataSnapshot.child("name").getValue().toString();
                        String p = dataSnapshot.child("password").getValue().toString();
                        String e = dataSnapshot.child("email").getValue().toString();
                        Boolean v = (Boolean) dataSnapshot.child("verified").getValue();
                        String md5Pass = md5(pass);
                        if(p.equals(md5Pass)){
                            if(v == true){
                                File file = new File(Environment.getExternalStorageDirectory().toString() + "/traffic/" + key);
                                bundle = new Bundle();
                                bundle.putString("name",un);
                                bundle.putString("email",e);
                                prompt.setTextColor(Color.GREEN);
                                prompt.setText("Retreiving Data...");
                                downloadData();
//                                changeIntent();
                            }
                            else {
                                prompt.setText("User Not Verified Yet");
                                bar.setVisibility(View.GONE);
                                login.setVisibility(View.VISIBLE);
                                prompt.setTextColor(Color.RED);
                            }
                        }
                        else {
                            Toast.makeText(getApplicationContext(),"Wrong Credentials...", Toast.LENGTH_SHORT).show();
                            counter--;
                            prompt.setText("Incorrect Username or Password\nNo of attempts left:"+counter+"!!!");
                            bar.setVisibility(View.GONE);
                            login.setVisibility(View.VISIBLE);
                            prompt.setTextColor(Color.RED);
                            if(counter == 0){
                                login.setEnabled(false);
                                prompt.setText("Maximum Login Tries Limit Exceeded!!!");
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    System.out.println("The read failed: " + databaseError.getCode());
                }
            });
        }
    }

    public void downloadData(){
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl("gs://traffic-predictor-233145.appspot.com/");
        StorageReference  islandRef = storageRef.child("output.csv");

        final File localFile = new File(Environment.getExternalStorageDirectory(),"output.csv");

        islandRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Log.e("firebase ",";local tem file created  created " +localFile.toString());
                changeIntent();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.e("firebase ",";local tem file not created  created " +exception.toString());
            }
        });
    }


    public void changeIntent(){
        Intent intent = new Intent(LoginPage.this, MainActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }
}
