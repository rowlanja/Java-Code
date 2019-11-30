

/**
 * 
 */
import java.net.DatagramSocket;
import java.awt.Toolkit;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;




/**
 *
 * Client class
 * 
 * An instance accepts user input 
 *
 */
public class Client extends Node {
	static final int DEFAULT_SRC_PORT = 50000;
	static final int DEFAULT_DST_PORT = 50001;
	static final int WORKER_PORT1 = 50005;
	static final int WORKER_PORT2 = 50005;
	static ArrayList<Worker> availWorkers = new ArrayList<Worker>();
	static ArrayList<Worker> unavailWorkers = new ArrayList<Worker>();
	static ArrayList<Client> availClients = new ArrayList<Client>();

	static final String DEFAULT_DST_NODE = "localhost";	
	static final String DEFAULT_DST_SERVER = "localhost";	
	
	int NumOfPackets;
	Terminal terminal;
	InetSocketAddress dstAddress;
	Worker worker;
    Toolkit toolkit;
    String stringtosend;
    static int interval;
    static Timer timer;
    DatagramPacket packet;
    Integer identity;
    int homePort;
    
    
	Client(Terminal terminal, String dstHost, int dstPort1, int srcPort, int identity) {
		try {
			this.terminal= terminal;
			dstAddress= new InetSocketAddress(dstHost, dstPort1);
			socket= new DatagramSocket(srcPort);
			listener.go();
			this.identity = identity;
			this.homePort = srcPort;
		}
		catch(java.lang.Exception e) {e.printStackTrace();}
	}


	public synchronized void onReceipt(DatagramPacket packet) {
		NumOfPackets--;
		terminal.println("Package Completed \n");
		if(NumOfPackets == 0) {
		timer.cancel();
		try {
			this.wait(1000);
			this.start();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		}
	}
 
	public synchronized void start() throws Exception {
		String data= null;
		String numWorkers = (terminal.read("How many workers do you want to send to : "));
		data = (terminal.read("Enter a string : "));
		if(numWorkers.contains("All") || numWorkers.contains("all") ) {
			 terminal.println("Sending to all workers");
 	    }
 	    else  terminal.println("Sending to " + Integer.parseInt(numWorkers) + " workers");
		String append = this.identity.toString();
		String send  = append + " " + data;
		byte[] byteSend = send.getBytes();
		packet= new DatagramPacket(byteSend, byteSend.length, dstAddress);
  	    System.out.println("timerStarted");
		//timer();
  	    int numOfWorkers;
  	    if(numWorkers.contains("All") || numWorkers.contains("all")) {
  	    	 numOfWorkers = availWorkers.size();
  	    }
  	    else  numOfWorkers = Integer.parseInt(numWorkers);
		for(int i = 0; i < numOfWorkers ;i++) {
			NumOfPackets++;
			send();	
		}
		timer();

		//send();
	}
	
	
	public synchronized void send() throws Exception {
		socket.send(packet);
		terminal.print("Packet sent to server.....\n");
	}
	
	
	public synchronized void timer() throws Exception{
		 int delay = 5000;
		 int period = 5000;
		 timer = new Timer();
		 interval = 20;
		 timer.scheduleAtFixedRate(new TimerTask() 
		 {
		     public void run() {
	        	 try {
	        		terminal.println("Packet Lost, Timeout");
					send();
				} catch (Exception e) {
					e.printStackTrace();
				}
		     }
		  }, delay, period);
	}


	public static void main(String[] args) {
		try {	
			
			Terminal client1 = new Terminal("Client");
			Terminal client2 = new Terminal("Client");
			client1.println("To send to one worker enter : 1\nTo send to multiple workers enter how many\nTo send to all workers type all");
			client1.println("Enter int value for number of workers");
			client2.println("To send to one worker enter : 1\nTo send to multiple workers enter how many\nTo send to all workers type all");
			client2.println("Enter int value for number of workers");

			Terminal worker = new Terminal("Worker");
			Terminal worker2t = new Terminal("Worker");
			Terminal worker3t = new Terminal("Worker");

			Worker w1 = new Worker(worker, 50005,"a", true);
			w1.start(true);
			availWorkers.add(w1);
			Worker w2 = new Worker(worker2t, 50010,"b", true);
			w2.start(true);
			availWorkers.add(w2);
			Worker w3 = new Worker(worker3t, 50015,"b", true);
			w3.start(true);
			availWorkers.add(w3);
			

			Terminal servert = new Terminal("Server");
			(new Server(servert, 50001, DEFAULT_DST_SERVER, 50005, 50010, availWorkers, availClients)).start();
			
			Client c1 = new Client(client1, DEFAULT_DST_NODE, DEFAULT_DST_PORT, 50000, 1);
			availClients.add(c1);
			Client c2 = new Client(client2, DEFAULT_DST_NODE, DEFAULT_DST_PORT, 49999, 2);
			availClients.add(c2);
			c1.start();
			c2.start();
			client1.println("Program completed");
		} catch(java.lang.Exception e) {e.printStackTrace();}
	}
}