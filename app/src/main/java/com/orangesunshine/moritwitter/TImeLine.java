package com.orangesunshine.moritwitter;

import android.support.v7.app.AppCompatActivity;

//todo:  keystore, fabric

public class TImeLine extends AppCompatActivity {

//    ListView listView;
//    private static SharedPreferences mSharedPreferences;
//    private ArrayList<Tweet> tweets;
//    private static final String TWITTER_KEY = BuildConfig.TWITTER_KEY;
//    private static final String TWITTER_SECRET = BuildConfig.TWITTER_SECRET;
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_time_line);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
//
//        mSharedPreferences = getApplicationContext().getSharedPreferences(
//                PREFERENCE_NAME, 0);
//        tweets = new ArrayList<>();
//
//        listView = (ListView)findViewById(R.id.timeLineListView);
//        new AsyncTask<Void, Void, Void>() {
//            private ProgressDialog progressDialog;
//
//            protected void onPreExecute() {
//                progressDialog = ProgressDialog.show(TImeLine.this,
//                        "", "Loading. Please wait...", true);
//            }
//            @Override
//            protected Void doInBackground(Void... params) {
//                try {
//
//                    ConfigurationBuilder builder = new ConfigurationBuilder();
//                    builder.setOAuthConsumerKey(TWITTER_KEY);
//                    builder.setOAuthConsumerSecret(TWITTER_SECRET);
//                    //TLの取得
//                    // Access Token
//                    String access_token = mSharedPreferences.getString(PREF_KEY_OAUTH_TOKEN, "");
//                    // Access Token Secret
//                    String access_token_secret = mSharedPreferences.getString(PREF_KEY_OAUTH_SECRET, "");
//
//                    AccessToken accessToken = new AccessToken(access_token, access_token_secret);
//                    Twitter twitter = new TwitterFactory(builder.build()).getInstance(accessToken);
//
//                    ResponseList<twitter4j.Status> homeTl = twitter.getHomeTimeline(); // loading each tweet and put in array
//
//                    for (twitter4j.Status status : homeTl) { // taking one tweet from the array
//                        Tweet tweet1 = new Tweet(); //tweet object
//                        //getting all media (pics, gif, video)
//                        MediaEntity[] mediaEntities = status.getMediaEntities();
//                        //つぶやきのユーザーIDの取得
//                        String userName = status.getUser().getScreenName();
//                        //つぶやきの取得
//                        String tweet = status.getText();
//                        String profilePictureURL = status.getUser().getProfileImageURL();
//
//                        if (mediaEntities.length >0){ //if there are media
//                            ArrayList<String> mediaURLs = new ArrayList<String>();
//                            for (int i = 0;i<mediaEntities.length;i++){
//                                MediaEntity mediaEntity = mediaEntities[i]; // get each medium
//                                mediaURLs.add(mediaEntity.getMediaURL());
//                            }
//                            tweet1.setMedia_images(mediaURLs);
//                        }
//                        tweet1.setUser(userName);
//                        tweet1.setContent(tweet);
//                        tweet1.setProf_image(profilePictureURL);
//                        tweets.add(tweet1);
//                    }
//
//                } catch (TwitterException e) {
//                    e.printStackTrace();
//                    if(e.isCausedByNetworkIssue()){
//                        Toast.makeText(getApplicationContext(), "ネットワークに接続して下さい", Toast.LENGTH_LONG);
//                    }else{
//                        Toast.makeText(getApplicationContext(), "エラーが発生しました。", Toast.LENGTH_LONG);
//                    }
//                }
//                return null;
//            }
//
//            @Override
//            protected void onPostExecute(Void aVoid) {
//                TweetListAdaptor adapter = new TweetListAdaptor(getBaseContext(),tweets);
//                listView.setAdapter(adapter);
//                progressDialog.dismiss();
//                super.onPostExecute(aVoid);
//            }
//        }.execute();
//    }
}
