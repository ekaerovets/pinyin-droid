package ru.ekaerovets.pinyindroid;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Map;

public class WordsActivity extends AppCompatActivity {

    public static final double K_DIFF = 0.4;
    public static final double DIFF_MIN = 0.0015;
    public static final double DIFF_MAX = 2;

    private DataHolder dataHolder;

    NumberFormat formatter = new DecimalFormat("#0.00");

    private int count = 0;
    Item[] data = new Item[10];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_words);
    }

    @Override
    protected void onStart() {
        super.onStart();
        String json = DataService.loadFromFile(this);
        dataHolder = new DataHolder(json, 3);
        for (int i = 0; i < 10; i++) {
            if (i < 5) {
                data[i] = null;
            } else {
                data[i] = dataHolder.fetchItem();
            }
        }
        count = 0;
        ((TextView) findViewById(R.id.twNextWord)).setText(data[5] == null ? "" : getNotNull(data[5].getKey()));
    }

    @Override
    protected void onStop() {
        super.onStop();
        for (int i = 0; i < 10; i++) {
            Item p = data[i];
            if (p == null) {
                continue;
            }
            if (i < 5) {
                updateDiff(p, p.getAnswerStatus() == 1);
            }
            dataHolder.freeChar(p);
        }
        DataService.saveToFile(this, dataHolder.getJson());
    }


    public void onNextClick(View v) {
        Item p = data[0];
        if (p != null) {
            updateDiff(p, p.getAnswerStatus() == 1);
            p.setAnswerStatus(0);
            dataHolder.freeChar(p);
        }
        System.arraycopy(data, 1, data, 0, 9);
        data[9] = dataHolder.fetchItem();

        Map<String, String> stat = dataHolder.getStat();
        String s = stat.get("new") + " + " + stat.get("learn") + "\n" +
                stat.get("sum");
        ((TextView) findViewById(R.id.twStat)).setText(s);
        ((TextView) findViewById(R.id.twCount)).setText(Integer.toString(++count));

        p = data[4];

        ((TextView) findViewById(R.id.twWord)).setTextColor(Color.BLACK);

        ((TextView) findViewById(R.id.twNextWord)).setText(data[5] == null ? "" : getNotNull(data[5].getKey()));
        ((TextView) findViewById(R.id.twWord)).setText(p == null ? "" : getNotNull(p.getKey()));
        ((TextView) findViewById(R.id.twPinyin)).setText(p == null ? "" : getNotNull(p.getValueOrig()));
        ((TextView) findViewById(R.id.twMeaning)).setText(p == null ? "" : getNotNull(p.getWordMeaning()));
        ((TextView) findViewById(R.id.twDiff)).setText(p == null ? "" : formatDiff(p.getDiff()));

    }

    public void onToggleClick(View v) {
        if (data[4] != null) {
            data[4].setAnswerStatus(1 - data[4].getAnswerStatus());
            ((TextView) findViewById(R.id.twWord)).setTextColor(data[4].getAnswerStatus() == 1 ? Color.RED : Color.BLACK);

        }
    }


    private void updateDiff(Item p, boolean isDiff) {
        if (isDiff) {
            p.setDiff(p.getDiff() + K_DIFF * randNear());
            if (p.getDiff() > DIFF_MAX) {
                p.setDiff(DIFF_MAX);
            }
        } else {
            if (p.getDiff() > 0.05) {
                p.setDiff(p.getDiff() * 0.45 * randNear());
            } else if (p.getDiff() > 0.007) {
                p.setDiff(p.getDiff() * 0.55 * randNear());
            } else {
                p.setDiff(p.getDiff() * 0.65 * randNear());
            }
            if (p.getDiff() < DIFF_MIN) {
                p.setDiff(-1);
                p.setStage(1);
            }
        }
    }

    public double randNear() {
        double r = Math.random();
        return 0.97 + 0.06 * r;
    }

    private String getNotNull(String s) {
        return s == null ? "" : s;
    }

    private String formatDiff(double d) {
        return formatter.format(10 / d);
    }


}
