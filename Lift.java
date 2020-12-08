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
 * 
 */

public class Lift extends Actor
{
    private static final Random random = Building.getRandomizer();

    // Static constants  
    public static final int LIFT_STOPPED = 0;
    public static final int LIFT_UP = 1;
    public static final int LIFT_DOWN = 2;
    public static final int LIFT_OPEN = -1;
    
    private static final int TIMER_END = 250;

    private int status;      // Saves statu:s see above
    private int direction;   // sets directins according to buttons set
    private int pastFloorNr; // Saves past floor number for quick access or test while moving
    private int timer;       // counts time to close door again. May be restarted be a person entering lift
    private ArrayList<Person> people;  // List of the people currently in the lift
    private Floor currentFloor;        // Set to current serving floor; null if none!
    private int[] goToDest;            // Saves all Destination to go in the lift
    private LiftController controller; // Connects to controller
    
    private GreenfootImage openImage;
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
        pastFloorNr = 0;  // First storage
        
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
            status = LIFT_STOPPED;
            pastFloorNr = currentFloor.getFloorNr(); // to look back
            //Check if correct button is set or anybody wants out -> open            
            if ((currentFloor.getButtons() == Buttons.UP) || (currentFloor.getButtons() == Buttons.UP_DOWN) || wantOut() 
                 || isFirst() ) {
                openDoors(Buttons.UP);
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
            pastFloorNr = currentFloor.getFloorNr(); // to look back
            //Check if correct button is set or anybody wants out -> open
            if ((currentFloor.getButtons() == Buttons.DOWN) || (currentFloor.getButtons() == Buttons.UP_DOWN) || wantOut() 
                 || isFirst() ) {
                openDoors(Buttons.DOWN);
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
        if (direction != Buttons.NONE) {
             start(direction);  
             return;
        }
        
        // Check if any aquisation is left out:
        for (int checkFloorNr =0; checkFloorNr < Building.DEFAULT_FLOORS; checkFloorNr++) {
                    int checkDest =getG2Dest(checkFloorNr);
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
        
        // Last Check if one person is missing:
        if (controller.allClosed()) {
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
        }
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
     * 
     */
    public boolean isEmpty() {
        return people.isEmpty();
    }
    /**
     * 
     */
    public boolean isFirst() {
        if (people.isEmpty()) {
            for (int f=0; f < Building.DEFAULT_FLOORS; f++) {
                  if (getG2Dest(f) > 0) {
                      return false;    
                  };    
            }
            return (currentFloor.getButtons() != Buttons.NONE);
        }
        return false;
    }
    
    /*
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
                  direction = Buttons.UP;
              } else {
                  direction = dir;
              }   
         }
    }
    /*
     * Clears direction of the lift (if nobody has entered)
     */
    public void clearDirection() {
        direction = Buttons.NONE;
    }
    /*
     * Resets timer für closing doors
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
    public void decG2Dest(int flrNr) {
        goToDest[flrNr]--;
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

        switch (status) {
             case LIFT_UP:      img.drawString(" ^", 22, 32); break;
             case LIFT_DOWN:    img.drawString(" v", 22, 32); break;
             case LIFT_STOPPED: img.drawString(" -", 22, 32); break;
             case LIFT_OPEN:    img.drawString("<->", 22, 32); break;
        }
    }
}   
    
    