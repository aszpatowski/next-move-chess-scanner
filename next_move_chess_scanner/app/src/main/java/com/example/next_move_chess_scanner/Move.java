package com.example.next_move_chess_scanner;

public class Move {
    public Move(String UCImove, String SANmove, String note, String winrate, boolean selected) {
        this.UCIMove = UCImove;
        this.SANMove = SANmove;
        this.note = note;
        this.winRate = winrate;
        this.selected = selected;
    }

    public String getUCIMove() {

        return UCIMove;
    }

    public void setUCIMove(String UCIMove) {
        this.UCIMove = UCIMove;
    }

    public String getSANMove() {
        return SANMove;
    }

    public void setSANMove(String SANMove) {
        this.SANMove = SANMove;
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

    public String getWinRate() {
        return winRate;
    }

    public void setWinRate(String winRate) {
        this.winRate = winRate;
    }

    private String UCIMove;
    private String SANMove;
    private String note;
    private String winRate;
    private boolean selected;

}
