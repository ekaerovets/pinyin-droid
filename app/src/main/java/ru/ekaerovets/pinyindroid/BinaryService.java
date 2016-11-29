package ru.ekaerovets.pinyindroid;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * User: dmitry.karyakin
 * Date: 29.11.2016
 */
public class BinaryService {

    private static Item readItem(DataInputStream in) throws IOException {
        Item item = new Item();
        byte flags = in.readByte();
        item.setWord(in.readUTF());
        if ((flags & 0x04) != 0) {
            item.setMeaning(in.readUTF());
        }
        if ((flags & 0x08) != 0) {
            item.setPinyin(in.readUTF());
        }
        item.setStage(in.readInt());
        item.setDiff(in.readInt());
        item.setDue(in.readInt());
        item.setMark((flags & 0x20) != 0);
        if ((flags & 0x10) != 0) {
            item.setExample(in.readUTF());
        }
        return item;
    }

    private static void writeItem(Item item, DataOutputStream out) throws IOException {
        byte flags = 0;
        if (item.getMeaning() != null) {
            flags |= 0x04;
        }
        if (item.getPinyin() != null) {
            flags |= 0x08;
        }
        if (item.getExample() != null) {
            flags |= 0x10;
        }
        if (item.isMark()) {
            flags |= 0x20;
        }
        out.writeByte(flags);
        out.writeUTF(item.getWord());
        if (item.getMeaning() != null) {
            out.writeUTF(item.getMeaning());
        }
        if (item.getPinyin() != null) {
            out.writeUTF(item.getPinyin());
        }
        out.writeInt(item.getStage());
        out.writeInt(item.getDiff());
        out.writeInt(item.getDue());
        if (item.getExample() != null) {
            out.writeUTF(item.getExample());
        }
    }

    private static Stat readStat(DataInputStream in) throws IOException {
        Stat stat = new Stat();
        stat.setSessionStart(new Date(in.readLong()));
        stat.setSessionEnd(new Date(in.readLong()));
        stat.setType(in.readChar());
        stat.setTotalAnswers(in.readInt());
        stat.setReviewWrong(in.readInt());
        stat.setReviewCorrect(in.readInt());
        stat.setLearnWrong(in.readInt());
        stat.setLearnCorrect(in.readInt());
        return stat;
    }

    private static void writeStat(Stat stat, DataOutputStream out) throws IOException {
        out.writeLong(stat.getSessionStart().getTime());
        out.writeLong(stat.getSessionEnd().getTime());
        out.writeChar(stat.getType());
        out.writeInt(stat.getTotalAnswers());
        out.writeInt(stat.getReviewWrong());
        out.writeInt(stat.getReviewCorrect());
        out.writeInt(stat.getLearnWrong());
        out.writeInt(stat.getLearnCorrect());
    }

    public static SyncData readSyncData(DataInputStream in) throws IOException {
        SyncData data = new SyncData();
        int len = in.readInt();
        List<Item> chars = new ArrayList<>(len);
        for (int i = 0; i < len; i++) {
            chars.add(readItem(in));
        }
        data.setChars(chars);
        len = in.readInt();
        List<Item> pinyins = new ArrayList<>(len);
        for (int i = 0; i < len; i++) {
            pinyins.add(readItem(in));
        }
        data.setPinyins(pinyins);
        len = in.readInt();
        List<Item> words = new ArrayList<>(len);
        for (int i = 0; i < len; i++) {
            words.add(readItem(in));
        }
        data.setWords(words);
        len = in.readInt();
        List<Stat> stats = new ArrayList<>(len);
        for (int i = 0; i < len; i++) {
            stats.add(readStat(in));
        }
        data.setStat(stats);
        return data;
    }

    public static void writeSyncData(SyncData data, DataOutputStream out) throws IOException {
        out.writeInt(data.getChars().size());
        for (Item item: data.getChars()) {
            writeItem(item, out);
        }
        out.writeInt(data.getPinyins().size());
        for (Item item: data.getPinyins()) {
            writeItem(item, out);
        }
        out.writeInt(data.getWords().size());
        for (Item item: data.getWords()) {
            writeItem(item, out);
        }
        out.writeInt(data.getStat().size());
        for (Stat stat: data.getStat()) {
            writeStat(stat, out);
        }
    }

}
