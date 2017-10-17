package scout.g3_ananth;

import scout.sim.*;
import scout.g3_ananth.*;
import java.util.*;
import java.lang.Math;

public class Test
{

  public static void main(String [] args)
	{
		Search2 searcher = new Search2(5);

    searcher.printGrids();

    for(int i = 2;i<=4;i++)
    {
      for(int j = 2;j<=4;j++)
      {
        searcher.setSafe(new Point(i,j));
      }
    }



    searcher.UpdateOverallDistAndPropogate(new Point(3,3),5);
    searcher.UpdateCentralDistAndPropogate(new Point(3,3),5);

    searcher.printGrids();
	}
}
