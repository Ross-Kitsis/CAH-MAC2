


public class CoopHeader
{
	private boolean intention;
	private int indexFailure;
	private int indexRetransmit;
	private int receiverID;
	
	public CoopHeader(boolean intention, int indexFailure, int indexRetransmit, int receiverID)
	{
		this.intention = intention;
		this.indexFailure = indexFailure;
		this.indexRetransmit = indexRetransmit;
		this.receiverID = receiverID;
	}
	public int getReceiverID()
	{
		return this.receiverID;
	}
	/**
	 * 
	 * @return intention - True if node wants to retransmit, false otherwise
	 */
	public boolean getIntention()
	{
		return this.intention;
	}
	/**
	 * 
	 * @return indexFailure - Returns the index where the failure occured
	 */
	public int getIndexFailure()
	{
		return this.indexFailure;
	}
	/**
	 * 
	 * @return indexRetransmit - returns the index where the node wishes to retransmit
	 */
	public int getIndexRetransmit()
	{
		return this.indexRetransmit;
	}
	public boolean equals(CoopHeader c)
	{
		if ((c.getIntention() == this.intention) && (c.getIndexFailure() == this.getIndexFailure()) && (c.getIndexRetransmit() == this.indexRetransmit))
		{
			return true;
		}else
		{
			return false;
		}
			
	}
	/**
	 * Sets coop values to defaults such that the message cannot express cooperation
	 */
	public void voidCoop()
	{
		intention = false;
		indexFailure = -1;
		indexRetransmit = -1;
	}
	
//	public CoopHeader deepCopy()
//	{
//		return new CoopHeader(this.getIntention(), this.getIndexFailure(), this.getIndexRetransmit());
//	}
	public void setIndexFail(int i)
	{
		this.indexFailure = i;
	}
	public void setRetransIndex(int i)
	{
		this.indexRetransmit = i;
	}
	public void setIntention(boolean b)
	{
		this.intention = true;
	}
	
}
