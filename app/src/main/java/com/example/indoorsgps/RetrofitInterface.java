package com.example.indoorsgps;

import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface RetrofitInterface {
    @PUT("/updateLocation/{userID}")
    Call<Void> executeUpdateLocation(@Path(value = "userID", encoded = true) String userID, @Body HashMap<String, String> map);

    @GET("/buildingsInfo")
    Call<List<BuildingModel>> executeGetAllBuildingsInfo();
}
