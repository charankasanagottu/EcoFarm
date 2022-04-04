package com.example.ecofarm.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.ecofarm.databinding.ActivityPdfAddBinding;
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

public class PdfAddActivity extends AppCompatActivity {

    private ActivityPdfAddBinding binding;

    private FirebaseAuth firebaseAuth;

//    arraylist to hold pdf Categories
    private ArrayList<String> categoryTitleArrayList, categoryIdArrayList;

    //uri of picked pdf
    private Uri pdfUri=null;

    private static final int PDF_PICK_CODE=1000;

//    tag for debugin
    private static  final String TAG ="ADD_PDF_TAG";

    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();
        loadPdfCategories();
        
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //handle click, attach pdf
        binding.attachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pdfPickIntent();
            }
        });

//        handle click, pick category
        binding.categoryTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                categoryPickDialog();
            }
        });

        //handle click , upload pdf
        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //vaidate data
                validateData();
            }
        });
    }

    private String title="",description="";
    private void validateData() {
        Log.d(TAG, "validateData: validating data ...");
        
        //get data
        title = binding.titleEt.getText().toString().trim();
        description= binding.descriptionEt.getText().toString().trim();


        if(TextUtils.isEmpty(title)){
            Toast.makeText(this, "Enter Title ", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(description)){
            Toast.makeText(this, "Enter description", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(selectedCategoryTitle)){
            Toast.makeText(this, "Pick category", Toast.LENGTH_SHORT).show();
        }
        else if(pdfUri==null){
            Toast.makeText(this, "Pick Pdf..", Toast.LENGTH_SHORT).show();
        }
        else{
//            all data is validated
            upladPdfToStorage();
        }
    }

    private void upladPdfToStorage() {
//        upload Pdf to firebase
        Log.d(TAG, "upladPdfToStorage: uploading to storage..");

        //show progress
        progressDialog.setMessage("Uploading Pdf...");
        progressDialog.show();

        //timestamp
        long timestamp = System.currentTimeMillis();
        //firebase pdf path
        String filePathAndName = "Crops/"+timestamp;
        //storage reference
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
        storageReference.putFile(pdfUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d(TAG, "onSuccess: PDF uploaded to storage..");
                        Log.d(TAG, "onSuccess: getting pdf url");
//get pdf url
                        Task<Uri> uriTask= taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        String uploadedPdfUrl = ""+uriTask.getResult();

                        //upload to firebase db
                        uploadPdfInfoToDb(uploadedPdfUrl,timestamp);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Log.d(TAG, "onFailure: Pdf upload failed due to "+e.getMessage());
                        Toast.makeText(PdfAddActivity.this, "Pdf upload failed due to"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uploadPdfInfoToDb(String uploadedPdfUrl, long timestamp) {

        Log.d(TAG, "uploadPdfInfoToDb: uploading Pdf info to firebase db...");

        progressDialog.setMessage("Uploading Pdf info...");
        String uid= firebaseAuth.getUid();

        //setup data to upload
        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("uid",""+uid);
        hashMap.put("id",""+timestamp);
        hashMap.put("title",""+title);
        hashMap.put("description",""+description);
        hashMap.put("categoryId",""+selectedCategoryId);
        hashMap.put("url",""+uploadedPdfUrl);
        hashMap.put("timestamp",timestamp);

        //db reference Db > Crops
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Crops");
        ref.child(""+timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Log.d(TAG, "onSuccess: Successfuly uploaded ...");
                        Toast.makeText(PdfAddActivity.this, " Successfuly uploaded ...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Log.d(TAG, "onFailure: Failed to upload to db due to"+e.getMessage());
                        Toast.makeText(PdfAddActivity.this, "Failed to upload to db due to"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void loadPdfCategories() {
        Log.d(TAG,"loadPdfCategories: loading pdf categories...");
        categoryTitleArrayList= new ArrayList<>();
        categoryIdArrayList = new ArrayList<>();

//        db reference to load categories ... db> categories
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryTitleArrayList.clear();//clear data first
                categoryIdArrayList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){

                    //get id and title of category
                    String categoryId = " "+ds.child("id").getValue();
                    String categoryTitle = " "+ds.child("Crop").getValue();

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
        builder.setTitle("Pick Category")
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

    private void pdfPickIntent() {
        Log.d(TAG, "pdfPickIntent: starting pdf pick intent");
        Intent intent=new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select PDF"),PDF_PICK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            if(requestCode == PDF_PICK_CODE){
                Log.d(TAG, "onActivityResult: PDF picked");

                pdfUri= data.getData();

                Log.d(TAG,"onActivityResult: URI: "+pdfUri);
            }
        }
        else{
            Log.d(TAG,"onActivityResult: cancelled picking pdf");
            Toast.makeText(this, "Cancelled picking pdf", Toast.LENGTH_SHORT).show();

        }
    }
}
