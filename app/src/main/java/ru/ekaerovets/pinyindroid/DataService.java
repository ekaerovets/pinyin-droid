package ru.ekaerovets.pinyindroid;

import android.content.Context;
import android.view.View;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DataService {

    private static final String FILENAME = "data.json";

    private static final String STAT_FILE = "stat.dat";

    public static void sync(Context ctx, View v, String url, AjaxHandler handler) {
        String input = loadFromFile(ctx);
        List<StatEntry> statEntries = loadAllStat(ctx);

        AjaxHelper.asyncQuery(v, url, appendStat(in, statEntries), handler);
    }

    public static void saveToFile(Context ctx, String json) {
        try {
            FileOutputStream out = ctx.openFileOutput(FILENAME, Context.MODE_PRIVATE);
            out.write(json.getBytes());
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String loadFromFile(Context ctx) {
        try {
            FileInputStream in = ctx.openFileInput(FILENAME);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();
            return sb.toString();
        } catch (IOException e) {
            return "{chars: [], pinyins: []}";
        }
    }

    public static String appendStat(String input, List<StatEntry> entries) {
        StringBuilder sb = new StringBuilder();
        sb.append("{stat: [");
        boolean isFirst = true;
        for (StatEntry e: entries) {
            if (isFirst) {
                isFirst = false;
            } else {
                sb.append(", ");
            }
            sb.append("{sessionStart: ").append(e.getSessionStart().getTime()).append(", ");
            sb.append("sessionEnd: ").append(e.getSessionEnd().getTime()).append(", ");
            sb.append("type: \"").append(e.getType()).append("\", ");
            sb.append("totalAnswers: ").append(e.getTotalAnswers()).append(", ");
            sb.append("learnCorrect: ").append(e.getLearnCorrect()).append(", ");
            sb.append("learnWrong: ").append(e.getLearnWrong()).append(", ");
            sb.append("reviewCorrect: ").append(e.getReviewCorrect()).append(", ");
            sb.append("reviewWrong: ").append(e.getReviewWrong()).append("}");
        }
        sb.append("], ");
        sb.append(input.substring(1));
        return sb.toString();
    }

    public static List<StatEntry> loadAllStat(Context ctx) {
        List<StatEntry> res = new ArrayList<>();
        try {
            FileInputStream in = ctx.openFileInput(STAT_FILE);
            DataInputStream din = new DataInputStream(in);
            StatEntry e = new StatEntry();
            e.setSessionStart(new Date(din.readLong()));
            e.setSessionEnd(new Date(din.readLong()));
            e.setType(din.readChar());
            e.setTotalAnswers(din.readInt());
            e.setLearnCorrect(din.readInt());
            e.setLearnWrong(din.readInt());
            e.setReviewCorrect(din.readInt());
            e.setReviewWrong(din.readInt());
            res.add(e);

        } catch (EOFException ignore) {
        } catch (IOException e) {
            e.printStackTrace();
        }
        return e;
    }

    public static void clearStat(Context ctx) {
        ctx.deleteFile(STAT_FILE);
    }

    public static void saveStat(Context ctx, StatEntry entry) {
        try {
            FileOutputStream out = ctx.openFileOutput(STAT_FILE, Context.MODE_APPEND);
            DataOutputStream dout = new DataOutputStream(out);
            dout.writeLong(entry.getSessionStart().getTime());
            dout.writeLong(entry.getSessionEnd().getTime());
            dout.writeChar(entry.getType());
            dout.writeInt(entry.getTotalAnswers());
            dout.writeInt(entry.getLearnCorrect());
            dout.writeInt(entry.getLearnWrong());
            dout.writeInt(entry.getReviewCorrect());
            dout.writeInt(entry.getReviewWrong());
            dout.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
