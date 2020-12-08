import greenfoot.World;
import greenfoot.Actor;
import java.util.*;

/**
 * Lift simulation: LiftController
 * 
 * @author KEL
 * @version <li> V0.4  18.12.2017 old Version
 * @version <li> V0.5  21.08.2017 Refactoring
 * 
 */
public class LiftController 
{
    private static LiftController controller = null;
    private static int liftIndex = 0;
    private Lift[] lift;
    //private int[][] destination; //lift and floors:

    private static Random random = new Random();
     
    /**
     * Singleton: There will be only ONE LiftController
     */
    protected LiftController()
    {
      lift = new Lift[Building.DEFAULT_LIFTS]; 
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
     * 
     */
        public boolean allClosed() {
       // Search for waiting lift at current floor
        for (int li=0; li < Building.DEFAULT_LIFTS; li++) {
           if ((lift[li].getStatus() != Lift.LIFT_STOPPED)) {
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
       
       // Find next stopped lift
       for (int li=0; li < Building.DEFAULT_LIFTS; li++) {
           if (lift[li].getStatus() == Lift.LIFT_STOPPED) {
              int diff = toFloorNr - lift[li].getPastFloorNr();
              if (diff > 0) {
                   lift[li].incG2Dest(toFloorNr);
                   lift[li].start(Buttons.UP);
                   return;  // no further searching
               }
              if (diff <0) {
                   lift[li].incG2Dest(toFloorNr);
                   lift[li].start(Buttons.DOWN);
                   return;  // no further searching
              }
           }
       }
       // Set first Lift for destination
       lift[1].incG2Dest(toFloorNr);
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