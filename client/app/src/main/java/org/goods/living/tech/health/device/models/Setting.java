package org.goods.living.tech.health.device.models;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

import io.objectbox.annotation.Convert;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.converter.PropertyConverter;

@Entity
public class Setting extends BaseModel {

    @Id
    public long id;

    public boolean loglocationOffEvent;


    @Convert(converter = StringListConverter.class, dbType = String.class)
    public ArrayList<String> workingUSSD;


    public static class StringListConverter implements PropertyConverter<ArrayList<String>, String> {

        @Override
        public ArrayList<String> convertToEntityProperty(String jsonString) {
            if (jsonString == null) {
                return null;
            }


            try {
                ArrayList<String> list = new ArrayList<String>();
                JSONArray response = new JSONArray(jsonString);
                if (response != null) {
                    int len = response.length();
                    for (int i = 0; i < len; i++) {
                        list.add(response.get(i).toString());
                    }
                }
                return list;

            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
            //  return new Gson().fromJson(jsonString, new TypeToken<ArrayList<String>>() {
            //  }.getType());
        }

        @Override
        public String convertToDatabaseValue(ArrayList<String> list) {
            if (list == null) {
                return null;
            }

            JSONArray response = new JSONArray();
            if (response != null) {
                int len = response.length();
                for (int i = 0; i < list.size(); i++) {
                    response.put(list.get(i).toString());
                }
            }

            return response.toString();//new Gson().toJson(creatureList);
        }
    }
}