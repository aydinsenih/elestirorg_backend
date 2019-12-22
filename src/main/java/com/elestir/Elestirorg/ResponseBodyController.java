package com.elestir.Elestirorg;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

public class ResponseBodyController {
    //Object returnBody;
    HashMap<String, Object> mBody = new HashMap<>();
    public final int FAILED = 0;
    public final int SUCCESS = 1;

    public ResponseBodyController(HashMap<String, String> hmBody) {
        this.mBody.put("data", hmBody);
        //this.returnBody = mBody;
    }
    public ResponseBodyController(List<HashMap<String,Object>> lBody){
        this.mBody.put("data", lBody);
        //this.returnBody = lBody;
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

    public void put(String key, String value){
        mBody.put(key, value);
    }

    public String getResponseBodyAsJson() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(mBody);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "{\"status\" : \"failed\" , \"message\" : \"Json error!\"}";
        }
    }

    public String serializeAnswers(HashMap<String, String> hMap) {
        ObjectMapper objectMapper = new ObjectMapper();
        HashMap<Integer,String> map = new HashMap<>();
        if (!(5 >= hMap.size() && 2 <= hMap.size())){
            return null;
        }
        int pointer = 1;
        for (String value: hMap.values()) {
            map.put(pointer,value);
            pointer++;
        }
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
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
