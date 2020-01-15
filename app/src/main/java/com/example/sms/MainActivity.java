package com.example.sms;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    Button snd;
    EditText ToView;
    EditText MessageView;
    String frm;
    ArrayList<String> mesgs;
    ArrayAdapter<String> itemsAdapter;


    //// best sauce i could find https://stackoverflow.com/questions/2793150/how-to-use-java-net-urlconnection-to-fire-and-handle-http-requests
    //// also this https://dzone.com/articles/how-to-parse-json-data-from-a-rest-api-using-simpl

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        frm = "mobile_user";

        mesgs = new ArrayList<>();

        snd = (Button) findViewById(R.id.snd);
        ToView = (EditText) findViewById(R.id.ToView);
        MessageView = (EditText) findViewById(R.id.MessageView);

        itemsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mesgs);
        itemsAdapter.setNotifyOnChange(true);

        snd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send_msg sender = new send_msg();
                sender.execute(new String[] {ToView.getText().toString(), MessageView.getText().toString()});
                itemsAdapter.add(ToView.getText().toString() + ": " + MessageView.getText().toString());
            }
        });

        ListView listView = (ListView) findViewById(R.id.listview);
        listView.setAdapter(itemsAdapter);
    }

    private class send_msg extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... in) {
            String resp = "";
            try {
                URL link = new URL("http://10.0.2.2:3000/myroute/sendsms");
                HttpURLConnection urlConnection = (HttpURLConnection) link.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json; utf-8");
                urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.setDoOutput(true);

                JSONObject body = new JSONObject();
                body.put("src_num", frm);
                body.put("dest_num", in[0]);
                body.put("msg", in[1]);

//            try(OutputStream os = urlConnection.getOutputStream()) {
//                byte[] input = body.toString().getBytes("utf-8");
//                os.write(input, 0, input.length);
//            }
                OutputStream wr = urlConnection.getOutputStream();
                wr.write(body.toString().getBytes("UTF-8"));
                wr.close();

                urlConnection.connect();

                int status = urlConnection.getResponseCode();
                Log.i("HTTP Client", "HTTP status code : " + status);
                switch (status) {
                    case 200:
                    case 201:
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            sb.append(line + "\n");
                        }
                        bufferedReader.close();
                        Log.i("HTTP Client", "Received String : " + sb.toString());
                        //return received string
                        return sb.toString();
                }


                Log.d("Message", "Message sent.");
                //Toast.makeText(getApplicationContext(), "Message Sent", Toast.LENGTH_LONG).show();

            } catch (Exception e) {
                System.out.println(e.getMessage());
//                Toast.makeText(getApplicationContext(), "ERROR:\n" + e, Toast.LENGTH_LONG).show();
            }

            return resp;
        }
    }


    class Message {
        String src, msg;
        Integer id;
    }

    private class get_msg extends AsyncTask<String, Void, Message> {

        @Override
        protected Message doInBackground(String... url) {
            String resp= "";
            try {
                URL link = new URL("http://localhost:3000/myroute/getsms");
                HttpURLConnection conn = (HttpURLConnection) link.openConnection();
                conn.setRequestMethod("GET");
                conn.connect();
                String inline = "";
                Scanner sc = new Scanner(link.openStream());
                while (sc.hasNext()) {
                    inline += sc.nextLine();
                }
                JSONObject json = new JSONObject(inline);
                Message mes = new Message();
                mes.src = json.getString("src_num");
                mes.msg = json.getString("msg");
                mes.id = json.getInt("id");

                return mes;

            } catch (Exception err) {
                System.out.println(err.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Message result) {
            sent_msg task = new sent_msg();
            task.execute(new Message[] { result });
        }
    }

    private class sent_msg extends AsyncTask<Message, Void, String> {

        @Override
        protected String doInBackground(Message... in) {
            String resp = "";
                try {
                    URL link = new URL("http://localhost:3000/myroute/sentsms/" + in[0].id);
                    HttpURLConnection conn = (HttpURLConnection) link.openConnection();
                    conn.setRequestMethod("GET");
                    conn.connect();

                } catch (Exception err) {
                    System.out.println(err.getMessage());
                }
                return resp;
        }
    }

}
