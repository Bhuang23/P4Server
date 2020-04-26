import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.function.Consumer;
public class Server{	
	int port = 0;
	public ArrayList<Wordguess> serverinfo = new ArrayList<Wordguess>();
	int guess1 = 0;
	int guess2 = 0;
	int wpoint = 2;
	public ArrayList<Integer> counter = new ArrayList<Integer>();
	public ArrayList<ClientThread> clients = new ArrayList<ClientThread>();
	boolean playeroneonserver;
	TheServer server;
	Wordguess serverclientinfo;
	private Consumer<Serializable> callback;
	Server(){
	}
	Server(Consumer<Serializable> call, int port2){
		callback = call;
		server = new TheServer();
		port = port2;
		server.start();
		serverclientinfo = new Wordguess();
		playeroneonserver = false;
	}
	public class TheServer extends Thread{
		public void run() {
			try(ServerSocket mysocket = new ServerSocket(port);){
			    System.out.println("Server is waiting for a client!");
			    while(true) {
			    				ClientThread c;
			    				//wait for new client
			    				c = new ClientThread(mysocket.accept());
			    				//server assigns clientThread one to client if playeroneonserver is false
			    				if(counter.size() != 0)
			    				{
			    					//connect client and assign client count to count of most recent client to leave
			    					c.count = counter.get(0);
			    					callback.accept("client has connected to server: " + "client #" + String.valueOf(c.count));
			    					clients.add(c.count-1, c);
			    					Wordguess client = new Wordguess();
			    					client.count = c.count;
			    					serverinfo.add(c.count-1, client);
			    					counter.remove(0);
			    					serverclientinfo.numplayers+=1;
			    					if(serverclientinfo.numplayers >= 1)
			    					{
			    						serverclientinfo.haveoneplayer = true;
			    					}
			    					c.start();
			    				}
			    				else
			    				{
			    					//connect client and assign client count to clients.size()
			    					c.count = clients.size()+1;
			    					callback.accept("client has connected to server: " + "client #" + String.valueOf(c.count));
			    					Wordguess client = new Wordguess();
			    					client.count = c.count;
			    					serverinfo.add(client);
			    					clients.add(c);
			    					serverclientinfo.numplayers+=1;
			    					if(serverclientinfo.numplayers >= 1)
			    					{
			    						serverclientinfo.haveoneplayer = true;
			    					}
			    					playeroneonserver = true;
			    					c.start();
			    				}
						}
			}
			//end of try
			catch(Exception e) {
				callback.accept("Server socket did not launch");
			}
			}//end of while
	}
		class ClientThread extends Thread{
			
			Socket connection;
			public int count;
			ObjectInputStream in;
			ObjectOutputStream out;
			Wordguess tempinfo;
			String name;
			ClientThread(Socket s){
				this.connection = s;
			}
			//update all clients with string message
			public void updateClients(String message) {
				for(int i = 0; i < clients.size(); i++) {
					ClientThread t = clients.get(i);
					try {
						t.out.writeObject(message);
					}
					catch(Exception e) {}
				}
			}
			//update all clients with Wordguess clientinfo
			public void send(Wordguess string)
			{
				for(int i = 0; i < clients.size(); i++) {
					ClientThread t = clients.get(i);
					try {
					 t.out.writeObject(string);
					}
					catch(Exception e) {}
				}
			}
			//test if player one won
			public void run(){
				
				try {
					in = new ObjectInputStream(connection.getInputStream());
					out = new ObjectOutputStream(connection.getOutputStream());
					connection.setTcpNoDelay(true);	
					Serializable clientNum = count;
					out.writeObject(clientNum);
				}
				catch(Exception e) {
					System.out.println("Streams not open");
				}
				 while(true) {
					 try 
						 {
							    Object object = in.readObject();
							    //check if clients send MorraInfo
							    if(object instanceof Wordguess)
							    {
							    }
							    else
							    {
							    	    	callback.accept(object.toString());
							    }
					    }
					    catch(Exception e) {
					    	//client left
					    	callback.accept("Error from socket from client: " + count + "....closing down");
					    	//update all clients with new morraInfo for new game
					    	serverclientinfo.numplayers--;
					    	counter.add(count);
					    	serverinfo.remove(count-1);
					    	clients.remove(this);
					    	break;
						    //end of while
					    }
					}
		}//end of client thread
	}
}


	
	
