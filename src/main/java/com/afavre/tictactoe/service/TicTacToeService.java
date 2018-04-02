package com.afavre.tictactoe.service;

import com.afavre.tictactoe.domain.*;
import com.afavre.tictactoe.exception.BoxAlreadyAssignedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TicTacToeService {

    private TicTacToeGame ticTacToeGame;

    public TicTacToeGame getTicTacToeGame() {
        return this.ticTacToeGame;
    }

    public void createNewGame(Symbol userSymbol) {
        this.ticTacToeGame = new TicTacToeGame(userSymbol);
    }

    public TicTacToeGame next(Symbol symbol, int x, int y) throws BoxAlreadyAssignedException {
        this.ticTacToeGame.putBox(symbol, x, y);
        boolean winner = this.ticTacToeGame.isWinner(symbol);
        if (winner)
            return ticTacToeGame;
        boolean draw = this.ticTacToeGame.isDraw();
        if (draw)
            return ticTacToeGame;

        this.playComputer();

        this.ticTacToeGame.isWinner(symbol);

        this.ticTacToeGame.isDraw();

        return this.ticTacToeGame;
    }

    public TicTacToeGame playComputer() throws BoxAlreadyAssignedException {
        Optional<Box> box = strategyWin(this.ticTacToeGame.getComputerSymbol());
        if (box.isPresent()) {
            this.ticTacToeGame.putBox(box.get());
            return this.ticTacToeGame;
        }

        Optional<Box> boxPreventWin = strategyPreventWin(this.ticTacToeGame.getComputerSymbol());
        if (boxPreventWin.isPresent()) {
            this.ticTacToeGame.putBox(boxPreventWin.get());
            return this.ticTacToeGame;
        }

        Optional<Box> boxFork = strategyFork(this.ticTacToeGame.getComputerSymbol());
        if (boxFork.isPresent()) {
            this.ticTacToeGame.putBox(boxFork.get());
            return this.ticTacToeGame;
        }

        Optional<Box> boxPreventFork = strategyPreventFork(this.ticTacToeGame.getComputerSymbol());
        if (boxPreventFork.isPresent()) {
            this.ticTacToeGame.putBox(boxPreventFork.get());
            return this.ticTacToeGame;
        }

        // Strategy - use the center
        Box boxCenter= this.ticTacToeGame.getGrid().getBox(GridCorner.CENTER);
        if (boxCenter == null) {
            this.ticTacToeGame.getGrid().putBox(this.ticTacToeGame.getComputerSymbol(), GridCorner.CENTER);
            return this.ticTacToeGame;
        }


        Optional<Box> boxOpposite = strategyOppositeCorner(this.ticTacToeGame.getComputerSymbol());
        if (boxOpposite.isPresent()) {
            this.ticTacToeGame.putBox(boxOpposite.get());
            return this.ticTacToeGame;
        }


        Optional<Box> boxEmptyCorner = strategyEmptyCorner(this.ticTacToeGame.getComputerSymbol());
        if (boxEmptyCorner.isPresent()) {
            this.ticTacToeGame.putBox(boxEmptyCorner.get());
            return this.ticTacToeGame;
        }

        Optional<Box> boxEmptySide = strategyEmptySide(this.ticTacToeGame.getComputerSymbol());
        if (boxEmptySide.isPresent()) {
            this.ticTacToeGame.putBox(boxEmptySide.get());
            return this.ticTacToeGame;
        }

        System.out.println("------- Unexpected case ---------");
        return this.ticTacToeGame;
    }

    protected Optional<Box> strategyWin(Symbol symbol) {
        return nextMove(symbol);
    }

    protected Optional<Box> strategyPreventWin(Symbol symbol) {
        Optional<Box> box = nextMove(Symbol.getOpponentSymbol(symbol));
        return box.map(box1 -> new Box(symbol,
                                       box1.getX(),
                                       box1.getY()));
    }

    protected Optional<Box> strategyFork(Symbol symbol) {
        Optional<Box> boxLine = nextMoveForkLineColumn(symbol, true);
        if (boxLine.isPresent())
            return boxLine.map(box1 -> new Box(symbol,
                                               box1.getX(),
                                               box1.getY()));

        Optional<Box> boxColumn = nextMoveForkLineColumn(symbol, false);
        return boxColumn.map(box2 -> new Box(symbol,
                                             box2.getX(),
                                             box2.getY()));

    }

    protected Optional<Box> strategyPreventFork(Symbol symbol) {
        Optional<Box> boxLine = nextMoveForkLineColumn(Symbol.getOpponentSymbol(symbol), true);
        Optional<Box> boxColumn = nextMoveForkLineColumn(Symbol.getOpponentSymbol(symbol), false);
        Optional<List<Box>> boxesDiagonal = forkMoveDiagonal(Symbol.getOpponentSymbol(symbol));

        List<Box> result = new ArrayList<>();

        boxColumn.ifPresent(result::add);
        boxLine.ifPresent(result::add);
        boxesDiagonal.ifPresent(result::addAll);

        // Check if among the possible forks, one will allow the computer to align 2 symbols
        for (Box forkBox : result) {
            Optional<List<Integer>> lineWithNoOpponentSymbole = this.ticTacToeGame.getGrid()
                                                                                  .getLineWithNoOpponentSymbole(symbol,
                                                                                                                forkBox.getX(),
                                                                                                                true);
            if (lineWithNoOpponentSymbole.isPresent() && lineWithNoOpponentSymbole.get().size() < 3)
                return Optional.of(new Box(symbol, forkBox.getX(), forkBox.getY()));

            Optional<List<Integer>> lineWithNoOpponentSymboleColumn = this.ticTacToeGame.getGrid()
                                                                                        .getLineWithNoOpponentSymbole(symbol,
                                                                                                                forkBox.getX(),
                                                                                                                false);
            if (lineWithNoOpponentSymboleColumn.isPresent() && lineWithNoOpponentSymboleColumn.get().size() < 3)
                return Optional.of(new Box(symbol, forkBox.getX(), forkBox.getY()));
        }


        // to check if still needed
        if (boxLine.isPresent()) {
            boolean hasOppositeCorner = this.ticTacToeGame.getGrid().hasTwoOppositeCorners(Symbol.getOpponentSymbol(symbol));

            if (hasOppositeCorner) {
                Optional<Box> boxHasOppositeCorner = strategyEmptySide(symbol);
                return boxHasOppositeCorner.map(box1 -> new Box(symbol,
                                                                box1.getX(),
                                                                box1.getY()));
            }

            return boxLine.map(box1 -> new Box(symbol,
                                               box1.getX(),
                                               box1.getY()));
        }

        if (boxColumn.isPresent()) {
            boolean hasOppositeCorner = this.ticTacToeGame.getGrid().hasTwoOppositeCorners(Symbol.getOpponentSymbol(symbol));

            if (hasOppositeCorner) {
                Optional<Box> boxHasOppositeCorner = strategyEmptySide(symbol);
                return boxHasOppositeCorner.map(box1 -> new Box(symbol,
                                                                box1.getX(),
                                                                box1.getY()));
            }

            return boxLine.map(box1 -> new Box(symbol,
                                               box1.getX(),
                                               box1.getY()));
        }

        return Optional.empty();

    }

    protected Optional<Box> nextMove(Symbol symbol) {
        Optional<Box> boxLine = nextMoveColumnLine(symbol, true);
        if (boxLine.isPresent())
            return boxLine;

        Optional<Box> boxColumn = nextMoveColumnLine(symbol, false);
        if (boxColumn.isPresent())
            return boxColumn;

        return nextMoveDiagonal(symbol);
    }

    private Optional<Box> nextMoveColumnLine(Symbol symbol, boolean checkLine) {
        for (int x = 0; x < TicTacToeGame.GRID_SIZE; x++) {
            Optional<List<Integer>> lineWithNoOpponentSymbole = this.ticTacToeGame.getGrid()
                                                                                  .getLineWithNoOpponentSymbole(symbol,
                                                                                                                x,
                                                                                                                checkLine);

            if (lineWithNoOpponentSymbole.isPresent()) {
                List<Integer> option = lineWithNoOpponentSymbole.get();

                if (option.size() == 1) {
                    int a = checkLine ? x : option.get(0);
                    int b = checkLine ? option.get(0) : x;
                    return Optional.of(new Box(symbol, a, b));
                }
            }
        }
        return Optional.empty();
    }

    private Optional<Box> nextMoveDiagonal(Symbol symbol) {
        List<Box> diagonal1 = new ArrayList<Box>() {{
            add(new Box(symbol, 0, 0));
            add(new Box(symbol, 1, 1));
            add(new Box(symbol, 2, 2));
        }};

        List<Box> diagonal2 = new ArrayList<Box>() {{
            add(new Box(symbol, 2, 0));
            add(new Box(symbol, 1, 1));
            add(new Box(symbol, 0, 2));
        }};

        Optional<List<Box>> boxesDiagonal1 = iterateDiagonal(diagonal1, symbol);

        if (boxesDiagonal1.isPresent() && boxesDiagonal1.get().size() == 1)
            return Optional.of(boxesDiagonal1.get().get(0));

        Optional<List<Box>> boxesDiagonal2 = iterateDiagonal(diagonal2, symbol);

        if (boxesDiagonal2.isPresent() && boxesDiagonal2.get().size() == 1)
            return Optional.of(boxesDiagonal2.get().get(0));

        return Optional.empty();
    }

    private Optional<List<Box>> iterateDiagonal(List<Box> diagonal, Symbol symbol) {
        List<Box> toRemove = new ArrayList<>();
        for (Box boxExpected : diagonal) {
            Box box = this.ticTacToeGame.getGrid().getBox(boxExpected.getX(), boxExpected.getY());
            if (box != null && box.getSymbol() == symbol)
                toRemove.add(box);
            else if (box != null && box.getSymbol() != symbol) {
                return Optional.empty();
            }

        }

        diagonal.removeAll(toRemove);

        return Optional.of(diagonal);

    }

    private Optional<Box> nextMoveForkLineColumn(Symbol symbol, boolean isLine) {
        // A case for with which the column/line contain only one symbol

        for (int x = 0; x < TicTacToeGame.GRID_SIZE; x++) {
            Optional<List<Integer>> lineWithNoOpponentSymbole = this.ticTacToeGame.getGrid()
                                                                                  .getLineWithNoOpponentSymbole(symbol,
                                                                                                                x,
                                                                                                                isLine);

            if (lineWithNoOpponentSymbole.isPresent()) {
                List<Integer> option = lineWithNoOpponentSymbole.get();

                if (option.size() == 2) {

                    Optional<List<Integer>> columnWithNoOpponentSymbole = this.ticTacToeGame.getGrid()
                                                                                            .getLineWithNoOpponentSymbole(symbol,
                                                                                                                          option.get(0),
                                                                                                                          !isLine);

                    if (columnWithNoOpponentSymbole.isPresent()
                        && columnWithNoOpponentSymbole.get().size() == 2)
                        return Optional.of(new Box(symbol, x, option.get(0)));

                    Optional<List<Integer>> columnWithNoOpponentSymbole2 = this.ticTacToeGame.getGrid()
                                                                                            .getLineWithNoOpponentSymbole(symbol,
                                                                                                                          option.get(1),
                                                                                                                          !isLine);

                    if (columnWithNoOpponentSymbole2.isPresent()
                        && columnWithNoOpponentSymbole2.get().size() == 2)
                        return Optional.of(new Box(symbol, x, option.get(1)));

                }
            }
        }

        return Optional.empty();

    }

    private Optional<List<Box>> forkMoveDiagonal(Symbol symbol) {
        List<Box> diagonal1 = new ArrayList<Box>() {{
            add(new Box(symbol, 0, 0));
            add(new Box(symbol, 1, 1));
            add(new Box(symbol, 2, 2));
        }};

        List<Box> diagonal2 = new ArrayList<Box>() {{
            add(new Box(symbol, 2, 0));
            add(new Box(symbol, 1, 1));
            add(new Box(symbol, 0, 2));
        }};

        Optional<List<Box>> boxesDiagonal1 = iterateDiagonal(diagonal1, symbol);

        if (boxesDiagonal1.isPresent() && boxesDiagonal1.get().size() == 2)
            return boxesDiagonal1;

        Optional<List<Box>> boxesDiagonal2 = iterateDiagonal(diagonal2, symbol);

        if (boxesDiagonal2.isPresent() && boxesDiagonal2.get().size() == 2)
            return boxesDiagonal2;

        return Optional.empty();
    }

    private Optional<Box> strategyOppositeCorner(Symbol symbol) {

        Box topLeftCorner = this.ticTacToeGame.getGrid().getBox(GridCorner.TOP_LEFT);
        Box bottomRightCorner = this.ticTacToeGame.getGrid().getBox(GridCorner.BOTTOM_RIGHT);
        if (topLeftCorner != null
            && topLeftCorner.getSymbol() == Symbol.getOpponentSymbol(symbol)
            && bottomRightCorner == null)
            return Optional.of(new Box(symbol, GridCorner.BOTTOM_RIGHT));

        if (bottomRightCorner != null
            && bottomRightCorner.getSymbol() == Symbol.getOpponentSymbol(symbol)
            && topLeftCorner == null)
            return Optional.of(new Box(symbol, GridCorner.TOP_LEFT));

        Box bottomLeftCorner = this.ticTacToeGame.getGrid().getBox(GridCorner.BOTTOM_LEFT);
        Box topRightCorner = this.ticTacToeGame.getGrid().getBox(GridCorner.TOP_RIGHT);
        if (bottomLeftCorner != null
            && bottomLeftCorner.getSymbol() == Symbol.getOpponentSymbol(symbol)
            && topRightCorner == null)
            return Optional.of(new Box(symbol, GridCorner.TOP_RIGHT));

        if (topRightCorner != null
            && topRightCorner.getSymbol() == Symbol.getOpponentSymbol(symbol)
            && bottomLeftCorner == null)
            return Optional.of(new Box(symbol, GridCorner.BOTTOM_LEFT));

        return Optional.empty();

    }

    private Optional<Box> strategyEmptyCorner(Symbol symbol) {

        Box topLeftCorner = this.ticTacToeGame.getGrid().getBox(GridCorner.TOP_LEFT);
        if (topLeftCorner == null)
            return Optional.of(new Box(symbol, GridCorner.TOP_LEFT));

        Box topRightCorner = this.ticTacToeGame.getGrid().getBox(GridCorner.TOP_RIGHT);
        if (topRightCorner == null)
            return Optional.of(new Box(symbol, GridCorner.TOP_RIGHT));

        Box bottomLeftCorner = this.ticTacToeGame.getGrid().getBox(GridCorner.BOTTOM_LEFT);
        if (bottomLeftCorner == null)
            return Optional.of(new Box(symbol, GridCorner.BOTTOM_LEFT));

        Box bottomRightCorner = this.ticTacToeGame.getGrid().getBox(GridCorner.BOTTOM_RIGHT);
        if (bottomRightCorner == null)
            return Optional.of(new Box(symbol, GridCorner.BOTTOM_RIGHT));

        return Optional.empty();
    }

    private Optional<Box> strategyEmptySide(Symbol symbol) {

        Box leftSide = this.ticTacToeGame.getGrid().getBox(GridCorner.LEFT_SIDE);
        if (leftSide == null)
            return Optional.of(new Box(symbol, GridCorner.LEFT_SIDE));

        Box rightSide = this.ticTacToeGame.getGrid().getBox(GridCorner.RIGHT_SIDE);
        if (rightSide == null)
            return Optional.of(new Box(symbol, GridCorner.RIGHT_SIDE));

        Box topSide = this.ticTacToeGame.getGrid().getBox(GridCorner.TOP_SIDE);
        if (topSide == null)
            return Optional.of(new Box(symbol, GridCorner.TOP_SIDE));

        Box bottomSide = this.ticTacToeGame.getGrid().getBox(GridCorner.BOTTOM_SIDE);
        if (bottomSide == null)
            return Optional.of(new Box(symbol, GridCorner.BOTTOM_SIDE));

        return Optional.empty();
    }

}
