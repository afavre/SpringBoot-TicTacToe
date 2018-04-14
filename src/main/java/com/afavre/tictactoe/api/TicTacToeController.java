package com.afavre.tictactoe.api;

import com.afavre.tictactoe.domain.TicTacToeGame;
import com.afavre.tictactoe.domain.request.NewGameRequest;
import com.afavre.tictactoe.domain.request.NextMoveRequest;
import com.afavre.tictactoe.exception.BoxAlreadyAssignedException;
import com.afavre.tictactoe.service.TicTacToeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class TicTacToeController {

    @Autowired
    private TicTacToeService ticTacToeService;

    @RequestMapping(value = "/games", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity<TicTacToeGame> createNewGame(@RequestBody NewGameRequest newGameRequest) {

        TicTacToeGame newGame = this.ticTacToeService.createNewGame(newGameRequest);
        return new ResponseEntity<>(newGame, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/games/{gameId}", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity<Object> play(@PathVariable("gameId") String gameId,
                                       @RequestBody NextMoveRequest nextMoveRequest) {

        TicTacToeGame ticTacToeGame = this.ticTacToeService.getTicTacToeGame(gameId);

        if (ticTacToeGame == null) {
            return new ResponseEntity<>("Unexisting game Id", HttpStatus.BAD_REQUEST);
        }

        if (ticTacToeGame.isTerminated())
            return new ResponseEntity<>("Game finished", HttpStatus.OK);

        try {
            ticTacToeGame = this.ticTacToeService.next(ticTacToeGame,
                                                       ticTacToeGame.getUserSymbol(),
                                                       nextMoveRequest.getX(),
                                                       nextMoveRequest.getY());
        } catch (BoxAlreadyAssignedException e) {
            return new ResponseEntity<>("Box Already taken, try another one",
                                        HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(ticTacToeGame, HttpStatus.OK);
    }

    @RequestMapping(value = "/games/{gameId}", method = RequestMethod.GET)
    public ResponseEntity<Object> get(@PathVariable("gameId") String gameId) {

        TicTacToeGame ticTacToeGame = this.ticTacToeService.getTicTacToeGame(gameId);

        if (ticTacToeGame == null) {
            return new ResponseEntity<>("Unexisting game Id", HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(ticTacToeGame, HttpStatus.OK);
    }

}
