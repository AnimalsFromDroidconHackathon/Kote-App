package pl.whiter.kote_app.api;


import pl.whiter.kote_app.model.Kote;
import retrofit.http.GET;
import rx.Observable;

public interface AnimalService {

    @GET("/kote")
    Observable<Kote> getKoteAnimal();
}
