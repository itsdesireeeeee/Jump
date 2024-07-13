package jump61;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.ArrayDeque;
import java.util.Formatter;
import java.util.function.Consumer;

import static jump61.Side.RED;
import static jump61.Side.BLUE;
import static jump61.Side.WHITE;
import static jump61.Square.INITIAL;
import static jump61.Square.square;

/** Represents the state of a Jump61 game.  Squares are indexed either by
 *  row and column (between 1 and size()), or by square number, numbering
 *  squares by rows, with squares in row 1 numbered from 0 to size()-1, in
 *  row 2 numbered from size() to 2*size() - 1, etc. (i.e., row-major order).
 *
 *  A Board may be given a notifier---a Consumer<Board> whose
 *  .accept method is called whenever the Board's contents are changed.
 *
 *  @author Desiree Garcia
 */
class Board {

    /** An uninitialized Board.  Only for use by subtypes. */
    protected Board() {
        _notifier = NOP;
    }

    /** An N x N board in initial configuration. */
    Board(int N) {
        this();
        _size = N;
        _numMoves = 0;
        _board = new Square[size() * size()];
        _history = new ArrayList<>();
        Arrays.fill(_board, INITIAL);
    }

    /** A board whose initial contents are copied from BOARD0, but whose
     *  undo history is clear, and whose notifier does nothing. */
    Board(Board board0) {
        this(board0.size());
        copy(board0);
        _readonlyBoard = new ConstantBoard(this);
    }

    /** Returns a readonly version of this board. */
    Board readonlyBoard() {
        return _readonlyBoard;
    }

    /** (Re)initialize me to a cleared board with N squares on a side. Clears
     *  the undo history and sets the number of moves to 0. */
    void clear(int N) {
        Board b = new Board(N);
        _size = N;
        _history =  new ArrayList<>();
        _board = new Square[b._size * b._size];
        Arrays.fill(_board, INITIAL);
        _numMoves = 0;

        announce();
    }

    /** Copy the contents of BOARD into me. */
    void copy(Board board) {
        _size = board.size();
        _board = new Square[size() * size()];
        internalCopy(board);
        _history = new ArrayList<>();
    }

    /** Copy the contents of BOARD into me, without modifying my undo
     *  history. Assumes BOARD and I have the same size. */
    private void internalCopy(Board board) {
        assert size() == board.size();
        for (int i = 0; i < board.size() * board.size(); i++) {
            _board[i] = board.get(i);
        }
    }

    /** Return the number of rows and of columns of THIS. */
    int size() {
        return _size;
    }

    /** Returns the contents of the square at row R, column C
     *  1 <= R, C <= size (). */
    Square get(int r, int c) {
        return get(sqNum(r, c));
    }

    /** Returns the contents of square #N, numbering squares by rows, with
     *  squares in row 1 number 0 - size()-1, in row 2 numbered
     *  size() - 2*size() - 1, etc. */
    Square get(int n) {
        if (exists(n)) {
            return _board[n];
        }
        throw new GameException("Square" + n + "does not exist.");
    }

    /** Returns the total number of spots on the board. */
    int numPieces() {
        int totalSpots = 0;
        for (Square s : _board) {
            totalSpots += s.getSpots();
        }
        return totalSpots;
    }

    /** Returns the Side of the player who would be next to move.  If the
     *  game is won, this will return the loser (assuming legal position). */
    Side whoseMove() {
        return ((numPieces() + size()) & 1) == 0 ? RED : BLUE;
    }

    /** Return true iff row R and column C denotes a valid square. */
    final boolean exists(int r, int c) {
        return 1 <= r && r <= size() && 1 <= c && c <= size();
    }

    /** Return true iff S is a valid square number. */
    final boolean exists(int s) {
        int N = size();
        return 0 <= s && s < N * N;
    }

    /** Return the row number for square #N. */
    final int row(int n) {
        return n / size() + 1;
    }

    /** Return the column number for square #N. */
    final int col(int n) {
        return n % size() + 1;
    }

    /** Return the square number of row R, column C. */
    final int sqNum(int r, int c) {
        return (c - 1) + (r - 1) * size();
    }

    /** Return a string denoting move (ROW, COL)N. */
    String moveString(int row, int col) {
        return String.format("%d %d", row, col);
    }

    /** Return a string denoting move N. */
    String moveString(int n) {
        return String.format("%d %d", row(n), col(n));
    }

    /** Returns true iff it would currently be legal for PLAYER to add a spot
        to square at row R, column C. */
    boolean isLegal(Side player, int r, int c) {
        return isLegal(player, sqNum(r, c));
    }

    /** Returns true iff it would currently be legal for PLAYER to add a spot
     *  to square #N. */
    boolean isLegal(Side player, int n) {
        if (exists(n)) {
            Side playerSide = get(n).getSide();
            if (playerSide == WHITE || playerSide == player) {
                return true;
            }
        }
        return false;
    }

    /** Returns true iff PLAYER is allowed to move at this point. */
    boolean isLegal(Side player) {
        return whoseMove().playableSquare(player);
    }

    /** Returns the winner of the current position, if the game is over,
     *  and otherwise null. */
    final Side getWinner() {
        if (numOfSide(RED) == size() * size()) {
            return RED;
        } else if (numOfSide(BLUE) == size() * size()) {
            return BLUE;
        }
        return null;
    }

    /** Return the number of squares of given SIDE. */
    int numOfSide(Side side) {
        int numSide = 0;
        for (Square sq : _board) {
            if (sq.getSide() == side) {
                numSide += 1;
            }
        }
        return numSide;
    }

    /** Add a spot from PLAYER at row R, column C.  Assumes
     *  isLegal(PLAYER, R, C). */
    void addSpot(Side player, int r, int c) {
        addSpot(player, sqNum(r, c));
    }

    /** Add a spot from PLAYER at square #N.  Assumes isLegal(PLAYER, N). */
    void addSpot(Side player, int n) {
        if (!isLegal(player, n)) {
            return;
        }
        markUndo();
        int oldSpotNumber = get(n).getSpots();
        internalSet(n, oldSpotNumber + 1, player);
        jump(n);
        _numMoves += 1;
    }

    /** Set the square at row R, column C to NUM spots (0 <= NUM), and give
     *  it color PLAYER if NUM > 0 (otherwise, white). */
    void set(int r, int c, int num, Side player) {
        internalSet(r, c, num, player);
        announce();
    }

    /** Set the square at row R, column C to NUM spots (0 <= NUM), and give
     *  it color PLAYER if NUM > 0 (otherwise, white).  Does not announce
     *  changes. */
    private void internalSet(int r, int c, int num, Side player) {
        internalSet(sqNum(r, c), num, player);
    }

    /** Set the square #N to NUM spots (1 <= NUM), and give it color PLAYER
     *  if NUM > 1 (otherwise, white). Does not announce changes. */
    private void internalSet(int n, int num, Side player) {
        _board[n] = square(player, num);
    }

    /** Undo the effects of one move (that is, one addSpot command).  One
     *  can only undo back to the last point at which the undo history
     *  was cleared, or the construction of this Board. */
    void undo() {
        _numMoves -= 1;
        Board b = _history.get(_numMoves);
        _history.remove(_numMoves);
        internalCopy(b);
    }

    /** Record the beginning of a move in the undo history. */
    private void markUndo() {
        Board b = new Board();
        b.copy(this);
        _history.add(b);
    }

    /** Add DELTASPOTS spots of side PLAYER to row R, column C,
     *  updating counts of numbers of squares of each color. */
    private void simpleAdd(Side player, int r, int c, int deltaSpots) {
        internalSet(r, c, deltaSpots + get(r, c).getSpots(), player);
    }

    /** Add DELTASPOTS spots of color PLAYER to square #N,
     *  updating counts of numbers of squares of each color. */
    private void simpleAdd(Side player, int n, int deltaSpots) {
        internalSet(n, deltaSpots + get(n).getSpots(), player);
    }

    /** Used in jump to keep track of squares needing processing.  Allocated
     *  here to cut down on allocations. */
    private final ArrayDeque<Integer> _workQueue = new ArrayDeque<>();

    /** Do all jumping on this board, assuming that initially, S is the only
     *  square that might be over-full.
     *  @param s is the square that begins jump. */
    private void jump(int s) {
        int numNeighbors = neighbors(s);
        Side player = get(s).getSide();
        ArrayList<Integer> indexArray = neighborIndexes(row(s), col(s));

        if (numNeighbors == 4 && get(s).getSpots() == 5) {
            jumpAdd(s, player, indexArray);
            for (int i : indexArray) {
                jump(i);
            }
        } else if (numNeighbors == 3 && get(s).getSpots() == 4) {
            jumpAdd(s, player, indexArray);
            for (int i : indexArray) {
                jump(i);
            }
        } else if (numNeighbors == 2 && get(s).getSpots() == 3) {
            jumpAdd(s, player, indexArray);
            for (int i : indexArray) {
                jump(i);
            }
        }

    }

    /** Recursively calls add spot.
     * @param s = current spot
     * @param player = side player is.
     * @param arr = contains neighbors of s.
     * */
    private void jumpAdd(int s, Side player, ArrayList<Integer> arr) {
        if (getWinner() != null) {
            return;
        }
        _board[s] = square(player, 1);
        for (int i : arr) {
            internalSet(i, get(i).getSpots() + 1, player);
        }
    }

    /** Returns my dumped representation. */
    @Override
    public String toString() {
        String breaks = "===";
        String st = breaks, color;
        for (int r = 1; r < size() + 1; r++) {
            String line = "   ";
            for (int c = 1; c < size() + 1; c++) {
                if (get(r, c).getSide() == RED) {
                    color = "r";
                } else if (get(r, c).getSide() == BLUE) {
                    color = "b";
                } else {
                    color = "-";
                }
                line = line + " " + get(r, c).getSpots() + color;
            }
            st = st + "\r\n" + line;
        }
        st = st + "\r\n" + breaks;
        return st;
    }

    /** Returns an external rendition of me, suitable for human-readable
     *  textual display, with row and column numbers.  This is distinct
     *  from the dumped representation (returned by toString). */
    public String toDisplayString() {
        String[] lines = toString().trim().split("\\R");
        Formatter out = new Formatter();
        for (int i = 1; i + 1 < lines.length; i += 1) {
            out.format("%2d %s%n", i, lines[i].trim());
        }
        out.format("  ");
        for (int i = 1; i <= size(); i += 1) {
            out.format("%3d", i);
        }
        return out.toString();
    }

    /** Returns the number of neighbors of the square at row R, column C. */
    int neighbors(int r, int c) {
        int size = size();
        int n;
        n = 0;
        if (r > 1) {
            n += 1;
        }
        if (c > 1) {
            n += 1;
        }
        if (r < size) {
            n += 1;
        }
        if (c < size) {
            n += 1;
        }
        return n;
    }

    /** Returns the indexes of neighbors of the square at row R, column C. */
    public ArrayList<Integer> neighborIndexes(int r, int c) {
        ArrayList<Integer> indexes = new ArrayList<>();
        if (exists(r + 1, c)) {
            indexes.add(sqNum(r + 1, c));
        }
        if (exists(r - 1, c)) {
            indexes.add(sqNum(r - 1, c));
        }
        if (exists(r, c + 1)) {
            indexes.add(sqNum(r, c + 1));
        }
        if (exists(r, c - 1)) {
            indexes.add(sqNum(r, c - 1));
        }

        return indexes;
    }


    /** Returns the number of neighbors of square #N. */
    int neighbors(int n) {
        return neighbors(row(n), col(n));
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Board)) {
            return false;
        } else {
            Board B = (Board) obj;
            if (B.size() != this.size()) {
                return false;
            }
            for (int i = 0; i < B.size() * B.size(); i++) {
                if (get(i).getSide() != B.get(i).getSide()) {
                    return false;
                } else if (get(i).getSpots() != B.get(i).getSpots()) {
                    return false;
                }
            }
            return true;
        }
    }



    @Override
    public int hashCode() {
        return numPieces();
    }

    /** Set my notifier to NOTIFY. */
    public void setNotifier(Consumer<Board> notify) {
        _notifier = notify;
        announce();
    }

    /** Take any action that has been set for a change in my state. */
    private void announce() {
        _notifier.accept(this);
    }

    /** A notifier that does nothing. */
    private static final Consumer<Board> NOP = (s) -> { };

    /** A read-only version of this Board. */
    private ConstantBoard _readonlyBoard;

    /** Use _notifier.accept(B) to announce changes to this board. */
    private Consumer<Board> _notifier;

    /** The length of the current board. */
    private int _size;

    /** The length of the current board. */
    private int _numMoves;

    /** The board of squares. */
    private Square[] _board;

    /** The history of the board. */
    private ArrayList<Board> _history;

}
