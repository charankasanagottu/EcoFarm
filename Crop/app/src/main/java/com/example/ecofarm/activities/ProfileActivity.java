package com.example.ecofarm.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.ecofarm.MyApplication;
import com.example.ecofarm.R;
import com.example.ecofarm.databinding.ActivityProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    /*view binding*/
    private ActivityProfileBinding binding;

    /*firebase auth for loading use data using uid*/
    private FirebaseAuth firebaseAuth;

    private static final String  TAG ="PROFILE_TAG";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        /*setup firebase auth*/
        firebaseAuth = FirebaseAuth.getInstance();
        loadUserInfo();

        /*handle click , start profile edit page */
        binding.prfileEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, ProfileEditActivity.class));
            }
        });

        /*handle click, goback */
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void loadUserInfo() {
        /**/
        Log.d(TAG, "loadUserInfo: loading user info...");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
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

                        //format date to dd/mm/yyyy
                        String formattedDate = MyApplication.formatTimestamp(Long.parseLong(timestamp));

                        /*set data to UI*/
                        binding.emailTv.setText(email);
                        binding.nameTv.setText(name);
                        binding.memberDateTv.setText(formattedDate);
                        binding.accountTypeTv.setText(userType);
                        binding.soilTypeTv.setText(soilType);

                        /*setup image, using glide*/
                        Glide.with(ProfileActivity.this)
                                .load(Profile_Image)
                                .placeholder(R.drawable.ic_person)
                                .into(binding.profileIv);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}