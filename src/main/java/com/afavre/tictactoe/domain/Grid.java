package com.afavre.tictactoe.domain;

import jdk.nashorn.internal.runtime.options.Option;
import org.omg.Messaging.SYNC_WITH_TRANSPORT;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.afavre.tictactoe.util.Utils.isSameSymbole;

public class Grid {

    private Box[][] grid;

    public Grid(int gridSize) {
        this.grid = new Box[gridSize][gridSize];
    }

    public Box[][] getGrid() {
        return this.grid;
    }

    public Box getBox(int x, int y) {
        return this.grid[x][y];
    }

    public Box getBox(GridCorner corner) {
        return this.getBox(corner.getX(), corner.getY());
    }

    public void putBox(Symbol symbol, int x, int y) {
        this.grid[x][y] = new Box(symbol, x, y);
    }

    public void putBox(Box box) {
        this.grid[box.getX()][box.getY()] = box;
    }

    public void putBox(Symbol symbol, GridCorner gridCorner) {
        this.grid[gridCorner.getX()][gridCorner.getY()] = new Box(symbol,
                                                                  gridCorner.getX(),
                                                                  gridCorner.getY());
    }


    /**
     * Retrieve the list of empty box for lineNumber (columnNumber is isLine is false)
     * return Optional.empty() if an opponent symbol is found
     * @param symbol
     * @param lineNumber
     * @param isLine
     * @return
     */
    public Optional<List<Integer>> getLineWithNoOpponentSymbole(Symbol symbol,
                                                                int lineNumber,
                                                                boolean isLine) {
        List<Integer> options = new ArrayList<Integer>() {{
            add(0);
            add(1);
            add(2);
        }};

        for (int y = 0; y < TicTacToeGame.GRID_SIZE; y++) {

            int a = isLine ? lineNumber : y;
            int b = isLine ? y : lineNumber;

            int toRemove = isLine ? b : a;

            if (isSameSymbole(this.getBox(a, b), symbol))
                options.remove(options.indexOf(toRemove));
                // opponent symbol on the line - move to next line
            else if (isSameSymbole(this.getBox(a, b), Symbol.getOpponentSymbol(symbol))) {
                return Optional.empty();
            }
        }

        return Optional.of(options);
    }

    public boolean hasTwoOppositeCorners(Symbol symbol) {
        return isSameSymbole(this.getBox(GridCorner.TOP_LEFT), symbol)
               && isSameSymbole(this.getBox(GridCorner.BOTTOM_RIGHT), symbol)
               || isSameSymbole(this.getBox(GridCorner.BOTTOM_LEFT), symbol)
                  && isSameSymbole(this.getBox(GridCorner.TOP_RIGHT), symbol);


    }

}
