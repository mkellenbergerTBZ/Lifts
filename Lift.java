import greenfoot.*;
import java.util.*;
import javax.swing.ImageIcon;

/**
 * Lift simulation: Floor
 * 
 * @author KEL
 * @version <li> V0.4  18.12.2017 old Version
 * @version <li> V0.5  21.08.2017 Refactoring
 * @version <li> V0.6  30.08.2017 Liftcontrolling
 * @version <li> V0.7  12.12.2020 fixed: Persons on edge floor are left out 
 *                                       Lift is moving without buttons hit
 * @version <li> V0.8  12.12.2020 fixed: While going up an empty - not stopping for oposite direction
 */

public class Lift extends Actor
{
    private static final Random random = Building.getRandomizer();

    // Static constants  
    public static final int LIFT_STOPPED = 0; //Cabine is waiting w closed Door
    public static final int LIFT_UP = 1;      //Cabine is going up from floor to floor
    public static final int LIFT_DOWN = 2;    //Cabine is going down from floor to floor
    public static final int LIFT_OPEN = -1;   //Cabine is waiting w open Door
    
    private static final int TIMER_END = 250; // CountDownTimer to close door

    private int status;      // Saves status: see above
    private int direction;   // sets directins according to buttons set
    private int pastFloorNr; // Saves past floor number for quick access or test while moving
    private int timer;       // counts time to close door again. May be restarted be a person entering lift
    private ArrayList<Person> people;  // List of the people currently in the lift
    private Floor currentFloor;        // Set to current serving floor; null if none!
    private int[] goToDest;            // Saves all Destination to go in the lift
    private LiftController controller; // Connects to controller
    private boolean idle;              // status for idle
    private int emptyCounter;          // counts floor moves to avoid endless moves because of person enter wrong lift
    
    private GreenfootImage openImage;  // Cabine images
    private GreenfootImage emptyImage;
    private GreenfootImage closedImage;
    private GreenfootImage personImage;
    
    /**
     * Creates and initializes Lift cabine:
     * Reset of all properties
     * Connects to only controller
     * Sets space fpr passenger and destination
     */
    public Lift() {
        personImage =  new GreenfootImage("images/person.gif");
        openImage   =  new GreenfootImage("images/lift-open.jpg");
        emptyImage  =  new GreenfootImage("images/lift-open.jpg");
        closedImage =  new GreenfootImage("images/lift-closed.jpg");
        setImage(closedImage);
        status = LIFT_STOPPED;    // Init Status
        direction = Buttons.NONE; // No direction jet
        timer = 0;        // Timer ended
        /// currentFloor = atFloor(); // gets current Floor
        pastFloorNr = 0;  // First init
        idle = true;      // no aquirement
        emptyCounter = 0; // start counting
        
        // Intiate destination array
        goToDest = new int[Building.DEFAULT_FLOORS];
        for (int f=0; f < Building.DEFAULT_FLOORS; f++) {
            clearG2Dest(f); // Reset all goto-destinations of this lift        
        }
        
        controller = LiftController.getInstance();  // Sets the only controller
        people = new ArrayList<Person>();           // Space for persons in Lift
    }

    /** 
     * Checks status and calls methode to act
     * Counts counter down if opend to close the door again
     */
    public void act() {
        updateImage(); //!!!
        switch (status) {
            case LIFT_UP:     goingUp();        break;
            case LIFT_DOWN:   goingDown();      break;
            case LIFT_STOPPED:standingClosed(); break;
            case LIFT_OPEN:   timer++; standingOpen();   break;  // Uses Countdowntimer
        }
    }
    /**
     * Act on status: We are currently going up - perform the next step.
     * Checks if cabine is at a new floor level:
     *   Stopps and checks if somebody awaits lift --> open doors
     */
    private void goingUp()
    {
        moveUp();
        currentFloor = atFloor();
        if(currentFloor != null) { // now standing at a certain floor
            emptyCounter++;
            status = LIFT_STOPPED;
            pastFloorNr = currentFloor.getFloorNr(); // to look back
            
            //Check if correct button is set or anybody wants out -> open
            int butts = currentFloor.getButtons(); // gets status of buttons
            // Check while going further up
            if ((butts == Buttons.UP) || 
                (butts == Buttons.UP_DOWN) || 
                 wantOut() || 
                 isFirst(butts) 
                ) {
                openDoors(Buttons.UP);
                updateImage();
            }
            // Check at top
            if ((currentFloor.getFloorNr() == Building.DEFAULT_FLOORS-1) && 
                (butts == Buttons.DOWN)
               ) {
                openDoors(Buttons.DOWN);
                updateImage(); 
            }
            
        }
    }
    
    /**
     *  Act on status: We are currently going down - perform the next step.
     */
    private void goingDown()
    {
        moveDown();
        currentFloor = atFloor();
        if(currentFloor != null) { // now standing at a certain floor
            status = LIFT_STOPPED; 
            emptyCounter++;
            pastFloorNr = currentFloor.getFloorNr(); // to look back
            
            //Check if correct button is set or anybody wants out -> open
            int butts = currentFloor.getButtons(); // gets status of buttons
            // Check while going further down
            if ((butts == Buttons.DOWN) ||
                (butts == Buttons.UP_DOWN) ||
                 wantOut() ||
                 isFirst(butts) 
               ) {
                openDoors(Buttons.DOWN);
                updateImage(); 
            }
            // Check at buttom
            if ((currentFloor.getFloorNr() == 0) && 
                (butts == Buttons.UP)
               ){
                openDoors(Buttons.UP);
                updateImage(); 
            }
        }
    }
    /**
     *  Act on status: We are currently standing with doors closed - perform the next step.
     */
    private void standingClosed()
    {       
        
        // Checks to continue in selected destination:
        if (direction != Buttons.NONE ) {
         start(direction);  
         return;
        } 
        
        // Check if any aquisation is left out:
        for (int checkFloorNr = 0; checkFloorNr < Building.DEFAULT_FLOORS; checkFloorNr++) {
            int checkDest = getG2Dest(checkFloorNr);
            if ( checkDest  > 0 ) {
                int diff = checkFloorNr - pastFloorNr;
                if (diff != 0) {
                    int dir = Buttons.DOWN;
                    if (diff > 0) {
                        dir = Buttons.UP;
                    } 
                    start(dir);
                    return;
                }
            }
        }
        
        idle = true;  // Lift is ready for new guest
         
        // Last Check if one person is missing: !!!
        /**if (controller.allStopped() && !controller.nonePressed()) {
            for (int checkFloorNr =0; checkFloorNr < Building.DEFAULT_FLOORS; checkFloorNr++) {
                    Floor checkFloor = ((Building)getWorld()).getFloor(checkFloorNr);
                    if ( checkFloor.getButtons()  > Buttons.NONE ) {
                        int diff = checkFloorNr - pastFloorNr;
                        if (diff != 0) {
                            int dir = Buttons.DOWN;
                            if (diff > 0) {
                                dir = Buttons.UP;
                            } 
                            start(dir);
                            return;
                        }
    
                    }
             }
        }**/
    }
    /**
     *  Act on status: We are currently standing with doors open - perform the next step
     *  when timer elapsed -> close doors!
     */
    private void standingOpen() {
        if(timer == TIMER_END) {
            timer = 0;
            closeDoors();
        }
    }

    //=====================================================================================
    
    /**
     * Open the lift doors.
     * @param dir tells in with direction the lift is moving
     */
    public void openDoors(int dir) {
        setImage(openImage); //Open doors
        status = LIFT_OPEN;  //Set a new status
        updateImage();       // Paint doors open
        currentFloor = atFloor(); // Nessecary for first call after Setup/Stopp!
        
        // Replacment for for-loop (-> it.remove() element in loop!)
        Person p;
        Iterator<Person> it = people.iterator();
        //Check if persons want to leave
        while (it.hasNext()) {
            p = it.next();
            if (p.getDestFloorNr() == currentFloor.getFloorNr()) {
                p.leaveLift(currentFloor); // Leave onto destination floor
                decG2Dest(currentFloor.getFloorNr()); // Decrements stored value
                it.remove();        // Go out of lift
                emptyCounter = 0;
                updateImage();  
            }
        }
        
        
        if (getG2Dest(currentFloor.getFloorNr()) != 0) {
            clearG2Dest(currentFloor.getFloorNr()); // all out on this floor
        }

        //Checks if cabine is empty --> no fix direction
        if (isEmpty()) {
         direction = Buttons.NONE;  //!!! 
        }
        
        // Check direction bevor and after people possibly entered
        setDirection(dir); // Checks if at top or bottom
 
        currentFloor.liftArrived(this);   
    }
    /**
     *  Checks amount of people in lift
     *  @return true if no people reside in lift
     */
    public boolean isEmpty() {
        return people.isEmpty();
    }
    
    /**
     * Checks if lift is idle and any buttons are pressed at floor
     * @return true lift is valued idle
     */
    public boolean isFirst(int butts) {
        if (idle) {
            return (butts != Buttons.NONE);
        }
        return false;
    }
    
    /**
     * Checks wether person wants to get out at floor
     */
    public boolean wantOut() {
        if (status == LIFT_STOPPED) {             
            // Replacment for for-loop (-> it.remove() element in loop!)
            Person p;
            Iterator<Person> it = people.iterator();
            //Check if persons want to leave
            while (it.hasNext()) {
                p = it.next();
                if (p.getDestFloorNr() == pastFloorNr) return true;
            }
            return false;
        } else  {
            return false;
        }
    }
    
    /**
     * Close the lift doors.
     */
    public void closeDoors() {
        setImage(closedImage);
        if (people.isEmpty()) {
            clearDirection();
        }
        status = LIFT_STOPPED;
    }
    

    /**
     * person goes into Lift
     */
    public void goInto(Person person) {
        idle = false;
        people.add(person);
        updateImage();
    }
    
    /**
     * Start lift after STOPPED or just set direction correctly
     */
    public void start(int dir){
        //Checks direction first and turns if needed
        setDirection(dir);
        
        // Go if stopped
        if (direction == Buttons.UP) {
          if (status == LIFT_STOPPED) 
              status = LIFT_UP;
        } else if (direction == Buttons.DOWN){
          if (status == LIFT_STOPPED) 
              status = LIFT_DOWN;
        }
        
        // Stops lift if it still runs
        if (emptyCounter > 12) {
          clearDirection();
          idle = true;
          emptyCounter = 0;
        }
    
    }    

    //=====================================================================================

    /**
     * Return  lift status
     */
    public int getStatus() {
        return status;
    }
    /**
     * Return direction status
     */
    public int getDirection() {
        return direction;
    }
    
    /**
     * Sets direction status. Changes direction if reached top or bottom
     * @param dir UP or DOWN
     */
    public void setDirection(int dir) {
         currentFloor =  atFloor();
         if (currentFloor != null) {
              // check for turn
              if ((dir == Buttons.UP) && (currentFloor.getFloorNr() == Building.DEFAULT_FLOORS -1)) {
                  direction = Buttons.DOWN;
              } else if ((dir == Buttons.DOWN) && (currentFloor.getFloorNr() == 0)) {
                  if (controller.nonePressed()) { // If Lift is astray
                      clearDirection();
                  } else {
                      direction = Buttons.UP;
                  }
              } else {
                  direction = dir;
              }   
         }
    }
    
    /**
     * Clears direction of the lift (if nobody has entered)
     */
    public void clearDirection() {
        direction = Buttons.NONE;
        status = LIFT_STOPPED;
    }
    
    /**
     * Resets timer f�r closing doors
     */
    public void restartTimer() {
        timer = 0;    
    }

    /**
     * Increments destination on certain floor
     * @param flrNr floor number of set destination
     */
    public void incG2Dest(int flrNr) {
        goToDest[flrNr]++;
    }
    
    /**
    * Increments destination on certain floor
    * @param flrNr floor number of set destination
    */
    private void decG2Dest(int flrNr) {
        
        if (goToDest[flrNr] > 0) {
            goToDest[flrNr]--;
        }
    }
    
    /**
     * Gets destination amount on certain floor
     * @param flrNr floor number of set destination
     */
    public int getG2Dest(int flrNr) {
        return goToDest[flrNr];
    }
    
    /**
     * Sets destination amount on certain floor
     * @param flrNr floor number of set destination
     */
    public void clearG2Dest(int flrNr) {
        goToDest[flrNr] = 0;
    }
    
    
    /**
    * Are we at a floor? Return floor or null.
    */
    public Floor atFloor() {
       return ((Building)getWorld()).getFloorAt(getY());
    }
    
    /**
     * Gets last floor past by or currentliy waiting
     */
    public int getPastFloorNr(){
        return pastFloorNr;
    }
    
    /**
     * Move a pixel up.
     */
    private void moveUp() {
        setLocation(getX(), getY() - 1);
    }
    
    /**
     * Move a pixel down.
     */
    private void moveDown() {
        setLocation(getX(), getY() + 1);
    }
          
    /**
     * Update this lift's images (open and closed) according to it's state.
     */
    private void updateImage()
    {
        openImage.drawImage(emptyImage, 0, 0);
        if(people.size() > 3)
            openImage.drawImage(personImage, 3, 14);
        if(people.size() > 0)
            openImage.drawImage(personImage, 12, 15);
        if(people.size() > 1)
            openImage.drawImage(personImage, 5, 22);
        if(people.size() > 2)
            openImage.drawImage(personImage, 17, 20);
        paintNumber(openImage);
        paintNumber(closedImage);
        
        // Include or exclude red debugging info on Lift
        //paintDebug( openImage );
        //paintDebug( closedImage );
    }
    
    /**
     * Paint the number of passengers onto the lift's image.
     * @param img This Image  will be updated with a passengernumber
     */
    private void paintNumber(GreenfootImage img)
    {
        img.setColor(Color.WHITE);
        img.fillRect(20, 4, 24, 16);
        img.setColor(Color.BLACK);
        img.drawRect(20, 4, 24, 16);
        
        switch (direction) {
             case LIFT_UP:      img.drawString("^ " + Integer.toString(people.size()), 22, 17); break;
             case LIFT_DOWN:    img.drawString("v " + Integer.toString(people.size()), 22, 17); break;
             case LIFT_STOPPED: img.drawString("- " + Integer.toString(people.size()), 22, 17); break;
        }
    }
    
    /**
     * Paint the number of debug Info onto the lift's image.
     * @param img This Image will be updated with a debug info (red)
     */
    private void paintDebug(GreenfootImage img)
    {
        img.setColor(Color.WHITE);
        img.fillRect(20, 20, 24, 16);
        img.setColor(Color.RED);
        img.drawRect(20, 20, 24, 16);
        
        img.drawString(String.valueOf(emptyCounter), 22, 32);

        /**
        if (idle) {
            img.drawString(" *", 22, 32);
        } else {
            img.drawString(" !", 22, 32);
        }
         switch (status) {
             case LIFT_UP:      img.drawString(" ^", 22, 32); break;
             case LIFT_DOWN:    img.drawString(" v", 22, 32); break;
             case LIFT_STOPPED: img.drawString(" -", 22, 32); break;
             case LIFT_OPEN:    img.drawString("<->", 22, 32); break;
        }**/
    }
}   
    
    