package com.elestir.Elestirorg;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringController {
    private Pattern pattern = null;

    public boolean isNumeric(String strNum){
        pattern = Pattern.compile("^[+]?[0-9]{10,}$");
        if(strNum == null)
            return false;
        Matcher m = pattern.matcher(strNum);
        return m.matches();
    }

    public boolean isEmailValid(String strEmail){
        pattern = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");
        if(strEmail == null)
            return false;
        Matcher m = pattern.matcher(strEmail);
        return m.matches();
    }
}
