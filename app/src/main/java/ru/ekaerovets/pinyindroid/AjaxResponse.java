package ru.ekaerovets.pinyindroid;

import java.util.Map;


public class AjaxResponse {

    private int code;
    private Map<String, Object> result;

    public AjaxResponse(int code, Map<String, Object> result) {
        this.code = code;
        this.result = result;
    }

    public int getCode() {
        return code;
    }

    public Map<String, Object> getResult() {
        return result;
    }
}
