package scout.g3;

import scout.sim.*;
import java.util.*;

public class SWplayer extends DirectionalPlayer {
    List<Point> enemyRelativeLocations;
    List<Point> safeRelativeLocations;

    List<Point> enemyLocations;
    List<Point> safeLocations;

    List<Point> outpostLocations;
    int t,n,s,id;

    int x = -1;
    int y = -1;

    int outpost1Players;
    int outpost2Players;
    int outpost3Players;
    int outpost4Players;

    int outpostCount;
    int westEnd;
    int eastEnd;
    int northEnd;
    int southEnd;

    int curOutpost;

    int accX = 0;
    int accY = 0;
    boolean oriented;
    boolean orientedX;
    boolean orientedY;
    boolean rightDir;

    Point position;
    Point middle;


    Point startPos;

    int startIndex;
    int endIndex;

    boolean[] shared;

    ArrayList<Point> moves;
    ArrayList<Point> indMoves;
    List<Point> locations;

    PlayerPhase phase;

    public enum PlayerPhase {
        JoinOutpost, FindSection, Explore, MeetCenter, ReturnOutpost
    }

    public SWplayer(int id) {
        super(id);
        this.id = id;
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

        moves = new ArrayList<>();
        this.t = t;
        this.n = n;
        this.s = s;

        shared = new boolean[s];

        int minPlayers = s / 4;
        int extraPlayers = s % 4;

        middle = new Point((n+1)/2, (n+1)/2);

        this.position = new Point(0, 0);
        outpostLocations = new ArrayList<>();
        outpostLocations.add(new Point(n+1, 0));
        outpostLocations.add(new Point(0, 0));
        outpostLocations.add(new Point(0, n+1));
        outpostLocations.add(new Point(n+1, n+1));



        outpost1Players = minPlayers + (extraPlayers > 0 ? 1 : 0);
        westEnd = 1;
        eastEnd = n/2;
        northEnd = n/2+1;
        southEnd = n;

        phase = PlayerPhase.JoinOutpost;

        SWpath();
        SWsubdivide();
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
                    x = n+1;
                    y = 0;
                }
                Object data = ((Outpost) obj).getData();
                
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


    @Override
    public void communicate(ArrayList<ArrayList<ArrayList<String>>> nearbyIds, List<CellObject> concurrentObjects) {
        --t;
    }

    @Override
    public void moveFinished() {
        x += this.position.x;
        y += this.position.y;
    }


    private void findSection() {
        moveTo(startPos.x, startPos.y, PlayerPhase.Explore);
    }


    private void meetCenter() {
        // Do nothing for now
        moveTo(middle, PlayerPhase.MeetCenter);
    }

    private void returnOutpost() {
        moveTo(outpostLocations.get(0).x, outpostLocations.get(0).y, PlayerPhase.ReturnOutpost);
    }


    private void joinOutpost() {
        moveTo(outpostLocations.get(0).x, outpostLocations.get(0).y, PlayerPhase.FindSection);
    }

    private void explore() {
        if(startIndex > endIndex) {
            setPosition(0,0);
            phase = PlayerPhase.MeetCenter;
        } else {
            Point nextP = moves.get(startIndex++);
            setPosition(nextP.x, nextP.y);
        }
        /*
        if(rightDir) {
            // go right
            if(y >= eastEnd) {
                //x = x - 2 >= southEnd ? x - 2 : southEnd;
                if(x-2 <= northEnd) {
                    phase = PlayerPhase.MeetCenter;
                }
                moves.add(new Point(-1, 0));
                moves.add(new Point(-1, 0));
                rightDir = !rightDir;
                setPosition(-1, 0);
            } else {
                setPosition(0, 1);
            }
        } else {
            // go left
            if(y <= westEnd) {
                //x = x - 2 >= southEnd ? x - 2 : southEnd;
                if(x-2 <= northEnd) {
                    phase = PlayerPhase.MeetCenter;
                }
                moves.add(new Point(-1, 0));
                moves.add(new Point(-1, 0));
                rightDir = !rightDir;
                setPosition(-1, 0);
            } else {
                setPosition(0, -1);
            }
        }
        */
    }

    private void moveTo(Point p, PlayerPhase nextPhase) {
        if(!oriented) {
            if(orientedX) {
                setPosition(0, -1);
            } else if(orientedY) {
                setPosition(1, 0);
            } else {
                setPosition(1, -1);
            }
        } else {
            int nextX = 0;
            int nextY = 0;
            if(p.y - y > 0) {
                nextY = 1;
            }
            if(p.y - y < 0) {
                nextY = -1;
            }
            if(p.x - x > 0) {
                nextX = 1;
            }
            if(p.x - x < 0) {
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

    private void moveTo(int newX, int newY, PlayerPhase nextPhase) {
        if(!oriented) {
            if(orientedX) {
                setPosition(0, -1);
            } else if(orientedY) {
                setPosition(1, 0);
            } else {
                setPosition(1, -1);
            }
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

    private void SWsubdivide() {
        int totalPathSize = moves.size();
        int totalNum = s/4 + (s % 4 > 3 ? 1 : 0);
        int pathLen = totalPathSize/totalNum;
        System.out.println(pathLen);

        int relId = id/4;

        int remPath = totalPathSize % totalNum;

        if(relId + 1 <= remPath) {
            pathLen++;
        }


        startIndex = relId*pathLen;
        endIndex = (relId + 1)*pathLen - 1;

        System.out.println("Start: " + startIndex);
        System.out.println("end: " + endIndex);
        startPos = locations.get(startIndex);

        /*
        int totalNum = s/4 + (s % 4 > 3 ? 1 : 0);
        System.out.println(totalNum);
        int squares = (int) Math.pow(4, Math.floor(Math.log(totalNum)/Math.log(4)));
        System.out.println(squares);

        int extraDiv = totalNum - squares;
        int squareNum = (id - 3)/4 % squares;
        System.out.println(squareNum);

        int squareWidth = (int) Math.ceil(n/(2.0*Math.sqrt(squares)));
        int squareHeight = (int) Math.ceil(n/(2.0*Math.sqrt(squares)));
        System.out.println("Width: " + squareWidth);
        System.out.println("Height: " + squareHeight);

        int horizontalOffset = (int) (squareNum % Math.sqrt(squares)) * squareWidth;
        int verticalOffset = (int) (squareNum / Math.sqrt(squares)) * squareHeight;

        System.out.println("h offset: " + horizontalOffset);
        System.out.println("v offset: " + verticalOffset);

        westEnd = 1 + horizontalOffset;
        System.out.println("West: " + westEnd);

        eastEnd = westEnd + squareWidth - 1;
        System.out.println("East: " + eastEnd);

        northEnd = n/2 + verticalOffset;
        System.out.println("North: " + northEnd);

        southEnd = northEnd + squareHeight - 1;
        System.out.println("South: " + southEnd);
        */
    }


    private void SWpath() {
        List<Point> path = new LinkedList<>();
        locations = new ArrayList<>();

        int topX = southEnd;
        int topY = eastEnd;
        boolean down = false;
        locations.add(new Point(topX, topY));

        while(topX != northEnd || topY != westEnd) {
            int nextX = 0;
            int nextY = 0;
            if(topY == eastEnd) {
                nextX = topX - 3 < northEnd ? northEnd - topX + 1 : -3;
                nextY = -1;
                down = !down;
            } else if(topY == westEnd) {
                if(topX - 3 < northEnd) {
                    break;
                }
                nextX = -3;
                nextY = 1;
                down = !down;
            } else if(topX == southEnd) {
                nextY = topY - 3 < westEnd ? westEnd - topY : -3;
                nextX = -1;
                down = !down;
            } else if(topX == northEnd) {
                if(topY - 3 < westEnd) {
                    break;
                }
                nextY = -3;
                nextX = 1;
                down = !down;
            } else if(down) {
                nextX = 1;
                nextY = -1;
            } else {
                nextX = -1;
                nextY = 1;
            }

            topX += nextX;
            topY += nextY;

            System.out.println("SW:" + topX + " " + topY);

            while(nextX != 0 || nextY != 0) {
                int nextDx = 0;
                int nextDy = 0;
                if(nextX > 0) {
                    nextX--;
                    nextDx++;
                } else if(nextX < 0) {
                    nextX++;
                    nextDx--;
                }

                if(nextY > 0) {
                    nextY--;
                    nextDy++;
                } else if(nextY < 0) {
                    nextY++;
                    nextDy--;
                }

                locations.add(new Point(topX, topY));
                path.add(new Point(nextDx, nextDy));
                moves.add(new Point(nextDx, nextDy));
            }
        }
    }

    private void setPosition(int x, int y) {
        this.position.x = x;
        this.position.y = y;
    }

    @Override
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

}