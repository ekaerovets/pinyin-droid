package ru.ekaerovets.pinyindroid;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataHolder {


    private static final String SPECIAL = "āáǎàēéěèīíǐìōóǒòūúǔùǖǘǚǜ";
    private static final String SPECIAL_UNTONED = "aeiouü";

    private static final String[] FONT_COLORS = {"#707070", "#bbc11c", "#a90000", "#009839", "#0002AA"};

    private List<Item> chars;
    private List<Item> pinyins;
    private List<Item> words;
    private Map<String, String> pinyinMap;

    private Map<String, String> pinMap;
    private Map<String, String> charMap;

    private List<Item> current;

    public DataHolder(String json, int type) {
        Log.d("TAG", "new holder from string " + json.length());
        try {
            JSONObject root = new JSONObject(json);
            chars = parseItems(root.getJSONArray("chars"), 1);
            pinyins = parseItems(root.getJSONArray("pinyins"), 2);
            words = parseItems(root.getJSONArray("words"), 3);
        } catch (JSONException e) {
            Log.e("TAG", "Failed to load data", e);
        }
        Log.d("TAG", "total pinyins " + pinyins.size() + " and chars " + chars.size());
        buildPinyinMap();
        buildMaps();
        if (type == 1) {
            current = chars;
        } else if (type == 2) {
            current = pinyins;
        } else {
            current = words;
        }
    }

    private void buildMaps() {
        pinMap = new HashMap<>();
        charMap = new HashMap<>();
        for (Item item: chars) {
            charMap.put(item.getKey(), item.getValueOrig());
        }
        for (Item item: pinyins) {
            pinMap.put(item.getKey(), item.getValueOrig());
        }
    }

    // 1 - char, 2 - pinyin, 3 - word
    private List<Item> parseItems(JSONArray arr, int type) throws JSONException {
        List<Item> res = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            Item item = new Item();
            item.setKey(obj.getString("word"));
            if (type == 1) {
                item.setValue(obj.getString("meaning"));
            } else if (type == 2) {
                item.setValue(obj.getString("pinyin"));
            } else if (type == 3) {
                item.setValueOrig(obj.getString("pinyin"));
                item.setWordMeaning(obj.getString("meaning"));
            }
            item.setStage(obj.getInt("stage"));
            item.setDiff(obj.getDouble("diff"));
            if (obj.has("example")) {
                item.setExample(obj.getString("example"));
            }
            item.setUsed(false);
            item.setMark(obj.getBoolean("mark"));
            res.add(item);
        }
        return res;
    }


    /**
     * Function builds map of pinyin (without tone) to all the characters that have that pinyin
     */
    private void buildPinyinMap() {
        pinyinMap = new HashMap<>();
        for (Item p: pinyins) {
            for (String pp: p.getValue()) {
                int tone = 0;
                for (int i = 0; i < pp.length(); i++) {
                    int index = SPECIAL.indexOf(pp.charAt(i));
                    if (index != -1) {
                        tone = (index % 4) + 1;
                        String letter = SPECIAL_UNTONED.substring(index / 4, index / 4 + 1);
                        pp = pp.substring(0, i) + letter + pp.substring(i + 1, pp.length());
                        break;
                    }
                }
                String html = "<font color=\"" + FONT_COLORS[tone] + "\">" + p.getKey() + "</font>";
                if (pinyinMap.containsKey(pp)) {
                    pinyinMap.put(pp, pinyinMap.get(pp) + html);
                } else {
                    pinyinMap.put(pp, html);
                }
            }
        }
    }

    public String getValue(Item p, boolean pinyin) {
        return pinyin ? pinMap.get(p.getKey()) : charMap.get(p.getKey());
    }

    public String getPinyinSimilar(Item p) {
        Map<String, String> similar = new HashMap<>();
        for (String pp: p.getValue()) {
            for (int i = 0; i < pp.length(); i++) {
                int index = SPECIAL.indexOf(pp.charAt(i));
                if (index != -1) {
                    String letter = SPECIAL_UNTONED.substring(index / 4, index / 4 + 1);
                    pp = pp.substring(0, i) + letter + pp.substring(i + 1, pp.length());
                    break;
                }
            }
            if (!similar.containsKey(pp)) {
                similar.put(pp, pinyinMap.get(pp));
            }
        }
        if (similar.size() == 1) {
            return similar.values().iterator().next();
        } else {
            String res = "";
            boolean isFirst = true;
            for (Map.Entry<String, String> e: similar.entrySet()) {
                if (!isFirst) {
                    res += "<br>";
                }
                isFirst = false;
                res += e.getKey() + ": " + e.getValue();
            }
            return res;
        }
    }

    public void freeChar(Item item) {
        item.setUsed(false);
    }

    public Item fetchItem() {
        double[] k = new double[current.size()];
        Item[] items = new Item[current.size()];
        double sum = 0;
        int nValid = 0;
        Item toAdd = null;
        for (Item o : current) {
            if (o.isUsed() || o.getStage() != 2) {
                continue;
            }
            if (o.getDiff() < 0) {
                toAdd = o;
                continue;
            }
            sum += o.getDiff();
            k[nValid] = sum;
            items[nValid] = o;
            nValid++;
        }
        if (sum < 10 && toAdd != null) {
            sum += 0.4;
            toAdd.setDiff(0.4);
            k[nValid] = sum;
            items[nValid] = toAdd;
            nValid++;
        }
        double res = sum * Math.random();
        for (int i = 0; i < nValid; i++) {
            if (res < k[i] || (i == nValid - 1)) {
                Item pinyin = items[i];
                pinyin.setUsed(true);
                return pinyin;
            }
        }
        return null;
    }

    public String getJson() {
        try {
            JSONObject res = new JSONObject();
            res.put("chars", toArray(chars, 1));
            res.put("pinyins", toArray(pinyins, 2));
            res.put("words", toArray(words, 3));
            return res.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    private JSONArray toArray(List<Item> items, int type) throws JSONException {
        JSONArray res = new JSONArray();
        for (Item item : items) {
            JSONObject obj = new JSONObject();
            obj.put("word", item.getKey());
            if (type == 1) {
                obj.put("meaning", item.getValueOrig());
            } else if (type == 2) {
                obj.put("pinyin", item.getValueOrig());
            } else if (type == 3) {
                obj.put("pinyin", item.getValueOrig());
                obj.put("meaning", item.getWordMeaning());
            }
            obj.put("stage", item.getStage());
            obj.put("diff", item.getDiff());
            if (item.getExample() != null) {
                obj.put("example", item.getExample());
            }
            obj.put("mark", item.isMark());
            res.put(obj);
        }
        return res;
    }

    public Map<String, String> getStat() {
        int learn = 0;
        int newItems = 0;
        double sum = 0;
        for (Item item: current) {
            if (item.getStage() == 2) {
                if (item.getDiff() < 0) {
                    newItems++;
                } else {
                    learn++;
                    sum += item.getDiff();
                }
            }
        }
        Map<String, String> res = new HashMap<>();
        res.put("learn", Integer.toString(learn));
        res.put("new", Integer.toString(newItems));
        res.put("sum", Double.toString(sum));
        return res;
    }

}
