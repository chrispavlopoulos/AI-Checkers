import java.util.Scanner;

public class Checkers {

    private int moves;

    private Scanner input;
    private Board board;
    private Player player1;
    private Player player2;

    private boolean aiMoved;


    public Checkers(){
        this(new Player(Board.Color.black, true), new Player(Board.Color.red, true));
    }

    public Checkers(Player player1){
        this(player1, new Player(player1.color == Board.Color.red? Board.Color.black: Board.Color.red, true));
    }

    public Checkers(Player player1, Player player2){
        this.player1 = player1;
        this.player2 = player2;

        init();
    }

    private void init(){
        input = new Scanner(System.in);

        moves = 0;
        board = new Board();

        player1.score = 0;
        player2.score = 0;

        aiMoved = false;
    }

    public void start(){

        boolean endGame;
        Board.GameState gameState = Board.GameState.playing;

        Player currentPlayer = player1;

        while(true){
            if(checkWin(currentPlayer)){

                break;
            }

            //  Update whose turn it is
            currentPlayer = moves % 2 == 0? player1: player2;

            if(currentPlayer.isComputer) {
                Move move = Agent.evaluate(currentPlayer, player1 == currentPlayer ? player2 : player1, board);
                board.print();
                board.checkBoard(currentPlayer);
                showDelayedMessage("Thinking");
                System.out.println();
                gameState = board.makeMove(currentPlayer, move.source, move.direction);

                board.print();
                System.out.println("Player " + Board.colorString(currentPlayer.color) + " moved " + move.source + " to " + board.getSquareAt(move.source, move.direction));


                if (player1.isComputer && player2.isComputer) {
                    System.out.println("Press enter to continue: ");
                    String in = input.nextLine();

                    if (checkFinish(in))
                        break;
                } else {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }


                if(gameState != Board.GameState.jumpAgain)
                    moves++;

                aiMoved = true;

                continue;
            }


            if(!aiMoved)
                board.print();

            aiMoved = false;


            if(gameState == Board.GameState.error){
                System.out.println(Board.colorString(currentPlayer.color, true) + " player, please take your turn again");
            }else
                System.out.println("Player " + Board.colorString(currentPlayer.color) +"'s turn");

            board.checkBoard(currentPlayer);


            System.out.print("Enter piece to move: ");
            String firstIn = input.nextLine();
            endGame = checkFinish(firstIn);
            if(endGame)
                break;

            System.out.print("Enter square to move to: ");
            String secondIn = input.nextLine();

            endGame = checkFinish(secondIn);

            if(endGame)
                break;
            else
                gameState = board.makeMove(currentPlayer, firstIn, secondIn);

            if(gameState == Board.GameState.error || gameState == Board.GameState.jumpAgain) {
                System.out.print("--- Press enter to continue --- ");
                input.nextLine();

                continue;
            }else{
                showDelayedMessage("Moving");
            }

            System.out.println();

            moves++;
        }
    }

    private void showDelayedMessage(String message){
        System.out.print(message);
        for(int i = 0; i < 5; i++){
            System.out.print(".");
            try {
                Thread.sleep(200);
            }catch (InterruptedException e){
                System.out.println(e.getMessage());
                break;
            }
        }
        System.out.println();
    }

    private boolean checkWin(Player currentPlayer){
        if(currentPlayer.score != Player.maxScore)
            return false;


        board.print();

        System.out.println(" ---------- PLAYER " + Board.colorString(currentPlayer.color).toUpperCase() + " YOU WIN! ---------- ");
        System.out.println("    -------      Game Over      -------    ");
        System.out.println("Press enter to restart, or say \"stop\" to quit: ");
        String in = input.nextLine();
        if(!checkFinish(in)){
            init();
            start();
        }

        return true;
    }

    private boolean checkFinish(String input){
        return input.equals("end") || input.equals("stop") || input.equals("no");
    }
}
