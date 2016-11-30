package ru.ekaerovets.pinyindroid.model;

import java.util.List;

import ru.ekaerovets.pinyindroid.model.Item;
import ru.ekaerovets.pinyindroid.model.Stat;

/**
 * @author karyakin dmitry
 *         date 03.06.16.
 */
public class SyncData {

    private List<Item> chars;
    private List<Item> words;
    private List<Item> pinyins;
    private transient List<Stat> stat;

    public SyncData() {
    }

    public List<Item> getChars() {
        return chars;
    }

    public void setChars(List<Item> chars) {
        this.chars = chars;
    }

    public List<Item> getPinyins() {
        return pinyins;
    }

    public void setPinyins(List<Item> pinyins) {
        this.pinyins = pinyins;
    }

    public List<Stat> getStat() {
        return stat;
    }

    public void setStat(List<Stat> stat) {
        this.stat = stat;
    }

    public List<Item> getWords() {
        return words;
    }

    public void setWords(List<Item> words) {
        this.words = words;
    }

}
