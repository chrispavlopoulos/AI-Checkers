import javafx.util.Pair;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class Agent extends Player{

    private Player enemy;
    Move[] lastTwoMoves; // lastTwoMoves[0] is the oldest move of the two
    int repeatedMoves;
    boolean needsRepeatFix;

    public Agent(Board.Color color){
        super(color, true);
        lastTwoMoves = new Move[]{null, null};
    }

    public Move evaluate(Player enemy, Board board){
        this.enemy = enemy;

        iterations = 0;

        Move bestMove = minMax(this, board, true, 5, Integer.MIN_VALUE, Integer.MAX_VALUE).getValue();

        if(lastTwoMoves[0] == null)
            lastTwoMoves[0] = bestMove;
        else if(lastTwoMoves[1] == null)
            lastTwoMoves[1] = bestMove;
        else {
            if(lastTwoMoves[0].equals(bestMove)){
                repeatedMoves++;
            }else{
                repeatedMoves = 0;
            }

            needsRepeatFix = repeatedMoves >= 2;

            lastTwoMoves[0] = lastTwoMoves[1];
            lastTwoMoves[1] = bestMove;
        }

        System.out.println("Ran " + iterations +" iterations");
        return bestMove;
    }

    final int sleepTime = 0;
    boolean shouldPrint = false;
    int iterations = 0;
    private Pair<Integer, Move> minMax(Player player, Board board, boolean maximizingPlayer, int depth, int alpha, int beta){
        if(shouldPrint)
            System.out.println("At depth: " +depth +" with board number " +iterations);
        iterations++;
        boolean printPost = false;

        int bestScore = maximizingPlayer? Integer.MIN_VALUE: Integer.MAX_VALUE;
        Move bestMove = null;

        if(depth == 0 || score == Player.maxScore || enemy.score == Player.maxScore){

            return  new Pair<>(board.evaluate(this), null);
        }


        HashMap<Square, List<Board.Direction>> children = board.getAllPossibleMoves(player);
        ArrayList<Move> bestMoves = new ArrayList<>();

        if(maximizingPlayer){

            for(Square key: children.keySet()){
                for(Board.Direction direction: children.get(key)){


                    Board.GameState gameState = board.makeAIMove(player, key, direction);
                    Board.Undo undo = board.getUndo();

                    /*if(iterations == 1)
                        board.print();*/
                    /*if(shouldPrint) {
                        board.print();
                        try {
                            System.out.println("Exploring Max . . .");
                            System.out.println();
                            Thread.sleep(sleepTime);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }*/

                    //if(depth == 3)
                        //printPost = true;

                    int score;
                    if(gameState == Board.GameState.jumpAgain) {
                        //System.out.println("Player " + player.color.toString() + " can jump again");
                        score = minMax(player, board, true, depth, alpha, beta).getKey();
                    }else
                        score = minMax(enemy, board, false, depth - 1, alpha, beta).getKey();


                    if(printPost) {
                        System.out.println();
                        board.print();
                        System.out.println("Explored Max on board #" +iterations +" with score: "+score);
                    }

                    board.undoMove(undo);

                    if(score == bestScore)
                        bestMoves.add(new Move(key, direction));

                    if(score > bestScore && !clampRepeatedMove(key, direction)){
                        bestScore = score;

                        bestMoves.clear();
                        bestMoves.add(new Move(key, direction));

                        alpha = Math.max(alpha, bestScore);
                        if(beta <= alpha) {
                            if(shouldPrint)
                                System.out.println(" -- PRUNED -- ");

                            break;
                        }
                    }

                    /*board.print();
                    System.out.println("Undone last move " + undoText);
                    System.out.println();*/
                }
            }

        }else{

            for(Square key: children.keySet()){
                for(Board.Direction direction: children.get(key)){

                    Board.GameState gameState = board.makeAIMove(player, key, direction);
                    Board.Undo undo = board.getUndo();

                    if(shouldPrint) {
                        board.print();
                        try {
                            System.out.println("Exploring Min . . .");
                            System.out.println();
                            Thread.sleep(sleepTime);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    int score;
                    if(gameState == Board.GameState.jumpAgain) {
                        //System.out.println("Player " +player.color.toString() + " can jump again");
                        score = minMax(player, board, false, depth, alpha, beta).getKey();
                    }else
                        score = minMax(this, board, true, depth - 1, alpha, beta).getKey();


                    board.undoMove(undo);

                    if(score == bestScore)
                        bestMoves.add(new Move(key, direction));

                    if(score < bestScore){
                        bestScore = score;

                        bestMoves.clear();
                        bestMoves.add(new Move(key, direction));

                        beta = Math.min(beta, bestScore);
                        if(beta <= alpha) {
                            if(shouldPrint)
                                System.out.println(" -- PRUNED -- ");

                            break;
                        }
                    }

                    /*board.print();
                    System.out.println("Undone last move " + undoText);
                    System.out.println();*/
                }
            }

        }

        if(!bestMoves.isEmpty())
            bestMove = bestMoves.get(new Random().nextInt(bestMoves.size()));

        return new Pair<>(bestScore, bestMove);
    }

    private boolean clampRepeatedMove(Square src, Board.Direction direction){
        if(needsRepeatFix && src == lastTwoMoves[0].source &&
                direction == lastTwoMoves[0].direction){

            return true;
        }else
            return false;
    }

}
