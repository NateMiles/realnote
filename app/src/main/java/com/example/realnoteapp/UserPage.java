package com.example.realnoteapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;



public class UserPage extends AppCompatActivity {
    Button startChat;
    Button setUser;
    Button randChat;
    FirebaseFirestore db;
    String email;
    FirebaseAuth mAuth;
    public String username;
    Boolean history;
    double lat;
    double lng;
    boolean count;
    JSONObject jsonInfo;
    JSONArray resultInfo;
    JSONArray locationInfo;
    String zipcode;
    Intent chat;
    String roomName;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_page);
        getSupportActionBar().hide();
        setTitle("Chat Settings");
        count=true;
        chat = new Intent(getApplicationContext(), chat.class);
        mAuth = FirebaseAuth.getInstance();
        //initialise storage for user information
        db = FirebaseFirestore.getInstance();
        //get email from signin
        email = getIntent().getStringExtra("email").toLowerCase();
        setUser = (Button) findViewById(R.id.saveUser);
        startChat = (Button) findViewById(R.id.chatBtn);
        randChat = (Button) findViewById(R.id.randChat);
        getUsername();
        setUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUser();
                getUsername();
            }
        });
        startChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startChat();
            }
        });
        randChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getRandChat();
                startChat();
            }
        });

        LocationManager mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if ( ContextCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {

            ActivityCompat.requestPermissions( this, new String[] {  android.Manifest.permission.ACCESS_FINE_LOCATION  }, 1);
        }

        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, mLocationListener);

    }

    private void startChat() {
        chat.putExtra("historyEnabled", history);
        startActivity(chat);

    }

    private void setUser() {
        Map<String, Object> user = new HashMap<>();
        username = ((TextView) findViewById(R.id.userName)).getText().toString();
        DocumentReference userRef = db.collection("users").document(email);
        userRef
                .update("name", username)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        System.out.println("SUCCESS");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    private void getRandChat() {
        history=false;
        chat.putExtra("history", history);
        DocumentReference docRef = db.collection("randChat").document("openChat");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("aa", "DocumentSnapshot data: " + document.getData());
                        String users= document.getString("users");
                        String room= document.getString("chatname");

                        if(users.equals("2")){
                            resetRandUsers("0");
                            resetRandName();
                        }
                        if(room!=null){
                            roomName=room;
                            System.out.println("ROOMNAME IS"+roomName);
                            chat.putExtra("roomName", roomName);
                            if(users.equals("1")){
                                resetRandUsers("2");
                            }else if(users.equals("0")){
                                resetRandUsers("1");
                            }
                        }
                    } else {
                        Log.d("aa", "No such document");
                    }
                } else {
                    Log.d("aa", "get failed with ", task.getException());
                }
            }
        });
    }
    private void resetRandName() {
        Map<String, Object> user = new HashMap<>();
        DocumentReference userRef = db.collection("randChat").document("openChat");
        userRef
                .update("chatname", email)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        System.out.println("SUCCESS");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        e.printStackTrace();
                    }
                });
    }
    private void resetRandUsers(String num) {
        Map<String, Object> user = new HashMap<>();
        DocumentReference userRef = db.collection("randChat").document("openChat");
        userRef
                .update("users", num)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        System.out.println("SUCCESS");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        e.printStackTrace();
                    }
                });
    }
    public void itemClicked(View v) {
        //code to check if this checkbox is checked!
        CheckBox checkBox = (CheckBox) v;
        if (checkBox.isChecked()) {
            history = true;
        }
    }

    private void getUsername() {
        DocumentReference docRef = db.collection("users").document(email);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("aa", "DocumentSnapshot data: " + document.getData());
                        username = document.getString("name");
                        if(username!=null) {
                            chat.putExtra("username", username);
                        }
                    } else {
                        Log.d("aa", "No such document");
                    }
                } else {
                    Log.d("aa", "get failed with ", task.getException());
                }
            }
        });
    }
    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            System.out.println("LOCATION"+location.toString());
            lat= location.getLatitude();
            lng= location.getLongitude();
            if(count==true) {
                new JsonTask().execute("http://www.mapquestapi.com/geocoding/v1/reverse?key=nivoaVTB7lEmduigwZaU8yy9ETrujfr9&location=" + lat + "," + lng);
                count=false;
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };
    private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected String doInBackground(String... params) {


            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line+"\n");
                     try{
                         jsonInfo = new JSONObject(line.toString());
                         resultInfo=jsonInfo.getJSONArray("results");
                         locationInfo=resultInfo.getJSONObject(0).getJSONArray("locations");
                         zipcode=locationInfo.getJSONObject(0).getString("postalCode");
                     }catch (Exception e){
                         e.printStackTrace();
                     }
                    try{
                     }catch (Exception e){

                     }
                }

                return zipcode;


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            zipcode=result;
            if(zipcode!=null) {
                chat.putExtra("zip", zipcode);
            }
        }
    }
}
