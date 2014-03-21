

import java.util.*;

public class Node 
{
	//Node parameters
	private int ID; //Unique ID of the node
	private int numSlots;
	private double PoS;
	private int range;
	private int lane;
	private int xlanePosition;
	private int ylanePosition;
	
	private List<Integer> OHS = new ArrayList<Integer>();
	private List<Integer> THS = new ArrayList<Integer>();
	
	Clock clock = Clock.CLOCK;

	
	//Timeslot variables
	private int reservation = -1; // -1 is default if have no reservation
	private int originalReservation; //Backup reservation for when resetting the node
	private int originalReservationDuration; //Backup original duration when resetting node
	private ReservationBean[] timeSlots;
	private ReservationBean[] FI;
	
	
	private ReservationBean[] originalTimeSlots;
	private ReservationBean[] originalFI;
	
	Random rng = new Random();
	
	//Retransmit parameters
	private boolean intention = false;
	private int retransIndex = -1;
	private int failIndex = -1;
	private int retransID = -1;
	private int frameRetransmit = -1;
	private Message toCooperate = null;
	
	//Utilization statistics
	private int numSent = 0;
	private int success = 0;
	
	//Clock-collision parameters
	private int lastRecMsgTime = -1;
	private boolean decSuccess = true;
	private Message mayRemove = null; //Msg to remove from buffer if have a collision
	
	//Map of the messages received and the message in memory
	private Map<Integer,ArrayList<MemoryBean>> rMsgs = new HashMap<Integer,ArrayList<MemoryBean>>();
	
	//Retransmission Ack parameters
	private int ackSlot = -1;
	private int ackID = -1;
	
	//Message buffer to deal with collisions
	private List<Message> buffer = new ArrayList<Message>();
	
	//Parameters for reserving a slot
	private int waitCount = 0;
	private int maxReservationDuration;
	private int remainingReservationTime; //Remaining time until need to contend again
	
	//Jam parameters
	private boolean haveJam = false;
	
	public Node(int ID, int numSlots, double PoS, int range,int xlanePosition, int ylanePosition, int maxReservationDuration)
	{
		this.ID = ID; //No Change
		this.numSlots = numSlots; //No Change
		this.PoS = PoS; //No Change
		this.range = range; //No Change
		this.xlanePosition = xlanePosition; //No Change
		this.ylanePosition = ylanePosition; //No Change
		this.maxReservationDuration = maxReservationDuration; //No Change
		this.originalReservationDuration = rng.nextInt(this.maxReservationDuration) + 1;
		
		//******* Will Change, need to set back after a trial
		
		this.remainingReservationTime = originalReservationDuration;
		timeSlots = new ReservationBean[this.numSlots];
		FI = new ReservationBean[this.numSlots];
		for(int i = 0; i < numSlots; i++)
		{
			timeSlots[i] = new ReservationBean(-1, this.maxReservationDuration); //Set all timeslots to -1
			FI[i] = new ReservationBean(-1, this.maxReservationDuration);
		}
		
		
		originalTimeSlots = new ReservationBean[this.numSlots];
		originalFI = new ReservationBean[this.numSlots];
		
		for(int i = 0; i < numSlots; i++)
		{
			originalTimeSlots[i] = new ReservationBean(-1, this.maxReservationDuration); //Set all timeslots to -1
			originalFI[i] = new ReservationBean(-1, this.maxReservationDuration);
			//System.out.println(originalTimeSlots[i].getHolder());
			//Scanner in = new Scanner(System.in);
			//in.nextLine();
		}
	}
	/**
	 * gets the Nodes ID
	*/
	public int getID() {
		return ID;
	}
	public void setID(int iD) {
		ID = iD;
	}
	public int getRemainingReservationDuration()
	{
		return this.remainingReservationTime;
	}
	public int getRange() {
		return range;
	}
	public int getLane() {
		return lane;
	}
	public int getXLanePosition() {
		return xlanePosition;
	}
	public int getyLanePosition() {
		return ylanePosition;
	}
	public int getNumSlots() {
		return numSlots;
	}
	public void setNumSlots(int numSlots) {
		this.numSlots = numSlots;
	}
	public double getPoS() {
		return PoS;
	}
	public void setPoS(double poS) {
		PoS = poS;
	}
	
	public void addToOHS(int addID)
	{
		this.OHS.add(addID);
	}
	public List<Integer> getOHS()
	{
		return this.OHS;
	}
	public void addToTHS(int addID)
	{
		this.THS.add(addID);
	}
	public List<Integer> getTHS()
	{
		return this.THS;
	}
	public int getReservation()
	{
		return this.reservation;
	}
	public void setReservation(int reservation)
	{
		this.originalReservation = reservation;
		this.reservation = reservation;
		this.timeSlots[reservation].setHolder(this.ID);
		this.timeSlots[reservation].setDuration(this.remainingReservationTime);
		
		
		this.FI[reservation].setHolder(this.ID);
		this.FI[reservation].setDuration(this.remainingReservationTime);
	}
	/**
	 * Only called during model initialization to initialize the FID values
	 */
	public void setFI(int index, int ID, int duration)
	{
		this.FI[index].setHolder(ID);
		this.FI[index].setDuration(duration);
	}
	/**
	 * Only called during model initialization to set reserved timeslots
	 * @param index
	 * @param ID
	 */
	public void setTimeSlotReservation(int index, int ID, int duration)
	{
		this.timeSlots[index].setHolder(ID);
		this.timeSlots[index].setDuration(duration);
	}
	
	public ReservationBean[] getTimeSlots()
	{
		return this.timeSlots;
	}
	public ReservationBean[] getFI()
	{
		return this.FI;
	}
	/**
	 * Sends an acknowledgement to a node which wishes to retransmit a message for the node
	 * The message contains the ID of the node which is permitted to send
	 * @return
	 */
	public ACKMessage sendACK()
	{
		int timeSlot = clock.getTimeSlot();
		if(timeSlot == this.ackSlot)
		{
			return new ACKMessage(ackID);
		}
		return null;
	}
	/**
	 * Recieves retransmit acknowledgement and proceeds tih retransmit or aborts
	 * @param m
	 */
	public void recACK(ACKMessage m)
	{
		if(this.rng.nextDouble() <= this.PoS)
		{
			if(this.ID != m.getAckID() && this.intention)
			{
				this.intention = false;
				this.failIndex = -1;
				this.retransIndex = -1;
				this.frameRetransmit = -1;
			}
		}
	}
	/**
	 * Returns true if the passed ID is in the nodes OHS, false otherwise
	 * @param ID
	 * @return
	 */
	private boolean containedInOHS(int ID)
	{
		boolean contained = false;
		for(int i: this.OHS)
		{
			if(i == ID)
			{
				contained = true;
				break;
			}
		}
		return contained;
	}
	/**
	 * Returns true if the passed ID is in the nodes THS, false otherwise
	 * @param ID
	 * @return
	 */
	private boolean containedInTHS(int ID)
	{
		boolean contained = false;
		for(int i: this.THS)
		{
			if(i == ID)
			{
				contained = true;
				break;
			}
		}
		return contained;
	}
	private void clearSuccessFromCache(Message m)
	{
		ReservationBean[] mFI = m.getFID();
		ArrayList<MemoryBean> r = rMsgs.get(m.getSenderID());
		if(r != null)
		{
			for(int i = 0; i < r.size(); i++)
			{
				Message possibleRemove = r.get(i).getM();
				if((mFI[possibleRemove.getTimeSlot()] == this.FI[possibleRemove.getTimeSlot()]) && (r.get(i).getFrameCreated() == clock.getFrame() -1) && this.FI[possibleRemove.getTimeSlot()].getHolder() != -1)
				{
					//System.out.println("removing");
					r.remove(i);
				}
			}
		}
	}

	/**
	 * removed the passed niehgbour ID from relationship tables
	 * @param ID
	 */
	public void removeNeighbour(int nID)
	{
		//this.OHS.remove(ID);
		for(int i = 0; i < OHS.size(); i++)
		{
			if(OHS.get(i) == nID)
			{
				OHS.remove(i);
				break;
			}
		}
		//this.THS.remove(ID);
		for(int i = 0; i < THS.size(); i++)
		{
			if(THS.get(i) == nID)
			{
				THS.remove(i);
				break;
			}
		}
		for(int i = 0; i < this.numSlots; i++)
		{
			if(this.timeSlots[i].getHolder() == nID)
			{
				this.timeSlots[i].setHolder(-1);
				this.FI[i].setHolder(-1);
			}
		}
	}
	/**
	 * Clears cached messages in memory greater than the passed value
	 */
	public void clearStaleCache(int maxStored)
	{
		int currentFrame = clock.getFrame();
		Set<Integer> keys = rMsgs.keySet();
		Iterator<Integer> it = keys.iterator();
		while(it.hasNext())
		{
			int key = (int) it.next();
			List<MemoryBean> memory = rMsgs.get(key);
			for(int i = 0; i < memory.size(); i++)
			{
				int memoryCreationFrame = memory.get(i).getFrameCreated();
				int diff = currentFrame - memoryCreationFrame;
				if(diff > maxStored)
				{
					memory.remove(i);
				}
			}
		}
	}
	/**
	 * Clears a nodes reservation and its OHS and THS
	 */
	public void clearReservation()
	{
	//	System.out.println("Node " + this.ID + " reservation cleared");
		this.reservation = -1;
		for(int i = 0; i < this.numSlots; i++)
		{
			this.timeSlots[i].setHolder(-1);
			this.FI[i].setHolder(-1);;
		}
		this.OHS = new ArrayList<Integer>();
		this.THS = new ArrayList<Integer>();
	}
	public int getNumSent()
	{
		return this.numSent;
	}
	public int getSuccess()
	{
		return this.success;
	}
	public Message sendMsg()
	{
		Message toSend = null;
		if(this.reservation == -1)
		{
			attemptReservation();
			//System.out.println("No reservation " + this.ID);
		}
		
		if(this.reservation == clock.getTimeSlot() && this.OHS.size() > 0)
		{
			int msgID = Integer.parseInt(this.ID + "" + clock.getTime());
			int senderID = this.ID;
			int receiverID = this.OHS.get(rng.nextInt(this.OHS.size()));
			int timeSlot = clock.getTimeSlot();
			int creationTime = clock.getTime();
			boolean isRetransmit = false;
			ReservationBean[] Res = FI;
			CoopHeader c = null;
			int remainingDuration = this.remainingReservationTime;
			toSend = new Message(msgID, senderID, receiverID, timeSlot, creationTime, isRetransmit, Res, c, remainingDuration);
			
			this.numSent++;
		}
		return toSend;
	}
	public void addMessageToBuffer(Message m)
	{
		//System.out.println("Node ID " + this.ID + " Receiver ID: " + m.getReceiverID() + " Sender ID: " + m.getSenderID());
		this.buffer.add(m);
	}
	public void processBuffer()
	{
		int bufferSize = this.buffer.size();
		if(bufferSize == 1)
		{
			this.recieveMsg(buffer.get(0));
			//System.out.println("Buffer size 1");
		}else if(bufferSize > 1)
		{
			for(int i = 0; i < buffer.size(); i++)
			{
				//Scanner s = new Scanner(System.in);
				System.out.println("Collision: Node " + this.ID + " Message sender: " + buffer.get(i).getSenderID() + " Receiver id: " + buffer.get(i).getReceiverID());
				//s.nextLine();
			}
			this.haveJam = true;
		}		
		
		buffer.clear();
	}
	public void recieveMsg(Message m)
	{
		double didReceive = rng.nextDouble();
		/*******TEMPORARY**********/
		//didReceive = 0.0;
		/*******End Temp**********/
		int senderID = m.getSenderID();
		int receiverID = m.getReceiverID();
		boolean isRetransmit = m.isRetransmit();
		int senderRemainingDuration = m.getRemainingDuration();
		
		
		if(didReceive <= PoS)
		{
			if(isRetransmit == false)
			{
				updateNeighbourTables(m);
				
				if(receiverID == this.ID)
				{
					this.success++;
					//System.out.println("Node " + this.ID + " got msg for it");
				}
			}
		}
	}
	public void updateNeighbourTables(Message m)
	{
		ReservationBean[] messageFI = m.getFID();
		
		this.timeSlots[clock.getTimeSlot()].setHolder(m.getSenderID());
		this.timeSlots[clock.getTimeSlot()].setDuration(m.getRemainingDuration());
		this.FI[clock.getTimeSlot()].setHolder(m.getSenderID());
		this.FI[clock.getTimeSlot()].setDuration(m.getRemainingDuration());
	
		
		if(!this.OHS.contains(m.getSenderID()))
		{
			this.OHS.add(m.getSenderID());
		}
		
		for(int i = 0; i < this.numSlots; i++)
		{
			if(this.timeSlots[i].getHolder() == -1 && messageFI[i].getHolder() != -1)
			{
//				Scanner in = new Scanner(System.in);
//				in.nextLine();
				this.timeSlots[i].setHolder(messageFI[i].getHolder());
				this.timeSlots[i].setDuration(messageFI[i].getDuration());
				//System.out.println("HERE");
			}
			if(!this.THS.contains(messageFI[i].getHolder()) && messageFI[i].getHolder() != -1)
			{
				this.THS.add(messageFI[i].getHolder());
			}
		}
		
		if(this.reservation == -1)
		{
			/***********************
			System.out.println("R: " + this.ID + " S: " + m.getSenderID());
			System.out.print("S: ");
			for(int i = 0; i < this.numSlots; i++)
			{
				System.out.print(messageFI[i].getHolder() + " , ");
			}
			System.out.println();
			System.out.print("R: ");
			for(int i = 0; i < this.numSlots; i++)
			{
				System.out.print(this.timeSlots[i].getHolder() + " , ");
			}
			System.out.println("\n\n");
			*************************/
//			
//			Scanner input = new Scanner(System.in);
//			input.nextLine();
		}
		/*************************
		System.out.println("R: " + this.ID + " S: " + m.getSenderID());
		System.out.print("S: ");
		for(int i = 0; i < this.numSlots; i++)
		{
			System.out.print(messageFI[i].getHolder() + " , ");
		}
		System.out.println();
		System.out.print("R: ");
		for(int i = 0; i < this.numSlots; i++)
		{
			System.out.print(this.timeSlots[i].getHolder() + " , ");
		}
		System.out.println("\n\n");
		*************************/
	}
	public void updateNodeDuration()
	{
		this.remainingReservationTime--;
		for(int i = 0; i < this.numSlots; i++)
		{
			this.timeSlots[i].decrementDuration();
			this.FI[i].decrementDuration();
		}
	}
	public void clearExpiredReservations()
	{
		//NEED TO IMPLEMENT
		for(int i = 0; i < this.numSlots; i++)
		{
			if(this.timeSlots[i].getHolder() != -1 && this.timeSlots[i].getDuration() <= 0)
			{
				this.timeSlots[i].setHolder(-1);
			}
			
			if(this.FI[i].getHolder() != -1 && this.FI[i].getDuration() <= 0)
			{
				this.FI[i].setHolder(-1);
			}
		}
		
		if(this.remainingReservationTime <= 0 && this.reservation >= 0)
		{
			System.out.println("NODE "+ ID +" RESERVATION EXPIRED, OLD RESERVATION IS " + this.reservation);
			this.reservation = -1;
//			
//			Scanner in = new Scanner(System.in);
//			in.nextLine();
			
			for(int i = 0; i < numSlots; i++)
			{
				timeSlots[i] = new ReservationBean(-1, this.maxReservationDuration); //Set all timeslots to -1
				FI[i] = new ReservationBean(-1, this.maxReservationDuration);
			}
		}
	}
	public void attemptReservation()
	{
		if(waitCount < this.numSlots)
		{
			waitCount++;
		}else
		{
			List<Integer> possible = new ArrayList<Integer>();
			for(int i = 0; i < this.numSlots; i++)
			{
				if(timeSlots[i].getHolder() == -1)
				{
					possible.add(i);
				}
			}
			if(possible.size() > 0 && this.reservation == -1)
			{
				this.reservation = possible.get(rng.nextInt(possible.size()));
				this.remainingReservationTime = this.maxReservationDuration;
				this.waitCount = 0;
				
				this.timeSlots[reservation].setHolder(this.ID);
				this.timeSlots[reservation].setDuration(this.maxReservationDuration);
				
				this.FI[reservation].setHolder(this.ID);
				this.FI[reservation].setDuration(this.maxReservationDuration);
				
				System.out.println("NODE " + ID + " RESERVING SLOT " + reservation);
//				Scanner input = new Scanner(System.in);
//				input.nextLine();
			}
		}
	}
	public JamMessage sendJam()
	{
		if(this.haveJam == true)
		{
			this.haveJam = false;
			System.out.println("HAVE A JAM **********************************************");
			return new JamMessage(this.ID);
		}else
		{
			return null;
		}
	}
	public void recJam(JamMessage j)
	{
		if(clock.getTimeSlot() == this.reservation)
		{
			//Have a JAM during my timeslot
			System.out.println("Node " + ID + " has JAM at timeslot " + clock.getTimeSlot() + " clearing and recontending");
			this.reservation = -1;
			this.remainingReservationTime = 0;
			this.clearExpiredReservations();
//			Scanner in = new Scanner(System.in);
//			in.nextLine();
		}else
		{
			this.FI[clock.getTimeSlot()].setHolder(-1);
			this.timeSlots[clock.getTimeSlot()].setHolder(-1);
		}
	}
	public void reset()
	{
		this.reservation = originalReservation;
		this.remainingReservationTime = originalReservationDuration;
		timeSlots = new ReservationBean[this.numSlots];
		FI = new ReservationBean[this.numSlots];
		for(int i = 0; i < numSlots; i++)
		{
			timeSlots[i] = new ReservationBean(originalTimeSlots[i].getHolder(), originalTimeSlots[i].getDuration()); //Set all timeslots to -1
			FI[i] = new ReservationBean(originalFI[i].getHolder(), originalFI[i].getDuration());
		}	
		
		this.numSent = 0;
		this.success = 0;
		
	}
	public void saveValues()
	{
	//	System.out.println(this.FI[1].getHolder() + " " + this.FI[1].getDuration());
	//	System.out.println(this.timeSlots[1].getHolder() + " " + this.timeSlots[1].getDuration());
	//	System.out.println(this.originalFI);
		
		
		for(int i = 0; i < this.numSlots; i++)
		{
			originalTimeSlots[i] = new ReservationBean(timeSlots[i].getHolder(), timeSlots[i].getDuration()); //Set all timeslots to -1
			originalFI[i] = new ReservationBean(FI[i].getHolder(), FI[i].getDuration());
		}
		
	}
}
