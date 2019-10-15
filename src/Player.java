public class Player {

    public Board.Color color;
    public boolean isComputer;

    public int score;
    public static final int maxScore = 12;

    public Player(Board.Color color){
        this(color, false);
    }

    public Player(Board.Color color, boolean isComputer) {
        this.color = color;
        this.isComputer = isComputer;

        this.score = 0;
    }

}
