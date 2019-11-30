

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
public class Server extends Node {
	static final int DEFAULT_PORT = 50001;
	InetSocketAddress dstAddress;
	InetSocketAddress dstAddress2;
	ArrayList<Worker> unavailWorkers = new ArrayList<Worker>();
	ArrayList<Worker> availWorkers;
	 ArrayList<Client> availClients;

	Terminal terminal;
	
	/*
	 * 
	 */
	Server(Terminal terminal, int port, String dstHost,int dstPort1, int dstPort2, ArrayList<Worker> availWorkers, ArrayList<Client> availClients) {
		try {
			this.availWorkers = availWorkers;
			this.availClients = availClients;
			this.terminal= terminal;
			socket= new DatagramSocket(port);
			dstAddress= new InetSocketAddress(dstPort1);
			dstAddress2= new InetSocketAddress(dstPort2);

			System.out.print("Server port is connected to : "+ socket.getPort()+ "\n");
			System.out.print("Server port is   : " + socket.getLocalPort() + "\n");
			System.out.print("Server socket is connected to :" + socket.getInetAddress() + "\n");
			System.out.print("Server socket address is : " + socket.getLocalSocketAddress() + "\n");
			System.out.print("Server inet port : " + dstAddress.getPort() + "\n");
			System.out.print("Server inet address : " + dstAddress.getAddress() + "\n");

			
			listener.go();
		}
		catch(java.lang.Exception e) {e.printStackTrace();}
	}


	public synchronized void onReceipt(DatagramPacket packet) {
		try {	
			boolean ack = false;
			
			/*check if message is a "OK" meaning its not an acknowledgement but it adds worker back into availables*/

			StringContent packetCont = new StringContent(packet);
			String s  = packetCont.toString();
			String[] items = s.split(" ");
		    List<String> itemList = new ArrayList<String>(Arrays.asList(items));

		    for (String item  : itemList)  System.out.print(" PASSED INTO SERVER IS : " + item);
			

			
	//-------------------    check if we received a ack    -----------------------//

			if(itemList.contains("OK") || itemList.get(1) == "OK" || itemList.get(1).contains("OK") || itemList.get(1).equals("OK")) {
			ack = true;
			}
			
			int temp2 = packet.getPort();
			System.out.println("Recived from " + packet.getPort());
			boolean found = false;
			
			if(unavailWorkers.size() != 0 ) {
				for(int i = 0; i < this.unavailWorkers.size(); i++) {
					Worker temp = this.unavailWorkers.get(i);
					int port = temp.port;

	  //--message received form worker, work has been completed, sending to server--//
					
				if(temp2 == port && ack == true) {
					
	  //-----------------  check for what client to send to   -------------------//
					
					String sentIdentity = itemList.get(0);
					int port2 = 0;
					int sentIdentityNum = Integer.parseInt(sentIdentity);
				    for (Client item  : availClients) {
				    	System.out.print(" CLIENT HOME PORTS AVAILABLE : " + item.homePort);
				    	System.out.print(" CLIENT HOME PORTS COMPARED TO : " + sentIdentityNum);

					    port2 = item.homePort;
						int yupNum = item.identity;
						 if(sentIdentityNum == yupNum) {
							 break;
						 }
				    }
				    
					availWorkers.add(temp);
					unavailWorkers.remove(temp);
					packet.setPort(port2);
					terminal.println("Worker Acknowledgement recieved");
					found = true;
					System.out.println("packet complete");
					socket.send(packet);					
 					break;
					}
				
	  //----message received form worker, work has not been completed, sending to server---//

				
				else if(temp2 == port && ack != true)  {
					System.out.println("notified");
					availWorkers.add(temp);
					unavailWorkers.remove(temp);
					System.out.println("worker complete");
					found = true;
					break;
					}
				}
			}
			
		
		/*message recieved form client, sending to worker*/
			/*
			 * step 1: check for any available works
			 * step 2 : get a worker
			 * step 3 : prepare sending packet to that worker and add to unavailable worker
			 * step 4 : send the packet
			 *
			 */

		if(found == false) {
			Worker available;
			int port;
			String who = null;
			if(temp2 == 50000) who = "Received from Client 1, sending to worker ";
			if(temp2 == 49999) who = "Received from Client 2, sending to worker ";
			
	
				if(availWorkers.size() != 0) {
				System.out.println("client complete");
				terminal.println(who);
				available = availWorkers.get(0);
				availWorkers.remove(0);
				port = available.port;
				packet.setPort(port);
				unavailWorkers.add(available);
				}
				else {
					terminal.println("No Available Workers");
				}
			socket.send(packet);
			}
		}
		catch(Exception e) {e.printStackTrace();}

	}

	
	public synchronized void start() throws Exception {
		terminal.println("Waiting to relay message");
	}

	public static void main(String[] args) {
		try {					
		} catch(java.lang.Exception e) {e.printStackTrace();}
	}
}
