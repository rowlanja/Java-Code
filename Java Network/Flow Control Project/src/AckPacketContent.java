

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;

/**
 * Class for packet content that represents acknowledgements
 * 
 */
public class AckPacketContent implements PacketContent {
	
	String info;
	String type;
	/**
	 * Constructor that takes in information about a file.
	 * @param filename Initial filename.
	 * @param size Size of filename.
	 */
	AckPacketContent(String info) {
		type= "ACKPACKET";
		this.info = info;
	}

	/**
	 * Constructs an object out of a datagram packet.
	 * @param packet Packet that contains information about a file.
	 */
	protected AckPacketContent(ObjectInputStream oin) {
		try {
			type= "ACKPACKET";
			info= oin.readUTF();
		} 
		catch(Exception e) {e.printStackTrace();}
	}

	/**
	 * Writes the content into an ObjectOutputStream
	 *
	 */
	protected void toObjectOutputStream(ObjectOutputStream oout) {
		try {
			oout.writeUTF(info);
		}
		catch(Exception e) {e.printStackTrace();}
	}


	
	/**
	 * Returns the content of the packet as String.
	 * 
	 * @return Returns the content of the packet as String.
	 */
	public String toString() {
		return "ACK:" + info;
	}
	
	/**
	 * Returns the info contained in the packet.
	 * 
	 * @return Returns the info contained in the packet.
	 */
	public String getPacketInfo() {
		return info;
	}

	@Override
	public DatagramPacket toDatagramPacket() {
		// TODO Auto-generated method stub
		return null;
	}
}