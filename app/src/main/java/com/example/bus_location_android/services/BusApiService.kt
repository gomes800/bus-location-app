import com.example.bus_location_android.domain.BusLocation
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface BusApiService {
    @GET("/bus/locations")
    fun getBusLocations(
        @Query("line") line: String,
        @Query("forceRefresh") forceRefresh: Boolean = false
    ): Call<List<BusLocation>>
}

