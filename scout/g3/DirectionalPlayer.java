package scout.g3;

import scout.sim.*;
import java.util.*;

public abstract class DirectionalPlayer extends scout.sim.Player {
    List<Point> enemyRelativeLocations;
    List<Point> safeRelativeLocations;

    List<Point> enemyLocations;
    List<Point> safeLocations;

    List<Point> outpostLocations;
    int t,n,s,id;

    int x;
    int y;

    int outpostCount;
    int westEnd;
    int eastEnd;
    int northEnd;
    int southEnd;

    int curOutpost;

    int accX;
    int accY;
    boolean oriented;
    boolean orientedX;
    boolean orientedY;

    Point position;
    Point middle;
    Point startPos;

    int startIndex;
    int endIndex;

    boolean[] shared;

    List<Point> moves;
    List<Point> locations;

    PlayerPhase phase;

    public enum PlayerPhase {
        JoinOutpost, FindSection, Explore, MeetCenter, ReturnOutpost
    }

    public DirectionalPlayer(int id) {
        super(id);
    }

    @Override
    public void init(String id, int s, int n, int t, List<Point> landmarkLocations) {
        enemyRelativeLocations = new ArrayList<>();
        safeRelativeLocations = new ArrayList<>();

        enemyLocations = new ArrayList<>();
        safeLocations = new ArrayList<>();

        moves = new ArrayList<>();
        locations = new ArrayList<>();

        this.t = t;
        this.n = n;
        this.s = s;

        shared = new boolean[s];
        middle = new Point((n+1)/2, (n+1)/2);
        phase = PlayerPhase.JoinOutpost;
        position = new Point(0, 0);

        initDirections();
    }

    @Override
    public Point move(ArrayList<ArrayList<ArrayList<String>>> nearbyIds, List<CellObject> concurrentObjects) {
        for(int i = 0 ; i < 3; ++ i) {
            for(int j = 0 ; j < 3 ; ++ j) {
                boolean safe = true;
                if(nearbyIds.get(i).get(j) == null) {
                    if(i == 0 && j == 1) {
                        orientedX = true;
                        x = 0;
                    }
                    if(i == 2 && j == 1) {
                        orientedX = true;
                        x = n+1;
                    }
                    if(i == 1 && j == 0) {
                        orientedY = true;
                        y = 0;
                    }
                    if(i == 1 && j == 2) {
                        orientedY = true;
                        y = n+1;
                    }
                    continue;
                } 
                for(String ID : nearbyIds.get(i).get(j)) {
                    if(ID.charAt(0) == 'E') {
                        safe = false;
                    }
                }
                if(oriented) {
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

        switch(phase) {
            case JoinOutpost:
                joinOutpost();
                break;
            case FindSection:
                findSection();
                break;
            case Explore:
                explore();
                break;
            case MeetCenter:
                meetCenter();
                break;
            case ReturnOutpost:
                returnOutpost();
                break;
            default:
                explore();
        }

        return this.position;
    }

    public abstract void setOutpost();

    public abstract void defaultMove();

    public abstract void initDirections();

    public void shareInfo(List<Point> sharedSafeLocations, List<Point> sharedEnemyLocations) {
        for(Point ps : sharedSafeLocations) {
            if(!safeLocations.contains(ps)) {
                safeLocations.add(ps);
            }
        }

        for(Point pe : sharedEnemyLocations) {
            if(!enemyLocations.contains(pe)) {
                enemyLocations.add(pe);
            }
        }
    }

    public void moveTo(int newX, int newY, PlayerPhase nextPhase) {
        if(!oriented) {
            defaultMove();
        } else {
            int nextX = 0;
            int nextY = 0;
            if(newY - y > 0) {
                nextY = 1;
            }
            if(newY - y < 0) {
                nextY = -1;
            }
            if(newX - x > 0) {
                nextX = 1;
            }
            if(newX - x < 0) {
                nextX = -1;
            }
            if(nextX != 0 || nextY != 0) {
                setPosition(nextX, nextY);
            } else {
                setPosition(0, 0);
                phase = nextPhase;
            }
        }
    }


    @Override
    public void communicate(ArrayList<ArrayList<ArrayList<String>>> nearbyIds, List<CellObject> concurrentObjects) {
        int playerCount = 0;
        for(CellObject obj : concurrentObjects) {
            if (obj instanceof Player) {
                playerCount++;
                if(!shared[((Player) obj).id]) {
                    ((Player) obj).shareInfo(safeLocations, enemyLocations);
                    shared[((Player) obj).id] = true;
                }
                if(phase == PlayerPhase.MeetCenter && playerCount == s) {
                    phase = PlayerPhase.ReturnOutpost;
                }
            } else if (obj instanceof Enemy) {

            } else if (obj instanceof Landmark) {
                x = ((Landmark) obj).getLocation().x;
                y = ((Landmark) obj).getLocation().y;
                oriented = true;
                orientedX = true;
                orientedY = true;
            } else if (obj instanceof Outpost) {
                outpostCount++;
                if(outpostCount > 1 && x == outpostLocations.get(curOutpost).x && y == outpostLocations.get(curOutpost).y) {
                    curOutpost = (curOutpost + 1) % 4;
                }
                if(outpostCount == 2) {
                } else if(outpostCount == 1) {
                    oriented = true;
                    orientedX = true;
                    orientedY = true;
                    // Move NW
                    setOutpost();
                }
                Object data = ((Outpost) obj).getData();
                
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
        --t;
    }

    @Override
    public void moveFinished() {
        x += this.position.x;
        y += this.position.y;
    }

    public void findSection() {
        moveTo(startPos.x, startPos.y, PlayerPhase.Explore);
    }


    public void meetCenter() {
        moveTo(middle, PlayerPhase.MeetCenter);
    }

    public void returnOutpost() {
        moveTo(outpostLocations.get(0).x, outpostLocations.get(0).y, PlayerPhase.ReturnOutpost);
    }


    public void joinOutpost() {
        moveTo(outpostLocations.get(0).x, outpostLocations.get(0).y, PlayerPhase.FindSection);
    }

    public void explore() {
        if(startIndex > endIndex || t <= 6*n) {
            setPosition(0,0);
            phase = PlayerPhase.MeetCenter;
        } else {
            Point nextP = moves.get(startIndex++);
            setPosition(nextP.x, nextP.y);
        }
    }

    private void moveTo(Point p, PlayerPhase nextPhase) {
        moveTo(p.x, p.y, nextPhase);
    }

    public void setPosition(int x, int y) {
        this.position.x = x;
        this.position.y = y;
    }
}