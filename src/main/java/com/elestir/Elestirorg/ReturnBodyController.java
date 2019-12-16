package com.elestir.Elestirorg;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;

public class ReturnBodyController {
    HashMap<String, String> returnBody =  new HashMap<>();

    public void setStatus(String status){
        returnBody.put("status", status);
    }

    public void put(String key, String value){
        returnBody.put(key, value);
    }

    public void setReturnBody(HashMap<String, String> returnBody) {
        this.returnBody = returnBody;
    }

    public String getReturnBodyAsJson() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(returnBody);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "{\"Status\" : \"JSON error!\"}";
        }
    }
}
