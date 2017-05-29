package com.cs122b.gamedbapp;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Xml;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import constants.constants;
import XMLParser.LoginXMLParser;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    //Used when Login button is tapped
    public void onLoginClick(View view)
    {
        //hide keyboard on login button press
        InputMethodManager inputManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);

        final EditText emailText = (EditText) this.findViewById(R.id.email);
        final EditText passwordText = (EditText) this.findViewById(R.id.password);
        final TextView errorView = (TextView) this.findViewById(R.id.textView);

        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();


        String base_url = "http://" + constants.IP + ":" + constants.HTTP_PORT;
        String path = "/servlet/xlogin";
        String full_url = base_url + path + "?email=" + email + "&password=" + password;

        final Context context = this;
        final Intent intent = new Intent(context, SuccessfulLoginActivity.class);
        RequestQueue queue = Volley.newRequestQueue(context);

        Log.d("INFO",full_url);

        StringRequest postRequest = new StringRequest(Request.Method.POST, full_url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response)
                    {
                        Log.d("RESPONSE",response);
                        LoginXMLParser loginParser = new LoginXMLParser();

                        try
                        {
                            Map contents = loginParser.parse(response);

                            //if login not successful print out error message
                            if(!contents.get("status_code").equals("1"))
                            {
                                String error_msg = contents.get("message").toString();
                                errorView.setText(error_msg);
                                emailText.setText("");
                                passwordText.setText("");
                            }
                            else//otherwise go to success page
                            {
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
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        Log.d("SECURITY.ERROR",error.toString());
                        errorView.setText("ERROR: Website is down");
                    }
                }
                );

        queue.add(postRequest);
    }
}

