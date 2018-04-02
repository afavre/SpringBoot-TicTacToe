package com.afavre.tictactoe.util;

import com.afavre.tictactoe.domain.Box;
import com.afavre.tictactoe.domain.Symbol;

public class Utils {

    public static boolean isSameSymbole(Box box, Symbol symbol) {
        return box != null && box.getSymbol() == symbol;

    }

}
