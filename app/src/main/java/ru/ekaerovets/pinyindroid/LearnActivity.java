package ru.ekaerovets.pinyindroid;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Map;

public class LearnActivity extends AppCompatActivity implements ShowItemCallback {

    private DataHolder dataHolder;
    private PinyinView pinyinView;

    private StatEntry statEntry;

    int count = 0;

    NumberFormat formatter = new DecimalFormat("#0.00");
    NumberFormat cumulFormatter = new DecimalFormat("#0.0000");
    private Item current;
    private boolean isChars;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle b = getIntent().getExtras();
        isChars = b.getBoolean("isChars");
        setContentView(R.layout.activity_learn);
        pinyinView = (PinyinView) findViewById(R.id.pinyinView);
        pinyinView.setShowCallback(this);
        pinyinView.setIsChars(isChars);
    }

    @Override
    protected void onStart() {
        super.onStart();
        String json = DataService.loadFromFile(this);
        dataHolder = new DataHolder(json, isChars ? 1 : 2);
        pinyinView.attachDataHolder(dataHolder);
        statEntry = new StatEntry();
        statEntry.setType(isChars ? 'c' : 'p');
        count = 0;
        ((TextView) findViewById(R.id.tvZi)).setText("");
        ((TextView) findViewById(R.id.tvPinyin)).setText("");
        ((TextView) findViewById(R.id.tvMeaning)).setText("");
        ((TextView) findViewById(R.id.tvDiff)).setText("");
        ((CheckBox) findViewById(R.id.chbMark)).setChecked(false);
        ((TextView) findViewById(R.id.tvSimilar)).setText("");
        statEntry.setSessionStart(new Date());
    }

    @Override
    protected void onStop() {
        super.onStop();
        pinyinView.freeDataHolder(statEntry);
        DataService.saveToFile(this, dataHolder.getJson());
        statEntry.setSessionEnd(new Date());
        DataService.saveStat(this, statEntry);
    }

    private String getNotNull(String s) {
        return s == null ? "" : s;
    }

    private String formatDiff(double d) {
        return formatter.format(10 / d);
    }

    @Override
    public void show(Item p) {
        current = p;
        ((TextView) findViewById(R.id.tvZi)).setText(p == null ? "" : p.getKey());
        ((TextView) findViewById(R.id.tvPinyin)).setText(p == null ? "" : p.getValueOrig());
        ((TextView) findViewById(R.id.tvMeaning)).setText(p == null ? "" : getNotNull(dataHolder.getValue(p, isChars)));
        ((TextView) findViewById(R.id.tvDiff)).setText(p == null ? "" : formatDiff(p.getDiff()));
        ((CheckBox) findViewById(R.id.chbMark)).setChecked(p != null && p.isMark());
        String similar = null;
        if (isChars) {
            if (p != null && p.getRadix() != null) {
                similar = "<font color=\"#00ff00\">" + p.getRadix() + "</font>";
            }
        } else {
            if (p != null) {
                similar = dataHolder.getPinyinSimilar(p);
            }
        }
        ((TextView) findViewById(R.id.tvSimilar)).setText(similar == null ? "" : Html.fromHtml(similar),
                TextView.BufferType.SPANNABLE);
    }

    public void onLearnNextClick(View v) {
        pinyinView.shiftLeft(statEntry);
        Map<String, String> stat = dataHolder.getStat();
        String s = stat.get("new") + " + " + stat.get("learn") + "\n" +
                cumulFormatter.format(Double.parseDouble(stat.get("sum")));
        ((TextView) findViewById(R.id.tvStat)).setText(s);
        int reviewSuccess = Integer.parseInt(stat.get("review_success"));
        int reviewFail = Integer.parseInt(stat.get("review_fail"));
        String review = reviewSuccess + "/" + (reviewSuccess + reviewFail) + " (" + formatter.format(100.0 * reviewSuccess / (reviewSuccess + reviewFail)) + "%)";
        ((TextView) findViewById(R.id.tvCount)).setText(Integer.toString(++count) + "\n" + review);
    }

    public void onMarkClick(View v) {
        boolean checked = ((CheckBox) findViewById(R.id.chbMark)).isChecked();
        if (current != null) {
            current.setMark(checked);
            pinyinView.invalidate();
        }
    }

    public void onLearnToggleClick(View v) {
        pinyinView.toggle();
    }

    public void onTriviaClick(View v) {
        pinyinView.setTrivia();
        pinyinView.invalidate();
    }


}
