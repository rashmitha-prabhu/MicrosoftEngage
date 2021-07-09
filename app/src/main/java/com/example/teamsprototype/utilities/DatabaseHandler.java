package com.example.teamsprototype.utilities;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.teamsprototype.model.MeetingModel;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {

//    Creates and manages the local SQLite Database used for the meeting scheduler

    private static final int VERSION = 1;
    private static final String NAME = "meetingDatabase";
    private static final String TABLE = "meeting";
    private static final String ID = "id";
    private static final String AGENDA = "agenda";
    private static final String DATE = "date";
    private static final String TIME = "time";
    private static final String CODE = "code";

    private static final String CREATE_TABLE = "CREATE TABLE " + TABLE +
            " ( " + ID +" INTEGER PRIMARY KEY AUTOINCREMENT, "+ AGENDA +" TEXT, "
            + DATE +" TEXT, " + TIME +" TEXT, " + CODE +" TEXT)";

    private SQLiteDatabase db;

    public DatabaseHandler(Context context) {
        super(context, NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE);
        onCreate(db);
    }

    public void openDatabase(){
        db = this.getWritableDatabase();
    }

    public void insertTask(MeetingModel task){
        ContentValues cv = new ContentValues();
        cv.put(AGENDA, task.getAgenda());
        cv.put(DATE, task.getDate());
        cv.put(TIME, task.getTime());
        cv.put(CODE, task.getCode());
        db.insert(TABLE, null, cv);
    }

    public List<MeetingModel> getTask(){
        List<MeetingModel> list = new ArrayList<>();
        Cursor cur = null;
        String query = "SELECT * FROM "+TABLE+" ORDER BY "+DATE+" ASC, "+TIME+" ASC";
        db.beginTransaction();
        try{
            cur = db.rawQuery(query, null);
            if(cur!=null){
                if(cur.moveToFirst()){
                    do{
                        MeetingModel task = new MeetingModel();
                        task.setId(cur.getInt(cur.getColumnIndex(ID)));
                        task.setAgenda(cur.getString(cur.getColumnIndex(AGENDA)));
                        task.setDate(cur.getString(cur.getColumnIndex(DATE)));
                        task.setTime(cur.getString(cur.getColumnIndex(TIME)));
                        task.setCode(cur.getString(cur.getColumnIndex(CODE)));
                        list.add(task);
                    } while(cur.moveToNext());
                }
            }
        } finally {
            assert cur != null;
            cur.close();
            db.endTransaction();
        }
        return list;
    }

    public void deleteTask(int id){
        db.delete(TABLE, ID+"=?", new String[]{String.valueOf(id)});
    }

}
