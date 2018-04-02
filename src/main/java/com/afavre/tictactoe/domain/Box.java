package com.afavre.tictactoe.domain;

import java.util.Objects;

public class Box {

    private final Symbol symbol;

    private final int x;

    private final int y;


    public Box(Symbol symbol, int x, int y) {
        this.symbol = symbol;
        this.x = x;
        this.y = y;
    }

    public Box(Symbol symbol, GridCorner gridCorner) {
        this.symbol = symbol;
        this.x = gridCorner.getX();
        this.y = gridCorner.getY();
    }

    public Symbol getSymbol() {
        return this.symbol;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    @Override
    public String toString() {
        return this.symbol.getKey();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Box box = (Box) o;
        return x == box.x &&
               y == box.y &&
               symbol == box.symbol;
    }

    @Override
    public int hashCode() {

        return Objects.hash(symbol, x, y);
    }


}
