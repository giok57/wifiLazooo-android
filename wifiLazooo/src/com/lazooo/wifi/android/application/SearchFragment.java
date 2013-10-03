package com.lazooo.wifi.android.application;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
        return inflater.inflate(R.layout.search_fragment, container, false);
    }


}
