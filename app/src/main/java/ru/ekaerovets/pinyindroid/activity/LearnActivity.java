package ru.ekaerovets.pinyindroid.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import ru.ekaerovets.pinyindroid.handlers.PinyinViewEventListener;
import ru.ekaerovets.pinyindroid.model.Item;
import ru.ekaerovets.pinyindroid.model.Stat;
import ru.ekaerovets.pinyindroid.service.Engine;

public class LearnActivity extends AppCompatActivity implements PinyinViewEventListener {

    private PinyinView pinyinView;
    private Engine engine;

    NumberFormat formatter = new DecimalFormat("#0.00");
    private boolean isChars;
    private boolean prelearn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle b = getIntent().getExtras();
        isChars = b.getBoolean("isChars");
        prelearn = b.getBoolean("prelearn");
        setContentView(R.layout.activity_learn);
        pinyinView = (PinyinView) findViewById(R.id.pinyinView);
        pinyinView.setEventListener(this);
        pinyinView.setIsChars(isChars);
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            engine = new Engine(this, isChars ? 'c' : 'p', prelearn);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        engine.shutdown();
        finish();
    }

    private void displayItem(Item item) {
        ((TextView) findViewById(R.id.tvLearnZi)).setText(item == null ? "" : item.getWord());
        ((TextView) findViewById(R.id.tvLearnPinyin)).setText(item == null ? "" : (isChars ? engine.getPinyin(item) : item.getPinyin()));
        ((TextView) findViewById(R.id.tvLearnMeaning)).setText(item == null ? "" : (isChars ? item.getMeaning() : engine.getMeaning(item)));
        ((TextView) findViewById(R.id.tvLearnDiff)).setText(Html.fromHtml(getDiffStr(item)));
    }

    private void updateStat() {
        int nLearning = engine.getLearning();
        int nQueued = engine.getQueued();
        int nTriviaDue = engine.getReviewCount();
        String html = "<font color=\"#808080\">" + nLearning + "</font>/<font color=\"#FF8000\">" +
                nQueued + "</font>/<font color=\"00B000\">" + nTriviaDue + "</font>";
        ((TextView) findViewById(R.id.tvLearnDue)).setText(Html.fromHtml(html));

        Stat stat = engine.getStat();
        int reviewCorrect = stat.getReviewCorrect();
        int reviewTotal = reviewCorrect + stat.getReviewWrong();
        String review;
        if (reviewTotal == 0) {
            review = "-/-";
        } else {
            review = reviewCorrect + "/" + reviewTotal + " (" + (reviewCorrect * 100 / reviewTotal) + "%)";
        }
        ((TextView) findViewById(R.id.tvLearnReview)).setText(review);
        ((TextView) findViewById(R.id.tvLearnCounter)).setText(engine.getAnswerCount());
    }

    private String getDiffStr(Item item) {
        if (item == null) {
            return "";
        }
        String html = "";
        if (item.getStage() == 1) {
            html =  "<font color=\"#00B000\">" + item.getDiff() + "</font>";
        } else if (item.getStage() == 2) {
            if (item.getDiff() == 25) {
                html = "<font color=\"#FF0000\">•</font>";
            } else if (item.getDiff() == 125) {
                html = "<font color=\"#FF8000\">••</font>";
            } else if (item.getDiff() == 625) {
                html = "<font color=\"#80FF00\">•••</font>";
            }
        }
        return html;
    }

    public void invalidateView() {
        pinyinView.setData(engine.getDisplayItems());
        pinyinView.invalidate();
    }

    public void onLearnNextClick(View v) {
        engine.next();
        updateStat();
        displayItem(null);
        invalidateView();
    }

    public void onLearnDiffClick(View v) {
        engine.toggleDiff();
        invalidateView();
    }

    public void onTriviaClick(View v) {
        engine.toggleTrivia();
        invalidateView();
    }

    public void onLearnNewClick(View v) {
        engine.toggleNew();
        invalidateView();
    }

    public void onToggleMark(View v) {
        boolean checked = ((CheckBox) findViewById(R.id.chbMark)).isChecked();
        engine.toggleMark(checked);
        invalidateView();
    }

    @Override
    public void onClickEvent(int index, boolean isUp) {
        if (isUp) {
            engine.toggle(index);
        } else {
            displayItem(engine.getDisplayItems()[index]);
        }
    }
}
