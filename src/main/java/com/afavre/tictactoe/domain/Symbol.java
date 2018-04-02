package com.afavre.tictactoe.domain;

import java.util.Optional;

public enum Symbol {
    X("x"),
    O("o");

    private String key;

    Symbol(String key) {
        this.key = key;
    }

    public String getKey() {
        return this.key;
    }

    public static Optional<Symbol> getMatching(String symbole) {

        for (Symbol symb: Symbol.values())
            if (symb.key.equalsIgnoreCase(symbole))
                return Optional.of(symb);

        return Optional.empty();
    }

    public static Symbol getOpponentSymbol(Symbol symbol) {
        return symbol == Symbol.X ? Symbol.O : Symbol.X;
    }

}
