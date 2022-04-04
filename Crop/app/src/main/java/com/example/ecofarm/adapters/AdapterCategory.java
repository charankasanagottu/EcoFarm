package com.example.ecofarm.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecofarm.activities.PdfListAdminActivity;
import com.example.ecofarm.filters.FilterCategory;
import com.example.ecofarm.models.ModelCategory;
import com.example.ecofarm.databinding.RowCategoryBinding;
import com.example.ecofarm.models.ModelCategoryAdmin;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class AdapterCategory extends RecyclerView.Adapter<AdapterCategory.HolderCategory> implements Filterable {

    private Context context;
    public ArrayList<ModelCategoryAdmin> categoryArrayList,filterList;

    //view binding
    private RowCategoryBinding binding;

    //instance of our filter class
    private FilterCategory filter;


    public AdapterCategory(Context context, ArrayList<ModelCategoryAdmin> categoryArrayList) {
        this.context = context;
        this.categoryArrayList = categoryArrayList;
        this.filterList = filterList;
    }

    @NonNull
    @Override
    public HolderCategory onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //bind row_category.xml
        binding = RowCategoryBinding.inflate(LayoutInflater.from(context),parent,false);

        return new HolderCategory(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderCategory holder, int position) {
//        get data
        ModelCategoryAdmin model = categoryArrayList.get(position);
        String id= model.getId();
        String crop =model.getCrop();
        String uid=model.getUid();
        long timestamp= model.getTimestamp();
        String benchmark = model.getBenchmark();

        //set data
        holder.categoryTv.setText(crop);

        //delete category
        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //confirm delete dialog
                AlertDialog.Builder builder=new AlertDialog.Builder(context);
                builder.setTitle("Delete")
                        .setMessage("Are you sure , you want to delete this crop values?")
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(context, "Deleting...", Toast.LENGTH_SHORT).show();
                                deleteCategory(model,holder);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });

        //handle item click , goto PdfListAdminActivity , also pass pdf categoryId
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PdfListAdminActivity.class);
                intent.putExtra("categoryId",id);
                intent.putExtra("categoryTitle",crop);
                context.startActivity(intent);
            }
        });
    }

    private void deleteCategory(ModelCategoryAdmin model, HolderCategory holder) {
        String id=model.getId();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.child(id)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //deleted successfully
                        Toast.makeText(context, "Deleted Successfully...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed to delte value
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public int getItemCount() {
        int category;
        try {
            category = categoryArrayList.size();

        } catch (NullPointerException e) {
            category = 0;
        }
        return category;
    }

    @Override
    public Filter getFilter() {
        if(filter==null){
            filter= new FilterCategory(filterList,this);
        }
        return filter;
    }

    /*View holder class to hold UI view for row_category.xml*/
    class HolderCategory extends RecyclerView.ViewHolder{
        TextView categoryTv;
        ImageButton deleteBtn;
        public HolderCategory(View itemView){
            super(itemView);

            //init ui Views
            categoryTv=binding.categoryTv;
            deleteBtn = binding.deleteBtn;

        }

    }
}
