package scout.g3;

import scout.sim.*;
import java.util.*;

public class NWplayer extends DirectionalPlayer {

    public NWplayer(int id) {
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
        eastEnd = n/2+1;
        northEnd = 1;
        southEnd = n/2;

        NWpath();
        NWsubdivide();
    }

    @Override
    public void setOutpost() {
        x = 0;
        y = 0; 
    }

    @Override
    public void defaultMove() {
        if(orientedX) {
            setPosition(0, -1);
        } else if(orientedY) {
            setPosition(-1, 0);
        } else {
            setPosition(-1, -1);
        }
    }

    private void NWsubdivide() {
        int totalPathSize = moves.size();
        int totalNum = s/4 + (s % 4 > 0 ? 1 : 0);
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

    private void NWpath() {
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