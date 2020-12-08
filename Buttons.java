import greenfoot.Actor;
import greenfoot.GreenfootImage;
import javax.swing.ImageIcon;

/**
 * Lift simulation: There are two buttons on each floor. These buttans are modelled within this class
 * 
 * Act: Checks if one ore both are pressed and aquires a cabine in either direction
 *
 * @author KEL
 * @version <li> V0.4  18.12.2017 old Version
 * @version <li> V0.5  21.08.2017 Refactoring
 * 
 */
public class Buttons extends Actor
{
    // Public static constants for Buttons
    public static final int NONE = 0;
    public static final int UP = 1;
    public static final int DOWN = 2;
    public static final int UP_DOWN = 3; // Only used as returnvalue if both are set!
   
    private GreenfootImage imageNone;
    private GreenfootImage imageUp;
    private GreenfootImage imageDown;
    private GreenfootImage imageUpDown;
    
    // Buttons
    private boolean up;      // True if set
    private boolean down;    // True if set
    private boolean pressed; // True if either pressed
    private int settedDir;   // Values: UP, DOWN, UP_DOWN
    private int floorNumber; // Reference to associated floorNr
    
    /** 
     * Creates two buttons for UP and DOWN request
     * 
     */
    public Buttons(int floorNr) {
        imageUpDown = new GreenfootImage("images/button-up-down.jpg");
        imageUp = new GreenfootImage("images/button-up.jpg");
        imageDown =new GreenfootImage("images/button-down.jpg");
        imageNone = new GreenfootImage("images/button.jpg");
        
        floorNumber = floorNr;  //Presets:
        up = false;
        down = false;
        pressed = false;
        
        setImage(imageNone);  // Init button drawing
    }
    
    /**
    * Aquires (UP- or DOWN-going) lift if pressed 
    */
    public void act() {
        if (pressed) {
              LiftController controller = LiftController.getInstance();
              if (settedDir == UP_DOWN) {
                  controller.aquireAnyLift(floorNumber, UP); 
                  settedDir = DOWN; // DOWN is left
                  // leave pressed for other direction to aquire a cabine
              } else {
                  controller.aquireAnyLift(floorNumber, settedDir); 
                  pressed = false;
              }
        }

    }

    /**
     * Activate one button in certain direction
     * @param dir is either UP or DOWN
     */
    public void press(int dir) {
        set(dir, true); // Note: If dir is not UP or DOWN nothing happens!
        settedDir = dir;
        pressed = true;    // this aquiers a lift on next act
    }
    
    /**
     * Clears one button 
     * @param dir is either UP or DOWN
     */
    public void clear(int dir) {
        set(dir, false); // Note: If dir is not UP or DOWN nothing happens!
    }
    
    /**
     * Sets one button on or off
     * @param direction is either UP or DOWN
     * @param onoff is true for on
     */
    private void set(int direction, boolean onOff) {
        if(direction == UP) {
            up = onOff;
            updateImage();
        }
        else if(direction == DOWN) {
            down = onOff;
            updateImage();
        }
    }
    
    /**
     * Gets the status of the buttons
     * @return int UP, DOWN or UP_DOWN if both are pressed, NONE if none is pressed
     */
    public int getButtons() {
        if(up && down) 
            return UP_DOWN; // Both are pressed
        else if(up)
            return UP;
        else if(down)
            return DOWN;
        else
            return NONE;
    }
    
    
    /**
     * Re-Draws buttons
     */
    private void updateImage() {
        if(up && down) 
            setImage(imageUpDown);
        else if(up)
            setImage(imageUp);
        else if(down)
            setImage(imageDown);
        else
            setImage(imageNone);
    }
}