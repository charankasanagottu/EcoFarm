package com.example.ecofarm.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.ColorSpace;
import android.os.Bundle;
import android.view.Display;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.ecofarm.CropUserFragment;
import com.example.ecofarm.WeatherMainActivity;
import com.example.ecofarm.databinding.ActivityDashboarduserBinding;
import com.example.ecofarm.models.ModelCategory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DashboarduserActivity extends AppCompatActivity {

    /*to show in tabs*/
    public ArrayList<ModelCategory> categoryArrayList;
    public ViewPagerAdapter viewPagerAdapter;

    private ActivityDashboarduserBinding binding;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboarduserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();

        setupViewPageAdapter(binding.viewPager);
        binding.tableLayout.setupWithViewPager(binding.viewPager);

        progressDialog = new ProgressDialog(this);

        //handle click , logout
        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.signOut();
                checkUser();
            }
        });

        /*handle click , open profile */
        binding.prfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboarduserActivity.this, ProfileActivity.class));
            }
        });

        /*handle click button to check weather details*/
        binding.imageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboarduserActivity.this, WeatherMainActivity.class));
            }
        });
    }

    private void setupViewPageAdapter(ViewPager viewPager){
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(),FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,this);

        categoryArrayList = new ArrayList<>();

        /*load Categories from firebase*/
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories"); /*once again check spell from firebase*/
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange( DataSnapshot snapshot) {
                /*clear before adding to list*/
                categoryArrayList.clear();

                /*load categories - static eg , ALL,most viewd, downloaded..*/
                /*add data to models*/
                ModelCategory modelRecommend= new ModelCategory("01","Recommend","",1);
                ModelCategory modelAll= new ModelCategory("02","All","",1);
                ModelCategory modelMostViewed= new ModelCategory("03","Most Viewed","",1);
                ModelCategory modelMostDownloaded= new ModelCategory("04","Most Downloaded","",1);

                /*add models*/
                categoryArrayList.add(modelRecommend);
                categoryArrayList.add(modelAll);
                categoryArrayList.add(modelMostViewed);
                categoryArrayList.add(modelMostDownloaded);
                /*add data to view pager adapter */
                viewPagerAdapter.addFragment(CropUserFragment.newInstance(
                        ""+modelAll.getId(),
                        ""+modelAll.getCrop(),
                        ""+modelAll.getUid()
                ),modelAll.getCrop());

                viewPagerAdapter.addFragment(CropUserFragment.newInstance(
                        ""+modelRecommend.getId(),
                        ""+modelRecommend.getCrop(),
                        ""+modelRecommend.getUid()
                ),modelRecommend.getCrop());

                viewPagerAdapter.addFragment(CropUserFragment.newInstance(
                        ""+modelMostViewed.getId(),
                        ""+modelMostViewed.getCrop(),
                        ""+modelMostViewed.getUid()
                ),modelMostViewed.getCrop());

                viewPagerAdapter.addFragment(CropUserFragment.newInstance(
                        ""+modelMostDownloaded.getId(),
                        ""+modelMostDownloaded.getCrop(),
                        ""+modelMostDownloaded.getUid()
                ),modelMostDownloaded.getCrop());
                /*refresh list */
                viewPagerAdapter.notifyDataSetChanged();

                /*Now load from firebase*/
                for (DataSnapshot ds:snapshot.getChildren()){
                    /*get data */
                    ModelCategory model = ds.getValue(ModelCategory.class);
                    /*add category to list*/
                    categoryArrayList.add(model);
                    /*add data to view pager */
                    viewPagerAdapter.addFragment(CropUserFragment.newInstance(
                            ""+model.getId(),
                            ""+model.getCrop(),
                            ""+model.getUid()), model.getCrop());
                    /*refresh list */
                    viewPagerAdapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onCancelled( DatabaseError error) {

            }
        });

        /*set adapter to view pager*/
        viewPager.setAdapter(viewPagerAdapter);

    }

    public class ViewPagerAdapter extends FragmentPagerAdapter{

        private ArrayList<CropUserFragment> fragmentList = new ArrayList<>();
        private ArrayList<String> fragmentTitleList=new ArrayList<>();
        private Context context;

        public ViewPagerAdapter(FragmentManager fm, int behavior,Context context) {
            super(fm, behavior);
            this.context = context;
        }

        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        private void addFragment(CropUserFragment fragment,String title){
            /*add fragment as parameter in fragment list*/
            fragmentList.add(fragment);
            /*add titled passed as parameter in fragment list*/
            fragmentTitleList.add(title);
        }
        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitleList.get(position);
        }
    }

    private void checkUser(){
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser==null){
            //not logged in  , goto main screen
            startActivity(new Intent(DashboarduserActivity.this, MainActivity.class));
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