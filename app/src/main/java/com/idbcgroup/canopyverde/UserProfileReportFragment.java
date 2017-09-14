package com.idbcgroup.canopyverde;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class UserProfileReportFragment extends Fragment {

    private ListView reportList;
    private int user_id;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        SharedPreferences pref_session = this.getActivity().getSharedPreferences("Session", 0);
        user_id = pref_session.getInt("id",0);
        // Inflate the layout for this fragment
        View reportView = inflater.inflate(R.layout.fragment_user_profile_report, container, false);
        reportList = (ListView) reportView.findViewById(R.id.report_list);

        GetReport g = new GetReport();
        g.execute();

        return reportView;
    }

    /**
     * Async Task who provides the user's reports of the app
     */
    private class GetReport extends AsyncTask<String, Integer, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... params) {
            JSONObject apiResponse = new JSONObject();
            URL url;
            HttpURLConnection urlConnection;

            try {
                url = new URL("https://canopy-verde.herokuapp.com/report/"+user_id+"/");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setConnectTimeout(10000);

                APIResponse response = JSONResponseController.getJsonResponse(urlConnection,false);

                if (response != null) {
                    if (response.getStatus() == HttpURLConnection.HTTP_OK) {
                        apiResponse.put("status",0);
                        apiResponse.put("body",response.getBodyArray());

                    } else if (response.getStatus() == HttpURLConnection.HTTP_BAD_REQUEST) {
                        apiResponse.put("status",1);
                        apiResponse.put("body",response.getBody());
                    } else if (response.getStatus() == HttpURLConnection.HTTP_NOT_FOUND) {
                        apiResponse.put("status",-1);
                        apiResponse.put("body",response.getBody());
                    } else {
                        apiResponse.put("status",-2);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return apiResponse;
        }

        // Process doInBackground() results
        @Override
        protected void onPostExecute(JSONObject response) {

            try {
                //Log.d("RESPONSE POST",String.valueOf(response.getInt("status")));
                ArrayList<Report> data = new ArrayList<>();
                if (response.getInt("status") == 0) {
                    JSONArray reportArray = response.getJSONArray("body");
                    JSONObject userReport;

                    for (int i = 0; i < reportArray.length(); i++) {
                        userReport = reportArray.getJSONObject(i);

                        int status = userReport.getInt("status");
                        String type = userReport.getString("type");
                        String location = userReport.getString("location");
                        Date date = java.sql.Date.valueOf(userReport.getString("date"));

                        data.add(0,new Report(status, type, location, date));
                    }

                    reportList.setAdapter(new ListAdapter(getActivity(), R.layout.report_list_row, data) {

                        /* This will implement the abstract method onEntry(Implemented in AnnounceAdapter), with
                        the respective elements and handlers.
                         */
                        @Override
                        public void onEntry(Object entry, View view) {

                            if (entry != null){

                                ImageView pointStatus = (ImageView) view.findViewById(R.id.pointStatus);
                                TextView reportType = (TextView) view.findViewById(R.id.treeTypeDisplay);
                                TextView reportLocation = (TextView) view.findViewById(R.id.locationDisplay);
                                TextView reportDay = (TextView) view.findViewById(R.id.dateDisplay);
                                TextView reportDate = (TextView) view.findViewById(R.id.monthDisplay);

                                int status = ((Report) entry).getStatus();
                                String type = ((Report) entry).getTreeType();
                                String location = ((Report) entry).getLocation();
                                Date date = ((Report) entry).getDate();

                                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy",Locale.US);
                                String date_formatted = dateFormat.format(date);

                                String[] dateParts = date_formatted.split("-");
                                String day = dateParts[0];
                                String month =  dateParts[1] + " " + dateParts[2];

                                if (status == 0) {
                                    pointStatus.setImageResource(R.drawable.p_rojo);
                                } else if (status == 1) {
                                    pointStatus.setImageResource(R.drawable.p_amarillo);
                                } else {
                                    pointStatus.setImageResource( R.drawable.p_verde);
                                }

                                reportType.setText(type);
                                reportLocation.setText(location);
                                reportDay.setText(day);
                                reportDate.setText(month);

                            }
                        }
                    });

                }
            }catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


}
