package pl.whiter.kote_app.api;



import retrofit.RestAdapter;

public enum AnimalApi {


    API();

    public static final String ENDPOINT = "TEST";
    private AnimalService service;

    AnimalApi(){
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(ENDPOINT)
                .build();

        service = restAdapter.create(AnimalService.class);
    }


    public static AnimalService getService(){
        return AnimalApi.API.service;
    }



}
