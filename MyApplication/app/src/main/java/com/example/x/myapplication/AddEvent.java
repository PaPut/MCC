package com.example.x.myapplication;

import java.io.IOException;
import java.io.InputStream;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AddEvent {

    String name;
    String start;
    String end;
    String description;

    /*public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addevent);


        findViewById(R.id.submitEvent).setOnClickListener(this);
    }

    @Override


    public void onClick(View arg0) {
        Button b = (Button)findViewById(R.id.submitEvent);


        b.setClickable(true);
        new ().execute();
    }*/


    HttpClient httpClient = new DefaultHttpClient();
    HttpContext localContext = new BasicHttpContext();
    HttpPost httpPost = new HttpPost("http://10.0.2.2:3000/events");

    /*try {
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("name", name);
            jsonObject.put("start", start);
            jsonObject.put("end", end);
            jsonObject.put("description", description);
        }
        catch (JSONException e) {

        }


    }*/
}
