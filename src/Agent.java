import javax.swing.*;
import java.util.HashMap;
import java.util.List;

public class Agent extends Player{

    private Player enemy;

    public Agent(Board.Color color){
        super(color, true);
    }


    private static class AlphaBetaNode{
        int alphaMax;
        int betaMin;
        Move alphaChild, betaChild;
        HashMap<Square, List<Board.Direction>> children;

        public AlphaBetaNode(){
            alphaMax = Integer.MIN_VALUE;
            betaMin = Integer.MAX_VALUE;
        }
    }

    public Move evaluate(Player enemy, Board board){
        this.enemy = enemy;

        AlphaBetaNode root = new AlphaBetaNode();

        minMax(root, this, true, board, 2, Integer.MIN_VALUE, Integer.MAX_VALUE);

        return root.alphaChild;
    }

    final int sleepTime = 0;
    private int minMax(AlphaBetaNode parent, Player player, boolean maximizingPlayer, Board board, int depth, int alpha, int beta){
        if(depth == 0 || score == Player.maxScore || enemy.score == Player.maxScore){
            /*System.out.println();
            System.out.println("LIMIT REACHED");
            System.out.println();*/
            return board.evaluate(this);
        }

        AlphaBetaNode node = new AlphaBetaNode();
        node.children = board.getAllPossibleMoves(this);

        if(maximizingPlayer){

            for(Square key: node.children.keySet()){
                for(Board.Direction direction: node.children.get(key)){
                    int bestValue = Integer.MIN_VALUE;
                    Board.Undo undo = board.makeAIMove(player, key, direction);

                    //board.print();
                    /*try{
                        System.out.println("Exploring Max . . .");
                        System.out.println();
                        Thread.sleep(sleepTime);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }*/
                    int tempMax = minMax(node, player == this? enemy: this, false, board, depth - 1, alpha, beta);
                    if(tempMax > bestValue){

                    }
                    if(tempMax > parent.alphaMax){
                        parent.alphaMax = tempMax;
                        parent.alphaChild = new Move(key, direction);
                    }

                    String undoText = board.undoMove(undo);
                    /*board.print();
                    System.out.println("Undone last move " + undoText);
                    System.out.println();*/
                }
            }

            return node.alphaMax;
        }else{

            for(Square key: node.children.keySet()){
                for(Board.Direction direction: node.children.get(key)){
                    Board.Undo undo = board.makeAIMove(player, key, direction);
                    //board.print();
                    /*try{
                        System.out.println("Exploring Min . . .");
                        System.out.println();
                        Thread.sleep(sleepTime);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }*/
                    int tempMin = minMax(node, player == this? enemy: this,true, board, depth - 1, alpha, beta);
                    if(tempMin < parent.betaMin){
                        parent.betaMin = tempMin;
                        parent.betaChild = new Move(key, direction);
                    }

                    String undoText = board.undoMove(undo);
                    /*board.print();
                    System.out.println("Undone last move " + undoText);
                    System.out.println();*/
                }
            }

            return node.betaMin;
        }
    }

}
