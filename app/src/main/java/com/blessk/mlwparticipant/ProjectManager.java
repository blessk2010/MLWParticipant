package com.blessk.mlwparticipant;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


public class ProjectManager extends AppCompatActivity {

    Button btnProjectAddEdit;
    DatabaseHelper dbHelper;
    String[] init_seting;
    String project_code;
    String server_ip;
    String auth_code;

    EditText project_et;
    EditText server_et;
    EditText auth_code_et;

    String new_proj_code;
    String new_server_ip;
    String new_auth_code;

    //progress
    ProgressBar progressBar;
    TextView progressText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_manager);

        //getSupportActionBar().setDisplayShowHomeEnabled(true);
        //getSupportActionBar().setIcon(R.mipmap.logo);
        //progress
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressText = (TextView) findViewById(R.id.progressText);
        progressBar.setIndeterminate(true);
        //progress
        project_et=(EditText)findViewById(R.id.project_code);
        project_et.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
        server_et=(EditText)findViewById(R.id.server_ip);
        auth_code_et=(EditText)findViewById(R.id.auth_code);

        btnProjectAddEdit = (Button)findViewById(R.id.button);


        btnProjectAddEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                //add edit
                new_proj_code=project_et.getText().toString().trim();
                new_server_ip=server_et.getText().toString().trim();
                new_auth_code=auth_code_et.getText().toString().trim();

                if(new_proj_code.length()>0 && new_server_ip.length()>0 && new_auth_code.length()>0)
                {
                    String results=validateData();
                    if(results!=null && results.equals("1"))
                    {
                        saveSettings(new_proj_code,new_server_ip,new_auth_code,project_code);
                    }
                    else if(results!=null)
                    {
                        AlertDialog.Builder alertDialog= new AlertDialog.Builder(ProjectManager.this);
                        alertDialog.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which)
                            {
                                //dismiss
                            } });
                        alertDialog.setCancelable(true);
                        alertDialog.setTitle(getBaseContext().getString(R.string.app_name)+" Error");
                        alertDialog.setMessage(results);
                        alertDialog.create().show();

                    }
                    else
                    {
                        //general error
                    }
                }
                else
                {
                    Toast.makeText(ProjectManager.this, "Fill in the form", Toast.LENGTH_SHORT).show();

                }

            }
        });
        //initialising withdb data

        ProjectManager.this.runOnUiThread(new Runnable() {
            @Override
            public void run()
            {
                dbHelper=new DatabaseHelper(getBaseContext());
                init_seting=dbHelper.getProjectSetting();
                project_code=init_seting[0];
                server_ip=init_seting[1];
                auth_code=init_seting[2];

                if(server_ip!=null)
                    server_et.setText(server_ip);
                if(project_code!=null)
                    project_et.setText(project_code);
                if(auth_code!=null)
                    auth_code_et.setText(auth_code);
            }
        });
    }
    private String validateData()
    {
        CheckNetworkStatus nc=new CheckNetworkStatus();
        final Context context=ProjectManager.this;
        String result=null;
        if(nc.isNetworkAvailable(context))
        {
            try
            {
                /*MasterService fu = new MasterService(context);
                String request_type = "validate";
                result=fu.execute(request_type, server_ip, project_code, authentication_code,null).get();*/
                result=new DatabaseUpdator2().execute().get().toString();
            }
            catch (Exception e)
            {
                result="General processing error";
            }
        }
        else
        {
            nc.getNetworkNotAvailableMessage(ProjectManager.this);
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
    private void saveSettings(String new_proj_code,String new_server_ip,String new_auth_code,String project_code)
    {
        if(project_code!=null)
        {
            //update
            if(dbHelper.updateSetting(new_proj_code,new_server_ip,new_auth_code,project_code))
            {
                //success
                Toast.makeText(ProjectManager.this, "Settings saved successfully", Toast.LENGTH_SHORT).show();
                restartMainActivity();
            }
            else
            {
                //failure
                Toast.makeText(ProjectManager.this, "Unable to save settings", Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            //add data
            if(dbHelper.createSetting(new_proj_code,new_server_ip,new_auth_code))
            {
                //success
                Toast.makeText(ProjectManager.this, "Settings updated successfully", Toast.LENGTH_SHORT).show();
                restartMainActivity();
            }
            else
            {
                //failure
                Toast.makeText(ProjectManager.this, "Unable to update settings", Toast.LENGTH_SHORT).show();
            }

        }
    }
    private String getUpdates(final String request_type)
    {
        CheckNetworkStatus nc=new CheckNetworkStatus();
        final Context context=getBaseContext();
        String result=null;
        if(nc.isNetworkAvailable(context))
        {
            dbHelper=new DatabaseHelper(context);

            try {
                MasterService fu = new MasterService(context);
                result=fu.getUpdate(request_type, new_server_ip, new_proj_code, new_auth_code,null);
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
    private class DatabaseUpdator2 extends AsyncTask<Object, Integer, Object> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // initialize the dialog
            //show progress bar
            progressText.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Object... params) {
            // do the hard work here
            // call publishProgress() to make any update in the UI
            return getUpdates("validate");
            //return "";
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
            //show progress bar
            progressText.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            //do any other UI related task
        }
    }
}
