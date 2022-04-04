package com.example.ecofarm.filters;

import android.widget.Filter;

import com.example.ecofarm.adapters.AdapterCategory;
import com.example.ecofarm.adapters.AdapterPdfAdmin;
import com.example.ecofarm.models.ModelPdf;

import java.util.ArrayList;

public class FilterPdfAdmin extends Filter {
    //arraylist in which we search
    ArrayList<ModelPdf> filterList;

    //adapter in  which filtering need to be implemented
    AdapterPdfAdmin adapterPdfAdmin;

    //constructor
    public FilterPdfAdmin(ArrayList<ModelPdf> filterList, AdapterPdfAdmin adapterPdfAdmin) {
        this.filterList = filterList;
        this.adapterPdfAdmin = adapterPdfAdmin;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        //value should not be null or empty
        if(constraint !=null && constraint.length()>0)
        {
            //change to upper case
            constraint =constraint.toString().toUpperCase();
            ArrayList<ModelPdf> filtermodels= new ArrayList<>();

                for(int i=0;i<filterList.size();i++)
                {
                    if (filterList.get(i).getTitle().toUpperCase().contains(constraint)) {
                        //add to filtered list
                        filtermodels.add(filterList.get(i));

                    }
                }

                results.count=filtermodels.size();
                results.values=filtermodels;
            }
        else
        {
            results.count=filterList.size();
            results.values=filterList;
        }

        return results; //dont miss it
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        //apply filter changes
        adapterPdfAdmin.pdfArrayList = (ArrayList<ModelPdf>)results.values;

        //notify changes
        adapterPdfAdmin.notifyDataSetChanged();
    }
}
