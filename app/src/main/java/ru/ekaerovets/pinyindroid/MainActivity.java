package ru.ekaerovets.pinyindroid;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onPinyinsClick(View view) {
        Log.d("SPEED", "Load start");
        Intent intent = new Intent(this, LearnActivity.class);
        Bundle b = new Bundle();
        b.putBoolean("isChars", false);
        intent.putExtras(b);
        startActivity(intent);
    }

    public void onCharsClick(View view) {
        Intent intent = new Intent(this, LearnActivity.class);
        Bundle b = new Bundle();
        b.putBoolean("isChars", true);
        intent.putExtras(b);
        startActivity(intent);
    }

    public void onWordsClick(View view) {
        startActivity(new Intent(this, WordsActivity.class));
    }

    public void onSyncClick(View view) {
        progressDialog = ProgressDialog.show(this, "Sync", "Sync in progress");
        String syncUrl = "http://192.168.0.117:8080/rest/sync_pinyin";
        DataService.sync(this, null, syncUrl, new AjaxHandler() {
            @Override
            public void handle(View v, final int statusCode, String response) {
                if (statusCode == 200) {
                    DataService.saveToFile(MainActivity.this, response);
                    DataService.clearStat(MainActivity.this);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Sync success", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                    }
                });
                if (statusCode != 200) {
                    Log.e("TAG", "Cannot sync");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Sync failed, code = " + statusCode, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

}
