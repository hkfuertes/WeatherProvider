package net.mfuertes.weatherprovider

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.lang.Exception
import java.net.URL
import java.util.Calendar


class WeatherFetcher{
    companion object {
        private const val ACTION_GENERIC_WEATHER =
            "nodomain.freeyourgadget.gadgetbridge.ACTION_GENERIC_WEATHER"
        private const val EXTRA_WEATHER_JSON = "WeatherJson"

        fun fetchWeather(apiKey: String, latitude: Double, longitude: Double): JSONObject? {
            val url = "https://api.openweathermap.org/data/2.5/onecall" +
                    "?lat=" + latitude +
                    "&lon=" + longitude +
                    "&appid=" + apiKey;

            val geoUrl = "http://api.openweathermap.org/geo/1.0/reverse" +
                    "?lat=" + latitude +
                    "&lon=" + longitude +
                    "&limit=1" +
                    "&appid=" + apiKey;

            Log.d("WeatherFetcher:Url", url)
            Log.d("WeatherFetcher:GeoUrl", geoUrl)

            return try {
                val response = parseObject(URL(url).readText())
                val geoResponse = parseArray(URL(geoUrl).readText())

                //Log.d("WeatherFetcher:Response", response.toString())
                //Log.d("WeatherFetcher:GeoResponse", geoResponse.toString())

                if (response != null) {
                    var weatherObject = currentWeather(response, geoResponse)
                    addForecast(weatherObject, response)

                    weatherObject;
                } else
                    null
            } catch (ex: Exception) {
                Log.d("WeatherFetcher:Exception", ex.toString())
                null
            }

        }

        private fun parseObject(json: String): JSONObject? {
            return try {
                JSONObject(json)
            } catch (e: JSONException) {
                null
            }
        }

        private fun parseArray(json: String): JSONArray? {
            return try {
                JSONArray(json)
            } catch (e: JSONException) {
                null
            }
        }

        fun sendToGadgetBridge(context: Context, weatherObject: JSONObject) {
            context.sendBroadcast(
                Intent(ACTION_GENERIC_WEATHER)
                    .putExtra(EXTRA_WEATHER_JSON, weatherObject.toString())
                    .setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
            )
        }

        private fun currentWeather(response: JSONObject, geoResponse: JSONArray?): JSONObject {
            val weatherJson = JSONObject()

            weatherJson.put("timestamp", (Calendar.getInstance().timeInMillis / 1000).toInt())
            weatherJson.put("currentTemp", response.getJSONObject("current").getInt("temp"))
            weatherJson.put(
                "todayMinTemp",
                response.getJSONArray("daily").getJSONObject(0).getJSONObject("temp").getInt("min")
            )
            weatherJson.put(
                "todayMaxTemp",
                response.getJSONArray("daily").getJSONObject(0).getJSONObject("temp").getInt("max")
            )
            weatherJson.put(
                "currentCondition",
                response.getJSONObject("current").getJSONArray("weather").getJSONObject(0)
                    .getString("description")
            )
            weatherJson.put(
                "currentConditionCode",
                response.getJSONObject("current").getJSONArray("weather").getJSONObject(0)
                    .getInt("id")
            )
            weatherJson.put("currentHumidity", response.getJSONObject("current").getInt("humidity"))
            weatherJson.put("windSpeed", response.getJSONObject("current").getInt("wind_speed"))
            weatherJson.put("windDirection", response.getJSONObject("current").getInt("wind_deg"))

            if(geoResponse != null){
                weatherJson.put("location", geoResponse.getJSONObject(0).getString("name")) //TODO: Send location name.
            }

            return weatherJson;
        }

        private fun addForecast(weatherObject: JSONObject, response: JSONObject) {
            val weatherForecasts = JSONArray()

            for (i in 1 until response.getJSONArray("daily").length()) {
                val dailyJsonData = JSONObject()
                dailyJsonData.put(
                    "conditionCode",
                    response.getJSONArray("daily").getJSONObject(i).getJSONArray("weather")
                        .getJSONObject(0)
                        .getInt("id")
                )
                dailyJsonData.put(
                    "humidity",
                    response.getJSONArray("daily").getJSONObject(i).getInt("humidity")
                )
                dailyJsonData.put(
                    "maxTemp",
                    response.getJSONArray("daily").getJSONObject(i).getJSONObject("temp")
                        .getInt("max")
                )
                dailyJsonData.put(
                    "minTemp",
                    response.getJSONArray("daily").getJSONObject(i).getJSONObject("temp")
                        .getInt("min")
                )
                weatherForecasts.put(dailyJsonData)
            }

            weatherObject.put("forecasts", weatherForecasts)
        }
    }


}