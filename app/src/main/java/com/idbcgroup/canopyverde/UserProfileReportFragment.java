package com.idbcgroup.canopyverde;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.apache.commons.lang3.text.WordUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class UserProfileReportFragment extends Fragment {

    private List<Report> reportList = new ArrayList<>();
    private RecyclerView recyclerView;
    private RecyclerView.Adapter<ReportAdapter.ReportViewHolder> reportAdapter;
    private RecyclerView.LayoutManager layoutManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View reportView = inflater.inflate(R.layout.fragment_user_profile_report, container, false);

        recyclerView = (RecyclerView) reportView.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        reportAdapter = new ReportAdapter(reportList);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        recyclerView.setAdapter(reportAdapter);
        prepareReportData();

        return reportView;
    }

    private void prepareReportData() {

        Report report;

        Calendar calendar = Calendar.getInstance();
        Date date = new Date(calendar.getTime().getTime());

        SimpleDateFormat df = new SimpleDateFormat("dd\nMMM yyy", Locale.US);
        String formattedDate = df.format(date);

        report = new Report(2,"Samán", "Av. Saman", formattedDate); //"26\nJUN 2017"
        reportList.add(report);

        report = new Report(2,"Caobo", "Av. 3", "15\nJUN 2017");
        reportList.add(report);

        report = new Report(1,"Araguaney", "Av. Ara", "14\nMAY 2017");
        reportList.add(report);

        report = new Report(2,"Apamate", "Calle 4", "13\nAPR 2017");
        reportList.add(report);

        report = new Report(1,"Roble", "Av. Yaracuy", "12\nMAR 2017");
        reportList.add(report);

        reportAdapter.notifyDataSetChanged();
    }

}
