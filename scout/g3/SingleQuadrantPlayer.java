package scout.g3;

import scout.sim.*;
import java.util.*;


/**
 * Player used when s < 3. Treats the whole board as a single quadrant.
 */
public class SingleQuadrantPlayer extends DirectionalPlayer {

    public SingleQuadrantPlayer(int id) {
        super(id);
        this.id = id;
    }

    @Override
    public void initDirections() {
        outpostLocations = new ArrayList<>();
        outpostLocations.add(new Point(0, 0));
        outpostLocations.add(new Point(0, n+1));
        outpostLocations.add(new Point(n+1, n+1));
        outpostLocations.add(new Point(n+1, 0));

        westEnd = 0;
        eastEnd = n+1;
        northEnd = 1;
        southEnd = n;

        path();
        subdivide();
        setOutposPos();
    }

    @Override
    public void setOutpost() {
        x = outpostLocations.get(0).x;
        y = outpostLocations.get(0).y;
    }


    private void setOutposPos() {
        int outpostIdx = -1;
        if(startPos != null) {
            if(Math.abs(startPos.x) < Math.abs(n+1 - startPos.x)) {
                if(Math.abs(startPos.y) < Math.abs(n+1 - startPos.y)) {
                    outpostIdx = 0;
                } else {
                    outpostIdx = 1;
                }
            } else {
                if(Math.abs(startPos.y) < Math.abs(n+1 - startPos.y)) {
                    outpostIdx = 3;
                } else {
                    outpostIdx = 2;
                }
            }
        } else {
            outpostIdx = 0;
        }

        Collections.rotate(outpostLocations, 4 - outpostIdx);
    }

    @Override
    public void defaultMove() {
        if(orientedX) {
            if(outpostLocations.get(0).y == 0) {
                setPosition(0, -1);
            } else {
                setPosition(0, 1);
            }
        } else if(orientedY) {
            if(outpostLocations.get(0).x == 0) {
                setPosition(-1, 0);
            } else {
                setPosition(1, 0);
            }
        } else {
            if(outpostLocations.get(0).x == 0 && outpostLocations.get(0).y == 0) {
                setPosition(-1, -1);
            } else if(outpostLocations.get(0).x == 0 && outpostLocations.get(0).y == n+1) {
                setPosition(-1, 1);
            } else if(outpostLocations.get(0).x == n+1 && outpostLocations.get(0).y == n+1) {
                setPosition(1, 1);
            } else if(outpostLocations.get(0).x == n+1 && outpostLocations.get(0).y == 0) {
                setPosition(1, -1);
            }
            
        }
    }

    private void subdivide() {
        int totalPathSize = moves.size();
        int totalNum = s/4 + s % 4;
        int pathLen = totalPathSize/totalNum;

        int remPath = totalPathSize % totalNum;

        if(id + 1 <= remPath) {
            pathLen++;
        }

        startIndex = id*pathLen;
        endIndex = (id + 1)*pathLen - 1;
        startPos = locations.get(startIndex);
    }

    private void path() {
        int topX = northEnd;
        int topY = eastEnd;
        boolean down = true;
        locations.add(new Point(topX, topY));

        while(topX != southEnd || topY != westEnd) {
            int nextX = 0;
            int nextY = 0;
            if(topY == eastEnd) {
                nextX = topX + 3 > southEnd ? southEnd - topX - 1 : 3;
                nextY = -1;
                down = !down;
            } else if(topY == westEnd) {
                if(topX + 3 > southEnd) {
                    break;
                }
                nextX = 3;
                nextY = 1;
                down = !down;
            } else if(topX == southEnd) {
                if(topY - 3 < westEnd) {
                    break;
                }
                nextY = -3;
                nextX = -1;
                down = !down;
            } else if(topX == northEnd) {
                nextY = topY - 3 < westEnd ? westEnd - topY + 1 : -3;
                nextX = 1;
                down = !down;
            } else if(down) {
                nextX = 1;
                nextY = 1;
            } else {
                nextX = -1;
                nextY = -1;
            }

            topX += nextX;
            topY += nextY;

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
                moves.add(new Point(nextDx, nextDy));
            }
        }
    }

}