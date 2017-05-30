package com.cs122b.gamedbapp;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


//TODO(HARVEY): Create a java object that extends from parcelable which will contain all data required to display on page.
public class SearchableActivity extends ListActivity {

    private Integer resultsPerPage;
    private Integer size;
    private Integer pages;
    private String[] masterList;
    private Integer pageIndex;
    private ListView listView;

    private Button nextButton;
    private Button prevButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchable);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        ArrayList<? extends Parcelable> searchResults = bundle.getParcelableArrayList(SearchActivity.SEARCH);
        nextButton = (Button) this.findViewById(R.id.nextButton);
        prevButton = (Button) this.findViewById(R.id.prevButton);

        pageIndex = 0;
        size = searchResults.size();
        resultsPerPage = 20;
        pages =  (int) Math.ceil((double)size / resultsPerPage);
        masterList = searchResults.toArray(new String[size]);

        //create a list of sub results to show
        String[] list = new String[resultsPerPage];
        for(int i = 0;i < resultsPerPage;++i)
        {
            int offset = (pageIndex * resultsPerPage) + i;
            list[i] = masterList[offset];
        }

        Toast.makeText(this,"Search Results Size: " + size, Toast.LENGTH_LONG).show();

        listView = getListView();
        if(!searchResults.isEmpty())
        {
            //display search results
            listView.setVisibility(View.VISIBLE);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(listView.getContext(), android.R.layout.simple_list_item_1, list);
            listView.setAdapter(adapter);
        }
        else
        {
            listView.setVisibility(View.INVISIBLE);
        }

        //if there are too few results hide and disable next and previous buttons
        if(pages == 0)
        {
            nextButton.setVisibility(View.INVISIBLE);
            prevButton.setVisibility(View.INVISIBLE);
            nextButton.setEnabled(false);
            prevButton.setEnabled(false);
        }

    }

    public void onNextPageClick(View view)
    {
        if(pageIndex + 1 < pages)
        {
            pageIndex++;

            Integer numResults = size - (pageIndex * resultsPerPage);

            Integer resultsSize = numResults < resultsPerPage ?  numResults : resultsPerPage;

            String[] list = new String[resultsSize];

            for (int i = 0; i < resultsSize; ++i)
            {
                int offset = (pageIndex * resultsPerPage) + i;
                list[i] = masterList[offset];
            }

            listView.setVisibility(View.VISIBLE);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(listView.getContext(), android.R.layout.simple_list_item_1, list);
            listView.setAdapter(adapter);
        }
    }

    public void onPrevPageClick(View view)
    {
        if(pageIndex - 1 >= 0)
        {
            pageIndex--;

            String[] list = new String[resultsPerPage];
            for (int i = 0; i < resultsPerPage; ++i)
            {
                int offset = (pageIndex * resultsPerPage) + i;
                list[i] = masterList[offset];
            }

            listView.setVisibility(View.VISIBLE);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(listView.getContext(), android.R.layout.simple_list_item_1, list);
            listView.setAdapter(adapter);
        }

    }


}
