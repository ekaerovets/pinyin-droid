package ru.ekaerovets.pinyindroid;

/**
 * @author karyakin dmitry
 *         date 30.10.15.
 */
public class Item {

    // the key. A single character or a word
    private String word;

    // Meaning of the character / word
    private String meaning;

    private String pinyin;
    // Pronunciation of the character / word

    // stage: 1 - trivia, 2 - learn, 3 - new
    private int stage;

    // for learn is -1 (queue), 25, 125, 625, for trivia is 4 to 128 or -1, for new is always -1
    private int diff;

    // due day. The 1st lesson with AV is the day 1. Days switch on midnight
    private int due;

    // Override flag. If there were any updates from PC, then this flag is set. Any changes from mobile will be lost
    private transient boolean override;

    // Mark flag. Just like using star in Anki
    private boolean mark;

    // Free text - typically usage examples
    private String example;

    // A special field that shows when the item can be reused again in review queue.
    private transient int seqId;

    // A special field that shows the current answer status for an item in mobile app
    private transient int answerStatus;

    // A special field to allow to mark characters/pinyins within a word as difficult
    // each byte corresponds to a character.
    // 0x01 mask indicates cursor, 0x02 - diff char, 0x04 - diff pinyin
    // in most cases the field is nullable
    private transient byte[] flags;

    public Item() {

    }

    public int getAnswerStatus() {
        return answerStatus;
    }

    public void setAnswerStatus(int answerStatus) {
        this.answerStatus = answerStatus;
    }

    public int getDiff() {
        return diff;
    }

    public void setDiff(int diff) {
        this.diff = diff;
    }

    public int getDue() {
        return due;
    }

    public void setDue(int due) {
        this.due = due;
    }

    public String getExample() {
        return example;
    }

    public void setExample(String example) {
        this.example = example;
    }

    public boolean isMark() {
        return mark;
    }

    public void setMark(boolean mark) {
        this.mark = mark;
    }

    public String getMeaning() {
        return meaning;
    }

    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }

    public boolean isOverride() {
        return override;
    }

    public void setOverride(boolean override) {
        this.override = override;
    }

    public String getPinyin() {
        return pinyin;
    }

    public void setPinyin(String pinyin) {
        this.pinyin = pinyin;
    }

    public int getSeqId() {
        return seqId;
    }

    public void setSeqId(int seqId) {
        this.seqId = seqId;
    }

    public int getStage() {
        return stage;
    }

    public void setStage(int stage) {
        this.stage = stage;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public byte[] getFlags() {
        return flags;
    }

    public void setFlags(byte[] flags) {
        this.flags = flags;
    }
}
