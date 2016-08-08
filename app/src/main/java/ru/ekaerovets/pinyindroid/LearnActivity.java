package ru.ekaerovets.pinyindroid;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Map;

public class LearnActivity extends AppCompatActivity implements ShowItemCallback {

    private DataHolder dataHolder;
    private PinyinView pinyinView;

    int count = 0;

    NumberFormat formatter = new DecimalFormat("#0.00");
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
        Log.d("SPEED", "Before file");
        String json = DataService.loadFromFile(this);
        Log.d("SPEED", "File done");
        dataHolder = new DataHolder(json, isChars ? 1 : 2);
        Log.d("SPEED", "Create dh");
        pinyinView.attachDataHolder(dataHolder);
        count = 0;
        Log.d("SPEED", "Load end");
    }

    @Override
    protected void onStop() {
        super.onStop();
        pinyinView.freeDataHolder();
        DataService.saveToFile(this, dataHolder.getJson());
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
        ((TextView) findViewById(R.id.tvComment)).setText(p == null ? "" : getNotNull(p.getExample()));
        ((TextView) findViewById(R.id.tvDiff)).setText(p == null ? "" : formatDiff(p.getDiff()));
        ((CheckBox) findViewById(R.id.chbMark)).setChecked(p != null && p.isMark());
        ((TextView) findViewById(R.id.tvSimilar)).setText(isChars || p == null ? "" :
                Html.fromHtml(dataHolder.getPinyinSimilar(p)), TextView.BufferType.SPANNABLE);
    }

    public void onLearnNextClick(View v) {
        pinyinView.shiftLeft();
        Map<String, String> stat = dataHolder.getStat();
        String s = stat.get("new") + " + " + stat.get("learn") + "\n" +
                stat.get("sum");
        ((TextView) findViewById(R.id.tvStat)).setText(s);
        ((TextView) findViewById(R.id.tvCount)).setText(Integer.toString(++count));
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

    public void onPlecoClick(View v) {

    }

}
