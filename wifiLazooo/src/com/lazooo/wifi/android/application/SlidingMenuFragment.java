package com.lazooo.wifi.android.application;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import java.util.ArrayList;
import java.util.List;

public class SlidingMenuFragment extends Fragment implements ExpandableListView.OnChildClickListener {

    private ExpandableListView sectionListView;

    //non modificare
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        List<Section> sectionList = createMenu();

        View view = inflater.inflate(R.layout.slidingmenu_fragment, container, false);
        this.sectionListView = (ExpandableListView) view.findViewById(R.id.slidingmenu_view);
        this.sectionListView.setGroupIndicator(null);

        SectionListAdapter sectionListAdapter = new SectionListAdapter(this.getActivity(), sectionList);
        this.sectionListView.setAdapter(sectionListAdapter);

        this.sectionListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                return true;
            }
        });

        this.sectionListView.setOnChildClickListener(this);

        int count = sectionListAdapter.getGroupCount();
        for (int position = 0; position < count; position++) {
            this.sectionListView.expandGroup(position);
        }

        return view;
    }

    //section è un contenitore di sectionItem, ed è il "capitolo".
    //Se si vuole aggiungere un elemento seguire gli esempi sottostanti
    private List<Section> createMenu() {
        List<Section> sectionList = new ArrayList<Section>();

        Section menuSections = new Section("General");
        menuSections.addSectionItem(101, "Log in", null);
        menuSections.addSectionItem(102, "Log out", null);

        Section oGeneralSection = new Section("Search");
        oGeneralSection.addSectionItem(201, getString(R.string.aroundme), "ic_pinpoint");
        oGeneralSection.addSectionItem(202, getString(R.string.map), "ic_map");
        oGeneralSection.addSectionItem(203, getString(R.string.search), "ic_search");
        oGeneralSection.addSectionItem(204, getString(R.string.myactivity), "ic_bookmark_on");

        sectionList.add(menuSections);
        sectionList.add(oGeneralSection);
        return sectionList;
    }


    //Gestisce gli eventi dei click sui bottoni
    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

        switch ((int) id) {
            case 101: //login
                Log.d("funge", String.valueOf(id));
                break;
            case 102: //logout
                Log.d("funge", String.valueOf(id));
                break;
            case 201: //around me
                Log.d("funge", String.valueOf(id));
                break;
            case 202: //map
                Log.d("funge", String.valueOf(id));
                break;
            case 203: //ricerca
                Log.d("funge", String.valueOf(id));
                break;
            case 204: //le mie attività
                Log.d("funge", String.valueOf(id));
                break;
        }

        return false;
    }
}
