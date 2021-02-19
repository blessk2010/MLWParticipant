package com.blessk.mlwparticipant;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
public class MasterService
{
    Context context;
    /*private ProgressDialog progressDialog;*/
    private String server_name, request_type,url_updater,request_param;
    MasterService(Context context)
    {
        this.context=context;
        this.server_name=null;
        this.url_updater=null;
        this.request_param=null;
    }

    public String getUpdate(String request_type, String server_ip, String project_code, String auth_code, String last_updated)
    {
        try {
            this.request_type=request_type;
            this.server_name=server_ip;
            this.url_updater=server_name+"/mlw_pid_search/mlw_participant_visit.php";
            String request_parameter=last_updated;

            URL url=new URL(this.url_updater);
            HttpURLConnection conn=(HttpURLConnection)url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);

            OutputStream os=conn.getOutputStream();
            BufferedWriter br=new BufferedWriter(new OutputStreamWriter(os,"UTF-8"));
            String post_data= URLEncoder.encode("request_type","UTF-8")+"="+URLEncoder.encode(request_type,"UTF-8")+"&"+URLEncoder.encode("project_code","UTF-8")+"="+URLEncoder.encode(project_code,"UTF-8")+"&"+URLEncoder.encode("auth_code","UTF-8")+"="+URLEncoder.encode(auth_code,"UTF-8");
            if(request_parameter!=null)
                post_data+="&"+URLEncoder.encode("request_param","UTF-8")+"="+URLEncoder.encode(request_parameter,"UTF-8");

            Log.i("BlessK sending : ",post_data);
            br.write(post_data);
            br.flush();
            br.close();
            os.close();

            //reading result
            InputStream is=conn.getInputStream();
            BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(is,"iso-8859-1"));
            String result="";
            String line="";
            while((line=bufferedReader.readLine())!=null)
            {
                result+=line;
            }
            bufferedReader.close();
            is.close();
            conn.disconnect();

            if(this.request_type.equals("validate") && (result != null && !result.toLowerCase().startsWith("error")))
            {
                return "1";
            }
            else
            {
                return result;
            }
        }
        catch (Exception ex)
        {
            String msg=ex.getMessage();
            if(msg.toLowerCase().contains(this.url_updater))
            {
                msg=msg.replace(this.url_updater,"");
                msg+=" Updater service not found on the "+this.server_name+"\n" +
                        "Contact the administrator for help";
            }
            return "Error : "+msg;
        }
    }
}
