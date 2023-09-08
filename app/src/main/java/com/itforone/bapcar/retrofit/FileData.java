package com.itforone.bapcar.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class FileData {
    public ArrayList<Files> getFiles() {
        return files;
    }


    @SerializedName("files")
    @Expose
    ArrayList<Files> files;
}
