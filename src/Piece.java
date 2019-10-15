public class Piece {


    public Board.Color color;
    public int row, col;
    public boolean isKing = false;

    public Piece(Board.Color color, int row, int col){
        this.color = color;
        this.row = row;
        this.col = col;
    }

    @Override
    public String toString() {
        return "A " +Board.colorString(color) + " piece on " +Board.numberToLetter(col) +(row + 1);
    }


    public char colorChar(){
        return color == Board.Color.red? (isKing? 'R': 'r'): (isKing? 'B': 'b');
    }
}
