package com.acms.iexplore;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface RetrofitInterface {
    @PUT("/updateLocation/{userID}")
    Call<Void> executeUpdateLocation(@Path(value = "userID", encoded = true) String userID, @Body UserLocationModel userLocation);

    @GET("/buildingsInfo")
    Call<List<BuildingModel>> executeGetAllBuildingsInfo();
}
