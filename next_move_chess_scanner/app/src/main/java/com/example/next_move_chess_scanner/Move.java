package com.example.next_move_chess_scanner;

public class Move {
    public Move(String UCImove, String SANmove, int score, int rank, String note, String winrate, boolean selected) {
        this.UCImove = UCImove;
        this.SANmove = SANmove;
        this.score = score;
        this.rank = rank;
        this.note = note;
        this.winrate = winrate;
        this.selected = selected;
    }

    public String getUCImove() {

        return UCImove;
    }

    public void setUCImove(String UCImove) {
        this.UCImove = UCImove;
    }

    public String getSANmove() {
        return SANmove;
    }

    public void setSANmove(String SANmove) {
        this.SANmove = SANmove;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getWinrate() {
        return winrate;
    }

    public void setWinrate(String winrate) {
        this.winrate = winrate;
    }

    private String UCImove;
    private String SANmove;
    private int score;
    private int rank;
    private String note;
    private String winrate;
    private boolean selected;

}
