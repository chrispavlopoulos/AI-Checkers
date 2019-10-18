public class Move{

    Square source;
    Board.Direction direction;

    public Move(Square source, Board.Direction direction){
        this.source = source;
        this.direction = direction;
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof Move))
            return false;

        Move other = (Move) o;
        return source == other.source && direction == other.direction;
    }
}
