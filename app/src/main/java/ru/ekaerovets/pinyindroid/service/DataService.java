package ru.ekaerovets.pinyindroid.service;

import android.content.Context;
import android.view.View;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import ru.ekaerovets.pinyindroid.handlers.SyncResultHandler;
import ru.ekaerovets.pinyindroid.model.Item;
import ru.ekaerovets.pinyindroid.model.SyncData;
import ru.ekaerovets.pinyindroid.utils.DateUtils;

public class DataService {

    private static final String FILENAME = "data.json";
    private static final String SYNC_URL = "http://192.168.0.117:8080/rest/mobile";


    // todo: should be refactored somehow
    // 1 - show the number of due items (black)
    // 2 - show the number of suspended items (reddish)
    // 3 - show the number of new items (blue)
    // 4 - show the number of not due items (green)
    // 5 - sum of the above (gray)
    public static int[] summary = new int[15];
    private static int currentDay = 0;

    public static SyncData loadFromFile(Context ctx) throws IOException {
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(ctx.openFileInput(FILENAME)))) {
            return BinaryService.readSyncData(in);
        }
    }

    public static void saveToFile(SyncData data) throws IOException {
        try (DataOutputStream out =
                     new DataOutputStream(new BufferedOutputStream(ctx.openFileOutput(FILENAME, Context.MODE_PRIVATE)))) {
            updateSummaryInternal(data);
            BinaryService.writeSyncData(data, out);
        }
    }

    public static void sync(final Context ctx, final SyncResultHandler syncResultHandler) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL u = new URL(SYNC_URL);
                    HttpURLConnection conn = (HttpURLConnection) u.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    try {
                        conn.setDoOutput(true);
                        conn.setChunkedStreamingMode(0);
                        OutputStream out = new BufferedOutputStream(conn.getOutputStream());
                        try (BufferedInputStream in = new BufferedInputStream(ctx.openFileInput(FILENAME))) {
                            int val;
                            while ((val = in.read()) != -1) {
                                out.write(val);
                            }
                        }
                        out.flush();

                        int code = conn.getResponseCode();
                        if (code != 200) {
                            syncResultHandler.onSyncCompleted(code);
                            return;
                        }

                        try (BufferedInputStream in = new BufferedInputStream(conn.getInputStream())) {
                            SyncData syncData = BinaryService.readSyncData(new DataInputStream(in));
                            updateSummaryInternal(syncData);
                            saveToFile(syncData);
                        }
                        syncResultHandler.onSyncCompleted(200);


                    }finally {
                        conn.disconnect();
                    }
                } catch (IOException e) {
                    syncResultHandler.onSyncCompleted(0);
                }
            }
        }).start();

    }

    public static boolean updateSummary(Context ctx) {
        if (DateUtils.getDayIndex() != currentDay) {
            try {
                updateSummaryInternal(loadFromFile(ctx));
                return true;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }

    // 1 - show the number of due items (black)
    // 2 - show the number of suspended items (reddish)
    // 3 - show the number of new items (blue)
    // 4 - show the number of not due items (green)
    // 5 - sum of the above (gray)
    private static void updateSummaryInternal(SyncData syncData) {
        int today = DateUtils.getDayIndex();
        currentDay = today;
        for (int i = 0; i < 15; i++) {
            summary[i] = 0;
        }
        Set<Character> knownChars = new HashSet<>();
        for (Item item: syncData.getChars()) {
            if (item.getStage() == 1) {
                knownChars.add(item.getWord().charAt(0));
                if (item.getDue() == -1 || item.getDue() > today) {
                    summary[3]++;
                } else {
                    summary[0]++;
                }
            } else if (item.getStage() == 2) {
                summary[0]++;
            } else if (item.getStage() == 3) {
                summary[2]++;
            }
        }
        summary[4] = summary[0] + summary[2] + summary[3];
        Set<Character> knownCharPin = new HashSet<>();
        for (Item item: syncData.getPinyins()) {
            Character key = item.getWord().charAt(0);
            if (knownChars.contains(key)) {
                knownCharPin.add(key);
                if (item.getStage() == 1) {
                    if (item.getDue() == -1 || item.getDue() > today) {
                        summary[8]++;
                    } else {
                        summary[5]++;
                    }
                } else if (item.getStage() == 2) {
                    summary[5]++;
                } else if (item.getStage() == 3) {
                    summary[7]++;
                }
            } else {
                summary[6]++;
            }
        }
        summary[9] = summary[5] + summary[6] + summary[7] + summary[8];

        for (Item item: syncData.getWords()) {
            boolean isKnown = true;
            for (int i = 0; i < item.getWord().length(); i++) {
                if (!knownCharPin.contains(item.getWord().charAt(i))) {
                    isKnown = false;
                    break;
                }
            }
            if (isKnown) {
                if (item.getStage() == 1) {
                    if (item.getDue() == -1 || item.getDue() > today) {
                        summary[13]++;
                    } else {
                        summary[10]++;
                    }
                } else if (item.getStage() == 2) {
                    summary[10]++;
                } else if (item.getStage() == 3) {
                    summary[12]++;
                }
            } else {
                summary[11]++;
            }
        }
        summary[14] = summary[10] + summary[11] + summary[12] + summary[13];

    }

}
