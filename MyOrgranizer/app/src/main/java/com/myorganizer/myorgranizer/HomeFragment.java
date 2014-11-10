package com.myorganizer.myorgranizer;


import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.myorganizer.myorgranizer.JsTypes.*;
import static com.myorganizer.myorgranizer.GridAdapters.*;
import static com.myorganizer.myorgranizer.CABCallbacks.*;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HomeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment implements View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String TAG ="Home";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    //gridview adapter stuff
    GridView gridView;
    Button mButton;

    //json stuff
    HomeJson  homeJson;

    //select state/gridview
    SelectionManager mSm;
    String mParentContainerId;
    List<String> mObjectIds; //this is just all ids


    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public HomeFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        mButton = (Button) v.findViewById(R.id.up_one_level_butt);
        mButton.setVisibility(View.GONE);;
        mButton.setOnClickListener(this);
        //UpdateGridView("Null");
        mListener.PerformRequestPost("/WS_GetHouses",null,new GetHouse() );
        gridView = (GridView) v.findViewById(R.id.gridView);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View v, int position,
                                    long arg3) {
                Log.e(TAG, Integer.toString(position));

                if( mSm.state){
                    if(mSm.isSelected(mObjectIds.get(position))){
                        HighlightItem(v, getResources());
                        mSm.unSelect(mObjectIds.get(position), position);
                    }else{
                        mSm.select(mObjectIds.get(position), position);
                        HighlightItemPerm(v, getResources());
                    }
                }else{
                    if(position < mObjectIds.size()) { //item clicked is a container
                        //clean gridview and do a request
                        UpdateGridView(mObjectIds.get(position));
                    }else{
                        //gridItem clicked is an item
                        //launch the editItem fragment
                    }
                }
            }
        });
        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> arg0, View v,
                                           int position, long arg3) {
                Log.e(TAG, "long press");
                //set the image as wallpaper
                mSm.select(mObjectIds.get(position), position);
                HighlightItemPerm(v, getResources());

                if(mSm.actionMode == null) {
                    SelectedActionModeCallback callback = new SelectedActionModeCallback(
                            gridView,mSm,getResources());

                    mSm.actionMode = getActivity().startActionMode(callback);
                    return true;
                }
                return false;

            }
        });
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
    public void onClick(View v) {
        switch( v.getId()){
            case R.id.up_one_level_butt:
                UpdateGridView(mParentContainerId);
                break;
            default:
                Log.e(TAG, "no onclick registered");
        }
    }

    public void UpdateGridView(String ContainerId){

        if(ContainerId.equals("Null")) {
            gridView.setAdapter(new EmptyAdapter());
            mListener.PerformRequestPost("/WS_GetHouses", null, new GetHouse());
            mButton.setVisibility(View.GONE);
            return;
        }

        gridView.setAdapter(new EmptyAdapter());
        RequestParams r = new RequestParams();
        r.add("ContainerID", ContainerId);
        mButton.setVisibility(View.VISIBLE);
        mListener.PerformRequestPost("/WS_GetContainer", r, new GetContainer());
    }

    public  class GetHouse extends AsyncHttpResponseHandler{
        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] response) {
            Log.e(TAG, new String(response));

            try
            {
                homeJson= new ObjectMapper().readValue(response, HomeJson.class);
                Log.e(TAG,"done");
                mSm= new SelectionManager(homeJson.HouseIds.size() + homeJson.SemiShareIds.size() + homeJson.SemiShareIds.size());
                mObjectIds = new ArrayList<String>();
                mObjectIds.addAll(homeJson.HouseIds);
                mObjectIds.addAll(homeJson.SemiShareIds);
                mObjectIds.addAll(homeJson.SemiShareIds);
                gridView.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
                gridView.setAdapter(new HomeAdapter(homeJson, getActivity()));


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
        public void onFailure(int statusCode, Header[] headers, byte[] response, Throwable error) {
            Log.e(TAG, "There was a problem in retrieving the url : " + error.toString());
        }
    }

    public class GetContainer extends AsyncHttpResponseHandler{
        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] response) {
            Log.e(TAG, new String(response));
            try
            {
                ContainerJson containerJson = new ObjectMapper().readValue(response, ContainerJson.class);
                mObjectIds = new ArrayList<String>(containerJson.ContainerIds);
                mObjectIds.addAll(containerJson.ItemIds);
                mSm= new SelectionManager(containerJson.ContainerIds.size());
                gridView.setAdapter(new ContainerAdapter(containerJson, getActivity()));
                mParentContainerId = getParent(containerJson);
                Log.e(TAG,"done");
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
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            Log.e(TAG, "There was a problem in retrieving the url : " + error.toString());
        }
    }

    public String getParent(ContainerJson containerJson){
        int size = containerJson.PathIds.size();
        if(size>1) {
            return containerJson.PathIds.get(size - 2);
        }else{
            return "Null";
        }
    }
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public String getToken();
        public void ChangeFragment(Fragment f, boolean backstack);
        public void PerformRequestPost(String addr, RequestParams r, AsyncHttpResponseHandler h);
        public void PerformRequestGet(String addr, AsyncHttpResponseHandler h);
    }


}
