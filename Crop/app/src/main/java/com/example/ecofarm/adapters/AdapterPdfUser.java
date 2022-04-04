package com.example.ecofarm.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.ecofarm.MyApplication;
import com.example.ecofarm.PdfDetailActivity;
import com.example.ecofarm.databinding.RowPdfUserBinding;
import com.example.ecofarm.filters.FilterPdfUser;
import com.example.ecofarm.models.ModelPdf;
import com.github.barteksc.pdfviewer.PDFView;

import java.util.ArrayList;

public class AdapterPdfUser extends RecyclerView.Adapter<AdapterPdfUser.HolderPdfUser> implements Filterable {

    private Context context;
    public ArrayList<ModelPdf> pdfArrayList,filterList;
    private FilterPdfUser filter;

    private RowPdfUserBinding binding;

    private static final String TAG= "ADAPTER_PDF_USER_TAG";

    public AdapterPdfUser(Context context, ArrayList<ModelPdf> pdfArrayList) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
        this.filterList = pdfArrayList;
    }

    @Override
    public HolderPdfUser onCreateViewHolder( ViewGroup parent, int viewType) {
        //bind the view
        binding = RowPdfUserBinding.inflate(LayoutInflater.from(context),parent,false);

    return new HolderPdfUser(binding.getRoot());
    }

    @Override
    public void onBindViewHolder( HolderPdfUser holder, int position) {
        /*Get data , set data click etc..*/

        /*get data */
        ModelPdf model = pdfArrayList.get(position);
        String cropId = model.getId();
        String title = model.getTitle();
        String description = model.getDescription();
        String pdfUrl = model.getUrl();
        String categoryId = model.getCategoryId();
        long timestamp  = model.getTimestamp();

        /*convert time */
        String date = MyApplication.formatTimestamp(timestamp);

        /*set data*/
        holder.titleTv.setText(title);
        holder.descriptionTv.setText(description);
        holder.dateTv.setText(date);

        MyApplication.loadpdfFromSinglePage(
                ""+pdfUrl,
                ""+title,
                holder.pdfView,
                holder.progressBar);

        MyApplication.loadCategory(
                ""+categoryId,
                holder.categoryTv);

        MyApplication.loadPdfSize(
                ""+pdfUrl,
                ""+title,
                holder.sizeTv
        );

        /*holder click , show pdf details activity */
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PdfDetailActivity.class);
                intent.putExtra("cropId",cropId);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return pdfArrayList.size(); //returns size of pdf count
    }

    @Override
    public Filter getFilter() {
        if(filter==null){
            filter = new FilterPdfUser(filterList,this);
        }
        return filter;
    }

    class HolderPdfUser extends RecyclerView.ViewHolder{

        TextView titleTv,descriptionTv,categoryTv,sizeTv,dateTv;
        PDFView pdfView;
        ProgressBar progressBar;

        public HolderPdfUser(View itemView) {
            super(itemView);

            titleTv = binding.titleTv;
            descriptionTv = binding.descriptionTv;
            categoryTv= binding.categoryTv;
            sizeTv=binding.sizeTv;
            dateTv = binding.dateTv;
            pdfView = binding.pdfView;
            progressBar = binding.progressBar;
        }


    }
}
