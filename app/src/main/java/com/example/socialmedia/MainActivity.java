package com.example.socialmedia;

import android.accounts.NetworkErrorException;
import android.content.Intent;
import android.media.Image;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

public class MainActivity extends AppCompatActivity {

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
   private RecyclerView postList;
    private Toolbar mtoolbar;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private FirebaseAuth mAuth;
    private DatabaseReference UserRef,PostsRef,currentUserRef;
    private CircleImageView NavProfileImage;
    private TextView NavUserName;
    String currentUserId;
    private ImageButton AddNewPostButton;
    private DatabaseReference LikesRef;
    Boolean likeChecker = false;
    private String image,model_profile_image,model_post_image;
    TextView fragment1,fragment2,fragment3;
    ViewPager viewPager;
    PagerViewAdapter pagerViewAdapter;
    TabLayout myTabLayout;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mtoolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        mtoolbar.setTitle("Home");
        setSupportActionBar(mtoolbar);
        mAuth = FirebaseAuth.getInstance();
        UserRef = FirebaseDatabase.getInstance().getReference().child("users");
        LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        PostsRef.keepSynced(true);
        UserRef.keepSynced(true);
        AddNewPostButton = (ImageButton)findViewById(R.id.add_new_post_button);
        currentUserId = mAuth.getCurrentUser().getUid();
        currentUserRef = UserRef.child(currentUserId);
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this,drawerLayout,R.string.drawer_open,R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        navigationView = (NavigationView)findViewById(R.id.navigation_view);
        View navView = navigationView.inflateHeaderView(R.layout.navigation_header);
        NavProfileImage = (CircleImageView)navView.findViewById(R.id.nav_profile_image);
        NavUserName = (TextView)navView.findViewById(R.id.nav_user_full_name);

        fragment1 = (TextView)findViewById(R.id.fragment1);
        fragment2  =(TextView)findViewById(R.id.fragment2);
        fragment3 = (TextView)findViewById(R.id.fragment3);
        viewPager = (ViewPager)findViewById(R.id.fragment_container);
        pagerViewAdapter = new PagerViewAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerViewAdapter);

        myTabLayout = (TabLayout)findViewById(R.id.main_tabs);
        myTabLayout.setupWithViewPager(viewPager);
        final HashMap<String,Object> isonline =new HashMap<>();
        isonline.put("online",true);
        isonline.put("last_seen",0l);

        final HashMap<String,Object> isoffline=new HashMap<>();
        isoffline.put("online",false);
        isoffline.put("last_seen",System.currentTimeMillis());

        currentUserRef.onDisconnect().updateChildren(isoffline).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                currentUserRef.updateChildren(isonline);
            }
        });

        UserRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(dataSnapshot.hasChild("fullname"))
                    {
                        String fullname = dataSnapshot.child("fullname").getValue().toString();
                        NavUserName.setText(fullname);
                    }
                    if(dataSnapshot.hasChild("profileimage"))
                    {
                        image = dataSnapshot.child("profileimage").getValue().toString();
                        Picasso.get().load(image).placeholder(R.drawable.profile).networkPolicy(NetworkPolicy.OFFLINE).into(NavProfileImage, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError(Exception e) {
                                Picasso.get().load(image).placeholder(R.drawable.profile).into(NavProfileImage);
                            }
                        });
                    }
                    else
                    {
                        Toast.makeText(MainActivity.this, "Profile upload Profile Image...", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                UserMenuSelector(menuItem);
                return false;
            }
        });

        AddNewPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToPostActivity();
            }
        });

//        DisplayAllUsersPosts();
        NavProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToSettingsActivity();
            }
        });
    }
//    private void DisplayAllUsersPosts() {
//        Query sortpostsindescendingorder = PostsRef.orderByChild("counter");
//        FirebaseRecyclerOptions<Posts> options=new FirebaseRecyclerOptions.Builder<Posts>().setQuery(sortpostsindescendingorder,Posts.class).build();
//
//        FirebaseRecyclerAdapter<Posts, PostsViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Posts, PostsViewHolder>(options) {
//
//            @Override
//            protected void onBindViewHolder(@NonNull final PostsViewHolder holder, int position, @NonNull Posts model) {
//                final String PostKey = getRef(position).getKey();
//                try {
//                    holder.username.setText(model.getFullname());
//                    holder.time.setText(" " + model.getTime());
//                    holder.date.setText(" " + model.getDate());
//                    holder.description.setText(model.getDescription());
//                    model_profile_image = model.getProfileimage();
//                    model_post_image  = model.getPostimage();
//                    Picasso.get().load(model.getProfileimage()).networkPolicy(NetworkPolicy.OFFLINE).into(holder.user_post_image, new Callback() {
//                    @Override
//                    public void onSuccess() {
//
//                    }
//
//                    @Override
//                    public void onError(Exception e) {
//                        Picasso.get().load(model_profile_image).into(holder.user_post_image);
//                    }
//                });
//                Picasso.get().load(model.getPostimage()).networkPolicy(NetworkPolicy.OFFLINE).into(holder.postImage, new Callback() {
//                    @Override
//                    public void onSuccess() {
//
//                    }
//
//                    @Override
//                    public void onError(Exception e) {
//                        Picasso.get().load(model_post_image).into(holder.postImage);
//                    }
//                });
//                }
//                catch (Exception e){
//                    e.printStackTrace();
//                }
//                holder.itemView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        Intent ClickPostIntent = new Intent(MainActivity.this,OnCickPostActivity.class);
//                        ClickPostIntent.putExtra("PostKey",PostKey);
//                        startActivity(ClickPostIntent);
//                    }
//                });
//                holder.comment_button.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        Intent commentIntent = new Intent(MainActivity.this,CommentsActivity.class);
//                        commentIntent.putExtra("PostKey",PostKey);
//                        startActivity(commentIntent);
//                    }
//                });
//                holder.setLikebuttonStatus(PostKey);
//                holder.like_button.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        likeChecker = true;
//                        LikesRef.addValueEventListener(new ValueEventListener() {
//                            @Override
//                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                                if(likeChecker.equals(true)){
//                                    if(dataSnapshot.child(PostKey).hasChild(currentUserId)){
//                                        LikesRef.child(PostKey).child(currentUserId).removeValue();
//                                        likeChecker = false;
//                                    }
//                                    else{
//                                        LikesRef.child(PostKey).child(currentUserId).setValue(true);
//                                        likeChecker = false;
//                                    }
//                                }
//                            }
//
//                            @Override
//                            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                            }
//                        });
//                    }
//                });
//
//            }
//
//            @NonNull
//            @Override
//            public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.all_posts_layout,parent,false);
//                PostsViewHolder viewHolder=new PostsViewHolder(view);
//                return viewHolder;
//            }
//        };
//        postList.setAdapter(firebaseRecyclerAdapter);
//
//        firebaseRecyclerAdapter.startListening();
//
//    }
//    public static class PostsViewHolder extends RecyclerView.ViewHolder{
//        TextView username,date,time,description,noOflikes;
//        CircleImageView user_post_image;
//        ImageView postImage;
//        ImageButton like_button,comment_button;
//        DatabaseReference LikesRef;
//        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
//        int likescount = 0;
//
//        public PostsViewHolder(View itemView) {
//            super(itemView);
//
//            username=itemView.findViewById(R.id.post_user_name);
//            date=itemView.findViewById(R.id.post_date);
//            time=itemView.findViewById(R.id.post_time);
//            description=itemView.findViewById(R.id.post_retrieve_description);
//            postImage=itemView.findViewById(R.id.post_image);
//            user_post_image=itemView.findViewById(R.id.post_profile_image);
//            noOflikes = itemView.findViewById(R.id.no_of_likes_textView);
//            like_button = itemView.findViewById(R.id.dislike_button);
//            comment_button = itemView.findViewById(R.id.comment_button);
//            LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
//
//        }
//
//
//        public void setLikebuttonStatus(final String postKey) {
//            LikesRef.addValueEventListener(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                    if(dataSnapshot.child(postKey).hasChild(currentUserId)){
//                        likescount = (int)dataSnapshot.child(postKey).getChildrenCount();
//                        like_button.setImageResource(R.drawable.like);
//                        noOflikes.setText(Integer.toString(likescount) + " likes");
//                    }
//                    else{
//                        likescount = (int)dataSnapshot.child(postKey).getChildrenCount();
//                        like_button.setImageResource(R.drawable.dislike);
//                        noOflikes.setText(Integer.toString(likescount) + " likes");
//
//                    }
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                }
//            });
//
//        }
//    }


    private void SendUserToPostActivity() {
        Intent addNewPostIntent = new Intent(MainActivity.this,PostActivity.class);
        startActivity(addNewPostIntent);
        finish();
    }

    private void UserMenuSelector(MenuItem item){
        switch (item.getItemId()){

            case R.id.nav_add_new_post:
                SendUserToPostActivity();
                break;

            case R.id.nav_profile:
                SendUserToProfileActivity();
               break;
            case R.id.nav_home:
                Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_friends:
                SendUserToFriendsActivity();
                break;
            case R.id.nav_find_friends:
                SendUserToFindFriendsActivity();
                break;
            case R.id.nav_messages:
                Intent settingsIntent = new Intent(MainActivity.this,MainActivity.class);
                startActivity(settingsIntent);
                break;
            case R.id.nav_settings:
                SendUserToSettingsActivity();
                break;
            case R.id.nav_logout:
//                updateUserStatus("offline");
               mAuth.signOut();
               SendUserToLoginActivity();
                break;


        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser==null){
            SendUserToLoginActivity();
        }
        else{
            checkUserExistence();
        }
    }



    private void checkUserExistence() {
        final String current_user_id = mAuth.getCurrentUser().getUid();
        UserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(current_user_id)){
                    SendUserToSetupActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void SendUserToSetupActivity() {
        Intent setupIntent = new Intent(MainActivity.this,SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }

    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this,LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    private void SendUserToSettingsActivity() {
        Intent settingsIntent = new Intent(MainActivity.this,SettingsActivity.class);
        startActivity(settingsIntent);
        finish();
    }
    private void SendUserToProfileActivity() {
        Intent settingsIntent = new Intent(MainActivity.this,ProfileActivity.class);
        startActivity(settingsIntent);
        finish();
    }
    private void SendUserToFindFriendsActivity() {
        Intent findfriendsIntent = new Intent(MainActivity.this,FindFriendsActivity.class);
        startActivity(findfriendsIntent);
        finish();
    }
    private void SendUserToFriendsActivity() {
        Intent friendsIntent = new Intent(MainActivity.this,FriendsActivity.class);
        startActivity(friendsIntent);
        finish();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(actionBarDrawerToggle.onOptionsItemSelected(item))
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
