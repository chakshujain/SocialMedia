package com.example.socialmedia.Adapters;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.socialmedia.Models.Messages;
import com.example.socialmedia.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder>{
    private List<Messages> userMessagesList;

    private FirebaseAuth mAuth;
    private DatabaseReference usersDatabaseRef,NotificationsRef;

    public MessagesAdapter(List<Messages> userMessagesList) {
        this.userMessagesList = userMessagesList;
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView SenderMessageText,ReceiverMessageTxt,senderTime,receiverTime;
        public CircleImageView receiverProfileImage;
        public LinearLayout receiverLinearLayout;
        public LinearLayout senderLinearLayout;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            SenderMessageText = (TextView)itemView.findViewById(R.id.sender_message_text);
            ReceiverMessageTxt = (TextView) itemView.findViewById(R.id.receiver_message_text);
            receiverProfileImage = (CircleImageView)itemView.findViewById(R.id.message_profile_image);
            receiverLinearLayout = (LinearLayout)itemView.findViewById(R.id.receiver_linear_layout);
            senderLinearLayout = (LinearLayout)itemView.findViewById(R.id.sender_linear_layout);
            senderTime = (TextView)itemView.findViewById(R.id.sender_time);
            receiverTime = (TextView)itemView.findViewById(R.id.receiver_time);

        }
    }

    @NonNull
    @Override
    public MessagesAdapter.MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.message_layout_of_users,viewGroup,false);
        mAuth = FirebaseAuth.getInstance();
        MessageViewHolder viewHolder=new MessageViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final MessagesAdapter.MessageViewHolder messageViewHolder, int i) {
        String messageSenderId = mAuth.getCurrentUser().getUid();
        Messages messages = userMessagesList.get(i);
        String fromUserId = messages.getFrom();
        String fromMessageType = messages.getType();

        messageViewHolder.receiverLinearLayout.setVisibility(View.VISIBLE);
        messageViewHolder.senderLinearLayout.setVisibility(View.VISIBLE);
        messageViewHolder.SenderMessageText.setVisibility(View.VISIBLE);
        messageViewHolder.ReceiverMessageTxt.setVisibility(View.VISIBLE);

        if(fromMessageType.equals("text")){
            if(fromUserId.equals(messageSenderId)){
                messageViewHolder.ReceiverMessageTxt.setVisibility(View.INVISIBLE);
                messageViewHolder.receiverProfileImage.setVisibility(View.INVISIBLE);
                messageViewHolder.receiverLinearLayout.setVisibility(View.INVISIBLE);
                messageViewHolder.senderTime.setText(messages.getTime());
//                messageViewHolder.SenderMessageText.setBackgroundResource(R.drawable.receiver_message_text_background);
                messageViewHolder.SenderMessageText.setTextColor(Color.BLACK);
//                messageViewHolder.SenderMessageText.setGravity(Gravity.LEFT);
                messageViewHolder.SenderMessageText.setText(messages.getMessage());


            }
            else{
                usersDatabaseRef = FirebaseDatabase.getInstance().getReference().child("users").child(fromUserId);
                usersDatabaseRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){

                            if(dataSnapshot.hasChild("profileimage")){
                                String image = dataSnapshot.child("profileimage").getValue().toString();
                                Picasso.get().load(image).placeholder(R.drawable.profile).into(messageViewHolder.receiverProfileImage);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                messageViewHolder.senderLinearLayout.setVisibility(View.INVISIBLE);
                messageViewHolder.SenderMessageText.setVisibility(View.INVISIBLE);
                messageViewHolder.receiverLinearLayout.setVisibility(View.VISIBLE);
                messageViewHolder.ReceiverMessageTxt.setVisibility(View.VISIBLE);
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                messageViewHolder.receiverTime.setText(messages.getTime());
//               messageViewHolder.ReceiverMessageTxt.setBackgroundResource(R.drawable.receiver_message_text_background);
                messageViewHolder.ReceiverMessageTxt.setTextColor(Color.BLACK);
//                messageViewHolder.ReceiverMessageTxt.setGravity(Gravity.RIGHT);
                messageViewHolder.ReceiverMessageTxt.setText(messages.getMessage());


            }
        }
    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }


}