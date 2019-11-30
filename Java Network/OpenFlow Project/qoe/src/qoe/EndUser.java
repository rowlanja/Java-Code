package qoe;

/**
 * 
 */
import java.net.DatagramSocket;
import java.awt.Toolkit;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;

/**
 *
 * Client class
 * 
 * An instance accepts user input
 *
 */
public class EndUser extends Node {
	static final int DEFAULT_SRC_PORT = 50000;
	static final int DEFAULT_DST_PORT = 50050;
	static final int WORKER_PORT1 = 50005;
	static final int WORKER_PORT2 = 50005;

	static final String DEFAULT_DST_NODE = "localhost";
	static final String DEFAULT_DST_SERVER = "localhost";

	int NumOfPackets;
	Terminal terminal;

	InetSocketAddress dstAddress;
	Toolkit toolkit;
	String stringtosend;
	static int interval;
	DatagramPacket packet;
	String identity;
	String tiedPort;
	int homePort;

	EndUser(Terminal terminal1, String dstHost, int dstPort1, int srcPort, String identity, String tiedPort) {
		try {
			this.terminal = terminal1;

			dstAddress = new InetSocketAddress(dstHost, 50010);
			socket = new DatagramSocket(srcPort);
			listener.go();
			this.identity = identity;
			this.homePort = srcPort;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized void start() throws Exception {
		try {
			//String EU = "EUPING " + this.identity + " " + Integer.toString(this.homePort)  + " " + this.tiedPort;
			
			String userReceiving = terminal.read("Enter a End User followed by message : ");
			String combine = "Msg " + this.identity + " " + userReceiving;
			byte[] byteSend = combine.getBytes();
			packet = new DatagramPacket(byteSend, byteSend.length, dstAddress);
			terminal.println("Sending " + combine);
			if (this.identity.equals("E1"))
				packet.setPort(50010);
			if (this.identity.equals("E2"))
				packet.setPort(50030);
			if (this.identity.equals("E3"))
				packet.setPort(50020);
			send();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized void send() throws Exception {
		socket.send(packet);
	}

	public synchronized void onReceipt(DatagramPacket packet) {
		StringContent stringx = new StringContent(packet);
		String sstringx = stringx.toString();
		terminal.println("Recieved : " + sstringx + "\n");
		try {
			start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		try {
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}
	}
}