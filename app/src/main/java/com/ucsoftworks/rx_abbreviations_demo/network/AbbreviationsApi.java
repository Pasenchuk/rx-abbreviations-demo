package com.ucsoftworks.rx_abbreviations_demo.network;


import com.ucsoftworks.rx_abbreviations_demo.network.models.SearchResponse;

import java.util.List;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by pasencukviktor on 20/03/16
 */
public interface AbbreviationsApi {

    @GET("dictionary.py")
    Observable<List<SearchResponse>> getAbbreviationsResponse(@Query("sf") CharSequence searchString);

}
