package data;

import android.app.Activity;
import android.content.SharedPreferences;

/**
 * Created by topaz on 9/5/16.
 */
public class CityPreference {
    SharedPreferences preferences;

    public CityPreference(Activity activity){
        preferences = activity.getPreferences(Activity.MODE_PRIVATE);
    }

    public String getCity(){
        return preferences.getString("city", "Denver");
    }

    public void setCity(String city) {
        preferences.edit().putString("city", city).commit();
    }

}
