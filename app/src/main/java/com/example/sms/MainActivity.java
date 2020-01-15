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

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
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
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    Button snd;
    Button refresh;
    EditText ToView;
    EditText MessageView;
    String frm;
    ArrayList<String> mesgs;
    ArrayAdapter<String> itemsAdapter;
    RequestQueue requestQueue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        frm = "01003435365";

        mesgs = new ArrayList<>();

        snd = (Button) findViewById(R.id.snd);
        ToView = (EditText) findViewById(R.id.ToView);
        MessageView = (EditText) findViewById(R.id.MessageView);
        refresh = findViewById(R.id.refresh);
        requestQueue = Volley.newRequestQueue(this);

        itemsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mesgs);
        itemsAdapter.setNotifyOnChange(true);

        snd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                send_msg sender = new send_msg();
//                sender.execute(new String[] {ToView.getText().toString(), MessageView.getText().toString()});
                itemsAdapter.add(ToView.getText().toString() + ": " + MessageView.getText().toString());
                try {
                    String URL = "http://10.0.2.2:3000/myroute/sendsms";
                    JSONObject jsonBody = new JSONObject();
                    jsonBody.put("src_num", frm);
                    jsonBody.put("dest_num", ToView.getText().toString());
                    jsonBody.put("msg", MessageView.getText().toString());
                    final String requestBody = jsonBody.toString();

                    StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.i("VOLLEY", response);
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("VOLLEY", error.toString());
                        }
                    }) {
                        @Override
                        public String getBodyContentType() {
                            return "application/json; charset=utf-8";
                        }

                        @Override
                        public byte[] getBody() throws AuthFailureError {
                            try {
                                return requestBody.getBytes("utf-8");
                            } catch (UnsupportedEncodingException uee) {
                                VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                                return null;
                            }
                        }

                        @Override
                        protected Response<String> parseNetworkResponse(NetworkResponse response) {
                            String responseString = "";
                            if (response != null) {
                                responseString = String.valueOf(response.statusCode);
                                // can get more details such as response.headers
                            }
                            return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
                        }
                    };

                    requestQueue.add(stringRequest);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                ToView.setText("");
                MessageView.setText("");
            }
        });

//        refresh.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String url = "http://10.0.2.2:3000/myroute/getsms/" + frm;
//
//                JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
//                    @Override
//                    public void onResponse(JSONArray response_arr) {
//                        try {
//                            JSONObject response = response_arr.getJSONObject(0);
//                            String frm = response.getString("src_num");
//                            String msg = response.getString("msg");
//                            int id = response.getInt("id");
//                            itemsAdapter.add(frm + ": " + msg);
//                                    sent(id);
//                        } catch (Exception err) {
//                            System.out.println(err.toString());
//                        }
//                    }
//                }, new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        Log.d("Error.Response", error.toString());
//                    }
//                });
//                requestQueue.add(jsonObjectRequest);
//            }
//        });

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                String url = "http://10.0.2.2:3000/myroute/getsms/" + frm;

                JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response_arr) {
                        try {
                            JSONObject response = response_arr.getJSONObject(0);
                            String frm = response.getString("src_num");
                            String msg = response.getString("msg");
                            int id = response.getInt("id");
                            itemsAdapter.add(frm + ": " + msg);
                            sent(id);
                        } catch (Exception err) {
                            System.out.println(err.toString());
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error.Response", error.toString());
                    }
                });
                requestQueue.add(jsonObjectRequest);
                //your method
            }
        }, 0, 1000);//put here time 1000 milliseconds=1 second

        ListView listView = (ListView) findViewById(R.id.listview);
        listView.setAdapter(itemsAdapter);
    }

    void sent(int id) {
        String url = "http://10.0.2.2:3000/myroute/sentsms/" + id;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("VOLLEY", response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("VOLLEY", error.toString());
            }
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                String responseString = "";
                if (response != null) {
                    responseString = String.valueOf(response.statusCode);
                }
                return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
            }
        };

        requestQueue.add(stringRequest);
    }


    //// MANUAL ASYNC TASK BELOW

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
//                OutputStream wr = urlConnection.getOutputStream();
//                wr.write(body.toString().getBytes("UTF-8"));
//                wr.close();

                urlConnection.getOutputStream().write(body.toString().getBytes("UTF8"));

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
            String resp = "";
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
            task.execute(new Message[]{result});
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
