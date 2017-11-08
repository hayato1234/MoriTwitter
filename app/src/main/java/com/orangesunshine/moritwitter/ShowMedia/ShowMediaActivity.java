package com.orangesunshine.moritwitter.ShowMedia;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.orangesunshine.moritwitter.FollowList.FollowListMain;
import com.simleman.moritwitter.R;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;

import me.relex.circleindicator.CircleIndicator;

public class ShowMediaActivity extends AppCompatActivity {

    public static final String MEDIA_POSITION = "oaksdbjfalhdf";
    public static final String MEDIA_POS = "uoyrvuajeutruiwe";
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 0;

    MediaViewPager mediaViewPager;
    CircleIndicator circleIndicator;
    String[] mediaUri;
    Bitmap bitmapa;

    int mediaPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_media);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_tab_home);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ShowMediaActivity.this, FollowListMain.class));
            }
        });
        if (getSupportActionBar()!=null){
            getSupportActionBar().setTitle("");
        }

        mediaUri = getIntent().getStringArrayExtra(MEDIA_POSITION);
        mediaPosition = getIntent().getIntExtra(MEDIA_POS,0);

        mediaViewPager = (MediaViewPager)findViewById(R.id.media_view_pager);

        if (mediaUri!=null){
            circleIndicator = (CircleIndicator)findViewById(R.id.indicator);
            if (mediaUri.length==1)circleIndicator.setVisibility(View.GONE);
            MediaViewPagerAdapter adapter = new MediaViewPagerAdapter(mediaUri);
            mediaViewPager.setAdapter(adapter);
            circleIndicator.setViewPager(mediaViewPager);
            mediaViewPager.setCurrentItem(mediaPosition);
        }
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
                saveImage(mediaViewPager.getCurrentItem());
                break;
            case R.id.show_media_original:
                moveToTweet();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveImage(int position){

        String imageUri = mediaUri[position];
        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        DataSource<CloseableReference<CloseableImage>> dataSource =
                imagePipeline.fetchDecodedImage(ImageRequest.fromUri(imageUri), this);

        dataSource.subscribe(new BaseBitmapDataSubscriber() {

            @Override protected void onNewResultImpl(Bitmap bitmap) {


                bitmapa = bitmap;

                if (isExternalStorageWritable()){

                    if (ContextCompat.checkSelfPermission(ShowMediaActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED){//permission is not granted
                        ActivityCompat.requestPermissions(ShowMediaActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                    }else {
                        saveToInternalStorage(bitmap);

                    }
                } else {
                    Log.d("tag", "ShowMediaActivity, loaded: not writable");
                }
            }

            @Override protected void onFailureImpl(
                    DataSource<CloseableReference<CloseableImage>> dataSource) {
                Log.d("tag", "ShowMediaActivity, onFailureImpl: ");
            }

        }, CallerThreadExecutor.getInstance());

    }
    private void saveToInternalStorage(Bitmap bitmapImage){
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/MoriTwitter");
        Random generator = new Random();
        int n = 10000;
        n = generator.nextInt(n);
        String fname = "Image-"+ n +".jpg";
        File file = new File (myDir, fname);
        if (file.exists ()) file.delete ();
        if (!myDir.exists()){
            try{
                myDir.mkdir();
            }catch (Exception e){
                Log.d("tag", "showMediaAct, erro saving: "+e);
            }
        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            makeNotification(R.string.image_saved);
        } catch (Exception e) {
            makeNotification(R.string.image_not_saved);
            e.printStackTrace();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            final Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            final Uri contentUri = Uri.fromFile(file);
            scanIntent.setData(contentUri);
            sendBroadcast(scanIntent);
        } else {
            final Intent intent = new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory()));
            sendBroadcast(intent);
        }
   }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permitted
                    saveToInternalStorage(bitmapa);
                }else {
                    makeNotification(R.string.permission_needed);
                }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    private void moveToTweet(){

    }

    private void makeNotification(final int text){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ShowMediaActivity.this,text,Toast.LENGTH_SHORT).show();
            }
        });
    }
}
