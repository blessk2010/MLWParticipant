package com.blessk.mlwparticipant;
import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.support.v4.app.ActivityCompat;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class MainActivity extends AppCompatActivity implements ActivityCommunicator{
    String[] init_setting;
    String project_code;
    String server_ip;
    String auth_code;
    String last_updated;
    String selected_location;
    String [] appPermissions={
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.VIBRATE
    };
    private static final int PERMISSIONS_REQUEST_CODE = 1240;

    private DatabaseHelper dbHelper;
    TextView footer_tv;

    //progress
    ProgressBar progressBar;
    TextView progressText;
    private Handler handler = new Handler();
    private int progressStatus = 0;
    //progress
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setIcon(R.mipmap.logo);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        footer_tv=(TextView)findViewById(R.id.footer_value);
        // Check for app permissions
        // In case one or more permissions are not granted,
        // ActivityCompat.requestPermissions() will request permissions
        // and the control goes to onRequestPermissionsResult() callback method.
        if (checkAndRequestPermissions())
        {
            // All permissions are granted already. Proceed ahead
            initApp();
        }
        // The else case isn't required. The checkAndRequestPermissions() will control the flow

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId())
        {
            case R.id.action_about:
                showAbout();
                return true;
            case R.id.action_db_update:
                updateDatabase();
                return true;
            case R.id.action_settings:
                updateSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    public void showAbout()
    {
        startActivity(new Intent(MainActivity.this,About.class));

    }
    public void updateSettings()
    {
        startActivity(new Intent(MainActivity.this,ProjectManager.class));

    }
    private String getUpdates(final String request_type)
    {
        CheckNetworkStatus nc=new CheckNetworkStatus();
        final Context context=MainActivity.this;
        String result=null;
        if(nc.isNetworkAvailable(context))
        {
            dbHelper=new DatabaseHelper(context);

            try {
                MasterService fu = new MasterService(context);
                result=fu.getUpdate(request_type, server_ip, project_code, auth_code,last_updated);
            }
            catch (Exception e)
            {
                result="General processing error";
                e.printStackTrace();
            }
        }
        else
        {
            nc.getNetworkNotAvailableMessage(context);
        }
        return result;
    }
    private void restartMainActivity()
    {
        //return
        Context c=getBaseContext();
        Intent i = c.getPackageManager()
                .getLaunchIntentForPackage(c.getPackageName() );
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        c.startActivity(i);
    }
    public void updateDatabase()
    {
        new DatabaseUpdator().execute();

    }
    public boolean checkAndRequestPermissions()
    {
        // Check which permissions are granted
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String perm : appPermissions)
        {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED)
            {
                listPermissionsNeeded.add(perm);
            }
        }

        // Ask for non-granted permissions
        if (!listPermissionsNeeded.isEmpty())
        {
            ActivityCompat.requestPermissions(this,
                    listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),
                    PERMISSIONS_REQUEST_CODE
            );
            return false;
        }

        // App has all permissions. Proceed ahead
        return true;
    }
    private void initApp()
    {
        //progress
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressText = (TextView) findViewById(R.id.progressText);
        //progressBar.setVisibility(View.VISIBLE);
        //progress
        dbHelper=new DatabaseHelper(MainActivity.this);
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run()
            {

                init_setting=dbHelper.getProjectSetting();
                project_code=init_setting[0];
                server_ip=init_setting[1];
                auth_code=init_setting[2];
                last_updated=init_setting[3];
                if(last_updated!=null)
                {
                    footer_tv.setText("last updated :"+last_updated);
                }

                if(project_code==null)
                {
                    //start setting activity
                    startActivity(new Intent(MainActivity.this,ProjectManager.class));
                    return;
                }
            }
        });

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Home"));
        tabLayout.addTab(tabLayout.newTab().setText("Search by date"));
        tabLayout.addTab(tabLayout.newTab().setText("Search by pid"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        final PagerAdapter adapter = new PagerAdapter
                (getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab)
            {
                viewPager.setCurrentItem(tab.getPosition());
                if(tab.getPosition()==1)
                {
                    //pid fragment
                    TabSearchDateFragment fragment =(TabSearchDateFragment)adapter.getRegisteredFragment(tab.getPosition());
                    fragment.setSelectedLocation(selected_location);
                }
                else if(tab.getPosition()==2)
                {
                    //pid fragment
                    TabSearchPIDFragment fragment =(TabSearchPIDFragment)adapter.getRegisteredFragment(tab.getPosition());
                    fragment.setSelectedLocation(selected_location);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

    }
    @Override
    public void passDataToActivity(String str){
        this.selected_location=str;
        getSupportActionBar().setTitle(this.project_code+" - "+selected_location);
    }
    public void updateActivity() {

        //update locations
        final String results = getUpdates("update_sites");
        if (results.toLowerCase().contains("error")) {
            MainActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                alertDialog.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //dismiss
                    }
                });
                alertDialog.setCancelable(true);
                alertDialog.setTitle(getBaseContext().getString(R.string.app_name) + " Error");
                alertDialog.setMessage(results);
                alertDialog.create().show();
            }});
        } else {
            try {
                JSONObject jObject = new JSONObject(results);
                JSONArray locationArray = new JSONArray(jObject.getString("results"));
                dbHelper.deleteAllLocations();
                //progress
                progressBar.setMax(locationArray.length());
                int all = locationArray.length();
                for (int i = 0; i < locationArray.length(); i++) {
                    JSONObject location = new JSONObject(locationArray.get(i).toString());
                    String id = location.getString("id");
                    String name = location.getString("name");
                    if (dbHelper.createLocation(id, name)) {

                    } else {
                        Log.i("BlessK: ", "Unable to update location");
                        MainActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(MainActivity.this, "Unable to update location", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                    progressBar.setProgress(i + 1);
                    double sec = i % 20 == 0 ? i : ((0.25 * all) + i);
                    if (sec >= all)
                        sec = all;

                    progressBar.setSecondaryProgress((int) sec);
                    showProcessProgress("Updating sites...(" + (i + 1) + "/" + all + ")");
                }

                //Date date= Calendar.getInstance().getTime();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String date = sdf.format(new Date());
                dbHelper.updateSettingLastUpdate(date);
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(MainActivity.this, "Sites  updated successfully", Toast.LENGTH_LONG).show();
                    }
                });

            } catch (final Exception ex) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(MainActivity.this, ex.getMessage() + ">>" + results, Toast.LENGTH_LONG).show();
                    }
                });
                ex.printStackTrace();
            }

        /*update visits*************/
        final String resultsPids = getUpdates("update_pids");
        if (results.toLowerCase().contains("error")) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
            alertDialog.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    //dismiss
                }
            });
            alertDialog.setCancelable(true);
            alertDialog.setTitle(getBaseContext().getString(R.string.app_name) + " Error");
            alertDialog.setMessage(results);
            alertDialog.create().show();
            /*/Toast.makeText(MainActivity.this, resultsPids, Toast.LENGTH_LONG).show();
            MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(MainActivity.this, resultsPids, Toast.LENGTH_LONG).show();
                }});*/

        } else {
            try {
                JSONObject jObject = new JSONObject(resultsPids);
                JSONArray visitsArray = new JSONArray(jObject.getString("results"));
                dbHelper.deleteAllParticipants();
                //progress
                progressBar.setMax(visitsArray.length());
                int all = visitsArray.length();
                for (int i = 0; i < visitsArray.length(); i++) {
                    JSONObject v = new JSONObject(visitsArray.get(i).toString());
                    //properties
                    String pid = v.getString("pid");
                    String date = v.getString("date");
                    String visit = v.getString("visit");
                    String status = v.getString("status");
                    String location = v.getString("location");
                    if (dbHelper.createParticipantVisit(location, date, pid, visit, status)) {

                    } else {
                        Log.i("BlessK: ", "Unable to update visits");
                        MainActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(MainActivity.this, "Unable to update visits", Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                    progressBar.setProgress(i + 1);
                    double sec = i % 20 == 0 ? i : ((0.25 * all) + i);
                    if (sec >= all)
                        sec = all;

                    progressBar.setSecondaryProgress((int) sec);
                    showProcessProgress("Updating visits...(" + (i + 1) + "/" + all + ")");
                }

                //Date date= Calendar.getInstance().getTime();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String date = sdf.format(new Date());
                dbHelper.updateSettingLastUpdate(date);
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run()
                    {
                        Toast.makeText(MainActivity.this, "Visits successfully updated", Toast.LENGTH_LONG).show();
                    }
                });
                //finally restart the app for new update to take effect
                restartMainActivity();
                //Log.i("Visits thread", "Visits updated");
                //Toast.makeText(MainActivity.this, "Visits successfully updated ", Toast.LENGTH_LONG).show();
            } catch (final Exception ex) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(MainActivity.this, ex.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
                //Toast.makeText(MainActivity.this, ex.getMessage(), Toast.LENGTH_LONG).show();
                ex.printStackTrace();
            }
        }
    }

    }
    public void showProcessProgress(final String msg)
    {
        MainActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                progressText.setText(msg);
            }});
    }

    private class DatabaseUpdator extends AsyncTask<Object, Integer, Object> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // initialize the dialog
            progressText.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Object doInBackground(Object... params) {
            // do the hard work here
            // call publishProgress() to make any update in the UI
            updateActivity();
            return "";
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            // called from publishProgress(), you can update the UI here
            // for example, you can update the dialog progress
            // m_dialog.setProgress(values[0]); --> no apply here, because we made it indeterminate
        }

        @Override
        protected void onPostExecute(Object result) {
            super.onPostExecute(result);

            // close the dialog
            progressText.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            //restartMainActivity();
            //do any other UI related task
        }
    }
}