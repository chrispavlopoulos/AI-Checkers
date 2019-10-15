import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Checkers checkers = new Checkers(new Player(Board.Color.black));
        checkers.start();

    }
}