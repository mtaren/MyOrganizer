package com.myorganizer.myorgranizer;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;
import com.tonicartos.widget.stickygridheaders.StickyGridHeadersBaseAdapter;
import com.tonicartos.widget.stickygridheaders.StickyGridHeadersSimpleAdapter;

import java.util.ArrayList;
import java.util.List;
import static com.myorganizer.myorgranizer.JsTypes.*;

/**
 * Created by Martin on 12/4/2014.
 */
public class GridAdapters {

    //helper functions
    public static void HighlightItemPerm(View v, Resources c){
        v.setBackground(c.getDrawable(R.drawable.highlight_blue_perm));
        v.setAlpha((float) 0.50);
    }
    public static void HighlightItem(View v, Resources c){
        v.setBackground(c.getDrawable(R.drawable.highlight_blue));
        v.setAlpha((float) 1);

    }

    public static class Img{
        String Url;
        String Name;
        Img( String _url,String _name){
            Url = _url;
            Name = _name;
        }
    }

    public static class Txt{
        String Name;
        Txt(String _name){Name = _name;}
    }


    public static class HomeAdapter extends BaseAdapter implements StickyGridHeadersBaseAdapter{

        ArrayList<Img> list;
        Context context;
        HomeAdapter(HomeJson homeJson, Context context){
            this.context = context;
            list = new ArrayList<Img>();
            for(int i = 0; i < homeJson.HouseIds.size(); i++){
              list.add(new Img( homeJson.HousePics.get(i), homeJson.HouseNames.get(i)));}
            for(int i = 0; i < homeJson.ShareIds.size(); i++){
                list.add(new Img( homeJson.SharePics.get(i), homeJson.ShareNames.get(i)));}
            for(int i = 0; i < homeJson.SemiShareIds.size(); i++){
                list.add(new Img( homeJson.SemiSharePics.get(i), homeJson.SemiShareNames.get(i)));}
        }
        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int i) {
            return list.get(i);
        }

        @Override
        public long getItemId(int i) {
            //used in database stuff more, doesnt really matter for us
            return i;
        }

        @Override
        public int getCountForHeader(int i) {
            //the number of elements for this header
            return list.size();
        }

        @Override
        public int getNumHeaders() {
            //number of headers
            return 1;
        }

        @Override
        public View getHeaderView(int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            HeaderViewHolder holder;
            if (convertView == null) {
                holder = new HeaderViewHolder();
                convertView = inflater.inflate(R.layout.grid_header, parent, false);
                holder.text = (TextView) convertView.findViewById(R.id.textView);
                convertView.setTag(holder);
            } else {
                holder = (HeaderViewHolder) convertView.getTag();
            }
            //set header text as first char in name

            holder.text.setText("Home");
            return convertView;
        }

        class HeaderViewHolder {
            TextView text;
        }

        class ViewHolder{
            ImageView myImage;
            TextView myText;
            ViewHolder(View v){
                myImage = (ImageView) v.findViewById(R.id.imageView);
                myText = (TextView) v.findViewById(R.id.textView);
            }
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            View row = view;
            ViewHolder holder = null;
            if( row == null){
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                row = inflater.inflate(R.layout.grid_adapter_imagetype,viewGroup,false); //
                holder = new ViewHolder(row);
                row.setTag(holder);
            }
            else{
                holder = (ViewHolder) row.getTag();

            }

            String path = context.getString(R.string.domain) + list.get(i).Url;
//            Log.e("test", path);

            holder.myText.setText(list.get(i).Name);
            Ion.with(holder.myImage).load(path); // AMAZING!

            return row;
        }
    }

    public static class ContainerAdapter extends BaseAdapter implements StickyGridHeadersBaseAdapter{

        ArrayList<Img> list;
        Context context;
        String [] headers;
        int numContainers;
        int numItems;
        int [] numHeaders;
        ContainerAdapter(ContainerJson containerJson, Context context){
            this.context = context;
            numContainers = containerJson.ContainerIds.size();
            numItems = containerJson.ItemIds.size();
            list = new ArrayList<Img>();
            for(int i = 0; i < numContainers; i++){
                list.add(new Img( containerJson.ContainerUrls.get(i), containerJson.ContainerNames.get(i)));}
            for(int i = 0; i < numItems; i++){
                list.add(new Img( containerJson.ItemUrls.get(i), containerJson.ItemNames.get(i)));}
            if(containerJson.ContainerNames.size() == 0){
                headers = new String[1];
                headers[0] = "Items";
                numHeaders = new int[1];
                numHeaders[0] = numItems;
            }else{
                headers = new String[2];
                headers[0] = "Containers";
                headers[1] = "Items";
                numHeaders = new int[2];
                numHeaders[0] = numContainers;
                numHeaders[1] = numItems;

            }
        }
        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int i) {
            return list.get(i);
        }

        @Override
        public long getItemId(int i) {return i;}

        @Override
        public int getCountForHeader(int i) {
            //the number of elements for this header
            return numHeaders[i];
        }

        @Override
        public int getNumHeaders() {
            //number of headers
            return headers.length;
        }

        @Override
        public View getHeaderView(int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            HeaderViewHolder holder;
            if (convertView == null) {
                holder = new HeaderViewHolder();
                convertView = inflater.inflate(R.layout.grid_header, parent, false);
                holder.text = (TextView) convertView.findViewById(R.id.textView);
                convertView.setTag(holder);
            } else {
                holder = (HeaderViewHolder) convertView.getTag();
            }
            //set header text as first char in name

            holder.text.setText(headers[position]);
            return convertView;
        }

        class HeaderViewHolder {
            TextView text;
        }

        class ViewHolder{
            ImageView myImage;
            TextView myText;
            ViewHolder(View v){
                myImage = (ImageView) v.findViewById(R.id.imageView);
                myText = (TextView) v.findViewById(R.id.textView);
            }
        }
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            View row = view;
            ViewHolder holder = null;
            if( row == null){
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                row = inflater.inflate(R.layout.grid_adapter_imagetype,viewGroup,false); //
                holder = new ViewHolder(row);
                row.setTag(holder);
            }
            else{
                holder = (ViewHolder) row.getTag();
            }

            String path = context.getString(R.string.domain) + list.get(i).Url;
            holder.myText.setText(list.get(i).Name);
            Ion.with(holder.myImage).load(path); // AMAZING!

            return row;
        }


    }



    public static class SearchAdapter extends BaseAdapter implements StickyGridHeadersBaseAdapter{

        ArrayList<Txt> list;
        Context context;
        String [] headers;
        int numContainers;
        int numItems;
        int [] numHeaders;
        SearchAdapter(ContainerJson containerJson, Context context){
            this.context = context;
            numContainers = containerJson.ContainerIds.size();
            numItems = containerJson.ItemIds.size();
            list = new ArrayList<Txt>();
            for(int i = 0; i < numContainers; i++){
                list.add(new Txt(containerJson.ContainerNames.get(i)));}
            for(int i = 0; i < numItems; i++){
                list.add(new Txt(containerJson.ItemNames.get(i)));}
            if(containerJson.ContainerNames.size() == 0){
                headers = new String[1];
                headers[0] = "Items";
                numHeaders = new int[1];
                numHeaders[0] = numItems;
            }else{
                headers = new String[2];
                headers[0] = "Containers";
                headers[1] = "Items";
                numHeaders = new int[2];
                numHeaders[0] = numContainers;
                numHeaders[1] = numItems;

            }
        }
        @Override
        public int getCount() {
            return list.size();
        }
        @Override
        public Object getItem(int i) {
            return list.get(i);
        }
        @Override
        public long getItemId(int i) {return i;}
        @Override
        public int getCountForHeader(int i) {return numHeaders[i];}
        @Override
        public int getNumHeaders() {return headers.length;}

        @Override
        public View getHeaderView(int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            HeaderViewHolder holder;
            if (convertView == null) {
                holder = new HeaderViewHolder();
                convertView = inflater.inflate(R.layout.grid_header, parent, false);
                holder.text = (TextView) convertView.findViewById(R.id.textView);
                convertView.setTag(holder);
            } else {
                holder = (HeaderViewHolder) convertView.getTag();
            }

            holder.text.setText(headers[position]);
            return convertView;
        }

        class HeaderViewHolder {
            TextView text;
        }

        class ViewHolder{
            TextView myText;
            ViewHolder(View v){
                myText = (TextView) v.findViewById(R.id.textView);
            }
        }
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            View row = view;
            ViewHolder holder = null;
            if( row == null){
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                row = inflater.inflate(R.layout.grid_adapter_texttype,viewGroup,false); //
                holder = new ViewHolder(row);
                row.setTag(holder);
            }
            else{
                holder = (ViewHolder) row.getTag();
            }

            holder.myText.setText(list.get(i).Name);
            return row;
        }


    }



    //used to clear gridview
    public static class EmptyAdapter extends BaseAdapter implements StickyGridHeadersBaseAdapter{
        EmptyAdapter(){}
        @Override
        public int getCount() {return 0;}
        @Override
        public Object getItem(int i) {return null;}
        @Override
        public long getItemId(int i) {return i;}
        @Override
        public int getCountForHeader(int i) {return 0;}
        @Override
        public int getNumHeaders() {return 0;}
        @Override
        public View getHeaderView(int position, View convertView, ViewGroup parent) {return null;}
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {return null;}
    }


}
