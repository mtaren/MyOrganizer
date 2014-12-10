package com.myorganizer.myorgranizer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;


public class JsTypes {

    public static class HomeJson{
        //Ids
        @JsonProperty("MyHouses")
        public ArrayList<String> HouseIds;
        @JsonProperty("Shared")
        public ArrayList<String> ShareIds;
        @JsonProperty("SemiShare")
        public ArrayList<String> SemiShareIds;
        //names
        @JsonProperty("MyHousesNames")
        public ArrayList<String> HouseNames;
        @JsonProperty("SharedNames")
        public ArrayList<String> ShareNames;
        @JsonProperty("SemiShareNames")
        public ArrayList<String> SemiShareNames;
        //picurl
        @JsonProperty("MyHousesPics")
        public ArrayList<String> HousePics;
        @JsonProperty("SharedPics")
        public ArrayList<String> SharePics;
        @JsonProperty("SemiSharePics")
        public ArrayList<String> SemiSharePics;

        @JsonProperty("PathID")
        public ArrayList<String> PathIds;
        @JsonProperty("Path")
        public ArrayList<String> PathNames;

        @JsonCreator
        HomeJson(){};
 }



    public static class ContainerJson{
            //Containers
            @JsonProperty("Containers")
            public ArrayList<String> ContainerIds;
            @JsonProperty("ContainersNames")
            public ArrayList<String> ContainerNames;
            @JsonProperty("ContainersPics")
            public ArrayList<String> ContainerUrls;
            //names
            @JsonProperty("Items")
            public ArrayList<String> ItemIds;
            @JsonProperty("ItemsNames")
            public ArrayList<String> ItemNames;
            @JsonProperty("ItemsPics")
            public ArrayList<String> ItemUrls;
            //Path stuff... dont need?
            @JsonProperty("PathID")
            public ArrayList<String> PathIds;
            @JsonProperty("Path")
            public ArrayList<String> PathNames;

            @JsonCreator
            ContainerJson(){};
        }

    public static class ItemObj{

        @JsonProperty("Name")
        public String name;

        @JsonProperty("Desc")
        public String Desc;
        @JsonProperty("PicUrl")
        public String picUrl;
        @JsonProperty("Category")
        public String cat;
        @JsonProperty("Qty")
        public int qty;
        @JsonProperty("Id")
        public String Id;
        @JsonProperty("Path")
        public String Path;


        @JsonCreator
        ItemObj(){};
    }

    public static class ContainerObj{

        @JsonProperty("Name")
        public String name;
        @JsonProperty("Desc")
        public String Desc;
        @JsonProperty("PicUrl")
        public String picUrl;
        @JsonProperty("Category")
        public String cat;
        @JsonProperty("Id")
        public String Id;
        @JsonProperty("Path")
        public String Path;


        @JsonCreator
        ContainerObj(){};
    }






    ///////// testing ------------------////////////////
    public static class testOneHolder {
        @JsonProperty("oneList")
        public List<testOne> mContactList;
    }

    public static class testOne{


        @JsonProperty("one")
        public String one;

        @JsonCreator
        public testOne(){}

        public testOne(String str){ one=str; }

        public String getOne(){
            return one;
        }

        public void setOne(String one){
            this.one = one;
        }
    }

    public static class testList{
        @JsonProperty("testList")
        public ArrayList<String> arrList;
    }
}
