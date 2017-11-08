package com.orangesunshine.moritwitter;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.gson.Gson;

import java.util.ArrayList;

/**
 * Created by hayatomoritani on 12/5/16.
 */

public class TweetDataBase extends SQLiteOpenHelper {

    private static final String DB_NAME = "tweets_data_base";
    public static final String FOLLOWING_TB = "followings";
    public static final String FOLLOWING_TB2 = "followings2";
    public static final String FOLLOWING_TB3 = "followings3";
    public static final String FOLLOWING_TB4 = "followings4";
    private static final String ID = "_id";
    private static final String FOLLOWING = "following";
    private static final String NEW_TWEETS = "new_tweets";  // use this to check disabled, 0 is diabled and 1 is not, temporary use need to change
    private static final String SCREEN_NAME = "screen_name";

    public TweetDataBase(Context context) {
        super(context, DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = String.format("CREATE TABLE %s(%s integer primary key, %s text,%s text, %s integer)", FOLLOWING_TB, ID,SCREEN_NAME, FOLLOWING,NEW_TWEETS);
        db.execSQL(sql);
        String sql2 = String.format("CREATE TABLE %s(%s integer primary key, %s text,%s text, %s integer)", FOLLOWING_TB2, ID,SCREEN_NAME, FOLLOWING,NEW_TWEETS);
        db.execSQL(sql2);
        String sql3 = String.format("CREATE TABLE %s(%s integer primary key, %s text,%s text, %s integer)", FOLLOWING_TB3, ID,SCREEN_NAME, FOLLOWING,NEW_TWEETS);
        db.execSQL(sql3);
        String sql4 = String.format("CREATE TABLE %s(%s integer primary key, %s text,%s text, %s integer)", FOLLOWING_TB4, ID,SCREEN_NAME, FOLLOWING,NEW_TWEETS);
        db.execSQL(sql4);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void saveFollowings (ArrayList<BasicUserInfo> users,int accountNumber){

        SQLiteDatabase db = getWritableDatabase();
        String box;
        switch (accountNumber){
            case 3:
                box = String.format("INSERT INTO %s(%s,%s,%s)", FOLLOWING_TB4,SCREEN_NAME, FOLLOWING,NEW_TWEETS);
                break;
            case 2:
                box = String.format("INSERT INTO %s(%s,%s,%s)", FOLLOWING_TB3,SCREEN_NAME, FOLLOWING,NEW_TWEETS);
                break;
            case 1:
                box = String.format("INSERT INTO %s(%s,%s,%s)", FOLLOWING_TB2,SCREEN_NAME, FOLLOWING,NEW_TWEETS);
                break;
            default:
                box = String.format("INSERT INTO %s(%s,%s,%s)", FOLLOWING_TB,SCREEN_NAME, FOLLOWING,NEW_TWEETS);
                break;
        }
        for (BasicUserInfo user: users){
            Gson gson = new Gson();
            String userJson = gson.toJson(user);
            int intDisabled;
            String inside = String.format("('%s','%s',%s );", user.getScreenName(),userJson,user.getIsDisabled());
            String sql = box + " VALUES " + inside;
            db.execSQL(sql);
        }
    }

    public void changeTweetCount(BasicUserInfo modifyUser,int accountNumber){
        Gson gson = new Gson();
        String userJson = gson.toJson(modifyUser);
        SQLiteDatabase db = getWritableDatabase();
        String sql;
        switch (accountNumber){
            case 3:
                sql = String.format("UPDATE %s SET %s = '%s' WHERE %s = '%s';", FOLLOWING_TB4, FOLLOWING,userJson,SCREEN_NAME,modifyUser.getScreenName());
                break;
            case 2:
                sql = String.format("UPDATE %s SET %s = '%s' WHERE %s = '%s';", FOLLOWING_TB3, FOLLOWING,userJson,SCREEN_NAME,modifyUser.getScreenName());
                break;
            case 1:
                sql = String.format("UPDATE %s SET %s = '%s' WHERE %s = '%s';", FOLLOWING_TB2, FOLLOWING,userJson,SCREEN_NAME,modifyUser.getScreenName());
                break;
            default:
                sql = String.format("UPDATE %s SET %s = '%s' WHERE %s = '%s';", FOLLOWING_TB, FOLLOWING,userJson,SCREEN_NAME,modifyUser.getScreenName());
                break;
        }
        db.execSQL(sql);
    }

    public void changeIsDisable(String userName,int accountNumber){
        SQLiteDatabase db = getWritableDatabase();
        String sql;
        switch (accountNumber){
            case 3:
                sql = String.format("UPDATE %s SET %s = %s WHERE %s = '%s';", FOLLOWING_TB4, NEW_TWEETS,0,SCREEN_NAME,userName);
                break;
            case 2:
                sql = String.format("UPDATE %s SET %s = %s WHERE %s = '%s';", FOLLOWING_TB3, NEW_TWEETS,0,SCREEN_NAME,userName);
                break;
            case 1:
                sql = String.format("UPDATE %s SET %s = %s WHERE %s = '%s';", FOLLOWING_TB2, NEW_TWEETS,0,SCREEN_NAME,userName);
                break;
            default:
                sql = String.format("UPDATE %s SET %s = %s WHERE %s = '%s';", FOLLOWING_TB, NEW_TWEETS,0,SCREEN_NAME,userName);
                break;
        }
        db.execSQL(sql);
    }

    public void deleteTableData(String tableName){

        SQLiteDatabase db = getWritableDatabase();
        String sql = String.format("DELETE FROM %s", tableName);
        //Log.d("tag", "TweetDB deleteTableData: "+sql);
        //Log.d("tag", "deleteTableData: "+sql);
        db.execSQL(sql);
    }

    public void deleteDataBase(){
        this.deleteTableData(DB_NAME);
    }

    public ArrayList<BasicUserInfo> getFollowings(int accountNumber){
        ArrayList<BasicUserInfo> names = new ArrayList<>();
        Gson gson = new Gson();

        SQLiteDatabase db = getReadableDatabase();
        String sql;
        switch (accountNumber){
            case 3:
                sql = String.format("SELECT %s FROM %s",FOLLOWING,FOLLOWING_TB4);
                break;
            case 2:
                sql = String.format("SELECT %s FROM %s",FOLLOWING,FOLLOWING_TB3);
                break;
            case 1:
                sql = String.format("SELECT %s FROM %s",FOLLOWING,FOLLOWING_TB2);
                break;
            default:
                sql = String.format("SELECT %s FROM %s",FOLLOWING,FOLLOWING_TB);
                break;
        }
        //Log.d("tag", "TweetDB getfollowings: "+sql);
        Cursor cursor = db.rawQuery(sql, null);
        while (cursor.moveToNext()) {
            String a = cursor.getString(0);
            BasicUserInfo user = gson.fromJson(a,BasicUserInfo.class);
            names.add(user);
        }

        cursor.close();
        return names;
    }
}
