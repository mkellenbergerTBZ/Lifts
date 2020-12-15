import greenfoot.World;
import greenfoot.Actor;
import java.util.*;

/**
 * Lift simulation: LiftController
 * 
 * @author KEL
 * @version <li> V0.4  18.12.2017 old Version
 * @version <li> V0.5  21.08.2017 Refactoring
 * @version <li> V0.7  12.08.2020 Add floors to get to the buttons
 * @version <li> V0.9  12.12.2020 Fixed: Not nearest lift is aquired!
 * 
 */
public class LiftController 
{
    private static LiftController controller = null;
    private static int liftIndex = 0;
    private Lift[] lift;
    private Floor[] floor;
    //private int[][] destination; //lift and floors:

    private static Random random = new Random();
     
    /**
     * Singleton: There will be only ONE LiftController
     */
    protected LiftController()
    {
      lift = new Lift[Building.DEFAULT_LIFTS]; 
      floor = new Floor[Building.DEFAULT_FLOORS]; 
     // destination = new int[Building.DEFAULT_LIFTS][Building.DEFAULT_FLOORS];
    }
    
    /**
    * Gets THE instance of a liftcontroller.
    */
    public static LiftController getInstance() {
      if(controller == null) {
         controller = new LiftController();
      }
      return controller;
    }
   
    /**
    * Setup: add lift to controller
    */
    public void addLift(int index, Lift li) {
        lift[index] = li;
    }
    
    /**
    * Setup: add floor to controller
    */
    public void addFloor(int index, Floor fl) {
        floor[index] = fl;
    }
    
    /**
     *  Chekcs if all lifts are stopped
     */
        public boolean allStopped() {
       // Search for waiting lift at current floor
        for (int li=0; li < Building.DEFAULT_LIFTS; li++) {
           if ((lift[li].getStatus() != Lift.LIFT_STOPPED)) {
               return false ; 
           }
       }
       return true;
    }
    
      
    /**
     * Chekc if all buttons are releases
     */
        public boolean nonePressed() {
       // Search for waiting lift at current floor
        for (int fl=0; fl < Building.DEFAULT_FLOORS; fl++) {
           if ((floor[fl].getButtons() != Buttons.NONE)) {
               return false ; 
           }
       }
       return true;
    }  
    /**
     * Aquires any Lift for certain floorNr and direction (UP / DOWN)
     */
    public void aquireAnyLift(int toFloorNr, int direction) {
       // Search for waiting lift at current floor
        for (int li=0; li < Building.DEFAULT_LIFTS; li++) {
           if ((lift[li].atFloor() != null) && (lift[li].atFloor().getFloorNr()== toFloorNr)) {
               lift[li].openDoors(direction); 
               return; // no further searching
           }
       }
       
       // Search for already setted lift
        for (int li=0; li < Building.DEFAULT_LIFTS; li++) {
           if (lift[li].getG2Dest(toFloorNr) > 0) {
               return;  // no further searching
           }
       }

       // Find next lift heading in my direction
       for (int li=0; li < Building.DEFAULT_LIFTS; li++) {
           int diff = toFloorNr - lift[li].getPastFloorNr();
           if (lift[li].getStatus() == Lift.LIFT_UP && (diff > 0)) {
               lift[li].incG2Dest(toFloorNr);
               return;  // no further searching
           }
           if (lift[li].getStatus() == Lift.LIFT_DOWN && (diff <0)) {
               lift[li].incG2Dest(toFloorNr);
               return;  // no further searching
           }
       }
                  
       // Find next stopped and nearest lift
       int diffLift;                         // store distance
       int minDiff=Building.DEFAULT_FLOORS;   // find minimal distance
       int nLift=0;                           // narest Lift
       for (int li=0; li < Building.DEFAULT_LIFTS; li++) {
           //search for nearest closed lift 
           if (lift[li].getStatus() == Lift.LIFT_STOPPED) {
              diffLift = toFloorNr - lift[li].getPastFloorNr();
              if (Math.abs(diffLift) < Math.abs(minDiff)){ // there are + and - values!!
                  // if this lift is nearer -> store it
                  minDiff = diffLift;
                  nLift = li;
              }
           }
       }
       // chose nearest lift
       if (lift[nLift].getStatus() == Lift.LIFT_STOPPED) {
          if (minDiff > 0) {
               lift[nLift].incG2Dest(toFloorNr);
               lift[nLift].start(Buttons.UP);
               return;  // no further searching
           }
          if (minDiff <0) {
               lift[nLift].incG2Dest(toFloorNr);
               lift[nLift].start(Buttons.DOWN);
               return;  // no further searching
          }
           
       }

       // Set first Lift for destination (This should not happen!!!)
       lift[0].incG2Dest(toFloorNr);
       return;
    }
    
    
    /**
    * Return a random floor number.
    */
    public int getRandomLiftNr() {
        return 0; // !!! random.nextInt(Building.DEFAULT_LIFTS);
    }
    
    /**
     * A method that when called returns a random number.
     */
    public static Random getRandomizer()  {
        return random;
    }

}