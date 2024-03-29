public class Square {

    public Board.Color color;
    public int row, col;
    private Piece piece;

    public Square(Board.Color color, int row, int col){
        this.color = color;
        this.row = row;
        this.col = col;

        piece = null;
    }

    public boolean hasPiece(){
        return this.piece != null;
    }

    public Piece getPiece(){
        return this.piece;
    }

    public void setPiece(Piece piece) {
        this.piece = piece;
    }

    public String printSquare(){
       return piece == null? "|   ": "| " + piece.colorChar() +" ";
    }

    @Override
    public String toString(){
        return Board.numberToLetter(col) + "" + (row + 1);
    }
}
