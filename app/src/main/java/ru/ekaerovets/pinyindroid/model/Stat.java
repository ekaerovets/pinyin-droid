package ru.ekaerovets.pinyindroid.model;

import java.util.Date;

/**
 * User: dmitry.karyakin
 * Date: 21.11.2016
 */
public class Stat {

    private int id;
    private Date sessionStart;
    private Date sessionEnd;
    private char type;
    private int totalAnswers;
    private int reviewWrong;
    private int reviewCorrect;
    private int learnWrong;
    private int learnCorrect;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLearnCorrect() {
        return learnCorrect;
    }

    public void setLearnCorrect(int learnCorrect) {
        this.learnCorrect = learnCorrect;
    }

    public int getLearnWrong() {
        return learnWrong;
    }

    public void setLearnWrong(int learnWrong) {
        this.learnWrong = learnWrong;
    }

    public int getReviewCorrect() {
        return reviewCorrect;
    }

    public void setReviewCorrect(int reviewCorrect) {
        this.reviewCorrect = reviewCorrect;
    }

    public int getReviewWrong() {
        return reviewWrong;
    }

    public void setReviewWrong(int reviewWrong) {
        this.reviewWrong = reviewWrong;
    }

    public Date getSessionEnd() {
        return sessionEnd;
    }

    public void setSessionEnd(Date sessionEnd) {
        this.sessionEnd = sessionEnd;
    }

    public Date getSessionStart() {
        return sessionStart;
    }

    public void setSessionStart(Date sessionStart) {
        this.sessionStart = sessionStart;
    }

    public int getTotalAnswers() {
        return totalAnswers;
    }

    public void setTotalAnswers(int totalAnswers) {
        this.totalAnswers = totalAnswers;
    }

    public char getType() {
        return type;
    }

    public void setType(char type) {
        this.type = type;
    }
}
