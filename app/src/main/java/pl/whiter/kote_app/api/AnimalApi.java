package pl.whiter.kote_app.api;


import com.squareup.okhttp.OkHttpClient;

import retrofit.RestAdapter;
import retrofit.client.OkClient;

public enum AnimalApi {


    API();

    public static final String ENDPOINT = "TEST";
    private AnimalService service;

    AnimalApi(){
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(ENDPOINT)
                .setClient(new OkClient(new OkHttpClient()))
                .build();

        service = restAdapter.create(AnimalService.class);
    }


    public static AnimalService getService(){
        return AnimalApi.API.service;
    }



}
