package com.example.socialmedia;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.storage.StorageManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {
    EditText Username,FullName,UserCountry;
    Button SaveInformationButton;
    CircleImageView ProfileImage;
    private FirebaseAuth mAuth;
    private DatabaseReference Userref;
    private ProgressDialog loadingBar;
    private StorageReference UserProfileImageRef;
    String currentUserId;
    //permission constant
    private static final int camera_request_code=100;
    private static final int storage_request_code=200;

    //imagePick constants
    private static final  int Image_pick_camera_code=300;
    private static  final int Image_pick_gallery_code=400;
    Uri images_uri=null;
    private StorageTask uploadTask;

    String[] cameraPermissions;
    String[] storagePermissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        mAuth = FirebaseAuth.getInstance();
        loadingBar = new ProgressDialog(this);
        currentUserId = mAuth.getCurrentUser().getUid();
        Userref = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId);
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("profileImages");
        Username = (EditText)findViewById(R.id.setup_username);
        FullName = (EditText)findViewById(R.id.setup_fullname);
        UserCountry = (EditText)findViewById(R.id.setup_country);
        SaveInformationButton = (Button)findViewById(R.id.setup_information_button);
        ProfileImage = (CircleImageView)findViewById(R.id.setup_profile_image);

        //permissions
        cameraPermissions=new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};


        SaveInformationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = Username.getText().toString();
                String fullname = FullName.getText().toString();
                String country = UserCountry.getText().toString();

                if (TextUtils.isEmpty(username)) {
                        Username.setFocusable(true);
                        Username.setError("Please Fill Username");
                }
                if (TextUtils.isEmpty(fullname)) {
                    Username.setFocusable(true);
                    Username.setError("Please Fill Fullname");
                }
                if (TextUtils.isEmpty(country)) {
                    Username.setFocusable(true);
                    Username.setError("Please Fill Country");
                }
                else {
                    if (images_uri == null) {
                        SaveAccountSetupInformation(username,fullname,country,"noImage");
                    } else {
                        SaveAccountSetupInformation(username,fullname,country,String.valueOf(images_uri));
                    }

                }
            }
        });
        ProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePickDialogBox();
            }
        });

    }



    private void showImagePickDialogBox() {

        String[] options ={"Camera","Gallery"};

        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Choose Image From");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which==0){
                    //camera
                    if (!checkCameraPermission()) {
                        requestCameraPermission();
                    }
                    else{
                        PickfromCamera();

                    }

                }
                if(which==1){
                    //gallery
                    if(!checkstoragePermission()){
                        requestStoragePermission();
                    }
                    else {
                        pickFromGallery();
                    }

                }

            }
        });
        builder.create().show();

    }

    private void pickFromGallery() {
        Intent intent =new Intent(Intent.ACTION_PICK);
        intent.setType("image/");
        startActivityForResult(intent,Image_pick_gallery_code);
    }

    private void PickfromCamera() {
        ContentValues cv= new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE,"Temp Pick");
        cv.put(MediaStore.Images.Media.DESCRIPTION,"Temp Description");
        images_uri=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,cv);

        Intent intent =new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,images_uri);
        startActivityForResult(intent,Image_pick_camera_code);
    }

    private boolean checkstoragePermission(){
        boolean result= ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)==(
                PackageManager.PERMISSION_GRANTED);
        return result;

    }

    private  void requestStoragePermission(){
        ActivityCompat.requestPermissions(this,storagePermissions,storage_request_code);

    }

    private boolean checkCameraPermission(){
        boolean result= ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)==(
                PackageManager.PERMISSION_GRANTED);
        boolean result1= ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)==(
                PackageManager.PERMISSION_GRANTED);
        return result && result1;

    }

    private  void requestCameraPermission(){
        ActivityCompat.requestPermissions(this,cameraPermissions,camera_request_code);

    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // goto previous activity
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case camera_request_code :{
                if(grantResults.length>0){
                    boolean cameraAccepted=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted=grantResults[1]==PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted && storageAccepted){
                        PickfromCamera();
                    }
                    else{
                        Toast.makeText(this,"Camera and Storage Permission both are necessary...",Toast.LENGTH_SHORT).show();
                    }

                }
                else {

                }

            }
            break;
            case storage_request_code:{
                if(grantResults.length>0){
                    boolean storageAccepted=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    if(storageAccepted){
                        pickFromGallery();
                    }
                    else{
                        Toast.makeText(this,"Storage permission is necessary...",Toast.LENGTH_SHORT).show();
                    }

                }
                else{

                }

            }
            break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(resultCode==RESULT_OK){
            if(requestCode==Image_pick_gallery_code){

                images_uri=data.getData();

                Picasso.get().load(images_uri.toString()).into(ProfileImage);

            }
            else if(requestCode ==Image_pick_camera_code){
                Log.i("error---",images_uri.toString());
                Picasso.get().load(images_uri.toString()).into(ProfileImage);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }



    private void SaveAccountSetupInformation(final String username,final String fullname,final String country,String uri) {

            loadingBar.setTitle("Saving Information");
            loadingBar.setMessage("Please wait while we save your account");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

        if(!uri.equals("noImage")){
            Bitmap bmp = null;
            try {
                bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), images_uri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            //here you can choose quality factor in third parameter(ex. i choosen 25)
            bmp.compress(Bitmap.CompressFormat.JPEG, 25, baos);
            byte[] fileInBytes = baos.toByteArray();
            final StorageReference ref= FirebaseStorage.getInstance().getReference().child("UsersImages").child(mAuth.getCurrentUser().getUid());
            uploadTask =ref.putBytes(fileInBytes);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if(!task.isSuccessful()){
                        throw  task.getException();
                    }
                    else{
                        return  ref.getDownloadUrl();
                    }
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        String mUri = downloadUri.toString();
                            HashMap userMap = new HashMap();
                            userMap.put("username", username);
                            userMap.put("fullname", fullname);
                            userMap.put("country", country);
                            userMap.put("status", "");
                            userMap.put("gender", "");
                            userMap.put("dob", "");
                            userMap.put("relationship_status", "");
                            userMap.put("profileimage", mUri);

                        Userref.setValue(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                SendUserToMainActivity();
                                Toast.makeText(getApplicationContext(), "Your Account is created successfully", Toast.LENGTH_LONG).show();
                                loadingBar.dismiss();
                                ProfileImage.setImageURI(null);
                                Username.setText("");
                                FullName.setText("");
                                UserCountry.setText("");
                                images_uri =null;

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                loadingBar.dismiss();
                                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_SHORT).show();
                    }
                }

                    });
            }
        else{
            HashMap userMap = new HashMap();
            userMap.put("username", username);
            userMap.put("fullname", fullname);
            userMap.put("country", country);
            userMap.put("status", "");
            userMap.put("gender", "");
            userMap.put("dob", "");
            userMap.put("relationship_status", "");
            userMap.put("profileimage","noImage");

            Userref.setValue(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    SendUserToMainActivity();
                    Toast.makeText(getApplicationContext(), "Your Account is created successfully", Toast.LENGTH_LONG).show();
                    loadingBar.dismiss();
                    ProfileImage.setImageResource(R.drawable.profile);
                    Username.setText("");
                    FullName.setText("");
                    UserCountry.setText("");
                    images_uri =null;

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    loadingBar.dismiss();
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }

        }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(SetupActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
