package ru.ekaerovets.pinyindroid;

import android.view.View;

import java.util.Map;


public interface AjaxHandler {

    public void handle(View v, int statusCode, String response);

}
