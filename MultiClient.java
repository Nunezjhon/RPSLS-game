package p3_client;

/*
 *  MultiClient.java
 *
 *  Author(s): Joseph Canning (jec2)
 *
 *  Description:
 *
 *      The Client class is one of two programs for playing an online game of rock-paper-scissors-lizard-spock. This
 *      class is used by a player to connect to a server. All player interactions in the game are handled in this class
 *      as well as all necessary information for the player to know regarding the status of the game. The server and
 *      client communicate using plain text over Sockets which is read using a Scanner and written using a PrintWriter.
 */

// TODO: update the UI in this class to be like Jhon's project

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import static p3_server.InputListener.*;

public class MultiClient extends Application {

    // primitives/strings
    private final int WIN_WIDTH = 300;                  // width of the UI window
    private final int WIN_HEIGHT = 400;                 // height of the UI window
    private final int IMG_SIZE = 40;                    // width/height of images
    private final int NUM_PLAYS = 5;                    // number of hand signal choices
    private final String IMG_PATH = "/images/";         // relative path to image files
    private final String YP_TEXT = "Your score: ";      // UI prompts, labels, etc.
    private final String OP_TEXT = "Opponent's score: ";// |
    private final String PLAY_TEXT = "Opponent played: ";//\
    private int score;                                  // player's score
    private int opScore;                                // opponent's score
    private int port;                                   // port to listen on
    private int id;                                     // identifies client to server; 0 or 1
    private String address;                             // IP address of server to which the client is connecting
    private volatile String last;                       // last message from server

    // UI
    private Scene menu;
    private VBox vbox;
    private HBox playBox;
    private HBox portBox;
    private HBox ipBox;
    private HBox reBox;
    private Label scoreText;
    private Label otherScore;
    private Label otherPlay;
    private Label message;
    private Label portText;
    private Label ipText;
    private Label rematch;
    private TextField portField;
    private TextField ipField;
    private Button[] plays;
    private Button portEnter;
    private Button ipEnter;
    private Button connect;
    private Button yes;
    private Button no;
    private Image[] images;

    // TCP/IP
    private Socket socket;      // connection end-point
    private Scanner in;         // reads input from server
    private PrintWriter out;    // writes output to server

    @Override
    public void init() { // initialize all UI objects

        // vbox
        vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        vbox.setSpacing(5);

        // game info
        scoreText = new Label(YP_TEXT + score);
        scoreText.setPrefSize(120, 20);
        scoreText.setAlignment(Pos.CENTER);
        otherScore = new Label(OP_TEXT + opScore);
        otherScore.setPrefSize(120, 20);
        otherScore.setAlignment(Pos.CENTER);
        otherPlay = new Label(PLAY_TEXT);
        otherPlay.setPrefSize(200, 20);
        otherPlay.setAlignment(Pos.CENTER);
        message = new Label("Enter address and port");
        message.setPrefSize(200, 20);
        message.setAlignment(Pos.CENTER);
        vbox.getChildren().addAll(scoreText, otherScore, otherPlay, message);

        // images
        images = new Image[NUM_PLAYS];
        images[0] = new Image(IMG_PATH + "rock.png", IMG_SIZE, IMG_SIZE, false, true);
        images[1] = new Image(IMG_PATH + "paper.png", IMG_SIZE, IMG_SIZE, false, true);
        images[2] = new Image(IMG_PATH + "scissors.png", IMG_SIZE, IMG_SIZE, false, true);
        images[3] = new Image(IMG_PATH + "lizard.png", IMG_SIZE, IMG_SIZE, false, true);
        images[4] = new Image(IMG_PATH + "spock.png", IMG_SIZE, IMG_SIZE, false, true);

        // play buttons
        plays = new Button[NUM_PLAYS];
        playBox = new HBox();
        playBox.setSpacing(2);
        playBox.setPadding(new Insets(0, 0, 20, 0));
        playBox.setAlignment(Pos.CENTER);
        initButtons();
        vbox.getChildren().add(playBox);

        // address entry
        ipText = new Label("IP");
        ipText.setPrefSize(30, 20);
        ipField = new TextField("127.0.0.1");
        ipField.setPrefSize(75, 20);
        ipEnter = new Button("Enter");
        ipEnter.setPrefSize(50, 20);
        ipEnter.setOnAction(e -> {

            address = ipField.getText();

            if (port > 0) { // enable connect if an address and a port are given
                connect.setDisable(false);
            }

        });
        ipBox = new HBox();
        ipBox.setAlignment(Pos.CENTER);
        ipBox.setSpacing(5);
        ipBox.setPadding(new Insets(20, 0, 0, 0));
        ipBox.getChildren().addAll(ipText, ipField, ipEnter);
        vbox.getChildren().add(ipBox);

        // port entry
        portText = new Label("Port");
        portText.setPrefSize(30, 20);
        portField = new TextField("5555");
        portField.setPrefSize(75, 20);
        portEnter = new Button("Enter");
        portEnter.setPrefSize(50, 20);
        portEnter.setOnAction(e -> {

            port = Integer.parseInt(portField.getText());

            if (address != null && !address.equals("")) { // enable connect if an address and a port are given
                connect.setDisable(false);
            }

        });
        portBox = new HBox();
        portBox.setAlignment(Pos.CENTER);
        portBox.setSpacing(5);
        portBox.setPadding(new Insets(0, 0, 20, 0));
        portBox.getChildren().addAll(portText, portField, portEnter);
        vbox.getChildren().add(portBox);

        // connect button
        connect = new Button("Connect");
        connect.setDisable(true);
        connect.setPrefSize(75, 20);
        setConnect();
        vbox.getChildren().add(connect);

        // rematch label
        rematch = new Label("Rematch?");
        rematch.setPrefSize(100, 20);
        rematch.setAlignment(Pos.CENTER);
        rematch.setPadding(new Insets(20, 0, 0, 0));
        rematch.setVisible(false);
        vbox.getChildren().add(rematch);

        // rematch buttons
        reBox = new HBox();
        reBox.setSpacing(2);
        reBox.setAlignment(Pos.CENTER);
        yes = new Button("Yes");
        yes.setPrefSize(45, 20);
        yes.setDisable(true);
        yes.setVisible(false);
        yes.setOnAction(e -> {

            send(YES_SIG + id);
            rematch.setVisible(false);
            yes.setDisable(true);
            yes.setVisible(false);
            no.setDisable(true);
            no.setVisible(false);
            score = 0;
            opScore = 0;
            Platform.runLater(() -> {

                scoreText.setText(YP_TEXT + score);
                otherScore.setText(OP_TEXT + opScore);
                otherPlay.setText(PLAY_TEXT);

            });

        });
        no = new Button("No");
        no.setPrefSize(45, 20);
        no.setDisable(true);
        no.setVisible(false);
        no.setOnAction(e -> stop());
        reBox.getChildren().addAll(yes, no);
        vbox.getChildren().add(reBox);

    }

    @Override
    public void start(Stage stage) { // display stage

        // stage
        menu = new Scene(vbox, WIN_WIDTH, WIN_HEIGHT);
        stage.setScene(menu);
        stage.setTitle("RPSLS Client");
        stage.setResizable(false);
        stage.show();

    }

    @Override
    public void stop() { // stop the program

        disconnect();
        System.exit(1);

    }

    // disconnect from server
    private void disconnect() {

        new Thread(() -> {

            try {

                send(NO_SIG + id);
                socket.close();
                delay();
                in.close();
                out.close();

            } catch (Exception e) {

                e.printStackTrace();

                if (e instanceof IOException) {
                    System.err.println("\nCould not disconnect from server");
                } else {
                    System.err.println("\nNot connected to server");
                }

            }

        }).start();

        score = 0;
        opScore = 0;

        Platform.runLater(() -> { // clear UI

            otherPlay.setText(PLAY_TEXT);
            scoreText.setText(YP_TEXT + score);
            otherScore.setText(OP_TEXT + opScore);

            if (message.getText().toLowerCase().contains("disconnect")) {
                message.setText("Disconnected");
            }

            for (Button b : plays) {
                b.setDisable(true);
            }

        });

    }

    // initialize the buttons representing plays
    private void initButtons() {

        for (int i = 0; i < NUM_PLAYS; i++) { // add buttons

            plays[i] = new Button();
            plays[i].setPrefSize(IMG_SIZE, IMG_SIZE);
            ImageView view = new ImageView(images[i]);
            view.setFitWidth(IMG_SIZE);
            view.setFitHeight(IMG_SIZE);
            plays[i].setGraphic(view);
            plays[i].setDisable(true);
            playBox.getChildren().add(plays[i]);

        }

        for (int i = 0; i < NUM_PLAYS; i++) { // set button actions

            final int j = i; // for lambda

            plays[i].setOnAction(e -> {

                new Thread(() -> {

                    for (Button b : plays) {
                        Platform.runLater(() -> b.setDisable(true));
                    }

                    synchronized (this) {

                        send(PLAY_SIG);
                        delay();
                        send("" + j);

                    }

                    Platform.runLater(() -> message.setText("Opponent's turn"));

                }).start();

            });

        }

    }

    // set connect to connect to a server
    private void setConnect() {

        connect.setText("Connect");
        connect.setOnAction(e -> {

            try {
                socket = new Socket(address, port);
            } catch (Exception x) {

                x.printStackTrace();
                System.err.println("\nCould not connect to server");

            }

            try {

                listen(socket);
                send("client joined");
                setDisconnect();

            } catch (Exception x) {

                x.printStackTrace();
                System.err.println("\nUnable to read from server IO streams");

            } finally {
                System.gc();
            }

        });

    }

    // read input from server and respond
    public void listen(Socket socket) throws Exception {

        in = new Scanner(socket.getInputStream());
        out = new PrintWriter(socket.getOutputStream(), true);
        new Thread(() -> {

            while (in.hasNextLine()) {

                last = in.nextLine();

                if (last.equals(WAIT_SIG)) { // connection successful; must wait for player 2

                    Platform.runLater(() -> message.setText("Waiting for opponent"));
                    id = 0;

                }

                else if (last.contains(WEL_SIG)) { // connection successful

                    Platform.runLater(() -> message.setText("Connected"));
                    id = Integer.parseInt(last.substring(WEL_SIG.length()));

                }

                else if (last.equals(TURN_SIG)) { // client's turn; enable play buttons

                    Platform.runLater(() -> {

                        message.setText("Your turn");

                        for (Button b : plays) {
                            b.setDisable(false);
                        }

                    });

                }

                else if (last.equals(WIN_SIG)) {

                    score++;
                    Platform.runLater(() -> message.setText("You won the round!"));
                    Platform.runLater(() -> scoreText.setText(YP_TEXT + score));

                }

                else if (last.equals(LOSE_SIG)) {

                    opScore++;
                    Platform.runLater(() -> message.setText("You lost the round!"));
                    Platform.runLater(() -> otherScore.setText(OP_TEXT + opScore));

                }

                else if (last.equals(TIE_SIG)) {
                    Platform.runLater(() -> message.setText("You tied!"));
                }

                else if (last.contains(OP_SIG)) {
                    Platform.runLater(() -> otherPlay.setText(PLAY_TEXT + last.substring(OP_SIG.length())));
                }

                else if (last.equals(END_SIG)) {

                    if (score > opScore) {
                        Platform.runLater(() -> message.setText("You won the game! Rematch?"));
                    } else {
                        Platform.runLater(() -> message.setText("You lost the game! Rematch?"));
                    }

                    Platform.runLater(() -> {

                        rematch.setVisible(true);
                        yes.setDisable(false);
                        yes.setVisible(true);
                        no.setDisable(false);
                        no.setVisible(true);

                    });

                }

                else if (last.contains(FORCE_SIG)) {

                    int source = Integer.parseInt(last.substring(FORCE_SIG.length()));
                    disconnect();
                    Platform.runLater(() -> {

                        setConnect();
                        rematch.setVisible(false);
                        yes.setDisable(true);
                        yes.setVisible(false);
                        no.setDisable(true);
                        no.setVisible(false);

                        switch (source) {

                            case -1 : message.setText("Server disconnect"); break;
                            case 0  : message.setText("Player 1 quit, disconnecting"); break;
                            case 1  : message.setText("Player 2 quit, disconnecting"); break;

                        }

                    });

                }

            }

        }).start();

    }

    // set connect to disconnect from a server
    private void setDisconnect() {

        connect.setText("Disconnect");
        connect.setOnAction(e -> {

            disconnect();
            setConnect();

        });

    }

    // send a signal to the server
    private void send(String message) {

        out.println(message);
        out.flush();

    }

    // wait for a tenth of a second
    private void delay() {

        try {
            Thread.sleep(100);
        } catch (InterruptedException x) {
            x.printStackTrace();
        }

    }

}
