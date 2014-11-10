package com.myorganizer.myorgranizer;

import android.text.Selection;
import android.view.ActionMode;

import java.util.HashSet;

/**
 * Created by Martin on 12/5/2014.
 */
public class SelectionManager {
    HashSet<String> SelectedContainerIds;
    HashSet<String> SelectedItemIds;
    int numContainers;
    ActionMode actionMode;
    boolean state;

    SelectionManager(int numContainers){
        this.numContainers = numContainers;
        SelectedContainerIds = new HashSet<String>();
        SelectedItemIds = new HashSet<String>();
        state = false;
    }

    public boolean isSelected(String s){
        if( SelectedContainerIds.contains(s) || SelectedItemIds.contains(s)){
            return true;
        }
        //return true if it is in selectedItems or Ids
        return false;
    }

    public void select(String id, int index){
        state = true;
        if (index >= numContainers) {
            SelectedItemIds.add(id);
        }else{
            SelectedContainerIds.add(id);
        }

    }
    public void unSelect(String id, int index){
        if (index >= numContainers){
            SelectedItemIds.remove(id);
        }else{
            SelectedContainerIds.remove(id);
        }
    }
    public void clearSelection(){
        actionMode = null;
        state = false;
        SelectedContainerIds = new HashSet<String>();
        SelectedItemIds = new HashSet<String>();
    }

}
