package com.simleman.moritwitter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import java.io.InputStream;
import java.net.URL;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class MainActivity extends AppCompatActivity {

    static String TWITTER_CONSUMER_KEY = "";
    static String TWITTER_CONSUMER_SECRET = "";

    // Preference Constants
    static String PREFERENCE_NAME = "twitter_oauth";
    static final String PREF_KEY_OAUTH_TOKEN = "oauth_token";
    static final String PREF_KEY_OAUTH_SECRET = "oauth_token_secret";
    static final String PREF_KEY_TWITTER_LOGIN = "isTwitterLoggedIn";
    private static final String PREF_USER_NAME = "twitter_user_name";
    public static final String PREF_PROFILE_IMAGE = "twitter_profile_url";

    static final String TWITTER_CALLBACK_URL = "oauth://callback";

    // Twitter oauth urls
    static final String URL_TWITTER_OAUTH_VERIFIER = "oauth_verifier";

    // Progress dialog
    ProgressDialog pDialog;

    // Twitter
    private static Twitter mTwitter;
    private static RequestToken requestToken;

    /* Any number for uniquely distinguish your request */
    public static final int WEBVIEW_REQUEST_CODE = 100;

    // Shared Preferences
    private static SharedPreferences mSharedPreferences;

    private RelativeLayout profileLayout, loginLayout;
    private TextView mProfileName;
    private ImageView mProfileImage;
    private EditText mTweetText;

    private Bitmap mProfileBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Shared Preferences
        mSharedPreferences = getApplicationContext().getSharedPreferences(
                PREFERENCE_NAME, 0);

        loginLayout = (RelativeLayout) findViewById(R.id.login_layout);
        profileLayout = (RelativeLayout) findViewById(R.id.profile_layout);
        mProfileName = (TextView) findViewById(R.id.user_name);
        mProfileImage = (ImageView) findViewById(R.id.user_profilePicture);
        mTweetText = (EditText) findViewById(R.id.tweet_text);

        updateUI();
    }

    /**
     * Check to see if user currently logged in or not and updates the UI
     */
    private void updateUI() {
        if (isUserLoggedIn()) {
            loginLayout.setVisibility(View.GONE);
            profileLayout.setVisibility(View.VISIBLE);

            String username = mSharedPreferences.getString(PREF_USER_NAME, "");
            String profilePictureURL = mSharedPreferences.getString(PREF_PROFILE_IMAGE, "");
            new LoadProfilePicture().execute(profilePictureURL);
            // Displaying in xml ui
            mProfileName.setText(Html.fromHtml("<b>Welcome " + username + "</b>"));
        } else {
            loginLayout.setVisibility(View.VISIBLE);
            profileLayout.setVisibility(View.GONE);
        }
    }

    /**
     * Calls when user click tweet button
     * @param view
     */
    public void postTweet(View view) {
        // Call update status function
        // Get the status from EditText
        String status = mTweetText.getText().toString();

        if (TextUtils.isEmpty(status)) {
            // EditText is empty
            Toast.makeText(getApplicationContext(),
                    "Please enter status message", Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        // update status
        new PostTweetOnTwitter().execute(status);
        mTweetText.setText("");

    }

    /**
     * Calls when user click login button
     * @param view
     */
    public void loginUser(View view) {
        new LoginUserOnTwitter().execute();
    }

    /**
     * Calls when user click logout button
     * @param view
     */
    public void logoutUser(View view) {
        // Clear the shared preferences
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.remove(PREF_KEY_OAUTH_TOKEN);
        editor.remove(PREF_KEY_OAUTH_SECRET);
        editor.remove(PREF_KEY_TWITTER_LOGIN);
        editor.remove(PREF_USER_NAME);
        editor.remove(PREF_PROFILE_IMAGE);
        editor.apply();
        updateUI();
    }

    /**
     * Check user already logged in your application using twitter Login flag is
     * fetched from Shared Preferences
     */
    private boolean isUserLoggedIn() {
        // return twitter login status from Shared Preferences
        return mSharedPreferences.getBoolean(PREF_KEY_TWITTER_LOGIN, false);
    }

    /**
     * Function to login user
     */
    class LoginUserOnTwitter extends AsyncTask<Void, Void, String> {


        @Override
        protected String doInBackground(Void... params) {

            ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
            configurationBuilder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
            configurationBuilder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);
            Configuration configuration = configurationBuilder.build();
            mTwitter = new TwitterFactory(configuration).getInstance();
            try {
                requestToken = mTwitter.getOAuthRequestToken();

                /*/*//**
                 *  Loading twitter login page on webview for authorization
                 *  Once authorized, results are received at onActivityResult
                 *  *//**//**/

                final Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
                intent.putExtra(WebViewActivity.EXTRA_URL, requestToken.getAuthenticationURL());
                startActivityForResult(intent, WEBVIEW_REQUEST_CODE);

            } catch (TwitterException e) {
                Log.d("TAG", "doInBackground: "+e.toString());
                e.printStackTrace();
            }
            return null;
        }
    }

    /**
     * Saving user information, after user is authenticated for the first time.
     * You don't need to show user to login, until user has a valid access toen
     */
    private void saveTwitterInfo(AccessToken accessToken) {

        long userID = accessToken.getUserId();

        User user;
        try {
            user = mTwitter.showUser(userID);

            String username = user.getName();
            String profilePicture = user.getOriginalProfileImageURL();
            /* Storing oAuth tokens to shared preferences */
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString(PREF_KEY_OAUTH_TOKEN, accessToken.getToken());
            editor.putString(PREF_KEY_OAUTH_SECRET, accessToken.getTokenSecret());
            editor.putBoolean(PREF_KEY_TWITTER_LOGIN, true);
            editor.putString(PREF_USER_NAME, username);
            editor.putString(PREF_PROFILE_IMAGE, profilePicture);
            editor.apply();

        } catch (TwitterException e1) {
            e1.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == WEBVIEW_REQUEST_CODE && data != null) {
                final Uri uri = Uri.parse(data.getStringExtra("KEY_URI"));
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String verifier = uri.getQueryParameter(MainActivity.URL_TWITTER_OAUTH_VERIFIER);
                        try {
                            AccessToken accessToken = mTwitter.getOAuthAccessToken(requestToken, verifier);
                            saveTwitterInfo(accessToken);
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateUI();
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                            if (e.getMessage() != null) {
                                Log.e("Twitter-->", e.getMessage());

                            } else {
                                Log.e("Twitter-->", "ERROR: Twitter callback failed");
                            }
                        }
                    }
                }).start();
            }

            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * Function to update status
     */
    class PostTweetOnTwitter extends AsyncTask<String, Void, String> {

        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Updating to twitter...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * getting Places JSON
         */
        protected String doInBackground(String... args) {
            Log.d("Tweet Text", "> " + args[0]);
            String status = args[0];
            try {
                ConfigurationBuilder builder = new ConfigurationBuilder();
                builder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
                builder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);

                // Access Token
                String access_token = mSharedPreferences.getString(PREF_KEY_OAUTH_TOKEN, "");
                // Access Token Secret
                String access_token_secret = mSharedPreferences.getString(PREF_KEY_OAUTH_SECRET, "");

                AccessToken accessToken = new AccessToken(access_token, access_token_secret);
                Twitter twitter = new TwitterFactory(builder.build()).getInstance(accessToken);

                // Update status
                twitter4j.Status response = twitter.updateStatus(status);

                Log.d("Status", "> " + response.getText());
            } catch (TwitterException e) {
                // Error in updating status
                Log.d("Twitter Update Error", e.getMessage());
            }
            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after getting all products
            pDialog.dismiss();
        }

    }

    /**
     * Function to load profile picture
     */
    private class LoadProfilePicture extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Loading profile ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();

        }

        /**
         * Download image from the url
         **/
        protected Bitmap doInBackground(String... args) {
            try {
                mProfileBitmap = BitmapFactory.decodeStream((InputStream) new URL(args[0]).getContent());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return mProfileBitmap;
        }

        /**
         * After completing background task Dismiss the progress dialog and set bitmap to imageview
         **/
        protected void onPostExecute(Bitmap image) {
            Bitmap image_circle = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);

            BitmapShader shader = new BitmapShader(image, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            Paint paint = new Paint();
            paint.setShader(shader);
            Canvas c = new Canvas(image_circle);
            c.drawCircle(image.getWidth() / 2, image.getHeight() / 2, image.getWidth() / 2, paint);
            mProfileImage.setImageBitmap(image_circle);

            pDialog.hide();

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent i = new Intent(this,TImeLine.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
