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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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


    private void UserMenuSelector(MenuItem item){
        switch (item.getItemId()){

            case R.id.nav_add_new_post:
                SendUserToPostActivity();
                break;

            case R.id.nav_profile:
                SendUserToProfileActivity();
               break;
            case R.id.nav_home:
                SendUserToMainActivity();
                break;
            case R.id.nav_friends:
                SendUserToFriendsActivity();
                break;
            case R.id.nav_find_friends:
                SendUserToFindFriendsActivity();
                break;
            case R.id.nav_messages:
//
//                Fragment fragment = new ChatsFragment ();
//                FragmentManager fragmentManager = getSupportFragmentManager();
//                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//                fragmentTransaction.replace(R.id.main_container, fragment).addToBackStack(null);
//                fragmentTransaction.commit();
                SendUserToMainActivity();

                break;
            case R.id.nav_settings:
                SendUserToSettingsActivity();
                break;
            case R.id.nav_logout:
               mAuth.signOut();
                final HashMap<String,Object> isoffline=new HashMap<>();
                isoffline.put("online",false);
                isoffline.put("last_seen",System.currentTimeMillis());
                currentUserRef.updateChildren(isoffline);
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
    }
    private void SendUserToProfileActivity() {
        Intent settingsIntent = new Intent(MainActivity.this,ProfileActivity.class);
        startActivity(settingsIntent);
    }
    private void SendUserToFindFriendsActivity() {
        Intent findfriendsIntent = new Intent(MainActivity.this,FindFriendsActivity.class);
        startActivity(findfriendsIntent);
    }
    private void SendUserToFriendsActivity() {
        Intent friendsIntent = new Intent(MainActivity.this,FriendsActivity.class);
        startActivity(friendsIntent);
    }
    private void SendUserToPostActivity() {
        Intent addNewPostIntent = new Intent(MainActivity.this,PostActivity.class);
        startActivity(addNewPostIntent);
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(MainActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
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
