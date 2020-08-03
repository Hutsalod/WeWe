package chat.wewe.android.api;

/*https://comeliest-adaptions.000webhostapp.com/*/
public class UtilsApi {
    // 10.0.2.2 ini adalah localhost.

    // Mendeklarasikan Interface BaseApiService
    public static BaseApiService getAPIService(){
        return RetrofitClient.getClient("https://weltwelle.com/").create(BaseApiService.class);
    }
}
