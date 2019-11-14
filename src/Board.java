import java.util.*;

public class Board {

    Square doubleJumper = null;


    public HashMap<Square, List<Direction>> getAllPossibleMoves(Player currentPlayer) {
        HashMap<Square, List<Direction>> allMoves = new HashMap<>();
        boolean mustJump = false;

        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[0].length; col++) {
                Square square = grid[row][col];
                if(doubleJumper != null && square != doubleJumper)
                    continue;


                if (square.hasPiece() && square.getPiece().color == currentPlayer.color) {

                    List<Direction> possibleMoves = getAllJumpsAroundMe(currentPlayer.color, square);

                    //  If we can jump from this spot, we must.
                    if (!possibleMoves.isEmpty() || mustJump) {

                        //  If we just found out that we have to jump, we may have added some invalid moves now so we clear.
                        if (!mustJump)
                            allMoves.clear();

                        mustJump = true;
                    }
                    //  Otherwise, let's look at possible moves we can make from here.
                    else
                        possibleMoves.addAll(getAllMovesAroundMe(square));


                    if (!possibleMoves.isEmpty())
                        allMoves.put(square, possibleMoves);
                }
            }
        }

        return allMoves;
    }


    public static enum Color {
        red, black, white
    }

    public static enum Direction {
        topLeft, topRight, bottomLeft, bottomRight
    }

    public static enum GameState {
        playing, error, jumpAgain, pendingMessage
    }

    public static String colorString(Color color) {
        return colorString(color, false);
    }

    public static String colorString(Color color, boolean titleCase) {
        if (titleCase)
            return color == Color.red ? "Red" : color == Color.black ? "Black" : "White";
        else
            return color == Color.red ? "red" : color == Color.black ? "black" : "white";
    }

    private Square[][] grid;
    private boolean[][] recentlyKilled;
    private HashMap<Square, List<Direction>> jumpSrcDest;
    private static final List<Character> letters = new ArrayList<>(Arrays.asList('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'));

    private Undo undo;

    public Board() {
        grid = new Square[8][8];
        initRecentlyKilled();

        init();
    }

    /**
     * Red starts at the top
     * <p>
     * Black starts at the bottom
     */

    public void init() {
        Square square;
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[0].length; col++) {
                if (row % 2 == 0) {
                    if (col % 2 == 0) {
                        square = new Square(Color.white, row, col);
                    } else
                        square = new Square(Color.black, row, col);
                } else {
                    if (col % 2 == 1) {
                        square = new Square(Color.white, row, col);
                    } else
                        square = new Square(Color.black, row, col);
                }

                if (row < 3) {
                    if (row % 2 == 0 && col % 2 == 1) {
                        square.setPiece(new Piece(Color.red, row, col));
                    } else if (row % 2 == 1 && col % 2 == 0)
                        square.setPiece(new Piece(Color.red, row, col));

                } else if (row > grid[0].length - 4) {
                    if (row % 2 == 1 && col % 2 == 0) {
                        square.setPiece(new Piece(Color.black, row, col));
                    } else if (row % 2 == 0 && col % 2 == 1)
                        square.setPiece(new Piece(Color.black, row, col));
                }

                grid[row][col] = square;
            }
        }

    }

    public void checkBoard(Player currentPlayer) {
        jumpSrcDest = new HashMap<>();

        initRecentlyKilled();

        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[0].length; col++) {

                Square currentSquare = grid[row][col];

                if (currentSquare.hasPiece() && currentSquare.getPiece().color == currentPlayer.color) {
                    List<Direction> directions = getAllJumpsAroundMe(currentPlayer.color, currentSquare);

                    if (!directions.isEmpty())
                        jumpSrcDest.put(currentSquare, directions);
                }

            }
        }

        if (!jumpSrcDest.isEmpty() && !currentPlayer.isComputer) {
            StringBuilder out = new StringBuilder();

            Object[] srcs = jumpSrcDest.keySet().toArray();
            Square src;
            for (int i = 0; i < srcs.length; i++) {
                src = (Square) srcs[i];
                out.append("\t");
                out.append(Board.rowColToMove(src.row, src.col));
                out.append(" must jump ");

                List<Direction> dests = jumpSrcDest.get(src);
                for (int j = 0; j < dests.size(); j++) {
                    int[] coords = offsetFromDirection(src.row, src.col, dests.get(j));
                    Square dest = grid[coords[0]][coords[1]];
                    out.append(Board.rowColToMove(dest.row, dest.col));

                    if (j < dests.size() - 2)
                        out.append(", ");
                    else if (j == dests.size() - 2) {
                        out.append(" or ");
                    }
                }

                if (i == srcs.length - 2) {
                    out.append(" or\n");
                }
            }

            System.out.println(out.toString());

        }
    }

    private List<Direction> getAllMovesAroundMe(Square square) {
        List<Direction> possibleMoves = new ArrayList<>();


        int[] offset;
        Square dest;
        for (Direction direction : Direction.values()) {
            offset = offsetFromDirection(square.row, square.col, direction);
            if (!isInBounds(offset[0], offset[1]))
                continue;

            dest = grid[offset[0]][offset[1]];
            if (legalDirection(square, dest) && !dest.hasPiece())
                possibleMoves.add(direction);
        }


        return possibleMoves;
    }

    private List<Direction> getAllJumpsAroundMe(Color myColor, Square currentSquare) {
        List<Direction> result = new ArrayList<>();
        if (!currentSquare.hasPiece())
            return result;

        if (!currentSquare.getPiece().isKing) {

            //  A non-king red can only jump downwards.
            if (myColor == Color.red) {
                //  Bottom right
                if (hasJumpableEnemy(myColor, currentSquare.row, currentSquare.col, Direction.bottomRight))
                    result.add(Direction.bottomRight);

                //  Bottom left
                if (hasJumpableEnemy(myColor, currentSquare.row, currentSquare.col, Direction.bottomLeft))
                    result.add(Direction.bottomLeft);
            }
            //  A non-king black can only jump upwards.
            else {
                //  Top left
                if (hasJumpableEnemy(myColor, currentSquare.row, currentSquare.col, Direction.topLeft))
                    result.add(Direction.topLeft);

                //  Top right
                if (hasJumpableEnemy(myColor, currentSquare.row, currentSquare.col, Direction.topRight))
                    result.add(Direction.topRight);
            }

            return result;
        }

        for (Direction direction : Direction.values()) {
            if (hasJumpableEnemy(myColor, currentSquare.row, currentSquare.col, direction))
                result.add(direction);
        }

        return result;
    }

    private boolean hasJumpableEnemy(Color myColor, int row, int col, Direction direction) {
        int[] offset = offsetFromDirection(row, col, direction);
        int newRow = offset[0];
        int newCol = offset[1];

        if (!isInBounds(newRow, newCol))
            return false;
        else if (grid[newRow][newCol].hasPiece() && grid[newRow][newCol].getPiece().color != myColor) {
            //  There's a piece here, but can we jump it?
            //  To do this, we move one more in that direction (to the spot where the piece will end up)
            //  and we check to see if that spot is out of bounds, and if there happens to be a piece there already.
            //  You can't jump over a piece who has another piece behind it in Checkers.
            offset = offsetFromDirection(newRow, newCol, direction);
            newRow = offset[0];
            newCol = offset[1];

            //  If this new spot is in bounds and there is no piece there, this is a valid jump
            return isInBounds(newRow, newCol) && !grid[newRow][newCol].hasPiece();
        } else
            return false;
    }

    public int[] offsetFromDirection(int row, int col, Direction direction) {
        int newRow = row, newCol = col;
        if (direction == Direction.topLeft) {
            newRow -= 1;
            newCol -= 1;
        } else if (direction == Direction.topRight) {
            newRow -= 1;
            newCol += 1;
        } else if (direction == Direction.bottomRight) {
            newRow += 1;
            newCol += 1;
        } else { // bottomLeft
            newRow += 1;
            newCol -= 1;
        }

        return new int[]{newRow, newCol};
    }

    private boolean isInBounds(int row, int col) {
        return row >= 0 && row < grid.length && col >= 0 && col < grid[0].length;
    }

    private boolean isInBounds(Square square, Direction direction) {
        int[] offset = offsetFromDirection(square.row, square.col, direction);
        return isInBounds(offset[0], offset[1]);
    }

    public GameState makeMove(Player currentPlayer, String src, String dest) {
        if (!validInput(src) || !validInput(dest)) {
            showError("Invalid input.",
                    "Must enter input of the form: [Letter][Number] \nWhere: [Letter] is the column and [Number] is the row (starting at 1)");

            return GameState.error;
        }

        String illegalReason = isLegalMove(currentPlayer, src, dest);
        if (!illegalReason.isEmpty()) {
            showError("Illegal move.",
                    illegalReason);

            return GameState.error;
        }

        Square startSquare = getSquareAt(src);
        Square endSquare = getSquareAt(dest);

        return doMove(currentPlayer, startSquare, endSquare);
    }

    public GameState makeMove(Player currentPlayer, Square startSquare, Direction direction) {
        return doMove(currentPlayer, startSquare, getSquareAt(startSquare, direction));
    }

    public GameState makeAIMove(Player currentPlayer, Square startSquare, Direction direction) {
        return doMove(currentPlayer, startSquare, getSquareAt(startSquare, direction), true);
    }

    public Undo getUndo(){
        return undo;
    }

    private GameState doMove(Player currentPlayer, Square startSquare, Square endSquare) {
        return doMove(currentPlayer, startSquare, endSquare, false);
    }

    private GameState doMove(Player currentPlayer, Square startSquare, Square endSquare, boolean aiMove) {
        doubleJumper = null;

        //  If the destination has a piece, this is a jump.
        if (endSquare.hasPiece()) {
            currentPlayer.score++;

            Piece victim = endSquare.getPiece();
            endSquare.setPiece(null);
            setRecentlyKilledPos(endSquare.row, endSquare.col);

            Direction direction = getDirection(startSquare, endSquare);
            int[] offset = offsetFromDirection(endSquare.row, endSquare.col, direction);
            endSquare = grid[offset[0]][offset[1]];

            endSquare.setPiece(startSquare.getPiece());
            startSquare.setPiece(null);

            //  King me
            boolean becameKing = false;
            if (currentPlayer.color == Color.red && endSquare.row == grid.length - 1
                    || currentPlayer.color == Color.black && endSquare.row == 0) {

                becameKing = !endSquare.getPiece().isKing;
                endSquare.getPiece().isKing = true;
            }

            undo = new Undo(currentPlayer, startSquare, endSquare, becameKing, victim);

            List<Direction> anotherJumpDirections = getAllJumpsAroundMe(currentPlayer.color, endSquare);
            if (!anotherJumpDirections.isEmpty()) {
                doubleJumper = endSquare;
                return GameState.jumpAgain;
            } else
                return GameState.playing;

        } else {

            endSquare.setPiece(startSquare.getPiece());
            startSquare.setPiece(null);

            //  King me
            boolean becameKing = false;
            if (currentPlayer.color == Color.red && endSquare.row == grid.length - 1
                    || currentPlayer.color == Color.black && endSquare.row == 0) {

                becameKing = !endSquare.getPiece().isKing;
                endSquare.getPiece().isKing = true;
            }

            undo = new Undo(currentPlayer, startSquare, endSquare, becameKing);

            return GameState.playing;
        }
    }

    public String undoMove(Undo undo) {
        if (undo == null)
            return "No undo available";

        Square source = undo.src;
        Square dest = undo.dest;

        source.setPiece(dest.getPiece());
        if (undo.becameKing) {
            source.getPiece().isKing = false;
        }

        dest.setPiece(null);

        if (undo.jumpedPiece != null) {
            Direction direction = getDirection(source, dest);
            int[] offset = offsetFromDirection(source.row, source.col, direction);
            grid[offset[0]][offset[1]].setPiece(undo.jumpedPiece);

            undo.currentPlayer.score--;
            initRecentlyKilled();
        }

        return "From " + rowColToMove(dest.row, dest.col) + " back to " + rowColToMove(source.row, source.col);
    }

    private String isLegalMove(Player currentPlayer, String src, String dest) {
        Square startSquare = getSquareAt(src);
        Square endSquare = getSquareAt(dest);

        //System.out.println("The piece at " + src + " is: " + startSquare.getPiece());


        if (startSquare.getPiece() == null)
            return "There is no piece in your start position.";
        else if (currentPlayer.color != startSquare.getPiece().color) {
            return "The piece you are trying to move is not yours.";
        } else if (startSquare.color != endSquare.color) {
            return "A piece cannot move to a square that isn't diagonal to itself.";
        } else if (startSquare.row == endSquare.row && startSquare.col == endSquare.col) {
            return "You really thought that would work?";
        } else if (Math.abs(startSquare.row - endSquare.row) > 1) {
            return "A piece cannot move more than 1 row up or down at a time.";
        } else if (!legalDirection(startSquare, endSquare)) {
            return "That piece cannot go in that direction.";
        } else if (endSquare.hasPiece() && startSquare.getPiece().color == endSquare.getPiece().color) {
            return "You can't jump your own piece.";
        } else if (!jumpSrcDest.isEmpty()) {
            if (!jumpSrcDest.containsKey(startSquare))
                return "That piece is not among the pieces that must jump.";
            else if (!withinDirections(startSquare, endSquare, jumpSrcDest.get(startSquare)))
                return "That piece must jump one of the listed pieces that surround it.";
        }

        return "";
    }

    private boolean legalDirection(Square startSquare, Square endSquare) {
        if (startSquare.getPiece() == null)
            return false;

        Piece piece = startSquare.getPiece();

        if (!piece.isKing) {
            if (piece.color == Color.red && startSquare.row >= endSquare.row)
                return false;
            else if (piece.color == Color.black && startSquare.row <= endSquare.row)
                return false;
        }

        return true;
    }

    private boolean withinDirections(Square startSquare, Square endSquare, List<Direction> directions) {
        for (Direction direction : directions) {
            int[] offset = offsetFromDirection(startSquare.row, startSquare.col, direction);
            if (offset[0] == endSquare.row && offset[1] == endSquare.col)
                return true;
        }

        return false;
    }

    private Direction getDirection(Square startSquare, Square endSquare) {
        int r1 = startSquare.row, c1 = startSquare.col;
        int r2 = endSquare.row, c2 = endSquare.col;

        if (r2 < r1 && c2 < c1)
            return Direction.topLeft;
        else if (r2 < r1 && c2 > c1)
            return Direction.topRight;
        else if (r2 > r1 && c2 < c1)
            return Direction.bottomLeft;
        else
            return Direction.bottomRight;
    }

    private boolean validInput(String input) {
        if (input == null || input.length() != 2)
            return false;

        char letter = Character.toUpperCase(input.charAt(0));
        char number = input.charAt(1);
        if (letter < 'A' || letter > 'H' || number < '1' || number > '8')
            return false;


        return true;
    }

    /**
     * The evaluation is impacted by the following:
     * - Increases for each piece the current player has on the board
     *
     * @param currentPlayer - The player we currently care about
     * @return - The evaluation result
     */
    public int evaluate(Player currentPlayer) {
        //  Reds and blacks on the board in this state, normal worth is 1, kings are worth 5
        int reds = 0, blacks = 0;

        //  This counts the progress that the non-king pieces are to becoming king. Progress starts from the middle.
        //  Red needs to get to the bottom, black needs to get to the top
        int redProgress = 0, blackProgress = 0;

        int redDanger = 0, blackDanger = 0;


        Square square;
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[0].length; col++) {
                square = grid[row][col];

                if (square.hasPiece()) {
                    double progress = square.getPiece().color == Color.black
                            ? Math.ceil(Math.pow(2, grid.length - row) / 10.0)
                            : Math.ceil(Math.pow(2, row + 1) / 10.0);

                    //  We don't count the status of a piece that can be jumped, we actually penalize that
                    if(square.getPiece().color == currentPlayer.color && isInDanger(currentPlayer.color, square)) {
                        if(square.getPiece().color == Color.red)
                            redDanger += square.getPiece().isKing? 20: 10;
                        else
                            blackDanger += square.getPiece().isKing? 20: 10;

                        continue;
                    }

                    if (square.getPiece().color == Color.red) {
                        if (square.getPiece().isKing) {
                            reds += 5;
                        } else {

                            //  We can only count this as progress if we can move this piece forward, otherwise,
                            //  this position is useless
                            if (isProgress(square.getPiece().color, square)) {
                                redProgress += progress;
                            }

                            reds++;
                        }


                    } else {
                        if (square.getPiece().isKing) {
                            blacks += 5;
                        } else {

                            //  We can only count this as progress if we can move this piece forward, otherwise,
                            //  this position is useless
                            if (isProgress(square.getPiece().color, square)) {
                                blackProgress += progress;
                            }

                            blacks++;
                        }
                    }
                }
            }
        }

        int winLoseAdjustment = 0;
        if (currentPlayer.color == Color.red) {
            if (reds == 0)
                winLoseAdjustment = -1000;
            else if (blacks == 0)
                winLoseAdjustment = 1000;
        } else {
            if (blacks == 0)
                winLoseAdjustment = -1000;
            else if (reds == 0)
                winLoseAdjustment = 1000;
        }

        int pieceAdvantage = currentPlayer.color == Color.red ? reds - blacks : blacks - reds;
        int progressTowardsKing = currentPlayer.color == Color.red ? redProgress : blackProgress;
        int dangerLevel = currentPlayer.color == Color.red ? redDanger : blackDanger;

        return pieceAdvantage * 2 + progressTowardsKing * 2 - dangerLevel * 2 + winLoseAdjustment;
    }

    private boolean isProgress(Color myColor, Square square) {
        int row = square.row;
        int col = square.col;

        List<Direction> jumpsAroundMe = getAllJumpsAroundMe(myColor, square);
        //  We can assume that this isn't a king
        if (myColor == Color.red) {

            //  I have more progress if I can jump a piece from here.
            if (jumpsAroundMe.contains(Direction.bottomLeft) || jumpsAroundMe.contains(Direction.bottomRight))
                return true;

                //  I have more progress if I can move at least left or right and not get captured.
            else
                return !isDangerousMove(myColor, square, Direction.bottomLeft) ||
                        !isDangerousMove(myColor, square, Direction.bottomRight);

        } else {

            //  I have more progress if I can jump a piece from here.
            if (jumpsAroundMe.contains(Direction.topLeft) || jumpsAroundMe.contains(Direction.topRight))
                return true;

                //  I have more progress if I can move at least left or right and not get captured.
            else
                return !isDangerousMove(myColor, square, Direction.topLeft) ||
                        !isDangerousMove(myColor, square, Direction.topRight);
        }

    }

    private boolean isDangerousMove(Color myColor, Square srcSquare, Direction destDirection) {
        int[] offset = offsetFromDirection(srcSquare.row, srcSquare.col, destDirection);
        int newRow = offset[0], newCol = offset[1];

        if (!isInBounds(newRow, newCol))
            return true;

        Square destSquare = grid[newRow][newCol];
        if (destSquare.hasPiece())
            return true;


        Direction srcDirection = getDirection(destSquare, srcSquare);


        //  We check each diagonal to see if there is a surrounding threat in this new position.
        //  (excluding the direction we just came from)

        int tempRow, tempCol;
        for (Direction direction : Direction.values()) {
            if (direction == srcDirection)
                continue;

            int[] tempOffset = offsetFromDirection(newRow, newCol, direction);
            tempRow = tempOffset[0];
            tempCol = tempOffset[1];
            if (!isInBounds(tempRow, tempCol))
                continue;
            Piece contender = grid[tempRow][tempCol].getPiece();
            if (contender == null || contender.color == myColor)
                continue;

            destSquare.setPiece(srcSquare.getPiece());
            srcSquare.setPiece(null);
            //  Uses the perspective of this enemy piece to see if we will be jumped in this new spot.
            boolean canJumpMe = hasJumpableEnemy(contender.color, tempRow, tempCol, getDirection(grid[tempRow][tempCol], destSquare));
            srcSquare.setPiece(destSquare.getPiece());
            destSquare.setPiece(null);

            if (canJumpMe) {
                return true;
            }
        }

        return false;
    }

    private boolean isInDanger(Color myColor, Square square){

        int tempRow, tempCol;
        for (Direction direction : Direction.values()) {

            int[] tempOffset = offsetFromDirection(square.row, square.col, direction);
            tempRow = tempOffset[0];
            tempCol = tempOffset[1];
            if (!isInBounds(tempRow, tempCol))
                continue;
            Piece contender = grid[tempRow][tempCol].getPiece();
            if (contender == null || contender.color == myColor)
                continue;


            //  Uses the perspective of this enemy piece to see if we will be jumped in this new spot.
            boolean canJumpMe = hasJumpableEnemy(contender.color, tempRow, tempCol, getDirection(grid[tempRow][tempCol], square));

            if (canJumpMe) {
                return true;
            }
        }

        return false;
    }

    public void print() {
        System.out.print(toString());
    }

    @Override
    public String toString() {
        StringBuilder boardOut = new StringBuilder();
        boardOut.append("    A   B   C   D   E   F   G   H \n");
        boardOut.append("  ---------------------------------\n");

        Square current;
        for (int row = 0; row < grid.length; row++) {
            boardOut.append((row + 1));
            boardOut.append(" ");
            for (int col = 0; col < grid[0].length; col++) {
                current = grid[row][col];

                if (current.hasPiece())
                    boardOut.append(current.printSquare());
                else if (recentlyKilled[row][col])
                    boardOut.append("| x ");
                else
                    boardOut.append(current.printSquare());

                if (col == grid[0].length - 1)
                    boardOut.append("|");
            }
            boardOut.append("\n");
            boardOut.append("  ---------------------------------\n");
        }

        return boardOut.toString();
    }

    private void showError(String title, String reason) {
        System.out.println("------------------------");
        System.out.println(title);
        System.out.println(reason);
        System.out.println("------------------------");

    }

    private Square getSquareAt(String input) {
        int row = input.charAt(1) - '1';
        int col = Board.letterToNumber(input.charAt(0));

        return grid[row][col];
    }

    public Square getSquareAt(Square source, Direction direction) {
        int[] offset = offsetFromDirection(source.row, source.col, direction);
        if (isInBounds(offset[0], offset[1]))
            return grid[offset[0]][offset[1]];
        else {
            System.out.println("---- Offset position was out of bounds ----");
            return source;
        }
    }

    public void initRecentlyKilled(){
        recentlyKilled = new boolean[grid.length][grid[0].length];
    }

    public int[] getRecentlyKilledPos(){
        for(int row = 0; row < recentlyKilled.length; row++){
            for (int col = 0; col < recentlyKilled[0].length; col++){
                if(recentlyKilled[row][col])
                    return new int[]{row, col};
            }
        }

        return new int[]{-1, -1};
    }

    public void setRecentlyKilledPos(int row, int col) {
        if(!isInBounds(row, col))
            return;

        initRecentlyKilled();
        recentlyKilled[row][col] = true;
    }

    public static int letterToNumber(char letter) {
        return letters.indexOf(Character.toUpperCase(letter));
    }

    public static char numberToLetter(int num) {
        return letters.get(num);
    }

    public static String rowColToMove(int row, int col) {
        return (numberToLetter(col) + "") + (row + 1);
    }


    public class Undo {

        Player currentPlayer;
        Square src, dest;
        boolean becameKing = false;
        Piece jumpedPiece;

        public Undo(Player currentPlayer, Square src, Square dest, boolean becameKing) {
            this(currentPlayer, src, dest, becameKing, null);
        }

        public Undo(Player currentPlayer, Square src, Square dest, boolean becameKing, Piece jumpedPiece) {
            this.currentPlayer = currentPlayer;
            this.src = src;
            this.dest = dest;
            this.becameKing = becameKing;
            this.jumpedPiece = jumpedPiece;
        }
    }

/*    @Override
    public String toString(){
        return "grid";
    }*/
}
