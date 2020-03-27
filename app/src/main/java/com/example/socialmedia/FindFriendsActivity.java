package com.example.socialmedia;

import android.content.Intent;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity {
    private Toolbar mtoolbar;
    private FirebaseAuth mAuth;
    private AutoCompleteTextView SearchInputText;
    private RecyclerView SearchResultList;
    private ImageButton SearchResultButton;
    private DatabaseReference UsersRef;
    private String currentUserId;
    ArrayList userFullNames = new ArrayList();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);
        mtoolbar = (Toolbar)findViewById(R.id.find_friends_toolbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle("Find FriendsActivity");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("users");
        SearchInputText = findViewById(R.id.search_edit_text);
        SearchResultButton = (ImageButton)findViewById(R.id.search_button);
        SearchResultList = (RecyclerView)findViewById(R.id.search_result_list);
        SearchResultList.hasFixedSize();
        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds:dataSnapshot.getChildren()) {
                    if (ds.hasChild("fullname"))
                        userFullNames.add(ds.child("fullname").getValue().toString());

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        Log.d("cp1",userFullNames.toString());
        SearchResultList.setLayoutManager(new LinearLayoutManager(this));

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_selectable_list_item,userFullNames);
        SearchInputText.setAdapter(arrayAdapter);
        SearchInputText.setThreshold(1);
        SearchResultButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String SearchBoxInput = SearchInputText.getText().toString();
                SearchFriends(SearchBoxInput);
            }
        });

    }

    private void SearchFriends(String searchBoxInput)
    {
        Query q = UsersRef.orderByChild("fullname").startAt(searchBoxInput).endAt(searchBoxInput + "\uf8ff");
        FirebaseRecyclerOptions<FindFriends> options=new FirebaseRecyclerOptions.Builder<FindFriends>().
                setQuery(q, FindFriends.class).build(); //query build past the query to FirebaseRecyclerAdapter
        FirebaseRecyclerAdapter<FindFriends, FindFriendsActivity.FindFriendViewHolder> adapter=new FirebaseRecyclerAdapter<FindFriends, FindFriendViewHolder>(options)
        {
            @Override
            protected void onBindViewHolder(@NonNull FindFriendsActivity.FindFriendViewHolder holder, final int position, @NonNull FindFriends model)
            {
//                final String PostKey = getRef(position).getKey();
                holder.fullname.setText(model.getFullname());
                holder.status.setText(model.getStatus());
                Picasso.get().load(model.getProfileimage()).into(holder.profileimage);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String visited_user_id = getRef(position).getKey();
                        Intent personprofileIntent = new Intent(FindFriendsActivity.this, PersonProfieActivity.class);
                        personprofileIntent.putExtra("visitedUserId",visited_user_id);
                        startActivity(personprofileIntent);
                    }
                });
            }
            @NonNull
            @Override
            public FindFriendsActivity.FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
            {
                View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.all_posts_display_layout,viewGroup,false);

                FindFriendViewHolder viewHolder=new FindFriendViewHolder(view);
                return viewHolder;
            }
        };

        SearchResultList.setAdapter(adapter);
        adapter.startListening();
    }

    public class FindFriendViewHolder extends RecyclerView.ViewHolder
    {
        TextView fullname, status;
        CircleImageView profileimage;

        public FindFriendViewHolder(@NonNull View itemView)
        {
            super(itemView);
            fullname = itemView.findViewById(R.id.search_fullname);
            status = itemView.findViewById(R.id.search_status);
            profileimage = itemView.findViewById(R.id.search_image);
        }
    }




}
