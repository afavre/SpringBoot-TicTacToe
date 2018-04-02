package com.afavre.tictactoe;

import com.afavre.tictactoe.domain.Box;
import com.afavre.tictactoe.domain.Symbol;
import com.afavre.tictactoe.domain.TicTacToeGame;
import com.afavre.tictactoe.exception.BoxAlreadyAssignedException;
import com.afavre.tictactoe.service.TicTacToeService;

import java.util.Optional;
import java.util.Scanner;

public class TicTacToeConsole {

    public static void main(String[] args) {

        TicTacToeService boardService = new TicTacToeService();
        Scanner scan = new Scanner(System.in);
        System.out.println("What team are you (x/o) ?");
        String in = scan.next();
        Optional<Symbol> matching = Symbol.getMatching(in);
        matching.ifPresent(boardService::createNewGame);

        while (!boardService.getTicTacToeGame().isTerminated()) {

            System.out.println("Next move Y ?");
            int x = scan.nextInt();
            System.out.println("Next move X ?");
            int y = scan.nextInt();
            try {
                boardService.next(matching.get(), x, y);
            } catch (BoxAlreadyAssignedException e) {
                System.out.println("Box already used, try another one");
            }
            draw(boardService.getTicTacToeGame());
        }

        if (boardService.getTicTacToeGame().isDraw())
            System.out.println("No winner - it is a draw");
        else
            System.out.println("The winner is " + boardService.getTicTacToeGame().getWinner());
    }

    private static void draw(TicTacToeGame ticTacToeGame) {
        for (Box[] xGrid : ticTacToeGame.getGrid().getGrid()) {
            System.out.println("-------");
            System.out.print("|");
            for (Box yGrid : xGrid) {
                String symbol = yGrid != null ? yGrid.getSymbol().getKey(): " ";
                System.out.print(symbol + "|");
            }
            System.out.println("");
        }
        System.out.println("-------");

    }

}
