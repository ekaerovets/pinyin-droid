package ru.ekaerovets.pinyindroid;

import android.content.Context;
import android.view.View;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class DataService {

    private static final String FILENAME = "data.json";

    public static void sync(Context ctx, View v, String url, AjaxHandler handler) {
        String input = loadFromFile(ctx);
        AjaxHelper.asyncQuery(v, url, input, handler);
    }

    public static void saveToFile(Context ctx, String json) {
        try {
            FileOutputStream out = ctx.openFileOutput(FILENAME, Context.MODE_PRIVATE);
            out.write(json.getBytes());
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String loadFromFile(Context ctx) {
        try {
            FileInputStream in = ctx.openFileInput(FILENAME);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();
            return sb.toString();
        } catch (IOException e) {
            return "{chars: [], pinyins: []}";
        }
    }

}
