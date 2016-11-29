package ru.ekaerovets.pinyindroid;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * User: dmitry.karyakin
 * Date: 29.11.2016
 */
public class Engine {

    private char type;
    private Item[] buffer;
    private int bufSize;

    private Stat stat = new Stat();
    private SyncData data;

    // in words mode contains the intersection of trivia pinyin and chars,
    // in pinyin mode contains the trivia chars
    private Set<String> knownItems;

    // Create the engine. Type is one of c, p or w.
    // bufSize is normally 5 for chars/pinyins and 2 for words
    // Return value is the array of 5 chars with their display values
    public Engine(char type) {
        this.type = type;
        bufSize = type == 'w' ? 2 : 5;
        buffer = new Item[bufSize];
        stat.setSessionStart(new Date());
        data = DataService.loadFromFile();

    }

    // Close and save the current session.
    public void shutdown() {

    }

    // Returns false if the next word shown is null
    public boolean hasNext() {

    }

    // Shows next item
    public void next() {

    }

    // Process clicking on diff button
    public void toggleDiff() {

    }

    // Process clicking on item itself. Index is in range [0..4]
    public void toggle(int index) {

    }

    // Process clicking on trivia button.
    public void toggleTrivia(int index) {

    }

    public void toggleMark() {

    }

    // When the user clicks left while learning words
    public void compoundL() {

    }

    // When the user clicks right while learning words
    public void compoundR() {

    }

    // Toggle character difficult
    public void compoundC() {

    }

    // Toggle pinyin difficult
    public void compoundP() {

    }

    public Item[] getDisplayItems() {

    }

    public Stat getStat() {

    }


    private Item fetchNext() {

    }

    // Set of trivial elements, used to filter out words, containing unknown chars/pinyins and
    // to filter out pinyins of unknown chars
    private void getKnownItems() {
        this.knownItems = new HashSet<>();
        if (this.type != 'c') {
            for (Item item: data.getChars()) {
                if (item.getStage() == 1) {
                    this.knownItems.add(item.getWord());
                }
            }
        }
        if (this.type == 'w') {
            Set<String> knownPinyins = new HashSet<>();
            for (Item item: data.getPinyins()) {
                if (item.getStage() == 1) {
                    knownPinyins.add(item.getWord());
                }
            }
            Iterator<String> it = knownItems.iterator();
            while (it.hasNext()) {
                String next = it.next();
                if (!knownPinyins.contains(next)) {
                    it.remove();
                }
            }
        }
    }

}
