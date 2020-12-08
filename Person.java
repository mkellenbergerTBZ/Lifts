import greenfoot.*;
import java.util.*;

/**
 * Lift simulation: Person will be set on floor (via constructir), moves to lift or leaves floor.
 * 
 * @author KEL
 * @version <li> V0.1  (Beta)
 * @version <li> V0.2  17.12.2017 Status LEAVE_FLOOR added
 * @version <li> V0.3  18.12.2017 Error status-call solved
 * @version <li> V0.4  18.12.2017 old Version
 * @version <li> V0.5  21.08.2017 Refactoring
 * @version <li> V0.6  06.12.2018 Some enhancements / comments added
 * @version <li> V0.7  08.12.2020 Fix error: Numbers on floor on exit! >> currentFloorNr deleted!
 * 
 */
public class Person extends Actor
{
    private static final Random random = Building.getRandomizer(); // gets random from Buildingclass

    public static final int STAYS_ON_FLOOR = 0; //Status values of the Person
    public static final int ENTER_LIFT     = 1; 
    public static final int STAYS_IN_LIFT  = 2; 
    public static final int LEAVE_LIFT     = 3;
    public static final int LEAVE_FLOOR    = 4;
    
    private static final int XPOS_DOOR = 210;
    private static final int XPOS_EXIT = 10;
    
    private Floor currentFloor;  //Points to actual floor if waitung/leaving, otw NULL
    private Lift currentLift;    //Points to actual lift if staying in lift, otw NULL 
    private int destFloorNr;     //Wish to go
    private int status;          //Counts Status
    /**
     * Standard constructor: not used;
     */
    private Person()
    {
    }
    /**
     * Creates person on certain floor (by clicking on the floor)! 
     * Person waits on lift to come ...
     * 
     * @param floor sets new Person to this floor
     * @see Floor#checkMouseClick
     */
    public Person(Floor floor)
    {
        setImage("person.gif");
        currentFloor = floor;
        status = STAYS_ON_FLOOR;
        
        /*
        int currentFloorNr = currentFloor.getFloorNr();
        if (currentFloorNr == 3){
            destFloorNr = 2;
        } else  {
            destFloorNr = 3;// !!! pickRandomFloorNr(building);
        }
        */
        destFloorNr = pickRandomFloorNR();
        if(isGoingup()) {
            currentFloor.pushButton(Buttons.UP);
        } else if (isGoingdown() ) {
            currentFloor.pushButton(Buttons.DOWN);
        } else 
            status = ENTER_LIFT;  // ERROR-Case >> Leave!!!
    }
    /**
     * Act: Animate persons movement
     */
    public void act()
    {
        // checkMouseClick();
        switch (status) {
          case STAYS_ON_FLOOR:              break; // Wait on lift to come...
          case ENTER_LIFT:    enterLift();  break; // Animate entering
          case STAYS_IN_LIFT:               break; // No animation! --> Lift
          case LEAVE_LIFT:    leaveLift();  break; // Animate leaving and ...
          case LEAVE_FLOOR:   leaveFloor(); break; // exiting building!
        }
    }
    
    /**
    * Starts and animates liftentering.
    */
    public void enterLift(Lift lift)
    {  // First move
        status = ENTER_LIFT;  // Sets new status on external call!
        currentLift = lift;   // Sets current lift
        enterLift();
        lift.restartTimer();
    }
    
    /**
    * Animates liftentering.
    */
    private void enterLift()
    {
        if (getX() < XPOS_DOOR) { // Still move
            move(1);
        } else { // Flipp to Lift
            setImage((GreenfootImage)null); // No Image since person enters the lift ....
            currentLift.goInto(this);
            //currentFloor.leave(this); !!!
            nextStatus();
        }
    } 
        
    /**
    * Sets and animates leaving at the new floor!
    */
    public void leaveLift(Floor floor)
    {
       status = LEAVE_LIFT;  // Sets new status on external call!
       currentFloor = floor; // Sets new floor level
       leaveLift();
    }
    
    /**
    * Animates leaving at the new floor!
    */
    private void leaveLift()
    {
       setImage("person.gif"); // Image reappears since person leaves lift ...
       int yPos = currentFloor.enter(this); // returns new floor-position
       setLocation(XPOS_DOOR - random.nextInt(10), yPos);
       nextStatus();
    }
    
     /**
     * Animates leaving the building.
     */
    private void leaveFloor()
    {
        if (getX() > XPOS_EXIT) { // move to lifts
            move(-random.nextInt(3));
        } else if (getX() <= XPOS_EXIT) { // Enter lift
            List<Floor> floors = getWorld().getObjects(Floor.class);
            for (Floor floor : floors) {
                if ( floor.getFloorNr() == currentFloor.getFloorNr() ){ 
                   floor.leave(this);             //Remove Person from building and floor
                   getWorld().removeObject(this);
                }

            }
        }
    }
    
    /**
     * Returns the destination floor number
     */
    public int getDestFloorNr()
    {
        return destFloorNr ;
    }   
    
    /**
     * Return whether or not we want to go up (otherwiese we want to go down).
     * 
     * 
     */
    public boolean isGoingup()
    {
        return (destFloorNr >  currentFloor.getFloorNr()) && (status < LEAVE_LIFT );
    }
    /**
     * Return whether or not we want to go down.
     * 
     * 
     */
    public boolean isGoingdown()
    {
        return (destFloorNr <  currentFloor.getFloorNr()) && (status < LEAVE_LIFT );
    }

    /**
     * Choose a random floor number (but not the one we are currently on).
     * 
     */
    private int pickRandomFloorNR()
    {
        int floorNr;
        do {
            floorNr = random.nextInt(Building.DEFAULT_FLOORS);
        } while (floorNr ==  currentFloor.getFloorNr());
        return floorNr;
    }
    
    /**
    * Sets Person to next Status.
    */
    public void nextStatus()
    {
        if (status < LEAVE_FLOOR) {
            status++;
        } 
    }
    
    /**
    * Gets Persons current Status.
    */
    public int getStatus()
    {
        return status;
    }
}