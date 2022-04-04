package com.example.ecofarm.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import com.example.ecofarm.activities.MainActivity;
import com.example.ecofarm.adapters.AdapterCategory;
import com.example.ecofarm.databinding.ActivityDashboardAdminBinding;

import com.example.ecofarm.models.ModelCategoryAdmin;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DashboardAdminActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private ActivityDashboardAdminBinding binding;
    private ProgressDialog progressDialog;

    private ArrayList<ModelCategoryAdmin> categoryArrayList;

    private AdapterCategory adapterCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();
        loadCategories();

        progressDialog = new ProgressDialog(this);

        binding.prfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardAdminActivity.this,ProfileActivity.class));
            }
        });
        //edit text change listen, search
        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //called as and when user typee each letter
                try {
                    adapterCategory.getFilter().filter(s);
                }
                catch (Exception e) {

                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //handle click , logout
        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.signOut();
                checkUser();
            }
        });

        //hand click, start category add screen
        binding.addCategoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardAdminActivity.this,CategoryAddActivity.class));
            }
        });
        binding.addPdfFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardAdminActivity.this,PdfAddActivity.class));
            }
        });
    }

    private void loadCategories() {
        //init araylist
        categoryArrayList= new ArrayList<>();

//        get all categories from firebase->cateogry
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //clear arraylist before adding the data into it
                categoryArrayList.clear();
                for(DataSnapshot ds:snapshot.getChildren()){
                    //get data
                    ModelCategoryAdmin model= ds.getValue(ModelCategoryAdmin.class);

                    //add to array list
                    categoryArrayList.add(model);
                }
                //setup adapter
                adapterCategory= new AdapterCategory(DashboardAdminActivity.this,categoryArrayList);
                //set adapter to recycleview
                binding.categoriesRv.setAdapter(adapterCategory);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkUser(){
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser==null){
            //not logged in  , goto main screen
            startActivity(new Intent(DashboardAdminActivity.this, MainActivity.class));
            finish();
        }
        else{
            //logged in
            String email= firebaseUser.getEmail();
            //set in textview of toolbar
            binding.subTitleTv.setText(email);

        }
    }
}