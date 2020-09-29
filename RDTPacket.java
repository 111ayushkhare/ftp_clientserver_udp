import java.io.Serializable;


public class RDTPacket implements Serializable {

	/**
	 * SerialVersionUID is used to ensure that during deserialization
	 * the same class (that was used during serialize process) is loaded
	 */
	private static final long serialVersionUID = 1L;

	public int seq;
	
	public byte[] data;
	
	public boolean last;

	/**
	 * Initialize sequence number,
	 * byte array data and
	 * boolean variable last if the packet is last one or not
	 */
	public RDTPacket(int seq, byte[] data, boolean last) {
		super();
		this.seq = seq;
		this.data = data;
		this.last = last;
	}

	/**
	 * Get the sequence number of the packet
	 */
	public int getSeq() {
		return seq;
	}

	/**
	 * Set the sequence number of the packet
	 */
	public void setSeq(int seq) {
		this.seq = seq;
	}

	/**
	 * Get the data in bytes array
	 */
	public byte[] getData() {
		return data;
	}

	/**
	 * Set the data to byte array 
	 */
	public void setData(byte[] data) {
		this.data = data;
	}

	/**
	 * Get method to check if packet is last one
	 */
	public boolean isLast() {
		return last;
	}

	/**
	 * Set the boolean variable last to true 
	 * if the packet is the last one
	 */
	public void setLast(boolean last) {
		this.last = last;
	}
	
}
