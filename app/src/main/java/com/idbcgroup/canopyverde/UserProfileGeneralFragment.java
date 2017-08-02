package com.idbcgroup.canopyverde;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.EditText;

import org.w3c.dom.Text;

public class UserProfileGeneralFragment extends Fragment {

    private EditText fullname, emailprofile, password,country, city;
    private SharedPreferences pref_session;
    OnEditProfileInfo mCallback;

    public interface OnEditProfileInfo {
        public void onProfileChange(String name, String email, String password,
                                    String country, String city);
        //public void onEdit(boolean enable);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnEditProfileInfo) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
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
        String name = pref_session.getString("name",null);
        String email = pref_session.getString("email",null);
        fullname.setText(name);
        emailprofile.setText(email);
/*
        fullname.setEnabled(true);
        emailprofile.setEnabled(true);
        password.setEnabled(enable);
        country.setEnabled(enable);
        city.setEnabled(enable);

        if (enable) {
            mCallback.onProfileChange(fullname.getText().toString(), emailprofile.getText().toString(), password.getText().toString(),
                    country.getText().toString(), String.valueOf(city.getText()));
        }
        */
        return view;
    }



}
