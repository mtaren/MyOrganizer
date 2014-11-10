package com.myorganizer.myorgranizer;

import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import static com.myorganizer.myorgranizer.DialogClasses.*;
import org.w3c.dom.Text;



/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ItemFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ItemFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ItemFragment extends Fragment implements View.OnClickListener{
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private final String TAG = "ItemFrag";
    private static final String ARG_PARAM1 = "name";
    private static final String ARG_PARAM2 = "desc";
    private static final String ARG_PARAM3 = "cat";
    private static final String ARG_PARAM4 = "qty";


    // TODO: Rename and change types of parameters
    private String mName;
    private String mDesc;
    private String mCat;
    private int mQty;

    private Button bName;
    private Button bDesc;
    private Button bCat;
    private Button bQty;



    private OnFragmentInteractionListener mListener;

    //new instance
    public static ItemFragment newInstance(String name, String desc, String cat, int qty ) {
        ItemFragment fragment = new ItemFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, name);
        args.putString(ARG_PARAM2, desc);
        args.putString(ARG_PARAM3, cat);
        args.putInt(ARG_PARAM4, qty);
        fragment.setArguments(args);
        return fragment;
    }

    public ItemFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mName = getArguments().getString(ARG_PARAM1);
            mDesc = getArguments().getString(ARG_PARAM2);
            mCat = getArguments().getString(ARG_PARAM3);
            mQty = getArguments().getInt(ARG_PARAM4);
        }else{
            mName = "Name";
            mDesc = "Description";
            mCat = "Category";
            mQty = 1;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_item, container, false);
        bName = (Button) v.findViewById(R.id.nameButt);
        bDesc = (Button) v.findViewById(R.id.descButt);
        bCat = (Button) v.findViewById(R.id.categoryButt);
        bQty = (Button) v.findViewById(R.id.qtyButt);

        bName.setText(mName);
        bDesc.setText(mDesc);
        bCat.setText(mCat);
        bQty.setText(Integer.toString(mQty));

        bName.setOnClickListener(this);
        bDesc.setOnClickListener(this);
        bCat.setOnClickListener(this);
        bQty.setOnClickListener(this);

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
        switch(v.getId()){
            case R.id.nameButt:
                FragmentManager f = getActivity().getFragmentManager();
                TextDialog dialog = new TextDialog();
//                dialog.setTargetFragment(this, 1);
                dialog.show(f, "nameDialog");
                break;
            case R.id.descButt:
                break;
            case R.id.categoryButt:
                break;
            case R.id.qtyButt:
                break;
            case R.id.cameraButt:
                break;
            case R.id.submitButt:
                break;
            default:
                Log.e(TAG, "non registered onclick listener");

        }

    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void PerformRequestPost(String addr, RequestParams r, AsyncHttpResponseHandler h);
    }

}
