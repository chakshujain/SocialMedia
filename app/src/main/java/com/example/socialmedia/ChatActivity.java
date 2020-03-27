package com.example.socialmedia;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import com.example.socialmedia.Adapters.MessagesAdapter;
import com.example.socialmedia.Models.Messages;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    public static Context mContext;
//    private Toolbar chatToolbar;
    private RecyclerView UserMessagesList;
    private List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessagesAdapter messagesAdapter;
    private EditText InputMessage;
    private ImageButton SendImageFileButton,SendMessageButton;
    private String messageReceiverId,messageReceiverFullName;
    private TextView receiverName,receiverLastSeen;
    private CircleImageView receiverProfileImage;
    private DatabaseReference RootRef,UsersRef,NotificationsRef;
    private FirebaseAuth mAuth;


    String messageSenderId,saveCurrentDate,saveCurrentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
//        chatToolbar = (Toolbar)findViewById(R.id.chat_bar_layout);
//        setSupportActionBar(chatToolbar);
//        ActionBar actionBar = getSupportActionBar();
//        actionBar.setDisplayHomeAsUpEnabled(true);
//        actionBar.setDisplayShowCustomEnabled(true);
//        LayoutInflater layoutInflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        View action_bar_view = layoutInflater.inflate(R.layout.chat_custom_bar,null);
//        actionBar.setCustomView(action_bar_view);
        mContext = ChatActivity.this;
        mAuth = FirebaseAuth.getInstance();
        RootRef = FirebaseDatabase.getInstance().getReference();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("users");
        NotificationsRef = FirebaseDatabase.getInstance().getReference().child("Notifications");
        messageSenderId = mAuth.getCurrentUser().getUid();
        UserMessagesList = (RecyclerView)findViewById(R.id.messages_list);
        InputMessage = (EditText)findViewById(R.id.input_message);
        SendImageFileButton = (ImageButton)findViewById(R.id.send_image_file_button);
        SendMessageButton = (ImageButton)findViewById(R.id.send_message_button);
        messagesAdapter = new MessagesAdapter(messagesList);
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        UserMessagesList.hasFixedSize();
        UserMessagesList.setLayoutManager(linearLayoutManager);
        UserMessagesList.setAdapter(messagesAdapter);
        receiverName = (TextView)findViewById(R.id.custom_profile_name);
        receiverLastSeen = (TextView)findViewById(R.id.custom_user_last_seen);
        receiverProfileImage = (CircleImageView) findViewById(R.id.custom_profile_image);
        messageReceiverFullName = getIntent().getExtras().get("fullname").toString();
        messageReceiverId = getIntent().getExtras().get("visitedUserId").toString();
        receiverName.setText(messageReceiverFullName);
        RootRef.child("users").child(messageReceiverId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(dataSnapshot.hasChild("profileimage")){
                        final String image = dataSnapshot.child("profileimage").getValue().toString();
                        Boolean isOnline = Boolean.parseBoolean((dataSnapshot.child("online").getValue().toString()));
                        final Long last_seen = Long.parseLong(dataSnapshot.child("last_seen").getValue().toString());
                        Picasso.get().load(image).placeholder(R.drawable.profile).networkPolicy(NetworkPolicy.OFFLINE).into(receiverProfileImage, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError(Exception e) {
                                Picasso.get().load(image).placeholder(R.drawable.profile).into(receiverProfileImage);
                            }
                        });
                        if(isOnline){
                            receiverLastSeen.setText("Online");
                        }
                        else{
                            Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
                            calendar.setTimeInMillis(last_seen);
                            String DateTime = DateFormat.format("dd/MM/yyyy  hh:mm aa", calendar).toString();
                            receiverLastSeen.setText("Last seen at: "+DateTime);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMessage();
                UserMessagesList.smoothScrollToPosition(messagesAdapter.getItemCount()+1);

            }
        });
        FetchMessages();
        messagesAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                UserMessagesList.smoothScrollToPosition(messagesAdapter.getItemCount()-1);
            }
        });
    }

    private void FetchMessages() {
        RootRef.child("Messages").child(messageSenderId).child(messageReceiverId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists()) {
                    Messages messages = dataSnapshot.getValue(Messages.class);
                    messagesList.add(messages);

                    messagesAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void SendMessage() {
        String messageText = InputMessage.getText().toString();
        if(TextUtils.isEmpty(messageText)){
            Toast.makeText(this, "Please write a message first...", Toast.LENGTH_SHORT).show();
        }
        else{
            String message_sender_ref = "Messages/" + messageSenderId + "/" + messageReceiverId;
            String message_receiver_ref = "Messages/" + messageReceiverId + "/" + messageSenderId;
            DatabaseReference user_message_key = RootRef.child("Messages").child(messageSenderId).child(messageReceiverId).push();
            String message_push_id = user_message_key.getKey();
            Calendar calFordDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
            saveCurrentDate = currentDate.format(calFordDate.getTime());

            Calendar calFordTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:aa");
            saveCurrentTime = currentTime.format(calFordTime.getTime());
            Map messageTextBody = new HashMap();
            messageTextBody.put("message",messageText);
            messageTextBody.put("time",saveCurrentTime);
            messageTextBody.put("date",saveCurrentDate);
            messageTextBody.put("type","text");
            messageTextBody.put("from",messageSenderId);

            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(message_sender_ref + "/" + message_push_id,messageTextBody);
            messageBodyDetails.put(message_receiver_ref + "/" + message_push_id,messageTextBody);
            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        HashMap<String,String>chatNotificationMap = new HashMap<>();
                        chatNotificationMap.put("from",messageSenderId);
                        chatNotificationMap.put("type","message");
                        NotificationsRef.child(messageReceiverId).push().setValue(chatNotificationMap);

                    }
                    else{
                        Toast.makeText(ChatActivity.this,task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                    }
                }
            });

        }
        InputMessage.setText("");
    }
}
