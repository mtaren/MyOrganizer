package com.myorganizer.myorgranizer;

import android.app.Activity;
import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.koushikdutta.async.http.AsyncHttpResponse;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.myorganizer.myorgranizer.GridAdapters.*;
import static com.myorganizer.myorgranizer.JsTypes.*;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link NotificationFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link NotificationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NotificationFragment extends Fragment {
    ;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private String TAG = "NotifFragment";
    public static String GETPEND = "/WS_FetchPendingEvents";
    public static String RESOLVEPEND = "/WS_ResolvePendEvent";

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    PendList mPendList;


    private OnFragmentInteractionListener mListener;

    public GridView mGridView;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment NotificationFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static NotificationFragment newInstance(String param1, String param2) {
        NotificationFragment fragment = new NotificationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public NotificationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {super.onCreate(savedInstanceState); }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_notification, container, false);
        mGridView = (GridView) v.findViewById(R.id.gridViewNotif);

        //Testing
        PendEventObj p = new PendEventObj();
        p.Title = "martinilin@gmail.com has shared something with you";
        p.Body = "this is the body";
        ArrayList<PendEventObj> e  = new ArrayList<>();
        e.add(p);
        if(mGridView!= null) {
            mGridView.setAdapter(new PendAdapter(e, getActivity()));
        }else{
            Log.e("pend", "mgrid is null");
        }
        GetPendEventRequest();
        //PerformPendRequest //do this later

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

    public void GetPendEventRequest(){
        mListener.PerformRequestPost(GETPEND, null ,new GetPendHandler());

    }
    public static void ResolovePendEvent_Request(String Action, String PendId, OnFragmentInteractionListener mlisten){
        RequestParams r = new RequestParams();
        r.add("action", Action);
        r.add("PendId", PendId);
        mlisten.PerformRequestPost(NotificationFragment.RESOLVEPEND, r, new NotificationFragment.ResolvePendHandler());
    }

    public  class GetPendHandler extends AsyncHttpResponseHandler{
        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] response) {
            Log.e(TAG, new String(response));

            try {
                mPendList= new ObjectMapper().readValue(response, PendList.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mGridView.setAdapter(new PendAdapter(mPendList.pendList , getActivity()));
        }
        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] response, Throwable error) {
            Log.e(TAG, "There was a problem in retrieving the url : " + error.toString());
        }
    }

    public static class ResolvePendHandler extends AsyncHttpResponseHandler{
        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] response) {
            Log.e("PendResolver", new String(response));

        }
        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] response, Throwable error) {
            Log.e("PendResolver", "There was a problem in retrieving the url : " + error.toString());
        }
    }



    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public String getToken();
        public void PerformRequestPost(String addr, RequestParams r, AsyncHttpResponseHandler h);
    }

}
