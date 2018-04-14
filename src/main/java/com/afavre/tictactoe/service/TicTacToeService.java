package com.afavre.tictactoe.service;

import com.afavre.tictactoe.domain.Box;
import com.afavre.tictactoe.domain.GridCorner;
import com.afavre.tictactoe.domain.Symbol;
import com.afavre.tictactoe.domain.TicTacToeGame;
import com.afavre.tictactoe.domain.request.NewGameRequest;
import com.afavre.tictactoe.exception.BoxAlreadyAssignedException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TicTacToeService {

    // Potential memory leak if the game are not deleted
    private Map<String, TicTacToeGame> games = new ConcurrentHashMap<>();

    public TicTacToeGame getTicTacToeGame(String gameId) {
        return this.games.get(gameId);
    }

    public TicTacToeGame createNewGame(NewGameRequest newGameRequest) {
        String gameId = UUID.randomUUID().toString();
        TicTacToeGame game = new TicTacToeGame(gameId,
                                               newGameRequest.getUserSymbole(),
                                               newGameRequest.getUsername());
        this.games.put(gameId, game);
        return game;
    }

    public void deleteGame(String gameId) {
        this.games.remove(gameId);
    }

    public TicTacToeGame next(TicTacToeGame ticTacToeGame,
                              Symbol symbol,
                              int x,
                              int y) throws BoxAlreadyAssignedException {
        ticTacToeGame.putBox(symbol, x, y);
        boolean winner = ticTacToeGame.isWinner(symbol);
        if (winner)
            return ticTacToeGame;
        boolean draw = ticTacToeGame.isDraw();
        if (draw)
            return ticTacToeGame;

        this.playComputer(ticTacToeGame);

        ticTacToeGame.isWinner(symbol);

        ticTacToeGame.isDraw();

        return ticTacToeGame;
    }

    public TicTacToeGame playComputer(TicTacToeGame ticTacToeGame) throws BoxAlreadyAssignedException {
        Optional<Box> box = strategyWin(ticTacToeGame,
                                        ticTacToeGame.getComputerSymbol());
        if (box.isPresent()) {
            ticTacToeGame.putBox(box.get());
            return ticTacToeGame;
        }

        Optional<Box> boxPreventWin = strategyPreventWin(ticTacToeGame,
                                                         ticTacToeGame.getComputerSymbol());
        if (boxPreventWin.isPresent()) {
            ticTacToeGame.putBox(boxPreventWin.get());
            return ticTacToeGame;
        }

        Optional<Box> boxFork = strategyFork(ticTacToeGame,
                                             ticTacToeGame.getComputerSymbol());
        if (boxFork.isPresent()) {
            ticTacToeGame.putBox(boxFork.get());
            return ticTacToeGame;
        }

        Optional<Box> boxPreventFork = strategyPreventFork(ticTacToeGame,
                                                           ticTacToeGame.getComputerSymbol());
        if (boxPreventFork.isPresent()) {
            ticTacToeGame.putBox(boxPreventFork.get());
            return ticTacToeGame;
        }

        // Strategy - use the center
        Box boxCenter= ticTacToeGame.getGrid().getBox(GridCorner.CENTER);
        if (boxCenter == null) {
            ticTacToeGame.getGrid().putBox(ticTacToeGame.getComputerSymbol(), GridCorner.CENTER);
            return ticTacToeGame;
        }


        Optional<Box> boxOpposite = strategyOppositeCorner(ticTacToeGame, ticTacToeGame.getComputerSymbol());
        if (boxOpposite.isPresent()) {
            ticTacToeGame.putBox(boxOpposite.get());
            return ticTacToeGame;
        }


        Optional<Box> boxEmptyCorner = strategyEmptyCorner(ticTacToeGame, ticTacToeGame.getComputerSymbol());
        if (boxEmptyCorner.isPresent()) {
            ticTacToeGame.putBox(boxEmptyCorner.get());
            return ticTacToeGame;
        }

        Optional<Box> boxEmptySide = strategyEmptySide(ticTacToeGame, ticTacToeGame.getComputerSymbol());
        if (boxEmptySide.isPresent()) {
            ticTacToeGame.putBox(boxEmptySide.get());
            return ticTacToeGame;
        }

        System.out.println("------- Unexpected case ---------");
        return ticTacToeGame;
    }

    protected Optional<Box> strategyWin(TicTacToeGame ticTacToeGame, Symbol symbol) {
        return nextMove(ticTacToeGame, symbol);
    }

    protected Optional<Box> strategyPreventWin(TicTacToeGame ticTacToeGame, Symbol symbol) {
        Optional<Box> box = nextMove(ticTacToeGame, Symbol.getOpponentSymbol(symbol));
        return box.map(box1 -> new Box(symbol,
                                       box1.getX(),
                                       box1.getY()));
    }

    protected Optional<Box> strategyFork(TicTacToeGame ticTacToeGame, Symbol symbol) {
        Optional<Box> boxLine = nextMoveForkLineColumn(ticTacToeGame, symbol, true);
        if (boxLine.isPresent())
            return boxLine.map(box1 -> new Box(symbol,
                                               box1.getX(),
                                               box1.getY()));

        Optional<Box> boxColumn = nextMoveForkLineColumn(ticTacToeGame, symbol, false);
        return boxColumn.map(box2 -> new Box(symbol,
                                             box2.getX(),
                                             box2.getY()));

    }

    protected Optional<Box> strategyPreventFork(TicTacToeGame ticTacToeGame, Symbol symbol) {
        Optional<Box> boxLine = nextMoveForkLineColumn(ticTacToeGame,
                                                       Symbol.getOpponentSymbol(symbol),
                                                       true);
        Optional<Box> boxColumn = nextMoveForkLineColumn(ticTacToeGame,
                                                         Symbol.getOpponentSymbol(symbol),
                                                         false);
        Optional<List<Box>> boxesDiagonal = forkMoveDiagonal(ticTacToeGame,
                                                             Symbol.getOpponentSymbol(symbol));

        List<Box> result = new ArrayList<>();

        boxColumn.ifPresent(result::add);
        boxLine.ifPresent(result::add);
        boxesDiagonal.ifPresent(result::addAll);

        // Check if among the possible forks, one will allow the computer to align 2 symbols
        for (Box forkBox : result) {
            Optional<List<Integer>> lineWithNoOpponentSymbole = ticTacToeGame.getGrid()
                                                                                  .getLineWithNoOpponentSymbole(symbol,
                                                                                                                forkBox.getX(),
                                                                                                                true);
            if (lineWithNoOpponentSymbole.isPresent() && lineWithNoOpponentSymbole.get().size() < 3)
                return Optional.of(new Box(symbol, forkBox.getX(), forkBox.getY()));

            Optional<List<Integer>> lineWithNoOpponentSymboleColumn = ticTacToeGame.getGrid()
                                                                                        .getLineWithNoOpponentSymbole(symbol,
                                                                                                                forkBox.getX(),
                                                                                                                false);
            if (lineWithNoOpponentSymboleColumn.isPresent() && lineWithNoOpponentSymboleColumn.get().size() < 3)
                return Optional.of(new Box(symbol, forkBox.getX(), forkBox.getY()));
        }


        // to check if still needed
        if (boxLine.isPresent()) {
            boolean hasOppositeCorner = ticTacToeGame.getGrid().hasTwoOppositeCorners(Symbol.getOpponentSymbol(symbol));

            if (hasOppositeCorner) {
                Optional<Box> boxHasOppositeCorner = strategyEmptySide(ticTacToeGame, symbol);
                return boxHasOppositeCorner.map(box1 -> new Box(symbol,
                                                                box1.getX(),
                                                                box1.getY()));
            }

            return boxLine.map(box1 -> new Box(symbol,
                                               box1.getX(),
                                               box1.getY()));
        }

        if (boxColumn.isPresent()) {
            boolean hasOppositeCorner = ticTacToeGame.getGrid().hasTwoOppositeCorners(Symbol.getOpponentSymbol(symbol));

            if (hasOppositeCorner) {
                Optional<Box> boxHasOppositeCorner = strategyEmptySide(ticTacToeGame, symbol);
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

    protected Optional<Box> nextMove(TicTacToeGame ticTacToeGame,
                                     Symbol symbol) {
        Optional<Box> boxLine = nextMoveColumnLine(ticTacToeGame, symbol, true);
        if (boxLine.isPresent())
            return boxLine;

        Optional<Box> boxColumn = nextMoveColumnLine(ticTacToeGame, symbol, false);
        if (boxColumn.isPresent())
            return boxColumn;

        return nextMoveDiagonal(ticTacToeGame, symbol);
    }

    private Optional<Box> nextMoveColumnLine(TicTacToeGame ticTacToeGame,
                                             Symbol symbol, boolean checkLine) {
        for (int x = 0; x < TicTacToeGame.GRID_SIZE; x++) {
            Optional<List<Integer>> lineWithNoOpponentSymbole = ticTacToeGame.getGrid()
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

    private Optional<Box> nextMoveDiagonal(TicTacToeGame ticTacToeGame, Symbol symbol) {
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

        Optional<List<Box>> boxesDiagonal1 = iterateDiagonal(ticTacToeGame, diagonal1, symbol);

        if (boxesDiagonal1.isPresent() && boxesDiagonal1.get().size() == 1)
            return Optional.of(boxesDiagonal1.get().get(0));

        Optional<List<Box>> boxesDiagonal2 = iterateDiagonal(ticTacToeGame, diagonal2, symbol);

        if (boxesDiagonal2.isPresent() && boxesDiagonal2.get().size() == 1)
            return Optional.of(boxesDiagonal2.get().get(0));

        return Optional.empty();
    }

    private Optional<List<Box>> iterateDiagonal(TicTacToeGame ticTacToeGame,
                                                List<Box> diagonal,
                                                Symbol symbol) {
        List<Box> toRemove = new ArrayList<>();
        for (Box boxExpected : diagonal) {
            Box box = ticTacToeGame.getGrid().getBox(boxExpected.getX(), boxExpected.getY());
            if (box != null && box.getSymbol() == symbol)
                toRemove.add(box);
            else if (box != null && box.getSymbol() != symbol) {
                return Optional.empty();
            }

        }

        diagonal.removeAll(toRemove);

        return Optional.of(diagonal);

    }

    private Optional<Box> nextMoveForkLineColumn(TicTacToeGame ticTacToeGame,
                                                 Symbol symbol,
                                                 boolean isLine) {
        // A case for with which the column/line contain only one symbol

        for (int x = 0; x < TicTacToeGame.GRID_SIZE; x++) {
            Optional<List<Integer>> lineWithNoOpponentSymbole = ticTacToeGame.getGrid()
                                                                                  .getLineWithNoOpponentSymbole(symbol,
                                                                                                                x,
                                                                                                                isLine);

            if (lineWithNoOpponentSymbole.isPresent()) {
                List<Integer> option = lineWithNoOpponentSymbole.get();

                if (option.size() == 2) {

                    Optional<List<Integer>> columnWithNoOpponentSymbole = ticTacToeGame.getGrid()
                                                                                            .getLineWithNoOpponentSymbole(symbol,
                                                                                                                          option.get(0),
                                                                                                                          !isLine);

                    if (columnWithNoOpponentSymbole.isPresent()
                        && columnWithNoOpponentSymbole.get().size() == 2)
                        return Optional.of(new Box(symbol, x, option.get(0)));

                    Optional<List<Integer>> columnWithNoOpponentSymbole2 = ticTacToeGame.getGrid()
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

    private Optional<List<Box>> forkMoveDiagonal(TicTacToeGame ticTacToeGame,
                                                 Symbol symbol) {
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

        Optional<List<Box>> boxesDiagonal1 = iterateDiagonal(ticTacToeGame, diagonal1, symbol);

        if (boxesDiagonal1.isPresent() && boxesDiagonal1.get().size() == 2)
            return boxesDiagonal1;

        Optional<List<Box>> boxesDiagonal2 = iterateDiagonal(ticTacToeGame, diagonal2, symbol);

        if (boxesDiagonal2.isPresent() && boxesDiagonal2.get().size() == 2)
            return boxesDiagonal2;

        return Optional.empty();
    }

    private Optional<Box> strategyOppositeCorner(TicTacToeGame ticTacToeGame, Symbol symbol) {

        Box topLeftCorner = ticTacToeGame.getGrid().getBox(GridCorner.TOP_LEFT);
        Box bottomRightCorner = ticTacToeGame.getGrid().getBox(GridCorner.BOTTOM_RIGHT);
        if (topLeftCorner != null
            && topLeftCorner.getSymbol() == Symbol.getOpponentSymbol(symbol)
            && bottomRightCorner == null)
            return Optional.of(new Box(symbol, GridCorner.BOTTOM_RIGHT));

        if (bottomRightCorner != null
            && bottomRightCorner.getSymbol() == Symbol.getOpponentSymbol(symbol)
            && topLeftCorner == null)
            return Optional.of(new Box(symbol, GridCorner.TOP_LEFT));

        Box bottomLeftCorner = ticTacToeGame.getGrid().getBox(GridCorner.BOTTOM_LEFT);
        Box topRightCorner = ticTacToeGame.getGrid().getBox(GridCorner.TOP_RIGHT);
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

    private Optional<Box> strategyEmptyCorner(TicTacToeGame ticTacToeGame, Symbol symbol) {

        Box topLeftCorner = ticTacToeGame.getGrid().getBox(GridCorner.TOP_LEFT);
        if (topLeftCorner == null)
            return Optional.of(new Box(symbol, GridCorner.TOP_LEFT));

        Box topRightCorner = ticTacToeGame.getGrid().getBox(GridCorner.TOP_RIGHT);
        if (topRightCorner == null)
            return Optional.of(new Box(symbol, GridCorner.TOP_RIGHT));

        Box bottomLeftCorner = ticTacToeGame.getGrid().getBox(GridCorner.BOTTOM_LEFT);
        if (bottomLeftCorner == null)
            return Optional.of(new Box(symbol, GridCorner.BOTTOM_LEFT));

        Box bottomRightCorner = ticTacToeGame.getGrid().getBox(GridCorner.BOTTOM_RIGHT);
        if (bottomRightCorner == null)
            return Optional.of(new Box(symbol, GridCorner.BOTTOM_RIGHT));

        return Optional.empty();
    }

    private Optional<Box> strategyEmptySide(TicTacToeGame ticTacToeGame, Symbol symbol) {

        Box leftSide = ticTacToeGame.getGrid().getBox(GridCorner.LEFT_SIDE);
        if (leftSide == null)
            return Optional.of(new Box(symbol, GridCorner.LEFT_SIDE));

        Box rightSide = ticTacToeGame.getGrid().getBox(GridCorner.RIGHT_SIDE);
        if (rightSide == null)
            return Optional.of(new Box(symbol, GridCorner.RIGHT_SIDE));

        Box topSide = ticTacToeGame.getGrid().getBox(GridCorner.TOP_SIDE);
        if (topSide == null)
            return Optional.of(new Box(symbol, GridCorner.TOP_SIDE));

        Box bottomSide = ticTacToeGame.getGrid().getBox(GridCorner.BOTTOM_SIDE);
        if (bottomSide == null)
            return Optional.of(new Box(symbol, GridCorner.BOTTOM_SIDE));

        return Optional.empty();
    }

}
