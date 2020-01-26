import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.*;
import java.util.Random;
import java.util.Scanner;
import java.util.ArrayList;

/**  Board GUI for implementation with various games
 *   Author: Kirill Levin, Troy Vasiga, Chris Ingram
 *		Mr. Benum (modifications for writing text)
 */

public class Board extends JFrame 
{
    
  private static final int X_DIM = 60;
  private static final int Y_DIM = 60;
  private static final int X_OFFSET = 65;
  private static final int Y_OFFSET = 60;
  private static final int MESSAGE_HEIGHT = 60;
  private static final int FONT_SIZE = 30;
  private static final int M_FONT_SIZE = 11;
  private static final int BUTTON_MESSAGE_GAP = 40;
  private static final int BUTTON_HEIGHT = 30;
  private static final int BUTTON_WIDTH = 110;
  private static final int TITLE_HEIGHT = 50;
  
  // Grid colours
  private static final Color GRID_COLOR_A = new Color(0,0,0);
  private static final Color GRID_COLOR_B = new Color(255,255,255);
  private static final Color GRID_COLOR_HIGHLIGHT = new Color (255, 255, 51);
  
  // Preset colours for pieces
  private static final Color[] COLOURS = 
    {Color.YELLOW, Color.BLUE, Color.CYAN, Color.GREEN, 
     Color.PINK, Color.WHITE, Color.RED, Color.ORANGE };
  
  // String used to indicate each colour
  private static final String[] COLOUR_NAMES = 
  {"yellow", "blue", "cyan", "green", "pink", "white", "red", "orange"};
  
  // Colour to use if a match is not found 
  private Coordinate lastClick;  // How the mouse handling thread communicates 
                                 // to the board where the last click occurred
  private String message = "Good luck";
  private int numLines = 0;
  private int[][] line = new int[4][100];  // maximum number of lines is 100
  private int columns, rows;
  
  //2D array to store all of the text that will be displayed on the board
  private String[][] wordGrid;
  private ArrayList <Coordinate> selections = new ArrayList<Coordinate>();
  private Coordinate lastCord;
  private ArrayList <String> word = new ArrayList<String>();
  private ArrayList <String> wordList = new ArrayList<String>();
  
  private final int duration = 180;
  private String timeMessage;
  private int elapsedTime;
  private long startTime;
  private long endTime;
  
  private String pathName1;
  private File readFile;
  private Scanner input;
  
  private String pathName2;
  private File writeFile;
  private PrintWriter output;
  
  JFrame frame;
  private boolean gameState = false;
  private int points;
  private String playerName;
  Writer writer;
  
  /** A constructor to build a 2D board.
 * @throws IOException 
   */
  public Board (int rows, int cols) throws IOException
  {
	this.columns = cols;
	this.rows = rows;
	this.setSize(2*X_OFFSET+X_DIM*cols,2*Y_OFFSET+Y_DIM*rows+BUTTON_HEIGHT+MESSAGE_HEIGHT);
	this.setTitle("Boggle");
	this.setResizable(false);
	this.setBackground(GRID_COLOR_B);
       
  //Initialize the wordGrid array to contain one of the randomly generated preset
  //letters from one 16 dice
    Random rand = new Random();
    wordGrid = new String[cols][rows];
    
    String dice[] = { "AAEEGN", "ABBJOO", "ACHOPS", "AFFKPS", "AOOTTW", "CIMOTU", "DEILRX", "DELRVY", "DISTTY",
    		"EEGHNW", "EEINSU",	"EHRTVW", "EIOSST", "ELRTTY", "HIMNUQu", "HLNNRZ"};     
    int randDice;
    int randLetter;
    String letter = "";
    for (int r = 0; r < wordGrid.length; r++) {
    	for (int c = 0; c < wordGrid[r].length; c++) {
    		do {
    			randDice = rand.nextInt(dice.length);
    		} while(dice[randDice] == "");
    		randLetter = rand.nextInt(6);
    		letter = dice[randDice].substring(randLetter, randLetter + 1);
    		if (letter.equals("Q")) {
    			letter += "u";
    		}
    		wordGrid[r][c] = letter;
    		dice[randDice] = "";
    	}
    }
    
    addMouseListener(
      new MouseInputAdapter() 
      {
        /** A method that is called when the mouse is clicked
         */
        public void mouseClicked(MouseEvent e) 
        { 
          int x = (int)e.getPoint().getX();
          int y = (int)e.getPoint().getY();
      
          // We need to be synchronized to the parent class so we can wake
          // up any threads that might be waiting for us
          synchronized(Board.this) 
          {
            // Subtract one from high end so clicks on the black edge
            // don't yield a row or column outside of board because of
            // the way the coordinate is calculated.
            if (x >= X_OFFSET &&
              x <= (X_OFFSET + (columns * X_DIM) - 1) &&
              y >= Y_OFFSET &&
              y <= (Y_OFFSET + (Board.this.rows * Y_DIM) - 1)) {
              lastClick = new Coordinate((int)e.getPoint().getY(),
                                         (int)e.getPoint().getX());
              // Notify any threads that would be waiting for a mouse click
              Board.this.notifyAll() ;
            } /* if */
          } /* synchronized */
        } /* mouseClicked */
      } /* anonymous MouseInputAdapater */
    );
    
    //game instructions
    JOptionPane.showMessageDialog(frame, "Boggle Instructions\n"
    		+ "The object of Boggle is to find as many words as you can in the\n"
    		+ "4x4 letter grid within 3 minutes.\n\n"
    		+ "Words that you find must be at least three letters long. Letters\n"
    		+ "must adjoin in sequence horizontally, vertically or diagonally in\n"
    		+ "any direction. No letter may be used more than once within a single\n"
    		+ "word. To submit a word, press on the 'Enter Word' button. If you\n"
    		+ "would like to end the game early press the 'End Game' button.\n"
    		+ "To view the words you submitted and their corresponding points, refer\n"
    		+ "to the text file 'words.txt'.\n\n"
    		+ "3 letter and 4 letter words award 1 point, 5 letter words award\n"
    		+ "2 points, 6 letter words award 3 points, 7 letter words award 5\n"
    		+ "point,s and 8+ letter words award 11 points.");
    playerName = JOptionPane.showInputDialog("Enter your name"); //input used for the writing to a text file
    initalizeWordFile();
    startTime();
    
    getContentPane().setLayout(null);
    JButton b1 = new JButton("Enter Word");
    JButton b2 = new JButton("End Game");
    JButton b3 = new JButton("Check Time");
    
    //button for entering words
    b1.setBackground(Color.WHITE);
    b1.setLocation((int)this.getSize().getWidth() / 2 - BUTTON_WIDTH/2, Y_OFFSET + Y_DIM * wordGrid[0].length);
    b1.setSize(BUTTON_WIDTH, BUTTON_HEIGHT);
    getContentPane().add(b1);
    b1.addActionListener(
    		new ActionListener() {
    			
    				public void actionPerformed(ActionEvent e) {
    					initializeFile();
    					enterWord();
    					selections.clear();
    					repaint();
    					checkTime();
    					if (isTimerDone()) {
    						endGame(); //procedure for ending the game
        					JOptionPane.showMessageDialog(frame, "You scored " + points + " points.");
        					System.exit(1);
    					} else if (isRepeated()) { //displays an error message if the inputed is repeated
    						displayMessage("You already inputed this word");
    						wordList.remove(wordList.size()-1);
    					} else if (checkLength()) { //displays an error message if the inputed word isn't 3 letters long
    						displayMessage("Words must be at least 3 letters long");
    						wordList.remove(wordList.size()-1);
    					} else if (checkWordFile() == false) {
    						displayMessage("That is not an English word"); //display an error message if the word is not a real English word
    						wordList.remove(wordList.size()-1);
    					} else { //if the inputed word doesn't break any rules, its corresponding points is accumulated
    						addPoints();
    						writePoints();
    					}
    					
    				}
    			}
    		
    );
    
    //button for ending the game early
    b2.setBackground(Color.WHITE);
    b2.setLocation((int)this.getSize().getWidth() / 4 * 3 - BUTTON_WIDTH/2 + 15, Y_OFFSET + Y_DIM * wordGrid[0].length);
    b2.setSize(BUTTON_WIDTH, BUTTON_HEIGHT);
    getContentPane().add(b2);
    b2.addActionListener(
    		new ActionListener() {
    			
    				public void actionPerformed(ActionEvent e) {
    					endGame();
    					JOptionPane.showMessageDialog(frame, "You scored " + points + " points.");
    					System.exit(1);
    					
    				}
    			}
    		
    );
    
    //button for checking the remaining time
    b3.setBackground (Color.WHITE);
    b3.setLocation((int)this.getSize().getWidth() / 4 - BUTTON_WIDTH/2 - 15, Y_OFFSET + Y_DIM * wordGrid[0].length);
    b3.setSize(BUTTON_WIDTH, BUTTON_HEIGHT);
    getContentPane().add(b3);
    b3.addActionListener(
    		new ActionListener() {

    			public void actionPerformed(ActionEvent e) {
    				checkTime();
    				displayTime();
    				if (isTimerDone()) {
    					displayMessage("Times Up!");
    					endGame();
    					JOptionPane.showMessageDialog(frame, "You scored " + points + " points.");
    					System.exit(1);
    				} else { 
    					displayMessage(timeMessage); //displays the remaining time for the game
    				}
    			}
    		}

    		);
    
    this.setDefaultCloseOperation(EXIT_ON_CLOSE);
    this.setVisible(true);
       
  }
   


  /**
   * Adds the clicked grid coordinate to the selections array list
   * Pre: c > 0
   * Post: Argument c is added to selections array list and the board
   * is repainted to update the highlighted cords
   */
  
  public void addSelection (Coordinate c) {
	  selections.add(c);
	  repaint();
	  message = "";
	  //System.out.print(c.getRow() + " ");
	  //System.out.print(c.getCol() + " ");
	  //System.out.print(selections);
	 
  }
  
  /**
   * Removes the clicked grid coordinate from the selections array list
   * Pre: c > 0
   * Post: Argument c is removed from the selections array list and the board
   * is repainted to update the highlighted cords
   */
  
  public void removeSelection (Coordinate c) {
	  selections.remove(c);
	  repaint();
	  message = "";
	  //System.out.print(selections);
	  
  }
  
  /**
   * Checks if c is a coordinate within the selections array list
   * Pre: none
   * Post: A boolean is returned 
   */
  
  public boolean isSelected (Coordinate c) {
	  
	  for (Coordinate e: selections) {
		  if (c.equals(e)) {
			  return true;	  
		  }
	  }
	  return false;	  
  }
  
  /**
   * Adds the letter at coordinate c to the word array to create a word
   * pre: none
   * post: Letter at coordinate c is added to the word array list 
   */
 
  public void addLetter (Coordinate c) {
	  word.add(wordGrid[c.getCol()][c.getRow()]);
	  //System.out.print(word);
  }
  
  /**
   * Removes the letter at coordinate c to the word array to create a word
   * pre: none
   * post: Letter at coordinate c is removed from the word array list 
   */
  
  public void removeLetter (Coordinate c) {
	  word.remove(wordGrid[c.getCol()][c.getRow()]);
	  //System.out.print(word);
  }
  
  /**
   * Last entered word is entered into the wordList array
   * pre: none
   * post: A word formed from the word arrayList is added to the wordList array and
   * the word arrayList is cleared
   */
  
  public void enterWord () {
	  String str = "";
	  for (String e: word) {
		  str += e;
	  }
	  wordList.add(str.toLowerCase());
	  word.clear();
	  
	  //System.out.print(wordList);
	  
  }
  
  /**
   * Checks if a word is repeated in wordList
   * pre: wordList can't be empty
   * post: A boolean is returned
   */
  public boolean isRepeated () {
	  for (int e = 0; e < wordList.size()-1; e++) {
		  if (wordList.get(wordList.size()-1).equals(wordList.get(e)) && wordList.size() > 1) {
			  return true;
		  }
	  }
	  return false;
  }
  
  /**
   * Checks the last inputed word is of the correct length
   * pre: wordList can't be empty
   * post: A boolean is returned
   */
  public boolean checkLength() {
	  if ((wordList.get(wordList.size() - 1)).length() < 3) {
		  return true;
	  }
	  return false;
  }
  
  /**
   * Returns the last cord in the grid that was highlighted
   * pre: selections can't be empty
   * post: the last element of arrayList selections is returned
   */
  public Coordinate getLastCord () {
	  lastCord = selections.get(selections.size() - 1);
	  //System.out.print(" " + lastCord + " ");
	  return lastCord;
  }
  
  /**
   * Checks if the coordinate argument matches the last selected cord
   * pre: none
   * post: A boolean is returned
   */
  public boolean isLastCord (Coordinate c) {
	  if (selections.indexOf(c) == selections.size() - 1) {
		  return true;
	  }
	  return false;
  }
  
  /**
   * Checks if the current click on the board is the only highlighted cord
   * pre: none
   * post: A boolean is returned
   */
  public boolean isFirstClick () {
	  if (selections.isEmpty()) {
		  return true;
	  }
	  return false;
  }
  
  /**
   * Checks if the last selected cord has other selected cords adjacent to it  
   * pre: none
   * post: a boolean is returned
   */
  public boolean isAdjacent (Coordinate c1, Coordinate c2) {
	  
	  if ((new Coordinate (c1.getRow(), c1.getCol() - 1).equals(c2))) { //north
		  return true;
	  } else if ((new Coordinate (c1.getRow(), c1.getCol() + 1).equals(c2)))  { //south
		  return true;
	  } else if ((new Coordinate (c1.getRow() - 1, c1.getCol()).equals(c2))) { //west
		  return true;
	  } else if ((new Coordinate (c1.getRow() + 1, c1.getCol()).equals(c2))) { //east
		  return true;
	  } else if ((new Coordinate (c1.getRow() - 1, c1.getCol() - 1)).equals(c2)) { //north-west 
		  return true;
	  } else if ((new Coordinate (c1.getRow() + 1, c1.getCol() - 1)).equals(c2)) { //north-east
		  return true;
	  } else if ((new Coordinate (c1.getRow() - 1, c1.getCol() + 1)).equals(c2)) { //south-west
		  return true;
	  } else if ((new Coordinate (c1.getRow() + 1, c1.getCol() + 1)).equals(c2)) { //south-east
		  return true;
	  }
	  else {
		  return false;
		  
	  }
  }
  
  /**
   * Initializes startTime so that the elapsed time between events can begin
   * pre: none
   * post: startTime is initialized
   */
  public void startTime() {
	  startTime = System.currentTimeMillis() / 1000;
  }
  
  /**
   * Elapsed time between startTime and time after events occur is measured and stored
   * pre: none
   * post: endTime and elapsedTime are initialized
   */
  public void checkTime() {
	  endTime = System.currentTimeMillis() / 1000;
	  elapsedTime = (int) (endTime-startTime);
  }
  
  /**
   * Remaining time for the game is displayed
   * pre: none
   * post: timeMessage is initialized
   */
  public void displayTime() {
	  int minutes = (duration - elapsedTime) /60;
	  int seconds = (duration - elapsedTime) % 60;
	  timeMessage = minutes + " minutes and " + seconds + " seconds remain";
  }
  
  /**
   * Checks if elapsedTime has exceeded the set duration of the game
   * pre: none
   * post: A boolean is returned
   */
  public boolean isTimerDone() {
	  if (elapsedTime >= duration) {
		  return true;
	  }
	  return false;
  }
  
  /**
   * Variables necessary for text file reading are initialized
   * pre: The text file to be read must be found in the same folder as this class
   * post: Variables and the stream are initialized
   */
  public void initializeFile() {
	  pathName1 = "WordList.txt";
	  readFile = new File(pathName1);
	  input = null;
	  try 
	  {
		  input = new Scanner(readFile);
	  }
	  catch (FileNotFoundException ex)
	  {
		  System.out.print(" *** Cannot open " + pathName1 + "***");
		  System.exit(1);
	  }
  }
  
  /**
   * The last inputed word is compared to all the words for WordList.txt
   * pre: wordList must not empty
   * post: returns a boolean
   */
  public boolean checkWordFile() {
	  String line;
	  while (input.hasNextLine()) {
		  line = input.nextLine();
		  if (wordList.get(wordList.size()-1).equals(line)) {
			  return true;
		  }
	  }
	  return false;
  }
  
  /**
   * words.txt is prepared for file writing
   * pre: The text file to be written on must be found in the same folder as this class
   * post: words.txt is overwritten and PrintWriter is initialized
   */
  public void initalizeWordFile() {
	  pathName2 = "words.txt";
	  writeFile = new File(pathName2);
	  output = null;
	  
	  try 
	  {
		  output = new PrintWriter (writeFile);
	  } 
	  catch (FileNotFoundException ex)
	  {
		  System.out.print("Cannot create: " + pathName2);
		  System.exit(1);
	  }
	  output.println("List of words and corresponding points\n");
	  output.println("Player: " + playerName);
	  output.format("%-12s %2s", "Words", "Points\n");
	  output.close();
	  
  }
  
  /**
   * A list of the player's inputed words and corresponding points are written
   * to the words.txt file
   * pre: The text file to be written on must be found in the same folder as this class
   * post: words.txt is written on
   */
  public void writePoints() {
	  writer = null;
	  try
	  {
		  writer = new FileWriter (pathName2, true);
	  }
	  catch (IOException ex)
	  {
		  System.out.print("Cannot create/open: " + pathName2);
		  System.exit(1);
	  }
	  PrintWriter output = new PrintWriter (writer);
	  output.println("");
	  output.format("%-11s %2d", wordList.get(wordList.size()-1), getPoints());
	  output.println("");
	  output.close();
  }
  
  /**
   * Corresponding points are accumulated after words are inputed
   * pre: none
   * post: variable 'points' is incremented
   */
  public void addPoints() {
	  if ((wordList.get(wordList.size()-1)).length() == 3 || (wordList.get(wordList.size()-1)).length() == 4) {
		  points += 1;
	  } else if ((wordList.get(wordList.size()-1)).length() == 5) {
		  points += 2;
	  } else if ((wordList.get(wordList.size()-1)).length() == 6) {
		  points += 3;
	  } else if ((wordList.get(wordList.size()-1)).length() == 7) {
		  points += 5;
	  } else if ((wordList.get(wordList.size()-1)).length() >= 8) {
		  points += 11;
	  }
  }
  
  /**
   * The corresponding points to the last inputed word is returned
   * pre: wordList can't be empty
   * post: a number from 1 to 15 is returned
   * 
   */
  public int getPoints() {
	  if ((wordList.get(wordList.size()-1)).length() == 3 || (wordList.get(wordList.size()-1)).length() == 4) {
		  return 1;
	  } else if ((wordList.get(wordList.size()-1)).length() == 5) {
		  return 2;
	  } else if ((wordList.get(wordList.size()-1)).length() == 6) {
		  return 3;
	  } else if ((wordList.get(wordList.size()-1)).length() == 7) {
		  return 5;
	  } else if ((wordList.get(wordList.size()-1)).length() >= 8) {
		  return 11;
	  } else {
		  return 0;
	  }
  }
  
  /**
   * Boolean gameState is changed to true
   * pre: none
   * post: gameState is now true
   */
  public void endGame() {
	  gameState = true;
  }
  
  /**
   * Checks the current state of gameState
   * pre: none
   * post: a boolean is returned
   */
  public boolean isGameDone() {
	  if (gameState == true) {
		  return true;
	  }
	  return false;
  }
  
  
  private void paintText(Graphics g)
  {
	
    g.setColor(Color.BLACK);
    g.drawRect(X_OFFSET, Y_OFFSET, X_DIM*wordGrid.length, Y_DIM*wordGrid[0].length);
    g.setFont(new Font(g.getFont().getFontName(), Font.PLAIN, M_FONT_SIZE));
    g.setColor(Color.WHITE);
    g.clearRect(X_OFFSET, Y_OFFSET+Y_DIM*wordGrid[0].length+1+BUTTON_HEIGHT+BUTTON_MESSAGE_GAP, X_DIM*wordGrid.length, MESSAGE_HEIGHT);
    g.setColor(Color.BLACK);
    g.drawRect(X_OFFSET, Y_OFFSET+Y_DIM*wordGrid[0].length+1+BUTTON_HEIGHT+BUTTON_MESSAGE_GAP, X_DIM*wordGrid.length, MESSAGE_HEIGHT);
    g.drawString(message, X_OFFSET+10, MESSAGE_HEIGHT/2+M_FONT_SIZE/2+Y_OFFSET+Y_DIM*wordGrid[0].length+BUTTON_HEIGHT+BUTTON_MESSAGE_GAP);
   
  }
  
  private void paintGrid(Graphics g)
  {
    g.setFont(new Font(g.getFont().getFontName(), Font.PLAIN+Font.BOLD, FONT_SIZE));    
    for (int i = 0; i < wordGrid.length; i++)
    {
      for (int j = 0; j < wordGrid[i].length; j++)
      {    
    	  Coordinate c = new Coordinate (i,j);
    	  if (isSelected (c)) { //if the current coordinate is in the selection arrayList, the cord is highlighted
    		  g.setColor(GRID_COLOR_HIGHLIGHT);
    		  g.fillRoundRect(X_OFFSET+X_DIM*j, Y_OFFSET+Y_DIM*i, X_DIM, Y_DIM, 25, 25);
    		  g.setColor(Color.BLACK);
    		  g.drawRoundRect(X_OFFSET+X_DIM*j, Y_OFFSET+Y_DIM*i, X_DIM, Y_DIM, 25, 25);
    		  g.drawString(wordGrid[j][i], X_OFFSET+X_DIM*j+1*X_DIM/4, Y_OFFSET+Y_DIM*i+1*Y_DIM/2);
    	  } else { 
    		  g.setColor(GRID_COLOR_B);
    		  g.fillRoundRect(X_OFFSET+X_DIM*j, Y_OFFSET+Y_DIM*i, X_DIM, Y_DIM, 25, 25);
    		  g.setColor(GRID_COLOR_A);
    		  g.drawRoundRect(X_OFFSET+X_DIM*j, Y_OFFSET+Y_DIM*i, X_DIM, Y_DIM, 25, 25);
    		  g.setColor(Color.BLACK);
    		  g.drawString(wordGrid[j][i], X_OFFSET+X_DIM*j+1*X_DIM/4, Y_OFFSET+Y_DIM*i+1*Y_DIM/2);
    	  }

        
      }
    }
    
    
  }
     
  
  
  private void drawLine(Graphics g)
  {
    for (int i =0; i < numLines; i++ ) 
    {
      ((Graphics2D) g).setStroke( new BasicStroke(5.0f) );
      g.drawLine(X_OFFSET+X_DIM/2+line[0][i]*X_DIM, Y_OFFSET+Y_DIM/2+line[1][i]*Y_DIM, X_OFFSET+X_DIM/2+line[2][i]*X_DIM, Y_OFFSET+Y_DIM/2+line[3][i]*Y_DIM);
      ((Graphics2D) g).setStroke( new BasicStroke(0.5f) );
    }
  }

  
  /** The method that draws everything
   */
  public void paint( Graphics g ) 
  {
    this.paintGrid(g);
    this.paintText(g);
    this.drawLine(g);
    //this.paintTimer(g);
     
  }
  
  /** Sets the message to be displayed under the board
   */
  public void displayMessage(String theMessage)
  {
    message = theMessage;
    repaint();
  }
   
  /** Draws a line on the board using the given co-ordinates as endpoints
   */
  public void drawLine(int row1, int col1, int row2, int col2)
  {
    line[0][numLines]=col1;
    line[1][numLines]=row1;
    line[2][numLines]=col2;
    line[3][numLines]=row2;
    numLines++;
    repaint();
  }

  /** Removes one line from a board given the co-ordinates as endpoints
   * If there is no such line, nothing happens
   * If multiple lines, all copies are removed
   */
  
  public void removeLine(int row1, int col1, int row2, int col2) 
  {
    int curLine = 0;
    while (curLine < numLines) 
    {
      // Check for either endpoint being specified first in our line table
      if ( (line[0][curLine] == col1 && line[1][curLine] == row1 &&
            line[2][curLine] == col2 && line[3][curLine] == row2)   || 
           (line[2][curLine] == col1 && line[3][curLine] == row1 &&
            line[0][curLine] == col2 && line[1][curLine] == row2) )
      {
        // found a matching line: overwrite with the last one
        numLines--;
        line[0][curLine] = line[0][numLines];
        line[1][curLine] = line[1][numLines];
        line[2][curLine] = line[2][numLines];
        line[3][curLine] = line[3][numLines];
        curLine--; // perhaps the one we copied is also a match
      }
      curLine++;
       
    }
    repaint();
  }
  
  /** Waits for user to click somewhere and then returns the click.
   */
  public Coordinate getClick()
  {
      Coordinate returnedClick = null;
      synchronized(this) {
          lastClick = null;
          while (lastClick == null)
          {
              try {
                  this.wait();
              } catch(Exception e) {
                  // We'll never call Thread.interrupt(), so just consider
                  // this an error.
                  e.printStackTrace();
                  System.exit(-1) ;
              } /* try */
          }
    
          int col = (int)Math.floor((lastClick.getCol()-X_OFFSET)/X_DIM);
          int row = (int)Math.floor((lastClick.getRow()-Y_OFFSET)/Y_DIM);
    
          // Put this into a new object to avoid a possible race.
          returnedClick = new Coordinate(row,col);
      }
      return returnedClick;
  }
}
  

