package com.myorganizer.myorgranizer;

import android.app.Activity;
import android.app.Fragment;
import android.content.res.Resources;
import android.text.Selection;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.Toast;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;

import java.io.IOException;
import java.util.ArrayList;

import static com.myorganizer.myorgranizer.GridAdapters.HighlightItem;
import static com.myorganizer.myorgranizer.JsTypes.*;

/**
 * Created by Martin on 12/5/2014.
 */
public class CABCallbacks {


    public static class SelectedActionModeCallback implements ActionMode.Callback{
        public final String TAG = "selectedACtionBar";
        public static final String GETOBJEDIT = "/WS_GetOneObjectAttributes";
        public static final String DELETECONTAINER= "/WS_DeleteContainer";
        public static final String DELETEITEM = "/WS_DeleteItem";
        public String mCurrentContainerId;
        private OnFragmentInteractionListener mListener;

        GridView gridView;
        private AsyncHttpClient httpClient = new AsyncHttpClient();
        SelectionManager Sm;
        Resources r;

        public ActionMode mActionMode;

        SelectedActionModeCallback(GridView gv,SelectionManager sm,Resources res
                , Activity activity, String containerId){
            gridView = gv;
            Sm = sm;
            r = res;
            mListener = (OnFragmentInteractionListener) activity;
            mCurrentContainerId = containerId;
        }


        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mActionMode = mode;
            mode.getMenuInflater().inflate(R.menu.batch_select, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            Log.e(TAG, "clicked " + Integer.toString(item.getItemId()));
            switch(item.getItemId()){
                case R.id.edit: //todo add please only selcect one for editing toast
                    Log.e(TAG,"clcikd edit");
                    if(Sm.SelectedContainerIds.size() + Sm.SelectedItemIds.size() >1){
                        Toast.makeText((android.content.Context) mListener, "Select 1 Item to Edit", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    if(Sm.SelectedContainerIds.size() != 0){
                        String[] str = new String[Sm.SelectedContainerIds.size()];
                        str = Sm.SelectedContainerIds.toArray(str);
                        GetContainerParams_Request(str[0]);
                    }else if(Sm.SelectedItemIds.size() != 0) {
                        String[] str = new String[Sm.SelectedItemIds.size()];
                        str = Sm.SelectedItemIds.toArray(str);
                        GetItemParams_Request(str[0]);
                    }
                    break;
                case R.id.delete:
                    Log.e(TAG,"clicked delete");
                    String[] str = new String[Sm.SelectedContainerIds.size()];
                    str = Sm.SelectedContainerIds.toArray(str);
                    for (String aStr : str) {
                        DeleteContainer_Request(aStr);
                    }
                    str = new String[Sm.SelectedItemIds.size()];
                    str = Sm.SelectedItemIds.toArray(str);
                    for (String aStr : str) {
                       DeleteItem_Request(aStr);
                    }
                    Toast.makeText((android.content.Context) mListener, "Delete Request Sent...", Toast.LENGTH_LONG).show();
                    mode.finish();

                default:
                    Log.e(TAG, "ehe");
            }
//            mode.finish();// this closes the action mode
//            mListener = null;
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {

            for(String s : Sm.SelectedContainerIds){
                Log.e(TAG,s);
            }
            Log.e(TAG, "now selected items");
            for(String s : Sm.SelectedItemIds){
                Log.e(TAG,s);
            }

            Log.e(TAG, "destryoed");
            for(int i=0; i<gridView.getChildCount(); i++) {
                View child =  gridView.getChildAt(i);
                HighlightItem(child, r);
            }
            Sm.clearSelection();
            Sm.state = false;
            mListener = null;
        }

        //change all httpClient to mListener
        public void GetItemParams_Request(String ItemId){
            RequestParams params = new RequestParams();
            params.add("ObjId", ItemId);
            params.add("ObjType", "item");
            httpClient.post(HomeActivity.domain + GETOBJEDIT, params,new StartEditItemHandler());
        }

        public void GetContainerParams_Request(String ContainerId) {
            RequestParams params = new RequestParams();
            params.add("ObjId", ContainerId);
            params.add("ObjType", "container");
            httpClient.post(HomeActivity.domain + GETOBJEDIT, params, new StartEditContainerHandler());
        }

        public void DeleteContainer_Request(String ContainerId) {
            RequestParams params = new RequestParams();
            params.add("ContainerId", ContainerId);
            httpClient.post(HomeActivity.domain + DELETECONTAINER, params, new StartEditContainerHandler());
        }
        public void DeleteItem_Request(String ItemId) {
            RequestParams params = new RequestParams();
            params.add("ItemId", ItemId);
            httpClient.post(HomeActivity.domain + DELETEITEM, params, new EchoResult() );
        }

        public class EchoResult extends AsyncHttpResponseHandler{
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                Log.e(TAG, new String(response));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
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
                    ItemObj it= new ObjectMapper().readValue(response, ItemObj.class);
                    Log.e(TAG,"done");
                    ItemFragment f = ItemFragment.newInstance(it.name, it.Desc, it.cat, it.qty,
                           mCurrentContainerId, HomeActivity.domain + it.picUrl, it.Path, it.Id);
                    mListener.ShowFragment(f);
                    mActionMode.finish();
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
        public class StartEditContainerHandler extends AsyncHttpResponseHandler{
            private String TAG = "StartEdiitobjhandler";
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                Log.e(TAG, new String(response));
                //parse obj
                try
                {
                    ContainerObj it= new ObjectMapper().readValue(response, ContainerObj.class);
                    Log.e(TAG,"done");
                    ContainerFragment f = ContainerFragment.newInstance(it.name, it.Desc, it.cat,
                            mCurrentContainerId, HomeActivity.domain + it.picUrl, it.Path, it.Id);
                    mListener.ShowFragment(f);
                    mActionMode.finish();


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
    }





    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and
        public void ChangeFragment(Fragment f, boolean backstack);
        public void ShowFragment(Fragment f);
    }
}
