import java.io.Serializable;


public class RDTAck implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private int packet;

	public RDTAck(int packet) {
		super();
		this.packet = packet;
	}

	public int getPacket() {
		return packet;
	}

}
