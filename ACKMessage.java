

/**
 * A simple class which represents a short message containing only the ID of the node from
 * which a retransmission is expected
 * @author Ross Kitsis
 *
 */
public class ACKMessage 
{
	private int ackID;

	public ACKMessage(int ackID) 
	{
		this.ackID = ackID;
	}

	public int getAckID() 
	{
		return ackID;
	}
	
}
