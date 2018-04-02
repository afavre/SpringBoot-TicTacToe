package com.afavre.tictactoe.domain;

public enum GridCorner {

    TOP_LEFT(0, 0),
    TOP_RIGHT(2, 0),
    BOTTOM_LEFT(0, 2),
    BOTTOM_RIGHT(2, 2),
    LEFT_SIDE(0, 1),
    RIGHT_SIDE(2, 1),
    TOP_SIDE(1, 0),
    BOTTOM_SIDE(1, 2),
    CENTER(1, 1);

    private int x;
    private int y;

    GridCorner(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
