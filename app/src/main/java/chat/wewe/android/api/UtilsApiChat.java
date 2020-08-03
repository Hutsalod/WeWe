package chat.wewe.android.api;

/*https://comeliest-adaptions.000webhostapp.com/*/
public class UtilsApiChat {
    // 10.0.2.2 ini adalah localhost.

    // Mendeklarasikan Interface BaseApiService
    public static BaseApiService getAPIService(){
        return RetrofitClientChat.getClient("https://chat.weltwelle.com/").create(BaseApiService.class);
    }
}
