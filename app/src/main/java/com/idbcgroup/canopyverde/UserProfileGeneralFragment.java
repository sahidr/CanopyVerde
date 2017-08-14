package com.idbcgroup.canopyverde;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

public class UserProfileGeneralFragment extends Fragment {

    private EditText fullname, emailprofile, password,country, city;
    private SharedPreferences pref_session;

    public EditText[] getFields() {
        return new EditText[]{this.fullname,this.emailprofile,this.password,this.country,this.city};
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_profile_general, container, false);

        fullname = (EditText) view.findViewById(R.id.fullNameDisplayRow);
        emailprofile = (EditText) view.findViewById(R.id.emailDisplayRow);
        password = (EditText) view.findViewById(R.id.passwordDisplayRow);
        country = (EditText) view.findViewById(R.id.countryDisplayRow);
        city = (EditText) view.findViewById(R.id.cityDisplayRow);

        pref_session = this.getActivity().getSharedPreferences("Session", 0);
        String email = pref_session.getString("email",null);
        String fullnameText = pref_session.getString("fullname",null);

        fullname.setText(fullnameText);
        emailprofile.setText(email);

        return view;
    }
}
