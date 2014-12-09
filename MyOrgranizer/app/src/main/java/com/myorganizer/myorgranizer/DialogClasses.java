package com.myorganizer.myorgranizer;

import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import android.widget.NumberPicker;

import com.koushikdutta.ion.Ion;

import uk.co.senab.photoview.PhotoView;


public class DialogClasses {

    public static final String DialogResult= "NameResult";
    public static final int CANCELCODE = 29;
    public static final int NAMECODE = 30;
    public static final int DESCCODE = 31;
    public static final int CATCODE = 32;
    public static final int QTYCODE = 33;





    public  static class TextDialog extends DialogFragment implements View.OnClickListener {
        private String TAG = "textDialog";
        public EditText textbox;
        public String text;
        public String title;
        private static final String RECODE = "recode";
        private static final String DEFAULTSTRING= "defaultString";
        private static final String TITLESTRING= "titleString";
        private int RET_CODE;


        @Override
        public void onActivityCreated(Bundle bundle){ // this sets keyboard up
            super.onActivityCreated(bundle);
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }

        //new instance
        public static TextDialog newInstance(int retCode, String defaultVal,String titleString) {
            TextDialog fragment = new TextDialog();
            Bundle args = new Bundle();
            args.putInt(RECODE , retCode);
            args.putString(DEFAULTSTRING, defaultVal);
            args.putString(TITLESTRING, titleString);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (getArguments() != null) {
                RET_CODE = getArguments().getInt(RECODE);
                text = getArguments().getString(DEFAULTSTRING);
                title = getArguments().getString(TITLESTRING);
            }
        }
        @Override
        public View onCreateView(LayoutInflater inf, ViewGroup container, Bundle bun){
            getDialog().setTitle(title);
            View v = inf.inflate(R.layout.dialog_text, null);
            v.findViewById(R.id.cancelButt).setOnClickListener(this);
            Button okButt = (Button) v.findViewById(R.id.okButt);
            okButt.setOnClickListener(this);
//            okButt.requestFocus();
            textbox = (EditText) v.findViewById(R.id.text);
            textbox.setText(text);
            textbox.setImeActionLabel("OK", EditorInfo.IME_ACTION_DONE);
            textbox.setOnEditorActionListener( //listen for done
                    new EditText.OnEditorActionListener() {
                        @Override
                        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                            if (actionId == EditorInfo.IME_ACTION_DONE) {
                                Log.e(TAG, textbox.getText().toString());
                                sendResult(RET_CODE, textbox.getText().toString() );
                                dismiss();
                            }
                            return true;
                        }
                    }
            );
            return v;
        }

        private void sendResult(int INT_CODE, String result) {
            Intent i = new Intent();
            if (result.equals("")){
                result = text;
            }
            i.putExtra(DialogResult, result);
            getTargetFragment().onActivityResult(getTargetRequestCode(), INT_CODE, i);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.okButt:
                    sendResult(RET_CODE, textbox.getText().toString() );
                    dismiss();
                    break;
                case R.id.cancelButt:
                    sendResult(CANCELCODE, textbox.getText().toString() );
                    dismiss();
                    break;
                default:
                    Log.e(TAG, "no id recognized on click");
            }
        }
    }

    public  static class NumberDialog extends DialogFragment implements View.OnClickListener {
        private String TAG = "textDialog";
        public NumberPicker nNumberPicker;
        public String title;
        public int qty;
        private static final String RECODE = "recode";
        private static final String NUMSTRING= "number";
        private static final String TITLESTRING= "Quantity";
        private static final int DEFAULTNUM= 1;
        private int RET_CODE;


        //new instance
        public static NumberDialog newInstance(int retCode, int defaultVal ,String titleString) {
            NumberDialog fragment = new NumberDialog();
            Bundle args = new Bundle();
            args.putInt(RECODE , retCode);
            args.putInt(NUMSTRING, defaultVal);
            args.putString(TITLESTRING, titleString);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onActivityCreated(Bundle bundle){ // this sets keyboard up
            super.onActivityCreated(bundle);
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (getArguments() != null) {
                RET_CODE = getArguments().getInt(RECODE);
                qty = getArguments().getInt(NUMSTRING);
                title = getArguments().getString(TITLESTRING);
            }
        }
        @Override
        public View onCreateView(LayoutInflater inf, ViewGroup container, Bundle bun){
            getDialog().setTitle(title);
            View v = inf.inflate(R.layout.dialog_number, null);
            nNumberPicker= (NumberPicker) v.findViewById(R.id.numberPicker);
            nNumberPicker.setMinValue(1);
            nNumberPicker.setMaxValue(3200000);
            nNumberPicker.setWrapSelectorWheel(false);
            nNumberPicker.setValue(1);

            Button okButt = (Button) v.findViewById(R.id.okButt);
            Button cancelButt = (Button) v.findViewById(R.id.cancelButt);
            okButt.setOnClickListener(this);
            cancelButt.setOnClickListener(this);
            return v;
        }

        private void sendResult(int INT_CODE, int result) {
            Intent i = new Intent();
            i.putExtra(DialogResult, result);
            getTargetFragment().onActivityResult(getTargetRequestCode(), INT_CODE, i);
        }

        @Override
        public void onClick(View v) {

            switch (v.getId()){
                case R.id.okButt:

                    sendResult(RET_CODE, nNumberPicker.getValue());
                    dismiss();
                    break;
                case R.id.cancelButt:
                    sendResult(CANCELCODE, 1 );
                    dismiss();
                    break;
                default:
                    Log.e(TAG, "no id recognized on click");
            }
        }
    }

    public  static class RadioDialog extends DialogFragment implements View.OnClickListener {
        private String TAG = "textDialog";
        public EditText textbox;
        public String text;
        public String title;
        private static final String RECODE = "recode";
        private static final String DEFAULTSTRING= "defaultString";
        private static final String TITLESTRING= "titleString";
        private int RET_CODE;
        private RadioGroup mRadioGroup;

        //new instance
        public static RadioDialog newInstance(int retCode, String defaultVal,String titleString) {
            RadioDialog fragment = new RadioDialog();
            Bundle args = new Bundle();
            args.putInt(RECODE , retCode);
            args.putString(DEFAULTSTRING, defaultVal);
            args.putString(TITLESTRING, titleString);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (getArguments() != null) {
                RET_CODE = getArguments().getInt(RECODE);
                text = getArguments().getString(DEFAULTSTRING);
                title = getArguments().getString(TITLESTRING);
            }
        }
        @Override
        public View onCreateView(LayoutInflater inf, ViewGroup container, Bundle bun){
            getDialog().setTitle(title);
            View v = inf.inflate(R.layout.dialog_radio, null);
            Button okButt = (Button) v.findViewById(R.id.okButt);
            okButt.setOnClickListener(this);
            mRadioGroup = (RadioGroup) v.findViewById(R.id.rd_group);
            checkRadioButton(mRadioGroup,text);

            //todo fill in the button selected by title.

            return v;
        }

        private void sendResult(int INT_CODE, String result) {
            Intent i = new Intent();
            if (result.equals("")){
                result = text;
            }
            i.putExtra(DialogResult, result);
            getTargetFragment().onActivityResult(getTargetRequestCode(), INT_CODE, i);
        }

        @Override
        public void onClick(View v) {
            int radioButtonID = mRadioGroup.getCheckedRadioButtonId();
            Log.e(TAG, Integer.toString(radioButtonID));

            if(radioButtonID != -1){
                Log.e(TAG, text);
                text = ((RadioButton) mRadioGroup.findViewById(radioButtonID)).getText().toString();
                sendResult(RET_CODE, text);
                dismiss();
            }else{
                Log.e(TAG, "null");
                dismiss();
            }
        }
    }

    public  static class ImageDialog extends DialogFragment {
        private String TAG = "imgDialog";
        public String mImageLoc;
        public static final String IMAGE = "img";


        //new instance
        public static ImageDialog newInstance(String imageLoc) {
            ImageDialog fragment = new ImageDialog();
            Bundle args = new Bundle();
            args.putString(IMAGE, imageLoc);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen);

//            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
//            WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();
//            params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
//            params.height =  ViewGroup.LayoutParams.WRAP_CONTENT;
//            params.gravity = Gravity.LEFT;
//            getDialog().getWindow().setAttributes(params);

            if (getArguments() != null) {
                mImageLoc = getArguments().getString(IMAGE);
            }
        }
        @Override
        public View onCreateView(LayoutInflater inf, ViewGroup container, Bundle bun){

            View v = inf.inflate(R.layout.dialog_photo, container, false);
            PhotoView p = (PhotoView) v.findViewById(R.id.image);
            Ion.with(p).deepZoom().load(mImageLoc);


            return v;
        }


    }

    public static void checkRadioButton(RadioGroup radioGroup, String matchtext){

        int count = radioGroup.getChildCount();
        for (int i=0;i<count;i++) {
            View o = radioGroup.getChildAt(i);
            if (o instanceof RadioButton) {
                String text = (String) ((RadioButton) o).getText();
                if(text.equals(matchtext)){
                    ((RadioButton) o).setChecked(true);
                    break;
                }
            }
        }

    }

}
