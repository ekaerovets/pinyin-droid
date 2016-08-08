package ru.ekaerovets.pinyindroid;

import android.view.View;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class AjaxHelper {

    public static void asyncQuery(final View v, final String url, final String input, final AjaxHandler handler) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Response ajax;
                try {
                    ajax = ajax(url, input);
                } catch (IOException e) {
                    handler.handle(v, 0, "");
                    return;
                }
                handler.handle(v, ajax.code, ajax.resp);
            }
        }).start();
    }

    private static class Response {
        public Response(int code, String resp) {
            this.code = code;
            this.resp = resp;
        }

        int code;
        String resp;
    }

    public static Response ajax(String url, String body) throws IOException {
        URL u = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) u.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        String result = "";
        int code = 0;
        try {
            conn.setDoOutput(true);
            conn.setChunkedStreamingMode(0);
            OutputStream out = new BufferedOutputStream(conn.getOutputStream());
            out.write(body.getBytes());
            out.flush();

            code = conn.getResponseCode();
            if (code != 200) {
                return new Response(code, "");
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            result = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            conn.disconnect();
        }
        return new Response(code, result);

    }

}
