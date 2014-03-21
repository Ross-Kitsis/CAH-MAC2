

/**
 * A timeslotbean to hold the time recieved and the holder of the slot
 * @author Ross Kitsis
 *
 */
public class ReservationBean 
{
	private int holder;
	private int duration;
	Clock c = Clock.CLOCK;
	
	
	public ReservationBean(int holder, int duration) 
	{
		this.holder = holder;
		this.duration = duration;
	}
	public int getHolder() {
		return holder;
	}
	public void setHolder(int holder) {
		this.holder = holder;
	}
	public int getDuration() {
		return duration;
	}
	public void setDuration(int duration) {
		this.duration = duration;
	}
	public void decrementDuration()
	{
		this.duration--;
	}
	
	
}
