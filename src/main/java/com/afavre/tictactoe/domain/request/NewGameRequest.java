package com.afavre.tictactoe.domain.request;

import com.afavre.tictactoe.domain.Symbol;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class NewGameRequest {

    private String username;

    private Symbol symbole;

    public NewGameRequest(String username, Symbol userSymbole) {
        this.username = username;
        this.symbole = userSymbole;
    }

    public String getUsername() {
        return this.username;
    }

    public Symbol getUserSymbole() {
        return this.symbole;
    }
}
