package com.idbcgroup.canopyverde;

import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Date;
import java.util.List;


public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {

    private List<Report> reportList;

    public static class ReportViewHolder extends RecyclerView.ViewHolder {
        // Campos respectivos de un item
        //public ImageView status;
        public TextView treeType;
        public TextView location;
        public TextView date;

        public ReportViewHolder(View v) {
            super(v);
            //status = (ImageView) v.findViewById(R.id.colorPoint);
            treeType = (TextView) v.findViewById(R.id.treeTypeDisplay);
            location = (TextView) v.findViewById(R.id.locationDisplay);
            date = (TextView) v.findViewById(R.id.dateDisplay);
        }
    }

    public ReportAdapter(List<Report> reportList) {
        this.reportList = reportList;
    }

    @Override
        public ReportViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.report_list_row, parent, false);
       return new ReportViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ReportViewHolder holder, int position) {
        Report report = reportList.get(position);
        //holder.status.setImageResource(android.R.drawable.presence_online);
        holder.treeType.setText(report.getTreeType());
        holder.location.setText(report.getLocation());
        String s= report.getDate();
        SpannableString ss1=  new SpannableString(s);
        ss1.setSpan(new RelativeSizeSpan(2f), 0,2, 0); // set size
        holder.date.setText(ss1);
        //holder.date.setText(report.getDate());

    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

}
