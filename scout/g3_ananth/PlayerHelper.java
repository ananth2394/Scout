package scout.g3_ananth;

import scout.sim.*;
import scout.g3_ananth.*;
import java.util.*;

public class PlayerHelper {
	private int n;
	private int absolute_x = -1;
	private int absolute_y = -1;
	private boolean isCalibrated = false;
	private Point lastMove = new Point(0,0);

	private int relative_x = 0;
	private int relative_y = 0;

	public List<Point> relativeEL; // enemy loc
	public List<Point> relativeSL; // safe loc
	public AbsoluteGrid grid;
	public ExplorerGrid explorer_grid;

	public PlayerHelper(int n) {
		this.n = n;
		this.relativeEL = new ArrayList<Point>();
		this.relativeSL = new ArrayList<Point>();
		this.grid = new AbsoluteGrid(n);
		this.explorer_grid = new ExplorerGrid(n);
	}

	public int getRelativeX(){
		return this.relative_x;
	}

	public int getRelativeY(){
		return this.relative_y;
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

	// move should only be called for the valid move!
	public Point move(String s) {
		Point p = new Point(0,0);
		switch (s) {
			case "N": {
				p = new Point(-1,0);
				break;
			}

			case "S": {
				p = new Point(+1,0);
				break;
			}

			case "W": {
				p = new Point(0,-1);
				break;
			}

			case "E": {
				p = new Point(0,+1);
				break;
			}

			case "NE": {
				p = new Point(-1,+1);
				break;
			}

			case "SE": {
				p = new Point(+1,+1);
				break;
			}

			case "SW": {
				p = new Point(+1,-1);
				break;
			}

			case "NW": {
				p = new Point(-1,-1);
				break;
			}

			case "C": {
				p = new Point(0,0);
				break;
			}
		}
		lastMove = p;
		return p;
	}

	public void updateCurrentLocation() {
		if (this.isCalibrated) {
			this.absolute_x += this.lastMove.x;
			this.absolute_y += this.lastMove.y;
		} else {
			if (this.absolute_x != -1) {
				this.absolute_x += this.lastMove.x;
			} else if (this.absolute_y != -1) {
				this.absolute_y += this.lastMove.y;
			}
			this.relative_x += this.lastMove.x;
			this.relative_y += this.lastMove.y;
		}

		if(this.isCalibrated)
		{
			Point loc = this.getAbsoluteLocation();
			if(loc.x == -1 || loc.y==-1)
			{
				System.out.println("WHAT THE HELL!");
			}
			this.explorer_grid.setExplored(loc,true);
		}

	}

	public boolean hitBoundary() {
		// assume its calibrated
		return (this.absolute_x == 0 || this.absolute_y == 0 || this.absolute_x == n+1 || this.absolute_y == n+1);
	}

	public boolean hitOutpost() {
		// assume its calibrated
		return ((this.absolute_x == 0 && this.absolute_y == 0) ||
				(this.absolute_x == n+1 && this.absolute_y == 0) ||
				(this.absolute_x == 0 && this.absolute_y == n+1) ||
				(this.absolute_x == n+1 && this.absolute_y == n+1));
	}

	public boolean isCalibrated() {
		return this.isCalibrated;
	}

	public void updatePlayerHelper(ArrayList<ArrayList<ArrayList<String>>> nearbyIds, List<CellObject> concurrentObjects) {
		// This function will:
		// 1. try to calibrate based on wall (including outpost, which you are hitting two walls) / landmark
		// 2. exchange information with outpost and players
		// 3. update nearby locations

		// Updating safe/enemy locations for either relative or absolute
        for(int i = 0 ; i < 3; ++ i) {
            for(int j = 0 ; j < 3 ; ++ j) {
                if (nearbyIds.get(i).get(j) == null) {
                	if (i == 0 && j == 1) { // meaning when you try to go up, you hit a wall
                		this.absolute_x = 0;
                	}
                	if (i == 2 && j == 1) {
                		this.absolute_x = n+1;
                	}
                	if (j == 0 && i == 1) {
                		this.absolute_y = 0;
                	}
                	if (j == 2 && i == 1) {
                		this.absolute_y = n+1;
                	}
                	this.tryCalibration();
                }
            }
        }

        for(CellObject obj : concurrentObjects) {
            if (obj instanceof Landmark) {
            	this.calibrateBy(((Landmark) obj).getLocation());
            } else if (obj instanceof Outpost) {
                Object data = ((Outpost) obj).getData();
                AbsoluteGrid copyGrid = new AbsoluteGrid(this.grid);
                if(data == null) {
                  ((Outpost) obj).setData(copyGrid);
                }
                else {
                	//((AbsoluteGrid)data).printGrid();
                	AbsoluteGrid.merge((AbsoluteGrid)data, this.grid);
                }
                for (Point safe : this.grid.getSafe()) {
                    ((Outpost) obj).addSafeLocation(safe);
                }
                for (Point enemy : this.grid.getEnemy()) {
                    ((Outpost) obj).addEnemyLocation(enemy);
                }
            }
        }

        // Record nearby location info (either to relative list, or absolute grid)
        for(int i = 0 ; i < 3; ++ i) {
            for(int j = 0 ; j < 3 ; ++ j) {
            	if (nearbyIds.get(i).get(j) == null) continue;
                boolean safe = true;
        		for(String ID : nearbyIds.get(i).get(j)) {
                	// record at which location (using "W", "N", etc.) who we see (enemy/landmark, etc. as well as ID)
                    if(ID.charAt(0) == 'E') {
                        safe = false;
                    }
                }
                if(!this.isCalibrated) {
                    Point consideredLocation = new Point(this.relative_x + i - 1, this.relative_y + j - 1);
                    if(safe) {
                        if(!this.relativeSL.contains(consideredLocation)) {
                            this.relativeSL.add(consideredLocation);
                        }
                    } else {
                        if(!this.relativeEL.contains(consideredLocation)) {
                            this.relativeEL.add(consideredLocation);
                        }
                    }
                }
                else {
                	Point consideredLocation = new Point(this.absolute_x + i - 1, this.absolute_y + j - 1);
                	if(safe) {
                		this.grid.addSafe(consideredLocation);
                	} else {
                		this.grid.addEnemy(consideredLocation);
                	}
                }
        	}
        }

        // Now try to gather all information from Players
        int gathered_absolute_X = this.absolute_x;
        int gathered_absolute_Y = this.absolute_y;
        for(CellObject obj : concurrentObjects) {
	        if (obj instanceof Player) {
	        	int cur_absolute_X = ((Player)obj).ph.getAbsoluteX();
	        	int cur_absolute_Y = ((Player)obj).ph.getAbsoluteY();
	        	if (cur_absolute_X != -1) {
	        		if (gathered_absolute_X != -1 && cur_absolute_X != gathered_absolute_X) {
	        			System.out.println("Wrong absolute X! One says it's " + cur_absolute_X + "One says it's " + gathered_absolute_X);
	        		}
	        		gathered_absolute_X = cur_absolute_X;
	        	}

	        	if (cur_absolute_Y != -1) {
	        		if (gathered_absolute_Y != -1 && cur_absolute_Y != gathered_absolute_Y) {
	        			System.out.println("Wrong absolute Y! One says it's " + cur_absolute_Y + "One says it's " + gathered_absolute_Y);
	        		}
	        		gathered_absolute_Y = cur_absolute_Y;
	        	}

	        }
	    }

	    this.setAbsoluteX(gathered_absolute_X);
	    this.setAbsoluteY(gathered_absolute_Y);

	    this.tryCalibration();
	    // for(CellObject obj : concurrentObjects) {
	    // 	if (obj instanceof Player) {
	    // 		((Player)obj).ph.setAbsoluteX(gathered_absolute_X);
	    // 		((Player)obj).ph.setAbsoluteY(gathered_absolute_Y);
	    // 		((Player)obj).ph.tryCalibration();
	    // 	}
	    // }



	    // At this moment, either all players have calibrated or none of them have calibrated
	    // if all calibrated, we can merge
	    // if none, we share relative information

	    if (this.isCalibrated) {
	    	AbsoluteGrid mergedGrid = this.grid;
				ExplorerGrid mergedGridE  = this.explorer_grid;
	    	// we create a mergedGrid, merge with everyone
	    	// then set each one's grid to mergedGrid
	    	for(CellObject obj : concurrentObjects) {
	    		if (obj instanceof Player) {
		    		AbsoluteGrid.merge(mergedGrid, ((Player)obj).ph.grid);
						ExplorerGrid.merge(mergedGridE, ((Player)obj).ph.explorer_grid);
						ExplorerGrid.mergeE(mergedGridE, ((Player)obj).ph.explorer_grid);
	    		}
	    	}
	    	this.grid.setGrid(mergedGrid.getGrid());
				this.explorer_grid.setGrid(mergedGridE.getGrid());
				this.explorer_grid.setGridE(mergedGridE.getGridE());

	    	// for(CellObject obj : concurrentObjects) {
	    	// 	if (obj instanceof Player) {
	    	// 		((Player)obj).ph.grid.setGrid(mergedGrid.getGrid());
				// 		((Player)obj).ph.explorer_grid.setGrid(mergedGridE.getGrid());
				// 		((Player)obj).ph.explorer_grid.setGridE(mergedGridE.getGridE());
				//
	    	// 	}
	    	// }
	    }
	    else {
	    	int pseudoAbsoluteX = 0;
	    	int pseudoAbsoluteY = 0;
	    	int delta_x = pseudoAbsoluteX - relative_x;
			int delta_y = pseudoAbsoluteY - relative_y;
			List<Point> allSafeLoc = new ArrayList<Point>();
			List<Point> allEnemyLoc = new ArrayList<Point>();

			for (Point p: this.relativeEL) {
				Point consideredLocation = new Point(p.x + delta_x, p.y + delta_y);
				if(!allEnemyLoc.contains(consideredLocation)) {
                    allEnemyLoc.add(consideredLocation);
                }
			}

			for (Point p: this.relativeSL) {
				Point consideredLocation = new Point(p.x + delta_x, p.y + delta_y);
				if(!allSafeLoc.contains(consideredLocation)) {
                    allSafeLoc.add(consideredLocation);
                }
			}

			for(CellObject obj : concurrentObjects) {
	    		if (obj instanceof Player) {
	    			delta_x = pseudoAbsoluteX - ((Player)obj).ph.getRelativeX();
	    			delta_y = pseudoAbsoluteY - ((Player)obj).ph.getRelativeY();
					for (Point p: ((Player)obj).ph.relativeEL) {
						Point consideredLocation = new Point(p.x + delta_x, p.y + delta_y);
						if(!allEnemyLoc.contains(consideredLocation)) {
		                    allEnemyLoc.add(consideredLocation);
		                }
					}

					for (Point p: ((Player)obj).ph.relativeSL) {
						Point consideredLocation = new Point(p.x + delta_x, p.y + delta_y);
						if(!allSafeLoc.contains(consideredLocation)) {
		                    allSafeLoc.add(consideredLocation);
		                }
					}
	    		}
	    	}

	    	// Now allSafeLoc and allEnemyLoc have all the relative info
	    	// We update them back

	    	// update this player helper
	    	delta_x = relative_x - pseudoAbsoluteX;
	    	delta_y = relative_y - pseudoAbsoluteY;
			for (Point p: allEnemyLoc) {
				Point consideredLocation = new Point(p.x + delta_x, p.y + delta_y);
				if(!this.relativeEL.contains(consideredLocation)) {
                    this.relativeEL.add(consideredLocation);
                }
			}

			for (Point p: allSafeLoc) {
				Point consideredLocation = new Point(p.x + delta_x, p.y + delta_y);
				if(!this.relativeSL.contains(consideredLocation)) {
                    this.relativeSL.add(consideredLocation);
                }
			}

			// update concurrent players
			// for(CellObject obj : concurrentObjects) {
	    // 		if (obj instanceof Player) {
	    // 			delta_x = ((Player)obj).ph.getRelativeX() - pseudoAbsoluteX;
	    // 			delta_y = ((Player)obj).ph.getRelativeY() - pseudoAbsoluteY;
			// 		for (Point p: allEnemyLoc) {
			// 			Point consideredLocation = new Point(p.x + delta_x, p.y + delta_y);
			// 			if(!((Player)obj).ph.relativeEL.contains(consideredLocation)) {
		  //                   ((Player)obj).ph.relativeEL.add(consideredLocation);
		  //               }
			// 		}
			//
			// 		for (Point p: allSafeLoc) {
			// 			Point consideredLocation = new Point(p.x + delta_x, p.y + delta_y);
			// 			if(!((Player)obj).ph.relativeSL.contains(consideredLocation)) {
		  //                   ((Player)obj).ph.relativeSL.add(consideredLocation);
		  //               }
			// 		}
	    // 		}
	    // 	}

	    }
			// this.grid.printGrid();
			// this.explorer_grid.printGrid();
			// this.explorer_grid.printGridE();
    }

    private List<List<String>> getNearby(ArrayList<ArrayList<ArrayList<String>>> nearbyIds, String keyword) {
    	List<List<String>> result = new ArrayList<List<String>>();
    	for(int i = 0 ; i < 3; ++ i) {
            for(int j = 0 ; j < 3 ; ++ j) {
				if (nearbyIds.get(i).get(j) == null) continue;
        		for(String ID : nearbyIds.get(i).get(j)) {
                	// record at which location (using "W", "N", etc.) who we see (enemy/landmark, etc. as well as ID)
                    if(ID.charAt(0) == keyword.charAt(0)) {
                    	List<String> tmp = new ArrayList<String>();
                    	tmp.add(ID);
                    	tmp.add(ijToDirection(i,j));
                        result.add(tmp);
                    }
                }
            }
        }
        return result;
    }

    public List<String> getAllValidMoves(ArrayList<ArrayList<ArrayList<String>>> nearbyIds) {
    	List<String> result = new ArrayList<String>();
    	for(int i = 0 ; i < 3; ++ i) {
            for(int j = 0 ; j < 3 ; ++ j) {
        		if (nearbyIds.get(i).get(j) != null) {
        			result.add(ijToDirection(i,j));
        		}
            }
        }
        return result;
    }

		public List<String> getBestMoves(ArrayList<ArrayList<ArrayList<String>>> nearbyIds)
		{
			List<String> result = new ArrayList<String>();
			int mn = 20;
    	for(int i = 0 ; i < 3; ++ i) {
            for(int j = 0 ; j < 3 ; ++ j) {
        		if (nearbyIds.get(i).get(j) != null) {
        			//result.add(ijToDirection(i,j));
							mn = Math.min(this.explorer_grid.getLocation(new Point(i,j)),mn);
        		}
            }
        }

				for(int i = 0 ; i < 3; ++ i) {
	            for(int j = 0 ; j < 3 ; ++ j) {
	        		if (nearbyIds.get(i).get(j) != null) {
	        			//result.add(ijToDirection(i,j));
								int val = this.explorer_grid.getLocation(new Point(i,j));
								if(val==mn)
								{
									result.add(ijToDirection(i,j));
								}
	        		}
	            }
	        }

        return result;
		}

		public String getRandomBestMove(ArrayList<ArrayList<ArrayList<String>>> nearbyIds)
		{
			List<String> result = getBestMoves(nearbyIds);
			Random random_index = new Random();
			int sz = (int)result.size();
			int ri = random_index.nextInt(sz);
			return result.get(ri);
		}
    public List<List<String>> getNearbyLandmarks(ArrayList<ArrayList<ArrayList<String>>> nearbyIds) {
    	return getNearby(nearbyIds,"L");
    }

    public List<List<String>> getNearbyEnemies(ArrayList<ArrayList<ArrayList<String>>> nearbyIds) {
    	return getNearby(nearbyIds,"E");
    }

    public List<List<String>> getNearbyPlayers(ArrayList<ArrayList<ArrayList<String>>> nearbyIds) {
    	return getNearby(nearbyIds,"P");
    }

    public List<List<String>> getNearbyOutposts(ArrayList<ArrayList<ArrayList<String>>> nearbyIds) {
    	return getNearby(nearbyIds,"O");
    }

    private void calibrateBy(Point p) {
    	if (!this.isCalibrated) {
        	this.absolute_x = p.x;
        	this.absolute_y = p.y;
        	this.tryCalibration();
        }
        else {
        	if (this.absolute_x != p.x ||
        		this.absolute_y != p.y) {
        		System.out.println("Absolute coordinates are off!" +
        			"Correct X: " + p.x +
        			"Correct Y: " + p.y +
        			"Your Absolute X: " + this.absolute_x +
        			"Your Absolute Y: " + this.absolute_y
        		);
        	}
        }
    }

	public int getAbsoluteX(){
		return this.absolute_x;
	}

	public int getAbsoluteY(){
		return this.absolute_y;
	}

	public Point getAbsoluteLocation(){
		Point p =  new Point(this.absolute_x,this.absolute_y);
		return p;
	}

	public void setAbsoluteX(int absolute_x){
		this.absolute_x = absolute_x;
	}

	public void setAbsoluteY(int absolute_y){
		this.absolute_y = absolute_y;
	}

	public void tryCalibration() {
		// when calibration is called
		// absolute_x and absolute_y should not be -1
		if (this.isCalibrated) {
			return;
		}
		if (this.absolute_x == -1 || this.absolute_y == -1) {
			return;
		}

		int delta_x = absolute_x - relative_x;
		int delta_y = absolute_y - relative_y;
		for (Point p: this.relativeEL) {
			this.grid.addEnemy(new Point(p.x + delta_x, p.y + delta_y));
		}
		for (Point p: this.relativeSL) {
			this.grid.addSafe(new Point(p.x + delta_x, p.y + delta_y));
		}

		// from now on we only update absolute coordinates
		this.relative_x = -1;
		this.relative_y = -1;
		this.isCalibrated = true;
	}

}
