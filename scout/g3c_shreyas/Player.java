package scout.g3c_shreyas;

import java.util.ArrayList;
import java.util.List;

import scout.sim.CellObject;
import scout.sim.Outpost;
import scout.sim.Point;

public class Player extends scout.sim.Player{
	private int id;
	private int s;
	private int n;
	private int t;
	private Point pos;
	private boolean oriented;
	private List<Point> landmarks;
	
	private Point initialOutpost;
	
	//dimension of smallest exploration square
	private int d;
	
	//dimension of own exploration square
	private int k;
	
	private Point exploreTopLeft;
	private Point targetCorner;
	
	//if on exploration square
	private boolean onSquare;
	//if return to top left
	private boolean backToOrigin;
	
	//keeps sharing info between outposts
	private boolean isLaborer;
	
	private class Mapper{
		public ArrayList<Point> enemyLocations;
		public ArrayList<Point> safeLocations;
		
		public Mapper(){
			enemyLocations = new ArrayList<Point>();
			safeLocations = new ArrayList<Point>();
		}
	}
	
	private Mapper mapper;

	public Player(int id) {
		super(id);
	}

	@Override
	public void init(String id, int s, int n, int t, List<Point> landmarkLocations) {
		this.id = Integer.parseInt(id.substring(1));
		this.s = s;
		this.n = n;
		this.t = t;
		landmarks = landmarkLocations;
		pos = new Point(-1,-1);
		oriented = false;
		
		initialOutpost = getInitialOutpost();
		
		d = 4;
//		k = d + 3*2*this.id;
//		k = d + 6*((this.id + 1)%(int)(Math.floor((n - d + 8)/6)) - 1);
		k = d + 3*(this.id%((int)(Math.floor((n-d)/3)) + 1));
		
		exploreTopLeft = new Point((n/2) - (k/2)+1,(n/2) - (k/2)+1);
		targetCorner = getTargetCorner();
		
		onSquare = false;
		backToOrigin = false;

		mapper = new Mapper();
		
//		System.out.println("id: " + id);
//		System.out.println("top left x: " + exploreTopLeft.x);
//		System.out.println("top left y: " + exploreTopLeft.y);
	}

	private Point getTargetCorner() {
		if(this.id < s/4)
			return new Point(exploreTopLeft.x,exploreTopLeft.y);
		else if(this.id >= s/4 && this.id < 2*s/4)
			return new Point(exploreTopLeft.x + k - 1,exploreTopLeft.y);
		else if(this.id >= 2*s/4 && this.id < 3*s/4)
			return new Point(exploreTopLeft.x + k - 1,exploreTopLeft.y + k - 1);
		else if(this.id >= 3*s/4 && this.id < s)
			return new Point(exploreTopLeft.x,exploreTopLeft.y + k - 1);
		
		
//		if(this.id < 5)
//			return new Point(exploreTopLeft.x,exploreTopLeft.y);
//		else if(this.id >= 5 && this.id < 10)
//			return new Point(exploreTopLeft.x + k - 1,exploreTopLeft.y);
//		else if(this.id >= 10 && this.id < 15)
//			return new Point(exploreTopLeft.x + k - 1,exploreTopLeft.y + k - 1);
//		else if(this.id >= 15 && this.id < 20)
//			return new Point(exploreTopLeft.x,exploreTopLeft.y + k - 1);
		return null;
	}

	private Point getInitialOutpost() {
		if(this.id < s/4)
			return new Point(0,0);
		else if(this.id >= s/4 && this.id < 2*s/4)
			return new Point(n+1,0);
		else if(this.id >= 2*s/4 && this.id < 3*s/4)
			return new Point(n+1,n+1);
		else if(this.id >= 3*s/4 && this.id < s)
			return new Point(0,n+1);
		
		
//		if(this.id < 5)
//			return new Point(0,0);
//		else if(this.id >= 5 && this.id < 10)
//			return new Point(n+1,0);
//		else if(this.id >= 10 && this.id < 15)
//			return new Point(n+1,n+1);
//		else if(this.id >= 15 && this.id < 20)
//			return new Point(0,n+1);
		return null;
	}

	//Strategy:
	//1. If not oriented, move to the allocated outpost to get oriented
	//2. If oriented, move towards the closest corner of your exploration square
	//3. ID 0 takes smallest, ID 1 second smallest, ID 2 the third smallest exploration square ... and the squares wrap around if ID is too large
	//4. Difference in lengths of consecutive exploration squares is 3 to avoid any overlap
	//5. Smallest square is of length 4
	@Override
	public Point move(ArrayList<ArrayList<ArrayList<String>>> nearbyIds, List<CellObject> concurrentObjects) {
		t--;
		
		updateMap(nearbyIds);
		
		if(nearbyIds.get(1).get(1).toString().matches(".*O.*")){
			for(Point p:mapper.enemyLocations){
				p.x = p.x -(pos.x) + initialOutpost.x;
				p.y = p.y -(pos.y) + initialOutpost.y;
			}
			for(Point p:mapper.safeLocations){
				p.x = p.x -(pos.x) + initialOutpost.x;
				p.y = p.y -(pos.y) + initialOutpost.y;
			}
			
			oriented = true;
			pos.x = initialOutpost.x;
			pos.y = initialOutpost.y;
			
		}
		
		if(!oriented){
			return goToOutpost(nearbyIds, initialOutpost);
		}
		else if(!backToOrigin){
			if(pos.equals(targetCorner))
				onSquare = true;
			
			if(!onSquare){
				return moveTowardsPointIfOriented(targetCorner);
			}
			else if(onSquare){				
				//if the player is on the top horizontal edge of the square
				if(pos.y == exploreTopLeft.y && pos.x < exploreTopLeft.x + (k-1)){
					if(pos.x + 1 == targetCorner.x)
						backToOrigin = true;
					return moveEast();
				}
				//if the player is on the right vertical edge of the square
				else if(pos.x == exploreTopLeft.x + (k-1) && pos.y < exploreTopLeft.y + (k-1)){
					if(pos.y + 1 == targetCorner.y)
						backToOrigin = true;
					return moveSouth();
				}
				//if the player is on the bottom horizontal edge of the square
				else if(pos.y == exploreTopLeft.y + (k-1) && pos.x > exploreTopLeft.x){
					if(pos.x - 1 == targetCorner.x)
						backToOrigin = true;
					return moveWest();
				}
				//if the player is on the left vertical edge of the square
				else{
					if(pos.y - 1 == targetCorner.y)
						backToOrigin = true;
					return moveNorth();
				}
			}
		}
		//if player is back to top left of exploration square
		else if(backToOrigin){
			return goToOutpost(nearbyIds, initialOutpost);
		}
		
		return null;
	}

	private Point moveTowardsPointIfOriented(Point p) {
		int directionX = (p.x - pos.x != 0)?((p.x - pos.x)/Math.abs(p.x - pos.x)):0;
		int directionY = (p.y - pos.y != 0)?((p.y - pos.y)/Math.abs(p.y - pos.y)):0;
		
		pos.x = pos.x + directionX;
		pos.y = pos.y + directionY;
		System.out.println("Direction: (" + directionX + "," + directionY + ")");
		return new Point(directionX,directionY);
	}

	private Point goToOutpost(ArrayList<ArrayList<ArrayList<String>>> nearbyIds, Point target) {
		int directionX = 2*((int)target.x/(n+1)) - 1;
		int directionY = 2*((int)target.y/(n+1)) - 1;
		
		System.out.println("Direction: (" + directionX + "," + directionY + ")");
		
		if(nearbyIds.get(directionX+1).get(directionY+1) != null){
			pos.x = pos.x + directionX;
			pos.y = pos.y + directionY;
			return new Point(directionX,directionY);
		}	
		else if(nearbyIds.get(directionX+1).get(1) != null){
			pos.x = pos.x + directionX;
			return new Point(directionX,0);
		}
		else if(nearbyIds.get(1).get(directionY+1) != null){
			pos.y = pos.y + directionY;
			return new Point(0,directionY);
		}
		else return null;
	}

	private void updateMap(ArrayList<ArrayList<ArrayList<String>>> nearbyIds) {
		for(int i = 0; i < 3; i++){
			for(int j = 0; j < 3; j++){
				if(nearbyIds.get(i).get(j) == null)
					continue;
				
				else if(nearbyIds.get(i).get(j).toString().matches(".*E.*")){
					mapper.enemyLocations.add(new Point(pos.x + i - 1, pos.y + j - 1));
				}
				
				else{
					mapper.safeLocations.add(new Point(pos.x + i - 1, pos.y + j - 1));
				}
			}
		}
	}

	@Override
	public void communicate(ArrayList<ArrayList<ArrayList<String>>> nearbyIds, List<CellObject> concurrentObjects) {
		for(CellObject c:concurrentObjects){
			if(c instanceof Player){				
				if(oriented && ((Player) c).isOriented() | (!oriented && !((Player) c).isOriented())){
					Mapper temp = ((Player) c).getMapper();
					
					for(Point p:temp.enemyLocations){
						if(!mapper.enemyLocations.contains(p))
							mapper.enemyLocations.add(p);
					}
					for(Point p:temp.safeLocations){
						if(!mapper.enemyLocations.contains(p))
							mapper.enemyLocations.add(p);
					}
				}
			}
			
			else if(c instanceof Outpost){
				for(Point p:mapper.enemyLocations){
					((Outpost)c).addEnemyLocation(p);
				}
				for(Point q:mapper.safeLocations){
					System.out.println("q: " + q.x + " " + q.y);
					((Outpost)c).addSafeLocation(q);
				}
								
				Mapper data = (Mapper)((Outpost)c).getData();	
				for(Point e: data.enemyLocations){
					mapper.enemyLocations.add(e);
				}
				for(Point s: data.safeLocations){
					mapper.safeLocations.add(s);
				}
				
				((Outpost)c).setData(mapper);
			}
		}
		
	}

	public boolean isOriented() {
		return oriented;
	}
	
	public Mapper getMapper(){
		return mapper;
	}
	
	public Point moveEast(){
		pos.x++;
		return new Point(1,0);
	}
	
	public Point moveWest(){
		pos.x--;
		return new Point(-1,0);
	}
	
	public Point moveNorth(){
		pos.y--;
		return new Point(0,-1);
	}
	
	public Point moveSouth(){
		pos.y++;
		return new Point(0,1);
	}
	
	public Point moveNorthEast(){
		pos.x++;
		pos.y--;
		return new Point(1,-1);
	}
	
	public Point moveNorthWest(){
		pos.x--;
		pos.y--;
		return new Point(-1,-1);
	}
	
	public Point moveSouthEast(){
		pos.x++;
		pos.y++;
		return new Point(1,1);
	}
	
	public Point moveSouthWest(){
		pos.x--;
		pos.y++;
		return new Point(-1,1);
	}

}
