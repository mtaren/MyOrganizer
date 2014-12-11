package com.myorganizer.myorgranizer;

import android.app.Activity;

import android.app.Fragment;
import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
//import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.MenuItemCompat;
import android.text.Selection;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;


import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SearchView;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link searchFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link searchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class searchFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String GETOBJEDIT = "/WS_GetOneObjectAttributes";
    private String TAG = "search";
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private SearchView mSearchView;
    private GridView mGridView;

    private OnFragmentInteractionListener mListener;

    //for gridview/selectionManager
    SelectionManager mSm;
    List mObjectIds;
    JsTypes.ContainerJson mContainerJson;


    // TODO: Rename and change types and number of parameters
    public static searchFragment newInstance(String param1, String param2) {
        searchFragment fragment = new searchFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public searchFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_search, container, false);
        mGridView = (GridView) v.findViewById(R.id.gridView);
        this.setHasOptionsMenu(true);

        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
   public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.search, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        mSearchView = new SearchView( (getActivity()).getActionBar().getThemedContext());
        MenuItemCompat.setShowAsAction(item, MenuItemCompat.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW | MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
        MenuItemCompat.setActionView(item, mSearchView);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { //on submit search
                Log.e(TAG, query);
                mSearchView.clearFocus();
                SearchRequest(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // do nothing. autocomplete here later if you really want
                //every letter calls this callback
                return false;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    public void GetItemParams_Request(String ItemId){
        RequestParams params = new RequestParams();
        params.add("ObjId", ItemId);
        params.add("ObjType", "item");
        mListener.PerformRequestPost(GETOBJEDIT, params, new StartEditItemHandler());
    }


    public void SearchRequest(String searchStr){
        RequestParams r = new RequestParams();
        r.add("searchStr", searchStr);
        r.add("useName", "true");
        r.add("useDesc", "true");
        r.add("Category", "All");
        r.add("containerId", "Null");
        mListener.PerformRequestPost("/WS_Search", r,new  SearchHandler());

    }

    public  class SearchHandler extends AsyncHttpResponseHandler{
        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] response) {
            Log.e(TAG, new String(response));

            try {
                mContainerJson = new ObjectMapper().readValue(response, JsTypes.ContainerJson.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mObjectIds = new ArrayList<String>(mContainerJson.ContainerIds);
            mObjectIds.addAll(mContainerJson.ItemIds);
            mSm= new SelectionManager(mContainerJson.ContainerIds.size());
            mGridView.setAdapter(new GridAdapters.SearchAdapter(mContainerJson, getActivity()));
            Log.e(TAG,"done");
            mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    if(position < mContainerJson.ContainerIds.size()){
                        mListener.ChangePage(0);
                        mListener.HomeFragmentGetContainer(mContainerJson.ContainerIds.get(position));
                    }else{ //this is an item
                        GetItemParams_Request(mContainerJson
                                .ItemIds.get(position-mContainerJson.ContainerIds.size()));
                        Log.e(TAG, "clicked item");
                    }
                }
            });
        }
        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] response, Throwable error) {
            Log.e(TAG, "There was a problem in retrieving the url : " + error.toString());
        }
    }
    public class StartEditItemHandler extends AsyncHttpResponseHandler{
        private String TAG = "StartEdiitobjhandler";
        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] response) {
            Log.e(TAG, new String(response));
            //parse obj
            try
            {
                JsTypes.ItemObj it= new ObjectMapper().readValue(response, JsTypes.ItemObj.class);
                Log.e(TAG,"done");
                ItemFragment f = ItemFragment.newInstance(it.name, it.Desc, it.cat, it.qty,
                        it.ParentId, HomeActivity.domain + it.picUrl, it.Path, it.Id);
                mListener.ShowFragment(f);
            }
            catch (JsonParseException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] resp, Throwable error) {
            Log.e(TAG, "There was a problem in retrieving the url : " + error.toString());
        }
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void PerformRequestPost(String addr, RequestParams r, AsyncHttpResponseHandler h);
        public void ChangePage(int i);
        public void HomeFragmentGetContainer(String containerId);
        public void ShowFragment(Fragment f);
    }

}
