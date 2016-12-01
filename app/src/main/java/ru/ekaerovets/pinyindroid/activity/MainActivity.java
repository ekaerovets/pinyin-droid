package ru.ekaerovets.pinyindroid.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import ru.ekaerovets.pinyindroid.handlers.SyncResultHandler;
import ru.ekaerovets.pinyindroid.service.DataService;

public class MainActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        if (DataService.updateSummary(this)) {
            updateSummary();
        }
    }

    private void updateSummary() {
        int[] summary = DataService.summary;
        ((TextView) findViewById(R.id.tvLearnDiff)).setText(Html.fromHtml(
                getSummaryRow(summary[0], summary[1], summary[2], summary[3], summary[4])));
        ((TextView) findViewById(R.id.tvLearnDiff)).setText(Html.fromHtml(
                getSummaryRow(summary[5], summary[6], summary[7], summary[8], summary[9])));
        ((TextView) findViewById(R.id.tvLearnDiff)).setText(Html.fromHtml(
                getSummaryRow(summary[10], summary[11], summary[12], summary[13], summary[14])));


    }

    private String getSummaryRow(int due, int suspended, int neww, int notDue, int total) {
        String res = "<font color=\"#000000\">" + due + "</font>/";
        res += "<font color=\"#FF00BB\">" + suspended + "</font>/";
        res += "<font color=\"#0000FF\">" + neww + "</font>/";
        res += "<font color=\"#008000\">" + notDue + "</font>/";
        res += "<font color=\"#808080\">" + total + "</font>";
        return res;
    }

    public void onMainLearnPinyins(View view) {
        Log.d("SPEED", "Load start");
        Intent intent = new Intent(this, LearnActivity.class);
        Bundle b = new Bundle();
        b.putBoolean("isChars", false);
        b.putBoolean("prelearn", false);
        intent.putExtras(b);
        startActivity(intent);
    }

    public void onMainPrelearnPinyins(View view) {

    }

    public void onMainGraphPinyins(View view) {

    }

    public void onMainLearnPinyins(View view) {
        Log.d("SPEED", "Load start");
        Intent intent = new Intent(this, LearnActivity.class);
        Bundle b = new Bundle();
        b.putBoolean("isChars", false);
        b.putBoolean("prelearn", false);
        intent.putExtras(b);
        startActivity(intent);
    }

    public void onMainPrelearnPinyins(View view) {

    }

    public void onMainGraphPinyins(View view) {

    }

    public void onMainLearnPinyins(View view) {
        Log.d("SPEED", "Load start");
        Intent intent = new Intent(this, LearnActivity.class);
        Bundle b = new Bundle();
        b.putBoolean("isChars", false);
        b.putBoolean("prelearn", false);
        intent.putExtras(b);
        startActivity(intent);
    }

    public void onMainPrelearnPinyins(View view) {

    }

    public void onMainGraphPinyins(View view) {

    }

    public void onSyncClick(View view) {
        progressDialog = ProgressDialog.show(this, "Sync", "Sync in progress");
        DataService.sync(this, new SyncResultHandler() {
            @Override
            public void onSyncCompleted(int code) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(),
                                code == 200 ? "Sync success" : "Sync failed, code = " + code, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

}
