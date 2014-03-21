
/**
 * A clock class which keeps track of the frame number, the timeslot and the total simulation time
 * @author Ross Kitsis
 *
 */
public class Clock 
{
	private int time = 0;
	private int numSlots;
	public final static Clock CLOCK = new Clock();
	
	private Clock()
	{
		
	}
	
	/**
	 * Sets the number of timeslots
	 * @param numSlots
	 */
	public void setNumSlots(int numSlots)
	{
		this.numSlots = numSlots;
	}
	/**
	 * Sets the clock forward 1 unit
	 */
	public void tick()
	{
		time++;
	}
	/**
	 * returns the current frame number
	 * @return
	 */
	public int getFrame()
	{
		return this.time/this.numSlots;
	}
	/**
	 * Returns the current timeslot in the frame
	 * @return
	 */
	public int getTimeSlot()
	{
		return this.time%this.numSlots;
	}
	/**
	 * Returns the simulation time
	 * @return
	 */
	public int getTime()
	{
		return this.time;
	}
	/**
	 * Resets the clock by setting it to 0
	 */
	public void reset()
	{
		this.time = 0;
	}
}
