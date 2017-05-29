package com.cs122b.gamedbapp;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class SearchableActivity extends ListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchable);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        //ArrayList<? extends Parcelable> searchResults = bundle.getParcelableArrayList(SearchActivity.SEARCH);

        //display search results
        //Integer size = searchResults.size();
        //Toast.makeText(this,"Search Results Size: " + size, Toast.LENGTH_LONG).show();
    }
}
