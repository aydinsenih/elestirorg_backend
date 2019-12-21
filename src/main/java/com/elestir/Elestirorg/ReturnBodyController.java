package com.elestir.Elestirorg;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.elestir.Elestirorg.Controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ReturnBodyController {
    //Object returnBody;
    HashMap<String, Object> mBody = new HashMap<>();
    public final int FAILED = 0;
    public final int SUCCESS = 1;

    public ReturnBodyController(HashMap<String, String> mBody) {
        this.mBody.put("data", mBody);
        //this.returnBody = mBody;
    }
    public ReturnBodyController(List<HashMap> lBody){
        this.mBody.put("data", lBody);
        //this.returnBody = lBody;
    }
    public ReturnBodyController(){

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

    public String getmBodyAsJson() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(mBody);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "{\"Status\" : \"JSON error!\"}";
        }
    }
}
