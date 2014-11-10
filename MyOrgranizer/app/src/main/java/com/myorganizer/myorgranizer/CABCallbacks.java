package com.myorganizer.myorgranizer;

import android.content.res.Resources;
import android.text.Selection;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;

import static com.myorganizer.myorgranizer.GridAdapters.HighlightItem;

/**
 * Created by Martin on 12/5/2014.
 */
public class CABCallbacks {


    public static class SelectedActionModeCallback implements ActionMode.Callback{
        public final String TAG = "selectedACtionBar";
        GridView gridView;

        SelectionManager Sm;
        Resources r;

        SelectedActionModeCallback(GridView gv,SelectionManager sm,Resources res){
            gridView = gv;

            Sm = sm;
            r = res;

        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
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
                case R.id.test:
                    Log.e(TAG,"clcikd test and not the other");
                    break;
                case 2:
                default:
                    Log.e(TAG, "ehe");
            }
            mode.finish();// this closes the action mode
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
        }
    }
}
