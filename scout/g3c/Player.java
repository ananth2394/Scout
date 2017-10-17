package scout.g3c;

import scout.sim.*;
import java.util.*;

public class Player extends scout.sim.Player {
    List<Point> enemyRelativeLocations;
    List<Point> safeRelativeLocations;

    List<Point> enemyLocations;
    List<Point> safeLocations;
    Random gen;
    int t,n,s,id;
    int x = -1;
    int y = -1;

    // TODO: keep track when you get back to your outpost
    // Give data to outpost and go clockwise to next outpost
    // to give your information, send every other player in opposite
    // direction
    int outpost1Players;
    int outpost2Players;
    int outpost3Players;
    int outpost4Players;


    int outpostCount;
    int westEnd;
    int eastEnd;
    int northEnd;
    int southEnd;
    int[][] outposts;

    int curOutpost;

    int accX = 0;
    int accY = 0;
    int seed;
    boolean oriented;
    boolean newPhase;
    boolean rightDir;
    boolean exploring;
    boolean relay;

    Point direction;
    boolean skipRow;

    PlayerType type;
    List<Point> history;
    Queue<Point> moves;

    private class OutpostData {
        public int playerCount;

        public OutpostData(int playerCount) {
            this.playerCount = playerCount;
        }
    }

    private enum PlayerType {
        NW, NE, SE, SW;        
    }

    private class PlayerState {
        // track board info, move history, enemy and safe locations
        List<Point> enemyLocations;
        List<Point> safeLocations;
        List<Point> history;

        boolean oriented;
    }

    /**
    * better to use init instead of constructor, don't modify ID or simulator will error
    */
    public Player(int id) {
        super(id);
        seed=id;
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

        moves = new LinkedList<>();
        gen = new Random(seed);
        this.t = t;
        this.n = n;
        this.s = s;


        int minPlayers = s / 4;
        int extraPlayers = s % 4;
        outposts = new int[4][2];


        if(Integer.parseInt(id.substring(1)) % 4 == 0) {
            // Move NW
            this.direction = new Point(-1, -1);
            this.type = PlayerType.NW;

            outposts[0][0] = 0;
            outposts[0][1] = 0;

            outposts[1][0] = 0;
            outposts[1][1] = n+1;

            outposts[2][0] = n+1;
            outposts[2][1] = n+1;

            outposts[3][0] = n+1;
            outposts[3][1] = 0;


            outpost1Players = minPlayers + (extraPlayers > 0 ? 1 : 0);
            westEnd = 1;
            eastEnd = n/2;
            northEnd = 0;
            southEnd = n/2 - 1;
            NWsubdivide();
        } else if(Integer.parseInt(this.getID().substring(1)) % 4 == 1) {
            // Move NE
            this.direction = new Point(-1, 1);
            this.type = PlayerType.NE;


            outposts[0][0] = 0;
            outposts[0][1] = n+1;

            outposts[1][0] = n+1;
            outposts[1][1] = n+1;

            outposts[2][0] = n+1;
            outposts[2][1] = 0;

            outposts[3][0] = 0;
            outposts[3][1] = 0;

            outpost2Players = minPlayers + (extraPlayers > 1 ? 1 : 0);
            westEnd = n/2 + 1;
            eastEnd = n;
            northEnd = 0;
            southEnd = n/2;
            NEsubdivide();
        } else if(Integer.parseInt(this.getID().substring(1)) % 4 == 2) {
            // Move SE
            this.direction = new Point(1, 1);
            this.type = PlayerType.SE;

            outposts[0][0] = n+1;
            outposts[0][1] = n+1;

            outposts[1][0] = n+1;
            outposts[1][1] = 0;

            outposts[2][0] = 0;
            outposts[2][1] = 0;

            outposts[3][0] = 0;
            outposts[3][1] = n+1;

            outpost3Players = minPlayers + (extraPlayers > 2 ? 1 : 0);
            westEnd = n/2 + 1;
            eastEnd = n;
            northEnd = n/2;
            southEnd = n;
            SEsubdivide();
        } else if(Integer.parseInt(this.getID().substring(1)) % 4 == 3) {
            // Move SW
            this.direction = new Point(1, -1);
            this.type = PlayerType.SW;

            outposts[0][0] = n+1;
            outposts[0][1] = 0;

            outposts[1][0] = 0;
            outposts[1][1] = 0;

            outposts[2][0] = 0;
            outposts[2][1] = n+1;

            outposts[3][0] = n+1;
            outposts[3][1] = n+1;

            outpost4Players = minPlayers + (extraPlayers > 2 ? 1 : 0);
            westEnd = 1;
            eastEnd = n/2;
            northEnd = n/2;
            southEnd = n;
            SWsubdivide();
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
                outpostCount++;
                if(outpostCount > 1 && x == outposts[curOutpost][0] && y == outposts[curOutpost][1]) {
                    curOutpost = (curOutpost + 1) % 4;
                }
                if(outpostCount == 2) {
                    relay = true;
                } else if(outpostCount == 1) {
                    oriented = true;
                    newPhase = true;
                    if(this.type == PlayerType.NW) {
                        // Move NW
                        x = 0;
                        y = 0;
                    } else if(this.type == PlayerType.NE) {
                        // Move NE
                        x = 0;
                        y = n+1;
                    } else if(this.type == PlayerType.SE) {
                        // Move SE
                        x = n+1;
                        y = n+1;
                    } else if(this.type == PlayerType.SW) {
                        // Move SW
                        x = n+1;
                        y = 0;
                    }
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

        if(!moves.isEmpty()) {
            Point ret = moves.poll();
            x += ret.x;
            y += ret.y;
            return ret;
        } else if(!newPhase && !relay) {
            if (nearbyIds.get(0).get(1) == null) {
                //Up is null
                if(this.type == PlayerType.NW) {
                    this.direction = new Point(0, -1);
                } else if(this.type == PlayerType.NE) {
                    this.direction = new Point(0, 1);
                }
            } else if (nearbyIds.get(1).get(0) == null) {
                //Left is null
                if(this.type == PlayerType.NW) {
                    this.direction = new Point(-1, 0);
                } else if(this.type == PlayerType.SW) {
                    this.direction = new Point(1, 0);
                }
            } else if (nearbyIds.get(1).get(2) == null) {
                //Right is null
                if(this.type == PlayerType.NE) {
                    this.direction = new Point(-1, 0);
                } else if(this.type == PlayerType.SE) {
                    this.direction = new Point(1, 0);
                }
            } else if (nearbyIds.get(2).get(1) == null) {
                //Down is null
                if(this.type == PlayerType.SE) {
                    this.direction = new Point(0, 1);
                } else if(this.type == PlayerType.SW) {
                    this.direction = new Point(0, -1);
                }
            }
            accY += this.direction.y;
            accX += this.direction.x;
        } else {
            if(this.type == PlayerType.NW) {
                return NWmove();
            } else if(this.type == PlayerType.NE) {
                /*
                for(Point unsafe : enemyLocations) {
                    System.out.println("Enemy at: " + unsafe.x + "," + unsafe.y);
                }
                */
                return NEmove();
            } else if(this.type == PlayerType.SE) {
                return SEmove();
            } else if(this.type == PlayerType.SW) {
                return SWmove();
            }
        }
        //return x \in {-1,0,1}, y \in {-1,0,1}
        x += this.direction.x;
        y += this.direction.y;
        return this.direction;
    }


    public Point NWmove() {
        if(!exploring) {
            int nextX = 0;
            int nextY = 0;
            if(westEnd - y > 0) {
                y++;
                nextY = 1;
            }
            if(northEnd - x > 0) {
                x++;
                nextX = 1;
            }
            if(nextX > 0 || nextY > 0) {
                return new Point(nextX, nextY);
            } else {
                exploring = true;
            }

        }

        if(relay) {
            int nextX = 0;
            int nextY = 0;
            if(outposts[curOutpost][1] - y > 0) {
                y++;
                nextY = 1;
            }
            if(outposts[curOutpost][1] - y < 0) {
                y--;
                nextY = -1;
            }
            if(outposts[curOutpost][0] - x > 0) {
                x++;
                nextX = 1;
            }
            if(outposts[curOutpost][0] - x < 0) {
                x--;
                nextX = -1;
            }
            if(nextX != 0 || nextY != 0) {
                return new Point(nextX, nextY);
            } else {
                curOutpost = (curOutpost + 1) % 4;
            }
            return new Point(0,0);
        } else {
            if(rightDir) {
                // go right
                if(y >= eastEnd) {
                    //x = x + 2 <= southEnd ? x + 2 : southEnd;
                    if(x+2 >= southEnd) {
                        newPhase = !newPhase;
                    }
                    x++;
                    moves.add(new Point(1, 0));
                    moves.add(new Point(1, 0));
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
                    moves.add(new Point(1, 0));
                    moves.add(new Point(1, 0));
                    rightDir = !rightDir;
                    return new Point(1, 0);
                } else {
                    y--;
                    return new Point(0, -1);
                }
            }
        }

        
    }

    public Point NEmove() {
        //System.out.println("x: " + x);
        //System.out.println("y: " + y);
        if(!exploring) {
            int nextX = 0;
            int nextY = 0;
            if(y - eastEnd > 0) {
                y--;
                nextY = -1;
            }
            if(northEnd - x > 0) {
                x++;
                nextX = 1;
            }
            if(nextX != 0 || nextY != 0) {
                return new Point(nextX, nextY);
            } else {
                exploring = true;
            }
        }
        
        if(relay) {
            int nextX = 0;
            int nextY = 0;
            if(outposts[curOutpost][1] - y > 0) {
                y++;
                nextY = 1;
            }
            if(outposts[curOutpost][1] - y < 0) {
                y--;
                nextY = -1;
            }
            if(outposts[curOutpost][0] - x > 0) {
                x++;
                nextX = 1;
            }
            if(outposts[curOutpost][0] - x < 0) {
                x--;
                nextX = -1;
            }
            if(nextX != 0 || nextY != 0) {
                return new Point(nextX, nextY);
            } else {
                curOutpost = (curOutpost + 1) % 4;
            }
            return new Point(0,0);
        } else {
            if(rightDir) {
                // go left
                if(y <= westEnd) {
                    //x = x - 2 >= southEnd ? x - 2 : southEnd;
                    if(x+2 >= southEnd) {
                        newPhase = !newPhase;
                    }
                    x++;
                    moves.add(new Point(1, 0));
                    moves.add(new Point(1, 0));
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
                    moves.add(new Point(1, 0));
                    moves.add(new Point(1, 0));
                    rightDir = !rightDir;
                    return new Point(1, 0);
                } else {
                    y++;
                    return new Point(0, 1);
                }
            }
        }
    }

    public Point SEmove() {
        if(!exploring) {
            int nextX = 0;
            int nextY = 0;
            if(y - eastEnd > 0) {
                y--;
                nextY = -1;
            }
            if(x - southEnd > 0) {
                x--;
                nextX = -1;
            }
            if(nextX != 0 || nextY != 0) {
                return new Point(nextX, nextY);
            } else {
                exploring = true;
            }
        }
        
        if(relay) {
            int nextX = 0;
            int nextY = 0;
            if(outposts[curOutpost][1] - y > 0) {
                y++;
                nextY = 1;
            }
            if(outposts[curOutpost][1] - y < 0) {
                y--;
                nextY = -1;
            }
            if(outposts[curOutpost][0] - x > 0) {
                x++;
                nextX = 1;
            }
            if(outposts[curOutpost][0] - x < 0) {
                x--;
                nextX = -1;
            }
            if(nextX != 0 || nextY != 0) {
                return new Point(nextX, nextY);
            } else {
                curOutpost = (curOutpost + 1) % 4;
            }
            return new Point(0,0);
        } else {
            if(rightDir) {
                // go left
                if(y <= westEnd) {
                    //x = x - 2 >= southEnd ? x - 2 : southEnd;
                    if(x-2 <= northEnd) {
                        newPhase = !newPhase;
                    }
                    x--;
                    moves.add(new Point(-1, 0));
                    moves.add(new Point(-1, 0));
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
                    moves.add(new Point(-1, 0));
                    moves.add(new Point(-1, 0));
                    rightDir = !rightDir;
                    return new Point(-1, 0);
                } else {
                    y++;
                    return new Point(0, 1);
                }
            }
        }
        
    }

    public Point SWmove() {
        if(!exploring) {
            int nextX = 0;
            int nextY = 0;
            if(westEnd - y  > 0) {
                y++;
                nextY = 1;
            }
            if(x - southEnd > 0) {
                x--;
                nextX = -1;
            }
            if(nextX != 0 || nextY != 0) {
                return new Point(nextX, nextY);
            } else {
                exploring = true;
            }
        }
        
        if(relay) {
            int nextX = 0;
            int nextY = 0;
            if(outposts[curOutpost][1] - y > 0) {
                y++;
                nextY = 1;
            }
            if(outposts[curOutpost][1] - y < 0) {
                y--;
                nextY = -1;
            }
            if(outposts[curOutpost][0] - x > 0) {
                x++;
                nextX = 1;
            }
            if(outposts[curOutpost][0] - x < 0) {
                x--;
                nextX = -1;
            }
            if(nextX != 0 || nextY != 0) {
                return new Point(nextX, nextY);
            } else {
                curOutpost = (curOutpost + 1) % 4;
            }
            return new Point(0,0);
        } else {
            if(rightDir) {
                // go right
                if(y >= eastEnd) {
                    //x = x - 2 >= southEnd ? x - 2 : southEnd;
                    if(x-2 <= northEnd) {
                        newPhase = !newPhase;
                    }
                    x--;
                    moves.add(new Point(-1, 0));
                    moves.add(new Point(-1, 0));
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
                    moves.add(new Point(-1, 0));
                    moves.add(new Point(-1, 0));
                    rightDir = !rightDir;
                    return new Point(-1, 0);
                } else {
                    y--;
                    return new Point(0, -1);
                }
            }
        }
        
    }

    /*
    private void convertRelativeToAbsolute() {
        for(Point p : enemyRelativeLocations) {
            absSafe = new Point(x,y);
            absSafe.x -= accX;
            absSafe.y -= accY;

            //enemyLocations.add()
        }
    }
    */

    private void NWsubdivide() {
        int totalNum = s/4 + (s % 4 > 0 ? 1 : 0);
        int squares = (int) Math.pow(4, Math.floor(Math.log(totalNum)/Math.log(4)));

        int extraDiv = totalNum - squares;
        int squareNum = id/4 % squares;

        int squareWidth = (int) Math.ceil(n/(2.0*Math.sqrt(squares)));
        int squareHeight = (int) Math.ceil(n/(2.0*Math.sqrt(squares)));

        int horizontalOffset = (int) (squareNum % Math.sqrt(squares)) * squareWidth;
        int verticalOffset = (int) (squareNum / Math.sqrt(squares)) * squareHeight;

        westEnd = 1 + horizontalOffset;
        eastEnd = westEnd + squareWidth - 1;
        northEnd = verticalOffset;
        southEnd = northEnd + squareHeight - 1;

        System.out.println(southEnd);
    }

    private void NEsubdivide() {
        int totalNum = s/4 + (s % 4 > 1 ? 1 : 0);
        //System.out.println(totalNum);
        int squares = (int) Math.pow(4, Math.floor(Math.log(totalNum)/Math.log(4)));
        //System.out.println(squares);

        int extraDiv = totalNum - squares;
        int squareNum = (id - 1)/4 % squares;
        //System.out.println(squareNum);

        int squareWidth = (int) Math.ceil(n/(2.0*Math.sqrt(squares)));
        int squareHeight = (int) Math.ceil(n/(2.0*Math.sqrt(squares)));
        System.out.println("Width: " + squareWidth);
        System.out.println("Height: " + squareHeight);

        int horizontalOffset = (int) (squareNum % Math.sqrt(squares)) * squareWidth;
        int verticalOffset = (int) (squareNum / Math.sqrt(squares)) * squareHeight;

        System.out.println("h offset: " + horizontalOffset);
        System.out.println("v offset: " + verticalOffset);

        westEnd = n/2 + 1 + horizontalOffset;
        System.out.println("West: " + westEnd);

        eastEnd = westEnd + squareWidth - 1;
        System.out.println("East: " + eastEnd);

        northEnd = verticalOffset;
        System.out.println("North: " + northEnd);

        southEnd = northEnd + squareHeight - 1;
        System.out.println("South: " + southEnd);
    }

    private void SEsubdivide() {
        int totalNum = s/4 + (s % 4 > 2 ? 1 : 0);
        //System.out.println(totalNum);
        int squares = (int) Math.pow(4, Math.floor(Math.log(totalNum)/Math.log(4)));
        //System.out.println(squares);

        int extraDiv = totalNum - squares;
        int squareNum = (id - 2)/4 % squares;
        //System.out.println(squareNum);

        int squareWidth = (int) Math.ceil(n/(2.0*Math.sqrt(squares)));
        int squareHeight = (int) Math.ceil(n/(2.0*Math.sqrt(squares)));
        System.out.println("Width: " + squareWidth);
        System.out.println("Height: " + squareHeight);

        int horizontalOffset = (int) (squareNum % Math.sqrt(squares)) * squareWidth;
        int verticalOffset = (int) (squareNum / Math.sqrt(squares)) * squareHeight;

        System.out.println("h offset: " + horizontalOffset);
        System.out.println("v offset: " + verticalOffset);

        westEnd = n/2 + 1 + horizontalOffset;
        System.out.println("West: " + westEnd);

        eastEnd = westEnd + squareWidth - 1;
        System.out.println("East: " + eastEnd);

        northEnd = n/2 + verticalOffset;
        System.out.println("North: " + northEnd);

        southEnd = northEnd + squareHeight - 1;
        System.out.println("South: " + southEnd);
    }

    private void SWsubdivide() {
        int totalNum = s/4 + (s % 4 == 1 ? 1 : 0);
        //System.out.println(totalNum);
        int squares = (int) Math.pow(4, Math.floor(Math.log(totalNum)/Math.log(4)));
        //System.out.println(squares);

        int extraDiv = totalNum - squares;
        int squareNum = (id - 3)/4 % squares;
        //System.out.println(squareNum);

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
    }
    public void stub() {
        ;
    }

    @Override
    public void communicate(ArrayList<ArrayList<ArrayList<String>>> nearbyIds, List<CellObject> concurrentObjects) {
        --t;
    }

    @Override
    public void moveFinished() {
        ;
    }
}

