package com.idbcgroup.canopyverde;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import java.util.ArrayList;

/**
 * This Class will receive an ArrayList and manage the objects of the array in a ListView
 * elements to the ListView and ListView's element corresponding.
 *
 * @link https://github.com/NgChiseng/TimePieceApp/blob/master/app/src/main/java/com/idbc/ngchiseng/timepieceapp/AnnouncesAdapter.java
 */
abstract class ListAdapter extends BaseAdapter {

    private final ArrayList<?> entries;
    private final int idListLayout;
    private final Context context;

    /**
     * Constructor for the ListAdapter class
     * @param context Base Context of the Parent Activity.
     * @param idListLayout id of the ListLayout that will content the elements.
     * @param entries Generic type ArraysList with the elements's data or information
     */
    ListAdapter(Context context, int idListLayout, ArrayList<?> entries){
        super();
        this.context = context;
        this.idListLayout = idListLayout;
        this.entries = entries;
    }

    /**
     * This method creates a row of the ListView
     * @param position position of the element in the ListView.
     * @param view the row associated with the layout
     * @param parent parent of the view
     * @return the row associated with the layout
     */
    @Override
    public View getView(int position, View view, ViewGroup parent){

        if(view == null){
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(idListLayout,null);
        }
        onEntry (entries.get(position), view);
        return view;
    }

    /**
     *  Method that will return the number of elements of the ArrayList
     * @return the count of entries in the ArrayList
     */
    @Override
    public int getCount(){
        return entries.size();
    }

    /**
     * Method that obtains the element associated with the position in the array
     * @param position of an element in the ArrayList.
     * @return the element in the ArrayList
     */
    @Override
    public Object getItem(int position){
        return entries.get(position);
    }

    /**
     * Method that obtains the element id associated with the position in the array
     * @param position of an element in the ArrayList.
     * @return the id of the element
     */
    @Override
    public long getItemId(int position) { return position;}

    /**
     * Abstract method taht will receive an Object and a View associated with a Layout
     * @param entry the Object associated with the ArrayList
     * @param view the view that contains the layout that will display the Objects
     */
    public abstract void onEntry(Object entry, View view);

}