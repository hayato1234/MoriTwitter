package com.orangesunshine.moritwitter.ShowMedia;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;

import com.simleman.moritwitter.R;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class VideoActivity extends AppCompatActivity {

    String videoURL;
    Context context;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;


    public static final String MEDIA_VIDEO = "aherbavkdubfh";
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private VideoView videoView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            videoView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    ///private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
//            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_video);
        context = this;
        if (getSupportActionBar()!=null){
            getSupportActionBar().setTitle("");
        }


        mVisible = true;
        videoURL = getIntent().getStringExtra(MEDIA_VIDEO);
        Log.d("tag", "videoA onMediaClicked: "+videoURL);
        videoView = (VideoView) findViewById(R.id.video_video_view);
        videoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                toggle();
                return false;
            }
        });
        if (videoURL == null) {
            Snackbar.make(videoView, R.string.Invalid_URL, Snackbar.LENGTH_INDEFINITE)
                    .setAction("Back", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onBackPressed();
                        }
                    }).show();
        } else {
            videoView.setVideoURI(Uri.parse(videoURL));
            videoView.setMediaController(new MediaController(this));
            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    videoView.start();
                }
            });
        }

        // Set up the user interaction to manually show or hide the system UI.

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
//        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        videoView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_media,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.save_media:
                if (isExternalStorageWritable()){ // storage available
                    if (ContextCompat.checkSelfPermission(VideoActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED){//permission is not granted
                        ActivityCompat.requestPermissions(VideoActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                    }else {
                        saveVideo(videoURL);
                    }
                }
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveVideo(final String savingURL) {

        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/MoriTwitter");
        Random generator = new Random();
        int n = 10000;
        n = generator.nextInt(n);
        String fname = "Video-"+ n +".mp4";
        final File file = new File (myDir, fname);
        if (file.exists ()) file.delete ();
        if (!myDir.exists()){
            try{
                myDir.mkdir();
            }catch (Exception e){
                Log.d("tag", "videoA, erro saving: "+e);
            }
        }
        new AsyncTask<Void, Void, Void>() {
            NotificationCompat.Builder mBuilder;
            NotificationManager mNotificationManager;
            @Override
            protected void onPreExecute() {
                mBuilder =
                        new NotificationCompat.Builder(context)
                                .setSmallIcon(R.drawable.ic_loading)
                                .setTicker(context.getResources().getString(R.string.downloading) + "...")
                                .setContentTitle(context.getResources().getString(R.string.app_name))
                                .setContentText("Saving video...")
                                .setProgress(100, 100, true)
                                .setOngoing(true);

                mNotificationManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.notify(6, mBuilder.build());
                super.onPreExecute();
            }

            @Override
            protected Void doInBackground(Void... params) {
                try {

                    // save it background
                    URL url = new URL(savingURL);
                    HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                    //connection.setRequestMethod("GET");
                    connection.setReadTimeout(5000);
                    connection.setConnectTimeout(30000);
                    connection.connect();

                    InputStream in = connection.getInputStream();
                    BufferedInputStream bis = new BufferedInputStream(in,1024*5);
                    FileOutputStream out = new FileOutputStream(file);
                    byte[] buffer = new byte[1024 * 5];
                    int len;
                    while((len=bis.read(buffer))!=-1){
                        out.write(buffer,0,len);
                    }
                    out.flush();
                    out.close();
                    bis.close();
                    Uri uri = Uri.fromFile(file);
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setDataAndType(uri, "video/*");
                    PendingIntent pending = PendingIntent.getActivity(context, 91, intent, 0);
                    mBuilder =
                            new NotificationCompat.Builder(context)
                                    .setSmallIcon(R.mipmap.ic_launcher)
                                    .setContentIntent(pending)
                                    .setAutoCancel(true)
                                    .setContentTitle(context.getResources().getString(R.string.app_name))
                                    .setContentText("Saved video!");

                    mNotificationManager.notify(6, mBuilder.build());

                }catch (MalformedURLException murle){
                    Log.d("tag", "videoA, saveVideo MalformedURLException:  "+murle);
                }catch (IOException ioe){
                    Log.d("tag", "videoA, saveVideo IOException:  "+ioe);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
//                Snackbar.make(videoView,"video saved",Snackbar.LENGTH_LONG)
//                        .setAction("Ok", new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                // for dismiss
//                            }
//                        }).show();
                super.onPostExecute(aVoid);
            }
        }.execute();


    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permitted
                    saveVideo(videoURL);
                }else {
                    Snackbar.make(videoView,"fail to save video",Snackbar.LENGTH_SHORT).show();
                }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }



}
