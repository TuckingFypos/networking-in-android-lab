package com.example.tuckingfypos.networkinglab;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private TextView mText;
    private Button mButton1;
    private Button mButton2;
    private Button mButton3;
    private ListView mListview;

    ArrayList<String> mItemList;
    ArrayAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mText = (TextView) findViewById(R.id.text);
        mButton1 = (Button) findViewById(R.id.cereal_button);
        mButton2 = (Button) findViewById(R.id.chocolate_button);
        mButton3 = (Button) findViewById(R.id.tea_button);
        mListview = (ListView) findViewById(R.id.listView);
        mItemList = new ArrayList<>();
        mAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1,
                android.R.id.text1, mItemList);
        mListview.setAdapter(mAdapter);


        mButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mItemList.clear();
                new DownloadTask().execute("http://api.walmartlabs.com" +
                        "/v1/search?apiKey=p2wb5h6mtq4rw84c2d4a55jy&format=json&query=cereal");
            }

        });

        mButton2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                mItemList.clear();
                new DownloadTask().execute("http://api.walmartlabs.com" +
                        "/v1/search?apiKey=p2wb5h6mtq4rw84c2d4a55jy&format=json&query=chocolate");
            }
        });

        mButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mItemList.clear();
                new DownloadTask().execute("http://api.walmartlabs.com" +
                        "/v1/search?apiKey=p2wb5h6mtq4rw84c2d4a55jy&format=json&query=tea");
            }
        });


        ConnectivityManager conMag = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = conMag.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            Toast.makeText(MainActivity.this, "Connection Established", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, "INTERNETS DEAD", Toast.LENGTH_LONG).show();
        }
    }

    //make a method that takes a string to be used as a URL
    public String downloadUrl(String myURL) throws IOException, JSONException {
        //establish an input stream as null
        InputStream is = null;
        //use a try with the throws above to catch your IOException elsewhere
        try {
            //make a URL object, set the string to it
            URL url = new URL(myURL);
            //establish an HttpURLConnection using that URL
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            //set the type of request you're going to send (GET, POST, etc)
            conn.setRequestMethod("GET");
            //make the connection
            conn.connect();

            //gives the input stream permission to get data using the connection
            is = conn.getInputStream();

            //make a string for use to use to easily parse the returned data and a method to read it
            String contentAsString = readIt(is);
            //parse the JSON
            String parsedJson = parseJson(contentAsString);
            //return the content
            return parsedJson;
            //finally runs no matter the result of the try
        } finally {
            //if the try got far enough in to establish the is, close it when it's done
            if (is != null) {
                is.close();
            }
        }
    }

    private String readIt(InputStream is) throws IOException {
        //use StringBuilder to turn your inputstream into a String efficiently
        StringBuilder sb = new StringBuilder();
        //BufferedReader takes another reader type, in this case InputStreamReader, and
        //more safely reads it.
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        //make a String
        String read;

        //create a while look which reads through br and assigns the data to read using sb
        while ((read = br.readLine()) != null) {
            sb.append(read);
        }
        return sb.toString();
    }

    private String performPost(String myUrl) throws IOException, JSONException{
        DataOutputStream os = null;
        InputStream is = null;

        try{
            URL url = new URL(myUrl);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            String urlParameters = "search?query=Tea&format=json&apiKey=p2wb5h6mtq4rw84c2d4a55jy";
            //strings need to be turned into bytes so they can be passed properly to other programs
            byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);
            int postDataLength = postData.length;

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("charset","utf-8");
            conn.setRequestProperty("Content-Length",Integer.toString(postDataLength));

            os = new DataOutputStream(conn.getOutputStream());
            os.write(postData);
            os.flush();

            is = conn.getInputStream();
            return readIt(is);
        } finally {
            if (is != null){
                is.close();
            }
            if (os != null){
                os.close();
            }
        }
    }

    private void parseJson(String contentAsString) throws JSONException{
        JSONObject search = new JSONObject(contentAsString);
        JSONArray items = search.getJSONArray("items");
        for (int i = 0; i < items.length(); i++) {

            JSONObject item = items.getJSONObject(i);
            mItemList.add(item.getString("name"));
        }

        private class DownloadTask extends AsyncTask<String,Void,Void> {

            @Override
            protected Void doInBackground(String... strings) {
                try {
                    downloadUrl(strings[0]);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void s) {
                super.onPostExecute(s);
                mAdapter.notifyDataSetChanged();
            }

        }
    }
