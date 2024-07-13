package jump61;

import java.util.ArrayList;
import java.util.Random;

import static jump61.Side.RED;

/** An automated Player.
 *  @author P. N. Hilfinger
 */
class AI extends Player {

    /** A new player of GAME initially COLOR that chooses moves automatically.
     *  SEED provides a random-number seed used for choosing moves.
     */
    AI(Game game, Side color, long seed) {
        super(game, color);
        _random = new Random(seed);
    }

    @Override
    String getMove() {
        Board board = getGame().getBoard();

        assert getSide() == board.whoseMove();
        int choice = getMoveAI(getSide(), board);
        getGame().reportMove(board.row(choice), board.col(choice));
        return String.format("%d %d", board.row(choice), board.col(choice));
    }

    /** Return a move after searching the game tree to DEPTH>0 moves
     *  from the current position. Assumes the game is not over. */
    private int searchForMove() {
        Board work = new Board(getBoard());
        int value;
        assert getSide() == work.whoseMove();
        _foundMove = -1;
        if (getSide() == RED) {
            value = 0;
        } else {
            value = 0;
        }
        return _foundMove;
    }


    /** Find a move from position BOARD and return its value, recording
     *  the move found in _foundMove iff SAVEMOVE. The move
     *  should have maximal value or have value > BETA if SENSE==1,
     *  and minimal value or value < ALPHA if SENSE==-1. Searches up to
     *  DEPTH levels.  Searching at level 0 simply returns a static estimate
     *  of the board value and does not set _foundMove. If the game is over
     *  on BOARD, does not set _foundMove. */
    private int minMax(Board board, int depth, boolean saveMove,
                       int sense, int alpha, int beta) {
        return 0;
    }

    /** Return a heuristic estimate of the value of board position B.
     *  Use WINNINGVALUE to indicate a win for Red and -WINNINGVALUE to
     *  indicate a win for Blue. */
    private int staticEval(Board b, int winningValue) {
        return 0;
    }




    /** Gets the index of the square that produces the best move
     * for the AI.
     * @param player = side player is.
     * @param b = current board.
     * @return integer giving best choice for a move.
     * */
    private int getMoveAI(Side player, Board b) {
        int choice;
        if (player == RED) {
            aiPiece(player, b, _aiPiecesRed);
            choice = moveHelper(player, b, _aiPiecesRed);
            aiPiece(player, b, _aiPiecesRed);
        } else {
            aiPiece(player, b, _aiPiecesBlue);
            choice = moveHelper(player, b, _aiPiecesBlue);
            aiPiece(player, b, _aiPiecesBlue);
        }
        return choice;
    }



    /** Finds the best possible move for the AI.
     * @param player = side player is.
     * @param board = current board.
     * @param ai = current spaces ai has.
     * @return integer giving best choice for a move.
     * */
    private int moveHelper(Side player, Board board, ArrayList<Integer> ai) {
        int choice = -1;
        int value, bestValue = 0;
        if (board.numOfSide(player) == 0) {
            return availableCorner(player, board);
        }
        Board b = new Board(board.size());
        b.copy(board);
        for (int s : ai) {
            b.addSpot(player, s);

            value = b.numOfSide(player);
            b.undo();

            if (value > bestValue) {
                bestValue = value;
                choice = s;
            }

            for (int n : b.neighborIndexes(board.row(s), board.col(s))) {
                if (!b.isLegal(player, n) || board.get(n).getSide() == player) {
                    continue;
                }
                b.addSpot(player, n);
                value = b.numOfSide(player);
                b.undo();
                if (value > bestValue) {
                    bestValue = value;
                    choice = n;
                }
            }
        }
        return choice;
    }



    /** Adds squares to an array list that the AI owns.
     * @param player = side player is.
     * @param b = current board.
     * @param a = current spaces ai has.
     * */
    private void aiPiece(Side player, Board b, ArrayList<Integer> a) {
        int i = 0, n = 0, p = b.numOfSide(player);
        while (i < b.size() * b.size()) {
            if (n > p) {
                break;
            } else if (b.get(i).getSide() == player && !a.contains(i)) {
                a.add(i);
                n++;
            } else if (a.contains(i) && b.get(i).getSide() != player) {
                a.remove((Object) i);
            }
            i++;
        }
    }



    /** Finds an available corner to place a square in.
     * @param player = side player is.
     * @param b = current board.
     * @return integer giving best choice for a move.
     * */
    private int availableCorner(Side player, Board b) {
        int choice;
        if (b.isLegal(player, b.size(), b.size())) {
            choice = b.sqNum(b.size(), b.size());
        } else {
            choice = b.sqNum(1, 1);
        }
        return choice;
    }



    /** A random-number generator used for move selection. */
    private Random _random;

    /** Used to convey moves discovered by minMax. */
    private int _foundMove;

    /** Squares the blue AI owns. */
    private ArrayList<Integer> _aiPiecesBlue = new ArrayList<>();

    /** Squares the red AI owns. */
    private ArrayList<Integer> _aiPiecesRed = new ArrayList<>();
}
