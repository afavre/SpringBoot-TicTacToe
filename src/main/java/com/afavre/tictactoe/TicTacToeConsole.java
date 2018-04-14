package com.afavre.tictactoe;

import com.afavre.tictactoe.domain.Box;
import com.afavre.tictactoe.domain.Symbol;
import com.afavre.tictactoe.domain.TicTacToeGame;
import com.afavre.tictactoe.domain.request.NewGameRequest;
import com.afavre.tictactoe.exception.BoxAlreadyAssignedException;
import com.afavre.tictactoe.service.TicTacToeService;

import java.util.Optional;
import java.util.Scanner;

public class TicTacToeConsole {

    public static void main(String[] args) {

        TicTacToeService ticTacToeService = new TicTacToeService();

        Scanner scan = new Scanner(System.in);
        System.out.println("What team are you (x/o) ?");
        String in = scan.next();
        Optional<Symbol> matching = Symbol.getMatching(in);
        TicTacToeGame game;
        if (!matching.isPresent()) {
            System.out.println("The symbol has to be X or O");
            return;
        }

        System.out.println("What is your name ?");
        String username = scan.next();

        game = ticTacToeService.createNewGame(new NewGameRequest(username, matching.get()));

        while (!game.isTerminated()) {

            System.out.println("Next move Y ?");
            int x = scan.nextInt();
            System.out.println("Next move X ?");
            int y = scan.nextInt();
            try {
                game = ticTacToeService.next(game, matching.get(), x, y);
            } catch (BoxAlreadyAssignedException e) {
                System.out.println("Box already used, try another one");
            }
            draw(game);
        }

        if (game.isDraw())
            System.out.println("No winner - it is a draw");
        else
            System.out.println("The winner is " + game.getWinner());
        ticTacToeService.deleteGame(game.getGameId());
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
