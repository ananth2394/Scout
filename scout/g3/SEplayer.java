package scout.g3;

import scout.sim.*;
import java.util.*;

public class SEplayer extends DirectionalPlayer {

    public SEplayer(int id) {
        super(id);
        this.id = id;
    }

    @Override
    public void initDirections() {
        outpostLocations = new ArrayList<>();
        outpostLocations.add(new Point(n+1, n+1));
        outpostLocations.add(new Point(n+1, 0));
        outpostLocations.add(new Point(0, 0));
        outpostLocations.add(new Point(0, n+1));

        westEnd = n/2;
        eastEnd = n+1;
        northEnd = n/2;
        southEnd = n;

        SEpath();
        SEsubdivide();
    }

    @Override
    public void setOutpost() {
        x = n+1;
        y = n+1; 
    }

    @Override
    public void defaultMove() {
        if(orientedX) {
            setPosition(0, 1);
        } else if(orientedY) {
            setPosition(1, 0);
        } else {
            setPosition(1, 1);
        }
    }

    private void SEsubdivide() {
        int totalPathSize = moves.size();
        int totalNum = s/4 + (s % 4 > 2 ? 1 : 0);
        int pathLen = totalPathSize/totalNum;

        int relId = id/4;

        int remPath = totalPathSize % totalNum;

        if(relId + 1 <= remPath) {
            pathLen++;
        }


        startIndex = relId*pathLen;
        endIndex = (relId + 1)*pathLen - 1;
        startPos = locations.get(startIndex);
    }

    private void SEpath() {
        List<Point> path = new LinkedList<>();
        locations = new ArrayList<>();

        int topX = southEnd;
        int topY = westEnd;
        boolean down = false;
        locations.add(new Point(topX, topY));

        while(topX != northEnd || topY != eastEnd) {
            int nextX = 0;
            int nextY = 0;
            if(topY == eastEnd) {
                if(topX - 3 < northEnd) {
                    break;
                }
                nextX = -3;
                nextY = -1;
                down = !down;
            } else if(topY == westEnd) {
                nextX = topX - 3 < northEnd ? northEnd - topX + 1 : -3;
                nextY = 1;
                down = !down;
            } else if(topX == southEnd) {
                nextY = topY + 3 > eastEnd ? eastEnd - topY - 1: 3;
                nextX = -1;
                down = !down;
            } else if(topX == northEnd) {
                if(topY + 3 > eastEnd) {
                    break;
                }
                nextY = 3;
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
                path.add(new Point(nextDx, nextDy));
                moves.add(new Point(nextDx, nextDy));
            }
        }
    }


}