package qoe;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Router extends Node {
	static final int DEFAULT_PORT = 50050;
	Terminal terminal;
	InetSocketAddress dstAddress;
	int port;
	String in;
	String out;
	String identity;
	String source;
	String destination;
	String pausedMessage;
	String assignedUser;
	ArrayList<ArrayList<String>> connections;
	/*
	 * 
	 */
	Router(Terminal terminal, int port, String dstHost, int dstPort1, String in, String out, String identity,
			String source, String destination, ArrayList<ArrayList<String>> connections, String assignedUser) {
		try {
			this.dstAddress = new InetSocketAddress(dstHost, dstPort1);
			this.terminal = terminal;
			this.port = port;
			this.in = in;
			this.out = out;
			this.source = source;
			this.destination = destination;
			this.identity = identity;
			socket = new DatagramSocket(port);
			listener.go();
			this.pausedMessage = null;
			this.connections = connections;
			this.assignedUser = assignedUser;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}
	}

	Router(int port, String in, String out, String identity) {
		try {
			this.port = port;
			this.in = in;
			this.out = out;
			this.identity = identity;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized void onReceipt(DatagramPacket packet) {
		try {
			StringContent content = new StringContent(packet);
			String s = content.toString();
			String[] items = s.split(" ");
			List<String> itemList = new ArrayList<String>(Arrays.asList(items));
			if (packet.getPort() < 50010) {
				this.pausedMessage = s;
			}

			// --------------------flow update--------------------//
			if (s.contains("fUO ")) {
				String newOut = itemList.get(1);
				this.out = newOut;
				terminal.println("New Router out : " + this.out);
			}

			else if (s.contains("fUTF")) {
				String from = itemList.get(1);
				String to = itemList.get(2);
				this.source = from;
				this.destination = to;
				terminal.println("New Router source : " + this.source + " and New Dest : " + this.destination + "\n");
			}
			// --------------------resume flow message--------------------//

			else if (s.contains("resume")) {
				String out = this.out;
				String outNew = out.trim();
				int destPort = Integer.parseInt(outNew);
				InetSocketAddress sendToFrom = new InetSocketAddress("localhost", destPort);
				String x = this.pausedMessage;
				String x2 = x.trim();
				byte[] xByte = x2.getBytes();
				DatagramPacket packetToFrom = new DatagramPacket(xByte, xByte.length, sendToFrom);
				packetToFrom.setPort(destPort);
				this.wait(2000);
				socket.send(packetToFrom);
			}

			// --------------------end user message--------------------//

			else if (s.contains("Msg")) {
				String src = this.source;
				String dest = this.destination;
				String trimSrc = src.trim();
				String trimDest = dest.trim();
				this.pausedMessage = s;
				terminal.println("assigned user " + this.assignedUser);
				terminal.println("package dest " + itemList.get(2));
				
				if(this.assignedUser != "unassigned" && this.assignedUser.contains(itemList.get(2)) == true ) {
				String[] checkerarr = this.assignedUser.split(" ");
				List<String> checklist = new ArrayList<String>(Arrays.asList(checkerarr));
				for(String x : checklist) terminal.println("checklist contains " + x);
					if(checklist.get(0).contains(itemList.get(2))) {	
						String lol = checklist.get(1).trim();
						int port = Integer.parseInt(lol);
						packet.setPort(port);
						socket.send(packet);
						terminal.println("sent message");
					}
				}
				else if (trimSrc.contains(itemList.get(1)) && trimDest.contains(itemList.get(2))) {
					terminal.println("this");
					String destPortStr = this.out.trim();
					int destPort = Integer.parseInt(destPortStr);
					packet.setPort(destPort); 
					terminal.println("sending to " + destPort);
					socket.send(packet);

				} else {
					terminal.println("\n requesting flow from " + itemList.get(1) + " to " + itemList.get(2) + "\n");
					initiateFlowRequest(itemList.get(1), itemList.get(2));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized void initiateFlowRequest(String src, String dst) {
		try {
			String route = "RF " + src + " " + dst;
			byte[] replyByte = route.getBytes();
			InetSocketAddress temp = this.dstAddress;
			DatagramPacket packet = new DatagramPacket(replyByte, replyByte.length, temp);
			packet.setPort(50050);
			socket.send(packet);
		} catch (IOException e) {
		}
	}

	public synchronized void start() throws Exception {
		ArrayList<String> EndUsers = this.connections.get(1);
		ArrayList<String> Routers = this.connections.get(0);
		String EndUsersStr = "";
		String RoutersStr = "";

		for(int i = 0; i < EndUsers.size(); i++) {
			EndUsersStr += EndUsers.get(i) + " ";
		}
		for(int i = 0; i < Routers.size(); i++) {
			RoutersStr += Routers.get(i) + " ";
		}
		String ping = "PING " + this.identity + " " + this.port;
		String reply = null;
		if(this.connections.get(1).size() > 0) {
			reply = "Hello " + this.identity + " " + this.port + " Routers " + RoutersStr + " EU "+ EndUsersStr;
		}
		else {		
			reply = "Hello " + this.identity + " " + this.port + " Routers " + RoutersStr;
		}
		byte[] replyByte = reply.getBytes();
		byte[] pingByte = ping.getBytes();


		InetSocketAddress temp = this.dstAddress;

		DatagramPacket packetPing = new DatagramPacket(pingByte, pingByte.length, temp);
		packetPing.setPort(50050);
		socket.send(packetPing);

		DatagramPacket packet = new DatagramPacket(replyByte, replyByte.length, temp);
		terminal.println("Router state : " + this.port + " " + this.in + " " + this.out + " " + this.identity);
		packet.setPort(50050);
		socket.send(packet);
	}

	public static void main(String[] args) {
		try {
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}
	}
}
