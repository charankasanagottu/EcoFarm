package com.example.ecofarm;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ecofarm.adapters.AdapterPdfUser;
import com.example.ecofarm.databinding.ActivityPdfDetailsBinding;
import com.example.ecofarm.databinding.FragmentCropUserBinding;
import com.example.ecofarm.models.ModelPdf;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CropUserFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CropUserFragment extends Fragment {

//    that we passed which creating instance of this fragment
    private String categoryId;
    private String crop;
    private String uid;

    private ArrayList<ModelPdf> pdfArrayList;
    private AdapterPdfUser adapterPdfUser;

/*view binding*/
    private FragmentCropUserBinding binding;

    private static final String TAG="CROPS_USER_TAG";

    public CropUserFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static CropUserFragment newInstance(String categoryId, String crop,String uid) {
        CropUserFragment fragment = new CropUserFragment();
        Bundle args = new Bundle();
        args.putString("categoryId", categoryId);
        args.putString("crop", crop);
        args.putString("uid",uid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoryId = getArguments().getString("categoryId");
            crop = getArguments().getString("crop");
            uid = getArguments().getString("uid");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate/bind  the layout for this fragment
        binding =FragmentCropUserBinding.inflate(LayoutInflater.from(getContext()), container, false);

        Log.d(TAG, "onCreateView: Crop:"+crop);
        if(crop.equals("Recommend")){
            /*load Recommend crops*/
            loadRecommmendbooks();
        }
        else if(crop.equals("All")){
            /*load all crops*/
            loadAllbooks();
        }
        else if(crop.equals("Most Viewed")){
            /*load MostViewed crops*/
            loadMostViewedDownloadedbooks("viewsCount");
        }
        else if(crop.equals("Most Downloaded")) {
            /*load Most Downloaded  crops*/
            loadMostViewedDownloadedbooks("downloadsCount");
        }
        else{
            loadCategorizedCrops();
        }

//        search
        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    adapterPdfUser.getFilter().filter(s);
                }
                catch (Exception e){
                    Log.d(TAG, "onTextChanged: "+e.getMessage());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        return binding.getRoot();
    }

    private void loadCategorizedCrops() {
        pdfArrayList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Crops");
        ref.orderByChild("categoryId").equalTo(categoryId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        //clear list before starting adding data into it
                        pdfArrayList.clear();
                        for (DataSnapshot ds:snapshot.getChildren()){
//                    get data
                            ModelPdf model = ds.getValue(ModelPdf.class);
//                    add to list
                            pdfArrayList.add(model);

                        }
//                setup adapter
                        adapterPdfUser = new AdapterPdfUser(getContext(),pdfArrayList);
//                set adapter to recylcer view
                        binding.cropsRv.setAdapter(adapterPdfUser);

                    }
                    @Override
                    public void onCancelled(DatabaseError error) {

                    }
                });
    }

    private void loadRecommmendbooks() {
        pdfArrayList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Crops");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                //clear list before starting adding data into it
                pdfArrayList.clear();
                for (DataSnapshot ds:snapshot.getChildren()){
//                    get data
                    ModelPdf model = ds.getValue(ModelPdf.class);
//                    add to list
                    pdfArrayList.add(model);

                }
//                setup adapter
                adapterPdfUser = new AdapterPdfUser(getContext(),pdfArrayList);
//                set adapter to recycler view
                binding.cropsRv.setAdapter(adapterPdfUser);

            }
            @Override
            public void onCancelled(DatabaseError error) {

            }
        });

    }

    private void loadAllbooks() {
//        init list
        pdfArrayList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Crops");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                //clear list before starting adding data into it
                pdfArrayList.clear();
                for (DataSnapshot ds:snapshot.getChildren()){
//                    get data
                    ModelPdf model = ds.getValue(ModelPdf.class);
//                    add to list
                    pdfArrayList.add(model);

                }
//                setup adapter
                adapterPdfUser = new AdapterPdfUser(getContext(),pdfArrayList);
//                set adapter to recylcer view
                binding.cropsRv.setAdapter(adapterPdfUser);

            }
            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
    }

    private void loadMostViewedDownloadedbooks(String orderBy) {
        pdfArrayList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Crops");
        ref.orderByChild(orderBy).limitToLast(10) //load 10 most viewed or downloaded crops
        .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                //clear list before starting adding data into it
                pdfArrayList.clear();
                for (DataSnapshot ds:snapshot.getChildren()){
//                    get data
                    ModelPdf model = ds.getValue(ModelPdf.class);
//                    add to list
                    pdfArrayList.add(model);

                }
//                setup adapter
                adapterPdfUser = new AdapterPdfUser(getContext(),pdfArrayList);
//                set adapter to recylcer view
                binding.cropsRv.setAdapter(adapterPdfUser);

            }
            @Override
            public void onCancelled(DatabaseError error) {

            }
        });

    }
}