package com.example.x.myapplication;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


public class ViewEvent extends ActionBarActivity {

    EditText title;
    EditText start;
    EditText end;
    EditText description;
    Button update;
    Button delete;

    String titleText;
    String startText;
    String endText;
    String descriptionText;

    String eventId;
    String googId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_event);

        title = (EditText)findViewById(R.id.updateTitle);
        start = (EditText)findViewById(R.id.updateStart);
        end = (EditText)findViewById(R.id.updateEnd);
        description = (EditText)findViewById(R.id.updateDescription);

        update = (Button)findViewById(R.id.updateButton);
        delete = (Button)findViewById(R.id.deleteButton);

        Intent intent = getIntent();

        eventId = intent.getStringExtra("id");
        googId = intent.getStringExtra("googId");
        String[] data = intent.getStringArrayExtra("data");
        for(int i = 0; i<data.length; i++){
            System.out.println(data[i].toString());

        }
        title.setText(data[2]);
        start.setText(data[0].split(" ")[2]);
        end.setText(data[1].split(" ")[2]);
        description.setText("");

        try {
            descriptionText = data[3].split(": ")[1];
            description.setText(descriptionText);
        } catch (Exception e) {}

        start.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                new TimePickerDialog(ViewEvent.this, startTime, myCalendar
                        .get(Calendar.HOUR_OF_DAY), myCalendar.get(Calendar.MINUTE), true).show();
                new DatePickerDialog(ViewEvent.this, startDate, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        end.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                new TimePickerDialog(ViewEvent.this, endTime, myCalendar
                        .get(Calendar.HOUR_OF_DAY), myCalendar.get(Calendar.MINUTE), true).show();
                new DatePickerDialog(ViewEvent.this, endDate, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                titleText = title.getText().toString();
                startText = start.getText().toString();
                endText = end.getText().toString();
                descriptionText = description.getText().toString();
                updateData updateData = new updateData();
                updateData.execute();
                Toast.makeText(ViewEvent.this, "Event Updated!", Toast.LENGTH_LONG).show();
                Intent intent=new Intent(ViewEvent.this, MainActivity.class);
                startActivity(intent);
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteData deleteData = new deleteData();
                deleteData.execute();
                Toast.makeText(ViewEvent.this, "Event Deleted!", Toast.LENGTH_LONG).show();
                Intent intent=new Intent(ViewEvent.this, MainActivity.class);
                startActivity(intent);
            }
        });


    }

    // Update function
    class updateData extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            HttpClient httpClient = new DefaultHttpClient();
            HttpPut httpPut = new HttpPut("http://10.0.2.2:3000/events/" + eventId + "/" + googId);

            try {

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("name", titleText);
                jsonObject.put("start", startText);
                jsonObject.put("end", endText);
                jsonObject.put("description", descriptionText);
                jsonObject.put("_id", eventId);
                jsonObject.put("googId", googId);

                if (jsonObject != null) {
                    httpPut.setHeader("Accept", "application/json");
                    httpPut.setHeader("Content-type", "application/json");

                    httpPut.setEntity(new StringEntity(jsonObject.toString()));
                    HttpResponse response = httpClient.execute(httpPut);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

    }

    // DELETE request
    class deleteData extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            HttpClient httpClient = new DefaultHttpClient();
            HttpDelete httpDelete = new HttpDelete("http://10.0.2.2:3000/events/" + eventId + "/" + googId);

            try {
                HttpResponse response = httpClient.execute(httpDelete);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

    }


    // Same as in MainActivity
    Calendar myCalendar = Calendar.getInstance();

    DatePickerDialog.OnDateSetListener startDate = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel(start);
        }

    };

    TimePickerDialog.OnTimeSetListener startTime = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minuteOfHour) {
            myCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            myCalendar.set(Calendar.MINUTE, minuteOfHour);
            updateLabel(start);
        }
    };

    DatePickerDialog.OnDateSetListener endDate = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel(end);
        }
    };

    TimePickerDialog.OnTimeSetListener endTime = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minuteOfHour) {
            myCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            myCalendar.set(Calendar.MINUTE, minuteOfHour);
            updateLabel(end);
        }
    };

    private void updateLabel(EditText e) {

        String dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.ROOT);

        e.setText(sdf.format(myCalendar.getTime()));
    }
}
