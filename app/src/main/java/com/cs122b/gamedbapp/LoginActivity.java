package com.cs122b.gamedbapp;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.Map;
import java.util.HashMap;

import constants.constants;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    //Used when Login button is tapped
    public void onLoginClick(View view)
    {
        EditText emailText = (EditText) this.findViewById(R.id.email);
        EditText passwordText = (EditText) this.findViewById(R.id.password);

        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();


        String base_url = "http://" + constants.IP + ":" + constants.HTTP_PORT;
        String path = "/servlet/xlogin";
        String full_url = base_url + path + "?email=" + email + "&password=" + password;

        final Context context = this;
        Intent intent = new Intent(context, SuccessfulLoginActivity.class);
        RequestQueue queue = Volley.newRequestQueue(context);

        Log.d("INFO",full_url);

        final Map<String, String> params = new HashMap<String,String>();
        //TODO(HARVEY): Use POST instead of GET later on
        StringRequest postRequest = new StringRequest(Request.Method.GET, full_url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response)
                    {
                        Log.d("RESPONSE",response);
                        TextView textView = (TextView) findViewById(R.id.textView);
                        textView.setText(response.toString());
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        Log.d("SECURITY.ERROR",error.toString());
                    }
                }
                ) {
                    @Override
                    protected Map<String, String> getParams() { return params; }
                };

        queue.add(postRequest);
    }
}
