//package com.example.socialmedia;
//
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.support.annotation.NonNull;
//import android.support.v4.app.Fragment;
//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import com.example.socialmedia.Models.Friends;
//import com.firebase.ui.database.FirebaseRecyclerAdapter;
//import com.firebase.ui.database.FirebaseRecyclerOptions;
//import com.google.android.gms.tasks.Task;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.Query;
//import com.google.firebase.database.ValueEventListener;
//import com.squareup.picasso.Callback;
//import com.squareup.picasso.NetworkPolicy;
//import com.squareup.picasso.Picasso;
//
//import de.hdodenhof.circleimageview.CircleImageView;
//
//public class ChatsFragment extends Fragment {
//    private RecyclerView FriendsList;
//    private DatabaseReference FriendsRef,UsersRef,MessagesRef;
//    private FirebaseAuth mAuth;
//    private String online_user_id,last_message;
//    FirebaseRecyclerAdapter<Friends,FriendsViewHolder> adapter;
//
//
//    public ChatsFragment() {
//        // Required empty public constructor
//    }
//
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.chats_fragment,null);
//        FriendsList = (RecyclerView)view.findViewById(R.id.fragment_messages_list);
//        FriendsList.hasFixedSize();
//        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
//        linearLayoutManager.setReverseLayout(true);
//        linearLayoutManager.setStackFromEnd(true);
//        FriendsList.setLayoutManager(linearLayoutManager);
//        mAuth = FirebaseAuth.getInstance();
//        online_user_id = mAuth.getCurrentUser().getUid();
//        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(online_user_id);
//        UsersRef = FirebaseDatabase.getInstance().getReference().child("users");
//        MessagesRef = FirebaseDatabase.getInstance().getReference().child("Messages");
//        DisplayAllFriends();
//        return view;
//    }
//
//    private void DisplayAllFriends() {
////       Query lastmessageposts = MessagesRef.child(online_user_id);
//        FirebaseRecyclerOptions<Friends> options=new FirebaseRecyclerOptions.Builder<Friends>().
//                setQuery(FriendsRef, Friends.class).build();
//        adapter=new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(options)
//        {
//            @Override
//            protected void onBindViewHolder(@NonNull final FriendsViewHolder holder, final int position, @NonNull Friends model)
//            {
//                final String userIds = getRef(position).getKey();
//                lastMessageDisplay(userIds,holder.lastMessage);
////                Query lastMessageQuery = MessagesRef.child(online_user_id).child(userIds).orderByChild("timeStamp").limitToLast(1);
////                lastMessageQuery.addListenerForSingleValueEvent(new ValueEventListener() {
////                    @Override
////                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
////                        if(dataSnapshot.exists()) {
////                            for (DataSnapshot ds :dataSnapshot.getChildren()) {
////                                last_message = ds.child("message").getValue().toString();
////                                holder.lastMessage.setText(last_message);
////
////                            }
////                        }
////                    }
////
////
////                    @Override
////                    public void onCancelled(@NonNull DatabaseError databaseError) {
////
////                    }
////                });
//
//                UsersRef.child(userIds).addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                        if (dataSnapshot.exists()) {
//                            final String fullname = dataSnapshot.child("fullname").getValue().toString();
//                            if (dataSnapshot.hasChild("online")) {
//                                Boolean isOnline = Boolean.parseBoolean(dataSnapshot.child("online").getValue().toString());
//                                if (isOnline) {
//                                    holder.greenDot.setVisibility(View.VISIBLE);
//                                } else {
//                                    holder.greenDot.setVisibility(View.INVISIBLE);
//                                }
//                            }
//                            holder.fullname.setText(fullname);
//
//                            holder.itemView.setOnClickListener(new View.OnClickListener() {
//                                @Override
//                                public void onClick(View v) {
//
//                                    Intent ChatIntent = new Intent(getActivity(), ChatActivity.class);
//                                    ChatIntent.putExtra("visitedUserId", userIds);
//                                    ChatIntent.putExtra("fullname", fullname);
//                                    startActivity(ChatIntent);
//                                }
//
//                            });
//
//                            if(dataSnapshot.hasChild("profileimage")) {
//                                final String image = dataSnapshot.child("profileimage").getValue().toString();
//                                Picasso.get().load(image).placeholder(R.drawable.profile).networkPolicy(NetworkPolicy.OFFLINE).into(holder.profileimage, new Callback() {
//                                    @Override
//                                    public void onSuccess() {
//
//                                    }
//
//                                    @Override
//                                    public void onError(Exception e) {
//                                        Picasso.get().load(image).placeholder(R.drawable.profile).into(holder.profileimage);
//                                    }
//                                });
//                            }
//                        }
//                    }
//
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                    }
//                });
//
//
//            }
//            @NonNull
//            @Override
//            public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
//            {
//                View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.all_posts_display_layout,viewGroup,false);
//                FriendsViewHolder viewHolder=new FriendsViewHolder(view);
//                return viewHolder;
//            }
//        };
//
//        FriendsList.setAdapter(adapter);
//        adapter.startListening();
//
//    }
//
//    private void lastMessageDisplay(String userIds,final TextView date) {
//        Query lastMessageQuery = MessagesRef.child(online_user_id).child(userIds).orderByKey().limitToLast(1);
//        lastMessageQuery.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if(dataSnapshot.exists()) {
//                    for (DataSnapshot ds :dataSnapshot.getChildren()) {
//                        last_message = ds.child("message").getValue().toString();
//                        date.setText(last_message);
////                        adapter.notifyDataSetChanged();
//
//                    }
//                }
//            }
//
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
//
//    }
//
//    public class FriendsViewHolder extends RecyclerView.ViewHolder
//    {
//        TextView fullname, lastMessage;
//        CircleImageView profileimage;
//        ImageView greenDot;
//
//        public FriendsViewHolder(@NonNull View itemView)
//        {
//            super(itemView);
//            fullname = itemView.findViewById(R.id.search_fullname);
//            lastMessage = itemView.findViewById(R.id.search_status);
//            profileimage = itemView.findViewById(R.id.search_image);
//            greenDot = itemView.findViewById(R.id.green_dot);
//        }
//    }
//}
//
//
//


// old code

package com.example.socialmedia;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.socialmedia.Models.Friends;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatsFragment extends Fragment {
    private RecyclerView FriendsList;
    private DatabaseReference FriendsRef,UsersRef,MessagesRef;
    private FirebaseAuth mAuth;
    private String online_user_id;


    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chats_fragment,null);
        FriendsList = (RecyclerView)view.findViewById(R.id.fragment_messages_list);
        FriendsList.hasFixedSize();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        FriendsList.setLayoutManager(linearLayoutManager);
        mAuth = FirebaseAuth.getInstance();
        online_user_id = mAuth.getCurrentUser().getUid();
        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(online_user_id);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("users");
        MessagesRef = FirebaseDatabase.getInstance().getReference().child("Messages");
        DisplayAllFriends();
        return view;
    }

    private void DisplayAllFriends() {
        Query lastmessageposts = MessagesRef.child(online_user_id);
        FirebaseRecyclerOptions<Friends> options=new FirebaseRecyclerOptions.Builder<Friends>().
                setQuery(lastmessageposts, Friends.class).build(); //query build past the query to FirebaseRecyclerAdapter
        FirebaseRecyclerAdapter<Friends,FriendsViewHolder> adapter=new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(options)
        {
            @Override
            protected void onBindViewHolder(@NonNull final FriendsViewHolder holder, final int position, @NonNull Friends model)
            {
                final String userIds = getRef(position).getKey();
                Query lastMessageQuery = MessagesRef.child(online_user_id).child(userIds).orderByKey().limitToLast(1);
                lastMessageQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()) {
                            for (DataSnapshot ds :dataSnapshot.getChildren()) {
                                final String last_message = ds.child("message").getValue().toString();
                                holder.date.setText(last_message);
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                UsersRef.child(userIds).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            final String fullname = dataSnapshot.child("fullname").getValue().toString();
                            if (dataSnapshot.hasChild("online")) {
                                Boolean isOnline = Boolean.parseBoolean(dataSnapshot.child("online").getValue().toString());
                                if (isOnline) {
                                    holder.greenDot.setVisibility(View.VISIBLE);
                                } else {
                                    holder.greenDot.setVisibility(View.INVISIBLE);
                                }
                            }
                            holder.fullname.setText(fullname);

                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    Intent ChatIntent = new Intent(getActivity(), ChatActivity.class);
                                    ChatIntent.putExtra("visitedUserId", userIds);
                                    ChatIntent.putExtra("fullname", fullname);
                                    startActivity(ChatIntent);
                                }

                            });

                            if(dataSnapshot.hasChild("profileimage")) {
                                final String image = dataSnapshot.child("profileimage").getValue().toString();
                                Picasso.get().load(image).placeholder(R.drawable.profile).networkPolicy(NetworkPolicy.OFFLINE).into(holder.profileimage, new Callback() {
                                    @Override
                                    public void onSuccess() {

                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        Picasso.get().load(image).placeholder(R.drawable.profile).into(holder.profileimage);
                                    }
                                });
                            }
                        }
                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }
            @NonNull
            @Override
            public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
            {
                View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.all_posts_display_layout,viewGroup,false);
                FriendsViewHolder viewHolder=new FriendsViewHolder(view);
                return viewHolder;
            }
        };

        FriendsList.setAdapter(adapter);
        adapter.startListening();

    }
    public class FriendsViewHolder extends RecyclerView.ViewHolder
    {
        TextView fullname, date;
        CircleImageView profileimage;
        ImageView greenDot;

        public FriendsViewHolder(@NonNull View itemView)
        {
            super(itemView);
            fullname = itemView.findViewById(R.id.search_fullname);
            date = itemView.findViewById(R.id.search_status);
            profileimage = itemView.findViewById(R.id.search_image);
            greenDot = itemView.findViewById(R.id.green_dot);
        }
    }
}