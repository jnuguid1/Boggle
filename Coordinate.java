public class Coordinate
{
  private int row;
  private int col;
  
  public Coordinate(int theRow, int theCol)
  {
    row = theRow;
    col = theCol;
  }
  
  public int getRow()
  {
    return row;
  }
    
  public int getCol()
  {
    return col;
  }
  
  public boolean equals(Object c)
  { 
    return this.row == ((Coordinate)c).row && this.col == ((Coordinate)c).col;
  }
  
  public String toString()
  {
    return this.row + "," + this.col;
  }
} 