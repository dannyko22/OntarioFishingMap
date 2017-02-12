package com.ontariofishingmap.app;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
/**
 * Created by Danny on 02/07/2014.
 */
public class DataBaseHelper extends SQLiteOpenHelper {

    //The Android's default system path of your application database.
    private static String DB_PATH = "/data/data/com.ontariofishingmap.app/databases/";

    private static String DB_NAME = "ontariofishing.db";

    private SQLiteDatabase myDataBase;

    private final Context myContext;


    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // table name
    private static final String TABLE_FISH = "fishdatapoints";

    // Table Columns names
    private static final String KEY_ID = "_id";
    private static final String Name = "name";
    private static final String Latitude = "Latitude";
    private static final String Longitude = "Longitude";
    private static final String Location = "Location";
    private static final String URL = "URL";
    private static final String Species = "Species";

    /**
     * Constructor
     * Takes and keeps a reference of the passed context in order to access to the application assets and resources.
     * @param context
     */
    public DataBaseHelper(Context context) {

        super(context, DB_NAME, null, 1);
        this.myContext = context;
    }

    /**
     * Creates a empty database on the system and rewrites it with your own database.
     * */
    public void createDataBase() throws IOException {

        boolean dbExist = checkDataBase();

        if(dbExist){
            //do nothing - database already exist
        }else{

            //By calling this method and empty database will be created into the default system path
            //of your application so we are gonna be able to overwrite that database with our database.
            this.getReadableDatabase();

            try {
                File f = new File(DB_PATH);
                if (!f.exists()) {
                    f.mkdir();
                }

                copyDataBase();

            } catch (IOException e) {

                throw new Error("Error copying database");

            }
        }

    }

    /**
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     * @return true if it exists, false if it doesn't
     */
    private boolean checkDataBase(){

        SQLiteDatabase checkDB = null;

        try{
            String myPath = DB_PATH + DB_NAME;
            File myFile = myContext.getDatabasePath(DB_NAME);
            //checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
            checkDB = SQLiteDatabase.openDatabase(myFile.getPath(), null, SQLiteDatabase.OPEN_READONLY);

        }catch(SQLiteException e){

        //database does't exist yet.

        }

        if(checkDB != null){

            checkDB.close();

        }

        return checkDB != null ? true : false;
    }

    /**
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transfering bytestream.
     * */
    private void copyDataBase() throws IOException{

        //Open your local db as the input stream
        InputStream myInput = myContext.getAssets().open(DB_NAME);

// Path to the just created empty db
        String outFileName = DB_PATH + DB_NAME;

//Open the empty db as the output stream
        OutputStream myOutput = new FileOutputStream(outFileName);

//transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;

        while ((length = myInput.read(buffer))>0){
            myOutput.write(buffer, 0, length);
        }

//Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();

    }

    public void openDataBase() throws SQLException {

//Open the database
        String myPath = DB_PATH + DB_NAME;
        File myFile = myContext.getDatabasePath(DB_NAME);
        myDataBase = SQLiteDatabase.openDatabase(myFile.getPath(), null, SQLiteDatabase.OPEN_READONLY);

    }

    @Override
    public synchronized void close() {

        if(myDataBase != null)
            myDataBase.close();

        super.close();

    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


    // Add your public helper methods to access and get content from the database.
    // You could return cursors by doing "return myDataBase.query(....)" so it'd be easy
    // to you to create adapters for your views.

    // Getting single contact
    ArrayList<FishData> getFishData() {

        FishData fishData;
        Cursor cursor;
        ArrayList<FishData> items = new ArrayList<FishData>();

        // 1. build the query
        String query = "SELECT  * FROM " + TABLE_FISH;


        cursor = myDataBase.rawQuery(query, null);

        int count = cursor.getCount();
        // populate array list
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            while (count != 0) {
                fishData = new FishData();
                fishData.setID(Integer.parseInt(cursor.getString(0)));
                fishData.setName(cursor.getString(1));
                fishData.setLatitude(cursor.getString(2));
                fishData.setLongitude(cursor.getString(3));
                fishData.setLocation(cursor.getString(4));
                fishData.setURL(cursor.getString(5));
                fishData.setSpecies(cursor.getString(6));
                items.add(fishData);
                cursor.moveToNext();
                count--;
            }
        }
        else
        {
            fishData =  new FishData();
            items.add(fishData);
        }
        // return contact

        return items;
    }


}