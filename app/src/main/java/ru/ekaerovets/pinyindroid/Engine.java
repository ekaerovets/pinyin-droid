package ru.ekaerovets.pinyindroid;

import java.util.*;

import static ru.ekaerovets.pinyindroid.Difficulty.*;

/**
 * User: dmitry.karyakin
 * Date: 29.11.2016
 */
public class Engine {

    private char type;
    private boolean prelearn;
    private Item[] buffer;
    private int bufSize;

    private Stat stat = new Stat();
    private SyncData data;

    private int today = DateUtils.getDayIndex();

    List<Item> active = new LinkedList<>();
    List<Item> queue = new ArrayList<>();

    int seqNum = 0;

    private Random rnd = new Random();

    private Item[] pickBuf;
    private int[] pickProb;
    private int pickSize;
    private int pickCumulative;

    // in words mode contains the intersection of trivia pinyin and chars,
    // in pinyin mode contains the trivia chars
    private Set<Character> knownItems;
    private int probSum = 0;
    private int cntNormal = 0;

    // Create the engine. Type is one of c, p or w.
    // bufSize is normally 5 for chars/pinyins and 2 for words
    // Return value is the array of 5 chars with their display values
    public Engine(char type, boolean prelearn) {
        this.type = type;
        this.prelearn = prelearn;
        bufSize = type == 'w' ? 2 : 5;
        buffer = new Item[bufSize];
        stat.setSessionStart(new Date());
        data = DataService.loadFromFile();
        getKnownItems();
        prepareLists();
    }

    // Close and save the current session.
    public void shutdown() {
        for (Item item: buffer) {
            freeItem(item);
        }
        stat.setSessionEnd(new Date());
        data.getStat().add(stat);

        DataService.saveToFile(data);
    }

    // Returns false if the next word shown is null
    public boolean hasNext() {
        if (type == 'w') {
            return buffer[1] != null;
        } else {
            return buffer[3] != null;
        }
    }

    // Shows next item
    public void next() {
        freeItem(buffer[0]);
        System.arraycopy(buffer, 1, buffer, 0, bufSize - 1);
        buffer[bufSize - 1] = fetchNext();
    }

    // Process clicking on diff button
    public void toggleDiff() {
        Item item = getCurrentItem();
        if (item != null) {
            int answerStatus = item.getAnswerStatus();
            item.setAnswerStatus(answerStatus == DIFFICULT ? NORMAL : DIFFICULT);
        }
    }

    // Process clicking on item itself. Index is in range [0..4]
    public void toggle(int index) {
        Item item = buffer[index];
        if (item != null) {
            int answerStatus = item.getAnswerStatus();
            item.setAnswerStatus(answerStatus == NORMAL ? DIFFICULT : NORMAL);
        }
    }

    // Process clicking on trivia button.
    public void toggleTrivia() {
        Item item = getCurrentItem();
        if (item != null) {
            int answerStatus = item.getAnswerStatus();
            item.setAnswerStatus(answerStatus == TRIVIA ? NORMAL : TRIVIA);
        }
    }

    public void toggleNew() {
        Item item = getCurrentItem();
        if (item != null) {
            int answerStatus = item.getAnswerStatus();
            item.setAnswerStatus(answerStatus == NEW ? NORMAL : NEW);
        }
    }

    public void toggleMark() {
        Item item = getCurrentItem();
        if (item != null) {
            item.setMark(!item.isMark());
        }
    }

    private Item getCurrentItem() {
        return buffer[type == 'w' ? 1 : 3];
    }

    // When the user clicks left while learning words
    public void compoundL() {
        Item item = getCurrentItem();
        ensureCompoundFlags(item);
        for (int i = 0; i < item.getFlags().length; i++) {
            if ((item.getFlags()[i] & 0x01) != 0) {
                if (i > 0) {
                    item.getFlags()[i - 1] |= 0x01;
                    item.getFlags()[i] ^= 0x01;
                    return;
                }
            }
        }
    }

    // When the user clicks right while learning words
    public void compoundR() {
        Item item = getCurrentItem();
        ensureCompoundFlags(item);
        for (int i = 0; i < item.getFlags().length; i++) {
            if ((item.getFlags()[i] & 0x01) != 0) {
                if (i < item.getFlags().length - 1) {
                    item.getFlags()[i + 1] |= 0x01;
                    item.getFlags()[i] ^= 0x01;
                    return;
                }
            }
        }
        item.getFlags()[0] |= 0x01;
    }

    // Toggle character difficult
    public void compoundC() {
        Item item = getCurrentItem();
        ensureCompoundFlags(item);
        for (int i = 0; i < item.getFlags().length; i++) {
            if ((item.getFlags()[i] & 0x01) != 0) {
                item.getFlags()[i] ^= 0x02;
                break;
            }
        }
    }

    // Toggle pinyin difficult
    public void compoundP() {
        Item item = getCurrentItem();
        ensureCompoundFlags(item);
        for (int i = 0; i < item.getFlags().length; i++) {
            if ((item.getFlags()[i] & 0x01) != 0) {
                item.getFlags()[i] ^= 0x04;
                break;
            }
        }
    }

    private void ensureCompoundFlags(Item item) {
        if (item.getFlags() == null) {
            item.setFlags(new byte[item.getWord().length()]);
        }
    }

    public Item[] getDisplayItems() {
        return buffer;
    }

    public Stat getStat() {
        return stat;
    }

    public int getAnswerCount() {
        return seqNum - type == 'w' ? 1 : 2;
    }

    private void populateBuf() {
        if (type == 'w') {
            buffer[1] = fetchNext();
        } else {
            buffer[3] = fetchNext();
            buffer[4] = fetchNext();
        }
    }

    private double getProb() {
        return probSum / 62.5;
    }

    private double getQueued() {
        if (prelearn) {
            return queue.size();
        }
        int size = queue.size();
        for (Item item: buffer) {
            if (item != null) {
                size++;
            }
        }
        return size;
    }

    private double getLearning() {
        if (prelearn) {
            return 0;
        }
        return cntNormal;
    }

    private Item fetchNext() {
        seqNum++;
        if (prelearn) {
            Item item = fetchNextPrelearn();
            if (item != null) {
                item.setAnswerStatus(NORMAL);
            }
            return item;
        }
        probSum = 0;
        cntNormal = 0;
        int cntTriviaDue = 0; // number of trivia characters ready for review in active
        int cntLearnWait = 0; // number of learn characters that were shown quite recently
        int cntLearnActive = 0; // number of learn characters ready for review

        // calculate stat on active characters
        for (Item item: active) {
            if (item.getStage() == 1) {
                cntTriviaDue++;
            } else {
                // stage == 2
                cntNormal++;
                if (item.getSeqId() > seqNum) {
                    cntLearnWait++;
                } else {
                    cntLearnActive++;
                }
                probSum += 625 / item.getDiff();
            }
        }

        // take buffer into considerations (it may contain learning chars)
        for (Item item: buffer) {
            if (item != null && item.getStage() == 2) {
                cntNormal++;
                probSum += 625 / item.getDiff();
            }
        }

        // take from queue if necessary
        while (probSum < 500) {
            Item item = dequeueRandom();
            if (item == null) {
                break;
            }
            cntLearnActive++;
            cntNormal++;
            probSum += 25;
            active.add(item);
        }

        boolean pickTrivia = rnd.nextDouble() < getProbForNValid(cntNormal);

        // 1 - pick learning, 2 - pick trivia, 3 - pick lwaiting, 4 - null
        int pickType = 0;
        if (pickTrivia) {
            if (cntTriviaDue > 0) {
                pickType = 2;
            } else if (cntLearnActive > 0) {
                pickType = 1;
            }
        } else {
            if (cntLearnActive > 0) {
                pickType = 1;
            } else if (cntTriviaDue > 0) {
                pickType = 2;
            }
        }
        if (pickType == 0) {
            if (cntLearnWait > 0) {
                pickType = 3;
            }
        }
        if (pickType == 0) {
            return null;
        }

        // create buffer for picking item
        if (pickType == 1) {
            ensurePickBuf(cntLearnActive);
        } else if (pickType == 2) {
            ensurePickBuf(cntTriviaDue);
        } else {
            ensurePickBuf(cntLearnWait);
        }

        // iterate active elements once more and pick something
        for (Item item: active) {
            if ((pickType == 1 && item.getStage() == 2 && item.getSeqId() <= seqNum) ||
                    (pickType == 2 && item.getStage() == 1) ||
                    (pickType == 3 && item.getStage() == 2 && item.getSeqId() > seqNum)) {
                addPickItem(item, pickType == 2 ? 1 : 625 / item.getDiff());
            }
        }
        Item item = pick();
        if (item != null) {
            item.setAnswerStatus(NORMAL);
        }
        return item;
    }

    private void ensurePickBuf(int capacity) {
        if (pickBuf == null) {
            pickBuf = new Item[capacity];
            pickProb = new int[capacity];
        } else if (pickBuf.length < capacity) {
            pickBuf = new Item[capacity];
            pickProb = new int[capacity];
        }
        pickSize = 0;
        pickCumulative = 0;
    }

    private void addPickItem(Item item, int prob) {
        pickBuf[pickSize] = item;
        pickCumulative += prob;
        pickProb[pickSize] = pickCumulative;
        pickSize++;
    }

    private Item pick() {
        int r = rnd.nextInt(pickCumulative);
        for (int i = 0; i < pickSize; i++) {
            if (r < pickProb[i]) {
                return pickBuf[i];
            }
        }
        throw new RuntimeException("Something got wrong");
    }

    private Item dequeueRandom() {
        while (true) {
            int size = queue.size();
            if (size == 0) {
                break;
            }
            int idx = rnd.nextInt(size);
            Item item = queue.get(idx);
            queue.remove(idx);
            if (!isKnown(item)) {
                continue;
            }
            item.setDiff(25);
            return item;
        }
        return null;
    }

    private Item fetchNextPrelearn() {
        int cntActive = 0;
        int cntWait = 0;
        for (Item item: queue) {
            if (item.getSeqId() > seqNum) {
                cntWait++;
            } else {
                cntActive++;
            }
        }
        if (cntActive + cntWait == 0) {
            return null;
        }
        boolean pickActive = cntActive > 0;
        ensurePickBuf(pickActive ? cntActive : cntWait);
        for (Item item: queue) {
            if (pickActive == (item.getSeqId() <= seqNum)) {
                addPickItem(item, 1);
            }
        }
        return pick();
    }

    // if a character of the word is marked as diff, remove it from known chars
    private boolean checkCompound(Item item) {
        byte[] flags = item.getFlags();
        if (flags == null) {
            return false;
        }
        boolean isBad = false;
        for (int i = 0; i < flags.length; i++) {
            if ((flags[i] & 0x06) != 0) {
                // the element must be trivia, otherwise the word with it would be suspended
                isBad = true;
                char c = item.getWord().charAt(i);
                knownItems.remove(c);
                if (((flags[i] & 0x02) != 0)) {
                    Item bad = findItem(data.getChars(), Character.toString(c));
                    if (bad != null && bad.getStage() == 1) {
                        bad.setStage(2);
                        bad.setDiff(-1);
                        bad.setDue(-1);
                    }
                }
                if (((flags[i] & 0x04) != 0)) {
                    Item bad = findItem(data.getPinyins(), Character.toString(c));
                    if (bad != null && bad.getStage() == 1) {
                        bad.setStage(2);
                        bad.setDiff(-1);
                        bad.setDue(-1);
                    }
                }
            }
        }
        return isBad;
    }

    private Item findItem(List<Item> items, String key) {
        for (Item item: items) {
            if (item.getWord().equals(key)) {
                return item;
            }
        }
        return null;
    }

    private void freeItem(Item item) {
        if (item == null) {
            return;
        }

        stat.setTotalAnswers(stat.getTotalAnswers() + 1);
        int stage = item.getStage();
        // the word contains a bad character
        if (checkCompound(item)) {
            stat.setReviewWrong(stat.getReviewWrong() + 1);
            if (stage != 3) {
                item.setStage(2);
                item.setDiff(-1);
                item.setDue(-1);
            }
            return;
        }
        int answerStatus = item.getAnswerStatus();
        int diff = item.getDiff();
        int due = item.getDue();
        switch (stage) {
            case 1:
                if (answerStatus == DIFFICULT) {
                    stat.setReviewWrong(stat.getReviewWrong() + 1);
                    stage = 2;
                    diff = -1;
                    due = -1;
                    if (isKnown(item)) {
                        queue.add(item);
                    }
                } else if (answerStatus == NEW) {
                    stat.setReviewWrong(stat.getReviewWrong() + 1);
                    stage = 3;
                    diff = -1;
                    due = -1;
                } else {
                    stat.setReviewCorrect(stat.getReviewCorrect() + 1);
                    diff *= answerStatus == TRIVIA ? 4 : 2;
                    if (diff > 128) {
                        diff = -1;
                        due = -1;
                    } else {
                        due = today + diff;
                    }
                }
                break;
            case 2:
                if (answerStatus == DIFFICULT) {
                    stat.setLearnWrong(stat.getLearnWrong() + 1);
                    diff = 25;
                    item.setSeqId(seqNum + 15);
                    if (isKnown(item)) {
                        queue.add(item);
                    }
                } else if (answerStatus == NORMAL) {
                    stat.setLearnCorrect(stat.getLearnCorrect() + 1);
                    if (diff < 625) {
                        diff *= 5;
                        if (isKnown(item)) {
                            active.add(item);
                        }
                        if (diff == 125) {
                            item.setSeqId(seqNum + 40);
                        } else {
                            item.setSeqId(seqNum + 100);
                        }
                    } else {
                        diff = 4;
                        stage = 1;
                        due = today + diff;
                    }
                } else if (answerStatus == NEW) {
                    stat.setReviewWrong(stat.getReviewWrong() + 1);
                    stage = 3;
                    diff = -1;
                    due = -1;
                } else if (answerStatus == TRIVIA) {
                    stat.setLearnCorrect(stat.getLearnCorrect() + 1);
                    stage = 1;
                    diff = 8;
                    due = today + diff;
                }
                break;
            case 3:
                if (answerStatus == TRIVIA) {
                    stat.setLearnCorrect(stat.getLearnCorrect() + 1);
                    stage = 2;
                    diff = 125;
                } else {
                    stat.setLearnWrong(stat.getLearnWrong() + 1);
                    if (isKnown(item)) {
                        queue.add(item);
                    }
                }
        }
        item.setStage(stage);
        item.setDiff(diff);
        item.setDue(due);
    }

    // remove new items, not due items, items containing unknown characters
    private void prepareLists() {
        List<Item> items;
        if (type == 'c') {
            items = data.getChars();
        } else if (type == 'p') {
            items = data.getPinyins();
        } else {
            items = data.getWords();
        }
        for (Item item: items) {
            if (prelearn) {
                if (item.getStage() != 3) {
                    continue;
                }
            } else {
                if (item.getStage() == 3) {
                    continue;
                }
                if (item.getStage() == 1) {
                    int due = item.getDue();
                    if (due == -1 || due > today) {
                        continue;
                    }
                }
            }

            if (!isKnown(item)) {
                continue;
            }
            if (prelearn || (item.getStage() == 2 && item.getDiff() == -1)) {
                queue.add(item);
            } else {
                active.add(item);
            }
        }
    }

    // true if the item contains all known parts (e.g. pinyin has its corresponding character trivia,
    // word has all its characters and pinyins trivia)
    private boolean isKnown(Item item) {
        if (type == 'p') {
            if (!knownItems.contains(item.getWord().charAt(0))) {
                return false;
            }
        } else if (type == 'w') {
            for (int i = 0; i < item.getWord().length(); i++) {
                if (!knownItems.contains(item.getWord().charAt(i))) {
                    return false;
                }
            }
        }
        return true;
    }

    // Set of trivial elements, used to filter out words, containing unknown chars/pinyins and
    // to filter out pinyins of unknown chars
    private void getKnownItems() {
        this.knownItems = new HashSet<>();
        if (type != 'c') {
            for (Item item: data.getChars()) {
                if (item.getStage() == 1) {
                    this.knownItems.add(item.getWord().charAt(0));
                }
            }
        }
        if (type == 'w') {
            Set<Character> knownPinyins = new HashSet<>();
            for (Item item: data.getPinyins()) {
                if (item.getStage() == 1) {
                    knownPinyins.add(item.getWord().charAt(0));
                }
            }
            Iterator<Character> it = knownItems.iterator();
            while (it.hasNext()) {
                Character next = it.next();
                if (!knownPinyins.contains(next)) {
                    it.remove();
                }
            }
        }
    }

    // probability that we should show Trivia character
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
