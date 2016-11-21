package ru.ekaerovets.pinyindroid;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
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
    private int currentType;

    private Map<String, String> pinMap;
    private Map<String, String> charMap;

    private List<Item> current;

    private List<Item> review;
    private int reviewSuccess = 0;
    private int reviewFail = 0;

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
        currentType = type;
        if (type == 1) {
            current = chars;
        } else if (type == 2) {
            current = pinyins;
        } else {
            current = words;
        }

        review = new ArrayList<>();
        for (Item item: current) {
            if (item.getStage() == 1 && (type == 2 || (type == 1 && item.getValueOrig().length() > 0))) {
                review.add(item);
            }
        }
        Collections.shuffle(review);

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
            if (obj.has("radix")) {
                item.setRadix(obj.getString("radix"));
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
        if (item.getAnswerStatus() == Difficulty.REVIEW) {
            reviewSuccess++;
        } else if (item.getAnswerStatus() == Difficulty.QUEUED) {
            reviewFail++;
        }
        item.setUsed(false);
        item.setAnswerStatus(null);
    }

    public Item fetchItem() {
        double[] k = new double[current.size()];
        Item[] items = new Item[current.size()];
        double sum = 0;
        int nValid = 0;
        int nActive = 0;
        Item toAdd = null;
        for (Item o : current) {
            if (o.isUsed() || o.getStage() != 2) {
                continue;
            }
            if (o.getDiff() < 0) {
                toAdd = o;
                nActive++;
                continue;
            }
            sum += o.getDiff();
            k[nValid] = sum;
            items[nValid] = o;
            nValid++;
            nActive++;
        }
        if (sum < 10 && toAdd != null) {
            sum += 0.4;
            toAdd.setDiff(0.4);
            k[nValid] = sum;
            items[nValid] = toAdd;
            nValid++;
        }
        if (review.size() > 0 && Math.random() < getProbForNValid(nActive)) {
            Item res = review.remove(review.size() - 1);
            res.setAnswerStatus(Difficulty.REVIEW);
            return res;
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
            if (item.getRadix() != null) {
                obj.put("radix", item.getRadix());
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
        res.put("review_queue", Integer.toString(review.size()));
        res.put("review_success", Integer.toString(reviewSuccess));
        res.put("review_fail", Integer.toString(reviewFail));
        return res;
    }

    private double getProbForNValid(int nValid) {
        if (nValid < 20) {
            return 0.8;
        } else if (nValid < 100) {
            return (100.0 - nValid) / 160.0 + 0.3;
        } else if (nValid < 200) {
            return (200.0 - nValid) * 0.003;
        } else {
            return 0;
        }
    }

}
