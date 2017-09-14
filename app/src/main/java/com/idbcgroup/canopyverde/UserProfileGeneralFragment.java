package com.idbcgroup.canopyverde;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

public class UserProfileGeneralFragment extends Fragment {

    private EditText fullname, email, password,country, city;

    /**
     * Gets the EditTexts of the view, with this method the Activity can obtain the texts
     * of the views
     * @return An array of EditText of the view
     */
    public EditText[] getFields() {
        return new EditText[]{this.fullname,this.email,this.password,this.country,this.city};
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_profile_general, container, false);

        fullname = (EditText) view.findViewById(R.id.fullNameDisplayRow);
        email = (EditText) view.findViewById(R.id.emailDisplayRow);
        password = (EditText) view.findViewById(R.id.passwordDisplayRow);
        country = (EditText) view.findViewById(R.id.countryDisplayRow);
        city = (EditText) view.findViewById(R.id.cityDisplayRow);

        SharedPreferences pref_session = this.getActivity().getSharedPreferences("Session", 0);
        String email_profile = pref_session.getString("email",null);
        String fullname_profile = pref_session.getString("fullname",null);
        String country_profile = pref_session.getString("country",getString(R.string.country));
        String city_profile = pref_session.getString("city",getString(R.string.city));

        fullname.setText(fullname_profile);

        email.setText(email_profile);
        country.setText(country_profile);
        city.setText(city_profile);

        return view;
    }
}
