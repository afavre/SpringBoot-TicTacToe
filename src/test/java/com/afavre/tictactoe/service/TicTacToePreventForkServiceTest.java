package com.afavre.tictactoe.service;

import com.afavre.tictactoe.domain.Box;
import com.afavre.tictactoe.domain.Symbol;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class TicTacToePreventForkServiceTest {

    private List<Box> input;
    private Box expected;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { Arrays.asList(new Box(Symbol.X, 1,1),
                                new Box(Symbol.O, 0,0),
                                new Box(Symbol.X, 2,2)),
                  new Box(Symbol.O, 0,2) },
        });
    }

    public TicTacToePreventForkServiceTest(List<Box> input, Box expected) {
        this.input = input;
        this.expected = expected;
    }

    @Test
    public void testNextMovePreventFork() {
        TicTacToeService ticTacToeService = new TicTacToeService();
        ticTacToeService.createNewGame(Symbol.X);

        for (Box box : input)
            ticTacToeService.getTicTacToeGame().getGrid().putBox(box.getSymbol(), box.getX(),box.getY());

        Optional<Box> box = ticTacToeService.strategyPreventFork(Symbol.O);
        assertTrue(box.isPresent());
        assertEquals(expected, box.get());
    }

}
