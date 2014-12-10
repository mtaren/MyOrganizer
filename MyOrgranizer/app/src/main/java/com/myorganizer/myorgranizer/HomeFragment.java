package com.myorganizer.myorgranizer;


import android.app.Activity;
import android.app.Fragment;
import android.content.ClipData;
import android.content.Intent;
import android.os.Bundle;

import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
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

import static com.myorganizer.myorgranizer.DialogClasses.DialogResult;
import static com.myorganizer.myorgranizer.DialogClasses.NAMECODE;
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
    public static final int SEARCHTOHOME = 10;
    public static final int REDRAWGRID = 11;


    //gridview adapter stuff
    GridView gridView;
    Button mButton;
    String mPathStr;

    //json stuff
    HomeJson  homeJson; //dont need to queu this

    BaseAdapter mAdapter; //used to queue adapters other wise stuff crashes


    //select state/gridview
    SelectionManager mSm;
    String mParentContainerId;
    String mCurrentContainerId = "Null";
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
        if (savedInstanceState != null) {
            Log.e(TAG, mCurrentContainerId);
            Log.e(TAG, mParentContainerId);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        this.setHasOptionsMenu(true);
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
                            gridView,mSm,getResources(), getActivity(), mCurrentContainerId);

                    mSm.actionMode = getActivity().startActionMode(callback);
                    return true;
                }
                return false;

            }
        });
        return v;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.home, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        Log.e(TAG,mCurrentContainerId);
        switch (item.getItemId()) {
            case R.id.add_container:
                Log.e(TAG,"add Container");
                ContainerFragment containFrag= ContainerFragment.newInstance(mCurrentContainerId, mPathStr);
                mListener.ShowFragment(containFrag);
                return true;
            case R.id.add_item:
                Log.e(TAG,"ADd item");
//                String Path = strJoin(homeJson.HouseNames.toArray(new String[homeJson.HouseNames.size()]),"/");
//                Path = Path + "Homes/";
                ItemFragment itemFrag= ItemFragment.newInstance(mCurrentContainerId, mPathStr);
                mListener.ShowFragment(itemFrag);
//                mListener.ChangeFragment(itemFrag, true);
                return true;
            case R.id.refresh:
                UpdateGridView(mCurrentContainerId);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.e(TAG, "Saveinsteance called");
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
            mListener.SetHomeFragment(this);
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e(TAG, "activity results");
        switch (requestCode) {
            case SEARCHTOHOME:
                String containerId= data.getStringExtra("test");
                UpdateGridView(containerId);
                Log.e(TAG,"got "+ containerId);
                break;
            case REDRAWGRID://doesnt use intent data
                gridView.setAdapter(mAdapter);
            default:
                Log.e(TAG, "unhandled onActivity Result");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener.unSetHomeFragment();
        mListener = null;
    }

    @Override
    public void onClick(View v) {
        switch( v.getId()){
            case R.id.up_one_level_butt:
                mCurrentContainerId = mParentContainerId;
                UpdateGridView(mCurrentContainerId);
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
        mCurrentContainerId = ContainerId;
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
                mSm= new SelectionManager(homeJson.HouseIds.size() + homeJson.ShareIds.size() + homeJson.SemiShareIds.size());
                mObjectIds = new ArrayList<String>();
                mObjectIds.addAll(homeJson.HouseIds);
                mObjectIds.addAll(homeJson.ShareIds);
                mObjectIds.addAll(homeJson.SemiShareIds);
                mPathStr = "Homes/";
//                gridView.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
                if(mListener.isViewPagerHidden()){
                    mListener.RedrawGridOnShow();
                    mAdapter = new HomeAdapter(homeJson, getActivity());
                    return;
                }
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
                mParentContainerId = getParent(containerJson);
                mPathStr = "Homes/" + strJoin(containerJson.PathNames.toArray(new String[containerJson.PathNames.size()]), "/");
                Log.e(TAG,mPathStr);
                Log.e(TAG,"done");

                if(mListener.isViewPagerHidden()){
                    mListener.RedrawGridOnShow();
                    mAdapter = new ContainerAdapter(containerJson, getActivity());
                    return;
                }

                gridView.setAdapter(new ContainerAdapter(containerJson, getActivity()));

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

    public static String strJoin(String[] aArr, String sSep) {
        StringBuilder sbStr = new StringBuilder();
        for (int i = 0, il = aArr.length; i < il; i++) {
            if (i > 0)
                sbStr.append(sSep);
            sbStr.append(aArr[i]);
        }
        return sbStr.toString();
    }
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public String getToken();
        public void ChangeFragment(Fragment f, boolean backstack);
        public void PerformRequestPost(String addr, RequestParams r, AsyncHttpResponseHandler h);
        public void PerformRequestGet(String addr, AsyncHttpResponseHandler h);
        public void ShowFragment(Fragment f);
        public void SetHomeFragment(HomeFragment f);
        public void unSetHomeFragment();
        public boolean isViewPagerHidden();
        public void RedrawGridOnShow();

    }


}
