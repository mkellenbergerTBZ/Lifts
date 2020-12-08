import greenfoot.*;
import java.util.*;
import javax.swing.ImageIcon;

/**
 * Lift simulation: Floor
 * 
 * Setup: Sets a floor with PopUp-Counter and two buttons
 * Act: Creates a person at Clicktarget on this floor
 * 
 * @author KEL
 * @version <li> V0.4  18.12.2017 old Version
 * @version <li> V0.5  21.08.2017 Refactoring
 * 
 */

public class Floor extends Actor
{
    private static final Random random = Building.getRandomizer(); // gets random from Buildingclass
    
    private int floorNumber;
    private Buttons buttons;
    private LiftController controller;
    private ArrayList<Person> people;  // List of the people currently waiting on this floor
    
    /**
     * This constructor sets a floor number and makes an array of the people on the elevator.
     * @param floorNumber sets number of this floor
     */    
    public Floor(int floorNumber)
    {
        this.floorNumber = floorNumber;
        people = new ArrayList<Person>();
        controller = LiftController.getInstance();
    }
    /**
    *  Adds floor and Bottons to the world
    */
    public void addedToWorld(World world)
    {
        buttons = new Buttons(floorNumber);
        world.addObject(buttons, getX()+78, getY());
    }
    
    /**
     * Do the regular simulation action. For a floor, that is: produce a new
     * person on every click.
     */
    public void act()
    {
        checkMouseClick();
        paintFloorPopUp(getImage());
    }
    /**
     * Click on floor adds a new person with target floor.
     */
    private void checkMouseClick()
    {
        if (Greenfoot.mouseClicked(this)) 
        {
            Person p = new Person(this);
            getWorld().addObject(p, Greenfoot.getMouseInfo().getX(),getY() + 8);
            people.add(p); // Puts person on floor --> pushButton
        }
    }
    /**
    * Marks the amount of people waiting at the current floor.
    */
    private void paintFloorPopUp(GreenfootImage img)
    {
        img.setColor(Color.WHITE);
        img.fillRect(20, 4, 24, 16);
        img.setColor(Color.BLACK);
        img.drawRect(20, 4, 24, 16);
        img.drawString(Integer.toString(people.size()), 22, 17);
    }    
    
    //====================================================================================
    
    
    /**
    * Return this floor's number.
    */
    public int getFloorNr()
    {
       return floorNumber;
    }
    
    /**
     * Press a button to call a lift to this floor. (see act() in Buttons)
     * @param direction is either UP or DOWN
     */
    public void pushButton(int direction)
    {
        buttons.press(direction);
    }
    
    /**
     * Reset a button.
     * @param direction is either UP or DOWN
     */
    public void clearButton(int direction)
    {
        buttons.clear(direction);
    }
    
    /**
    * Gets the status of the buttons
    * @return int UP, DOWN or UP_DOWN if both are pressed, NONE if none is pressed
    */
    public int getButtons() {
        return buttons.getButtons();
    }
    
    
    /**
     * Enters this floor's from lift.
     * 
     * @param p Person who enters the floor leaving lift 
     * @return int new floor-position
     * 
     * @see Person#leaveLift
     */
    public int enter(Person p)
    {
        people.add(p);
        paintFloorPopUp(getImage()); // show up again         
        return getY()+10; // Pass correct position for next location
    }
    
    /**
     * Leaves this floor's either to lift or exit. !!!
     * @param p Person who leaves the floor    
     */
    public void leave(Person p)
    {
        people.remove(p);
        paintFloorPopUp(getImage());
    }    
    
    /**
     * Action: Move all correct people towards open lift.
     * @param lift Lift has arrived and now gets all persons in assoiated direction
     */
    public void liftArrived(Lift lift)
    {
        // Replacment for for-loop (-> it.remove() element in loop!)
        Person p;
        Iterator<Person> it = people.iterator();
        while (it.hasNext()) {
           p = it.next();
           // if lift is without direction set first person to direct it
           if (lift.getDirection() == Buttons.NONE  || lift.isEmpty()) { 
              if ( p.isGoingup()) {
                lift.setDirection(Buttons.UP);
              } else if (p.isGoingdown())  {
                lift.setDirection(Buttons.DOWN);  
              }
           }
           
           // get all persons into lift with same directions
           if (p.isGoingup() && lift.getDirection() == Buttons.UP) { 
               p.enterLift(lift);
               it.remove(); // Remove from people list
               paintFloorPopUp(getImage());
           } else if (p.isGoingdown() && lift.getDirection() == Buttons.DOWN) {
               p.enterLift(lift);
               it.remove(); // Remove from people list
               paintFloorPopUp(getImage());
           }
        }
       clearButton(lift.getDirection()); // Clears button on floor
    }
}