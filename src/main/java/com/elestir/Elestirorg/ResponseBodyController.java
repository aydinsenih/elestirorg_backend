package com.elestir.Elestirorg;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

public class ResponseBodyController {
    HashMap<String, Object> mBody = new HashMap<>();
    public final int FAILED = 0;
    public final int SUCCESS = 1;

    public ResponseBodyController(HashMap<String, Object> hmBody) {
        this.mBody.put("data", hmBody);
    }
    public ResponseBodyController(List<Object> lBody){
        this.mBody.put("data", lBody);
    }
    public ResponseBodyController(){

    }

    public void setStatusAndMessage(int status, String message){
        setStatus(status);
        setMessage(message);
    }

    public void setStatus(int status){
        if (status == FAILED)
            mBody.put("status", "failed");
        if (status == SUCCESS)
            mBody.put("status", "success");
    }

    public void setMessage(String message){
        mBody.put("message", message);
    }

    public void put(String key, Object value){
        mBody.put(key, value);
    }

    public String getResponseBodyAsJson() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(mBody);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "{\"status\" : failed , \"message\" : Json error!}";
        }
    }

    public String serializeAnswers(HashMap<String, HashMap> hMap) {
        ObjectMapper objectMapper = new ObjectMapper();
        HashMap<Integer,String> map = new HashMap<>();
        if (!(5 >= hMap.size() && 2 <= hMap.size())){
            System.out.println("kisa uzun");
            return null;
        }
//        int pointer = 1;
//        for (String value: hMap.values()) {
//            map.put(pointer,value);
//            pointer++;
//        }//rearrange a map
        try {
            return objectMapper.writeValueAsString(hMap);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            System.out.println("catch");
            return null;
        }
    }

    public HashMap<String, Object> deserializeAnswers(String jsonString){
//        if (jsonString == null){
//            return null;
//        }
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(jsonString, new TypeReference<HashMap<String,Object>>() {
            });
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new HashMap<String, Object>(){{
                put("status", "failed");
                put("message", "Json error!");
            }};
        }
    }
}
