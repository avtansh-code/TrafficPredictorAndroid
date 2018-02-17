package com.example.avtansh.trafficpredictor;

import android.content.*;
import android.graphics.*;
import android.os.*;
import android.support.v7.app.AlertDialog;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.*;
import android.widget.*;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {


    List<trafficData> fullTrafficData = new ArrayList<>();
    List<trafficData> filteredTrafficData;
    Map<Integer, Double> groupedTrafficDataCong;
    Map<Integer, Double> groupedTrafficDataCurrSpeed;
    DecimalFormat decimalFormat = new DecimalFormat("##.00");
    String location;
    int normSpeed;
    Double threshold;

    Toolbar toolbar;
    DrawerLayout drawer;
    NavigationView navigationView;
    TextView name, email, hour, cong, cSpeed, locTag, locDetails;
    TabHost tabHost;
    TableLayout overallTable;
    TableRow  tr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        overallTable = (TableLayout) findViewById(R.id.overallTable);
        locTag = (TextView) findViewById(R.id.location);
        locDetails = (TextView) findViewById(R.id.locationDetails);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        Intent i = getIntent();
        Bundle b = i.getExtras();

        String uname = b.getString("name");
        String uemail = b.getString("email");

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header = navigationView.getHeaderView(0);

        name = (TextView)header.findViewById(R.id.username);
        email = (TextView)header.findViewById(R.id.email);

        name.setText(uname);
        email.setText(uemail);

        tabHost = (TabHost)findViewById(R.id.navTabs);
        tabHost.setup();

        TabHost.TabSpec spec = tabHost.newTabSpec("Tab One");
        spec.setContent(R.id.tab1);
        spec.setIndicator("Tab One");
        tabHost.addTab(spec);

        spec = tabHost.newTabSpec("Tab Two");
        spec.setContent(R.id.tab1);
        spec.setIndicator("Tab Two");
        tabHost.addTab(spec);


        try{
            BufferedReader br = new BufferedReader(new FileReader(new File(Environment.getExternalStorageDirectory(),"output.csv")));
            String line = br.readLine();
            while ((line = br.readLine()) != null) {

                String[] entries = line.split(",");

                trafficData owner = new trafficData(entries[1], Integer.parseInt(entries[2]), Integer.parseInt(entries[3]), entries[4], Integer.parseInt(entries[5]), Double.parseDouble(entries[6]));

                fullTrafficData.add(owner);
            }
        }
        catch (IOException ex){
            ex.printStackTrace();
        }

        changeLocation("Hauz Khas");

    }

    @Override
    public void onBackPressed(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setMessage("Exit App");
        builder.setPositiveButton("Yes",new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                exitAll();
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
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.Exit) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false);
            builder.setMessage("Exit");
            builder.setPositiveButton("Yes",new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    exitAll();
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

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.hauz_khas) {
            changeLocation("Hauz Khas");
        } else if (id == R.id.model_town) {
            changeLocation("Model Town");
        } else if (id == R.id.civil_lines) {
            changeLocation("Civil Lines");
        } else if (id == R.id.punjabi_bagh) {
            changeLocation("Punjabi Bagh");
        } else if (id == R.id.najafgarh) {
            changeLocation("Najafgarh");
        } else if (id == R.id.sarawati_vihar) {
            changeLocation("Saraswati Vihar");
        } else if (id == R.id.mukarba_chowk){
            changeLocation("Mukarba Chowk");
        } else if (id == R.id.seelampur){
            changeLocation("Seelampur");
        } else if (id == R.id.gurugram){
            changeLocation("Gurugram");
        } else if (id == R.id.noida){
            changeLocation("Noida");
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void changeLocation(String l){
        location = l;
        locTag.setText(location);
        filterData();
        normSpeed = filteredTrafficData.get(0).normSpeed;
        threshold = 0.0;
        groupData();
        locDetails.setText("Congestion Threshold: "+threshold+"%\nFree Flow Speed: "+normSpeed+" kmph");
        createOverallTable();
    }

    private void filterData(){
        filteredTrafficData = new ArrayList<>();
        for(trafficData data : fullTrafficData){
            if(data.location.equals(location)){
                filteredTrafficData.add(data);
            }
        }
    }

    private void groupData(){
        Map<Integer, List<trafficData>> groupedTrafficData = new HashMap<>();
        groupedTrafficDataCurrSpeed = new HashMap<>();
        groupedTrafficDataCong = new HashMap<>();
        double congTotal, congAvg, currSpeedTotal, currSpeedAvg;
        int count[] = new int[24];
        for(int i = 0; i < 24; i++){
            groupedTrafficData.put(i, new ArrayList<>());
            count[i] = 0;
        }

        for(trafficData data: filteredTrafficData){
            groupedTrafficData.get(data.hour).add(data);
            count[data.hour]++;
        }

        for(int i = 0; i < 24; i++){
            List<trafficData> temp = groupedTrafficData.get(i);
            congTotal = 0;
            currSpeedTotal = 0;
            for(trafficData data: temp){
                congTotal = congTotal +  data.cong;
                currSpeedTotal = currSpeedTotal + data.currSpeed;
            }
            congAvg = Double.parseDouble(decimalFormat.format(congTotal/count[i]));
            currSpeedAvg = Double.parseDouble(decimalFormat.format(currSpeedTotal/count[i]));
            if(currSpeedAvg > normSpeed){
                if(congAvg == 0.0){
                    currSpeedAvg = normSpeed;
                }
                else{
                    currSpeedAvg -= 1;
                }
            }
            groupedTrafficDataCong.put(i, congAvg);
            groupedTrafficDataCurrSpeed.put(i, currSpeedAvg);
        }
    }

    private void createOverallTable(){
        overallTable.removeAllViews();
        TableRow.LayoutParams llp;
        LinearLayout hcell, ccell, cpcell;

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels;

        tr = new TableRow(this);
        tr.setBackgroundColor(Color.DKGRAY);
        tr.setPadding(0,2,0,2);
        tr.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        ));

        llp =  new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);

        llp.setMargins(0, 0, 2, 0 );


        hcell = new LinearLayout(this);
        hcell.setBackgroundColor(Color.WHITE);
        hcell.setLayoutParams(llp);
        hour = new TextView(this);
        hour.setText("Hour");
        hour.setTextSize(20);
        hour.setWidth(20*width/100);
        hour.setHeight(100);
        hour.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        hour.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        hour.setPadding(15, 5, 15, 5);
        hcell.addView(hour);
        tr.addView(hcell);

        ccell = new LinearLayout(this);
        ccell.setBackgroundColor(Color.WHITE);
        ccell.setLayoutParams(llp);
        cong = new TextView(this);
        cong.setText("Congestion");
        cong.setTextSize(20);
        cong.setWidth(38*width/100);
        cong.setHeight(100);
        cong.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        cong.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        cong.setPadding(15, 5, 15, 5);
        ccell.addView(cong);
        tr.addView(ccell);

        cpcell = new LinearLayout(this);
        cpcell.setBackgroundColor(Color.WHITE);
        cpcell.setLayoutParams(llp);
        cSpeed = new TextView(this);
        cSpeed.setText("Average Speed");
        cSpeed.setTextSize(20);
        cSpeed.setWidth(42*width/100);
        cSpeed.setHeight(100);
        cSpeed.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        cSpeed.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        cSpeed.setPadding(15, 5, 15, 5);
        cpcell.addView(cSpeed);
        tr.addView(cpcell);

        overallTable.addView(tr, new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT));


        for(int i = 0; i < 24; i++){
            Double cp = groupedTrafficDataCong.get(i);
            Double speed = groupedTrafficDataCurrSpeed.get(i);

            tr = new TableRow(this);
            tr.setBackgroundColor(Color.DKGRAY);
            tr.setPadding(0,0,0,2);
            tr.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT
            ));

            llp =  new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);

            llp.setMargins(0, 0, 2, 0 );


            hcell = new LinearLayout(this);
            hcell.setBackgroundColor(Color.WHITE);
            hcell.setLayoutParams(llp);
            hour = new TextView(this);
            String c, n;
            if(i <= 9){
                c = '0'+i+":00";
            }
            else{
                c = i+":00";
            }
            if(i+1 <= 9){
                n = '0'+i+":00";
            }
            else{
                n = i+":00";
            }
            hour.setText(c + " - " + n );
            hour.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            hour.setPadding(15, 5, 15, 5);
            hour.setHeight(50);
            hcell.addView(hour);
            tr.addView(hcell);

            ccell = new LinearLayout(this);
            ccell.setBackgroundColor(Color.WHITE);
            ccell.setLayoutParams(llp);
            cong = new TextView(this);
            cong.setText(cp+"%");
            cong.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            cong.setPadding(15, 5, 15, 5);
            cong.setHeight(50);
            ccell.addView(cong);
            tr.addView(ccell);

            cpcell = new LinearLayout(this);
            cpcell.setBackgroundColor(Color.WHITE);
            cpcell.setLayoutParams(llp);
            cSpeed = new TextView(this);
            cSpeed.setText(speed+" kmph");
            cSpeed.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            cSpeed.setPadding(15, 5, 15, 5);
            cSpeed.setHeight(50);
            cpcell.addView(cSpeed);
            tr.addView(cpcell);

            overallTable.addView(tr, new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT));
        }

    }

    private void exitAll(){
        Intent intent = new Intent(this, LoginPage.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("Exit me", true);
        startActivity(intent);
        finish();
    }
}
