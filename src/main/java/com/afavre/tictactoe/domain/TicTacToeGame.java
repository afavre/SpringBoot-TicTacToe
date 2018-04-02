package com.afavre.tictactoe.domain;

import com.afavre.tictactoe.exception.BoxAlreadyAssignedException;

import java.util.UUID;

import static com.afavre.tictactoe.util.Utils.isSameSymbole;

public class TicTacToeGame {

    public final static int GRID_SIZE = 3;

    private final String gameId;

    private Symbol userSymbol;
    private Symbol computerSymbol;

    private boolean terminated = false;

    private Symbol winner;

    private final Grid grid;

    public TicTacToeGame(Symbol userSymbol) {
        this.grid = new Grid(GRID_SIZE);
        this.gameId = UUID.randomUUID().toString();
        this.userSymbol = userSymbol;
        this.computerSymbol = userSymbol == Symbol.X ? Symbol.O : Symbol.X;
    }

    public boolean isTerminated() {
        return this.terminated;
    }

    public Grid getGrid() {
        return this.grid;
    }

    public String getGameId() {
        return this.gameId;
    }

    public Symbol getWinner() {
        return this.winner;
    }

    public Symbol getUserSymbol() {
        return this.userSymbol;
    }

    public Symbol getComputerSymbol() {
        return this.computerSymbol;
    }

    public synchronized void putBox(Symbol symbol, int x, int y) throws BoxAlreadyAssignedException {
        if (this.grid.getBox(x,y) != null)
            throw new BoxAlreadyAssignedException();
        this.grid.putBox(symbol, x, y);
    }

    public synchronized void putBox(Box box) throws BoxAlreadyAssignedException {
        if (this.grid.getBox(box.getX(),box.getY()) != null)
            throw new BoxAlreadyAssignedException();
        this.grid.putBox(box);
    }

    public synchronized boolean isWinner(Symbol symbol) {
        boolean b = checkColumn(symbol)
                    || checkLine(symbol)
                    || checkDiagonal(symbol);
        if (b) {
            this.winner = symbol;
            this.terminated = true;
        }

        return b;
    }

    public synchronized boolean isDraw() {
        for (Box[] xGrid : this.grid.getGrid())
            for (Box xYGrid : xGrid)
                if (xYGrid == null)
                    return false;

        this.terminated = true;
        return true;
    }

    private boolean checkLine(Symbol symbol) {
        for (int x = 0; x < GRID_SIZE; x++) {
            int lineOccurence = 0;
            for (int y = 0; y < GRID_SIZE; y++) {
                if (isSameSymbole(this.grid.getBox(x, y), symbol))
                    lineOccurence++;
            }

            if (lineOccurence == GRID_SIZE)
                return true;

        }
        return false;
    }


    /**
     * @param symbol
     * @return
     */
    private boolean checkColumn(Symbol symbol) {
        for (int y = 0; y < GRID_SIZE; y++) {
            int lineOccurence = 0;
            for (int x = 0; x < GRID_SIZE; x++) {
                if (isSameSymbole(this.grid.getBox(x, y), symbol))
                    lineOccurence++;
            }

            if (lineOccurence == GRID_SIZE)
                return true;

        }
        return false;
    }

    private boolean checkDiagonal(Symbol symbol) {

        return (isSameSymbole(this.grid.getBox(0,0), symbol)
               && isSameSymbole(this.grid.getBox(1,1), symbol)
               && isSameSymbole(this.grid.getBox(2,2), symbol))
               || (isSameSymbole(this.grid.getBox(0, 2), symbol)
                  && isSameSymbole(this.grid.getBox(1,1), symbol)
                  && isSameSymbole(this.grid.getBox(2,0), symbol));
    }

}
