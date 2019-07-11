package com.example.realnoteapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.scaledrone.lib.HistoryRoomListener;
import com.scaledrone.lib.Listener;
import com.scaledrone.lib.Room;
import com.scaledrone.lib.RoomListener;
import com.scaledrone.lib.Scaledrone;
import com.scaledrone.lib.SubscribeOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class chat extends AppCompatActivity implements RoomListener {
    private String channelID = "wurReXcy2cOLN8E9";
    private String roomName = "observable-room";
    private EditText editText;
    private Scaledrone scaledrone;
    private ImageButton sendBn;
    //private MessageAdapter messageAdapter;
    private ListView messagesView;
    private boolean historyEnabled;

    String username;
    FirebaseAuth mAuth;
    FirebaseFirestore db;

    ArrayAdapter<String> aa;

    ArrayList<String> arrayList= new ArrayList<String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        initliaizeElements();
        MemberData data = new MemberData(username, getRandomColor());

        scaledrone = new Scaledrone(channelID, data);
        scaledrone.connect(new Listener() {
            @Override
            public void onOpen() {

                System.out.println("Scaledrone connection open");
                Room room=scaledrone.subscribe(roomName, new RoomListener() {
                    // implement the default RoomListener methods here
                    @Override
                    public void onOpen(Room room) {
                        System.out.println("Connected to room");
                    }

                    @Override
                    public void onOpenFailure(Room room, Exception ex) {
                        System.err.println(ex);
                    }

                    @Override
                    public void onMessage(Room room, com.scaledrone.lib.Message receivedMessage) {
                        dbCreate(receivedMessage);
                        final ObjectMapper mapper = new ObjectMapper();
                        try{
                            //final MemberData data = mapper.treeToValue(receivedMessage.getMember().getClientData(), MemberData.class);
                            //System.out.println(data.toString());
                            final String messageTest=receivedMessage.getData().asText();
                            boolean currentUser =receivedMessage.getClientID()==scaledrone.getClientID();

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    arrayList.clear();
                                    arrayList.add(messageTest);
                                    System.out.println(messageTest+" TEST");
                                    aa.addAll(arrayList);
                                    messagesView.setAdapter(aa);
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                            System.out.println("HERES CONNECT FAIL");
                        }
                    }
                }, new SubscribeOptions(50));
                room.listenToHistoryEvents(new HistoryRoomListener() {
                    @Override
                    public void onHistoryMessage(Room room, com.scaledrone.lib.Message message) {
                        if (historyEnabled==true){
                            final ObjectMapper map = new ObjectMapper();
                            try {
                                System.out.println(message.getClientID());

                                //final MemberData data2 = map.treeToValue(message.getMember().getClientData(), MemberData.class);
                                final String msg=message.getData().asText();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        arrayList.clear();
                                        arrayList.add(msg);
                                        aa.addAll(arrayList);
                                        messagesView.setAdapter(aa);
                                        System.out.println("Received    a message from the past " + msg);
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                                System.out.println("HERES CONNECT FAIL");
                            }

                        }

                    }
                });
            }

            @Override
            public void onOpenFailure(Exception ex) {
                System.err.println(ex);
            }

            @Override
            public void onFailure(Exception ex) {
                System.err.println("CONNECT FAILED"+ex);
            }

            @Override
            public void onClosed(String reason) {
                System.err.println(reason);
            }


        });
    }


    public void sendMessage(View view) {
        String message = editText.getText().toString();
        message=username+": "+message;
        System.out.println("sending"+message);
        if (message.length() > 0) {
            scaledrone.publish(roomName, message);
            editText.getText().clear();
        }
    }

    @Override
    public void onOpen(Room room) {}

    @Override
    public void onOpenFailure(Room room, Exception ex) {
        System.err.println(ex);
    }

    @Override
    public void onMessage(Room room, com.scaledrone.lib.Message receivedMessage) {
        dbCreate(receivedMessage);
        final ObjectMapper mapper = new ObjectMapper();
        try{
            final MemberData data = mapper.treeToValue(receivedMessage.getMember().getClientData(), MemberData.class);
            System.out.println(data.toString());
            final String messageTest=receivedMessage.getData().asText();
            boolean currentUser =receivedMessage.getClientID()==scaledrone.getClientID();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    arrayList.clear();
                    arrayList.add(messageTest);
                    arrayList.add(messageTest);
                    aa.addAll(arrayList);
                    messagesView.setAdapter(aa);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private String getRandomColor() {
        Random r = new Random();
        StringBuffer sb = new StringBuffer("#");
        while(sb.length() < 7){
            sb.append(Integer.toHexString(r.nextInt()));
        }
        return sb.toString().substring(0, 7);
    }
    private void dbCreate(com.scaledrone.lib.Message message){
        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        user.put("clientID", message.getClientID());

        db.collection("ClientIDS").document(mAuth.getCurrentUser().getUid()).set(user);
    }
    private void initliaizeElements(){
        setTitle("Live Local Chat");
        Intent curInt=getIntent();
        getSupportActionBar().hide();
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        historyEnabled=curInt.getBooleanExtra("historyEnabled",false);
        if(curInt.getStringExtra("roomName")==null) {
            roomName = roomName + curInt.getStringExtra("zip");
        }else{
            roomName=roomName + curInt.getStringExtra("roomName");
        }
        username=curInt.getStringExtra("username");
        messagesView = (ListView) findViewById(R.id.otherMsg);
        messagesView.setStackFromBottom(true);
        sendBn=findViewById(R.id.sendBtn);
        editText=findViewById(R.id.editText);
        aa = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text = (TextView) view.findViewById(android.R.id.text1);
                text.setTextColor(Color.WHITE);
                return view;
            }
        };
    }
}

class MemberData {
    private String name;
    private String color;

    public MemberData(String name, String color) {
        this.name = name;
        this.color = color;
    }

    public MemberData() {
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "MemberData{" +
                "name='" + name + '\'' +
                ", color='" + color + '\'' +
                '}';
    }
}
