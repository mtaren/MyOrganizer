package com.myorganizer.myorgranizer;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.koushikdutta.ion.Ion;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Picasso;

import static com.myorganizer.myorgranizer.DialogClasses.*;

import org.apache.http.Header;
import org.w3c.dom.Text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import static com.myorganizer.myorgranizer.ScalingUtilities.ScaleFileForUpload;



/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ItemFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ItemFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ItemFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener{
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private final String TAG = "ItemFrag";
    private static final String ARG_PARAM0 = "Type";
    private static final String ARG_PARAM1 = "name";
    private static final String ARG_PARAM2 = "desc";
    private static final String ARG_PARAM3 = "cat";
    private static final String ARG_PARAM4 = "qty";
    private static final String ARG_PARAM5 = "containerId";
    private static final String ARG_PARAM6 = "PicUrl";
    private static final String ARG_PARAM7 = "Path";
    private static final String ARG_PARAM8 = "id";

    private String mParentContainer;


    ImageView mImage;
    public String mCurrentPhotoPath = null;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int SELECT_PHOTO = 100;

    private String mPathStr;

    private String mName;
    private String mDesc;
    private String mCat;
    private int mQty;

    private String mId = null;

    private Button bName;
    private Button bDesc;
    private Button bCat;
    private Button bQty;

    private TextView mTextview;

    //flags for required fields
    private boolean misNameOk= true;
    private boolean misDescOk= true;
    private boolean misCatOk = true;
    private boolean misImageOk = true;
    private boolean misImageSelected = false;

    //submit stuff
    private String mSubmitUrl;
    private  static final String MODIFY_URL = "/WS_ModifyObj";
    private  static final String ADDITEM_URL = "/WS_AddItem";




    private OnFragmentInteractionListener mListener;

    //new instance
    public static ItemFragment newInstance(String name, String desc, String cat, int qty,
    String conatinerId, String PicUrl, String Path, String id) {
        ItemFragment fragment = new ItemFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM0, "edit");
        args.putString(ARG_PARAM1, name);
        args.putString(ARG_PARAM2, desc);
        args.putString(ARG_PARAM3, cat);
        args.putInt(ARG_PARAM4, qty);
        args.putString(ARG_PARAM5, conatinerId);
        args.putString(ARG_PARAM6, PicUrl);
        args.putString(ARG_PARAM7, Path);
        args.putString(ARG_PARAM8, id);
        fragment.setArguments(args);

        return fragment;
    }

    public static ItemFragment newInstance(String conatinerId, String Path) {
        ItemFragment fragment = new ItemFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM0, "new");
        args.putString(ARG_PARAM5, conatinerId);
        args.putString(ARG_PARAM7, Path);
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
            String type = getArguments().getString(ARG_PARAM0);
            if(type.equals("edit")) {
                mSubmitUrl = MODIFY_URL;
                mName = getArguments().getString(ARG_PARAM1);
                mDesc = getArguments().getString(ARG_PARAM2);
                mCat = getArguments().getString(ARG_PARAM3);
                mQty = getArguments().getInt(ARG_PARAM4);
                mParentContainer= getArguments().getString(ARG_PARAM5);
                mCurrentPhotoPath = getArguments().getString(ARG_PARAM6);
                mPathStr= getArguments().getString(ARG_PARAM7);
                mId= getArguments().getString(ARG_PARAM8);
                misNameOk = true;
                misDescOk = true;
                misCatOk = true;
                misImageOk = true;
                misImageSelected = false;
            }else{
                mSubmitUrl = ADDITEM_URL;
                mName = "Name";
                mDesc = "Description";
                mCat = "Category";
                mQty = 1;
                misNameOk= false;
                misDescOk= false;
                misCatOk = false;
                misImageOk = false;
                misImageSelected = false;
                mParentContainer= getArguments().getString(ARG_PARAM5);
                mPathStr= getArguments().getString(ARG_PARAM7);
            }

        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().getActionBar().hide();
        View v = inflater.inflate(R.layout.fragment_item, container, false);
        mImage = (ImageView)  v.findViewById(R.id.image);
        mImage.setOnClickListener(this);
        this.setHasOptionsMenu(true);

        if(mCurrentPhotoPath != null){
            Ion.with(mImage).load(mCurrentPhotoPath);
        }

        mTextview = (TextView) v.findViewById(R.id.path);
        mTextview.setText(mPathStr);

        bName = (Button) v.findViewById(R.id.nameButt);
        bDesc = (Button) v.findViewById(R.id.descButt);
        bCat = (Button) v.findViewById(R.id.categoryButt);
        bQty = (Button) v.findViewById(R.id.qtyButt);

        Button cam = (Button) v.findViewById(R.id.cameraButt);
        Button submit = (Button) v.findViewById(R.id.submitButt);

        bName.setText(mName);
        bDesc.setText(mDesc);
        bCat.setText(mCat);
        bQty.setText(Integer.toString(mQty));

        bName.setOnClickListener(this);
        bDesc.setOnClickListener(this);
        bCat.setOnClickListener(this);
        bQty.setOnClickListener(this);
        cam.setOnClickListener(this);
        submit.setOnClickListener(this);
        cam.setOnLongClickListener(this);

        //set buttons to good if in edit mode
        if(misNameOk){
            ChangeButtonReady(bName);
            ChangeButtonReady(bDesc);
            ChangeButtonReady(bCat);
        }

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


    //starting the dialogs
    @Override
    public void onClick(View v) {
        FragmentManager f = getActivity().getFragmentManager();
        switch(v.getId()){
            case R.id.nameButt:
                TextDialog dialog = TextDialog.newInstance(NAMECODE, mName, "Name");
                dialog.setTargetFragment(this, NAMECODE);
                dialog.show(f, "nameDialog");
                break;
            case R.id.descButt:
                TextDialog dialogDesc = TextDialog.newInstance(DESCCODE, mDesc, "Description");
                dialogDesc.setTargetFragment(this, DESCCODE);
                dialogDesc.show(f, "DescDialog");
                break;
            case R.id.categoryButt:
                RadioDialog dialogCat = RadioDialog.newInstance(CATCODE, mCat, "Category");
                dialogCat.setTargetFragment(this, CATCODE);
                dialogCat.show(f, "CatDialog");
                break;
            case R.id.qtyButt:
                NumberDialog dialogQty = NumberDialog.newInstance(QTYCODE, mQty, "Quantity");
                dialogQty.setTargetFragment(this, QTYCODE);
                dialogQty.show(f, "QtyDialog");
                break;
            case R.id.cameraButt:
                Log.e(TAG,"hello");
                //dispatchGalleryIntent();
                dispatchTakePictureIntent();
                break;
            case R.id.submitButt:
                if(misCatOk && misDescOk && misNameOk && misImageOk){
                    Log.e(TAG,"valid submit");
                    PerformItemRequest(mSubmitUrl);
//                    mListener.GoBack();
                }
                break;

            case R.id.image:
                ImageDialog imgD = ImageDialog.newInstance(mCurrentPhotoPath);
//                imgD.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                //imgD.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
                imgD.show(f, "ImgDialog");
                break;
            default:
                Log.e(TAG, "non registered onclick listener");

        }

    }

    //getting the dialog results back
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e(TAG, "activity results");
        switch (requestCode){
            case NAMECODE:
                mName= data.getStringExtra(DialogResult);
                bName.setText(mName);
                ChangeButtonReady(bName);
                misNameOk = true;
                break;
            case DESCCODE:
                mDesc= data.getStringExtra(DialogResult);
                bDesc.setText(mDesc);
                ChangeButtonReady(bDesc);
                misDescOk = true;
                break;
            case QTYCODE:
                mQty = data.getIntExtra(DialogResult, 1);
                bQty.setText(Integer.toString(mQty));
            case CATCODE:
                mCat = data.getStringExtra(DialogResult);
                ChangeButtonReady(bCat);
                bCat.setText(mCat);
                misCatOk = true;
                break;
        }
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK){
//            handle the camera giving a image location back
            Log.e(TAG, mCurrentPhotoPath);
            misImageSelected = true;
            misImageOk = true;
            mCurrentPhotoPath = ScaleFileForUpload(mCurrentPhotoPath);
            Ion.with(mImage).load(mCurrentPhotoPath); // AMAZING!
            Log.e(TAG,"OK");
        }else if(requestCode == SELECT_PHOTO && resultCode == Activity.RESULT_OK){
            //handle the user choosing an image from the library
            //need magical extra stuff to get the real path or something. lame
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getActivity().getContentResolver().query(
                    selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            misImageSelected = true;
            misImageOk = true;
            mCurrentPhotoPath = cursor.getString(columnIndex);
            cursor.close();
            mCurrentPhotoPath = ScaleFileForUpload(mCurrentPhotoPath);
            Ion.with(mImage).load(mCurrentPhotoPath); // AMAZING!
        }


    }

    public void PerformItemRequest(String URL){
        RequestParams rp = new RequestParams();
        rp.add("Parent", mParentContainer);
        rp.add("Name",mName);
        rp.add("Desc",mDesc);
        rp.add("Category",mCat);
        rp.add("Qty",Integer.toString(mQty));
        if(mSubmitUrl == MODIFY_URL) {
            rp.add("ObjId", mId);
            rp.add("ObjType", "item");
        }


        mListener.PerformRequestPost(URL, rp, new ItemHandler());

    }

    public class ItemHandler extends AsyncHttpResponseHandler{
        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] response) {
            String ItemId =new String(response);
            Log.e(TAG, ItemId);
            if(misImageSelected){// if picture chose, got to upload picture
                PerformItemUploadPhoto(mCurrentPhotoPath, ItemId);
            }else{
                mListener.GoBack();
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            Log.e(TAG, "There was a problem in retrieving the url : " + error.toString());
        }

    }

    public void PerformItemUploadPhoto(String localFilePath, String ItemId){
        RequestParams params = new RequestParams();
        params.add("ObjId", ItemId);
        params.add("ObjType", "item");
        mListener.PerformRequestPost("/WS_GetBlobUrl", params, new BlobURLHandler());
    }

    public class BlobURLHandler extends  AsyncHttpResponseHandler{

        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] response) {
            final String BlobUrl =new String(response);
            File myFile = new File(mCurrentPhotoPath);
            RequestParams params = new RequestParams();


            AsyncHttpClient httpClient = new AsyncHttpClient();
            try {
                params.put("upload", myFile); //file upload
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            httpClient.post(BlobUrl, params  ,new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                mListener.GoBack();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                Log.e(TAG, "POST There was a problem in retrieving the url : " + e.toString());
                Log.e(TAG,BlobUrl);
            }
        });


        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

        }
    }

    public class PhotoUploadHandler extends AsyncHttpResponseHandler{
        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] response) {
            Log.e(TAG,"Photo uploaded..back");
            mListener.GoBack();
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            Log.e(TAG, "Photo not uploaded: There was a problem in retrieving the url : " + error.toString());
        }

    }


    public void ChangeButtonReady(Button b){
        b.setBackgroundResource(R.drawable.buttonblue_500);
        b.setTextColor(getActivity().getResources().getColor( R.color.white));
    }
    public void ChangeButtonNotReady(Button b){
        b.setBackgroundResource(R.drawable.buttonblue_100);
        b.setTextColor(getActivity().getResources().getColor(R.color.black));
    }

    public void dispatchGalleryIntent() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, SELECT_PHOTO);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.e("FATAL", "Error occurred while creating the File");
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    public boolean onLongClick(View v) {
        //only long click the camera button
        dispatchGalleryIntent();
        return false;
    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void PerformRequestPost(String addr, RequestParams r, AsyncHttpResponseHandler h);
        public void ChangeFragment(Fragment f, boolean backstack);
        public void GoBack();
    }

}
