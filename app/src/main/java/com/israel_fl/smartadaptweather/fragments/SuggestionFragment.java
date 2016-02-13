//package com.israel_fl.smartadaptweather.fragments;
//
//import android.content.Context;
//import android.net.Uri;
//import android.os.Bundle;
//import android.support.v4.app.Fragment;
//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import com.israel_fl.smartadaptweather.R;
//import com.israel_fl.smartadaptweather.controllers.SuggestionAdapter;
//import com.israel_fl.smartadaptweather.model.Suggestion;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class SuggestionFragment extends Fragment {
//
//    private static final String TAG = SuggestionFragment.class.getSimpleName();
//    private RecyclerView mRecyclerView;
//    private SuggestionAdapter mAdapter;
//    private ImageView suggestionIcon;
//    private TextView energyImpact;
//    private TextView costImpact;
//
//    public SuggestionFragment() {
//        // Required empty public constructor
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//    }
//
////    @Override
////    public View onCreateView(LayoutInflater inflater, ViewGroup container,
////                             Bundle savedInstanceState) {
////        // Inflate the layout for this fragment
////        //View layout = inflater.inflate(R.layout.fragment_suggestion, container, false);
////
////        // Set adapter
//////        mRecyclerView = (RecyclerView) layout.findViewById(R.id.suggestion_list);
//////        mAdapter = new SuggestionAdapter(getActivity(), getData());
//////        mRecyclerView.setAdapter(mAdapter);
//////        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
////
////        return layout;
////    }
//
//    private List<Suggestion> getData() {
//
//        List<Suggestion> suggestionList = new ArrayList<>();
//
//        int[] icons;
//        String[] suggestions = getResources().getStringArray(R.array.suggestions);
//
//        Suggestion suggestion = new Suggestion();
//        //suggestion.suggestionName = suggestions[i];
//
//        return suggestionList;
//    }
//
//}
