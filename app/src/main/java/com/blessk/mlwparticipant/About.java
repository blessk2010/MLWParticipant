package com.blessk.mlwparticipant;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import java.util.Calendar;

import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

public class About extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_about);

        final  String about="Welcome to "+getString(R.string.app_name)+"\n This application is designed to help you look up participant visits. \n" +
                "**********\n" +
                "Steps\n" +
                "1. Configure lookup updater service on the server.\n" +
                "2. Configure the app settings in the Settings window to enable this app to communicate with the server.\n" +
                "3. Click 'update database' on the menu to update the device.\n" +
                "**********\n" +
                "Author: Blesswell Kapalamula.(blesswellkapalamula@gmail.com).\n " +
                "Direct your suggestions and comments to:" +
                "\nThe MLW Senior Data Systems Manager:" +
                "\nName: Clemens Masesa." +
                " \nE-mail: cpmasesa@mlw.mw\n" +
                "\n " +
                "Credit to : MLW DATA TEAM.\n" +
                "FEEL FREE, SIT DOWN AND RELAX!\n" +
                "( All Rights Reserved )";
        Element adsElement = new Element();
        adsElement.setTitle("Advertise with us");

        View aboutPage = new AboutPage(this)
                .isRTL(false)
                .setDescription(about)
                .setImage(R.mipmap.ic_launcher)
                .addItem(new Element().setTitle("Version 1.0"))
                .addItem(adsElement)
                //.addGroup("Connect with us")
                .addEmail("blessk2010@hotmail.co.uk")
                .addWebsite("data.mlw.mw/portal/team","Data portal")
                .addFacebook("blesswellk")
                /*.addTwitter("medyo80")100001741509914
                .addYoutube("UCdPQtdWIsg7_pi4mrRu46vA")
                .addPlayStore("com.ideashower.readitlater.pro")*/
                .addInstagram("blesswellblessk")
                /*.addGitHub("medyo")*/
                .addItem(getCopyRightsElement())
                .create();
        setContentView(aboutPage);
        /*getSupportActionBar().setIcon(R.mipmap.logo);
        getSupportActionBar().setDisplayShowHomeEnabled(true);*/
    }
    Element getCopyRightsElement() {
        Element copyRightsElement = new Element();
        final String copyrights = String.format(getString(R.string.copy_right).toString(), Calendar.getInstance().get(Calendar.YEAR));
        copyRightsElement.setTitle(copyrights);
        //copyRightsElement.setIconDrawable(R.drawable.about_icon_link);
        copyRightsElement.setGravity(Gravity.CENTER);
        copyRightsElement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(About.this, copyrights, Toast.LENGTH_SHORT).show();
            }
        });
        return copyRightsElement;
    }
}
