package com.lazooo.wifi.android.application;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;

public class SearchFragment extends Fragment {

    private static SearchFragment instance;

    private SearchFragment() {
    }

    static SearchFragment getSearchFragment() {
        if (instance == null)
            return (instance = new SearchFragment());
        else
            return instance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.search_fragment, container, false);
        CardListAdapter mAdapter;
        mAdapter = new CardListAdapter(getActivity().getApplicationContext(), R.id.cardListView, new ArrayList<String>());

        final ListView list = (ListView) v.findViewById(R.id.cardListView);
        list.setAdapter(mAdapter);

        //Add Some Items in your list:
        for (int i = 1; i <= 10; i++) {
            mAdapter.add("Item " + i);
        }
        return v;
    }


}
