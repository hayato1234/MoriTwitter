package com.orangesunshine.moritwitter.FollowList;

import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.orangesunshine.moritwitter.LogInActivity;
import com.orangesunshine.moritwitter.TimeLineTab.TimeLineTabFragment;
import com.orangesunshine.moritwitter.search.SearchActivity;
import com.simleman.moritwitter.BuildConfig;
import com.simleman.moritwitter.R;
import com.orangesunshine.moritwitter.SettingsActivity;
import com.orangesunshine.moritwitter.TweetDataBase;
import com.squareup.picasso.Picasso;
import com.twitter.sdk.android.core.TwitterAuthConfig;

//import com.twitter.sdk.android.Twitter;
//import com.twitter.sdk.android.core.TwitterAuthConfig;
//import io.fabric.sdk.android.Fabric;

import io.fabric.sdk.android.Fabric;
import twitter4j.ResponseList;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;

public class FollowListMain extends AppCompatActivity implements TimeLineTabFragment.OnFragmentInteractionListener, NavigationView.OnNavigationItemSelectedListener {

    private static final String TWITTER_KEY = BuildConfig.TWITTER_KEY;
    private static final String TWITTER_SECRET = BuildConfig.TWITTER_SECRET;
    private static final String TAG = "tag";

    private static final int firstAccountId = 123456;
    private static final int secondAccountId = 123457;
    private static final int thirdAccountId = 123458;
    private static final int forthAccountId = 123459;


    public static final String[] USER_NAME_LIST = {LogInActivity.PREF_USER_NAME, LogInActivity.PREF_USER_NAME2, LogInActivity.PREF_USER_NAME3, LogInActivity.PREF_USER_NAME4};
    public static final String[] TOKEN_LIST = {LogInActivity.PREF_KEY_OAUTH_TOKEN, LogInActivity.PREF_KEY_OAUTH_TOKEN2, LogInActivity.PREF_KEY_OAUTH_TOKEN3, LogInActivity.PREF_KEY_OAUTH_TOKEN4};
    public static final String[] SECRET_LIST = {LogInActivity.PREF_KEY_OAUTH_SECRET, LogInActivity.PREF_KEY_OAUTH_SECRET2, LogInActivity.PREF_KEY_OAUTH_SECRET3, LogInActivity.PREF_KEY_OAUTH_SECRET4};
    public static final String[] ICON_LIST = {LogInActivity.PREF_PROFILE_IMAGE, LogInActivity.PREF_PROFILE_IMAGE2, LogInActivity.PREF_PROFILE_IMAGE3, LogInActivity.PREF_PROFILE_IMAGE4};
    static final String[] DB_TABLE_LIST = {TweetDataBase.FOLLOWING_TB,TweetDataBase.FOLLOWING_TB2,TweetDataBase.FOLLOWING_TB3,TweetDataBase.FOLLOWING_TB4};
    private static final String[] IS_LOGIN_LIST = {LogInActivity.PREF_KEY_TWITTER_LOGIN, LogInActivity.PREF_KEY_TWITTER_LOGIN2, LogInActivity.PREF_KEY_TWITTER_LOGIN3, LogInActivity.PREF_KEY_TWITTER_LOGIN4};

    private FollowPagerAdapter followPagerAdapter;
    private ViewPager viewPager;
    TabLayout tabLayout;
    String userName;
    TweetDataBase dataBase;
    private int[] tabList = new int[]{R.drawable.ic_tab_home,R.drawable.ic_tab_time_line,R.drawable.ic_tab_mention,R.drawable.ic_tab_me};
    private int numberOfAccounts;
    public static int accountNumber;

    private NavigationView navigationView;
    private SearchView searchView;

    public static twitter4j.Twitter myTwitter;
    ConfigurationBuilder builder;
    AccessToken accessToken;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activty_follow_list_main_drawer_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new com.twitter.sdk.android.Twitter(authConfig));

        sharedPreferences = getApplicationContext().getSharedPreferences(
                LogInActivity.PREFERENCE_NAME, 0);

        if (!sharedPreferences.getBoolean(LogInActivity.PREF_KEY_AT_LEAST_ONE_LOGIN,false)){
            startActivity(new Intent(this, LogInActivity.class));
            Log.d(TAG, "fListMain onCreate: not logged in any account yet"+numberOfAccounts);
        }else {
            numberOfAccounts = sharedPreferences.getInt(LogInActivity.PREF_ACCOUNT_COUNT,0);
            Log.d(TAG, "fListMain onCreate: "+numberOfAccounts);


            String access_token;
            String access_token_secret;
            accountNumber = sharedPreferences.getInt(LogInActivity.PREF_CURRENT_ACCOUNT,0);
            Log.d(TAG, "fListMain onCreate: account#"+accountNumber);
            dataBase = new TweetDataBase(FollowListMain.this);

            userName = sharedPreferences.getString(USER_NAME_LIST[accountNumber],"me");
            access_token = sharedPreferences.getString(TOKEN_LIST[accountNumber],"");
            access_token_secret = sharedPreferences.getString(SECRET_LIST[accountNumber],"");

            setNavView();

            viewPager=(ViewPager)findViewById(R.id.follow_view_pager);
            viewPager.setOffscreenPageLimit(2);
            followPagerAdapter = new FollowPagerAdapter(getSupportFragmentManager(),userName);
            viewPager.setAdapter(followPagerAdapter);
            tabLayout = (TabLayout)findViewById(R.id.follow_tab_layout);
            tabLayout.setupWithViewPager(viewPager);
            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {

                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {

                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {
                    if (tab.getPosition()==1){
                        TimeLineTabFragment fragment = (TimeLineTabFragment) followPagerAdapter.getFragment(1);
                        fragment.scrollToTop();
                    }
                }
            });

            for (int i =0;i<4;i++){
                tabLayout.getTabAt(i).setIcon(tabList[i]);
            }

            builder = new ConfigurationBuilder();
            builder.setOAuthConsumerKey(TWITTER_KEY);
            builder.setOAuthConsumerSecret(TWITTER_SECRET);
            accessToken = new AccessToken(access_token, access_token_secret);
            myTwitter = new TwitterFactory(builder.build()).getInstance(accessToken);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new MaterialDialog.Builder(FollowListMain.this)
                        .title(R.string.tweeting)
                        .input("type here", null, false, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                sendTweet(input.toString());

                            }
                        }).show();
            }
        });
    }

    private void setNavView() {
        View header = navigationView.getHeaderView(0);
        TextView navName = (TextView)header.findViewById(R.id.nav_header_name);
        String name = "@"+userName;
        navName.setText(name);
        ImageView navIcon = (ImageView)header.findViewById(R.id.nav_header_icon);
        Picasso.with(this).load(sharedPreferences.getString(ICON_LIST[accountNumber],"")).into(navIcon);
        TextView navSignOut = (TextView)header.findViewById(R.id.nav_sign_out);
        navSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder adb = new AlertDialog.Builder(FollowListMain.this);
                adb.setTitle(R.string.ask_sign_out);
                adb.setPositiveButton(R.string.sign_out, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (numberOfAccounts==1){
                            sharedPreferences.edit().clear().apply();
                            dataBase.deleteDataBase();
                            Log.d(TAG, "FlistM setnav: #account =1");
                        }else {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.remove(TOKEN_LIST[accountNumber]);
                            editor.remove(SECRET_LIST[accountNumber]);
                            editor.remove(USER_NAME_LIST[accountNumber]);
                            editor.remove(IS_LOGIN_LIST[accountNumber]);
                            editor.remove(ICON_LIST[accountNumber]);
                            editor.putInt(LogInActivity.PREF_ACCOUNT_COUNT,numberOfAccounts-1);
                            editor.putBoolean(FollowListMainFragment.FIRST_TIME,true);
                            editor.apply();
                            int nextAcc = 0;
                            for (String isLoggedIn:IS_LOGIN_LIST){
                                if (sharedPreferences.getBoolean(isLoggedIn,false)){
                                    Log.d(TAG, "fListMain setnav login with: "+nextAcc);
                                    editor.putInt(LogInActivity.PREF_CURRENT_ACCOUNT,nextAcc);
                                    editor.apply();
                                    break;
                                }
                                nextAcc++;
                            }
                            Log.d(TAG, "fListMain setnav acc#: "+sharedPreferences.getInt(LogInActivity.PREF_CURRENT_ACCOUNT,10));
                            dataBase.deleteTableData(DB_TABLE_LIST[accountNumber]);
                        }
                        startActivity(new Intent(FollowListMain.this,FollowListMain.class));
                    }
                });
                adb.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                adb.show();
            }
        });

        Menu menu = navigationView.getMenu();
        if (sharedPreferences.getBoolean(LogInActivity.PREF_KEY_TWITTER_LOGIN,false)){
            menu.add(R.id.account_group,firstAccountId,10,sharedPreferences.getString(LogInActivity.PREF_USER_NAME,"me"));
        }
        if (sharedPreferences.getBoolean(LogInActivity.PREF_KEY_TWITTER_LOGIN2,false)){
            menu.add(R.id.account_group,secondAccountId,11,sharedPreferences.getString(LogInActivity.PREF_USER_NAME2,"me"));
        }
        if (sharedPreferences.getBoolean(LogInActivity.PREF_KEY_TWITTER_LOGIN3,false)){
            menu.add(R.id.account_group,thirdAccountId,12,sharedPreferences.getString(LogInActivity.PREF_USER_NAME3,"me"));
        }
        if (sharedPreferences.getBoolean(LogInActivity.PREF_KEY_TWITTER_LOGIN4,false)){
            menu.add(R.id.account_group,forthAccountId,13,sharedPreferences.getString(LogInActivity.PREF_USER_NAME4,"me"));
        }
//        switch (numberOfAccounts){
//            case 4:menu.add(R.id.account_group,forthAccountId,13,sharedPreferences.getString(PREF_USER_NAME4,"me"));
//            case 3:menu.add(R.id.account_group,thirdAccountId,12,sharedPreferences.getString(PREF_USER_NAME3,"me"));
//            case 2:menu.add(R.id.account_group,secondAccountId,11,sharedPreferences.getString(PREF_USER_NAME2,"me"));
//            case 1:menu.add(R.id.account_group,firstAccountId,10,sharedPreferences.getString(PREF_USER_NAME,"me"));
//                break;
//            default:break;
//        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {}

    public void sendTweet(final String text){
        Log.d("tag", "sendTweet " + text);
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    myTwitter.updateStatus(text);
                } catch (TwitterException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_follow_list_main,menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()){
//            case R.id.log_out:
//                sharedPreferences.edit().clear().apply();
//                this.deleteDatabase("tweets_data_base");
//                startActivity(new Intent(this,FollowListMain.class));
//                break;
//            case R.id.test_menu:
//                dataBase.deleteTableData(TweetDataBase.FOLLOWING_TB);
//            default:
//                break;
//        }
//        return super.onOptionsItemSelected(item);
//    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        Log.d(TAG, "FlistM onNavigationItemSelected id: "+id+" , "+accountNumber);
        if (id == R.id.nav_add_account) {
            if (numberOfAccounts>3){
                Toast.makeText(this,"Only up to 4 accounts can be added",Toast.LENGTH_SHORT).show();
            }else {
                startActivity(new Intent(this,LogInActivity.class));
            }
        } else if (id == firstAccountId) {
            if (accountNumber!=0){
                Log.d(TAG, "FlistM onNavigationItemSelected: first");
                sharedPreferences.edit().putInt(LogInActivity.PREF_CURRENT_ACCOUNT,0).apply();
                startActivity(new Intent(this,FollowListMain.class));
            }
        } else if (id == secondAccountId) {
            if (accountNumber!=1){
                Log.d(TAG, "FlistM onNavigationItemSelected: second");
                sharedPreferences.edit().putInt(LogInActivity.PREF_CURRENT_ACCOUNT,1).apply();
                startActivity(new Intent(this,FollowListMain.class));
            }
        }else if (id == thirdAccountId) {
            if (accountNumber!=2){
                sharedPreferences.edit().putInt(LogInActivity.PREF_CURRENT_ACCOUNT,2).apply();
                startActivity(new Intent(this,FollowListMain.class));
            }
        }else if (id == forthAccountId) {
            if (accountNumber!=3){
                sharedPreferences.edit().putInt(LogInActivity.PREF_CURRENT_ACCOUNT,3).apply();
                startActivity(new Intent(this,FollowListMain.class));
            }
        }else if (id == R.id.nav_setting){
            startActivity(new Intent(this, SettingsActivity.class));
        }else if (id == R.id.nav_zero){
            FollowListMainFragment mainFragment = (FollowListMainFragment)getSupportFragmentManager().findFragmentByTag(makeFragmentName(R.id.follow_view_pager,0));
            mainFragment.resetAllUserTweetCount();
        }else if (id==R.id.nav_log_out_all){
            new AlertDialog.Builder(this)
                    .setTitle("Sign out of all accounts?")
                    .setPositiveButton(R.string.sign_out, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            sharedPreferences.edit().clear().apply();
                            deleteDatabase("tweets_data_base");
                            startActivity(new Intent(FollowListMain.this,FollowListMain.class));
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private static String makeFragmentName(int viewPagerId, int index) {
        return "android:switcher:" + viewPagerId + ":" + index;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu,menu);
        //SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        if (searchView==null){
            Log.d(TAG, "FLMain onCreateOptionsMenu: search view is null"); return false;
        }
        EditText searchPlate = (EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        searchPlate.setHint("Search");
        View searchPlateView = searchView.findViewById(android.support.v7.appcompat.R.id.search_plate);
        searchPlateView.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));

        searchView.setSuggestionsAdapter(new SimpleCursorAdapter(
                this, R.layout.search_suggestion_item, null,
                new String[] { SearchManager.SUGGEST_COLUMN_TEXT_1 },
                new int[] { R.id.search_suggest }));
        // use this method for search process
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //Toast.makeText(FollowListMain.this, query, Toast.LENGTH_SHORT).show();
                Intent i = new Intent(FollowListMain.this, SearchActivity.class);
                i.putExtra(SearchActivity.SEARCH_WORD,query);
                startActivity(i);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // use this method for auto complete search process
//                if (newText.length()>2){
//                    setSuggestions(newText);
//                }else {
//                    searchView.getSuggestionsAdapter().changeCursor(null);
//                }
                return false;
            }
        });
//        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
//            @Override
//            public boolean onSuggestionSelect(int position) {
//                Cursor cursor = (Cursor) searchView.getSuggestionsAdapter().getItem(position);
//                String choice = cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1));
//                cursor.close();
//                return true;
//            }
//
//            @Override
//            public boolean onSuggestionClick(int position) {
//                return false;
//            }
//        });
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        return true;
    }
    static final String[] sAutocompleteColNames = new String[] {
            BaseColumns._ID,                         // necessary for adapter
            SearchManager.SUGGEST_COLUMN_TEXT_1      // the full search term
    };
    private void setSuggestions(final String newText){

        new AsyncTask<Void, Void, Cursor>() {
            @Override
            protected Cursor doInBackground(Void... params) {
                MatrixCursor cursor = new MatrixCursor(sAutocompleteColNames);

                try {
                    int count = 0;
                    ResponseList<User> sugUsers = myTwitter.searchUsers(newText,1);
                    for (User sugUser:sugUsers){
                        Object[] obj = new Object[]{count++,sugUser.getScreenName()};
                        cursor.addRow(obj);
                    }
                } catch (TwitterException e) {
                    e.printStackTrace();
                    cancel(true);
                }

                return cursor;
            }

            @Override
            protected void onPostExecute(Cursor cursor) {
                searchView.getSuggestionsAdapter().changeCursor(cursor);
                super.onPostExecute(cursor);
            }
        }.execute();
    }

}
