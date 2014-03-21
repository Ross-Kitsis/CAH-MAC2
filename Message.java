

public class Message 
{
	//Processing Parameters
	private int timeSlot;
	private ReservationBean[] FID;
	
	//Sender & reciever parameters
	private int senderID;
	private int receiverID;
	//Message parameters
	private boolean isRetransmit;
	private int msgID;
	private int creationTime;
	
	//Cooperation parameters
	private CoopHeader coHeader;

	private int remaningDuration;
	
	public Message(int msgID, int senderID, int receiverID, int timeSlot,   
			int creationTime, boolean isRetransmit, ReservationBean[] fID,
			CoopHeader coHeader, int remainingDuration) 
	{
		
		
		this.timeSlot = timeSlot;//Clock.CLOCK.getTimeSlot();
		
		//FID = fID;
		this.FID = new ReservationBean[fID.length];
		for(int i = 0; i <fID.length; i++)
		{
			this.FID[i] = new ReservationBean(fID[i].getHolder(),fID[i].getDuration());
			//this.FID[i].setHolder(fID[i].getHolder());
			//this.FID[i].setDuration(fID[i].getDuration());
		}
		
		
		this.senderID = senderID;
		this.receiverID = receiverID;
		
		this.isRetransmit = isRetransmit;
		this.msgID = msgID;
		this.creationTime = creationTime;
		
		this.coHeader = coHeader;
		
		this.remaningDuration = remainingDuration;
	}

	public int getTimeSlot() {
		return timeSlot;
	}

	public void setTimeSent(int timeSent) {
		this.timeSlot = timeSent;
	}

	public ReservationBean[] getFID() {
		return FID;
	}

	public void setFID(ReservationBean[] fID) {
		FID = fID;
	}

	public int getSenderID() {
		return senderID;
	}

	public void setSenderID(int senderID) {
		this.senderID = senderID;
	}

	public int getReceiverID() {
		return receiverID;
	}

	public void setReceiverID(int receiverID) {
		this.receiverID = receiverID;
	}

	public boolean isRetransmit() {
		return isRetransmit;
	}

	public void setRetransmit(boolean isRetransmit) {
		this.isRetransmit = isRetransmit;
	}

	public int getMsgID() {
		return msgID;
	}

	public void setMsgID(int msgID) {
		this.msgID = msgID;
	}

	public int getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(int creationTime) {
		this.creationTime = creationTime;
	}

	public CoopHeader getCoHeader() {
		return coHeader;
	}

	public void setCoHeader(CoopHeader coHeader) {
		this.coHeader = coHeader;
	}
	public int getRemainingDuration()
	{
		return this.remaningDuration;
	}
	
}
