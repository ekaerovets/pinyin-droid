package ru.ekaerovets.pinyindroid;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by laby on 04.06.16.
 */
public class Item {

    private String key;
    private List<String> value;
    private String wordMeaning;
    private String valueOrig;
    private int stage;
    private double diff;
    private boolean mark;
    private String example;
    private boolean used;
    private int answerStatus;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public List<String> getValue() {
        return value;
    }

    public String getValueOrig() {
        return valueOrig;
    }

    public void setValueOrig(String valueOrig) {
        this.valueOrig = valueOrig;
    }

    public void setValue(String value) {
        this.valueOrig = value;
        String[] items = value.split("/");
        this.value = new ArrayList<>();
        for (String item : items) {
            this.value.add(item.trim());
        }
    }

    public String getWordMeaning() {
        return wordMeaning;
    }

    public void setWordMeaning(String wordMeaning) {
        this.wordMeaning = wordMeaning;
    }

    public int getStage() {
        return stage;
    }

    public void setStage(int stage) {
        this.stage = stage;
    }

    public double getDiff() {
        return diff;
    }

    public void setDiff(double diff) {
        this.diff = diff;
    }

    public boolean isMark() {
        return mark;
    }

    public void setMark(boolean mark) {
        this.mark = mark;
    }

    public String getExample() {
        return example;
    }

    public void setExample(String example) {
        this.example = example;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public int getAnswerStatus() {
        return answerStatus;
    }

    public void setAnswerStatus(int answerStatus) {
        this.answerStatus = answerStatus;
    }
}
