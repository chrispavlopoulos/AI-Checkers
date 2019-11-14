# AI-Checkers
Checkers with the ability to play against a computer that uses **Alpha-beta pruning** to make an _intelligent_ move against you. (All in your command line!)


## Running The Game
Here's step-by-step instructions to use the program:
1. Open the Command Line for your Operating System (Cmd on Windows).

2. Change your directory to the project directory's "src" folder.

3. Type and enter `javac *.java` to compile the classes necessary to run the program.
 
(Note: As I'd rather you not go through the same problem, I have to type `javac --release 8 *.java` in order to run the program on my computer. So please do so if you're experiencing issues.)


4. Then enter `java Main` to start playing Checkers against the computer!

## Playing The Game
For rules, please reference the [English Draughts rules for Checkers.](https://en.wikipedia.org/wiki/English_draughts)

The game works as follows:

- Specify who's who. You can let two computers play against each other (I included a random factor in the move it plays next so it should get heated)! Black always goes first.
- In order to move, you must first provide a start coordinate (i.e. 'A6'), then press enter. Now give the coordinate of the space you wish to move to (i.e. 'B5'). If you make an invalid move the console will let you know where you went wrong, so you can try again.
- When you are next to an enemy that you can jump, you must take the jump. In order to do so: select the piece by supplying the start coordinate, then enter the coordinate of the piece you wish to jump (NOT THE SQUARE YOUR PIECE IS GOING TO END UP IN). The console will inform you if you can make a second jump afterwards.
- The game ends when one player takes every piece of the other.
- You may also end the game by typing 'end', 'stop' or 'no' at any point during your turn.

Have fun!
