package com.ucsoftworks.rx_abbreviations_demo.network.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pasencukviktor on 21/03/16
 */
public class Lf {


    @SerializedName("lf")
    @Expose
    private String lf;
    @SerializedName("freq")
    @Expose
    private long freq;
    @SerializedName("since")
    @Expose
    private long since;
    @SerializedName("vars")
    @Expose
    private List<Var> vars = new ArrayList<>();

    public String getLf() {
        return lf;
    }

    public long getFreq() {
        return freq;
    }

    public long getSince() {
        return since;
    }

    public List<Var> getVars() {
        return vars;
    }
}

