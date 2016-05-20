package com.ucsoftworks.rx_abbreviations_demo.network.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pasencukviktor on 21/03/16
 */
public class SearchResponse {

    @SerializedName("sf")
    @Expose
    private String sf;
    @SerializedName("lfs")
    @Expose
    private List<Lf> lfs = new ArrayList<>();

    public String getSf() {
        return sf;
    }

    public List<Lf> getLfs() {
        return lfs;
    }
}
