package com.blessk.mlwparticipant;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;


public class DatabaseHelper extends SQLiteOpenHelper {
    private static String DB_NAME = "mlw_pid_search.db";
    private static String DB_PATH = "";
    private static final int DB_VERSION = 1;

    private static String PROJ_TABLE ="project";
    private static String LOC_TABLE ="location";
    private static String PART_TABLE ="participant";

    private SQLiteDatabase mDataBase;
    //private final Context mContext;
    private boolean mNeedUpdate = true;


    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        if (android.os.Build.VERSION.SDK_INT >= 17)
            DB_PATH = context.getApplicationInfo().dataDir + "/databases/";
        else
            DB_PATH = "/data/data/" + context.getPackageName() + "/databases/";
    }

    public void updateDataBase() throws IOException
    {
        if (mNeedUpdate) {
            File dbFile = new File(DB_PATH + DB_NAME);
            if (dbFile.exists())
                dbFile.delete();

            copyDataBase();

            mNeedUpdate = false;
        }
    }

    private boolean checkDataBase() {
        File dbFile = new File(DB_PATH + DB_NAME);
        return dbFile.exists();
    }

    private void copyDataBase()
    {
        if (!checkDataBase())
        {
            this.getReadableDatabase();
            this.close();
            try {
                copyDBFile();
            } catch (IOException mIOException) {
                throw new Error("ErrorCopyingDataBase "+mIOException.getMessage());
            }
        }
    }

    private void copyDBFile() throws IOException
    {
        String uri=Environment.getExternalStorageDirectory().toString()+"/mlw/mlw_pid_search/";

        FileInputStream mInput = new FileInputStream(new File(uri+"/"+DB_NAME));
        OutputStream mOutput = new FileOutputStream(DB_PATH + DB_NAME);
        byte[] mBuffer = new byte[1024];
        int mLength;
        while ((mLength = mInput.read(mBuffer)) > 0)
            mOutput.write(mBuffer, 0, mLength);
        mOutput.flush();
        mOutput.close();
        mInput.close();
    }

    public boolean openDataBase() throws SQLException {
        mDataBase = SQLiteDatabase.openDatabase(DB_PATH + DB_NAME, null, SQLiteDatabase.CREATE_IF_NECESSARY);
        return mDataBase != null;
    }

    @Override
    public synchronized void close() {
        if (mDataBase != null)
            mDataBase.close();
        super.close();
    }

    /*************************blessk*******************/
    public List<String> getParticipantLocation()
    {
        List<String> locations = new ArrayList<String>();


        String selectQuery = "SELECT  name FROM location ORDER BY name";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst())
        {
            do
            {
                locations.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return locations;
    }
    public Cursor getParticipantVisit(String location,String search_text_raw, String search_column,String sort)
    {
        SQLiteDatabase db=this.getReadableDatabase();
        String search_text="%"+search_text_raw+"%";
        Cursor result;
        //BUILDING LOCATION IF LOCATION=SEX DO NOT SHOW IN THE LIST LOCATION NAME
        String sql="SELECT p._id,p.pid,p.date,p.visit,p.status,l.name AS location FROM participant p  LEFT JOIN location l ON p.location_id=l._id ";
        if(!location.equals("All"))
        {
            if(search_column.equals("pid"))
            sql+=" WHERE l.name='"+location+"' AND p.pid='"+search_text_raw+"' ";
            else if (search_column.equals("date"))
                sql+=" WHERE l.name='"+location+"' AND p.date='"+search_text_raw+"' ";
        }
        else
        {
            if(search_column.equals("pid"))
                sql+=" WHERE p.pid='"+search_text_raw+"' ";
            else if (search_column.equals("date"))
                sql+=" WHERE p.date='"+search_text_raw+"' ";

        }
        if(sort.equals("Date"))
            sql+=" ORDER BY p.date";
        else
            sql+=" ORDER BY p.pid";

        result=db.rawQuery(sql,null);
        return result;
    }
    public String getProjectCode()
    {
        String project="Undefined";
        String selectQuery = "SELECT  name FROM project limit 1";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if(cursor.getCount()>0)
        {
            cursor.moveToNext();
            project=cursor.getString(0);

        }
        cursor.close();
        db.close();
        return project;
    }
    public String getParticipant(String id)
    {
        SQLiteDatabase db=this.getReadableDatabase();
        String sql="SELECT p.pid,p.date,p.visit,p.status,l.name AS location FROM participant p  LEFT JOIN location l ON p.location_id=l._id WHERE p.pid='"+id+"'";
        Cursor cursor = db.rawQuery(sql, null);
        String results="";
        if(cursor.getCount()>0)
        {
            cursor.moveToNext();
            results="\nPID: "+cursor.getString(0);
            results+="\nDate: "+cursor.getString(1);
            results+="\nVisit: "+cursor.getString(2);
            results+="\nStatus: "+cursor.getString(3);
            results+="\nLocation: "+cursor.getString(4);
            results+="\n\n Click Copy to copy the PID or Cancel to return to PID search.";

        }
        cursor.close();
        db.close();
        return results;

    }
    /*************************blessk*******************/

    @Override
    public void onCreate(SQLiteDatabase db)
    {
    }

    public void onCreateSettingsTable(SQLiteDatabase db)
    {
        db.execSQL(
                "CREATE TABLE "+this.PROJ_TABLE+"(" +
                        "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "name VARCHAR(50) UNIQUE,"+
                        "ip VARCHAR(20) UNIQUE,"+
                        "auth_code VARCHAR(20)," +
                        "last_updated VARCHAR(30))"
        );
        db.execSQL(
                "CREATE TABLE "+this.LOC_TABLE+"(" +
                        "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "name VARCHAR(50) UNIQUE)"
        );
        db.execSQL(
                "CREATE TABLE "+this.PART_TABLE+"(" +
                        "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "location_id VARCHAR(50)," +
                        "date VARCHAR(30),"+
                        "pid VARCHAR(30),"+
                        "visit VARCHAR(30),"+
                        "status INTEGER,"+
                        " FOREIGN KEY (location_id) REFERENCES "+LOC_TABLE+"(_id)"+
                        ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("DROP TABLE IF EXISTS "+PROJ_TABLE);
        db.execSQL("DROP TABLE IF EXISTS "+PART_TABLE);
        db.execSQL("DROP TABLE IF EXISTS "+LOC_TABLE);
        Log.v("Database Upgrade", "Database version higher than old.");
        onCreateSettingsTable(db);
    }
    public boolean createSetting(String name, String ip, String auth_code)
    {
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues contentValues=new ContentValues();
        contentValues.put("name",name);
        contentValues.put("ip",ip);
        contentValues.put("auth_code",auth_code);
        long result=db.insert(this.PROJ_TABLE,null,contentValues);
        db.close();
        if(result==-1)
        {
            return false;
        }
        else
            return true;
    }
    public boolean updateSetting(String name, String ip, String auth_code, String old_name)
    {
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues contentValues=new ContentValues();
        contentValues.put("name",name);
        contentValues.put("ip",ip);
        contentValues.put("auth_code",auth_code);
        long result=db.update(this.PROJ_TABLE,contentValues,"name=?",new String[]{old_name});
        db.close();
        if(result==-1)
        {
            return false;
        }
        else
            return true;
    }
    public boolean updateSettingLastUpdate(String last_updated)
    {
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues contentValues=new ContentValues();
        contentValues.put("last_updated",last_updated);
        long result=db.update(this.PROJ_TABLE,contentValues,"name!=?",new String[]{"**"});
        db.close();
        if(result==-1)
        {
            return false;
        }
        else
            return true;
    }
    public String[] getProjectSetting()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        if(!checkIfTableExists(db,PROJ_TABLE))
        {
            this.onCreateSettingsTable(db);
        }
        String project=null;
        String ip=null;
        String auth_code=null;
        String last_updated=null;
        String selectQuery = "SELECT  name,ip,auth_code,last_updated FROM "+PROJ_TABLE+" limit 1";

        try
        {
            //DB operation
            Cursor cursor = db.rawQuery(selectQuery, null);
            if(cursor.getCount()>0)
            {
                cursor.moveToNext();
                project=cursor.getString(0);
                ip=cursor.getString(1);
                auth_code=cursor.getString(2);
                last_updated=cursor.getString(3);

            }
            cursor.close();
        }
        finally
        {
            db.close();
            return new String[]{project,ip,auth_code,last_updated};
        }

    }
    public void deleteAllLocations()
    {
        SQLiteDatabase db=this.getWritableDatabase();
        db.execSQL("DELETE FROM " + LOC_TABLE);
        db.close();
    }
    public void deleteAllParticipants()
    {
        SQLiteDatabase db=this.getWritableDatabase();
        db.execSQL("DELETE FROM " + PART_TABLE);
        db.close();
    }
    public boolean createParticipantVisit(String location, String date, String pid,String visit ,String status)
    {
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues contentValues=new ContentValues();
        contentValues.put("location_id",location);
        contentValues.put("date",date);
        contentValues.put("pid",pid);
        contentValues.put("visit",visit);
        contentValues.put("status",status);
        long result=db.insert(this.PART_TABLE,null,contentValues);
        db.close();
        if(result==-1)
        {
            return false;
        }
        else
            return true;
    }
    public boolean createLocation(String id,String name)
    {
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues contentValues=new ContentValues();
        contentValues.put("_id",id);
        contentValues.put("name",name);
        long result=db.insert(this.LOC_TABLE,null,contentValues);
        db.close();
        if(result==-1)
        {
            return false;
        }
        else
            return true;
    }
    private boolean checkIfTableExists(SQLiteDatabase db, String table)
    {
        String sql="SELECT name FROM sqlite_master WHERE type='table' AND name='"+table+"'";
        Cursor mCursor=db.rawQuery(sql,null);
        if(mCursor.getCount()>0)
        {
            return true;
        }
        mCursor.close();
        return false;
    }
}