package com.example.x.myapplication;

        import org.apache.http.HttpEntity;
        import org.apache.http.HttpResponse;
        import org.apache.http.client.HttpClient;
        import org.apache.http.client.methods.HttpGet;
        import org.apache.http.client.methods.HttpPost;
        import org.apache.http.entity.StringEntity;
        import org.apache.http.impl.client.DefaultHttpClient;
        import org.apache.http.protocol.BasicHttpContext;
        import org.apache.http.protocol.HttpContext;
        import android.app.Activity;
        import android.app.DatePickerDialog;
        import android.app.TimePickerDialog;
        import android.content.Intent;
        import android.content.ContentResolver;
        import android.content.ContentUris;
        import android.content.ContentValues;
        import android.database.Cursor;
        import android.net.Uri;
        import android.os.AsyncTask;
        import android.os.Bundle;
        import android.provider.CalendarContract;
        import android.util.Log;
        import android.view.View;
        import android.view.View.OnClickListener;
        import android.widget.AdapterView;
        import android.widget.ArrayAdapter;
        import android.widget.Button;
        import android.widget.DatePicker;
        import android.widget.EditText;
        import android.widget.ListView;
        import android.widget.Spinner;
        import android.widget.TextView;
        import android.widget.TimePicker;

        import org.apache.http.util.EntityUtils;
        import org.json.JSONArray;
        import org.json.JSONObject;

        import java.lang.reflect.Array;
        import java.text.DateFormat;
        import java.text.SimpleDateFormat;
        import java.util.ArrayList;
        import java.util.Calendar;
        import java.util.Collections;
        import java.util.Date;
        import java.util.List;
        import java.util.Locale;
        import java.util.TimeZone;

public class MainActivity extends Activity implements OnClickListener {

    List<String> eventIds = new ArrayList<String>(); // List of event IDs
    List<String> googIds = new ArrayList<String>();  // List of events' googIds
    String eventId;
    String googId;
    Calendar myCalendar = Calendar.getInstance();
    EditText sort;
    Spinner spinner;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.my_button).setOnClickListener(this);
        /*FOR CALENDAR testing
        ContentResolver cr = getContentResolver();
        ContentValues values = new ContentValues();
        Cursor cur = null;
        Uri uri = CalendarContract.Calendars.CONTENT_URI;
        cr = getContentResolver();
        uri = CalendarContract.Events.CONTENT_URI;
        cur = cr.query(uri, new String[]{CalendarContract.Calendars._ID}, null, null, null);

        while (cur.moveToNext()) {
            System.out.println(cur.getString(0));
        }
*/      Button monthBut = (Button)findViewById(R.id.monthButton);
        monthBut.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(spinner.getSelectedItem().toString().equals("month")){
                    new LongRunningGetIO("month?month="+sort.getText().toString()).execute();
                    System.out.println("month?month="+sort.getText().toString());

                }
                if(spinner.getSelectedItem().toString().equals("day")){
                    new LongRunningGetIO("day?day="+sort.getText().toString()).execute();
                    System.out.println("day?day="+sort.getText().toString());

                }
                if(spinner.getSelectedItem().toString().equals("all")){
                    new LongRunningGetIO("events").execute();

                }
            }


        });

        Button addEvent = (Button)findViewById(R.id.addEventButton);
        sort = (EditText)findViewById(R.id.editSort);

        // When the addEvent button is clicked it takes us to activity_addevent
        addEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddEvent.class);
                startActivity(intent);
            }
        });

        // When the sort EditText is clicked it shows the previously defined Time and Datepicker dialogs
        sort.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                new DatePickerDialog(MainActivity.this, startDate, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        spinner = (Spinner) findViewById(R.id.spinner1);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.options, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);

        // ListView for events
        ListView list = (ListView)findViewById(R.id.eventList);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            // When an item is clicked, it takes you to the event's page (activity_single_event)
            public void onItemClick(AdapterView<?> parent, View viewClicked, int position, long id) {
                TextView textView = (TextView) viewClicked;
                eventId = eventIds.get(position);
                googId = googIds.get(position);

                Intent intent = new Intent(MainActivity.this, ViewEvent.class);

                // This allows these variables to be available in the ViewEvent class
                intent.putExtra("id", eventId);
                intent.putExtra("googId", googId);

                String[] data = textView.getText().toString().split("\n");
                intent.putExtra("data", data);


                startActivity(intent);
            }
        });



        new LongRunningGetIO("events").execute();
    }

    // DatePicker dialog for the sort date (month, day and year)
    DatePickerDialog.OnDateSetListener startDate = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel(sort);
        }

    };

    private void updateLabel(EditText e) {

        String dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);

        e.setText(sdf.format(myCalendar.getTime()));
    }

    // Adding the new events to Android Calendar
    public void addEvent(String title, long start, long end, String description){
        Boolean contains = false;

        //Create Cursor
        ContentResolver cr = getContentResolver();
        ContentValues values = new ContentValues();
        Cursor cur = null;
        Uri uri;
        long calID = 2;
        TimeZone tz = TimeZone.getDefault();
        cr = getContentResolver();
        uri = CalendarContract.Events.CONTENT_URI;
        cur = cr.query(uri, new String[]{CalendarContract.Events.TITLE, CalendarContract.Events.DESCRIPTION, CalendarContract.Events.CALENDAR_ID,CalendarContract.Events.DTEND}, null, null, null);

        //Looping through fields in Android db to check if exist
        while (cur.moveToNext()) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(cur.getLong(3));
            if(cur.getString(0).equals(title) && Math.abs(cur.getLong(3)-end)<100000){
                contains = true;
            }
        }

        if(!contains) {
            System.out.println("Lisättiin");
            values.put(CalendarContract.Events.DTSTART, start);
            values.put(CalendarContract.Events.DTEND, end);
            values.put(CalendarContract.Events.TITLE, title);
            values.put(CalendarContract.Events.DESCRIPTION, description);
            values.put(CalendarContract.Events.CALENDAR_ID, calID);
            values.put(CalendarContract.Events.EVENT_TIMEZONE, tz.getID());
            // long eventID = Long.parseLong(uri.getLastPathSegment());
            cr.insert(uri, values);
        }
    cur.close();

    }


    //Get calendar of Phone and add the missing to Cloud

    public void getEventsToAdd(ArrayList<String> descriptions, ArrayList<String> titles, ArrayList<Long> endmillis) {

        boolean add = true;
        ContentResolver cr = getContentResolver();
        ContentValues values = new ContentValues();
        Cursor cur = null;
        Uri uri = CalendarContract.Events.CONTENT_URI;

        cur = cr.query(uri, new String[]{CalendarContract.Events.TITLE, CalendarContract.Events.DESCRIPTION, CalendarContract.Events.DTSTART,CalendarContract.Events.DTEND}, null, null, null);

        while (cur.moveToNext()) {
            add = true;

            for(int i = 0; i< descriptions.size();i++){

                if((titles.get(i).equals(cur.getString(0))) || cur.getLong(3) < 100000){
                    add = false;
                }
            }
            if(add){


            Calendar cal1 = Calendar.getInstance();
            cal1.setTimeInMillis(cur.getLong(3));
            Calendar cal2 = Calendar.getInstance();
            cal2.setTimeInMillis(cur.getLong(2));
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault());
            String start = df.format(cal2.getTime());
            String end = df.format(cal1.getTime());

            //To clean some Android default events for better usability

                if(!(Integer.toString(cal2.get(Calendar.YEAR)).contains("1970")) && !(Integer.toString(cal1.get(Calendar.YEAR)).contains("1970"))  && cal2.get(Calendar.YEAR)<2016) {
                    System.out.println(cal2.get(Calendar.YEAR));
                    new LongRunningGetIO("events").post(cur.getString(0),start, end, cur.getString(1));

            }
        }
        }
        cur.close();
    }

    //The changes doesn't happen always instantly, f.ex. after restart program can be seen


    //At the moment deletes by title, not used at the moment.
    public void deleteItem(String query){

        String[] EVENT_PROJECTION = new String[] {
                CalendarContract.Events._ID,
                CalendarContract.Events.TITLE,
                CalendarContract.Events.DTSTART,
                CalendarContract.Events.DTEND,
                CalendarContract.Calendars._ID
        };

        ContentResolver cr = getContentResolver();
        Uri uri = CalendarContract.Events.CONTENT_URI;
        Cursor cur = cr.query(uri, EVENT_PROJECTION,null, null, null);

        while (cur.moveToNext()) {
            if(cur.getString(1).contains(query)){
                System.out.println("Del"+cur.getString(1)+cur.getString(4));
                Uri deleteUri = ContentUris.withAppendedId(uri,cur.getLong(0));
                System.out.println(getContentResolver().delete(deleteUri, null, null));
            }
        }
        cur.close();

    }
    @Override


    public void onClick(View arg0) {
        Button b = (Button)findViewById(R.id.my_button);
        b.setClickable(true);
        new LongRunningGetIO("events").execute();
    }

    private class LongRunningGetIO extends AsyncTask <Void, Void, List<String>> {
        String events;

        public LongRunningGetIO(String events) {
            this.events = events;
        }

        //Parsing from json to millis
        protected long parseToMillis(String json){
            String[] parsed = json.replace("+"," ").split(" |-|T|:");

            Calendar beginTime = Calendar.getInstance();
            beginTime.set(Integer.parseInt(parsed[0]), Integer.parseInt(parsed[1]), Integer.parseInt(parsed[2]), Integer.parseInt(parsed[3]), Integer.parseInt(parsed[4]));
            return beginTime.getTimeInMillis();


        }
        @Override

        // GETs the events and adds them to the ListView
        protected List<String> doInBackground(Void... params) {
            HttpClient httpClient = new DefaultHttpClient();
            HttpContext localContext = new BasicHttpContext();
            HttpGet httpGet = new HttpGet("http://10.0.2.2:3000/"+events);
            List<String> events = new ArrayList<String>();
            ArrayList<String> titles = new ArrayList<String>();
            ArrayList<String> descriptions = new ArrayList<String>();
            ArrayList<Long> ends = new ArrayList<Long>();
            try {
                HttpResponse response = httpClient.execute(httpGet, localContext);


                HttpEntity entity = response.getEntity();

                if (entity != null) {
                    String retSrc = EntityUtils.toString(entity);

                    JSONArray jsonArray = new JSONArray(retSrc);

                    for(int i=0; i < jsonArray.length(); i++){
                        String text = "";
                        JSONObject jsonObject = jsonArray.getJSONObject(i);

                        String id = jsonObject.optString("_id").toString();
                        String id2 = jsonObject.optString("googId").toString();
                        String name = jsonObject.optString("name").toString();
                        titles.add(name);
                        String start = jsonObject.optString("start").toString();
                        String end = jsonObject.optString("end").toString();
                        ends.add(parseToMillis(end));
                        String description = jsonObject.optString("description").toString();
                        //FOR event adding
                        if (description == null) {
                            description = "";
                        }
                        descriptions.add(description);
                        addEvent(name, parseToMillis(start), parseToMillis(end), description);
                        eventIds.add(id);
                        googIds.add(id2);
                        text +="Start Date: "+ start +" \nEnd Date: "+ end +"\n"+name+" \nDescription: "+ description +" \n\n";
                        events.add(text);
                    }
                    Collections.sort(events);
                    getEventsToAdd(descriptions, titles, ends);

                }
            } catch (Exception e) {
                System.out.println(e);
            }

            return events;
        }

        protected void post(String name, String start, String end, String description){
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost("http://10.0.2.2:3000/events");

            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("name", name);
                jsonObject.put("start", start);
                jsonObject.put("end", end);
                jsonObject.put("description", description);

                if (jsonObject != null) {
                    httpPost.setHeader("Accept", "application/json");
                    httpPost.setHeader("Content-type", "application/json");

                    httpPost.setEntity(new StringEntity(jsonObject.toString()));
                    HttpResponse response = httpClient.execute(httpPost);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        protected void onPostExecute(List<String> results) {
            ListView list = (ListView)findViewById(R.id.eventList);
            list.setAdapter(null);
            if (results!=null) {
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, R.layout.textview_events, results);
                list.setAdapter(adapter);
            }
            Button b = (Button)findViewById(R.id.my_button);
            b.setClickable(true);
            System.out.println("OnPostExcecute");
        }


    }

}