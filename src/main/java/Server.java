import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.function.Consumer;
public class Server{	
	int port = 0;
	public ArrayList<Wordguess> serverinfo = new ArrayList<Wordguess>();
	int guess1 = 0;
	int guess2 = 0;
	int wpoint = 2;
	public ArrayList<Integer> counter = new ArrayList<Integer>();
	public ArrayList<ClientThread> clients = new ArrayList<ClientThread>();
	public ArrayList<String> foods = new ArrayList<String>();
	public ArrayList<String> games = new ArrayList<String>();
	public ArrayList<String> countries = new ArrayList<String>();
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
		foods.add("pizza");
		foods.add("hamburgers");
		foods.add("pasta");
		games.add("scrabble");
		games.add("chess");
		games.add("poker");
		countries.add("england");
		countries.add("mexico");
		countries.add("India");
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
			String word;
			ClientThread(Socket s){
				this.connection = s;
				word = "";
			}
			//update all clients with string message
			public void send(String message, int count) {
					ClientThread t = clients.get(count-1);
					try {
						t.out.writeObject(message);
					}
					catch(Exception e) {}
			}
			//update all clients with Wordguess clientinfo
			public void send(Wordguess string, int count)
			{
					ClientThread t = clients.get(count-1);
					try {
					 t.out.writeObject(string);
					}
					catch(Exception e) {}
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
							    	Wordguess tempinfo = (Wordguess)object;
					    	    	serverinfo.get(count-1).category = tempinfo.category;
					    	    	System.out.println("Server: "+serverinfo.get(count-1).category);
					    	    	serverinfo.get(count-1).guess = tempinfo.guess;
					    	    	System.out.println("Server guess: "+serverinfo.get(count-1).guess);
					    	    	serverinfo.get(count-1).wordguess = tempinfo.wordguess;
					    	    	//callback client one's hand and guess to server gui
                                    if(serverinfo.get(count-1).guess.equals("")==false)
								    {
								    	//guessed letter
								    	callback.accept("client: " + count + " guessed the letter " + serverinfo.get(count-1).guess);
								    	if(word.indexOf(serverinfo.get(count-1).guess) >= 0)
								    	{
								    		//letter is inside word
								    		int index = word.indexOf(serverinfo.get(count-1).guess);
								    		while (index >= 0) {
								    		    serverinfo.get(count-1).position.add(index);
								    		    index = word.indexOf(serverinfo.get(count-1).guess, index + 1);
								    		}
								    		//send positions of letter in word back to client
								    		String str = serverinfo.get(count-1).guess+" is inside the word and is located at positions: ";
								    		str+=serverinfo.get(count-1).position.get(0);
								    		for(int i =1; i < serverinfo.get(count-1).position.size(); i++)
								    		{
								    			str+=", " +serverinfo.get(count-1).position.get(i);
								    		}
								    		send(str, count);
								    		serverinfo.get(count-1).position.clear();
								    		serverinfo.get(count-1).guess = "";
								    	}
								    	else
								    	{
								    		//client guessed wrong
								    		serverinfo.get(count-1).remaininguess-=1;
								    		if(serverinfo.get(count-1).remaininguess == 0)
								    		{
								    			//couldn't guess the word correctly after 6 guesses 6 times
								    			Wordguess tempinfo1 = new Wordguess();
								    			if(serverinfo.get(count-1).category == "foods")
								 				{
									    			serverinfo.get(count-1).maxguessfoods+=1;
									    			if(serverinfo.get(count-1).maxguessfoods==3)
									    			{
									    				//guessed food word wrong three times
									    				serverinfo.get(count-1).lost = true;
									    				tempinfo1.category = serverinfo.get(count-1).category;
									    				tempinfo1.numberofletters = serverinfo.get(count-1).numberofletters;
									 					tempinfo1.remaininguess = serverinfo.get(count-1).remaininguess;
									 					tempinfo1.won = serverinfo.get(count-1).won;
									 					tempinfo1.lost = serverinfo.get(count-1).lost;
									 					tempinfo1.guessedfoods = serverinfo.get(count-1).guessedfoods;
									 					tempinfo1.guessedgames = serverinfo.get(count-1).guessedgames;
									 					tempinfo1.guessedcountries = serverinfo.get(count-1).guessedcountries;
									    				callback.accept("Client: "+count+" lost the game");
									    				send(tempinfo1, count);
									    			}
									    			else
									    			{
									    				//guess food word wrong
									    				serverinfo.get(count-1).category = "";
									    				tempinfo1.category = serverinfo.get(count-1).category;
									    				tempinfo1.numberofletters = serverinfo.get(count-1).numberofletters;
									 					tempinfo1.remaininguess = serverinfo.get(count-1).remaininguess;
									 					tempinfo1.won = serverinfo.get(count-1).won;
									 					tempinfo1.lost = serverinfo.get(count-1).lost;
									 					tempinfo1.guessedfoods = serverinfo.get(count-1).guessedfoods;
									 					tempinfo1.guessedgames = serverinfo.get(count-1).guessedgames;
									 					tempinfo1.guessedcountries = serverinfo.get(count-1).guessedcountries;
									 					send(tempinfo1, count);
									    			}
								 				}
								 				else if(serverinfo.get(count-1).category == "games")
								 				{
								 					serverinfo.get(count-1).maxguessgames+=1;
									    			if(serverinfo.get(count-1).maxguessgames==3)
									    			{
									    				//guessed games word wrong three times
									    				serverinfo.get(count-1).lost = true;
									    				tempinfo1.category = serverinfo.get(count-1).category;
									    				tempinfo1.numberofletters = serverinfo.get(count-1).numberofletters;
									 					tempinfo1.remaininguess = serverinfo.get(count-1).remaininguess;
									 					tempinfo1.won = serverinfo.get(count-1).won;
									 					tempinfo1.lost = serverinfo.get(count-1).lost;
									 					tempinfo1.guessedfoods = serverinfo.get(count-1).guessedfoods;
									 					tempinfo1.guessedgames = serverinfo.get(count-1).guessedgames;
									 					tempinfo1.guessedcountries = serverinfo.get(count-1).guessedcountries;
									    				callback.accept("Client: "+count+" lost the game");
									    				send(tempinfo1, count);
									    			}
									    			else
									    			{
									    				//guessed games word wrong
										 				serverinfo.get(count-1).category = "";
										 				tempinfo1.category = serverinfo.get(count-1).category;
									    				tempinfo1.numberofletters = 0;
									 					tempinfo1.remaininguess = 6;
									 					tempinfo1.won = serverinfo.get(count-1).won;
									 					tempinfo1.lost = serverinfo.get(count-1).lost;
									 					tempinfo1.guessedfoods = serverinfo.get(count-1).guessedfoods;
									 					tempinfo1.guessedgames = serverinfo.get(count-1).guessedgames;
									 					tempinfo1.guessedcountries = serverinfo.get(count-1).guessedcountries;
										 				send(tempinfo1, count);
									    			}
								 				}
								 				else
								 				{
								 					serverinfo.get(count-1).maxguesscountries+=1;
								 					if(serverinfo.get(count-1).maxguesscountries==3)
									    			{
								 						//guessed countries word wrong three times
								 						serverinfo.get(count-1).lost = true;
								 						tempinfo1.lost = serverinfo.get(count-1).lost;
											    		tempinfo1.category = serverinfo.get(count-1).category;
									    				tempinfo1.numberofletters = serverinfo.get(count-1).numberofletters;
									 					tempinfo1.remaininguess = serverinfo.get(count-1).remaininguess;
									 					tempinfo1.won = serverinfo.get(count-1).won;
									 					tempinfo1.lost = serverinfo.get(count-1).lost;
									 					tempinfo1.guessedfoods = serverinfo.get(count-1).guessedfoods;
									 					tempinfo1.guessedgames = serverinfo.get(count-1).guessedgames;
									 					tempinfo1.guessedcountries = serverinfo.get(count-1).guessedcountries;
									 					send(tempinfo1, count);
									    				callback.accept("Client: "+count+" lost the game");
									    			}
									    			else
									    			{
									    				//guessed countries word wrong
									    				serverinfo.get(count-1).category = "";
									    				tempinfo1.category = serverinfo.get(count-1).category;
									    				tempinfo1.numberofletters = serverinfo.get(count-1).numberofletters;
									 					tempinfo1.remaininguess = serverinfo.get(count-1).remaininguess;
									 					tempinfo1.won = serverinfo.get(count-1).won;
									 					tempinfo1.lost = serverinfo.get(count-1).lost;
									 					tempinfo1.guessedfoods = serverinfo.get(count-1).guessedfoods;
									 					tempinfo1.guessedgames = serverinfo.get(count-1).guessedgames;
									 					tempinfo1.guessedcountries = serverinfo.get(count-1).guessedcountries;
									 					send(tempinfo1, count);
									    			}
								 				}
								    		}
								    		else
								    		{
								    			
								    			//guessed wrong letter
								    			callback.accept(serverinfo.get(count-1).guess + " is not located in: "+word);
									    		callback.accept("Client: "+ count +" has "+String.valueOf(serverinfo.get(count-1).remaininguess)+" remaining guesses");
									    		send("Letter is not located in word", count);
								    			send(String.valueOf(serverinfo.get(count-1).remaininguess)+" remaining guesses", count);
								    		}
								    		serverinfo.get(count-1).guess = "";
								    	}
								    }
								    else if(serverinfo.get(count-1).wordguess.equals("")==false)
								    {
								    	//guessed word
								    	callback.accept("client: " + count + " guessed the word " + serverinfo.get(count-1).wordguess);
								    	if(word.equals(serverinfo.get(count-1).wordguess)==true)
								    	{
								    		Wordguess tempinfo1 = new Wordguess();
								    		if(serverinfo.get(count-1).category == "foods")
							 				{
							 					//client picked right foods word 
								    			serverinfo.get(count-1).guessedfoods=true;
							 					if(serverinfo.get(count-1).guessedfoods==true && serverinfo.get(count-1).guessedgames==true && serverinfo.get(count-1).guessedcountries==true)
							 					{
							 						//client won
							 						serverinfo.get(count-1).won = true;
								 					serverinfo.get(count-1).lost = false;
								 					serverinfo.get(count-1).haveoneplayer = false;
								 					serverinfo.get(count-1).category = "";
								 					serverinfo.get(count-1).numberofletters = 0;
								 					serverinfo.get(count-1).guess = "";
								 					serverinfo.get(count-1).wordguess = "";
								 					serverinfo.get(count-1).guessedfoods = false;
								 					serverinfo.get(count-1).guessedgames = false;
								 					serverinfo.get(count-1).guessedcountries = false;
								 					serverinfo.get(count-1).maxguessfoods = 0;
								 					serverinfo.get(count-1).maxguessgames = 0;
								 					serverinfo.get(count-1).maxguesscountries = 0;
								 					serverinfo.get(count-1).position = new ArrayList<Integer>();
								 					serverinfo.get(count-1).remaininguess = 6;
							 						tempinfo1.category = serverinfo.get(count-1).category;
								    				tempinfo1.numberofletters = serverinfo.get(count-1).numberofletters;
								 					tempinfo1.remaininguess = serverinfo.get(count-1).remaininguess;
								 					tempinfo1.won = serverinfo.get(count-1).won;
								 					tempinfo1.lost = serverinfo.get(count-1).lost;
								 					tempinfo1.guessedfoods = serverinfo.get(count-1).guessedfoods;
								 					tempinfo1.guessedgames = serverinfo.get(count-1).guessedgames;
								 					tempinfo1.guessedcountries = serverinfo.get(count-1).guessedcountries;
								 					send(tempinfo1, count);
							 					}
							 					else
							 					{
							 						//client picked right foods word
							 						serverinfo.get(count-1).won = false;
								 					serverinfo.get(count-1).lost = false;
								 					serverinfo.get(count-1).haveoneplayer = false;
								 					serverinfo.get(count-1).category = "";
								 					serverinfo.get(count-1).numberofletters = 0;
								 					serverinfo.get(count-1).guess = "";
								 					serverinfo.get(count-1).wordguess = "";
								 					serverinfo.get(count-1).guessedfoods = true;
								 					serverinfo.get(count-1).guessedgames = false;
								 					serverinfo.get(count-1).guessedcountries = false;
								 					serverinfo.get(count-1).maxguessfoods = 0;
								 					serverinfo.get(count-1).maxguessgames = 0;
								 					serverinfo.get(count-1).maxguesscountries = 0;
								 					serverinfo.get(count-1).position = new ArrayList<Integer>();
								 					serverinfo.get(count-1).remaininguess = 6;
							 						tempinfo1.category = serverinfo.get(count-1).category;
								    				tempinfo1.numberofletters = serverinfo.get(count-1).numberofletters;
								 					tempinfo1.remaininguess = serverinfo.get(count-1).remaininguess;
								 					tempinfo1.won = serverinfo.get(count-1).won;
								 					tempinfo1.lost = serverinfo.get(count-1).lost;
								 					tempinfo1.guessedfoods = serverinfo.get(count-1).guessedfoods;
								 					tempinfo1.guessedgames = serverinfo.get(count-1).guessedgames;
								 					tempinfo1.guessedcountries = serverinfo.get(count-1).guessedcountries;
								 					send(tempinfo1, count);
							 					}
							 				}
							 				else if(serverinfo.get(count-1).category == "games")
							 				{
							 					//client picked right games word
							 					serverinfo.get(count-1).guessedgames=true;
							 					if(serverinfo.get(count-1).guessedfoods==true && serverinfo.get(count-1).guessedgames==true && serverinfo.get(count-1).guessedcountries==true)
							 					{
							 						//client won the game
							 						serverinfo.get(count-1).won = true;
								 					serverinfo.get(count-1).lost = false;
								 					serverinfo.get(count-1).haveoneplayer = false;
								 					serverinfo.get(count-1).category = "";
								 					serverinfo.get(count-1).numberofletters = 0;
								 					serverinfo.get(count-1).guess = "";
								 					serverinfo.get(count-1).wordguess = "";
								 					serverinfo.get(count-1).guessedfoods = false;
								 					serverinfo.get(count-1).guessedgames = false;
								 					serverinfo.get(count-1).guessedcountries = false;
								 					serverinfo.get(count-1).maxguessfoods = 0;
								 					serverinfo.get(count-1).maxguessgames = 0;
								 					serverinfo.get(count-1).maxguesscountries = 0;
								 					serverinfo.get(count-1).position = new ArrayList<Integer>();
								 					serverinfo.get(count-1).remaininguess = 6;
							 						tempinfo1.category = serverinfo.get(count-1).category;
								    				tempinfo1.numberofletters = serverinfo.get(count-1).numberofletters;
								 					tempinfo1.remaininguess = serverinfo.get(count-1).remaininguess;
								 					tempinfo1.won = serverinfo.get(count-1).won;
								 					tempinfo1.lost = serverinfo.get(count-1).lost;
								 					tempinfo1.guessedfoods = serverinfo.get(count-1).guessedfoods;
								 					tempinfo1.guessedgames = serverinfo.get(count-1).guessedgames;
								 					tempinfo1.guessedcountries = serverinfo.get(count-1).guessedcountries;
								 					send(tempinfo1, count);
							 					}
							 					else
							 					{
							 						serverinfo.get(count-1).won = false;
								 					serverinfo.get(count-1).lost = false;
								 					serverinfo.get(count-1).haveoneplayer = false;
								 					serverinfo.get(count-1).category = "";
								 					serverinfo.get(count-1).numberofletters = 0;
								 					serverinfo.get(count-1).guess = "";
								 					serverinfo.get(count-1).wordguess = "";
								 					serverinfo.get(count-1).guessedfoods = false;
								 					serverinfo.get(count-1).guessedgames = true;
								 					serverinfo.get(count-1).guessedcountries = false;
								 					serverinfo.get(count-1).maxguessfoods = 0;
								 					serverinfo.get(count-1).maxguessgames = 0;
								 					serverinfo.get(count-1).maxguesscountries = 0;
								 					serverinfo.get(count-1).position = new ArrayList<Integer>();
								 					serverinfo.get(count-1).remaininguess = 6;
							 						tempinfo1.category = serverinfo.get(count-1).category;
								    				tempinfo1.numberofletters = serverinfo.get(count-1).numberofletters;
								 					tempinfo1.remaininguess = serverinfo.get(count-1).remaininguess;
								 					tempinfo1.won = serverinfo.get(count-1).won;
								 					tempinfo1.lost = serverinfo.get(count-1).lost;
								 					tempinfo1.guessedfoods = serverinfo.get(count-1).guessedfoods;
								 					tempinfo1.guessedgames = serverinfo.get(count-1).guessedgames;
								 					tempinfo1.guessedcountries = serverinfo.get(count-1).guessedcountries;
								 					send(tempinfo, count);
							 					}
							 				}
							 				else
							 				{
							 					//client picked right countries word
							 					serverinfo.get(count-1).guessedcountries=true;
							 					if(serverinfo.get(count-1).guessedfoods==true && serverinfo.get(count-1).guessedgames==true && serverinfo.get(count-1).guessedcountries==true)
							 					{
							 						//client won the game
								 					serverinfo.get(count-1).won = true;
								 					serverinfo.get(count-1).lost = false;
								 					serverinfo.get(count-1).haveoneplayer = false;
								 					serverinfo.get(count-1).category = "";
								 					serverinfo.get(count-1).numberofletters = 0;
								 					serverinfo.get(count-1).guess = "";
								 					serverinfo.get(count-1).wordguess = "";
								 					serverinfo.get(count-1).guessedfoods = false;
								 					serverinfo.get(count-1).guessedgames = false;
								 					serverinfo.get(count-1).guessedcountries = false;
								 					serverinfo.get(count-1).maxguessfoods = 0;
								 					serverinfo.get(count-1).maxguessgames = 0;
								 					serverinfo.get(count-1).maxguesscountries = 0;
								 					serverinfo.get(count-1).position = new ArrayList<Integer>();
								 					serverinfo.get(count-1).remaininguess = 6;
							 						tempinfo1.category = serverinfo.get(count-1).category;
								    				tempinfo1.numberofletters = serverinfo.get(count-1).numberofletters;
								 					tempinfo1.remaininguess = serverinfo.get(count-1).remaininguess;
								 					tempinfo1.won = serverinfo.get(count-1).won;
								 					tempinfo1.lost = serverinfo.get(count-1).lost;
								 					tempinfo1.guessedfoods = serverinfo.get(count-1).guessedfoods;
								 					tempinfo1.guessedgames = serverinfo.get(count-1).guessedgames;
								 					tempinfo1.guessedcountries = serverinfo.get(count-1).guessedcountries;
								 					send(tempinfo1, count);
							 					}
							 					else
							 					{
								 					serverinfo.get(count-1).won = false;
								 					serverinfo.get(count-1).lost = false;
								 					serverinfo.get(count-1).haveoneplayer = false;
								 					serverinfo.get(count-1).category = "";
								 					serverinfo.get(count-1).numberofletters = 0;
								 					serverinfo.get(count-1).guess = "";
								 					serverinfo.get(count-1).wordguess = "";
								 					serverinfo.get(count-1).guessedfoods = false;
								 					serverinfo.get(count-1).guessedgames = false;
								 					serverinfo.get(count-1).guessedcountries = true;
								 					serverinfo.get(count-1).maxguessfoods = 0;
								 					serverinfo.get(count-1).maxguessgames = 0;
								 					serverinfo.get(count-1).maxguesscountries = 0;
								 					serverinfo.get(count-1).position = new ArrayList<Integer>();
								 					serverinfo.get(count-1).remaininguess = 6;
							 						tempinfo1.category = serverinfo.get(count-1).category;
								    				tempinfo1.numberofletters = serverinfo.get(count-1).numberofletters;
								 					tempinfo1.remaininguess = serverinfo.get(count-1).remaininguess;
								 					tempinfo1.won = serverinfo.get(count-1).won;
								 					tempinfo1.lost = serverinfo.get(count-1).lost;
								 					tempinfo1.guessedfoods = serverinfo.get(count-1).guessedfoods;
								 					tempinfo1.guessedgames = serverinfo.get(count-1).guessedgames;
								 					tempinfo1.guessedcountries = serverinfo.get(count-1).guessedcountries;
								 					send(tempinfo1, count);
							 					}
							 				}
								    		serverinfo.get(count-1).wordguess = "";
								    	}
								    	else
								    	{
								    		//didn't guess the word right
								    		Wordguess tempinfo1 = new Wordguess();
								    		if(serverinfo.get(count-1).category == "foods")
							 				{
								    			serverinfo.get(count-1).maxguessfoods+=1;
								    			if(serverinfo.get(count-1).maxguessfoods>3)
								    			{
								    				//guessed food word wrong three times
								 					serverinfo.get(count-1).won = false;
								 					serverinfo.get(count-1).lost = true;
								 					serverinfo.get(count-1).haveoneplayer = false;
								 					serverinfo.get(count-1).category = "";
								 					serverinfo.get(count-1).numberofletters = 0;
								 					serverinfo.get(count-1).guess = "";
								 					serverinfo.get(count-1).wordguess = "";
								 					serverinfo.get(count-1).guessedfoods = false;
								 					serverinfo.get(count-1).guessedgames = false;
								 					serverinfo.get(count-1).guessedcountries = false;
								 					serverinfo.get(count-1).maxguessfoods = 0;
								 					serverinfo.get(count-1).maxguessgames = 0;
								 					serverinfo.get(count-1).maxguesscountries = 0;
								 					serverinfo.get(count-1).position = new ArrayList<Integer>();
								 					serverinfo.get(count-1).remaininguess = 6;
								 					send(tempinfo1, count);
								    				callback.accept("Client: "+count+"lost the game");
								    			}
								    			else
								    			{
								    				//guess food word wrong
								 					serverinfo.get(count-1).won = false;
								 					serverinfo.get(count-1).lost = false;
								 					serverinfo.get(count-1).haveoneplayer = false;
								 					serverinfo.get(count-1).category = "";
								 					serverinfo.get(count-1).numberofletters = 0;
								 					serverinfo.get(count-1).guess = "";
								 					serverinfo.get(count-1).wordguess = "";
								 					serverinfo.get(count-1).guessedfoods = false;
								 					serverinfo.get(count-1).guessedgames = false;
								 					serverinfo.get(count-1).guessedcountries = false;
								 					serverinfo.get(count-1).maxguessfoods = 0;
								 					serverinfo.get(count-1).maxguessgames = 0;
								 					serverinfo.get(count-1).maxguesscountries = 0;
								 					serverinfo.get(count-1).position = new ArrayList<Integer>();
								 					serverinfo.get(count-1).remaininguess = 6;
								 					send(tempinfo1, count);
								 					callback.accept("Client: "+count+" guessed the wrong word");
								 					send("Client: "+count+" guessed the wrong word", count);
								    			}
							 				}
							 				else if(serverinfo.get(count-1).category == "games")
							 				{
							 					serverinfo.get(count-1).maxguessgames+=1;
								    			if(serverinfo.get(count-1).maxguessgames>3)
								    			{
								    				//guessed games word wrong three times
								 					serverinfo.get(count-1).won = false;
								 					serverinfo.get(count-1).lost = true;
								 					serverinfo.get(count-1).haveoneplayer = false;
								 					serverinfo.get(count-1).category = "";
								 					serverinfo.get(count-1).numberofletters = 0;
								 					serverinfo.get(count-1).guess = "";
								 					serverinfo.get(count-1).wordguess = "";
								 					serverinfo.get(count-1).guessedfoods = false;
								 					serverinfo.get(count-1).guessedgames = false;
								 					serverinfo.get(count-1).guessedcountries = false;
								 					serverinfo.get(count-1).maxguessfoods = 0;
								 					serverinfo.get(count-1).maxguessgames = 0;
								 					serverinfo.get(count-1).maxguesscountries = 0;
								 					serverinfo.get(count-1).position = new ArrayList<Integer>();
								 					serverinfo.get(count-1).remaininguess = 6;
								 					send(tempinfo1, count);
								    				callback.accept("Client: "+count+" lost the game");
								    			}
								    			else
								    			{
								    				//guessed games word wrong
								 					serverinfo.get(count-1).won = false;
								 					serverinfo.get(count-1).lost = false;
								 					serverinfo.get(count-1).haveoneplayer = false;
								 					serverinfo.get(count-1).category = "";
								 					serverinfo.get(count-1).numberofletters = 0;
								 					serverinfo.get(count-1).guess = "";
								 					serverinfo.get(count-1).wordguess = "";
								 					serverinfo.get(count-1).guessedfoods = false;
								 					serverinfo.get(count-1).guessedgames = false;
								 					serverinfo.get(count-1).guessedcountries = false;
								 					serverinfo.get(count-1).maxguessfoods = 0;
								 					serverinfo.get(count-1).maxguessgames = 0;
								 					serverinfo.get(count-1).maxguesscountries = 0;
								 					serverinfo.get(count-1).position = new ArrayList<Integer>();
								 					serverinfo.get(count-1).remaininguess = 6;
								 					send(tempinfo1, count);
								 					callback.accept("Client: "+count+" guessed the wrong word");
								    			}
							 				}
							 				else
							 				{
							 					serverinfo.get(count-1).maxguesscountries+=1;
							 					if(serverinfo.get(count-1).maxguesscountries>3)
								    			{
							 						//guessed countries word wrong three times
								 					serverinfo.get(count-1).won = false;
								 					serverinfo.get(count-1).lost = true;
								 					serverinfo.get(count-1).haveoneplayer = false;
								 					serverinfo.get(count-1).category = "";
								 					serverinfo.get(count-1).numberofletters = 0;
								 					serverinfo.get(count-1).guess = "";
								 					serverinfo.get(count-1).wordguess = "";
								 					serverinfo.get(count-1).guessedfoods = false;
								 					serverinfo.get(count-1).guessedgames = false;
								 					serverinfo.get(count-1).guessedcountries = false;
								 					serverinfo.get(count-1).maxguessfoods = 0;
								 					serverinfo.get(count-1).maxguessgames = 0;
								 					serverinfo.get(count-1).maxguesscountries = 0;
								 					serverinfo.get(count-1).position = new ArrayList<Integer>();
								 					serverinfo.get(count-1).remaininguess = 6;
								 					send(tempinfo1, count);
								    				callback.accept("Client: "+count+" lost the game");
								    			}
								    			else
								    			{
								    				//guessed countries word wrong
								 					serverinfo.get(count-1).won = false;
								 					serverinfo.get(count-1).lost = false;
								 					serverinfo.get(count-1).haveoneplayer = false;
								 					serverinfo.get(count-1).category = "";
								 					serverinfo.get(count-1).numberofletters = 0;
								 					serverinfo.get(count-1).guess = "";
								 					serverinfo.get(count-1).wordguess = "";
								 					serverinfo.get(count-1).guessedfoods = false;
								 					serverinfo.get(count-1).guessedgames = false;
								 					serverinfo.get(count-1).guessedcountries = false;
								 					serverinfo.get(count-1).maxguessfoods = 0;
								 					serverinfo.get(count-1).maxguessgames = 0;
								 					serverinfo.get(count-1).maxguesscountries = 0;
								 					serverinfo.get(count-1).position = new ArrayList<Integer>();
								 					serverinfo.get(count-1).remaininguess = 6;
								 					send(tempinfo1, count);
								 					callback.accept("Client: "+count+" guessed the wrong word");
								 					send("Client: "+count+" guessed the wrong word", count);
								    			}
							 				}
								    	}
								    	serverinfo.get(count-1).wordguess = "";
								    }
								    else
								    {
								    	//picked category
								    	try{
								    		callback.accept("client: " + count + " picked the " + serverinfo.get(count-1).category + " category");
									    	Wordguess tempinfo1 = new Wordguess();
							 				tempinfo.category = serverinfo.get(count-1).category;
							 				Random rand = new Random(); 
							 				if(serverinfo.get(count-1).category.equals("foods"))
							 				{
							 					//client picked foods so send number of letters of the picked food word
							 					word = foods.get(rand.nextInt(foods.size()));
							 					foods.remove(word);
							 					serverinfo.get(count-1).numberofletters =  word.length();
							 					tempinfo1.numberofletters = serverinfo.get(count-1).numberofletters;
							 					tempinfo1.remaininguess = serverinfo.get(count-1).remaininguess;
							 					tempinfo1.won = serverinfo.get(count-1).won;
							 					tempinfo1.lost = serverinfo.get(count-1).lost;
							 					tempinfo1.guessedfoods = serverinfo.get(count-1).guessedfoods;
							 					tempinfo1.guessedgames = serverinfo.get(count-1).guessedgames;
							 					tempinfo1.guessedcountries = serverinfo.get(count-1).guessedcountries;
												send(tempinfo1, count);
							 				}
							 				else if(serverinfo.get(count-1).category.equals("games"))
							 				{
							 					//client picked games so send number of letters of the picked games word
							 					word = games.get(rand.nextInt(games.size()));
							 					games.remove(word);
							 					serverinfo.get(count-1).numberofletters =  word.length();
							 					tempinfo1.numberofletters = serverinfo.get(count-1).numberofletters;
							 					tempinfo1.remaininguess = serverinfo.get(count-1).remaininguess;
							 					tempinfo1.won = serverinfo.get(count-1).won;
							 					tempinfo1.lost = serverinfo.get(count-1).lost;
							 					tempinfo1.guessedfoods = serverinfo.get(count-1).guessedfoods;
							 					tempinfo1.guessedgames = serverinfo.get(count-1).guessedgames;
							 					tempinfo1.guessedcountries = serverinfo.get(count-1).guessedcountries;
								 				send(tempinfo1, count);
							 				}
							 				else if(serverinfo.get(count-1).category.equals("countries"))
							 				{
							 					//client picked countries so send number of letters of the picked countries word
							 					word = countries.get(rand.nextInt(countries.size()));
							 					countries.remove(word);
							 					serverinfo.get(count-1).numberofletters =  word.length();
							 					tempinfo1.numberofletters = serverinfo.get(count-1).numberofletters;
							 					tempinfo1.remaininguess = serverinfo.get(count-1).remaininguess;
							 					tempinfo1.won = serverinfo.get(count-1).won;
							 					tempinfo1.lost = serverinfo.get(count-1).lost;
							 					tempinfo1.guessedfoods = serverinfo.get(count-1).guessedfoods;
							 					tempinfo1.guessedgames = serverinfo.get(count-1).guessedgames;
							 					tempinfo1.guessedcountries = serverinfo.get(count-1).guessedcountries;
								 				send(tempinfo1, count);
							 				}
							 				else
							 				{
							 					
							 				}
								    	}
								    	catch(Exception e)
								    	{
								    		e.printStackTrace();
								    	}
								    }
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


	
	
