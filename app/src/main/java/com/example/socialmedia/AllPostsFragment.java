package com.example.socialmedia;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;

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

import de.hdodenhof.circleimageview.CircleImageView;

import android.support.v4.app.Fragment;
import android.widget.Toast;

public class AllPostsFragment extends Fragment {
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private RecyclerView postList;
    private Toolbar mtoolbar;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private FirebaseAuth mAuth;
    private DatabaseReference UserRef,PostsRef;
    String currentUserId;

    private DatabaseReference LikesRef;
    Boolean likeChecker = false;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.posts_fragment,null);

        mAuth = FirebaseAuth.getInstance();
        UserRef = FirebaseDatabase.getInstance().getReference().child("users");
        LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        currentUserId = mAuth.getCurrentUser().getUid();
        postList = (RecyclerView) view.findViewById(R.id.all_users_post_list);
        postList.hasFixedSize();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);

        DisplayAllUsersPosts();

        return view;

    }

    private void DisplayAllUsersPosts() {
        Query sortpostsindescendingorder = PostsRef.orderByChild("timeStamp");
        FirebaseRecyclerOptions<Posts> options=new FirebaseRecyclerOptions.Builder<Posts>().setQuery(sortpostsindescendingorder,Posts.class).build();

        FirebaseRecyclerAdapter<Posts, PostsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Posts, PostsViewHolder>(options) {

            @Override
            protected void onBindViewHolder(@NonNull final PostsViewHolder holder, int position, @NonNull Posts model) {
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
                        Intent ClickPostIntent = new Intent(getActivity(),OnCickPostActivity.class);
                        ClickPostIntent.putExtra("PostKey",PostKey);
                        startActivity(ClickPostIntent);
                    }
                });
                holder.comment_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent commentIntent = new Intent(getActivity(),CommentsActivity.class);
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
            public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.all_posts_layout,parent,false);
                PostsViewHolder viewHolder=new PostsViewHolder(view);
                return viewHolder;
            }
        };
        postList.setAdapter(firebaseRecyclerAdapter);

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

}

}


