package scout.g3_ananth;

import scout.sim.*;
import scout.g3_ananth.*;
import java.util.*;
import java.lang.Math;

public class Search2
{
  public Point Outpost1;
  public Point Outpost2;
  public Point Outpost3;
  public Point Outpost4;
  public Point Centre;


  public AbsoluteGrid gridspace;
  public AbsoluteGrid O1_dist;
  public AbsoluteGrid O2_dist;
  public AbsoluteGrid O3_dist;
  public AbsoluteGrid O4_dist;
  public AbsoluteGrid overall_dist;// Stores the closest overall distance. As of now only this is updated. The other values are just for initialization;

  public AbsoluteGrid C_dist;

  public int ortho_coeff = 3;
  public int diag_coeff = 4; // change this based on enemy fraction we expect

  public int n;
  public ArrayList<Point> dir = new ArrayList<Point>();

  public Search2(int n) // create a brand new grid
  {
    this.gridspace = new AbsoluteGrid(n);
    this.n = n;

    this.O1_dist = new AbsoluteGrid(n);
    this.O2_dist = new AbsoluteGrid(n);
    this.O3_dist = new AbsoluteGrid(n);
    this.O4_dist = new AbsoluteGrid(n);


    this.overall_dist = new AbsoluteGrid(n);
    this.C_dist = new AbsoluteGrid(n);

    this.Outpost1 = new Point(0,0);
    this.Outpost2 = new Point(0,n+1);
    this.Outpost3 = new Point(n+1,n+1);
    this.Outpost4 = new Point(n+1,0); // Ordered clockwise you can change it if you need another ordering.

    this.Centre = new Point(n/2,n/2); // Update this for a different centre.


    initDistances(); // Init

  }
  public void printGrids()
  {
    System.out.println("Grid Space: ");
    gridspace.printGrid();

    System.out.println("Centre Distances");
    C_dist.printGrid();

    System.out.println("Min distance to any outpost");
    overall_dist.printGrid();
  }
  public void initDistances() // Initialize the distance arrays
  {



     //System.out.println("Outpost 4 dist:");
    for(int i = 0;i<=n+1;i++)
    {
      for(int j = 0;j<=n+1;j++)
      {
        C_dist.grid[i][j]=DiagonalDist(new Point(i,j),this.Centre);

        O1_dist.grid[i][j] = DiagonalDist(new Point(i,j),this.Outpost1);
        O2_dist.grid[i][j] = DiagonalDist(new Point(i,j),this.Outpost2);
        O3_dist.grid[i][j] = DiagonalDist(new Point(i,j),this.Outpost3);
        O4_dist.grid[i][j] = DiagonalDist(new Point(i,j),this.Outpost4);

        int mn = Math.min(O1_dist.grid[i][j],O2_dist.grid[i][j]);
        mn = Math.min(mn,O3_dist.grid[i][j]);
        mn = Math.min(mn,O4_dist.grid[i][j]);

        overall_dist.grid[i][j] = mn;
        //System.out.print(Outpost4_Dist.grid[i][j]+" ");
      }

      //System.out.println();
    }

    dir.add(new Point(-1,-1));
    dir.add(new Point(0,-1));
    dir.add(new Point(1,-1));
    dir.add(new Point(1,0));
    dir.add(new Point(1,1));
    dir.add(new Point(0,1));
    dir.add(new Point(-1,1));
    dir.add(new Point(-1,0));




  }

  public static void merge(Search2 s1, Search2 s2)  //merge the values of two Searches. Call this when two players are at the same location or when scout gets to an Outpost
  {
    AbsoluteGrid.merge(s1.gridspace,s2.gridspace);
    AbsoluteGrid.mergeMin(s1.overall_dist,s2.overall_dist); //Min point wise
    AbsoluteGrid.mergeMin(s1.C_dist,s2.C_dist); //Min point wise

    // AbsoluteGrid.merge(s1.O1_dist,s2.O1_dist);
    // AbsoluteGrid.merge(s1.O2_dist,s2.O2_dist);
    // AbsoluteGrid.merge(s1.O3_dist,s2.O3_dist);
    // AbsoluteGrid.merge(s1.O4_dist,s2.O4_dist);



  }

  public boolean validPoint(Point neb)
  {
    return(0<=neb.x && neb.x<=this.n+1 && 0<=neb.y && neb.y<=this.n+1);
  }

  public int getAdjacentDistance(Point neb, Point p) // returns distance between neb and p. IF neb is unexplored, uses estimate of this object
  {
    int dstmoved = -1;
    if(gridspace.getLocation(neb)==0)//Unexplored
    {
      dstmoved = DiagonalDist(neb,p,this.ortho_coeff,this.diag_coeff);
    }
    else if(gridspace.getLocation(neb)==1)//Enemy
    {
      dstmoved = DiagonalDist(neb,p,6,9);
    }
    else if(gridspace.getLocation(neb)==2) //Safe Location
    {
      dstmoved = DiagonalDist(neb,p,2,3);
    }

    return dstmoved;
  }
  public boolean UpdateOverallDistLocation(Point loc) //returns true if the value was changed. No propogation of relaxtation. Returns true if value was changed
  {
      int original_value = overall_dist.getLocation(loc);
      int mn = original_value;
      for(Point p : dir)
      {
        Point neb = new Point(p.x+loc.x,p.y+loc.y);

        if(validPoint(neb))
        {
          int dst = getAdjacentDistance(neb,loc)+overall_dist.getLocation(neb);
          mn = Math.min(mn,dst);
        }
      }

      overall_dist.setLocation(loc,mn);

      if(original_value!=overall_dist.getLocation(loc))
      {
        return true;
      }
      else
        return false;
  }

  public ArrayList<Point> getNeighbours(Point loc) //returns valid neighbhours of loc
  {
    ArrayList<Point> ans = new ArrayList<Point>();

    for(Point p : dir)
    {
      Point neb = new Point(p.x+loc.x,p.y+loc.y);

      if(validPoint(neb))
      {
        ans.add(neb);
      }
    }

    return ans;
  }
  public boolean UpdateCentralDistLocation(Point loc) //Updates distance to centre at location. No propogation. Returns true if the value was changed
  {
    int original_value = C_dist.getLocation(loc);
    int mn = original_value;
    for(Point p : dir)
    {
      Point neb = new Point(p.x+loc.x,p.y+loc.y);

      if(validPoint(neb))
      {
        int dst = getAdjacentDistance(neb,loc)+C_dist.getLocation(neb);
        mn = Math.min(mn,dst);
      }
    }

    C_dist.setLocation(loc,mn);

    if(original_value!=C_dist.getLocation(loc))
    {
      return true;
    }
    else
      return false;
  }

  public void UpdateOverallDistAndPropogate(Point loc, int depth) // depth is the depth of the BFS of updates. Set to 5 originally
  {
    depth = 5; // Comment this out when you have a good idea of what depth to have

    Queue<Point> q1 = new LinkedList<>();
    Set<Point> seen = new HashSet<Point>();

    UpdateOverallDistLocation(loc);
    seen.add(loc);
    q1.add(loc);

    int d= 0;
    while(!(q1.size()==0) && d <= depth)
    {

      Point front = q1.poll();

      ArrayList<Point> nebs = getNeighbours(front);

      for(Point neb: nebs)
      {
        if(!seen.contains(neb))
        {
          boolean val = UpdateOverallDistLocation(neb);
          seen.add(neb);
          if(val)
          {
            q1.add(neb);
          }
        }
      }
        d+=1;
    }

  }

  public void UpdateCentralDistAndPropogate(Point loc, int depth) // depth is the depth of the BFS of updates. Set to 5 originally
  {
    depth = 5; // Comment this out when you have a good idea of what depth to have

    Queue<Point> q1 = new LinkedList<>();
    Set<Point> seen = new HashSet<Point>();

    UpdateCentralDistLocation(loc);
    seen.add(loc);
    q1.add(loc);

    int d= 0;
    while(!(q1.size()==0) && d <= depth)
    {

      Point front = q1.poll();

      ArrayList<Point> nebs = getNeighbours(front);

      for(Point neb: nebs)
      {
        if(!seen.contains(neb))
        {
          boolean val = UpdateCentralDistLocation(neb);
          seen.add(neb);
          if(val)
          {
            q1.add(neb);
          }
        }
      }
        d+=1;
    }

  }
  public int getCentreDistEstimate(Point loc) // Returns current distance estimate to centre
  {
    return C_dist.getLocation(loc);
  }

  public int getOverallDistEstimate(Point loc) //Returns current distance estimate to
  {
    return overall_dist.getLocation(loc);
  }

  public void setEnemy(Point loc) // DOES NOT UPDATE DISTANCE ON IT"S OWN. Call Updates to do that;
  {
    this.gridspace.setLocation(loc,1);
  }

  public void setSafe(Point loc) // DOES NOT UPDATE DISTANCE ON IT"S OWN. Call Updates to do that;
  {
    this.gridspace.setLocation(loc,2);

  }

  // public void setEnemyAndUpdate(Point loc)
  // {
  //   this.setEnemy(loc);
  //   this.UpdateCentralDistLocation(loc);
  // }
  public Point getOutpost(int i) //Return the Outpost given an index
  {
    if(i==1)
      return Outpost1;
    else if(i==2)
      return Outpost2;
    else if(i==3)
        return Outpost3;
        else
          return Outpost4;
  }

  public Integer getManhattanDistance(Point a, Point b) //Manhattan Distance
  {
    return Math.abs(a.x-b.x) + Math.abs(a.y-b.y);

  }

  public int DiagonalDist(Point a, Point b) // Calculates distance along diagonal and then orthognal between two points
  {
    Point RD = relativeDist(a,b);
    return DiagonalDisttoZero(RD);
  }

  public int DiagonalDist(Point a, Point b, int o, int d) // Calculates distance along diagonal and then orthognal between two points
  {
    Point RD = relativeDist(a,b);
    return DiagonalDisttoZero(RD,o,d);
  }

  public int DiagonalDisttoZero(Point p)
  {
    int absx = Math.abs(p.x);
    int absy = Math.abs(p.y);

    int mx = Math.max(absx,absy);
    int mn = Math.min(absx,absy);

    return this.ortho_coeff*(mx-mn) + this.diag_coeff*mn;
  }

  public int DiagonalDisttoZero(Point p, int o, int d)
  {
    int absx = Math.abs(p.x);
    int absy = Math.abs(p.y);

    int mx = Math.max(absx,absy);
    int mn = Math.min(absx,absy);

    return o*(mx-mn) + d*mn;
  }


  public Point relativeDist(Point a, Point b) // Calculates the vector connecting a with b
  {
    //Gives point pointing from a to b i.e b-a

    return new Point(b.x-a.x,b.y-a.y);
  }

}
