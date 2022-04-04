package com.example.ecofarm.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import com.bumptech.glide.Glide;
import com.example.ecofarm.R;
import com.example.ecofarm.databinding.ActivityProfileEditBinding;
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
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;

public class ProfileEditActivity extends AppCompatActivity {

    //binding
    private ActivityProfileEditBinding binding;

    /*firebase auth , get user details by Uid*/
    private FirebaseAuth firebaseAuth;

    private  static final String TAG = "PROFILE_EDIT_TAG";

    private ArrayList<String> categoryTitleArrayList, categoryIdArrayList;

    private Uri imageUri = null;

    private String name = "";
    private String soilType="";

    /*progress dialog */
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        loadUserInfo();
        loadPdfCategories();

        progressDialog =new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false); /*dont dismiss while clicking outside of progress bar */

        /*handle click, goback */
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        /*handle click, pick image*/
        binding.profileIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImageAttachMenu();
            }
        });

        /*handle click, update profile*/
        binding.updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });

        binding.categoryTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                categoryPickDialog();
            }
        });
    }

    private void loadPdfCategories() {
        Log.d(TAG,"loadPdfCategories: loading pdf categories...");
        categoryTitleArrayList= new ArrayList<>();
        categoryIdArrayList = new ArrayList<>();

//        db reference to load categories ... db> categories
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryTitleArrayList.clear();//clear data first
                categoryIdArrayList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){

                    //get id and title of category
                    String categoryId = " "+ds.child("id").getValue();
                    String categoryTitle = " "+ds.child("soiltype").getValue();

                    //add to resepctive a.add(categoryTitle);
                    categoryTitleArrayList.add(categoryTitle);
                    categoryIdArrayList.add(categoryId);

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    //select category id and category title
    private String selectedCategoryId, selectedCategoryTitle;
    private void categoryPickDialog() {
//        first  we need to get categories from firebase
        Log.d(TAG, "categoryPickDialog: showing category pick dialog");

        //get string array of categories from a arraylist
        String[] categoriesArray = new String[categoryTitleArrayList.size()];

        for (int i=0;i<categoryTitleArrayList.size();i++){
            categoriesArray[i]= categoryTitleArrayList.get(i);
        }

        //alert dialog
        AlertDialog.Builder builder= new AlertDialog.Builder(this);
        builder.setTitle("Pick Soil")
                .setItems(categoriesArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        handle items click
                        //get clicked item from list
                        selectedCategoryTitle  = categoryTitleArrayList.get(which);
                        selectedCategoryId = categoryIdArrayList.get(which);
                        //set to category textview
                        binding.categoryTv.setText(selectedCategoryTitle);

                        Log.d(TAG, "onClick: Selected Category:"+selectedCategoryId+""+selectedCategoryTitle);
                    }
                })
                .show();
    }

    private void loadUserInfo() {
        /**/
        Log.d(TAG, "loadUserInfo: loading user info...");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        /*get all info of user here from snapshot */
                        String email = ""+snapshot.child("email").getValue();
                        String name= ""+snapshot.child("name").getValue();
                        String Profile_Image = ""+snapshot.child("Profile Image").getValue();
                        String timestamp = ""+snapshot.child("timestamp").getValue();
                        String uid = ""+snapshot.child("uid").getValue();
                        String userType = ""+snapshot.child("userType").getValue();
                        String soilType = ""+snapshot.child("soiltype").getValue();

                        /*set data to UI*/
                        binding.nameEt.setText(name);


                        /*setup image, using glide*/
                        Glide.with(ProfileEditActivity.this)
                                .load(Profile_Image)
                                .placeholder(R.drawable.ic_person)
                                .into(binding.profileIv);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void validateData() {

        /*get data */
        name = binding.nameEt.getText().toString().trim();
        soilType = binding.categoryTv.getText().toString().trim();
        /*validate data*/
        if(TextUtils.isEmpty(name)){
            /*no name is Entered*/
            Toast.makeText(this, "Enter name...", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(soilType)){
            Toast.makeText(this,"soilType...",Toast.LENGTH_SHORT).show();
        }
        else{
            /*name is entered*/
            if (imageUri==null){
                /*need to update without image*/
                updateProfile("");
            }
            else{
                /*need to update with image*/
                uploadImage();
            }
        }

    }

    private void uploadImage() {
        Log.d(TAG, "uploadImage: Uploading profile image...");
        progressDialog.setMessage("Updating profile image");
        progressDialog.show();

        /*image path name, use uid to replace previous*/
        String filePathAndName = "ProfileImages/"+firebaseAuth.getUid();

        /*storage reference */
        StorageReference reference = FirebaseStorage.getInstance().getReference(filePathAndName);
        reference.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d(TAG, "onSuccess: Profile image uploaded");
                        Log.d(TAG, "onSuccess: Getting url of uploaded image");
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while(!uriTask.isSuccessful());
                        String uploadedImageUrl = ""+uriTask.getResult();

                        Log.d(TAG, "onSuccess: Uploaded image Url"+uploadedImageUrl);
                        updateProfile(uploadedImageUrl);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: Failed to upload image due to"+e.getMessage());
                        progressDialog.dismiss();
                        Toast.makeText(ProfileEditActivity.this, "Failed to upload image due to"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateProfile(String imageUrl) {
        Log.d(TAG, "updateProfile: Updating user profile");
        progressDialog.setMessage("Updating user profile");
        progressDialog.show();

        /*setup data to update in db */
        HashMap<String,Object> hashMap= new HashMap<>();
        hashMap.put("name",""+name);
        hashMap.put("soiltype",""+soilType);
        if (imageUri!=null){
            hashMap.put("Profile Image",""+imageUrl);
        }

        /*update data to db*/
        DatabaseReference databaseReference=  FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(firebaseAuth.getUid())
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: Profile updated");
                        progressDialog.dismiss();
                        Toast.makeText(ProfileEditActivity.this, "Profile updated...", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: Failed to update db due to"+e.getMessage());
                        progressDialog.dismiss();
                        Toast.makeText(ProfileEditActivity.this, "Failed to update db due to"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void showImageAttachMenu() {
        /*init setup popup menu  */
        PopupMenu popupMenu = new PopupMenu(this,binding.profileIv);
        popupMenu.getMenu().add(Menu.NONE,0,0,"Camera");
        popupMenu.getMenu().add(Menu.NONE,1,1,"Gallery");

        popupMenu.show();

        /*handle menu item clicks */
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                /*get id of item clicked */
                int which = item.getItemId();
                if(which==0){
                    /*camera clicked*/
                    pickImageCamera();
                }
                else if(which==1) {
                    /*gallery clicked*/
                    pickImageGallery();
                }

                return false;
            }
        });
    }

    private void pickImageCamera() {
        /*intent to pick image from camera*/
        ContentValues values= new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"New Pick");
        values.put(MediaStore.Images.Media.DESCRIPTION,"Sample Image Description");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
        cameraActivityResultLauncher.launch(intent);
    }

    private void pickImageGallery() {
    /*intent to pick image from gallery */
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryActivityResultLauncher.launch(intent);

    }

    private ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    /*used to handle result of camera intent */
                    /*get Uri of image*/
                    if(result.getResultCode()== Activity.RESULT_OK){
                        Log.d(TAG, "onActivityResult: Picked From Camera"+imageUri);
                        Intent data = result.getData(); /*no need here as in camera csae we already have image in image uri Variable */

                        binding.profileIv.setImageURI(imageUri);
                    }
                    else{
                        Toast.makeText(ProfileEditActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
                    }
                }
            }

    );

    private ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    /*used to handle result of gallery intent */
                    /*get Uri of image*/
                    if(result.getResultCode()== Activity.RESULT_OK){
                        Log.d(TAG, "onActivityResult: "+imageUri);
                        Intent data = result.getData();
                        imageUri = data.getData();
                        Log.d(TAG, "onActivityResult: Picked From Gallery"+imageUri);

                    }
                }
            }

    );
}