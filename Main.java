import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class Main {
	
	final static int ROWS = 4;
    final static int COLS = 4;
    static Board b;
    //static Time t;
    private static int row;
    private static int col;
    static JFrame frame;
    JOptionPane optionPane = new JOptionPane();
    
	public static void main(String[] args) throws IOException {
		Coordinate c;
		b = new Board(ROWS,COLS);
		 
		 while (b.isGameDone() == false) { 
			 c=b.getClick();
			 row = c.getRow();
			 col = c.getCol();
			 if (b.isSelected(c) && b.isLastCord(c)) { //removes the highlight of a clicked cord if it was
				 b.removeSelection(c);				   //already highlighted
				 b.removeLetter(c);
			 } else if (b.isFirstClick()){			   //checks if the click on the board was the first highlight
				 b.addSelection(c);					   //(necessary for method board isAdjacent() to work)
				 b.addLetter(c);
			 } else if (b.isAdjacent(b.getLastCord(), c) && b.isSelected(c) == false) {  
				 b.addSelection(c);		//adds the current selection to the list of selections if									
				 b.addLetter(c);		//if it is adjacent to the last selection 						
			 } else if (b.isAdjacent(b.getLastCord(), c) == false) { 			  //if the current selection is not 		
				 b.displayMessage("Letters must be adjacent to the last letter"); //adjacent to the last selection an 
				 																  //error message is displayed
			 } else if (b.isSelected(c) && b.isLastCord(c) == false) { //An error message is displayed if the user
				 b.displayMessage("Remove only the last letter");	   //attempts to remove a selection that is not
				 													   //the most recent selection
			 } 
		 }
		 
	}

}
