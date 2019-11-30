

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class Worker extends Node {
	static final int DEFAULT_PORT = 50001;
	Terminal terminal;
	Server server;
	boolean canWork;
	static String name;
	int port;
	/*
	 * 
	 */
	Worker(Terminal terminal, int port, String name, boolean canWork) {
		try {
		    this.terminal = terminal;
		    this.port = port;
			socket= new DatagramSocket(port);
			this.canWork = canWork;
			listener.go();
		    
		}
		catch(java.lang.Exception e) {e.printStackTrace();}
	}
	 


	public synchronized void onReceipt(DatagramPacket packet) {
		try {
			
		if(this.canWork == true) {
		StringContent content= new StringContent(packet);
		String s  = content.toString();
		String[] items = s.split(" ");
	    List<String> itemList = new ArrayList<String>(Arrays.asList(items));
	    for (String item  : itemList) System.out.print(" WORKER strings in item list are " + item);
	

		
		int max = 10; 
        int min = 1; 
        int range = max - min + 1; 
        int rand = (int)(Math.random() * range) + min; 
        
            	if(rand > 2) {
        		
        		terminal.println("Recieved from client : " + itemList.get(0));
        		terminal.println("Message from client is : " + s);

        		this.wait(1000);
        		terminal.println("Sending Acknowledgement....." + "\n");
        		this.wait(1000);
        		DatagramPacket response;
        		String whoInt = itemList.get(0);
        		String reply = whoInt + " OK";
        		response= (new StringContent(reply)).toDatagramPacket();
        		response.setSocketAddress(packet.getSocketAddress());
        		socket.send(response);
        		
        	}
        	else {
			terminal.println("Packet Lost, not sending ACK\n");
			DatagramPacket notifyServer;
			notifyServer= (new StringContent("500 notify")).toDatagramPacket();
			notifyServer.setSocketAddress(packet.getSocketAddress());
    		socket.send(notifyServer);
        	}
		}
	}
	catch(Exception e) {e.printStackTrace();}
}

	
	public synchronized void start(boolean canWork) throws Exception {
		if(canWork == true) {
		terminal.println("Waiting for contact");
		}
		else {
			terminal.println("Not Available to Work");
		}
	}

	public static void main(String[] args) {
		try {
			System.out.print("worker main");
		} catch(java.lang.Exception e) {e.printStackTrace();}
	}
}
