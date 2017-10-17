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

    searcher.setSafe(new Point(3,3));

    searcher.UpdateOverallDistAndPropogate(new Point(3,3),5);
    searcher.UpdateCentralDistAndPropogate(new Point(3,3),5);

    searcher.printGrids();
	}
}
