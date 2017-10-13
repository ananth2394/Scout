package scout.g3_ananth;

import scout.sim.*;
import scout.g3_ananth.*;
import java.util.*;
import java.lang.Math;

public class Search
{
class State{

  public Point location;
  public Point parent;
  public Integer dist;

  public State(Point location, Point parent, Integer dist)
  {
    this.location = location;
    this.parent = parent;
    this.dist = dist;
  }

  public ArrayList<State> getAllValidNextStates()
  {
    ArrayList<State> ans  = new ArrayList<State>();

    ArrayList<Point> points = Search.this.getAllValidNextStates(location);
    // System.out.println("Current location: " + location.x + " " + location.y);
    // System.out.println("Nearby: ");
    for(Point p : points)
  {  // { System.out.println("Nearby point: " + p.x + " " + p.y);
      State s = new State(p,this.location,this.dist + getGridDistance(location,p));
      ans.add(s);
    }

    return ans;
  }

}
class HeuristicComparator implements Comparator<Object> {
public Point goal;
public HeuristicComparator(Point goal)
{
  this.goal = goal;
}
public int compare(Object o1, Object o2) {

    State p1 = (State) o1;
    State p2 = (State) o2;

    Integer abs1 = getManhattanDistance(p1.location,goal) + p1.dist;
    Integer abs2 = getManhattanDistance(p2.location,goal) + p2.dist;

    return Integer.compare(abs1,abs2);
  }
}

  public AbsoluteGrid grid;
  public int n;
  public Search(AbsoluteGrid grid)
  {
    this.grid = new AbsoluteGrid(grid);
    n = this.grid.grid.length;
    //this.grid.setGrid(grid);

  }

  public ArrayList<Point> getAllValidNextStates(Point x)
  {
    ArrayList<Point> ans = new ArrayList<Point> ();
    for(int i =-1;i<=1;i++)
    {
      for(int j=-1;j<=1;j++)
      {
        Point delta = new Point(i,j);
        Point newstate = sumVal(x,delta);

        if(0<=newstate.x && newstate.x<n && 0<=newstate.y && newstate.y<n)
        {
          ans.add(newstate);
        }
      }

    }
    System.out.println(ans);
    return ans;
  }

  public Point relativeDist(Point a, Point b)
  {
    //Gives point pointing from a to b i.e b-a

    return new Point(b.x-a.x,b.y-a.y);
  }

  public Point sumVal(Point a,Point b)
  {
    return new Point(b.x+a.x,b.y+a.y);
  }

  private String ijToDirection(int i, int j) {
		if (i==0 && j==0) {
			return "NW";
		} else if (i==0 && j==1) {
			return "N";
		} else if (i==0 && j==2) {
			return "NE";
		} else if (i==1 && j==0) {
			return "W";
		} else if (i==1 && j==1) {
			return "C";
		} else if (i==1 && j==2) {
			return "E";
		} else if (i==2 && j==0) {
			return "SW";
		} else if (i==2 && j==1) {
			return "S";
		} else {
			return "SE";
		}
	}

  private String pointToDirection(Point p)
  {
    return ijToDirection(p.x+1, p.y+1);
  }
  public Integer getManhattanDistance(Point a, Point b)
  {
    return Math.abs(a.x-b.x) + Math.abs(a.y-b.y);

  }
  public Integer getGridDistance(Point a, Point b)
  {
    Integer base_dist;
    Integer manhat = getManhattanDistance(a,b);

    if(manhat==0)
      base_dist = 0;
    else if(manhat == 1)
      base_dist = 2;
    else
      base_dist = 3;
    // System.out.println("Point a:");
    // System.out.println(a.x);
    //   System.out.println(a.y);
    //     System.out.println();
    if(this.grid.grid[a.x][a.y]==1)
    {
      base_dist = base_dist*3;
    }

    return base_dist;
  }
  public Pair<Integer,List<String> > FindShortestPath(Point start, Point end)
  {
    this.grid.printGrid();
  //  Point start2 = new Point(start);
    //Point end2 = new Point(end);

    System.out.println("Start: " + start.x + " " + start.y);
    System.out.println("End: " + end.x + " " + end.y);
    HeuristicComparator strategy = new HeuristicComparator(end);
     HashMap<Point,Integer> Distance = new HashMap<Point,Integer>();
     HashSet<Point> Seen = new HashSet<Point>();
     HashMap<Point,Point> Parent = new HashMap<Point,Point>();
    PriorityQueue<State > Pqueue = new PriorityQueue<State>(10,strategy);

    List<Point> path = new ArrayList<Point>();
    List<String> string_path = new ArrayList<String>();

    //Distance.put(start,0);
    //Seen.add(start);
    Pqueue.add(new State(start,new Point(-1,-1),0) );


    while(!(Pqueue.size()==0))
    {
        State top = Pqueue.poll();
        if(Seen.contains(top.location))
          continue;
        Seen.add(top.location);
        Distance.put(top.location,top.dist);
        Parent.put(top.location,top.parent);

        System.out.println("Top location:" + top.location.x + " " + top.location.y);
          System.out.println("Top Distance:" + top.dist);
          System.out.println("Top Parent:" + top.parent.x + " " + top.parent.y);

        ArrayList<State> validstates = top.getAllValidNextStates();

        for(State s: validstates)
        {

          if(!Seen.contains(s.location))
          {
            Pqueue.add(s);
          }
        }
    }

    if(Seen.contains(end))
    {     Point p = end;
      System.out.println("Start: " + start.x + " " + start.y);
      System.out.println("End: " + end.x + " " + end.y);
          while(!p.equals(start))
          {
            Point par = Parent.get(p);
            Point RD = relativeDist(par,p);

            path.add(RD);
            System.out.println("Parent: " + par.x + par.y);
            System.out.println("Move: " + RD.x + RD.y+"\n");

            p = par;
          }

          for(Point ptr:path)
          {
            string_path.add(pointToDirection(ptr));
          }
          Collections.reverse(string_path);
          for(int i = 0;i<n;i++)
          {
            for(int j = 0;j<n;j++)
              {
                System.out.print(Distance.get(new Point(i,j)));
                System.out.print(" ");
              }
              System.out.println();
          }
          return new Pair<Integer, List<String> > (Distance.get(end),string_path);

  }

  else

  {

      return new Pair<Integer, List<String> > (-1,new ArrayList<String>());
  }
  }


}
