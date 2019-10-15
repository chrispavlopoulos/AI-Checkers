public class Move{

    Square source;
    Board.Direction direction;

    public Move(Square source, Board.Direction direction){
        this.source = source;
        this.direction = direction;
    }
}
