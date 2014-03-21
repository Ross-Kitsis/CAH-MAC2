

public class MemoryBean 
{
	private Message m;
	private int frameCreated;
	/**
	 * Returns the message stored in memory
	 * @return
	 */
	public Message getM() {
		return m;
	}
	/**
	 * Sets the message stored in memory
	 * @param m
	 */
	public void setM(Message m) {
		this.m = m;
	}
	/**
	 * Gets the frame in which the message was created
	 * @return
	 */
	public int getFrameCreated() {
		return frameCreated;
	}
	/**
	 * Sets the frame in which the message was created
	 * @param frameCreated
	 */
	public void setFrameCreated(int frameCreated) {
		this.frameCreated = frameCreated;
	}
	/**
	 * Contrauctor for the memory = bean
	 * @param m
	 * @param frameCreated
	 */
	public MemoryBean(Message m, int frameCreated) {
		super();
		this.m = m;
		this.frameCreated = frameCreated;
	}

}
