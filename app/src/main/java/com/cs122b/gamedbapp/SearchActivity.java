package com.cs122b.gamedbapp;

import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import XMLParser.SearchXMLParser;
import constants.constants;

public class SearchActivity extends AppCompatActivity {

    public final static String SEARCH = "SearchResults";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
    }

    public void onSearchClick(View view)
    {
        final EditText searchText = (EditText) this.findViewById(R.id.searchText);

        //used to return a specific xml format
        Integer match = 1;
        //game to search for
        String name = searchText.getText().toString();
        //MYSQL offset used for pagination
        Integer offset = 0;
        //search results limit per page
        Integer limit = 50;


        name = name.replaceAll("\\s","+");
        String base_url = "http://" + constants.IP + ":" + constants.HTTP_PORT;
        String path = "/search/xquery";
        String full_url = base_url + path + "?match=" + match + "&name=" + name + "&offset=" + offset + "&limit=" + limit;

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
                             List contents = searchParser.parse(response);

                            for(Integer i = 0;i < contents.size();++i)
                            {
                                Log.d("RESPONSE" + i,contents.get(i).toString());
                            }

                            if(contents.isEmpty())
                            {
                                Log.d("RESPONSEE", "contents is empty");
                            }

                            //TODO(HARVEY): store List in a bundle which searchable_activity can access later.
                            //might cause a null reference exception and crash the app....
                            intent.putParcelableArrayListExtra(SEARCH, (ArrayList<? extends Parcelable>) contents);
                            startActivity(intent);
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
