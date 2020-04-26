import java.util.ArrayList;
import java.util.HashMap;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class WordGuessServer extends Application {	
	//declare gui variables
	Text clients1;
	TextField clients2;
	Button turnon, port2, b1, startserver, update;
	TextField port;
	Server serverConnection;
	ArrayList<Wordguess> serverinfo = new ArrayList<Wordguess>();
	HashMap<String, Scene> sceneMap;
	ListView<String> listItems, listItems2;
	Boolean delete1, delete2;
	TextField p1points, p2points;
	Label player1points, player2points;
	int port1;
	boolean clientoneonserver;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		launch(args);
	}

	//feel free to remove the starter code from this method
	@Override
	public void start(Stage primaryStage) throws Exception {
		// TODO Auto-generated method stub
		primaryStage.setTitle("(Server) Playing word guess!!!");
		listItems = new ListView<String>();
		port = new TextField();
		port2 = new Button("Port");
		turnon = new Button();
		startserver = new Button("Start Server");
		clients1 = new Text("Clients:");
		clients2 = new TextField("0");
		port1 = 0;
		//start first scene 
		startscene(primaryStage);
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });
		startserver.setDisable(true);
		primaryStage.show();
	}
	//first scene that is an intro screen
	public void startscene(Stage primaryStage)
	{
		Scene scene;
		//port button
		port2.setOnAction(e-> {
				//set port1 to the port textfield's input
			if(port.getText() == "")
			{
				
			}
			else
			{
				port1 = Integer.parseInt(port.getText());
				startserver.setDisable(false);
			}
		});
		//start server button
		startserver.setOnAction(e-> {
			//check if port1 is set
			if(port1 != 0)
			{
				//create new server
				serverConnection = new Server(data -> {
					Platform.runLater(()->{
					//continuously update client number
					clients2.setText(String.valueOf(serverConnection.serverclientinfo.numplayers));
				    //add strings from callback.accept() to the listItems listview
					listItems.getItems().add(data.toString());
					//go to new stage if there are two clients
					if(serverConnection.playeroneonserver==true)
					{
						if(serverConnection.serverclientinfo.numplayers>=1)
						{
								//new stage 
								newScene(primaryStage);
						}
						else
						{
								
						}
					}
					});
					}, port1);
			}	
		});
		BorderPane pane = new BorderPane();
		HBox ports = new HBox();
		HBox clients = new HBox();
		ports.getChildren().addAll(port, port2);
		clients.getChildren().addAll(clients1, clients2);
		VBox data = new VBox();
		data.getChildren().addAll(ports, clients);
		VBox buttons = new VBox();
		buttons.getChildren().addAll(startserver);
		pane.setTop(data);
		pane.setCenter(buttons);
		buttons.setAlignment(Pos.CENTER);
		scene = new Scene(pane,300,200);
		primaryStage.setScene(scene);
	}
	//second scene that displays state of game information
	public void newScene(Stage primaryStage) {
		BorderPane pane = new BorderPane();
		pane.setPadding(new Insets(70));
		pane.setStyle("-fx-background-color: coral");
		HBox clients = new HBox();
		clients.getChildren().addAll(clients1, clients2);
		VBox everything = new VBox(clients);
		pane.setTop(everything);
		pane.setCenter(listItems);
		Scene scene = new Scene(pane, 500, 400);
		primaryStage.setScene(scene);
	}

}
