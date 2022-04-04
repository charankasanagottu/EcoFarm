package com.example.ecofarm.filters;

import android.widget.Filter;

import com.example.ecofarm.adapters.AdapterCategory;
import com.example.ecofarm.models.ModelCategory;
import com.example.ecofarm.models.ModelCategoryAdmin;

import java.util.ArrayList;

public class FilterCategory extends Filter {
    //arraylist in which we search
    ArrayList<ModelCategoryAdmin> filterList;

    //adapter in  which filtering need to be implemented
    AdapterCategory adapterCategory;

    //constructor
    public FilterCategory(ArrayList<ModelCategoryAdmin> filterList, AdapterCategory adapterCategory) {
        this.filterList = filterList;
        this.adapterCategory = adapterCategory;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        //value should not be null or empty
        if(constraint !=null && constraint.length()>0)
        {
            //change to upper case
            constraint =constraint.toString().toUpperCase();
            ArrayList<ModelCategoryAdmin> filtermodels= new ArrayList<>();

                for(int i=0;i<filterList.size();i++)
                {
                    if (filterList.get(i).getCrop().toUpperCase().contains(constraint)) {
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
        adapterCategory.categoryArrayList = (ArrayList<ModelCategoryAdmin>) results.values;

        //notify changes
        adapterCategory.notifyDataSetChanged();
    }
}
