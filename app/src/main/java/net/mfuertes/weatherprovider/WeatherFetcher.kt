package net.mfuertes.weatherprovider

import android.content.Context
import android.content.Intent
import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.lang.Exception
import java.net.URL
import java.util.Calendar


class WeatherFetcher {
    companion object {
        // In case OWM decides that's enough
        private const val RESPONSE =
            "{\"timestamp\":1685981122,\"currentTemp\":297,\"todayMinTemp\":289,\"todayMaxTemp\":298,\"currentCondition\":\"scattered clouds\",\"currentConditionCode\":802,\"currentHumidity\":45,\"windSpeed\":4,\"windDirection\":340,\"forecasts\":[{\"conditionCode\":500,\"humidity\":36,\"maxTemp\":300,\"minTemp\":289},{\"conditionCode\":501,\"humidity\":79,\"maxTemp\":296,\"minTemp\":289},{\"conditionCode\":500,\"humidity\":50,\"maxTemp\":295,\"minTemp\":288},{\"conditionCode\":500,\"humidity\":48,\"maxTemp\":297,\"minTemp\":289},{\"conditionCode\":804,\"humidity\":37,\"maxTemp\":300,\"minTemp\":290},{\"conditionCode\":500,\"humidity\":32,\"maxTemp\":302,\"minTemp\":291},{\"conditionCode\":801,\"humidity\":27,\"maxTemp\":301,\"minTemp\":291}]}\n"

        private const val ACTION_GENERIC_WEATHER =
            "nodomain.freeyourgadget.gadgetbridge.ACTION_GENERIC_WEATHER"
        private const val EXTRA_WEATHER_JSON = "WeatherJson"

        fun fetchWeather(apiKey: String, latitude: Double, longitude: Double): JSONObject? {
            val url = "https://api.openweathermap.org/data/2.5/onecall" +
                    "?lat=" + latitude +
                    "&lon=" + longitude +
                    "&appid=" + apiKey;

            Log.d("WeatherFetcher", url)

            return try {
                val response = parse(URL(url).readText())

                if (response != null) {
                    var weatherObject = currentWeather(response)
                    addForecast(weatherObject, response)

                    weatherObject;
                } else
                    null
            } catch (ex: Exception) {
                null
            }

        }

        private fun parse(json: String): JSONObject? {
            return try {
                JSONObject(json)
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

        private fun currentWeather(response: JSONObject): JSONObject {
            val weatherJson = JSONObject()

            weatherJson.put("timestamp", (Calendar.getInstance().timeInMillis / 1000).toInt())
            //weatherJson.put("location", weatherLocation.name) //TODO: Send location name.
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