package com.afavre.tictactoe;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TicTacToeController {

    @RequestMapping("/")
    public String index() {
        return "Greetings from Spring Boot!";
    }

}
