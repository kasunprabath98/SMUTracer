package com.smu.tracing;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

// supporting class for MainActivity Class
public class CustomAdapter extends BaseAdapter implements Filterable {
    Activity activity;
    List<UserModel> users;
    List<UserModel> originalUsers;
    LayoutInflater inflater;
    private CustomAdapter.AppsFilter filter;
    private PackageManager packageManager;

    public CustomAdapter(Activity activity) {
        this.activity = activity;
    }

    public CustomAdapter(Activity activity, List<UserModel> users) {
        this.activity   = activity;
        this.users      = users;
        this.originalUsers = users;
        inflater        = activity.getLayoutInflater();
    }

    @Override
    public int getCount() {
        return users.size();
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public Filter getFilter() {
        if (filter == null) {
            filter = new CustomAdapter.AppsFilter();
        }
        return filter;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        ViewHolder holder = null;
        if (view == null){
            view = inflater.inflate(R.layout.activity_get_class_lib, viewGroup, false);
            holder = new ViewHolder();
            holder.tvUserName = (TextView)view.findViewById(R.id.tv_user_name);
            holder.ivCheckBox = (ImageView) view.findViewById(R.id.iv_check_box);
            view.setTag(holder);

        } else {
            holder = (ViewHolder) view.getTag();
        }

        UserModel model = users.get(i);
        holder.tvUserName.setText(model.getUserName());

        if (model.isSelected())
            holder.ivCheckBox.setBackgroundResource(R.drawable.checked);
        else
            holder.ivCheckBox.setBackgroundResource(R.drawable.check);
        return view;
    }

    public void updateRecords(List<UserModel> users){
        this.users = users;
        notifyDataSetChanged();
    }

    public List<UserModel> getRecords(){
        return users;
    }

    class ViewHolder{
        TextView tvUserName;
        ImageView ivCheckBox;
    }

    private class AppsFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            if (constraint != null && constraint.length() > 0) {
                ArrayList<UserModel> filterList = new ArrayList<>();
                for (int i = 0; i < originalUsers.size(); i++) {
                    //if ((originalUsers.get(i).loadLabel(packageManager).toString().toUpperCase())
                    if ((originalUsers.get(i).userName.toString().toUpperCase())
                            .contains(constraint.toString().toUpperCase())) {
                        UserModel applicationInfo = originalUsers.get(i);
                        filterList.add(applicationInfo);
                    }
                }
                CustomAdapter.this.users = filterList;
                results.count = filterList.size();
                results.values = filterList;

            } else {
                results.count = originalUsers.size();
                results.values = originalUsers;
            }
            return results;

        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            users = (ArrayList<UserModel>) results.values;
            notifyDataSetChanged();
        }
    }
}
