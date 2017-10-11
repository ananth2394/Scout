package scout.g3_ananth;

import scout.sim.*;
import scout.g3_ananth.*;
import java.util.*;



//Read scout.sim.Player for more information!
public class Player extends scout.sim.Player {
    public PlayerHelper ph;
    private int t,n,id;
    private List<Point> landmarkLocations;
    int stage = 0; /*

    0: Find outpost
    1: Explore board
    2: Get back to outpost
    */
    boolean stage0;
    int round=0;
    LinkedList<String> cycle;
    String[] checkmark = new String[2];

    // for stage1 turning
    int diagonalTurn = 0;

    /**
    * better to use init instead of constructor, don't modify ID or simulator will error
    */
    public Player(int id) {
        super(id);
        this.id=id;
    }

    /**
    *   Called at the start
    */
    @Override
    public void init(String id, int s, int n, int t, List<Point> landmarkLocations) {
        this.t = t;
        this.n = n;
        this.landmarkLocations = landmarkLocations;
        this.ph = new PlayerHelper(n,this.id);
        this.stage0 = false;
        this.stage = 0;
        // change for each player
        // this.cycle = new LinkedList<String>();
        // switch(this.id%4){
        //     case 0:
        //         cycle.addAll(Arrays.asList(new String[]{"S","S","S","SE","SW","SW","W","NW","S","S"}));
        //         checkmark=new String[]{"SE","NW"};
        //         break;
        //     case 1:
        //         cycle.addAll(Arrays.asList(new String[]{"W","W","W","SW","NW","NW","N","NE","W","W"}));
        //         checkmark=new String[]{"SW","NE"};
        //         break;
        //     case 2:
        //         cycle.addAll(Arrays.asList(new String[]{"E","E","E","NE","SE","SE","S","SW","E","E"}));
        //         checkmark=new String[]{"NE","SW"};
        //         break;
        //     case 3:
        //         cycle.addAll(Arrays.asList(new String[]{"N","N","N","NW","NE","NE","E","SE","N","N"}));
        //         checkmark=new String[]{"NW","SE"};
        //         break;
        // }

    }

    /**
     * nearby IDs is a 3 x 3 grid of nearby IDs with you in the center (1,1) position. A position is null if it is off the board.
     * Enemy IDs start with 'E', Player start with 'P', Outpost with 'O' and landmark with 'L'.
     *
     */
    @Override
    public Point move(ArrayList<ArrayList<ArrayList<String>>> nearbyIds, List<CellObject> concurrentObjects) {
        this.ph.updatePlayerHelper(nearbyIds, concurrentObjects);
        String moveDirection = "C";
        List<String> moves = this.ph.getAllValidMoves(nearbyIds);

        // Do your work here
        // we move toward an outpost :)
        if(this.stage == 0){
            if(this.id%4==0){
                moveDirection = toOutpost(moves,"NW", "N","W");
            } else if(this.id%4==1){
                moveDirection = toOutpost(moves,"NE", "N","E");
            }else if(this.id%4==2){
                moveDirection = toOutpost(moves,"SW", "S","W");
            }else{
                moveDirection = toOutpost(moves,"SE", "S","E");
            }
            if(moveDirection == "C"){
                this.stage = 1;
            }
        }

        if(this.stage == 1)
        {
          if(this.ph.moveToStage2(t))
          {  this.stage = 2;
            //break;
          }
          moveDirection = this.ph.getRandomBestMove(nearbyIds);


        }

        if(this.stage ==2 )
        {
          if(this.id%4==0){
              moveDirection = toOutpost(moves,"NW", "N","W");
          } else if(this.id%4==1){
              moveDirection = toOutpost(moves,"NE", "N","E");
          }else if(this.id%4==2){
              moveDirection = toOutpost(moves,"SW", "S","W");
          }else{
              moveDirection = toOutpost(moves,"SE", "S","E");
          }
          if(moveDirection == "C"){
              //this.stage = 1;
          }
        }
        // if(this.stage0 == true){
        //     String peekCycle = this.cycle.peek();
        //     if(peekCycle == checkmark[0]){
        //         if(!intersectionCheck()){
        //             moveDirection = peekCycle;
        //         } else {
        //             moveDirection = popQueue();
        //         }
        //     } else if(peekCycle == checkmark[1]){
        //         // hits boundary
        //         if(!this.ph.hitBoundary()){
        //             moveDirection = peekCycle;
        //         } else {
        //             moveDirection = popQueue();
        //         }
        //     } else {
        //         moveDirection = popQueue();
        //     }
        //
        //     if(this.ph.hitOutpost()){
        //       round++;
        //     }
        //
        // }
        //
        // if(round > 1){
        //   if(this.ph.hitOutpost()){
        //     if(this.ph.getAbsoluteX() == 0 && this.ph.getAbsoluteY() == 0){
        //       moveDirection = "S";
        //     } else if(this.ph.getAbsoluteX() == 0 && this.ph.getAbsoluteY() == n+1){
        //       moveDirection = "W";
        //     } else if(this.ph.getAbsoluteX() == n+1 && this.ph.getAbsoluteY() == 0){
        //       moveDirection = "E";
        //     } else {
        //       moveDirection = "N";
        //     }
        //   }else{
        //     if(!moves.contains("N")){
        //       moveDirection = "W";
        //     } else if(!moves.contains("E")){
        //       moveDirection = "N";
        //     } else if(!moves.contains("S")){
        //       moveDirection = "E";
        //     } else {
        //       moveDirection = "S";
        //     }
        //   }
        // }


        // if(this.id%4==1 && this.ph.hitOutpost()){
        //     this.ph.grid.printGrid();
        // }

        // make sure that you make your final move through PlayerHelper!

        return this.ph.move(moveDirection);
    }

    public boolean intersectionCheck(){
        switch (this.id%4){
            case 0:
                return (this.ph.getAbsoluteX() + this.ph.getAbsoluteY() == n);
            case 1:
                return (this.ph.getAbsoluteX() - this.ph.getAbsoluteY() == -1);
            case 2:
                return (this.ph.getAbsoluteX() - this.ph.getAbsoluteY() == 1);
            case 3:
                return (this.ph.getAbsoluteX() + this.ph.getAbsoluteY() == n+2);
        }
        return true;
    }

    public String popQueue(){
        String temp = this.cycle.pop();
        this.cycle.add(temp);
        return this.cycle.peek();
    }

    public String toOutpost(List<String> moves, String initial, String e1, String e2){
        if(moves.contains(initial)){
            return initial;
        } else if (moves.contains(e1)){
            return e1;
        } else if (moves.contains(e2)){
            return e2;
        }
        return "C";
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
        this.ph.updateCurrentLocation();
    }

}
