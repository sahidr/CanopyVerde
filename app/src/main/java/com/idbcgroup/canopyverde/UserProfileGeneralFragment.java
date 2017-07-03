package com.idbcgroup.canopyverde;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class UserProfileGeneralFragment extends Fragment {

    private TextView fullname, emailprofile;
    private SharedPreferences pref_session;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_profile_general, container, false);

        fullname = (TextView) view.findViewById(R.id.fullNameDisplayRow);
        emailprofile = (TextView) view.findViewById(R.id.emailDisplayRow);

        pref_session = this.getActivity().getSharedPreferences("Session", 0);
        String name = pref_session.getString("name",null);
        String email = pref_session.getString("email",null);

        fullname.setText(name);
        emailprofile.setText(email);

        return view;
    }

}
