package com.example.android.weather.weather;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.net.URL;

import Util.Utils;
import data.CityPreference;
import data.JSONWeatherParser;
import data.WeatherHttpClient;
import model.Weather;

public class MainActivity extends AppCompatActivity {

    private TextView cityName;
    private TextView temp;
    private ImageView iconView;
    private TextView description;
    private TextView humidity;
    private TextView pressure;
    private TextView wind;
    private TextView sunrise;
    private TextView sunset;
    private TextView updated;

    Weather weather = new Weather();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cityName = (TextView) findViewById(R.id.cityText);
        iconView = (ImageView) findViewById(R.id.weatherIcon);
        temp = (TextView) findViewById(R.id.tempText);
        description = (TextView) findViewById(R.id.cloudText);
        humidity = (TextView) findViewById(R.id.humidityText);
        pressure = (TextView) findViewById(R.id.pressureText);
        wind = (TextView) findViewById(R.id.windText);
        sunrise = (TextView) findViewById(R.id.sunriseText);
        sunset = (TextView) findViewById(R.id.sunsetText);
        updated = (TextView) findViewById(R.id.updateText);

        CityPreference cityPreference = new CityPreference(MainActivity.this);

        renderWeatherData(cityPreference.getCity());
    }
    //--- OnCreate End ---//

    public void renderWeatherData(String city){
        WeatherTask weatherTask = new WeatherTask();
        weatherTask.execute(city + "&units=metric");

    }


    //--- DownloadImage START ---//
    // code found at -- https://stackoverflow.com/questions/8423987/download-image-for-imageview-on-android
    public class DownloadImage extends AsyncTask<String, Integer, Drawable>{

        @Override
        protected Drawable doInBackground(String... arg0){
            // This is done in a background thread
            return downloadImage(arg0[0]);
        }

        /**
         * Called after the image has been downloaded
         * -> this calls a function on the main thread again
         */
        protected void onPostExecute(Drawable image)
        {
            //x.setImageDrawable(image);
            //Sets the Icon as the image that's been downloaded
            iconView.setImageDrawable(image);
        }

        /**
         * Actually download the Image from the _url
         * @param _url
         * @return
         */
        private Drawable downloadImage(String _url)
        {
            //Prepare to download image
            URL url;
            BufferedOutputStream out;
            InputStream in;
            BufferedInputStream buf;

            //BufferedInputStream buf;
            try {
                url = new URL(_url);
                in = url.openStream();

            /*
             * THIS IS NOT NEEDED
             *
             * YOU TRY TO CREATE AN ACTUAL IMAGE HERE, BY WRITING
             * TO A NEW FILE
             * YOU ONLY NEED TO READ THE INPUTSTREAM
             * AND CONVERT THAT TO A BITMAP
            out = new BufferedOutputStream(new FileOutputStream("testImage.jpg"));
            int i;

             while ((i = in.read()) != -1) {
                 out.write(i);
             }
             out.close();
             in.close();
             */

                // Read the inputstream
                buf = new BufferedInputStream(in);

                // Convert the BufferedInputStream to a Bitmap
                Bitmap bMap = BitmapFactory.decodeStream(buf);
                if (in != null) {
                    in.close();
                }
                if (buf != null) {
                    buf.close();
                }

                return new BitmapDrawable(bMap);

            } catch (Exception e) {
                Log.e("Error reading file", e.toString());
            }
            return null;
        }
    }
    //--- DownloadImage End ---//

    //--- WeatherTask Start ---//
    private class WeatherTask extends AsyncTask<String, Void, Weather>{

        @Override
        protected Weather doInBackground(String... strings) {
            String data = ( (new WeatherHttpClient()).getWeatherData(strings[0]));

            // Catch error if a city is entered with a space e.g. "Fort Carson" or "Denver, US" to avoid fatal error
            // The space should already be removed at showInputDialog but this error causes the app to need to be reinstalled to work again so double protection
            try {
                weather = JSONWeatherParser.getWeather(data);
            } catch (Exception NullWeatherData){
                Log.e("Error Parsing: ", "City has space in text");
            }
            if (weather == null){
                showChangeCityDialog();
            }
            return weather;
        }

        @TargetApi(Build.VERSION_CODES.N)
        @Override
        protected void onPostExecute(Weather weather) {
            super.onPostExecute(weather);


            //Convert times into readable formats
            //get unix times and multiply by 1000 to get proper length since it converts down to milliseconds
            long unixSunrise = weather.place.getSunrise() * 1000;
            long unixSunset  = weather.place.getSunset() * 1000;
            long unixUpdated = weather.place.getLastupdate() * 1000;
            //do the conversion
            java.util.Date sunriseDate = new java.util.Date(unixSunrise);
            java.util.Date sunsetDate  = new java.util.Date(unixSunset);
            java.util.Date updatedDate = new java.util.Date(unixUpdated);
            //Strip away everything but the time in 24hr time (use hh in place of kk or 12hour clock)
            //need to change sunset and rise times to time zone for where the location is not my local timezone
            String sunriseTime = String.valueOf(android.text.format.DateFormat.format("kk:mm:ss zzz", sunriseDate));
            String sunsetTime  = String.valueOf(android.text.format.DateFormat.format("kk:mm:ss zzz", sunsetDate));
            String updatedTime = String.valueOf(android.text.format.DateFormat.format("kk:mm:ss zzz", updatedDate));

            // Set Text for all items
            cityName.setText(weather.place.getCity() + ", " + weather.place.getCountry());
            temp.setText(weather.temperature.getTemp() + " °C");
            wind.setText("Wind: " + weather.wind.getSpeed() + " m/s");
            description.setText("Cloudiness: " + weather.currentCondition.getDescription());
            humidity.setText("Humidity: " + weather.currentCondition.getHumidity() + "%");
            pressure.setText("Pressure: " + weather.currentCondition.getPressure() + " hPa");
            sunrise.setText("Sunrise: " + sunriseTime);
            sunset.setText("Sunset: " + sunsetTime);
            updated.setText("Last Updated: " + updatedTime);

            // Set iconView using the url code given in the XML file
            new DownloadImage().execute(Utils.ICON_URL + weather.currentCondition.getIcon() +".png");

        }
    }
    //--- WeatherTask End ---//

    //--- Change city Dialog Start ---//
    private void showChangeCityDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Change City");

        final EditText cityInput =  new EditText(MainActivity.this);
        cityInput.setInputType(InputType.TYPE_CLASS_TEXT);
        cityInput.setHint("Enter city (Seattle) or zip code (80501)");
        builder.setView(cityInput);
        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which){
                CityPreference cityPreference = new CityPreference(MainActivity.this);
                cityPreference.setCity(cityInput.getText().toString());

                // Remove Spaces in city string to avoid fatal error
                String newCity = cityPreference.getCity().replace(" ", "");

                renderWeatherData(newCity);

            }
        });
        builder.show();
    }
    //--- Change city Dialog END ---//

    //--- Change city Dialog Start ---//
    private void showChangeUnitsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Change Units");
    }
    //--- Change city Dialog END ---//


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.change_cityId){
            showChangeCityDialog();
        }
        if(id == R.id.change_unitsId){
            showChangeUnitsDialog();
        }
        return super.onOptionsItemSelected(item);
    }
}
