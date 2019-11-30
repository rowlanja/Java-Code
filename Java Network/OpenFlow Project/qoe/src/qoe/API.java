package qoe;

import java.util.ArrayList;

public class API {
	
	
	public static ArrayList<ArrayList<String>> connections(String routerID) throws Exception {
		ArrayList<ArrayList<String>> connections = new ArrayList<ArrayList<String>>();
		ArrayList<String> EndUsers = new ArrayList<String>();
		ArrayList<String> Routers = new ArrayList<String>();
	
		switch(routerID) {
			case "R0" :
				Routers.add("R1 50010 10");
				Routers.add("R5 50030 20");
			break;
			case "R1" :
				Routers.add("R0 50035 10");
				Routers.add("R2 50015 2");
				Routers.add("R5 50030 70");
				EndUsers.add("E1 50000 2 ");

			break;
			case "R2" : 
				Routers.add("R1 50010 2");
				Routers.add("R3 50020 19");
				Routers.add("R5 50030 32");
			break;
			case "R3" :
				Routers.add("R2 50015 19");
				Routers.add("R4 50025 44");
				EndUsers.add("E3 49998 2 ");
			break;	
			case "R4" : 
				Routers.add("R3 50020 44");
				Routers.add("R5 50030 55");
			break;		
			case "R5" :
				Routers.add("R0 50035 20");
				Routers.add("R1 50010 70");
				Routers.add("R2 50015 32");
				Routers.add("R4 50025 55");
				EndUsers.add("E2 49999 2 ");

			break;	
			
			//end users are assigned an identity of of R(x)where s is equal to a number of 5's added to a base of 115
			
			
		}
		connections.add(Routers);
		connections.add(EndUsers);
		return connections;
	}
	
	public static void main(String[] args) {
	try {


		Terminal router1 = new Terminal("R 1");
		Terminal router2 = new Terminal("R 2");
		Terminal router3 = new Terminal("R 3");
		Terminal router4 = new Terminal("R 4");
		Terminal router5 = new Terminal("R 5");
		Terminal router0 = new Terminal("R 0");

		Terminal controller = new Terminal("Controller");
		ArrayList<ArrayList<String>> freinds;
		freinds = connections("R0");
		Router r0 = new Router(router0, 50035, "localhost", 50001, null, null, "R0", " ", " ", freinds, "unassigned");
	
		freinds = connections("R1");
		Router r1 = new Router(router1, 50010, "localhost", 50001, null, null, "R1", " ", " ", freinds, "E1 50000");
	
		freinds = connections("R2");
		Router r2 = new Router(router2, 50015, "localhost", 50001, null, null, "R2", " ", " ", freinds, "unassigned");
	
		freinds = connections("R3");
		Router r3 = new Router(router3, 50020, "localhost", 50001, null, null, "R3", " ", " ", freinds, "E3 49998");
	
		freinds = connections("R4");
		Router r4 = new Router(router4, 50025, "localhost", 50001, null, null, "R4", " ", " ", freinds, "unassigned" );
		
		freinds = connections("R5");
		Router r5 = new Router(router5, 50030, "localhost", 50001, null, null, "R5", " ", " ", freinds, "E2 49999");

		Controller c2 = new Controller(controller, 50050);

		Terminal endUser1 = new Terminal("User 1");
		Terminal endUser2 = new Terminal("User 2");
		Terminal endUser3 = new Terminal("User 2");

		endUser1.println("Current End Users Available : E2, E3");
		endUser1.println("Example message would be :E2 hi");
		endUser1.println("--------------------------------");
		endUser2.println("Current End Users Available : E1, E3");
		endUser2.println("Example message would be :E1 hi");
		endUser2.println("--------------------------------");
		endUser3.println("Current End Users Available : E1, E2");
		endUser3.println("Example message would be :E2 hi");
		endUser3.println("--------------------------------");

		
		EndUser EU1 = new EndUser(endUser1, "localhost", 50050, 50000, "E1", "50010");
		EndUser EU2 = new EndUser(endUser2, "localhost", 50050, 49999, "E2", "50030");
		EndUser EU3 = new EndUser(endUser3, "localhost", 50050, 49998, "E3", "50020");
		r0.start();
		r1.start();
		r2.start();
		r3.start();
		r4.start();
		r5.start();
		c2.start();
		EU1.start();
		EU2.start();
		EU3.start();
		
		
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}
	}
}
