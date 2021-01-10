package com.example.coolweather.util;

import android.text.TextUtils;

import com.example.coolweather.db.CityEntity;
import com.example.coolweather.db.CountyEntity;
import com.example.coolweather.db.ProviceEntity;
import com.google.gson.JsonArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Utility {
    /**
     *  解析和处理服务器响应的省级数据
     *
     * */
    public static boolean handleProvinceResponse(String response) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray proviceData = new JSONArray(response);
                for (int i = 0; i < proviceData.length(); i++) {
                    JSONObject proviceObject = proviceData.getJSONObject(i);
                    ProviceEntity proviceEntity = new ProviceEntity();
                    proviceEntity.setProviceCode(proviceObject.getInt("id"));
                    proviceEntity.setProviceName(proviceObject.getString("name"));
                    proviceEntity.save(); // 保存到数据库
                }
                return true;

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     *  解析和处理服务器响应的市级数据
     *
     * */
    public static boolean handleCityResponse(String response, int provinceId) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray cityData = new JSONArray(response);
                for (int i = 0; i < cityData.length(); i++) {
                    JSONObject cityObject = cityData.getJSONObject(i);
                    CityEntity cityEntity = new CityEntity();
                    cityEntity.setId(cityObject.getInt("id"));
                    cityEntity.setCityName(cityObject.getString("name"));
                    cityEntity.setProviceId(provinceId);
                    cityEntity.save();
                }
                return true;

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     *  解析和处理服务器响应的县级数据
     *
     * */
    public static boolean handleCountyResponse(String response, int cityId) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray countyData = new JSONArray(response);
                for (int i = 0; i < countyData.length(); i++) {
                    JSONObject countyObject = countyData.getJSONObject(i);
                    CountyEntity countyEntity = new CountyEntity();
                    countyEntity.setWeatherId(countyObject.getString("weather_id"));
                    countyEntity.setCountyName(countyObject.getString("name"));
                    countyEntity.setCityId(cityId);
                    countyEntity.save();
                }
                return true;

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
