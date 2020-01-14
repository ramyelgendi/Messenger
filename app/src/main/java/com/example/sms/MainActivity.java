package com.example.sms;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import java.io.BufferedWriter;
import java.io.InputStream;
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

    //// best sauce i could find https://stackoverflow.com/questions/2793150/how-to-use-java-net-urlconnection-to-fire-and-handle-http-requests
    //// also this https://dzone.com/articles/how-to-parse-json-data-from-a-rest-api-using-simpl

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        frm = "mobile_user";

        snd = (Button) findViewById(R.id.snd);
        ToView = (EditText) findViewById(R.id.ToView);
        MessageView = (EditText) findViewById(R.id.MessageView);

        final ArrayAdapter<String> itemsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mesgs);
        itemsAdapter.setNotifyOnChange(true);

        snd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(itemsAdapter);
            }
        });

        ListView listView = (ListView) findViewById(R.id.listview);
        listView.setAdapter(itemsAdapter);
    }

    protected void sendMessage(ArrayAdapter<String> itemsAdapter){
        try {
            String to = ToView.getText().toString();
            String message = MessageView.getText().toString();
            itemsAdapter.add(to+": "+message);

            URL url = new URL("http://localhost:3000/myroute/sendsms?src_num="+frm+"dest_num="+to+"&msg="+message);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            Toast.makeText(getApplicationContext(), "Message Sent", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            System.out.println(e.getMessage());
            Toast.makeText(getApplicationContext(), "ERROR:\n"+e, Toast.LENGTH_LONG).show();
        }


    }

    protected void getMessage(ArrayAdapter<String> adapter) {
        try {
            URL url = new URL("http://localhost:3000/myroute/getsms");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();
            String inline = "";
            Scanner sc = new Scanner(url.openStream());
            while(sc.hasNext())
            {
                inline+=sc.nextLine();
            }
            JSONObject json = new JSONObject(inline);
            String from = json.getString("src_num");
            String msg = json.getString("msg");

            mesgs.add(from+": "+msg);

            int id = json.getInt("id");
            sentMessage(id);

        } catch (Exception err) {
            System.out.println(err.getMessage());
        }
    }

    protected void sentMessage(int msg_id) {
        try {
            URL url = new URL("http://localhost:3000/myroute/sentsms?id="+msg_id);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

        } catch (Exception err) {
            System.out.println(err.getMessage());
        }
    }

}
