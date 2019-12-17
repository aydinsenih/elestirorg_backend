package com.elestir.Elestirorg;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.elestir.Elestirorg.Controller;

import java.util.HashMap;

public class ReturnBodyController {
    HashMap<String, String> returnBody =  new HashMap<>();
    public final int FAILED = 0;
    public final int SUCCESS = 1;


    public void setStatusAndMessage(int status, String message){
        setStatus(status);
        setMessage(message);
    }

    public void setStatus(int status){
        if (status == FAILED)
            returnBody.put("status", "failed");
        if (status == SUCCESS)
            returnBody.put("status", "success");
    }

    public void setMessage(String message){
        returnBody.put("message", message);
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
