package com.ucsoftworks.rx_abbreviations_demo.network.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by pasencukviktor on 21/03/16
 */
public class Var {

    @SerializedName("lf")
    @Expose
    private String lf;
    @SerializedName("freq")
    @Expose
    private long freq;
    @SerializedName("since")
    @Expose
    private long since;

    public String getLf() {
        return lf;
    }

    public long getFreq() {
        return freq;
    }

    public long getSince() {
        return since;
    }
}
