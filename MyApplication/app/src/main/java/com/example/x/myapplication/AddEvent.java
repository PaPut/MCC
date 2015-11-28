package com.example.x.myapplication;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import android.app.ActionBar;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.NameValuePair;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Locale;

public class AddEvent extends ActionBarActivity {

    EditText name1;
    EditText start1;
    EditText end1;
    EditText description1;
    Button submitEvent;
    String name;
    String start;
    String end;
    String description;

    Calendar myCalendar = Calendar.getInstance();

    DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel();
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addevent);

        name1 = (EditText)findViewById(R.id.editTitle);
        start1 = (EditText)findViewById(R.id.editStart);
        end1 = (EditText)findViewById(R.id.editEnd);
        description1 = (EditText)findViewById(R.id.editDescription);
        submitEvent = (Button)findViewById(R.id.submitEvent);

        start1.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(AddEvent.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        submitEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name = name1.getText().toString();
                start = start1.getText().toString();
                end = end1.getText().toString();
                description = description1.getText().toString();
                postData postData = new postData();
                postData.execute();
                Intent intent=new Intent(AddEvent.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private void updateLabel() {

        String myFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.ROOT);

        start1.setText(sdf.format(myCalendar.getTime()));
    }

    class postData extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost("http://10.0.2.2:3000/events");

            try {

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("name", name);
                jsonObject.put("start", start);
                jsonObject.put("end", end);
                jsonObject.put("description", description);

                Log.i("myTag", name + " " + start + " " + end + " " + description);
                Log.i("myTag", jsonObject.toString());

                // List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                // nameValuePairs.add(new BasicNameValuePair("req", jsonObject.toString()));

                httpPost.setHeader("Accept", "application/json");
                httpPost.setHeader("Content-type", "application/json");

                httpPost.setEntity(new StringEntity(jsonObject.toString()));
                HttpResponse response = httpClient.execute(httpPost);


            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

    }




}
