package jump61;

import static jump61.Side.*;

import org.junit.Test;
import static org.junit.Assert.*;

/** Unit tests of Boards.
 *  @author Desiree Garcia
 */

public class BoardTest {

    private static final String NL = System.getProperty("line.separator");

    @Test
    public void testSize() {
        Board B = new Board(5);
        assertEquals("bad length", 5, B.size());
        ConstantBoard C = new ConstantBoard(B);
        assertEquals("bad length", 5, C.size());
        Board D = new Board(C);
        assertEquals("bad length", 5, D.size());
    }

    @Test
    public void testSet() {
        Board B = new Board(5);
        B.set(2, 2, 1, RED);
        assertEquals("wrong number of spots", 1, B.get(2, 2).getSpots());
        assertEquals("wrong color", RED, B.get(2, 2).getSide());
        assertEquals("wrong count", 1, B.numOfSide(RED));
        assertEquals("wrong count", 0, B.numOfSide(BLUE));
        assertEquals("wrong count", 24, B.numOfSide(WHITE));
    }

    @Test
    public void testSet2() {
        Board B = new Board(3);
        assertEquals("board should start off white", 9, B.numOfSide(WHITE));
        assertEquals("board should be size 3", 3, B.size());
        assertEquals("There should be 9 spots.", 9, B.numPieces());
        for (int i = 0; i < B.size(); i++) {
            int numSpot = B.get(i).getSpots();
            assertEquals("Each square should have 1 spot", 1, numSpot);
        }
        B.addSpot(RED, 1, 1);
        B.addSpot(BLUE, 3, 1);
        assertEquals("wrong number of spots", 2, B.get(1, 1).getSpots());
        assertEquals("wrong number of spots", 2, B.get(3, 1).getSpots());
        B.addSpot(RED, 2, 2);
        assertEquals("wrong count", 6, B.numOfSide(WHITE));
        assertEquals("wrong count", 2, B.numOfSide(RED));
        assertEquals("wrong count", 1, B.numOfSide(BLUE));
        assertEquals("There should be 12 spots.", 12, B.numPieces());

    }

    @Test
    public void testClear() {
        Board B = new Board(3);
        B.addSpot(RED, 1, 1);
        B.addSpot(BLUE, 3, 1);
        System.out.println(B.toString());
        B.clear(2);
        assertEquals("size must be 2.", 2, B.size());
        assertEquals("board should start off white", 4, B.numOfSide(WHITE));
        System.out.println(B.toString());
    }


    @Test
    public void testEquals() {
        Board B = new Board(2);
        B.addSpot(RED, 1, 1);

        Board B2 = new Board(2);
        B2.addSpot(RED, 1, 1);

        assertEquals(B, B2);

        Board B3 = new Board(3);
        B3.addSpot(RED, 1, 1);
        System.out.println(B3.toString());

        assertNotEquals(B, B3);

        B3.addSpot(RED, -1, 4);
        System.out.println(B3.toString());

    }



    @Test
    public void testUndo() {
        Board B = new Board(2);
        B.addSpot(RED, 1, 1);
        assertEquals("wrong number of spots", 2, B.get(1, 1).getSpots());
        assertEquals("wrong color", RED, B.get(1, 1).getSide());

        B.undo();
        assertEquals("wrong number of spots", 1, B.get(1, 1).getSpots());
        assertEquals("wrong color", WHITE, B.get(1, 1).getSide());

        B.addSpot(RED, 2, 1);
        System.out.println(B.toString());

        B.addSpot(BLUE, 1, 1);
        assertEquals("wrong number of spots", 2, B.get(1, 1).getSpots());
        assertEquals("wrong color", BLUE, B.get(1, 1).getSide());

        B.undo();
        System.out.println(B.toString());
        assertEquals("wrong number of spots", 1, B.get(1, 1).getSpots());
        assertEquals("wrong color", WHITE, B.get(1, 1).getSide());

        B.undo();
        assertEquals("wrong color", WHITE, B.get(2, 1).getSide());
        assertEquals("board should be white", 4, B.numOfSide(WHITE));

        System.out.println(B.toString());

    }


    @Test
    public void twoByTwo() {
        Board B = new Board(2);
        B.addSpot(RED, 1, 1);
        B.addSpot(BLUE, 1, 2);
        B.addSpot(RED, 2, 1);
        System.out.println(B.toString());
        B.addSpot(BLUE, 1, 2);
        System.out.println(B.toString());
        assertEquals(BLUE, B.getWinner());
    }


    @Test
    public void simple3() {
        Board B = new Board(3);

        B.addSpot(RED, 1, 1);
        System.out.println(B.toString());

        B.addSpot(BLUE, 2, 3);
        System.out.println(B.toString());

        B.addSpot(RED, 2, 2);
        System.out.println(B.toString());

        B.addSpot(BLUE, 3, 3);
        System.out.println(B.toString());

        B.addSpot(RED, 2, 2);
        System.out.println(B.toString());

        B.addSpot(BLUE, 2, 3);
        System.out.println(B.toString());

        B.addSpot(RED, 1, 3);
        System.out.println(B.toString());

        B.addSpot(BLUE, 2, 3);
        System.out.println(B.toString());

        B.addSpot(RED, 2, 1);
        System.out.println(B.toString());

        B.addSpot(BLUE, 3, 1);
        System.out.println(B.toString());

        B.addSpot(RED, 2, 1);
        System.out.println(B.toString());

        B.addSpot(BLUE, 2, 2);
        System.out.println(B.toString());


        assertEquals(BLUE, B.getWinner());
    }



    @Test
    public void testMove() {
        Board B = new Board(6);
        checkBoard("#0", B);

        B.addSpot(RED, 1, 1);
        System.out.println(B.toString());
        checkBoard("#1", B, 1, 1, 2, RED);

        B.addSpot(BLUE, 2, 1);
        System.out.println(B.toString());
        checkBoard("#2", B, 1, 1, 2, RED, 2, 1, 2, BLUE);

        B.addSpot(RED, 1, 1);
        System.out.println(B.toString());
        checkBoard("#3", B, 1, 1, 1, RED, 2, 1, 3, RED, 1, 2, 2, RED);

        B.undo();
        System.out.println(B.toString());
        checkBoard("#2U", B, 1, 1, 2, RED, 2, 1, 2, BLUE);
        B.undo();
        System.out.println(B.toString());
        checkBoard("#1U", B, 1, 1, 2, RED);
        B.undo();
        System.out.println(B.toString());
        checkBoard("#0U", B);
    }


    @Test
    public void testJump2() {
        Board B = new Board(3);
        B.addSpot(RED, 1, 1);
        System.out.println(B.toString());
        B.addSpot(BLUE, 2, 3);
        System.out.println(B.toString());
        B.addSpot(RED, 1, 1);
        System.out.println(B.toString());
        assertEquals("wrong number of spots", 12, B.numPieces());
        B.addSpot(BLUE, 2, 2);
        System.out.println(B.toString());
        B.addSpot(RED, 3, 3);
        System.out.println(B.toString());
        B.addSpot(BLUE, 2, 2);
        System.out.println(B.toString());
        B.addSpot(RED, 1, 2);
        System.out.println(B.toString());
        B.addSpot(BLUE, 2, 2);
        System.out.println(B.toString());
        B.addSpot(RED, 1, 2);
        System.out.println(B.toString());
        assertEquals("wrong number of RED", 8, B.numOfSide(RED));
        B.addSpot(BLUE, 3, 1);
        System.out.println(B.toString());
        B.addSpot(RED, 2, 1);
        System.out.println(B.toString());
        assertEquals("wrong number of spots", 20, B.numPieces());
        assertEquals(RED, B.getWinner());
    }



    /** Checks that B conforms to the description given by CONTENTS.
     *  CONTENTS should be a sequence of groups of 4 items:
     *  r, c, n, s, where r and c are row and column number of a square of B,
     *  n is the number of spots that are supposed to be there and s is the
     *  color (RED or BLUE) of the square.  All squares not listed must
     *  be WHITE with one spot.  Raises an exception signaling a unit-test
     *  failure if B does not conform. */
    private void checkBoard(String msg, Board B, Object... contents) {
        for (int k = 0; k < contents.length; k += 4) {
            String M = String.format("%s at %d %d", msg, contents[k],
                                     contents[k + 1]);
            assertEquals(M, (int) contents[k + 2],
                         B.get((int) contents[k],
                               (int) contents[k + 1]).getSpots());
            assertEquals(M, contents[k + 3],
                         B.get((int) contents[k],
                               (int) contents[k + 1]).getSide());
        }
        int c;
        c = 0;
        for (int i = B.size() * B.size() - 1; i >= 0; i -= 1) {
            assertTrue("bad white square #" + i,
                       (B.get(i).getSide() != WHITE)
                       || (B.get(i).getSpots() == 1));
            if (B.get(i).getSide() != WHITE) {
                c += 1;
            }
        }
        assertEquals("extra squares filled", contents.length / 4, c);
    }

}
