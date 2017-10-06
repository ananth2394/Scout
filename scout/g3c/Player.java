package scout.g3c;

import scout.sim.*;
import java.util.*;

public class Player extends scout.sim.Player {
    List<Point> enemyRelativeLocations;
    List<Point> safeRelativeLocations;

    List<Point> enemyLocations;
    List<Point> safeLocations;
    Random gen;
    int t,n;
    int x = -1;
    int y = -1;

    int westEnd;
    int eastEnd;
    int northEnd;
    int southEnd;

    int accX = 0;
    int accY = 0;
    int seed;
    boolean oriented;
    boolean newPhase;
    boolean rightDir;
    Point direction;
    boolean skipRow;

    List<Point> history;

    private class PlayerState {
        // track board info, move history, enemy and safe locations
        List<Point> enemyLocations;
        List<Point> safeLocations;
        List<Point> history;

        boolean oriented;
        String[][] board;
    }

    /**
    * better to use init instead of constructor, don't modify ID or simulator will error
    */
    public Player(int id) {
        super(id);
        seed=id;
    }

    /**
    *   Called at the start
    */
    @Override
    public void init(String id, int s, int n, int t, List<Point> landmarkLocations) {
        enemyRelativeLocations = new ArrayList<>();
        safeRelativeLocations = new ArrayList<>();

        enemyLocations = new ArrayList<>();
        safeLocations = new ArrayList<>();
        gen = new Random(seed);
        this.t = t;
        this.n = n;

        if(Integer.parseInt(this.getID().substring(1)) % 4 == 0) {
            // Move NW
            this.direction = new Point(-1, -1);

            westEnd = 1;
            eastEnd = n/2;
            northEnd = 0;
            southEnd = n/2 - 1;
        } else if(Integer.parseInt(this.getID().substring(1)) % 4 == 1) {
            // Move NE
            this.direction = new Point(-1, 1);

            westEnd = n/2 + 1;
            eastEnd = n;
            northEnd = 0;
            southEnd = n/2;
        } else if(Integer.parseInt(this.getID().substring(1)) % 4 == 2) {
            // Move SE
            this.direction = new Point(1, 1);

            westEnd = n/2 + 1;
            eastEnd = n;
            northEnd = n/2;
            southEnd = n;
        } else if(Integer.parseInt(this.getID().substring(1)) % 4 == 3) {
            // Move SW
            this.direction = new Point(1, -1);

            westEnd = 1;
            eastEnd = n/2;
            northEnd = n/2;
            southEnd = n;
        }
    }

    @Override
    public Point move(ArrayList<ArrayList<ArrayList<String>>> nearbyIds, List<CellObject> concurrentObjects) {
        //System.out.println("I'm " + this.getID() + " at " + x + " " + y);
        for(int i = 0 ; i < 3; ++ i) {
            for(int j = 0 ; j < 3 ; ++ j) {
                boolean safe = true;
                if(nearbyIds.get(i).get(j) == null) continue;
                for(String ID : nearbyIds.get(i).get(j)) {
                    if(ID.charAt(0) == 'E') {
                        safe = false;
                    }
                }
                if(x != -1) {
                    Point consideredLocation = new Point(x + i - 1, y + j - 1);
                    if(safe) {
                        if(!safeLocations.contains(consideredLocation)) {
                            safeLocations.add(consideredLocation);
                        }
                    } else {
                        if(!enemyLocations.contains(consideredLocation)) {
                            enemyLocations.add(consideredLocation);
                        }
                    }
                } else {
                    Point consideredLocation = new Point(accX + i - 1, accY + j - 1);
                    if(safe) {
                        if(!safeRelativeLocations.contains(consideredLocation)) {
                            safeRelativeLocations.add(consideredLocation);
                        }
                    } else {
                        if(!enemyRelativeLocations.contains(consideredLocation)) {
                            enemyRelativeLocations.add(consideredLocation);
                        }
                    }
                }
            }
        }

        for(CellObject obj : concurrentObjects) {
            if (obj instanceof Player) {
                //communicate using custom methods?
                ((Player) obj).stub();
            } else if (obj instanceof Enemy) {

            } else if (obj instanceof Landmark) {
                x = ((Landmark) obj).getLocation().x;
                y = ((Landmark) obj).getLocation().y;
                oriented = true;
            } else if (obj instanceof Outpost) {
                Object data = ((Outpost) obj).getData();
                if(Integer.parseInt(this.getID().substring(1)) % 4 == 0) {
                    // Move NW
                    x = 0;
                    y = 0;
                } else if(Integer.parseInt(this.getID().substring(1)) % 4 == 1) {
                    // Move NE
                    x = 0;
                    y = n+1;
                } else if(Integer.parseInt(this.getID().substring(1)) % 4 == 2) {
                    // Move SE
                    x = n+1;
                    y = n+1;
                } else if(Integer.parseInt(this.getID().substring(1)) % 4 == 3) {
                    // Move SW
                    x = n+1;
                    y = 0;
                }
                oriented = true;
                newPhase = true;
                rightDir = true;
                if(data == null) {
                    ((Outpost) obj).setData((Object)"yay!!");
                }
                for(Point safe : safeLocations) {
                    ((Outpost) obj).addSafeLocation(safe);
                }
                for(Point unsafe : enemyLocations) {
                    ((Outpost) obj).addEnemyLocation(unsafe);
                }
            }
        }

        
        if(!newPhase) {
            if (nearbyIds.get(0).get(1) == null) {
                //Up is null
                if(Integer.parseInt(this.getID().substring(1)) % 4 == 0) {
                    this.direction = new Point(0, -1);
                } else if(Integer.parseInt(this.getID().substring(1)) % 4 == 1) {
                    this.direction = new Point(0, 1);
                }
            } else if (nearbyIds.get(1).get(0) == null) {
                //Left is null
                if(Integer.parseInt(this.getID().substring(1)) % 4 == 0) {
                    this.direction = new Point(-1, 0);
                } else if(Integer.parseInt(this.getID().substring(1)) % 4 == 3) {
                    this.direction = new Point(1, 0);
                }
            } else if (nearbyIds.get(1).get(2) == null) {
                //Right is null
                if(Integer.parseInt(this.getID().substring(1)) % 4 == 1) {
                    this.direction = new Point(-1, 0);
                } else if(Integer.parseInt(this.getID().substring(1)) % 4 == 2) {
                    this.direction = new Point(1, 0);
                }
            } else if (nearbyIds.get(2).get(1) == null) {
                //Down is null
                if(Integer.parseInt(this.getID().substring(1)) % 4 == 2) {
                    this.direction = new Point(0, 1);
                } else if(Integer.parseInt(this.getID().substring(1)) % 4 == 3) {
                    this.direction = new Point(0, -1);
                }
            }
            accY += this.direction.y;
            accX += this.direction.x;
        } else {
            if(Integer.parseInt(this.getID().substring(1)) % 4 == 0) {
                if(skipRow) {
                    skipRow = false;
                    x++;
                    return new Point(1, 0);
                }
                if(rightDir) {
                    // go right
                    if(y >= eastEnd) {
                        //x = x + 2 <= southEnd ? x + 2 : southEnd;
                        if(x+2 >= southEnd) {
                            newPhase = !newPhase;
                        }
                        x++;
                        skipRow = true;
                        rightDir = !rightDir;
                        return new Point(1, 0);
                    } else {
                        y++;
                        return new Point(0, 1);
                    }
                } else {
                    // go left
                    if(y <= westEnd) {
                        //x = x - 2 >= southEnd ? x - 2 : southEnd;
                        if(x+2 >= southEnd) {
                            newPhase = !newPhase;
                        }
                        x++;
                        skipRow = true;
                        rightDir = !rightDir;
                        return new Point(1, 0);
                    } else {
                        y--;
                        return new Point(0, -1);
                    }
                }
            } else if(Integer.parseInt(this.getID().substring(1)) % 4 == 1) {
                if(skipRow) {
                    skipRow = false;
                    x++;
                    return new Point(1, 0);
                }
                if(rightDir) {
                    // go left
                    if(y <= westEnd) {
                        //x = x - 2 >= southEnd ? x - 2 : southEnd;
                        if(x+2 >= southEnd) {
                            newPhase = !newPhase;
                        }
                        x++;
                        skipRow = true;
                        rightDir = !rightDir;
                        return new Point(1, 0);
                    } else {
                        y--;
                        return new Point(0, -1);
                    }
                } else {
                    // go right
                    if(y >= eastEnd) {
                        //x = x - 2 >= southEnd ? x - 2 : southEnd;
                        if(x+2 <= northEnd) {
                            newPhase = !newPhase;
                        }
                        x++;
                        skipRow = true;
                        rightDir = !rightDir;
                        return new Point(1, 0);
                    } else {
                        y++;
                        return new Point(0, 1);
                    }
                }
            } else if(Integer.parseInt(this.getID().substring(1)) % 4 == 2) {
                if(skipRow) {
                    skipRow = false;
                    x--;
                    return new Point(-1, 0);
                }
                if(rightDir) {
                    // go left
                    if(y <= westEnd) {
                        //x = x - 2 >= southEnd ? x - 2 : southEnd;
                        if(x-2 <= northEnd) {
                            newPhase = !newPhase;
                        }
                        x--;
                        skipRow = true;

                        rightDir = !rightDir;
                        return new Point(-1, 0);
                    } else {
                        y--;
                        return new Point(0, -1);
                    }
                } else {
                    // go right
                    if(y >= eastEnd) {
                        //x = x - 2 >= southEnd ? x - 2 : southEnd;
                        if(x-2 <= northEnd) {
                            newPhase = !newPhase;
                        }
                        x--;
                        skipRow = true;
                        rightDir = !rightDir;
                        return new Point(-1, 0);
                    } else {
                        y++;
                        return new Point(0, 1);
                    }
                }
            } else if(Integer.parseInt(this.getID().substring(1)) % 4 == 3) {
                if(skipRow) {
                    skipRow = false;
                    x--;
                    return new Point(-1, 0);
                }
                if(rightDir) {
                    // go right
                    if(y >= eastEnd) {
                        //x = x - 2 >= southEnd ? x - 2 : southEnd;
                        if(x-2 <= northEnd) {
                            newPhase = !newPhase;
                        }
                        x--;
                        skipRow = true;

                        rightDir = !rightDir;
                        return new Point(-1, 0);
                    } else {
                        y++;
                        return new Point(0, 1);
                    }
                } else {
                    // go left
                    if(y <= westEnd) {
                        //x = x - 2 >= southEnd ? x - 2 : southEnd;
                        if(x-2 <= northEnd) {
                            newPhase = !newPhase;
                        }
                        x--;
                        skipRow = true;
                        rightDir = !rightDir;
                        return new Point(-1, 0);
                    } else {
                        y--;
                        return new Point(0, -1);
                    }
                }
            }
        }
        //return x \in {-1,0,1}, y \in {-1,0,1}
        return this.direction;
    }


    public Point NWmove() {
        if(skipRow) {
            skipRow = false;
            x++;
            return new Point(1, 0);
        }
        if(rightDir) {
            // go right
            if(y >= eastEnd) {
                //x = x + 2 <= southEnd ? x + 2 : southEnd;
                if(x+2 >= southEnd) {
                    newPhase = !newPhase;
                }
                x++;
                skipRow = true;
                rightDir = !rightDir;
                return new Point(1, 0);
            } else {
                y++;
                return new Point(0, 1);
            }
        } else {
            // go left
            if(y <= westEnd) {
                //x = x - 2 >= southEnd ? x - 2 : southEnd;
                if(x+2 >= southEnd) {
                    newPhase = !newPhase;
                }
                x++;
                skipRow = true;
                rightDir = !rightDir;
                return new Point(1, 0);
            } else {
                y--;
                return new Point(0, -1);
            }
        }
    }

    public Point NEmove() {
        if(skipRow) {
            skipRow = false;
            x++;
            return new Point(1, 0);
        }
        if(rightDir) {
            // go left
            if(y <= westEnd) {
                //x = x - 2 >= southEnd ? x - 2 : southEnd;
                if(x+2 >= southEnd) {
                    newPhase = !newPhase;
                }
                x++;
                skipRow = true;
                rightDir = !rightDir;
                return new Point(1, 0);
            } else {
                y--;
                return new Point(0, -1);
            }
        } else {
            // go right
            if(y >= eastEnd) {
                //x = x - 2 >= southEnd ? x - 2 : southEnd;
                if(x+2 <= northEnd) {
                    newPhase = !newPhase;
                }
                x++;
                skipRow = true;
                rightDir = !rightDir;
                return new Point(1, 0);
            } else {
                y++;
                return new Point(0, 1);
            }
        }
    }

    public Point SEmove() {
        if(skipRow) {
            skipRow = false;
            x--;
            return new Point(-1, 0);
        }
        if(rightDir) {
            // go left
            if(y <= westEnd) {
                //x = x - 2 >= southEnd ? x - 2 : southEnd;
                if(x-2 <= northEnd) {
                    newPhase = !newPhase;
                }
                x--;
                skipRow = true;

                rightDir = !rightDir;
                return new Point(-1, 0);
            } else {
                y--;
                return new Point(0, -1);
            }
        } else {
            // go right
            if(y >= eastEnd) {
                //x = x - 2 >= southEnd ? x - 2 : southEnd;
                if(x-2 <= northEnd) {
                    newPhase = !newPhase;
                }
                x--;
                skipRow = true;
                rightDir = !rightDir;
                return new Point(-1, 0);
            } else {
                y++;
                return new Point(0, 1);
            }
        }
    }

    public Point SWmove() {
        if(skipRow) {
            skipRow = false;
            x--;
            return new Point(-1, 0);
        }
        if(rightDir) {
            // go right
            if(y >= eastEnd) {
                //x = x - 2 >= southEnd ? x - 2 : southEnd;
                if(x-2 <= northEnd) {
                    newPhase = !newPhase;
                }
                x--;
                skipRow = true;

                rightDir = !rightDir;
                return new Point(-1, 0);
            } else {
                y++;
                return new Point(0, 1);
            }
        } else {
            // go left
            if(y <= westEnd) {
                //x = x - 2 >= southEnd ? x - 2 : southEnd;
                if(x-2 <= northEnd) {
                    newPhase = !newPhase;
                }
                x--;
                skipRow = true;
                rightDir = !rightDir;
                return new Point(-1, 0);
            } else {
                y--;
                return new Point(0, -1);
            }
        }
    }

    private void convertRelativeToAbsolute() {
        //TODO
    }

    public void stub() {
        ;
    }

    @Override
    public void communicate(ArrayList<ArrayList<ArrayList<String>>> nearbyIds, List<CellObject> concurrentObjects) {
        --t;
    }
}

