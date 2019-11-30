package qoe;


import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Controller extends Node {
	static final int DEFAULT_PORT = 50050;

	static ArrayList<ArrayList<String>> flows = new ArrayList<ArrayList<String>>();
	static ArrayList<String> routerIdAndPort = new ArrayList<String>();

	static ArrayList<ArrayList<String>> routers = new ArrayList<ArrayList<String>>();
	static ArrayList<ArrayList<String>> routersCopy = new ArrayList<ArrayList<String>>();
	static ArrayList<String> preparedCopy = new ArrayList<String>();
	static ArrayList<String> EndUserConnections = new ArrayList<String>();


	Terminal terminal;
	static String name;
	static int flowCount;
	static int routerCount;
	int port;
	ArrayList<String> routerInfo;
	InetSocketAddress dstAddress;

	public static List<Vertex> nodes;
	public static List<Edge> edges;
	public Set<Vertex> settledNodes;
	public Set<Vertex> unSettledNodes;
	public Map<Vertex, Vertex> predecessors;
	public Map<Vertex, Integer> distance;

	/*
	 * 
	 */
	Controller(Terminal terminal, int port) {
		try {
			this.terminal = terminal;
			this.port = port;
			socket = new DatagramSocket(port);
			listener.go();
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}
	}

	Controller(Graph graph) {
		// create a copy of the array so that we can operate on this array
		this.nodes = new ArrayList<Vertex>(graph.getVertexes());
		this.edges = new ArrayList<Edge>(graph.getEdges());
	}


	// FRQ String reply = "Hello" + " " + this.port + " " + this.in + " " + this.out
	// + " " + this.identity;

	public synchronized void onReceipt(DatagramPacket packet) {
		try {
			StringContent content = new StringContent(packet);
			String s = content.toString();
			System.out.println("Controller recieved :" + s);
			String[] items = s.split(" ");
			List<String> itemList = new ArrayList<String>(Arrays.asList(items));
			for (String z : itemList) System.out.println(z);

			if(s.contains("PING")) {
				routerIdAndPort.add(s);
			}
			if (s.contains("Hello")) {
				addRouter(s);
				routerCount++;
				if(routerCount == 6) {
					prepare();
					flowBuilder();
				}
			}

			if (s.contains("RF")) {
				int portFrom = packet.getPort();

				String from = itemList.get(1).trim();
				String to = itemList.get(2).trim();
				String x1 = null;
				String x2 = null;


				for(int loop1 = 0; loop1 < EndUserConnections.size(); loop1++) {
					String z = EndUserConnections.get(loop1);
					String[] routerArr = z.split(" ");
					List<String> routerArrL = new ArrayList<String>(Arrays.asList(routerArr));
					String routerID = routerArrL.get(1);
					String EU = routerArrL.get(routerArrL.size()-5);

					EU = EU.trim();
					if(EU.contains(from)) x1 = z;
					if(EU.contains(to)) x2 = z;
				}

				String[] routerArr1 = x1.split(" ");
				List<String> routerArrL1 = new ArrayList<String>(Arrays.asList(routerArr1));
				String[] routerArr2 = x2.split(" ");
				List<String> routerArrL2 = new ArrayList<String>(Arrays.asList(routerArr2));
				x1 = routerArrL1.get(1);
				x2 = routerArrL2.get(1);

				String IDNum1 = x1.substring(1, 2);
				String IDNum2 = x2.substring(1, 2);
				int ID1int = Integer.parseInt(IDNum1);
				int ID2int = Integer.parseInt(IDNum2);

				LinkedList<Vertex> path = makeFlows(ID1int, ID2int);
				String str = path.toString();
				String substr = str.substring(1, str.length()-1);
				String[] nodes = substr.split(",");
				List<String> nodeList = new ArrayList<String>(Arrays.asList(nodes));

				for(int loop1 = 0; loop1 < nodeList.size(); loop1++) {
					String temp1 = nodeList.get(loop1);
					String routerNum = temp1.substring(temp1.length()-1, temp1.length());
					nodeList.set(loop1, routerNum);

				}

				nodeList.add(itemList.get(1));
				nodeList.add(itemList.get(2));

				transmitFlow(nodeList);
				resumeExecution(packet.getPort());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public synchronized void resumeExecution(int pausedPort) throws Exception {
		terminal.println("resuming execution..........\n");

		InetSocketAddress resumePort = new InetSocketAddress("localhost", pausedPort);
		String resume = "resume";
		byte[] resumeByte = resume.getBytes();
		DatagramPacket packetIO = new DatagramPacket(resumeByte, resumeByte.length, resumePort);
		packetIO.setPort(pausedPort);
		socket.send(packetIO);
	}

	public synchronized void transmitFlow(List<String> flowInfo) throws Exception {
		String to = flowInfo.get(flowInfo.size()- 1);
		String from = flowInfo.get(flowInfo.size() - 2);
		for(int loop1 = 0; loop1 < flowInfo.size()-2; loop1++) {
			String x = flowInfo.get(loop1);
			x = "R" + x;
			flowInfo.set(loop1, x);
		}


		String update1 = null; 
		String update2 = null;

		for(int loop1 = 0; loop1 < flowInfo.size()-2; loop1++) {
			String node1 = flowInfo.get(loop1);
			String nextNode = flowInfo.get(loop1 + 1);

			for(int loop3 = 0; loop3 < routers.size(); loop3++) {
				String[] routerArr = routers.get(loop3).get(0).split(" ");
				List<String> routerArrL = new ArrayList<String>(Arrays.asList(routerArr));
				if(routerArrL.get(0).contains(node1) || node1.equals(routers.get(loop3).get(0).trim())) {
					update1 = routers.get(loop3).get(0);
				}
			}

			for(int loop2 = 0; loop2 < routers.size(); loop2++) {
				String[] routerArr = routers.get(loop2).get(0).split(" ");
				List<String> routerArrL2 = new ArrayList<String>(Arrays.asList(routerArr));
				if(routerArrL2.get(0).contains(nextNode)) {
					update2 = routers.get(loop2).get(0);
				}
			}


			String port = update1.substring(update1.length()-5, update1.length());
			port = port.trim();
			String out = update2;

			String[] routerArr = update2.split(" ");
			List<String> routerArrL = new ArrayList<String>(Arrays.asList(routerArr));


			int routerPort = Integer.parseInt(port);
			String flowUpdateIO = "fUO " + routerArrL.get(1);
			String flowUpdateToFrom = ("fUTF " + from + " " + to).trim();

			byte[] byteSendIO = flowUpdateIO.getBytes();
			byte[] byteSendToFrom = flowUpdateToFrom.getBytes();

			int lengthIO = byteSendIO.length;
			int lengthToFrom = byteSendToFrom.length;

			InetSocketAddress sendIO = new InetSocketAddress("localhost", routerPort);
			DatagramPacket packetIO = new DatagramPacket(byteSendIO, lengthIO, sendIO);
			packetIO.setPort(routerPort);
			socket.send(packetIO);

			InetSocketAddress sendToFrom = new InetSocketAddress("localhost", routerPort);
			DatagramPacket packetToFrom = new DatagramPacket(byteSendToFrom, lengthToFrom, sendToFrom);
			packetToFrom.setPort(routerPort);
			socket.send(packetToFrom);
		}


	}

	public synchronized ArrayList<ArrayList<String>> findFlows(List<String> toFrom) throws Exception {

		String from = toFrom.get(1);
		String to = toFrom.get(2);
		String ato = to.trim();
		String afrom = from.trim();
		terminal.println("looking for to : " + ato + " from : " + afrom);

		ArrayList<ArrayList<String>> example = new ArrayList<ArrayList<String>>();
		for (int i = 0; i < flows.size(); i++) {
			ArrayList<String> configList = flows.get(i);
			String configTo = configList.get(1);
			String configFrom = configList.get(0);
			String acTo = configTo.trim();
			String acFrom = configFrom.trim();

			if (acTo.contains(ato) && acFrom.contains(afrom)) {
				example.add(configList);
			}
		}
		return example;
	}

	public synchronized ArrayList<String> findDistanceVectorForRoute(ArrayList<ArrayList<String>> allFlows)
			throws Exception {
		ArrayList<String> bestFlow = new ArrayList<String>();
		int currentLowest = 0;
		for (int i = 0; i < allFlows.size(); i++) {
			ArrayList<String> temp = allFlows.get(0);
			int size = temp.size() - 2;
			if (currentLowest == 0 || size < currentLowest) {
				bestFlow = temp;
				currentLowest = size;
			}
		}
		return bestFlow;
	}

	public synchronized void addRouter(String s) throws Exception {

		String[] items = s.split(" ");
		List<String> itemList = new ArrayList<String>(Arrays.asList(items));
		ArrayList<String> newRouter = new ArrayList<String>();
		String path= null;
		String src = itemList.get(1);
		String srcID = src.substring(1, 2);
		newRouter.add( itemList.get(1) + " " + itemList.get(2));
		itemList.remove(0);
		itemList.remove(0);
		itemList.remove(0);
		itemList.remove(0);
		//newRouter.add(srcID);
		if(s.contains("EU")) {
			EndUserConnections.add(s);
		}

		for(int i = 0; i < itemList.size(); i++) {

			path = srcID + "  ";
			String z = itemList.get(0);
			if(z.contains("R")) {
				String connectedIdentity = itemList.get(0);
				String dstID = connectedIdentity.substring(1, 2);
				//newRouter.add(dstID);
				String weight = itemList.get(2);
				path += dstID + " " + weight;
				newRouter.add(path);
				itemList.remove(0);
				itemList.remove(0);
				itemList.remove(0);
			}
			else if(z.contains("EU")) itemList.remove(0);
			else if(z.contains("E")) {
				String EUIdentity = itemList.get(0);
				String EUPort = itemList.get(1);
				String connection = EUIdentity + " " + EUPort;
				itemList.remove(0);
				itemList.remove(0);
			}
		}

		routers.add(newRouter);
	}



	public synchronized ArrayList<String> findRouter(String r1)  throws Exception {
		ArrayList<String> temp = new ArrayList<String>();
		for(int i = 0; i < routers.size(); i++) {
			if(routers.get(i).get(0) == r1)  routers.get(i);
		}
		return temp; 
	}
	//*********************************************************//
	public void execute(Vertex source) {
		settledNodes = new HashSet<Vertex>();
		unSettledNodes = new HashSet<Vertex>();
		distance = new HashMap<Vertex, Integer>();
		predecessors = new HashMap<Vertex, Vertex>();
		distance.put(source, 0);
		unSettledNodes.add(source);
		while (unSettledNodes.size() > 0) {
			Vertex node = getMinimum(unSettledNodes);
			settledNodes.add(node);
			unSettledNodes.remove(node);
			findMinimalDistances(node);
		}
	}

	private void findMinimalDistances(Vertex node) {
		List<Vertex> adjacentNodes = getNeighbors(node);
		for (Vertex target : adjacentNodes) {
			if (getShortestDistance(target) > getShortestDistance(node)
					+ getDistance(node, target)) {
				distance.put(target, getShortestDistance(node)
						+ getDistance(node, target));
				predecessors.put(target, node);
				unSettledNodes.add(target);
			}
		}

	}

	private int getDistance(Vertex node, Vertex target) {
		for (Edge edge : edges) {
			if (edge.getSource().equals(node)
					&& edge.getDestination().equals(target)) {
				return edge.getWeight();
			}
		}
		throw new RuntimeException("Should not happen");
	}

	private List<Vertex> getNeighbors(Vertex node) {
		List<Vertex> neighbors = new ArrayList<Vertex>();
		for (Edge edge : edges) {
			if (edge.getSource().equals(node)
					&& !isSettled(edge.getDestination())) {
				neighbors.add(edge.getDestination());
			}
		}
		return neighbors;
	}

	private Vertex getMinimum(Set<Vertex> vertexes) {
		Vertex minimum = null;
		for (Vertex vertex : vertexes) {
			if (minimum == null) {
				minimum = vertex;
			} else {
				if (getShortestDistance(vertex) < getShortestDistance(minimum)) {
					minimum = vertex;
				}
			}
		}
		return minimum;
	}

	private boolean isSettled(Vertex vertex) {
		return settledNodes.contains(vertex);
	}

	private int getShortestDistance(Vertex destination) {
		Integer d = distance.get(destination);
		if (d == null) {
			return Integer.MAX_VALUE;
		} else {
			return d;
		}
	}

	/*
	 * This method returns the path from the source to the selected target and
	 * NULL if no path exists
	 */
	public LinkedList<Vertex> getPath(Vertex target) {
		LinkedList<Vertex> path = new LinkedList<Vertex>();
		Vertex step = target;
		// check if a path exists
		if (predecessors.get(step) == null) {
			return null;
		}
		path.add(step);
		while (predecessors.get(step) != null) {
			step = predecessors.get(step);
			path.add(step);
		}
		// Put it into the correct order
		Collections.reverse(path);
		return path;
	}

	private static void addLane( int sourceLocNo, int destLocNo, int duration) {
		Edge lane = new Edge(nodes.get(sourceLocNo), nodes.get(destLocNo), duration );
		edges.add(lane);
	}


	public synchronized void start()  throws Exception {


	}
	public synchronized void prepare()  throws Exception {
		for(int loop1 = 0; loop1 < routers.size(); loop1++) {
			ArrayList<String> x = routers.get(loop1);
			if(x.size() > 0) {
				for(int loop2 = 1; loop2 < x.size(); loop2++) {
					preparedCopy.add(x.get(loop2));
				}
			}
		}
	}

	public synchronized void flowBuilder()  throws Exception {
		nodes = new ArrayList<Vertex>();
		edges  = new ArrayList<Edge>();  
		for(int loop1 = 0; loop1 <= routers.size(); loop1++) {
			Vertex location = new Vertex("Node_" + loop1, "Node_" + loop1);
			//terminal.println("Node_" + loop1 + "Node_" + loop1);
			nodes.add(location);
		}

		for(int loop2 = 0; loop2 < preparedCopy.size(); loop2++) {
			String connection = preparedCopy.get(loop2);
			String[] connectArr= connection.split(" ");
			List<String> itemList1 = new ArrayList<String>(Arrays.asList(connectArr));
			int to = Integer.parseInt(itemList1.get(0));
			int from = Integer.parseInt(itemList1.get(2));
			int weight = Integer.parseInt(itemList1.get(3));
			//terminal.println(" to " + itemList1.get(0) + " from " + itemList1.get(2) + " weight " + itemList1.get(3));
			addLane(to, from, weight);
		}

	}
	public synchronized LinkedList<Vertex> makeFlows(int src, int dest) throws Exception {	
		Graph graph = new Graph(nodes, edges);
		Controller dijkstra = new Controller(graph);
		dijkstra.execute(nodes.get(src));
		LinkedList<Vertex> path = dijkstra.getPath(nodes.get(dest));
		terminal.println("Optimzed route goes to : ");
		for (Vertex vertex : path) {
			terminal.println("" +  vertex);
		}
		return path;
	}

	public synchronized ArrayList<String> findRouterPort(String currentFlow) throws Exception {

		ArrayList<String> returnedRouters = new ArrayList<String>();
		String[] items = currentFlow.split(" ");
		List<String> itemList = new ArrayList<String>(Arrays.asList(items));
		String routerWanted = itemList.get(0);
		for (int i = 0; i < routers.size(); i++) {
			ArrayList<String> routerDescription = routers.get(i);
			String identity = routerDescription.get(5);
			if (identity.contains(routerWanted) || identity.equals(routerWanted)) {
				String portString = routerDescription.get(0);
				returnedRouters.add(portString);
				returnedRouters.add(identity);
			}
		}
		return returnedRouters;
	}

	public static void main(String[] args) {
		try {

		}
		catch (java.lang.Exception e) {
			e.printStackTrace();
		}
	}
}
