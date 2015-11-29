package com.example.x.myapplication;

        import org.apache.http.HttpEntity;
        import org.apache.http.HttpResponse;
        import org.apache.http.client.HttpClient;
        import org.apache.http.client.methods.HttpGet;
        import org.apache.http.impl.client.DefaultHttpClient;
        import org.apache.http.protocol.BasicHttpContext;
        import org.apache.http.protocol.HttpContext;
        import android.app.Activity;
        import android.app.DatePickerDialog;
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
        import android.widget.TextView;

        import org.apache.http.util.EntityUtils;
        import org.json.JSONArray;
        import org.json.JSONObject;

        import java.lang.reflect.Array;
        import java.util.ArrayList;
        import java.util.Calendar;
        import java.util.List;
        import java.util.TimeZone;

public class MainActivity extends Activity implements OnClickListener {

    List<String> eventIds = new ArrayList<String>(); // List of event IDs
    List<String> googIds = new ArrayList<String>();  // List of events' googIds
    String eventId;
    String googId;
    EditText month; // TODO
    String monthText; // TODO



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.my_button).setOnClickListener(this);

        Button addEvent = (Button)findViewById(R.id.addEventButton);

        // When the addEvent button is clicked it takes us to activity_addevent
        addEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddEvent.class);
                startActivity(intent);
            }
        });

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



        new LongRunningGetIO().execute();
    }



    // Adding the new events
    public void addEvent(String title, long start, long end, String description){
        Boolean contains = false;
        ContentResolver cr = getContentResolver();
        ContentValues values = new ContentValues();
        Cursor cur = null;
        Uri uri = CalendarContract.Events.CONTENT_URI;
        long calID = 1;
        TimeZone tz = TimeZone.getDefault();
        cr = getContentResolver();
        uri = CalendarContract.Events.CONTENT_URI;
        cur = cr.query(uri, new String[]{CalendarContract.Events.TITLE, CalendarContract.Events.DESCRIPTION, CalendarContract.Events.CALENDAR_ID,CalendarContract.Events.DTEND}, null, null, null);

        while (cur.moveToNext()) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(cur.getLong(3));

            if(cur.getString(0).equals(title) && cur.getString(1).equals(description) && Math.abs(cur.getLong(3)-end)<100000){
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

    public void getCalendarAndAdd(ArrayList<String> descriptions, ArrayList<String> titles, ArrayList<Long> endmillis) {

        long calID = 1;
        long startMillis = 0;
        long endMillis = 0;
        boolean add = true;
        ContentResolver cr = getContentResolver();
        ContentValues values = new ContentValues();
        Cursor cur = null;
        Uri uri = CalendarContract.Events.CONTENT_URI;

        cur = cr.query(uri, new String[]{CalendarContract.Events.TITLE, CalendarContract.Events.DESCRIPTION, CalendarContract.Events.DTSTART,CalendarContract.Events.DTEND}, null, null, null);

        while (cur.moveToNext()) {
            add = true;
            System.out.println(cur.getString(0));

            for(int i = 0; i< descriptions.size();i++){
                if(descriptions.get(i).equals(cur.getString(1)) && titles.get(i).equals(cur.getString(0))){
                    System.out.println("____"+descriptions.get(i)+cur.getString(1));
                    add = false;
                }
            }
            if(add){
            System.out.println("pitäisi lisätä");
            Calendar cal1 = Calendar.getInstance();
            cal1.setTimeInMillis(cur.getLong(3));
            Calendar cal2 = Calendar.getInstance();
            cal2.setTimeInMillis(cur.getLong(2));
            //yyyy-MM-dd'T    'HH:mm:ss.SSSZ"
            AddEvent newEVENT = new AddEvent(cur.getString(0),cal2.get(Calendar.YEAR)+"-"+cal2.get(Calendar.MONTH)+"-"+cal2.get(Calendar.DATE)+"'T'"+cal2.get(Calendar.HOUR)+":"+cal2.get(Calendar.MINUTE)+":"+cal2.get(Calendar.SECOND)+".SSSZ",cal1.get(Calendar.YEAR)+"-"+cal1.get(Calendar.MONTH)+"-"+cal1.get(Calendar.DATE)+"'T'"+cal1.get(Calendar.HOUR)+":"+cal1.get(Calendar.MINUTE)+":"+cal1.get(Calendar.SECOND)+".SSSZ", cur.getString(1));
            newEVENT.post();
        }
        }
        cur.close();
    }

    //The changes doesn't happen always instantly, f.ex. after restart program can be seen
    //At the moment deletes by title
    public void deleteItem(String query){

        String[] EVENT_PROJECTION = new String[] {
                CalendarContract.Events._ID,
                CalendarContract.Events.TITLE,
                CalendarContract.Events.DTSTART,
                CalendarContract.Events.DTEND
        };

        ContentResolver cr = getContentResolver();
        Uri uri = CalendarContract.Events.CONTENT_URI;
        Cursor cur = cr.query(uri, EVENT_PROJECTION, null, null, null);

        while (cur.moveToNext()) {
            if(cur.getString(1).contains(query)){
                System.out.println("Del"+cur.getString(1));
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
        new LongRunningGetIO().execute();
    }

    private class LongRunningGetIO extends AsyncTask <Void, Void, List<String>> {

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
            HttpGet httpGet = new HttpGet("http://10.0.2.2:3000/events");
            List<String> events = new ArrayList<String>();
            try {
                HttpResponse response = httpClient.execute(httpGet, localContext);


                HttpEntity entity = response.getEntity();

                if (entity != null) {
                    String retSrc = EntityUtils.toString(entity);

                    JSONArray jsonArray = new JSONArray(retSrc);
                    ArrayList<String> titles = new ArrayList<String>();
                    ArrayList<String> descriptions = new ArrayList<String>();
                    ArrayList<Long> ends = new ArrayList<Long>();

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
                        descriptions.add(description);
                        //FOR event adding
                        addEvent(name, parseToMillis(start), parseToMillis(end), description);
                        if (description == "null") {
                            description = "";
                        }

                        eventIds.add(id);
                        googIds.add(id2);
                        text += name + "\nStart Date: "+ start +" \nEnd Date: "+ end +" \nDescription: "+ description +" \n\n";
                        events.add(text);
                    }
                    getCalendarAndAdd(descriptions,titles,ends);
                }
            } catch (Exception e) {

            }
            return events;
        }


        protected void onPostExecute(List<String> results) {
            if (results!=null) {
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, R.layout.textview_events, results);
                ListView list = (ListView)findViewById(R.id.eventList);
                list.setAdapter(adapter);
            }
            Button b = (Button)findViewById(R.id.my_button);
            b.setClickable(true);
        }


    }

}