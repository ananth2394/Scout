package scout.g3_ananth;

import scout.sim.*;
import scout.g3_ananth.*;
import java.util.*;
import java.lang.Math;

public class TestSearch
{
  public static void main(String[] args){
    AbsoluteGrid grid = new AbsoluteGrid(5);

    Search s = new Search(grid);
    s.grid.printGrid();

    s.grid.addEnemy(new Point(3,3));
    s.grid.addEnemy(new Point(4,4));
    Pair<Integer,List<String> > soln = s.FindShortestPath(new Point(0,0),new Point(5,5));

    System.out.println(soln.getFirst());
    System.out.println(soln.getSecond());
}
}
