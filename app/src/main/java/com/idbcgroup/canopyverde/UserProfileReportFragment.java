package com.idbcgroup.canopyverde;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class UserProfileReportFragment extends Fragment {

    private List reportList = new ArrayList();
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View reportView = inflater.inflate(R.layout.fragment_user_profile_report, container, false);
        recyclerView = (RecyclerView) reportView.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new ReportAdapter(reportList);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        recyclerView.setAdapter(mAdapter);
        prepareReportData();

        return reportView;
    }

    private void prepareReportData() {

        Report report;

        report = new Report(2,"Samán", "Av. Saman", "16\nJUL 2017");
        reportList.add(report);

        report = new Report(2,"Caobo", "Av. 3", "15\nJUN 2017");
        reportList.add(report);

        report = new Report(1,"Araguaney", "Av. Ara", "14\nMAY 2017");
        reportList.add(report);

        report = new Report(2,"Apamate", "Calle 4", "13\nAPR 2017");
        reportList.add(report);

        report = new Report(1,"Roble", "Av. Yaracuy", "12\nMAR 2017");
        reportList.add(report);

        mAdapter.notifyDataSetChanged();
    }

}
