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
	public ArrayList<ArrayList<String>> foodslist = new ArrayList<ArrayList<String>>();
	public ArrayList<ArrayList<String>> gameslist = new ArrayList<ArrayList<String>>();
	public ArrayList<ArrayList<String>> countrieslist = new ArrayList<ArrayList<String>>();
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
		foods.add("hamburger");
		foods.add("pasta");
		games.add("scrabble");
		games.add("chess");
		games.add("poker");
		countries.add("england");
		countries.add("mexico");
		countries.add("india");
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
			    				//connect client and assign client count to clients.size()+1
			    				c.count = clients.size()+1;
			    				callback.accept("client has connected to server: " + "client #" + String.valueOf(c.count));
			    				Wordguess client = new Wordguess();
			    				client.count = c.count;
			    				serverinfo.add(client);
			    				clients.add(c);
			    				//each client gets their own list of foods, games and countries
			    				ArrayList<String> foodscopy = new ArrayList<String>();
			    				ArrayList<String> gamescopy = new ArrayList<String>();
			    				ArrayList<String> countriescopy = new ArrayList<String>();
			    				//copy everything into new lists
			    				for(int i = 0; i < 3; i++)
			    				{
			    					foodscopy.add(foods.get(i));
			    					gamescopy.add(games.get(i));
			    					countriescopy.add(countries.get(i));
			    				}
			    				foodslist.add(foodscopy);
			    				gameslist.add(gamescopy);
			    				countrieslist.add(countriescopy);
			    				serverclientinfo.numplayers+=1;
			    				//make sure all clients and serverinfo wordguesses have correct count
			    				for(int i = 0; i < clients.size(); i++){
			    					clients.get(i).count = i+1;
			    				}
			    				for(int i = 0; i < serverinfo.size(); i++)
			    				{
			    					serverinfo.get(i).count = i+1;
			    				}
			    				if(serverclientinfo.numplayers >= 1)
			    				{
			    					serverclientinfo.haveoneplayer = true;
			    				}
			    				playeroneonserver = true;
			    				c.start();
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
					    	    	//System.out.println("Server: "+serverinfo.get(count-1).category);
					    	    	serverinfo.get(count-1).guess = tempinfo.guess;
					    	    	//System.out.println("Server guess: "+serverinfo.get(count-1).guess);
					    	    	serverinfo.get(count-1).wordguess = tempinfo.wordguess;
					    	    	serverinfo.get(count-1).guessedfoods = tempinfo.guessedfoods;
					    	    	serverinfo.get(count-1).guessedgames = tempinfo.guessedgames;
					    	    	serverinfo.get(count-1).guessedcountries = tempinfo.guessedcountries;
				    				serverinfo.get(count-1).playagain = tempinfo.playagain;
				    				serverinfo.get(count-1).won = tempinfo.won;
				    				serverinfo.get(count-1).lost= tempinfo.lost;
                                    //check if guess or word
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
								    		String str = serverinfo.get(count-1).guess+" is inside the word and is letter: ";
								    		str+=String.valueOf(serverinfo.get(count-1).position.get(0)+1);
								    		for(int i =1; i < serverinfo.get(count-1).position.size(); i++)
								    		{
								    			str+=", " +String.valueOf(serverinfo.get(count-1).position.get(i)+1);
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
								    			if(serverinfo.get(count-1).category.equals("foods")==true)
								 				{
									    			serverinfo.get(count-1).maxguessfoods+=1;
									    			if(serverinfo.get(count-1).maxguessfoods==3)
									    			{
									    				//guessed food word wrong three times
									    				word = "";
								 						//reset words
								 						foodslist.get(count-1).clear(); 
								 						gameslist.get(count-1).clear(); 
								 						countrieslist.get(count-1).clear(); 
								 						foodslist.get(count-1).addAll(foods);
								 						gameslist.get(count-1).addAll(games); 
								 						countrieslist.get(count-1).addAll(countries); 
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
									 					serverinfo.get(count-1).playagain = false;
									 					serverinfo.get(count-1).categorieswon = 0;
									    				tempinfo1.playagain = serverinfo.get(count-1).playagain;
									    				tempinfo1.category = serverinfo.get(count-1).category;
									    				tempinfo1.numberofletters = serverinfo.get(count-1).numberofletters;
									 					tempinfo1.remaininguess = serverinfo.get(count-1).remaininguess;
									 					tempinfo1.won = serverinfo.get(count-1).won;
									 					tempinfo1.lost = serverinfo.get(count-1).lost;
									 					tempinfo1.guessedfoods = serverinfo.get(count-1).guessedfoods;
									 					tempinfo1.guessedgames = serverinfo.get(count-1).guessedgames;
									 					tempinfo1.guessedcountries = serverinfo.get(count-1).guessedcountries;
									 					send("You lost the game!", count);
									 					callback.accept("Client: "+count+" lost the game");
									    				send(tempinfo1, count);
									    			}
									    			else
									    			{
									    				//guess food word wrong
									    				word = "";
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
									 					serverinfo.get(count-1).position = new ArrayList<Integer>();
									 					serverinfo.get(count-1).remaininguess = 6;
									 					serverinfo.get(count-1).playagain = true;
									    				tempinfo1.playagain = serverinfo.get(count-1).playagain;
									    				tempinfo1.category = serverinfo.get(count-1).category;
									    				tempinfo1.numberofletters = serverinfo.get(count-1).numberofletters;
									 					tempinfo1.remaininguess = serverinfo.get(count-1).remaininguess;
									 					tempinfo1.won = serverinfo.get(count-1).won;
									 					tempinfo1.lost = serverinfo.get(count-1).lost;
									 					tempinfo1.guessedfoods = serverinfo.get(count-1).guessedfoods;
									 					tempinfo1.guessedgames = serverinfo.get(count-1).guessedgames;
									 					tempinfo1.guessedcountries = serverinfo.get(count-1).guessedcountries;
									 					send("You ran out of guesses, please pick another category!", count);
									 					send(tempinfo1, count);
									    			}
								 				}
								 				else if(serverinfo.get(count-1).category.equals("games")==true)
								 				{
								 					serverinfo.get(count-1).maxguessgames+=1;
									    			if(serverinfo.get(count-1).maxguessgames==3)
									    			{
									    				//guessed games word wrong three times
									    				word = "";
								 						//reset words
								 						foodslist.get(count-1).clear(); 
								 						gameslist.get(count-1).clear(); 
								 						countrieslist.get(count-1).clear(); 
								 						foodslist.get(count-1).addAll(foods);
								 						gameslist.get(count-1).addAll(games); 
								 						countrieslist.get(count-1).addAll(countries); 
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
									 					serverinfo.get(count-1).playagain = false;
									 					serverinfo.get(count-1).categorieswon = 0;
									    				tempinfo1.playagain = serverinfo.get(count-1).playagain;
									    				tempinfo1.category = serverinfo.get(count-1).category;
									    				tempinfo1.numberofletters = serverinfo.get(count-1).numberofletters;
									 					tempinfo1.remaininguess = serverinfo.get(count-1).remaininguess;
									 					tempinfo1.won = serverinfo.get(count-1).won;
									 					tempinfo1.lost = serverinfo.get(count-1).lost;
									 					tempinfo1.guessedfoods = serverinfo.get(count-1).guessedfoods;
									 					tempinfo1.guessedgames = serverinfo.get(count-1).guessedgames;
									 					tempinfo1.guessedcountries = serverinfo.get(count-1).guessedcountries;
									 					send("You lost the game!", count);
									 					callback.accept("Client: "+count+" lost the game");
									    				send(tempinfo1, count);
									    			}
									    			else
									    			{
									    				//guessed games word wrong
									    				word = "";
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
									 					serverinfo.get(count-1).position = new ArrayList<Integer>();
									 					serverinfo.get(count-1).remaininguess = 6;
									 					serverinfo.get(count-1).playagain = true;
									    				tempinfo1.playagain = serverinfo.get(count-1).playagain;
										 				tempinfo1.category = serverinfo.get(count-1).category;
									    				tempinfo1.numberofletters = serverinfo.get(count-1).numberofletters;
									 					tempinfo1.remaininguess = serverinfo.get(count-1).remaininguess;
									 					tempinfo1.won = serverinfo.get(count-1).won;
									 					tempinfo1.lost = serverinfo.get(count-1).lost;
									 					tempinfo1.guessedfoods = serverinfo.get(count-1).guessedfoods;
									 					tempinfo1.guessedgames = serverinfo.get(count-1).guessedgames;
									 					tempinfo1.guessedcountries = serverinfo.get(count-1).guessedcountries;
									 					send("You ran out of guesses, please pick another category!", count);
									 					send(tempinfo1, count);
									    			}
								 				}
								 				else
								 				{
								 					serverinfo.get(count-1).maxguesscountries+=1;
								 					if(serverinfo.get(count-1).maxguesscountries==3)
									    			{
								 						//guessed countries word wrong three times
								 						//reset words
								 						foodslist.get(count-1).clear(); 
								 						gameslist.get(count-1).clear(); 
								 						countrieslist.get(count-1).clear(); 
								 						foodslist.get(count-1).addAll(foods);
								 						gameslist.get(count-1).addAll(games); 
								 						countrieslist.get(count-1).addAll(countries); 
								 						word = "";
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
									 					serverinfo.get(count-1).playagain = false;
									 					serverinfo.get(count-1).categorieswon = 0;
									 					tempinfo1.playagain = serverinfo.get(count-1).playagain;
								 						tempinfo1.lost = serverinfo.get(count-1).lost;
											    		tempinfo1.category = serverinfo.get(count-1).category;
									    				tempinfo1.numberofletters = serverinfo.get(count-1).numberofletters;
									 					tempinfo1.remaininguess = serverinfo.get(count-1).remaininguess;
									 					tempinfo1.won = serverinfo.get(count-1).won;
									 					tempinfo1.lost = serverinfo.get(count-1).lost;
									 					tempinfo1.guessedfoods = serverinfo.get(count-1).guessedfoods;
									 					tempinfo1.guessedgames = serverinfo.get(count-1).guessedgames;
									 					tempinfo1.guessedcountries = serverinfo.get(count-1).guessedcountries;
									 					send("You lost the game!", count);
									 					send(tempinfo1, count);
									    				callback.accept("Client: "+count+" lost the game");
									    			}
									    			else
									    			{
									    				//guessed countries word wrong
									    				word = "";
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
									 					serverinfo.get(count-1).position = new ArrayList<Integer>();
									 					serverinfo.get(count-1).remaininguess = 6;
									 					serverinfo.get(count-1).playagain = true;
									 					tempinfo1.playagain = serverinfo.get(count-1).playagain;
									    				tempinfo1.category = serverinfo.get(count-1).category;
									    				tempinfo1.numberofletters = serverinfo.get(count-1).numberofletters;
									 					tempinfo1.remaininguess = serverinfo.get(count-1).remaininguess;
									 					tempinfo1.won = serverinfo.get(count-1).won;
									 					tempinfo1.lost = serverinfo.get(count-1).lost;
									 					tempinfo1.guessedfoods = serverinfo.get(count-1).guessedfoods;
									 					tempinfo1.guessedgames = serverinfo.get(count-1).guessedgames;
									 					tempinfo1.guessedcountries = serverinfo.get(count-1).guessedcountries;
									 					send("You ran out of guesses, please pick another category!", count);
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
								    		//callback.accept("category: "+serverinfo.get(count-1).category);
								    		if(serverinfo.get(count-1).category.equals("foods")==true)
							 				{
							 					//client picked right foods word 
								    			serverinfo.get(count-1).guessedfoods=true;
								    			serverinfo.get(count-1).categorieswon+=1;
							 					if(serverinfo.get(count-1).categorieswon==3)
							 					{
							 						//client won
							 						Wordguess tempinfo1 = new Wordguess();
							 						word = "";
							 						//reset words
							 						foodslist.get(count-1).clear(); 
							 						gameslist.get(count-1).clear(); 
							 						countrieslist.get(count-1).clear(); 
							 						foodslist.get(count-1).addAll(foods);
							 						gameslist.get(count-1).addAll(games); 
							 						countrieslist.get(count-1).addAll(countries); 
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
								 					serverinfo.get(count-1).categorieswon = 0;
								 					serverinfo.get(count-1).playagain = false;
								    				tempinfo1.playagain = serverinfo.get(count-1).playagain;
							 						tempinfo1.category = serverinfo.get(count-1).category;
								    				tempinfo1.numberofletters = serverinfo.get(count-1).numberofletters;
								 					tempinfo1.remaininguess = serverinfo.get(count-1).remaininguess;
								 					tempinfo1.won = serverinfo.get(count-1).won;
								 					tempinfo1.lost = serverinfo.get(count-1).lost;
								 					tempinfo1.categorieswon = serverinfo.get(count-1).categorieswon;
								 					tempinfo1.guessedfoods = serverinfo.get(count-1).guessedfoods;
								 					tempinfo1.guessedgames = serverinfo.get(count-1).guessedgames;
								 					tempinfo1.guessedcountries = serverinfo.get(count-1).guessedcountries;
								 					send("You won the game!", count);
								 					send(tempinfo1, count);
								 					callback.accept("Client: "+count+" won the game");
							 					}
							 					else
							 					{
							 						//client picked right foods word
							 						Wordguess tempinfo1 = new Wordguess();
							 						word = "";
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
								 					serverinfo.get(count-1).position = new ArrayList<Integer>();
								 					serverinfo.get(count-1).remaininguess = 6;
								 					serverinfo.get(count-1).playagain = false;
								    				tempinfo1.playagain = serverinfo.get(count-1).playagain;
							 						tempinfo1.category = serverinfo.get(count-1).category;
								    				tempinfo1.numberofletters = serverinfo.get(count-1).numberofletters;
								 					tempinfo1.remaininguess = serverinfo.get(count-1).remaininguess;
								 					tempinfo1.won = serverinfo.get(count-1).won;
								 					tempinfo1.lost = serverinfo.get(count-1).lost;
								 					tempinfo1.categorieswon = serverinfo.get(count-1).categorieswon;
								 					tempinfo1.guessedfoods = serverinfo.get(count-1).guessedfoods;
								 					tempinfo1.guessedgames = serverinfo.get(count-1).guessedgames;
								 					tempinfo1.guessedcountries = serverinfo.get(count-1).guessedcountries;
								 					send("You guessed the right word!", count);
								 					send(tempinfo1, count);
								 					callback.accept("Client: "+count+" guessed the right word");
							 					}
							 				}
							 				else if(serverinfo.get(count-1).category.equals("games")==true)
							 				{
							 					//client picked right games word
							 					serverinfo.get(count-1).guessedgames=true;
							 					serverinfo.get(count-1).categorieswon+=1;
							 					if(serverinfo.get(count-1).categorieswon==3)
							 					{
							 						//client won the game
							 						Wordguess tempinfo1 = new Wordguess();
							 						word = "";
							 						//reset words
							 						foodslist.get(count-1).clear(); 
							 						gameslist.get(count-1).clear(); 
							 						countrieslist.get(count-1).clear(); 
							 						foodslist.get(count-1).addAll(foods);
							 						gameslist.get(count-1).addAll(games); 
							 						countrieslist.get(count-1).addAll(countries); 
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
								 					serverinfo.get(count-1).categorieswon = 0;
								 					serverinfo.get(count-1).playagain = false;
								    				tempinfo1.playagain = serverinfo.get(count-1).playagain;
							 						tempinfo1.category = serverinfo.get(count-1).category;
								    				tempinfo1.numberofletters = serverinfo.get(count-1).numberofletters;
								 					tempinfo1.remaininguess = serverinfo.get(count-1).remaininguess;
								 					tempinfo1.categorieswon = serverinfo.get(count-1).categorieswon;
								 					tempinfo1.won = serverinfo.get(count-1).won;
								 					tempinfo1.lost = serverinfo.get(count-1).lost;
								 					tempinfo1.guessedfoods = serverinfo.get(count-1).guessedfoods;
								 					tempinfo1.guessedgames = serverinfo.get(count-1).guessedgames;
								 					tempinfo1.guessedcountries = serverinfo.get(count-1).guessedcountries;
								 					send("You won the game!", count);
								 					send(tempinfo1, count);
								 					callback.accept("Client: "+count+" won the game");
							 					}
							 					else
							 					{
							 						//picked right game word
							 						word = "";
							 						Wordguess tempinfo1 = new Wordguess();
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
								 					serverinfo.get(count-1).position = new ArrayList<Integer>();
								 					serverinfo.get(count-1).remaininguess = 6;
								 					serverinfo.get(count-1).playagain = false;
								    				tempinfo1.playagain = serverinfo.get(count-1).playagain;
							 						tempinfo1.category = serverinfo.get(count-1).category;
								    				tempinfo1.numberofletters = serverinfo.get(count-1).numberofletters;
								 					tempinfo1.remaininguess = serverinfo.get(count-1).remaininguess;
								 					tempinfo1.categorieswon = serverinfo.get(count-1).categorieswon;
								 					tempinfo1.won = serverinfo.get(count-1).won;
								 					tempinfo1.lost = serverinfo.get(count-1).lost;
								 					tempinfo1.guessedfoods = serverinfo.get(count-1).guessedfoods;
								 					tempinfo1.guessedgames = serverinfo.get(count-1).guessedgames;
								 					tempinfo1.guessedcountries = serverinfo.get(count-1).guessedcountries;
								 					send("You guessed the right word!", count);
								 					send(tempinfo1, count);
								 					callback.accept("Client: "+count+" guessed the right word");
							 					}
							 				}
							 				else
							 				{
							 					//client picked right countries word
							 					serverinfo.get(count-1).guessedcountries=true;
							 					serverinfo.get(count-1).categorieswon+=1;
							 					if(serverinfo.get(count-1).categorieswon == 3)
							 					{
							 						//client won the game
							 						Wordguess tempinfo1 = new Wordguess();
							 						word = "";
							 						//reset words
							 						foodslist.get(count-1).clear(); 
							 						gameslist.get(count-1).clear(); 
							 						countrieslist.get(count-1).clear(); 
							 						foodslist.get(count-1).addAll(foods);
							 						gameslist.get(count-1).addAll(games); 
							 						countrieslist.get(count-1).addAll(countries); 
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
								 					serverinfo.get(count-1).categorieswon = 0;
								 					serverinfo.get(count-1).position = new ArrayList<Integer>();
								 					serverinfo.get(count-1).remaininguess = 6;
								 					serverinfo.get(count-1).categorieswon = 0;
								 					serverinfo.get(count-1).playagain = false;
								    				tempinfo1.playagain = serverinfo.get(count-1).playagain;
							 						tempinfo1.category = serverinfo.get(count-1).category;
								    				tempinfo1.numberofletters = serverinfo.get(count-1).numberofletters;
								 					tempinfo1.remaininguess = serverinfo.get(count-1).remaininguess;
								 					tempinfo1.categorieswon = serverinfo.get(count-1).categorieswon;
								 					tempinfo1.won = serverinfo.get(count-1).won;
								 					tempinfo1.lost = serverinfo.get(count-1).lost;
								 					tempinfo1.guessedfoods = serverinfo.get(count-1).guessedfoods;
								 					tempinfo1.guessedgames = serverinfo.get(count-1).guessedgames;
								 					tempinfo1.guessedcountries = serverinfo.get(count-1).guessedcountries;
								 					send("You won the game!", count);
								 					send(tempinfo1, count);
								 					callback.accept("Client: "+count+" won the game");
							 					}
							 					else
							 					{
							 						Wordguess tempinfo1 = new Wordguess();
							 						//client won countries category
							 						word = "";
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
								 					serverinfo.get(count-1).position = new ArrayList<Integer>();
								 					serverinfo.get(count-1).remaininguess = 6;
								 					serverinfo.get(count-1).playagain = false;
								    				//reset everything
								 					tempinfo1.playagain = serverinfo.get(count-1).playagain;
							 						tempinfo1.category = serverinfo.get(count-1).category;
								    				tempinfo1.numberofletters = serverinfo.get(count-1).numberofletters;
								 					tempinfo1.remaininguess = serverinfo.get(count-1).remaininguess;
								 					tempinfo1.categorieswon = serverinfo.get(count-1).categorieswon;
								 					tempinfo1.won = serverinfo.get(count-1).won;
								 					tempinfo1.lost = serverinfo.get(count-1).lost;
								 					tempinfo1.guessedfoods = serverinfo.get(count-1).guessedfoods;
								 					tempinfo1.guessedgames = serverinfo.get(count-1).guessedgames;
								 					tempinfo1.guessedcountries = serverinfo.get(count-1).guessedcountries;
								 					tempinfo1.position = serverinfo.get(count-1).position;
								 					tempinfo1.remaininguess = serverinfo.get(count-1).remaininguess;
								 					send("You guessed the right word!", count);
								 					send(tempinfo1, count);
								 					callback.accept("Client: "+count+" guessed the right word");
							 					}
							 				}
								    		serverinfo.get(count-1).wordguess = "";
								    	}
								    	else
								    	{
								    		//didn't guess the word right
								    		if(serverinfo.get(count-1).category.equals("foods")==true)
							 				{
								    			serverinfo.get(count-1).maxguessfoods+=1;
								    			if(serverinfo.get(count-1).maxguessfoods==3)
								    			{
								    				//guessed food word wrong three times
								    				Wordguess tempinfo1 = new Wordguess();
								    				word = "";
							 						//reset words
							 						foodslist.get(count-1).clear(); 
							 						gameslist.get(count-1).clear(); 
							 						countrieslist.get(count-1).clear(); 
							 						foodslist.get(count-1).addAll(foods);
							 						gameslist.get(count-1).addAll(games); 
							 						countrieslist.get(count-1).addAll(countries); 
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
								 					serverinfo.get(count-1).categorieswon = 0;
								 					serverinfo.get(count-1).position = new ArrayList<Integer>();
								 					serverinfo.get(count-1).remaininguess = 6;
								 					serverinfo.get(count-1).playagain = false;
								    				tempinfo1.playagain = serverinfo.get(count-1).playagain;
								 					tempinfo1.category = serverinfo.get(count-1).category;
								    				tempinfo1.numberofletters = serverinfo.get(count-1).numberofletters;
								 					tempinfo1.remaininguess = serverinfo.get(count-1).remaininguess;
								 					tempinfo1.won = serverinfo.get(count-1).won;
								 					tempinfo1.lost = serverinfo.get(count-1).lost;
								 					tempinfo1.categorieswon = serverinfo.get(count-1).categorieswon;
								 					tempinfo1.guessedfoods = serverinfo.get(count-1).guessedfoods;
								 					tempinfo1.guessedgames = serverinfo.get(count-1).guessedgames;
								 					tempinfo1.guessedcountries = serverinfo.get(count-1).guessedcountries;
								 					tempinfo1.position = serverinfo.get(count-1).position;
								 					tempinfo1.remaininguess = serverinfo.get(count-1).remaininguess;
								 					send("You lost the game!", count);
								 					send(tempinfo1, count);
								    				callback.accept("Client: "+count+" lost the game");
								    			}
								    			else
								    			{
								    				//guess food word wrong
								    				Wordguess tempinfo1 = new Wordguess();
								    				word = "";
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
								 					serverinfo.get(count-1).position = new ArrayList<Integer>();
								 					serverinfo.get(count-1).remaininguess = 6;
								 					//client can pick another category
								 					serverinfo.get(count-1).playagain = true;
								    				tempinfo1.playagain = serverinfo.get(count-1).playagain;
								 					tempinfo1.category = serverinfo.get(count-1).category;
								    				tempinfo1.numberofletters = serverinfo.get(count-1).numberofletters;
								 					tempinfo1.remaininguess = serverinfo.get(count-1).remaininguess;
								 					tempinfo1.won = serverinfo.get(count-1).won;
								 					tempinfo1.lost = serverinfo.get(count-1).lost;
								 					tempinfo1.guessedfoods = serverinfo.get(count-1).guessedfoods;
								 					tempinfo1.guessedgames = serverinfo.get(count-1).guessedgames;
								 					tempinfo1.guessedcountries = serverinfo.get(count-1).guessedcountries;
								 					tempinfo1.categorieswon = serverinfo.get(count-1).categorieswon;
								 					tempinfo1.position = serverinfo.get(count-1).position;
								 					tempinfo1.remaininguess = serverinfo.get(count-1).remaininguess;
								 					send("You guessed the wrong word!", count);
								 					send(tempinfo1, count);
								 					callback.accept("Client: "+count+" guessed the wrong word");
								    			}
							 				}
							 				else if(serverinfo.get(count-1).category.equals("games")==true)
							 				{
							 					//System.out.println("games");
							 					serverinfo.get(count-1).maxguessgames+=1;
								    			if(serverinfo.get(count-1).maxguessgames==3)
								    			{
								    				//guessed games word wrong three times
								    				Wordguess tempinfo1 = new Wordguess();
								    				word = "";
							 						//reset words
							 						foodslist.get(count-1).clear(); 
							 						gameslist.get(count-1).clear(); 
							 						countrieslist.get(count-1).clear(); 
							 						foodslist.get(count-1).addAll(foods);
							 						gameslist.get(count-1).addAll(games); 
							 						countrieslist.get(count-1).addAll(countries); 
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
								 					serverinfo.get(count-1).categorieswon = 0;
								 					serverinfo.get(count-1).position = new ArrayList<Integer>();
								 					serverinfo.get(count-1).remaininguess = 6;
								 					tempinfo1.category = serverinfo.get(count-1).category;
								    				tempinfo1.numberofletters = serverinfo.get(count-1).numberofletters;
								 					tempinfo1.remaininguess = serverinfo.get(count-1).remaininguess;
								 					tempinfo1.won = serverinfo.get(count-1).won;
								 					tempinfo1.lost = serverinfo.get(count-1).lost;
								 					tempinfo1.categorieswon = serverinfo.get(count-1).categorieswon;
								 					tempinfo1.guessedfoods = serverinfo.get(count-1).guessedfoods;
								 					tempinfo1.guessedgames = serverinfo.get(count-1).guessedgames;
								 					tempinfo1.guessedcountries = serverinfo.get(count-1).guessedcountries;
								 					tempinfo1.position = serverinfo.get(count-1).position;
								 					tempinfo1.remaininguess = serverinfo.get(count-1).remaininguess;
								 					send("You lost the game!", count);
								 					send(tempinfo1, count);
								    				callback.accept("Client: "+count+" lost the game");
								    			}
								    			else
								    			{
								    				//guessed games word wrong
								    				Wordguess tempinfo1 = new Wordguess();
								    				word = "";
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
								 					serverinfo.get(count-1).categorieswon = 0;
								 					serverinfo.get(count-1).position = new ArrayList<Integer>();
								 					serverinfo.get(count-1).remaininguess = 6;
								 					//client can pick another category
								 					serverinfo.get(count-1).playagain = true;
								    				tempinfo1.playagain = serverinfo.get(count-1).playagain;
								 					tempinfo1.category = serverinfo.get(count-1).category;
								    				tempinfo1.numberofletters = serverinfo.get(count-1).numberofletters;
								 					tempinfo1.remaininguess = serverinfo.get(count-1).remaininguess;
								 					tempinfo1.won = serverinfo.get(count-1).won;
								 					tempinfo1.lost = serverinfo.get(count-1).lost;
								 					tempinfo1.categorieswon = serverinfo.get(count-1).categorieswon;
								 					tempinfo1.guessedfoods = serverinfo.get(count-1).guessedfoods;
								 					tempinfo1.guessedgames = serverinfo.get(count-1).guessedgames;
								 					tempinfo1.guessedcountries = serverinfo.get(count-1).guessedcountries;
								 					tempinfo1.position = serverinfo.get(count-1).position;
								 					tempinfo1.remaininguess = serverinfo.get(count-1).remaininguess;
								 					send("You guessed the wrong word!", count);
								 					send(tempinfo1, count);
								 					callback.accept("Client: "+count+" guessed the wrong word");
								    			}
							 				}
							 				else
							 				{
							 					serverinfo.get(count-1).maxguesscountries+=1;
							 					if(serverinfo.get(count-1).maxguesscountries==3)
								    			{
							 						//guessed countries word wrong three times
							 						Wordguess tempinfo1 = new Wordguess();
							 						word = "";
							 						//reset words
								    				foodslist.get(count-1).clear(); 
							 						gameslist.get(count-1).clear(); 
							 						countrieslist.get(count-1).clear(); 
							 						foodslist.get(count-1).addAll(foods);
							 						gameslist.get(count-1).addAll(games); 
							 						countrieslist.get(count-1).addAll(countries); 
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
								 					serverinfo.get(count-1).categorieswon = 0;
								 					tempinfo1.category = serverinfo.get(count-1).category;
								    				tempinfo1.numberofletters = serverinfo.get(count-1).numberofletters;
								 					tempinfo1.remaininguess = serverinfo.get(count-1).remaininguess;
								 					tempinfo1.won = serverinfo.get(count-1).won;
								 					tempinfo1.lost = serverinfo.get(count-1).lost;
								 					tempinfo1.guessedfoods = serverinfo.get(count-1).guessedfoods;
								 					tempinfo1.guessedgames = serverinfo.get(count-1).guessedgames;
								 					tempinfo1.categorieswon = serverinfo.get(count-1).categorieswon;
								 					tempinfo1.guessedcountries = serverinfo.get(count-1).guessedcountries;
								 					tempinfo1.position = serverinfo.get(count-1).position;
								 					tempinfo1.remaininguess = serverinfo.get(count-1).remaininguess;
								 					send("You lost the game!", count);
								 					send(tempinfo1, count);
								    				callback.accept("Client: "+count+" lost the game");
								    			}
								    			else
								    			{
								    				//guessed countries word wrong
								    				Wordguess tempinfo1 = new Wordguess();
								    				word = "";
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
								 					serverinfo.get(count-1).position = new ArrayList<Integer>();
								 					serverinfo.get(count-1).remaininguess = 6;
								 					//client can pick another category
								 					serverinfo.get(count-1).playagain = true;
								    				tempinfo1.playagain = serverinfo.get(count-1).playagain;
								    				tempinfo1.category = serverinfo.get(count-1).category;
								    				tempinfo1.numberofletters = serverinfo.get(count-1).numberofletters;
								 					tempinfo1.remaininguess = serverinfo.get(count-1).remaininguess;
								 					tempinfo1.won = serverinfo.get(count-1).won;
								 					tempinfo1.lost = serverinfo.get(count-1).lost;
								 					tempinfo1.categorieswon = serverinfo.get(count-1).categorieswon;
								 					tempinfo1.guessedfoods = serverinfo.get(count-1).guessedfoods;
								 					tempinfo1.guessedgames = serverinfo.get(count-1).guessedgames;
								 					tempinfo1.guessedcountries = serverinfo.get(count-1).guessedcountries;
								 					tempinfo1.position = serverinfo.get(count-1).position;
								 					tempinfo1.remaininguess = serverinfo.get(count-1).remaininguess;
								 					send("You guessed the wrong word!", count);
								 					send(tempinfo1, count);
								 					callback.accept("Client: "+count+" guessed the wrong word");
								    			}
							 				}
								    	}
								    	serverinfo.get(count-1).wordguess = "";
								    }
								    else if(serverinfo.get(count-1).category.equals("")==false)
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
							 					word = foodslist.get(count-1).get(rand.nextInt(foodslist.get(count-1).size()));
							 					callback.accept("Word to guessed: "+word);
							 					foodslist.get(count-1).remove(word);
							 					serverinfo.get(count-1).numberofletters =  word.length();
							 					send("Number of letters in word: "+serverinfo.get(count-1).numberofletters, count);
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
							 					word = gameslist.get(count-1).get(rand.nextInt(gameslist.get(count-1).size()));
							 					gameslist.get(count-1).remove(word);
							 					callback.accept("Word to guessed: "+word);
							 					serverinfo.get(count-1).numberofletters =  word.length();
							 					send("Number of letters in word: "+serverinfo.get(count-1).numberofletters, count);
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
							 					word = countrieslist.get(count-1).get(rand.nextInt(countrieslist.get(count-1).size()));
							 					countrieslist.get(count-1).remove(word);
							 					callback.accept("Word to guessed: "+word);
							 					serverinfo.get(count-1).numberofletters =  word.length();
							 					send("Number of letters in word: "+serverinfo.get(count-1).numberofletters, count);
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
					    	    	else
					    	    	{
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
					    	serverclientinfo.numplayers--;
					    	counter.add(count);
					    	serverinfo.remove(count-1);
					    	clients.remove(this);
	    					//shift all clients before the client that was removed to left if client removed wasn't the last client
					    	//i.e. client 2 becomes client 1 if client 1 left
					    	for(int i = 0; i < clients.size(); i++){
	    						clients.get(i).count = i+1;
	    					}
	    					for(int i = 0; i < serverinfo.size(); i++)
	    					{
	    						serverinfo.get(i).count = i+1;
	    					}
	    					//remove
	    					foodslist.remove(count-1);
	    					gameslist.remove(count-1);
	    					countrieslist.remove(count-1);
					    	break;
						    //end of while
					    }
					}
		}//end of client thread
	}
}


	
	
