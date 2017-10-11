package scout.g3_ananth;

import scout.sim.*;

import java.util.*;

public class AbsoluteGrid{
    public int[][] grid;
    public static final String RED = "\033[0;31m";     // RED
    public static final String GREEN = "\u001B[32m";   //GREEN
    public static final String CYAN = "\u001B[36m";    //CYAN
    public static final String YELLOW = "\u001B[33m";  //YELLOW
    public static final String DEFAULT = "\u001B[0m";  //DEFAULT

    public AbsoluteGrid(int n) {
        /*  the grid will contain what is located
            0-unexplored
            1-enemy
            2-safe
        */
        this.grid = new int[n+2][n+2];
        for( int i = 0; i < this.grid.length ; i++){
            for( int j = 0; j < this.grid[0].length; j++){
                this.grid[i][j] = 0;
            }
        }
    }

    public AbsoluteGrid(AbsoluteGrid another) {
        int n = another.grid.length;
        this.grid = new int[n][n];
        for( int i = 0; i < this.grid.length ; i++){
            for( int j = 0; j < this.grid[0].length; j++){
                if (another.grid[i][j]!=0){
                  this.grid[i][j] = another.grid[i][j];
                }
            }
        }
    }

    // add enemy and safe location
    public void addEnemy(Point location){
        this.grid[location.x][location.y] = 1;
    }

    public void addSafe(Point location){
        this.grid[location.x][location.y] = 2;
    }

    // returns list of enemies or safe locations
    private List<Point> getList(int type){
      List<Point> listLocation = new ArrayList<>();
      for( int i = 1; i < grid.length-1; i++){
        for( int j = 1; j < grid[0].length-1; j++){
            if(this.grid[i][j]==type){
              listLocation.add(new Point(i,j));
            }
        }
      }
      return listLocation;
    }
    public List<Point> getEnemy(){
        return getList(1);
    }

    public List<Point> getSafe(){
      return getList(2);
    }

    public int[][] getGrid(){
      return this.grid;
    }

    public void setGrid(int[][] grid){
      this.grid=grid;
    }

    public static void merge(AbsoluteGrid A1, AbsoluteGrid A2){
      int[][] a1 = A1.getGrid();
      int[][] a2 = A2.getGrid();

      int a1val = -1;
      int a2val = -1;

      for( int i = 1; i < a1.length-1 ; i++){
          for( int j = 1; j < a1[0].length-1; j++){
              a1val = a1[i][j];
              a2val = a2[i][j];

              if( a1val != a2val ){
                  if( a1val == 0 && a2val != 0 ){
                      a1[i][j] = a2val;
                  } else if( a1val != 0 && a2val == 0){
                      a2[i][j] = a1val;
                  } else{
                      System.out.println("There is a mismatch!");
                  }
              }
        }
      }

      A1.setGrid(a1);
      A2.setGrid(a2);
    }



    public void printGrid(){
        int grid_size = grid.length;
        //print col index
        System.out.print(YELLOW +  "    |  " );
        for(int i = 0; i < grid_size; i++){
            System.out.print((i % grid_size)+ "  ");
            if(i < 10){
                System.out.print(" ");
            }
        }
        System.out.println();
        //print dashed line
        for(int i = 0; i < grid_size*5; i++){
            System.out.print("-");
        }
        System.out.println(DEFAULT);
        for( int i = 0; i < grid_size; i++){
          //print row number
            System.out.print(YELLOW + (i % grid_size) + DEFAULT);
            if(i < 10){
                System.out.print(YELLOW + " "  + "  |  " + DEFAULT);
            }
            else{
              System.out.print(YELLOW +  "  |  " + DEFAULT);
            }
            for( int j = 0; j < grid_size; j++){
                if((i == 0 || i == grid_size -1) && (j == 0 || j == grid_size -1)){
                  System.out.print(CYAN + "P" + DEFAULT + "  ");
                  System.out.print(" ");
                  continue;
                }
                else if(i == 0 || j == 0 || i == grid_size -1 || j == grid_size -1){
                  System.out.print(CYAN + "0" + DEFAULT + "  ");
                  System.out.print(" ");
                  continue;
                }
                if(grid[i][j] == 1){
                    System.out.print(RED + grid[i][j] + DEFAULT + "  ");
                }
                else if(grid[i][j] == 2){
                    System.out.print(GREEN + grid[i][j] + DEFAULT + "  ");
                }
                else{
                    System.out.print(grid[i][j] + "  ");
                }

                  System.out.print(" ");

            }
            System.out.println();
        }
        System.out.println();
    }
}
