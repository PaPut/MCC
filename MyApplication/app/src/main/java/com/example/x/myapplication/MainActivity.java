package com.example.x.myapplication;

        import org.apache.http.HttpEntity;
        import org.apache.http.HttpResponse;
        import org.apache.http.client.HttpClient;
        import org.apache.http.client.methods.HttpGet;
        import org.apache.http.impl.client.DefaultHttpClient;
        import org.apache.http.protocol.BasicHttpContext;
        import org.apache.http.protocol.HttpContext;
        import android.app.Activity;
        import android.content.Intent;
        import android.content.ContentResolver;
        import android.content.ContentUris;
        import android.content.ContentValues;
        import android.database.Cursor;
        import android.net.Uri;
        import android.os.AsyncTask;
        import android.os.Bundle;
        import android.provider.CalendarContract;
        import android.view.View;
        import android.view.View.OnClickListener;
        import android.widget.Button;
        import android.widget.EditText;

        import org.apache.http.util.EntityUtils;
        import org.json.JSONArray;
        import org.json.JSONObject;

        import java.util.Calendar;
        import java.util.TimeZone;

public class MainActivity extends Activity implements OnClickListener {
    @Override


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.my_button).setOnClickListener(this);
        getCalendars();

        Button addEvent = (Button)findViewById(R.id.addEventButton);

        addEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddEvent.class);
                startActivity(intent);
            }
        });
    }



    // Adding the new events
    public void addEvent(String title, long start, long end, String description){

        ContentResolver cr = getContentResolver();
        ContentValues values = new ContentValues();
        Cursor cur = null;
        Uri uri = CalendarContract.Events.CONTENT_URI;

        long calID = 1;
        TimeZone tz = TimeZone.getDefault();
        cr = getContentResolver();
        uri = CalendarContract.Events.CONTENT_URI;
        values.put(CalendarContract.Events.DTSTART, start);
        values.put(CalendarContract.Events.DTEND, end);
        values.put(CalendarContract.Events.TITLE, title);
        values.put(CalendarContract.Events.DESCRIPTION, description);
        values.put(CalendarContract.Events.CALENDAR_ID, calID);
        values.put(CalendarContract.Events.EVENT_TIMEZONE, tz.getID());
        // long eventID = Long.parseLong(uri.getLastPathSegment());
        cr.insert(uri, values);

    }
    //Printout for calendars in android
    public void getCalendars() {

        long calID = 1;
        long startMillis = 0;
        long endMillis = 0;
        ContentResolver cr = getContentResolver();
        ContentValues values = new ContentValues();
        Cursor cur = null;
        Uri uri = CalendarContract.Events.CONTENT_URI;
        Calendar beginTime = Calendar.getInstance();
        beginTime.set(2015, 11, 28, 7, 30);
        startMillis = beginTime.getTimeInMillis();
        Calendar endTime = Calendar.getInstance();
        endTime.set(2015, 11, 28, 8, 45);
        endMillis = endTime.getTimeInMillis();
        cur = cr.query(uri, new String[]{CalendarContract.Events.TITLE, CalendarContract.Events.ACCOUNT_NAME, CalendarContract.Events.CALENDAR_ID,CalendarContract.Events.DTEND}, null, null, null);

        while (cur.moveToNext()) {

            System.out.println(cur.getString(0));
            System.out.println(cur.getString(1));
            System.out.println(cur.getString(2));
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(cur.getLong(3));
            System.out.println(cal.getTime());

        }
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
                System.out.println("Del");
                Uri deleteUri = ContentUris.withAppendedId(uri,cur.getLong(0));
                System.out.println(getContentResolver().delete(deleteUri, null, null));
            }
        }


    }
    @Override


    public void onClick(View arg0) {
        Button b = (Button)findViewById(R.id.my_button);

        getCalendars();
        b.setClickable(true);
        new LongRunningGetIO().execute();
    }

    private class LongRunningGetIO extends AsyncTask <Void, Void, String> {

        //Parsing from json to millis
        protected long parseToMillis(String json){
            String[] parsed = json.replace("+"," ").split(" |-|T|:");

            Calendar beginTime = Calendar.getInstance();
            System.out.println(parsed[0]);
            beginTime.set(Integer.parseInt(parsed[0]), Integer.parseInt(parsed[1]), Integer.parseInt(parsed[2]), Integer.parseInt(parsed[3]), Integer.parseInt(parsed[4]));
            return beginTime.getTimeInMillis();


        }
        @Override

        protected String doInBackground(Void... params) {
            HttpClient httpClient = new DefaultHttpClient();
            HttpContext localContext = new BasicHttpContext();
            HttpGet httpGet = new HttpGet("http://10.0.2.2:3000/events");
            String text = "";
            try {
                HttpResponse response = httpClient.execute(httpGet, localContext);


                HttpEntity entity = response.getEntity();

                if (entity != null) {
                    String retSrc = EntityUtils.toString(entity);

                    JSONArray jsonArray = new JSONArray(retSrc);

                    for(int i=0; i < jsonArray.length(); i++){
                        JSONObject jsonObject = jsonArray.getJSONObject(i);

                        String name = jsonObject.optString("name").toString();
                        String start = jsonObject.optString("start").toString();
                        String end = jsonObject.optString("end").toString();
                        String description = jsonObject.optString("description").toString();
                        //FOR event adding
                        addEvent(name,parseToMillis(start),parseToMillis(end),description);
                        if (description == "null") {
                            description = "";
                        }

                        text += name + "\nStart Date: "+ start +" \nEnd Date: "+ end +" \nDescription: "+ description +" \n\n";
                    }
                }
            } catch (Exception e) {
                return e.getLocalizedMessage();
            }
            return text;
        }


        protected void onPostExecute(String results) {
            if (results!=null) {
                EditText et = (EditText)findViewById(R.id.my_edit);
                et.setText(results);
            }
            Button b = (Button)findViewById(R.id.my_button);
            b.setClickable(true);
        }


    }

}