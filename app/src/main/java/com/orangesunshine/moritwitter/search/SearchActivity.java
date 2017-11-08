package com.orangesunshine.moritwitter.search;

import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.EditText;

import com.orangesunshine.moritwitter.FollowList.FollowListMain;
import com.simleman.moritwitter.R;

import twitter4j.User;

public class SearchActivity extends AppCompatActivity implements SearchPeopleFragment.OnListFragmentInteractionListener{

    public static final String SEARCH_WORD="ajdburolvawjehf";
    public static final String PREF_SEARCH_HISTORY="adljfkbaldflaed";
    private ViewPager viewPager;
    private SearchPagerAdapter pagerAdapter;
    private TabLayout tabLayout;
    static String searchWord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        viewPager = (ViewPager)findViewById(R.id.search_view_pager);
        tabLayout = (TabLayout)findViewById(R.id.search_tab_layout);
        pagerAdapter = new SearchPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        searchWord = getIntent().getExtras().getString(SEARCH_WORD,"");
        if (getSupportActionBar()!=null){
            getSupportActionBar().setTitle(searchWord);
        }

    }

    @Override
    public void onListFragmentInteraction(User user) {
        Log.d("tag", "SearchA, onListFragmentInteraction: "+user.getName());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu,menu);
        //SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        final SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        if (searchView==null){
            Log.d("tag", "FLMain onCreateOptionsMenu: null"); return false;
        }
        EditText searchPlate = (EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        searchPlate.setHint("Search");
        View searchPlateView = searchView.findViewById(android.support.v7.appcompat.R.id.search_plate);
        searchPlateView.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
        // use this method for search process
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //Toast.makeText(FollowListMain.this, query, Toast.LENGTH_SHORT).show();
                Log.d("tag", "SearchA onQuerysub: "+query);
                Intent i = new Intent(SearchActivity.this, SearchActivity.class);
                i.putExtra(SearchActivity.SEARCH_WORD,query);
                startActivity(i);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // use this method for auto complete search process
                return false;
            }
        });
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        return true;
    }
}
