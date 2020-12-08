import greenfoot.*;
import java.util.Random;

/**
 *   Creates the simulation world with 6 floors and 3 lift cabines
 *   
 *   Note: Has a public static randomizer method!
 * 
 * @author KEL
 * @version <li> V0.4  18.12.2017 old Version
 * @version <li> V0.5  21.08.2017 Refactoring
 * @version <li> V0.6  07.01.2019 Java-Doc completed
 * 
 */

public class Building extends World
{
    public static final int RESOLUTION = 1;
    public static final int DEFAULT_LIFTS = 3;  // 0-2
    public static final int DEFAULT_FLOORS = 6; // 0-5
    
    private static Random random = new Random();
    
    private Floor[] floors;             // Default number of floors
    private LiftController controller;  // Only one controller
    
    /**
     * Standard Constructor:
     * Create a building with default number of lifts and stories. this executes whole blocks of code from the STORIES and LIFTS methods.
     */
    public Building() 
    {
        this(DEFAULT_FLOORS, DEFAULT_LIFTS);
    }
    /**
     * Constructor:
     * Create a building with specified number of lifts and stories. This creates a world size based on the number of lifts and stories of the building.
     *
     * @param floors numbers of floors to build
     * @param lifts  numbers of lifts to add
     */
    public Building(int floors, int lifts)
    {
        //super(120 + lifts * 28, stories * 36 + 20);
        super(240 + lifts * 56, floors * 72 + 40, 1);
        
        //setBackgroundImage("brick.jpg");
        GreenfootImage background = new GreenfootImage("sandstone.jpg");
        setBackground(background);
        controller = LiftController.getInstance();
        
        createFloors(floors);
        createLifts(lifts, floors);
    }
    /**
     * Create all the floors in the building.
     *
     * @param numberOfFloors number of floors to be build
     */
    public void createFloors(int numberOfFloors)
    {
        floors = new Floor[numberOfFloors];
        for(int i=0; i<numberOfFloors; i++) {
            floors[i] = new Floor(i);
            addObject(floors[i], 100, (numberOfFloors-1-i) * 72 + 55);
        }
    }
    /**
     * Create all the lifts in the building.
     *
     * @param numberOfLifts number of lifts to be build
     * @param numberOfFloors number of floors to be build
     */
    public void createLifts(int numberOfLifts, int numberOfFloors)
    {
        GreenfootImage background = getBackground();
        background.setColor(new Color(255, 255, 255, 100));

        for(int i=0; i<numberOfLifts; i++) {
            background.fillRect(218 + i * 56, 18, 54, (numberOfFloors)*72 + 2);
            Lift lift = new Lift();
            addObject(lift, 245 + i * 56, (numberOfFloors-1)*72 + 55);
            controller.addLift(i, lift);
        }
    }
    
    /**
     * Sets a method name to create random number.
     */
    public static Random getRandomizer()
    {
        return random;
    }

    //===================================================================================

     /**
     * Calcs top floor number.
     * 
     * @return int top floor's number
     */
    public int getTopFloor()
    {
        return floors.length - 1;
    }
    
    /**
     * Creates a random floor number.
     *  
     * @return valid random floor number
     */
    public int getRandomFloorNr()
    {
        return random.nextInt(floors.length);
    }

    /**
     * Return the floor at a given screen cell y-coordinate.
     * If this cell is not the exact height of an existing floor, return null.
     * 
     * @param y y-coordinate to spot
     * @return the spotted floor-object
     */
    public Floor getFloorAt(int y)
    {
        for(int i=0; i<floors.length; i++) {
            if(floors[i].getY() == y) {
                return floors[i];
            }
        }
        return null;
    }
    /**
     * Return the floor at given number
     * 
     * @param floorNr valid number of selected floor
     * @return the selcted floor-object
     */ 
    public Floor getFloor(int floorNr)
    {
        return floors[floorNr];
    }
}