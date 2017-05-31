package com.cs122b.gamedbapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import XMLParser.SearchXMLParser;
import constants.constants;

public class SearchActivity extends AppCompatActivity {

    public final static String SEARCH = "SearchResults";
    public final static String LIMIT = "LIMIT";
    public static final String SEARCH_COUNT = "SearchCount";
    public static final String SEARCH_OFFSET = "SearchOffset";
    public static final String SEARCH_QUERY = "SearchQuery";

    private TextView errorView;
    private EditText searchText;

    private SharedPreferences mPrefs;

    //search query
    private String name;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        errorView = (TextView) this.findViewById(R.id.searchErrorView);
        errorView.setText("");

        searchText = (EditText) this.findViewById(R.id.searchText);

        mPrefs = this.getSharedPreferences(getString(R.string.search_preference_file_key),Context.MODE_PRIVATE);

        //if a search query was typed in previously, get that value when creating this new process
        String searchQuery = mPrefs.getString(getString(R.string.search_preference_file_key),"");
        searchText.setText(searchQuery);
    }

    @Override
    public void onBackPressed()
    {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(getString(R.string.search_preference_file_key),searchText.getText().toString());
        editor.commit();

        super.onBackPressed();
    }


    public void onSearchClick(View view)
    {
        //used to return a specific xml format
        Integer match = 3;
        //game to search for Note: %2B is the plus sign (+) value read in html
        //name = searchText.getText().toString().replaceAll("\\s","%2B");
        //Note: this ensures that whitespaces entered into the query aren't considered as valid searches.
        //name = name.trim();
        try {
            name = URLEncoder.encode(searchText.getText().toString(), "UTF-8");
        } catch(UnsupportedEncodingException e)
        {
            Log.d("encodingexception",e.getMessage());
        }
        name = name.trim();

        //MYSQL offset used for pagination
        Integer offset = 0;
        //search results limit per page
        final Integer limit = 25;

        Integer length = name.length();
        Log.d("NAMELEN",length.toString());

        String base_url = "http://" + constants.IP + ":" + constants.HTTP_PORT;
        String path = "/search/xquery";
        String full_url = base_url + path + "?match=" + match + "&name=" + name + "&offset=" + offset + "&limit=" + limit;

        final Context context = this;
        RequestQueue queue = Volley.newRequestQueue(context);

        Log.d("INFO",full_url);

        if(name.isEmpty())
        {
            errorView.setText("Please type in a game title to search for");
        }

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
                                Integer searchCount =  Integer.parseInt(searchParser.getSearchCount(response));
                                Integer searchOffset = Integer.parseInt(searchParser.getSearchOffset(response));

                                //DEBUG
                                for(Integer i = 0;i < contents.size();++i)
                                {
                                    Log.d("RESPONSE" + i,contents.get(i).toString());
                                }

                                Log.d("SEARCH_COUNT", searchCount.toString());
                                Log.d("LIMIT", limit.toString());

                                if(contents.isEmpty())
                                {
                                    Log.d("RESPONSEE", "contents is empty");
                                }

                                intent.putParcelableArrayListExtra(SEARCH, (ArrayList<? extends Parcelable>) contents);
                                intent.putExtra(LIMIT, limit);
                                intent.putExtra(SEARCH_COUNT,searchCount);
                                intent.putExtra(SEARCH_OFFSET,searchOffset);
                                intent.putExtra(SEARCH_QUERY,name);
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
