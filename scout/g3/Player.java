package scout.g3;

import scout.sim.*;
import java.util.*;

public class Player extends scout.sim.Player {
    DirectionalPlayer directionalPlayer;
    public int id;

    private class OutpostData {
        public int playerCount;

        public OutpostData(int playerCount) {
            this.playerCount = playerCount;
        }
    }

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
        this.id = id;
        if(id % 4 == 0) {
            directionalPlayer = new NWplayer(id);
        } else if(id % 4 == 1) {
            directionalPlayer = new NEplayer(id);
        } else if(id % 4 == 2) {
            directionalPlayer = new SEplayer(id);
        } else if(id % 4 == 3) {
            directionalPlayer = new SWplayer(id);
        }
    }

    public void stub() {
        ;
    }

    /**
    *   Called at the start
    */
    @Override
    public void init(String id, int s, int n, int t, List<Point> landmarkLocations) {
        directionalPlayer.init(id, s, n, t, landmarkLocations);
    }

    @Override
    public Point move(ArrayList<ArrayList<ArrayList<String>>> nearbyIds, List<CellObject> concurrentObjects) {
        return directionalPlayer.move(nearbyIds, concurrentObjects);
    }


    @Override
    public void communicate(ArrayList<ArrayList<ArrayList<String>>> nearbyIds, List<CellObject> concurrentObjects) {
        directionalPlayer.communicate(nearbyIds, concurrentObjects);
    }

    @Override
    public void moveFinished() {
        directionalPlayer.moveFinished();
    }

    public void shareInfo(List<Point> sharedSafeLocations, List<Point> sharedEnemyLocations) {
        directionalPlayer.shareInfo(sharedSafeLocations, sharedEnemyLocations);
    }

}

