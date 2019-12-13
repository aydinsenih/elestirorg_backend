package com.elestir.Elestirorg;

import java.util.regex.Pattern;

public class StringController {
    private Pattern pattern = null;

    public boolean isNumeric(String strNum){
        pattern = Pattern.compile("^[+]?[0-9]{10,}$");
        if(strNum == null)
            return false;
        return pattern.matcher(strNum).matches();
    }

    public boolean isEmailValid(String strEmail){
        pattern = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$");
        if(strEmail == null)
            return false;
        return pattern.matcher(strEmail).matches();
    }
}
