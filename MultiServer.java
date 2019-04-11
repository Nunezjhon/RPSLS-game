/*
	Jhon Nunez
	University of Illinois
	CS342 Software Design
	Project 3: Rock, Paper, Scissors, Lizard, Spock
*/

package p3_server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import p3_server.InputListener.Play;

// all signals should be static imports from InputListener; define any new signals there
import static p3_server.InputListener.Play.*;
import static p3_server.InputListener.FORCE_SIG;
import static p3_server.InputListener.WEL_SIG;

public class MultiServer extends Application {

	// reference types
	private ServerSocket server;						// connects clients
	private HashMap<Integer, InputListener> listeners; 	// IO for connected clients; keyed by client ID
	private Stage myStage; 								// primary stage
	private Scene host; 								// host scene
	private Button on,off,setPort;
	private TextField port;
	private Label clientNumber,p1Score,p2Score,p1Status,p2Status, winner;
	private Integer numPlayers; // TODO: does this need to be a reference type?
	private ArrayList<String> clientPlays;

	// primitives
	private int portNumber;
	private int plays; 									// amount of times played
	private int p1ScoreVal = 0; 						// player 1 score
	private int p2ScoreVal = 0; 						// player 2 score
	private int replay = 0;								// replay
	private int nextID;									// next client ID to be assigned
	
	
	public static void main(String[] args) {
		launch(args);	
	}
	
	@Override
	public void start(Stage primaryStage) {

		myStage = primaryStage; // create primary stage
		primaryStage.setTitle("RPSLS Host By: Jhon Nunez");
		//HOST SCENE---------------------------------------------------------------------------------------
		Label gameTitle = new Label("Welcome to RPSLS! Enter Port Number!");
		Label clients = new Label("Number of Clients");
		clientNumber = new Label("0");
		clientNumber.setFont(new Font("Arial", 12));
		clientNumber.setStyle("-fx-font-weight: bold");
		clientNumber.setTextFill(Color.DARKRED);
		
		Label p1 = new Label("Player 1 Score: ");
		Label p2 = new Label("Player 2 Score: ");
		p1Score = new Label("0");
		p1Score.setStyle("-fx-font-weight: bold");
		p2Score = new Label("0");
		p2Score.setStyle("-fx-font-weight: bold");
		Label playAgain = new Label("Play Again Status:");
		Label p1Play = new Label("Player 1 Status: ");
		Label p2Play = new Label("Player 2 Status: ");
		p1Status = new Label("NA");
		p1Status.setStyle("-fx-font-style: italic");
		p2Status = new Label("NA");
		p2Status.setStyle("-fx-font-style: italic");
		Label winLabel = new Label("Winner");
		
		winner = new Label("NA");
		winner.setStyle("-fx-font-weight: bold");
		winner.setTextFill(Color.BLUE);
		
		gameTitle.setFont(new Font("Arial",15) );		
		//background game image
		BackgroundImage gameBI= new BackgroundImage(new Image("images/hostBack.jpg",400,400,false,true),      
				BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,    
				BackgroundSize.DEFAULT);
		BorderPane gamePane = new BorderPane(); //create pane for card game
		gamePane.setPadding(new Insets(50)); //add padding
		gamePane.setBackground(new Background(gameBI));//set background image
		
		on = new Button("Turn On");
		on.setMaxWidth(200);
		off = new Button("Turn Off");
		off.setMaxWidth(200);
		setPort = new Button("Create");
		setPort.setMaxWidth(200);
		port = new TextField();
		port.setPromptText("Enter Port Number to listen to");
		port.setMaxWidth(200);	
		port.setText("7777");
		
		HBox top = new HBox(gameTitle); //title
		top.setAlignment(Pos.TOP_CENTER);
		HBox play1 = new HBox(p1, p1Score); //player 1 score
		HBox play2 = new HBox(p2, p2Score); //player 2 score
		play1.setAlignment(Pos.CENTER);
		play2.setAlignment(Pos.CENTER);
		HBox stat1 = new HBox(p1Play,p1Status); //player 1 status
		HBox stat2 = new HBox(p2Play,p2Status); //player 2 status
		stat1.setAlignment(Pos.CENTER);
		stat2.setAlignment(Pos.CENTER);
		VBox center = new VBox(port,setPort, on, off,clients,clientNumber,play1,play2,playAgain,stat1,stat2,winLabel,winner);
		center.setAlignment(Pos.TOP_CENTER);
		
		gamePane.setTop(top);
		gamePane.setCenter(center);
		
		host = new Scene(gamePane, 400,400);//create scene for host
		primaryStage.setScene(host); //set Scene
		primaryStage.show(); //show Scene
		
		//DISABLE ON and OFF
		on.setDisable(true);
		off.setDisable(true);

		setPort.setOnAction(e -> { // assign portNumber to value entered by user

			try {
				portNumber = Integer.parseInt(port.getText());
			} catch (NumberFormatException x) {

				x.printStackTrace();
				portNumber = 0;

			}

			// FIXME: update UI

		});

// TODO: delete (I left the old code so we can refer back to it until it's fully replaced)
//		setPort.setOnAction(new EventHandler<ActionEvent>() {
//			public void handle(ActionEvent event){
//
//					portNumber = Integer.parseInt(port.getText());//get user input
//
//					try {
//
//						connection = createServer(portNumber); //create server
//						connection.startConnection(); //start connection
//						port.setDisable(true); //disable text field
//						setPort.setDisable(true); //disable port
//						off.setDisable(false); //enable off button
//						System.out.println("Started with port: "+connection.getPort()+" "+connection.isServer());
//
//
//					} catch (Exception e) {
//
//						System.out.println("Nope! on server creation \n");
//						e.printStackTrace();
//					}
//			}
//		});

		off.setOnAction(e -> { // disconnect the server when off is pressed

			disconnect();
			off.setDisable(true);
			on.setDisable(false);

		});

// TODO: delete
//		off.setOnAction(new EventHandler<ActionEvent>() {
//			public void handle(ActionEvent event){
//				/*
//				if (connection.getIP() == null) {//if there is NOT a connection
//				  on.setDisable(false);
//				  off.setDisable(true);
//				}
//				else {//if there is a connection
//				*/
//					try {
//						if(numPlayers > 0) {
//							connection.send("SERVER_STOP"); //send server stop message
//							connection.stopConnection(); //stop connection
//						}
//
//						on.setDisable(false);
//						off.setDisable(true);
//
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//				//}
//			}
//		});
	
		on.setOnAction(e -> { // start the server

			acceptClients();
			on.setDisable(true);
			off.setDisable(false);

		});

// TODO: delete
//		on.setOnAction(new EventHandler<ActionEvent>() {
//			public void handle(ActionEvent event){
//
//				on.setDisable(true);
//				off.setDisable(false);
//
//				//
//				try {
//					connection = createServer(portNumber); //create server
//					connection.startConnection();//start connection
//
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//
//			}
//		});
	
	} // end of start

	private void disconnect() { // close all client connections; disable server

//		clientNumber = 0; // FIXME: reset player count
//		Platform.runLater(() -> numClientsLabel.setText(CLIENT_TEXT + numClients)); // FIXME: update UI

		for (InputListener l : listeners.values()) {

			Socket s = l.getClient();

			try {

				l.send(FORCE_SIG + -1); // send client force disconnect signal with server tag
				s.close();

			} catch (Exception x) {

				x.printStackTrace();

				if (x instanceof IOException) {
					System.err.println("\nPlayer already disconnected\n");
				} else {
					System.err.println("\nNo player to disconnect\n");
				}

			}

		}

		try {
			server.close();
		} catch (Exception x) {

			x.printStackTrace();

			if (x instanceof IOException) {
				System.err.println("\nServer could not be disconnected\n");
			} else {
				System.err.println("\nUnknown error\n");
			}

		}

	}

	@Override
	public void init() {

		numPlayers = 0;
		plays = 0;
		clientPlays = new ArrayList<>();

	}

	@Override
	public void stop() {

		disconnect();
		System.exit(0);

	}

	// creates a new thread to that runs forever, accepting any clients that wish to join the server; this method should be run immediately after the server starts
	private synchronized void acceptClients() {

		new Thread(() -> { // wait for players to connect

			try {

				listeners = new HashMap<>();
				server = new ServerSocket(portNumber);
				InputListener clientIO;

				while (true) {

					listeners.put(nextID, new InputListener(server.accept())); // accept client connection, establish IO
					clientIO = listeners.get(listeners.size() - 1); // get the client that was just connected
					clientIO.start(); // start IO thread
					clientIO.send(WEL_SIG + ++nextID);
//					Platform.runLater(() -> numClientsLabel.setText(CLIENT_TEXT + ++numClients)); // FIXME: update number of clients in UI, increment number of clients

				}

			} catch (IOException x) {

				x.printStackTrace();
				disconnect(); // tear down server
				System.err.println("\nFailed to set up server\n");

			} finally {
				System.gc();
			}

		}).start();

	}

//	private server createServer(int port) {
//		return new server(port, data -> {
//			Platform.runLater(()-> {//runnable (funcitonal interface) run when needed
//
//				System.out.println(data.toString());
//				if(data.toString() == "CLIENT_LEFT") {
//					numPlayers--;
//					clientNumber.setText(numPlayers.toString());
//				}
//
//				if(data.toString() == "CLIENT") {
//
//					numPlayers++; //add player per client connection
//					plays = 0; //decrease play counter: NOT PLAYS
//					clientNumber.setText(numPlayers.toString());
//					clientNumber.setTextFill(Color.FORESTGREEN);
//
//					if (numPlayers == 2) {
//						clientPlays.add(1,"");//initialize
//						connection.increaseNum(); //increase num plays in net
//						try { connection.send("P2");connection.send("TWO_PLAYERS");} catch (Exception e) {e.printStackTrace();} //player 2
//					}
//					else if (numPlayers == 1){
//						clientPlays.add(0,"");//initialize
//						connection.increaseNum(); //increase num plays in net
//						try { connection.send("P1");} catch (Exception e) {e.printStackTrace();} //player 1
//					}
//				}
//
//
//				//Player 1
//				if(data.toString().intern() == "P1: ROCK") {
//					plays++; //increase plays
//					clientPlays.add(0,"P1: ROCK"); //add rock
//					try { connection.send("P2GO");} catch (Exception e) {e.printStackTrace();} //enable play for p2
//				}
//				if(data.toString().intern() == "P1: PAPER") {
//					plays++; //increase plays
//					clientPlays.add(0,"P1: PAPER"); //add rock
//					try { connection.send("P2GO");} catch (Exception e) {e.printStackTrace();} //enable play for p2
//				}
//				if(data.toString().intern() == "P1: SCISSOR") {
//					plays++; //increase plays
//					clientPlays.add(0,"P1: SCISSOR"); //add rock
//					try { connection.send("P2GO");} catch (Exception e) {e.printStackTrace();} //enable play for p2
//				}
//				if(data.toString().intern() == "P1: LIZARD") {
//					plays++; //increase plays
//					clientPlays.add(0,"P1: LIZARD"); //add rock
//					try { connection.send("P2GO");} catch (Exception e) {e.printStackTrace();} //enable play for p2
//				}
//				if(data.toString().intern() == "P1: SPOCK") {
//					plays++; //increase plays
//					clientPlays.add(0,"P1: SPOCK"); //add rock
//					try { connection.send("P2GO");} catch (Exception e) {e.printStackTrace();} //enable play for p2
//				}
//
//				//Player 2
//				if(data.toString().intern() == "P2: ROCK") {
//					plays++; //increase plays
//					clientPlays.add(1,"P2: ROCK"); //add rock
//					//try { connection.send("P1GO");} catch (Exception e) {e.printStackTrace();} //enable play for p1
//				}
//				if(data.toString().intern() == "P2: PAPER") {
//					plays++; //increase plays
//					clientPlays.add(1,"P2: PAPER"); //add rock
//					//try { connection.send("P1GO");} catch (Exception e) {e.printStackTrace();} //enable play for p1
//				}
//				if(data.toString().intern() == "P2: SCISSOR") {
//					plays++; //increase plays
//					clientPlays.add(1,"P2: SCISSOR"); //add rock
//					//try { connection.send("P1GO");} catch (Exception e) {e.printStackTrace();} //enable play for p1
//				}
//				if(data.toString().intern() == "P2: LIZARD") {
//					plays++; //increase plays
//					clientPlays.add(1,"P2: LIZARD"); //add rock
//					//try { connection.send("P1GO");} catch (Exception e) {e.printStackTrace();} //enable play for p1
//				}
//				if(data.toString().intern() == "P2: SPOCK") {
//					plays++; //increase plays
//					clientPlays.add(1,"P2: SPOCK"); //add rock
//					//try { connection.send("P1GO");} catch (Exception e) {e.printStackTrace();} //enable play for p1
//				}
//
//				//System.out.println("Plays: "+plays);
//				//System.out.println("Replay: "+replay);
//				if (plays == 2 && replay == 0) {//send plays
//					try {connection.send(clientPlays.get(0)); connection.send(clientPlays.get(1));} catch (Exception e) {e.printStackTrace();} //send aponent card
//					plays = 0;//reset plays
//
//					//who won round?
//					int rwinner = roundWinner(clientPlays.get(0),clientPlays.get(1));
//					//update GUI
//					if (rwinner == 1) {
//						p1ScoreVal++;//increase player 1 score
//						p1Score.setText(""+p1ScoreVal); //update Server UI
//					}
//					else if(rwinner == 2) {
//						p2ScoreVal++;//increase player 2 score
//						p2Score.setText(""+p2ScoreVal); //update Server UI
//					}
//					//send winner to client
//					try { connection.send(""+rwinner);} catch (Exception e) {e.printStackTrace();} //winner
//
//
//					//delay
//					try {Thread.sleep(2000); }catch (Exception e) {e.printStackTrace();}
//
//					if(p1ScoreVal < 3 && p2ScoreVal < 3) {//keep playing
//					//resets images
//						try { connection.send("RESET");} catch (Exception e) {e.printStackTrace();} //winner
//					}
//
//					clientPlays.clear();//reset array
//					clientPlays.add(0,"");
//					clientPlays.add(1,"");
//
//					if(p1ScoreVal == 3) {
//						winner.setText("Player 1"); //update GUI
//						try { connection.send("WINNER1");} catch (Exception e) {e.printStackTrace();} //winner
//
//						p1Status.setText("NA");
//						p1Status.setTextFill(Color.BLACK);
//						p2Status.setText("NA");
//						p2Status.setTextFill(Color.BLACK);
//					}
//					if(p2ScoreVal == 3) {
//						winner.setText("Player 2"); //update GUI
//						try { connection.send("WINNER2");} catch (Exception e) {e.printStackTrace();} //winner
//
//						p1Status.setText("NA");
//						p1Status.setTextFill(Color.BLACK);
//						p2Status.setText("NA");
//						p2Status.setTextFill(Color.BLACK);
//					}
//				}
//
//				//-----------------------------------
//				//PLAY AGAIN
//				if(data.toString().intern() == "PLAYAGAIN1") {
//					p1Status.setText("YES");
//					p1Status.setTextFill(Color.GREEN);
//					replay++;//counter
//				}
//				if(data.toString().intern() == "PLAYAGAIN2") {
//					p2Status.setText("YES");
//					p2Status.setTextFill(Color.GREEN);
//					replay++;//counter
//				}
//
//				if(replay == 2) {
//					//reset all
//					p1Score.setText("0");
//					p2Score.setText("0");
//					p1ScoreVal = 0;
//					p2ScoreVal = 0;
//					winner.setText("NA");
//					try { connection.send("REPLAY");} catch (Exception e) {e.printStackTrace();} //winner
//
//
//
//
//					replay = 0;//reset replay
//
//
//				}
//
//			});
//		});
//	}//end of createServer()

	private void startGame(InputListener one, InputListener two) {

		// TODO: intiate a game between two players; most of createServer's functionality should be folded into this method

		new Thread(() -> {

			// TODO: actual game should be run in this thread so that multiple games can run at once

		}).start();

	}

	// returns 0 if player 1's play wins, 1 if player 2's play wins, or -1 if they match
	private int comparePlays(Play one, Play two) {

		if (one == ROCK) {

			if (two == PAPER) {
				return 1;
			} else if (two == SCISSORS) {
				return 0;
			} else if (two == LIZARD) {
				return 0;
			} else if (two == SPOCK) {
				return 1;
			} else {
				return -1;
			}

		} else if (one == PAPER) {

			if (two == ROCK) {
				return 0;
			} else if (two == SCISSORS) {
				return 1;
			} else if (two == LIZARD) {
				return 1;
			} else if (two == SPOCK) {
				return 0;
			} else {
				return -1;
			}

		} else if (one == SCISSORS) {

			if (two == ROCK) {
				return 1;
			} else if (two == PAPER) {
				return 0;
			} else if (two == LIZARD) {
				return 0;
			} else if (two == SPOCK) {
				return 1;
			} else {
				return -1;
			}

		} else if (one == LIZARD) {

			if (two == ROCK) {
				return 1;
			} else if (two == PAPER) {
				return 0;
			} else if (two == SCISSORS) {
				return 1;
			} else if (two == SPOCK) {
				return 0;
			} else {
				return -1;
			}

		} else { // one == spock

			if (two == ROCK) {
				return 0;
			} else if (two == PAPER) {
				return 1;
			} else if (two == SCISSORS) {
				return 0;
			} else if (two == LIZARD) {
				return 1;
			} else {
				return -1;
			}

		}

	}

// TODO: delete
//	public Integer roundWinner(String P1, String P2) {
//
//		if (P1 == "P1: ROCK" && (P2 == "P2: LIZARD" || P2 == "P2: SCISSOR") ) {//Rock beats lizard and scissors
//			return 1;//player 1 wins
//		}
//		if (P1 == "P1: PAPER" && (P2 == "P2: ROCK" || P2 == "P2: SPOCK") ) {//Paper beats Rock and Spock
//			return 1;//player 1 wins
//		}
//		if (P1 == "P1: SCISSOR" && (P2 == "P2: LIZARD" || P2 == "P2: PAPER") ) {//Scissors beats lizard and paper
//			return 1;//player 1 wins
//		}
//		if (P1 == "P1: LIZARD" && (P2 == "P2: PAPER" || P2 == "P2: SPOCK") ) {//Lizard beats paper and spock
//			return 1;//player 1 wins
//		}
//		if (P1 == "P1: SPOCK" && (P2 == "P2: ROCK" || P2 == "P2: SCISSOR") ) {//Pock beats rock and scissor
//			return 1;//player 1 wins
//		}
//
//		//-------------------------------------------------------------------------------------------------------
//
//		if (P2 == "P2: ROCK" && (P1 == "P1: LIZARD" || P1 == "P1: SCISSOR") ) {//Rock beats lizard and scissors
//			return 2;//player 2 wins
//		}
//		if (P2 == "P2: PAPER" && (P1 == "P1: ROCK" || P1 == "P1: SPOCK") ) {//Paper beats Rock and Spock
//			return 2;//player 2 wins
//		}
//		if (P2 == "P2: SCISSOR" && (P1 == "P1: LIZARD" || P1 == "P1: PAPER") ) {//Scissors beats lizard and paper
//			return 2;//player 2 wins
//		}
//		if (P2 == "P2: LIZARD" && (P1 == "P1: PAPER" || P1 == "P1: SPOCK") ) {//Lizard beats paper and spock
//			return 2;//player 2 wins
//		}
//		if (P2 == "P2: SPOCK" && (P1 == "P1: ROCK" || P1 == "P1: SCISSOR") ) {//Pock beats rock and scissor
//			return 2;//player 2 wins
//		}
//
//
//		return 0;//TIE
//
//	}
	
}
