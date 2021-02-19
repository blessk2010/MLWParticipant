package com.blessk.mlwparticipant;

/**
 * Created by BlessK on 1/22/2018.
 */
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 *
 * Checks whether the device is connected to internet or not
 */

public class CheckNetworkStatus {

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    public void getNetworkNotAvailableMessage(Context context)
    {
        AlertDialog.Builder alertDialog= new AlertDialog.Builder(context);
        alertDialog.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        //dismiss the dialog
                    }
                });
        alertDialog.setCancelable(true);
        alertDialog.setTitle("Network error");
        alertDialog.setMessage("You are not connected to any network");
        alertDialog.create().show();
    }
}