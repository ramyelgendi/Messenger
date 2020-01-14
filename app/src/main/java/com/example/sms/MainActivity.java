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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    Button snd;
    EditText ToView;
    EditText MessageView;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        snd = (Button) findViewById(R.id.snd);
        ToView = (EditText) findViewById(R.id.ToView);
        MessageView = (EditText) findViewById(R.id.MessageView);

        final ArrayAdapter<String> itemsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

        snd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(itemsAdapter);
            }
        });
//
//
//

        ListView listView = (ListView) findViewById(R.id.listview);
        listView.setAdapter(itemsAdapter);
    }

    protected void sendMessage(ArrayAdapter<String> itemsAdapter){
        try {
            String to = ToView.getText().toString();
            String message = MessageView.getText().toString();
            itemsAdapter.add(to+": "+message);

            URL url = new URL("http://localhost:3000/myroute/send?to="+to+"&message="+message);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            Toast.makeText(getApplicationContext(), "Message Sent", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            System.out.println(e.getMessage());
            Toast.makeText(getApplicationContext(), "ERROR:\n"+e, Toast.LENGTH_LONG).show();
        }


    }

}
