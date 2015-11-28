package com.example.x.myapplication;

        import java.io.IOException;
        import java.io.InputStream;
        import org.apache.http.HttpEntity;
        import org.apache.http.HttpResponse;
        import org.apache.http.client.HttpClient;
        import org.apache.http.client.methods.HttpGet;
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

public class MainActivity extends Activity implements OnClickListener {
    @Override


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        findViewById(R.id.my_button).setOnClickListener(this);
    }


    @Override


    public void onClick(View arg0) {
        Button b = (Button)findViewById(R.id.my_button);


        b.setClickable(true);
        new LongRunningGetIO().execute();
    }

    private class LongRunningGetIO extends AsyncTask <Void, Void, String> {

        /*protected String getASCIIContentFromEntity(HttpEntity entity) throws IllegalStateException, IOException {
            InputStream in = entity.getContent();


            StringBuffer out = new StringBuffer();
            int n = 1;
            while (n>0) {
                byte[] b = new byte[4096];
                n =  in.read(b);


                if (n>0) out.append(new String(b, 0, n));
            }


            return out.toString();
        }*/


        @Override


        protected String doInBackground(Void... params) {
            HttpClient httpClient = new DefaultHttpClient();
            HttpContext localContext = new BasicHttpContext();
<<<<<<< HEAD
            HttpGet httpGet = new HttpGet("http://10.0.2.2:3000/events");
            String text = "";
=======
            HttpGet httpGet = new HttpGet("http://172.16.41.167:3000/events");
            String text = null;
>>>>>>> 30ccc29cf3739e45ffb8d0e88c2fdcfb2e988a3e
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

                        if (description == "null") {
                            description = "";
                        }

                        text += name + "\nStart Date: "+ start +" \nEnd Date: "+ end +" \nDescription: "+ description +" \n\n";
                    }
                }




                // text = getASCIIContentFromEntity(entity);


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