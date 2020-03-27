package com.example.socialmedia;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.socialmedia.Models.Posts;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyPostsActivity extends AppCompatActivity {
    private Toolbar myPostsToolbar;
    private DatabaseReference LikesRef;
    Boolean likeChecker = false;
    private DatabaseReference UserRef,PostsRef;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private RecyclerView myPostsList;
    private String model_profile_image,model_post_image;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posts);
        myPostsToolbar = (Toolbar)findViewById(R.id.myPosts_app_bar_layout);
        setSupportActionBar(myPostsToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("My Posts");
        mAuth = FirebaseAuth.getInstance();
        UserRef = FirebaseDatabase.getInstance().getReference().child("users");
        LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        currentUserId = mAuth.getCurrentUser().getUid();
        myPostsList = (RecyclerView)findViewById(R.id.my_posts_list);
        myPostsList.hasFixedSize();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myPostsList.setLayoutManager(linearLayoutManager);
        DisplayMyPosts();
    }


        private void DisplayMyPosts() {
            Query myposts = PostsRef.orderByChild("uid").startAt(currentUserId).endAt(currentUserId + "\uf8ff");
            FirebaseRecyclerOptions<Posts> options=new FirebaseRecyclerOptions.Builder<Posts>().setQuery(myposts,Posts.class).build();

            FirebaseRecyclerAdapter<Posts, MyPostsActivity.PostsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Posts, MyPostsActivity.PostsViewHolder>(options) {

                @Override
                protected void onBindViewHolder(@NonNull final MyPostsActivity.PostsViewHolder holder, int position, @NonNull Posts model) {
                    final String PostKey = getRef(position).getKey();
                    holder.username.setText(model.getFullname());
                    holder.time.setText(" " + model.getTime());
                    holder.date.setText(" " + model.getDate());
                    holder.description.setText(model.getDescription());
                    final String model_profile_image = model.getProfileimage();

                    if (!TextUtils.isEmpty(model_profile_image)) {
                        try {
                            Picasso.get().load(model_profile_image).placeholder(R.drawable.profile).networkPolicy(NetworkPolicy.OFFLINE).into(holder.user_post_image, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError(Exception e) {
                                    Picasso.get().load(model_profile_image).placeholder(R.drawable.profile).into(holder.user_post_image);

                                }
                            });
                        } catch (Exception e) {
                            Picasso.get().load(R.drawable.profile).into(holder.user_post_image);
                        }

                    } else {
                        Picasso.get().load(R.drawable.profile).into(holder.user_post_image);
                    }

                    final String model_post_image = model.getPostimage();

                    Picasso.get().load(model.getPostimage()).networkPolicy(NetworkPolicy.OFFLINE).into(holder.postImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get().load(model_post_image).into(holder.postImage);
                        }
                    });

                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent ClickPostIntent = new Intent(MyPostsActivity.this,OnCickPostActivity.class);
                            ClickPostIntent.putExtra("PostKey",PostKey);
                            startActivity(ClickPostIntent);
                        }
                    });
                    holder.comment_button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent commentIntent = new Intent(MyPostsActivity.this,CommentsActivity.class);
                            commentIntent.putExtra("PostKey",PostKey);
                            startActivity(commentIntent);
                        }
                    });
                    holder.setLikebuttonStatus(PostKey);
                    holder.like_button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            likeChecker = true;
                            LikesRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(likeChecker.equals(true)){
                                        if(dataSnapshot.child(PostKey).hasChild(currentUserId)){
                                            LikesRef.child(PostKey).child(currentUserId).removeValue();
                                            likeChecker = false;
                                        }
                                        else{
                                            LikesRef.child(PostKey).child(currentUserId).setValue(true);
                                            likeChecker = false;
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    });

                }

                @NonNull
                @Override
                public MyPostsActivity.PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.all_posts_layout,parent,false);
                    MyPostsActivity.PostsViewHolder viewHolder=new MyPostsActivity.PostsViewHolder(view);
                    return viewHolder;
                }
            };
            myPostsList.setAdapter(firebaseRecyclerAdapter);

            firebaseRecyclerAdapter.startListening();

        }
        public static class PostsViewHolder extends RecyclerView.ViewHolder {
            TextView username, date, time, description, noOflikes;
            CircleImageView user_post_image;
            ImageView postImage;
            ImageButton like_button, comment_button;
            DatabaseReference LikesRef;
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            int likescount = 0;

            public PostsViewHolder(View itemView) {
                super(itemView);

                username = itemView.findViewById(R.id.post_user_name);
                date = itemView.findViewById(R.id.post_date);
                time = itemView.findViewById(R.id.post_time);
                description = itemView.findViewById(R.id.post_retrieve_description);
                postImage = itemView.findViewById(R.id.post_image);
                user_post_image = itemView.findViewById(R.id.post_profile_image);
                noOflikes = itemView.findViewById(R.id.no_of_likes_textView);
                like_button = itemView.findViewById(R.id.dislike_button);
                comment_button = itemView.findViewById(R.id.comment_button);
                LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");

            }


            public void setLikebuttonStatus(final String postKey) {
                LikesRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child(postKey).hasChild(currentUserId)) {
                            likescount = (int) dataSnapshot.child(postKey).getChildrenCount();
                            like_button.setBackgroundResource(R.drawable.happy_image);
                            noOflikes.setText(Integer.toString(likescount) + " likes");
                        } else {
                            likescount = (int) dataSnapshot.child(postKey).getChildrenCount();
                            like_button.setBackgroundResource(R.drawable.normal_image);
                            noOflikes.setText(Integer.toString(likescount) + " likes");

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

        }}
