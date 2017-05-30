package com.cs122b.gamedbapp;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import XMLParser.SearchXMLParser;
import constants.constants;

import static com.cs122b.gamedbapp.SearchActivity.LIMIT;
import static com.cs122b.gamedbapp.SearchActivity.SEARCH;
import static com.cs122b.gamedbapp.SearchActivity.SEARCH_COUNT;
import static com.cs122b.gamedbapp.SearchActivity.SEARCH_OFFSET;
import static com.cs122b.gamedbapp.SearchActivity.SEARCH_QUERY;


//TODO(HARVEY): if size < searchCount, query the server for another batch of games to be listed and store in an array list.
public class SearchableActivity extends ListActivity {

    //size is bounded by limit e.g. size can never be bigger than limit where limit = 50.
    private Integer size;
    private Integer pages;
    private String[] masterList;
    private Integer pageIndex;
    private ListView listView;

    private Button nextButton;
    private Button prevButton;


    //is known as the limit
    private Integer resultsPerPage;
    private Integer searchCount;
    private Integer searchOffset;
    //search query
    private String name;
    private Integer remainingForPrevious;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchable);


        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        resultsPerPage = bundle.getInt(LIMIT);
        searchCount = bundle.getInt(SEARCH_COUNT);
        searchOffset = bundle.getInt(SEARCH_OFFSET);
        name = bundle.getString(SEARCH_QUERY);

        ArrayList<? extends Parcelable> searchResults = bundle.getParcelableArrayList(SEARCH);
        nextButton = (Button) this.findViewById(R.id.nextButton);
        prevButton = (Button) this.findViewById(R.id.prevButton);
        TextView textView3 = (TextView)this.findViewById(R.id.textView3);
        TextView pageView = (TextView)this.findViewById(R.id.pageView);

        //note will return 0 if mapping not found, which is what we want when we first go to the search results page.
        pageIndex = bundle.getInt("PAGE");
        //used for going back a page. Note: 0 if the transition happened from search view to search results page, otherwise it should be a non-zero number if it was from search results page to search results page.
        remainingForPrevious = bundle.getInt("REMAINING");
        size = searchResults.size();
        pages =  (int) Math.ceil((double)searchCount / resultsPerPage);
        masterList = searchResults.toArray(new String[size]);

        Toast.makeText(this,"Total Search Results: " + searchCount, Toast.LENGTH_LONG).show();

        listView = getListView();
        if(!searchResults.isEmpty())
        {
            pageView.setText(pageIndex.toString());

            //display search results
            listView.setVisibility(View.VISIBLE);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(listView.getContext(), android.R.layout.simple_list_item_1, masterList);
            listView.setAdapter(adapter);
        }
        else
        {
            pageView.setText("");
            listView.setVisibility(View.INVISIBLE);
            textView3.setText("NO SEARCH RESULTS");
        }

        //if there are too few results hide and disable next and previous buttons
        if(pages == 0 || pages == 1)
        {
            pageView.setText("");
            nextButton.setVisibility(View.INVISIBLE);
            prevButton.setVisibility(View.INVISIBLE);
            nextButton.setEnabled(false);
            prevButton.setEnabled(false);
        }

    }


    //I could also have onNextPageClick and onPreviousPageClick go to the same intent e.g. this intent and display the array that way?
    public void onNextPageClick(View view)
    {
        //start a Volley Connection
        Integer resultsRemaining = searchCount - searchOffset;
        final Integer resultsCount = resultsRemaining < resultsPerPage ? resultsRemaining : resultsPerPage;

        if(searchOffset + resultsCount < searchCount)
        {
            searchOffset = searchOffset + resultsCount;
            Integer match = 3;
            String base_url = "http://" + constants.IP + ":" + constants.HTTP_PORT;
            String path = "/search/xquery";
            String full_url = base_url + path + "?match=" + match + "&name=" + name + "&offset=" + searchOffset + "&limit=" + resultsPerPage;

            final Context context = this;
            RequestQueue queue = Volley.newRequestQueue(context);

            Log.d("INFO",full_url);

            StringRequest postRequest = new StringRequest(Request.Method.GET, full_url,
                    new Response.Listener<String>()
                    {
                        @Override
                        public void onResponse(String response)
                        {
                            Log.d("RESPONSE",response);
                            SearchXMLParser searchParser = new SearchXMLParser();
                            Intent intent = new Intent(context,SearchableActivity.class);

                            try
                            {
                                //if there was something typed into the search bar perform a search.
                                if(!name.isEmpty())
                                {
                                    List contents = searchParser.parse(response);

                                    //DEBUG
                                    for(Integer i = 0;i < contents.size();++i)
                                    {
                                        Log.d("RESPONSE" + i,contents.get(i).toString());
                                    }

                                    Log.d("SEARCH_COUNT", searchCount.toString());
                                    Log.d("LIMIT", resultsPerPage.toString());

                                    if(contents.isEmpty())
                                    {
                                        Log.d("RESPONSEE", "contents is empty");
                                    }

                                    intent.putParcelableArrayListExtra(SEARCH, (ArrayList<? extends Parcelable>) contents);
                                    intent.putExtra(LIMIT, resultsPerPage);
                                    intent.putExtra(SEARCH_COUNT,searchCount);
                                    intent.putExtra(SEARCH_OFFSET,searchOffset);
                                    intent.putExtra(SEARCH_QUERY,name);
                                    intent.putExtra("REMAINING",resultsCount);
                                    intent.putExtra("PAGE",++pageIndex);
                                    startActivity(intent);
                                }

                            }
                            catch(XmlPullParserException e)
                            {
                                Log.d("XMLEXCEPTION:", e.getMessage());
                            }
                            catch(IOException e)
                            {
                                Log.d("IOEXCEPTION:", e.getMessage());
                            }
                        }
                    },
                    new Response.ErrorListener()
                    {
                        //used typically if web server is down.
                        @Override
                        public void onErrorResponse(VolleyError error)
                        {
                            Log.d("SECURITY.ERROR",error.toString());
                        }
                    }
            );

            queue.add(postRequest);
        }

    }

    public void onPrevPageClick(View view)
    {
        if(searchOffset - remainingForPrevious >= 0)
        {
            searchOffset = searchOffset - remainingForPrevious;
            Integer match = 3;
            String base_url = "http://" + constants.IP + ":" + constants.HTTP_PORT;
            String path = "/search/xquery";
            String full_url = base_url + path + "?match=" + match + "&name=" + name + "&offset=" + searchOffset + "&limit=" + resultsPerPage;

            final Context context = this;
            RequestQueue queue = Volley.newRequestQueue(context);

            Log.d("INFO",full_url);

            StringRequest postRequest = new StringRequest(Request.Method.GET, full_url,
                    new Response.Listener<String>()
                    {
                        @Override
                        public void onResponse(String response)
                        {
                            Log.d("RESPONSE",response);
                            SearchXMLParser searchParser = new SearchXMLParser();
                            Intent intent = new Intent(context,SearchableActivity.class);

                            try
                            {
                                //if there was something typed into the search bar perform a search.
                                if(!name.isEmpty())
                                {
                                    List contents = searchParser.parse(response);

                                    Integer resultsRemaining = searchCount - searchOffset;
                                    final Integer resultsCount = resultsRemaining < resultsPerPage ? resultsRemaining : resultsPerPage;

                                    //DEBUG
                                    for(Integer i = 0;i < contents.size();++i)
                                    {
                                        Log.d("RESPONSE" + i,contents.get(i).toString());
                                    }

                                    Log.d("SEARCH_COUNT", searchCount.toString());
                                    Log.d("LIMIT", resultsPerPage.toString());

                                    if(contents.isEmpty())
                                    {
                                        Log.d("RESPONSEE", "contents is empty");
                                    }

                                    intent.putParcelableArrayListExtra(SEARCH, (ArrayList<? extends Parcelable>) contents);
                                    intent.putExtra(LIMIT, resultsPerPage);
                                    intent.putExtra(SEARCH_COUNT,searchCount);
                                    intent.putExtra(SEARCH_OFFSET,searchOffset);
                                    intent.putExtra(SEARCH_QUERY,name);
                                    intent.putExtra("REMAINING",resultsCount);
                                    intent.putExtra("PAGE",--pageIndex);
                                    startActivity(intent);
                                }

                            }
                            catch(XmlPullParserException e)
                            {
                                Log.d("XMLEXCEPTION:", e.getMessage());
                            }
                            catch(IOException e)
                            {
                                Log.d("IOEXCEPTION:", e.getMessage());
                            }
                        }
                    },
                    new Response.ErrorListener()
                    {
                        //used typically if web server is down.
                        @Override
                        public void onErrorResponse(VolleyError error)
                        {
                            Log.d("SECURITY.ERROR",error.toString());
                        }
                    }
            );

            queue.add(postRequest);
        }

    }


}
