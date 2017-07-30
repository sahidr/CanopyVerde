package com.idbcgroup.canopyverde;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.style.TypefaceSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class TourActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private ImageView page0;
    private ImageView page1;
    private ImageView page2;
    private ImageView page3;
    private ImageView[] dots;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tour);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/TitilliumWeb-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        page0 = (ImageView) findViewById(R.id.page0);
        page1 = (ImageView) findViewById(R.id.page1);
        page2 = (ImageView) findViewById(R.id.page2);
        page3 = (ImageView) findViewById(R.id.page3);
        dots = new ImageView[]{page0, page1, page2,page3};

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        //mViewPager.setBackground(getDrawable(R.drawable.bg_tour));
        mViewPager.setAdapter(mSectionsPagerAdapter);
        //  mViewPager.setBackgroundColor(getResources().getColor(R.color.colorTransparent));
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                for (int i = 0; i < dots.length; i++) {
                    dots[i].setImageResource(
                            i == position ? R.drawable.dot_active : R.drawable.dot_inactive
                    );
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        private ImageView img;
        private TextView title;
        private TextView description;
        private Integer[] tour_imgs = {R.drawable.tour_01, R.drawable.tour_02,
                R.drawable.tour_03, R.drawable.tour_04};
        private Integer[] tour_titles = {R.string.title_tour_1, R.string.title_tour_2,
                R.string.title_tour_3, R.string.title_tour_4};
        private Integer[] tour_desc = {R.string.desc_tour_1, R.string.desc_tour_2,
                R.string.desc_tour_3, R.string.desc_tour_4};

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            /*
            * Library used to change the font family dynamically in the fragments
            * */
            CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                    .setDefaultFontPath("fonts/TitilliumWeb-Regular.ttf")
                    .setFontAttrId(R.attr.fontPath)
                    .build()
            );


            View rootView = inflater.inflate(R.layout.fragment_tour, container, false);

            Integer argSectionNumber = getArguments().getInt(ARG_SECTION_NUMBER) - 1;

            img = (ImageView) rootView.findViewById(R.id.app_img);
            img.setBackgroundResource(tour_imgs[argSectionNumber]);

            title = (TextView) rootView.findViewById(R.id.titleTour);
            title.setText(getString(tour_titles[argSectionNumber]));

            description = (TextView) rootView.findViewById(R.id.description);
            description.setText(getString(tour_desc[argSectionNumber]));

            return rootView;
        }
    }

    /*
    * Class to manage the position of the fragment
    * */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }
        @Override
        public int getCount() {
            return 4;
        }
    }

    /*
    * End of the tour
    * */
    public void toRegister(View view){
        SharedPreferences.Editor editor = getSharedPreferences("Tour", 0).edit();
        editor.putBoolean("visited", true);
        editor.apply();
        startActivity(new Intent(TourActivity.this, LoginActivity.class) // To Register
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        finish();
    }

    /*
    * Base Context for fontPath
    * */
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

}