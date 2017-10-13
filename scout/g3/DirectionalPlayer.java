package scout.g3;

import scout.sim.*;
import java.util.*;

public abstract class DirectionalPlayer extends scout.sim.Player {
	public DirectionalPlayer(int id) {
        super(id);
    }

    public abstract void shareInfo(List<Point> sharedSafeLocations, List<Point> sharedEnemyLocations);
}