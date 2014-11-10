package com.myorganizer.myorgranizer;

import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Martin on 12/7/2014.
 */
public class DialogClasses {

    public  static class TextDialog extends DialogFragment implements View.OnClickListener {
        private String TAG = "textDialog";
        public EditText textbox;


        @Override
        public void onActivityCreated(Bundle bundle){ // this sets keyboard up
            super.onActivityCreated(bundle);
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }

        @Override
        public View onCreateView(LayoutInflater inf, ViewGroup container, Bundle bun){
            View v = inf.inflate(R.layout.dialog_text, null);
            v.findViewById(R.id.cancelButt).setOnClickListener(this);
            Button okButt = (Button) v.findViewById(R.id.okButt);
            okButt.setOnClickListener(this);
//            okButt.requestFocus();
            textbox = (EditText) v.findViewById(R.id.text);
            textbox.setText("one");
            textbox.setImeActionLabel("OK", EditorInfo.IME_ACTION_DONE);
            textbox.setOnEditorActionListener( //listen for done
                    new EditText.OnEditorActionListener() {
                        @Override
                        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                            if (actionId == EditorInfo.IME_ACTION_DONE) {
                                Log.e(TAG, textbox.getText().toString());
                                dismiss();
                            }
                            return true;
                        }
                    }
            );
            return v;
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.okButt:
                    dismiss();
                    break;
                case R.id.cancelButt:
                    dismiss();
                    break;
                default:
                    Log.e(TAG, "no id recognized on click");



            }
        }
    }


}
