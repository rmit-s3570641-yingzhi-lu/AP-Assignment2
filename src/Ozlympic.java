import Game.Games;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

/*
 * Ozympic Class
 *
 * This class is the entrance of whole project
 *
 * Created by Ningqi Lu on 3/22/2017.
 * Modified by both Ningqi Lu and Yingzhi Lu
 */
public class Ozlympic extends Application {

    private Driver driver = new Driver();
    private static String Type = null; //record the type of game selected in toggle group
    private String playerchoice = null;// recored the player coice
    private ArrayList<Map.Entry<String, String>> storeDecreasedScoreList = new ArrayList<>();// a Arraylist to store the ID and score

    private final TableView<GameResultHistory> scoreTable = new TableView<>();
    private final ObservableList<GameResultHistory> data = FXCollections.observableArrayList();//a collection to get data
    //private ArrayList<String[]> selectedAttendAthlete = new ArrayList<String[]>(); //attend athlete in every game
    private ArrayList<String[]> attendAthlete = new ArrayList<>(); //attend athlete in every game

    private Button start = new Button("Start Game"); //create the start button
    private Button starting = new Button("Ready");//create button to ready the game
    private Button btnRestart = new Button("Restart"); //create the restart button
    private Button showAllResults = new Button("All Results");
    @SuppressWarnings("rawtypes")
	private TableColumn athleteIDCol = new TableColumn("Athlete ID");
    @SuppressWarnings("rawtypes")
	private TableColumn athleteScoreCol = new TableColumn<>("Athlete Score");
    @SuppressWarnings("rawtypes")
	private TableColumn pointsCol = new TableColumn<>("Points");
    private VBox vBox = new VBox();// a VBox to hold all the game results

    public Ozlympic() throws IOException {

    }

    /*    public static void main(String[] args) throws IOException {

            Driver driver = new Driver();
            driver.mainMenu();

        }*/
    @SuppressWarnings("unchecked")
	@Override // Override the start method in the Application class
    public void start(Stage primaryStage) {

        scoreTable.getColumns().addAll(athleteIDCol, athleteScoreCol, pointsCol);
        scoreTable.setEditable(false);
        athleteIDCol.setStyle("-fx-alignment: CENTER;");
        athleteScoreCol.setStyle("-fx-alignment: CENTER;");
        pointsCol.setStyle("-fx-alignment: CENTER;");

        Scene scene = new Scene(getFirstPage(), 500, 350);
        primaryStage.setTitle("Ozlympic Game"); // Set the stage title
        primaryStage.setScene(scene); // Place the scene in the stage
        primaryStage.setResizable(false);

        primaryStage.show(); // Display the stage
    }


    /**
     * get the main page of the game and call the progress bar method
     *
     * @return VBox holding the main button and text on the first page
     */
    private VBox getFirstPage() {
        // Hold two buttons in an HBox
        VBox titleInfo = new VBox();
        titleInfo.setSpacing(30);
        titleInfo.setAlignment(Pos.TOP_CENTER);
        titleInfo.setPadding(new Insets(20));

        //create the title of the game
        Text gameTitle = new Text(20, 20, "Welcome to the game Ozlympic!");
        gameTitle.setFont(Font.font("Courier", FontWeight.BOLD, FontPosture.ITALIC, 25));

        // create the text to welcome the player and ask the selection
        Text welcomeAndAsk = new Text(60, 60, "The game will be starting soon~ \n   Please choose a game to run!");
        welcomeAndAsk.setFont(Font.font("Courier", FontWeight.BOLD, FontPosture.ITALIC, 15));
        titleInfo.getChildren().addAll(gameTitle, welcomeAndAsk);

        //Hold three radiobutton in gameselect vBox
        HBox gameselect = new HBox();
        gameselect.setSpacing(30);
        gameselect.setAlignment(Pos.TOP_CENTER);
        gameselect.setPadding(new Insets(20));

        //create a radio box to select the game
        ToggleGroup group = new ToggleGroup();
        RadioButton swimming = new RadioButton("Swimming");
        swimming.setToggleGroup(group);
        swimming.setUserData("swimming");

        RadioButton cycling = new RadioButton("Cycling");
        cycling.setToggleGroup(group);
        cycling.setUserData("cycling");

        RadioButton running = new RadioButton("Running");
        running.setToggleGroup(group);
        running.setUserData("running");

        gameselect.getChildren().addAll(swimming, cycling, running);

        start.setAlignment(Pos.TOP_CENTER);
        start.setMinWidth(100);
        start.setDisable(true);

        showAllResults.setAlignment(Pos.TOP_CENTER);
        showAllResults.setMinWidth(100);
        showAllResults.setDisable(true);

        // return the type selected
        group.selectedToggleProperty().addListener((ov, old_toggle, new_toggle) -> {
            Type = group.getSelectedToggle().getUserData().toString();
            System.out.println(Type);
            start.setDisable(false);
        });

        //BorderPane created,put the hBox into the boarderPane
        VBox vbox = new VBox();
        vbox.setSpacing(10);
        vbox.setAlignment(Pos.TOP_CENTER);

        HBox hbtn = new HBox();
        hbtn.setSpacing(30);
        hbtn.setAlignment(Pos.TOP_CENTER);
        hbtn.getChildren().addAll(start, showAllResults);

        HBox hb = new HBox(initProgressBar());
        hb.setSpacing(10);
        hb.setAlignment(Pos.TOP_CENTER);
        hb.setVisible(false);

        // Create and register the handler
        start.setOnAction((ActionEvent e) -> {
            //selectedAttendAthlete.clear();
            if (group.getSelectedToggle() != null) {
                hb.setVisible(true);
                start.setDisable(true);
                thread.restart();
                thread.setOnSucceeded(event -> {
                    try {
                        driver.setType(Type);
                        driver.showAthleteinSelectedGame();
                        //System.out.println(Games.getAttendAthlete().size());
                        attendAthlete = Games.getAttendAthlete();
                        selectAthleteByPlayer();
                        //getPredictStage();
                    } catch (IOException | SQLException | ClassNotFoundException e1) {
                        e1.printStackTrace();
                    }
                });
            } else {
                start.setDisable(true);
            }
        });

        // Create and register the handler
        showAllResults.setOnAction((ActionEvent e) -> {
            Stage stage = new Stage();
            if (vBox.getScene() == null) {
                Scene s = new Scene(vBox);
                stage.setScene(s);
            } else {
                stage.setScene(vBox.getScene());
            }
            stage.setTitle("All Game Results");

            stage.show();
        });

        vbox.getChildren().addAll(titleInfo, gameselect, hbtn, hb);
        return vbox;
    }

    /**
     * get the result page which contains tableview and predict results and game details like referee
     */
    @SuppressWarnings("unchecked")
	private void getResultsTable() {
        Stage s2 = new Stage();
        s2.setTitle("Game Results");
        s2.setResizable(false);

        //draw the table which is used to show results
        VBox vBox = new VBox();
        vBox.setAlignment(Pos.TOP_CENTER);
        vBox.setSpacing(10);
        vBox.setPadding(new Insets(10));

        //a label to show the results
        //create the title of the game
        Text gameResult = new Text(20, 20, "Game Results");
        gameResult.setFont(Font.font("Courier", FontWeight.BOLD, FontPosture.ITALIC, 25));

        //create the table to store the data
        scoreTable.setItems(data);


        athleteIDCol.setCellValueFactory(
                new PropertyValueFactory<>("athleteID"));

        athleteScoreCol.setCellValueFactory(
                new PropertyValueFactory<>("athleteScore"));

        pointsCol.setCellValueFactory(
                new PropertyValueFactory<>("points"));

        //bind all data to the table view
        scoreTable.setEditable(false);
        //scoreTable.getColumns().addAll(athleteIDCol, athleteScoreCol,pointsCol);
        scoreTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        scoreTable.setPadding(new Insets(5));

        btnRestart.setMinWidth(100);

        Label isPredicted = new Label();
        isPredicted.setFont(Font.font("Courier", 14));
        isPredicted.setTextFill(Color.RED);

        vBox.getChildren().addAll(gameResult, scoreTable, gamedetailInfoShow(), isPredicted, btnRestart);
        vBox.setSpacing(10);
        if (playerchoice.equals(storeDecreasedScoreList.get(0).getKey())) {
            isPredicted.setText("Congratulation, your prediction is right!");
        } else {
            isPredicted.setText("Sorry, next time you could predit the right one :)");
        }

        btnRestart.setOnAction(event -> {
            s2.close();
            attendAthlete.clear();
            storeDecreasedScoreList.clear();
            //selectedAttendAthlete.clear();
            start.setDisable(false);
            showAllResults.setDisable(false);
        });

        Scene ss = new Scene(vBox, 400, 380);
        s2.setScene(ss);
        s2.show();
        //return s2;
    }

    /**
     * show the game info like game ID, referee ID and time stamp
     *
     * @return HBox Node contain all the information
     */
    private HBox gamedetailInfoShow() {
        //add the detail of the game such as referee and play time
        Label gameIdInfo = new Label(" GameID:");
        Label refereeInfo = new Label("Referee:");
        Label timeStamp = new Label("Time:");
        Text gameIdShowInfo = new Text();
        Text refereeShowInfo = new Text();
        Text timeStampShowInfo = new Text();

        gameIdShowInfo.setText(driver.getGameID());
        refereeShowInfo.setText(driver.getReferee());
        timeStampShowInfo.setText(driver.getTimestamp().toString());

        HBox gameOtherInfo = new HBox();
        gameOtherInfo.setSpacing(10);
        gameOtherInfo.setAlignment(Pos.BASELINE_LEFT);
        //gameOtherInfo.setPadding(new Insets(10));

        gameOtherInfo.getChildren().addAll(gameIdInfo, gameIdShowInfo, refereeInfo, refereeShowInfo, timeStamp, timeStampShowInfo);
        return gameOtherInfo;
    }

    private HBox showFirstThreeAthlete() {

        Label athleteID = new Label(" The winners of game are: ");
        Label firstAtheleteID = new Label(storeDecreasedScoreList.get(0).getKey());
        Label secondAtheleteID = new Label(storeDecreasedScoreList.get(1).getKey());
        Label thirdAtheleteID = new Label(storeDecreasedScoreList.get(2).getKey());

        HBox vb = new HBox();
        vb.setSpacing(10);
        vb.setAlignment(Pos.TOP_LEFT);
        vb.setStyle("-fx-background-color:#90b7dd;");

        vb.getChildren().addAll(athleteID, firstAtheleteID, secondAtheleteID, thirdAtheleteID);
        return vb;
    }

    private void selectAthleteByPlayer() throws SQLException, IOException, ClassNotFoundException {
        Stage selectAthlete = new Stage();
        /*attendAthlete.clear();
        attendAthlete = Games.getAttendAthlete();*/

        //selectAthlete stage title
        VBox titleInfo = new VBox();
        titleInfo.setSpacing(10);
        titleInfo.setAlignment(Pos.TOP_CENTER);
        titleInfo.setPadding(new Insets(10));

        Text selectTitle = new Text(20, 20, "Here is the athlete will attend the game\n  Please select 4-8 athletes to attend!");
        selectTitle.setFont(Font.font("Courier", FontWeight.BOLD, FontPosture.ITALIC, 15));
        titleInfo.getChildren().addAll(selectTitle);

        FlowPane select = new FlowPane();
        select.setVgap(20);
        select.setHgap(20);
        select.setAlignment(Pos.TOP_LEFT);
        select.setPadding(new Insets(30));
        select.getChildren().add(titleInfo);

        String[] names = new String[attendAthlete.size()];
        CheckBox[] cbs = new CheckBox[names.length];
        HashSet<String> selectedAthleteID = new HashSet<>();


        for (int i = 0; i < names.length; i++) {
            //System.out.println(attendAthlete.size());
            names[i] = attendAthlete.get(i)[0];
        }

        for (int i = 0; i < names.length; i++) {

            cbs[i] = new CheckBox(names[i]);
            cbs[i].setSelected(false);
            select.getChildren().add(cbs[i]);
            int finalI = i;
            cbs[i].selectedProperty().addListener((observable, oldValue, newValue) -> {
                        if (cbs[finalI].isSelected()) {
                            System.out.println(cbs[finalI].getText());
                            selectedAthleteID.add(cbs[finalI].getText());
                            System.out.println(selectedAthleteID);
                            //System.out.println(attendAthlete.get(finalI));
                        } else {
                            System.out.println(cbs[finalI].getText());
                            selectedAthleteID.remove(cbs[finalI].getText());
                            System.out.println(selectedAthleteID);
                        }
                    }
            );
        }

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.TOP_CENTER);
        hBox.setMinSize(300, 50);
        hBox.getChildren().add(starting);

        select.getChildren().add(hBox);

        starting.setOnAction(event -> {

            ArrayList<String[]> selectedAttendAthlete = new ArrayList<>(); //attend athlete in every game
            for (int i = 0; i < names.length; i++) {
                if (selectedAthleteID.contains(attendAthlete.get(i)[0])) {
                    selectedAttendAthlete.add(attendAthlete.get(i));
                }
            }
            if(selectedAttendAthlete.size()>=4 && selectedAttendAthlete.size()<=8){
                Games.setAttendAthlete(selectedAttendAthlete);
                try {
                    driver.startGame();
                    driver.displayAllPoints();
                    this.storeDecreasedScoreList = driver.getStoreDecreasedScoreList();
                    inputDataToTableView();
                    getPredictStage();
                    //Games.getAttendAthlete().clear();
                    selectAthlete.close();
                    selectedAttendAthlete.clear();
                } catch (IOException | ClassNotFoundException | SQLException e) {
                    e.printStackTrace();
                }
            }else{
                Stage stage=new Stage();
                Label warning =new Label("Please select athletes between 4-8");
                warning.setPadding(new Insets(30));
                warning.setAlignment(Pos.CENTER);
                warning.setFont(Font.font("Courier", FontWeight.BOLD, FontPosture.ITALIC, 15));
                Scene scene=new Scene(warning,300,100);
                stage.setScene(scene);
                stage.setTitle("WARNING");
                stage.setResizable(false);
                stage.show();
            }

        });

        Scene selectWindow = new Scene(select, 360, 300);
        selectAthlete.setTitle("Choose the player");
        selectAthlete.setResizable(false);
        selectAthlete.setScene(selectWindow);
        selectAthlete.show();
    }

    /**
     * a method to call the class GameResultHistory inorder to bind the data with table view
     */
    private void inputDataToTableView() {

        if (storeDecreasedScoreList.size() != 0) {
            // clear the table before reload the game
            scoreTable.getItems().clear();
            //input the tableview data
            data.add(new GameResultHistory(storeDecreasedScoreList.get(0).getKey(),
                    storeDecreasedScoreList.get(0).getValue(), "5"));
            data.add(new GameResultHistory(storeDecreasedScoreList.get(1).getKey(),
                    storeDecreasedScoreList.get(1).getValue(), "2"));
            data.add(new GameResultHistory(storeDecreasedScoreList.get(2).getKey(),
                    storeDecreasedScoreList.get(2).getValue(), "1"));

            for (int i = 3; i < Games.attendAthlete.size(); i++) {
                data.add(new GameResultHistory(storeDecreasedScoreList.get(i).getKey(),
                        storeDecreasedScoreList.get(i).getValue(), "0"));
            }
        }
    }

    /**
     *  a predict page which show all the athlete attend the game
     * let the player select one to predict
     * @throws IOException
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    @SuppressWarnings("JavaDoc")
    private void getPredictStage() throws IOException, SQLException, ClassNotFoundException {
        //Driver driver = new Driver();
        //create a new stage to pop up a new window
        Stage predict = new Stage();

        //create new elements of in the new window
        //Label warningMessage=new Label("message");
        VBox titleInfo = new VBox();
        titleInfo.setSpacing(10);
        titleInfo.setAlignment(Pos.TOP_CENTER);
        titleInfo.setPadding(new Insets(10));

        //create the title of the game
        Text predictTitle = new Text(20, 20, "Here is the athlete you selected! \n  Please select one to predicted!");
        predictTitle.setFont(Font.font("Courier", FontWeight.BOLD, FontPosture.ITALIC, 15));
        titleInfo.getChildren().addAll(predictTitle);

        //Hold three radiobutton in gameselect vBox
        VBox winnerSelect = new VBox();
        winnerSelect.setSpacing(15);
        winnerSelect.setAlignment(Pos.TOP_CENTER);
        winnerSelect.setPadding(new Insets(5));

        ChoiceBox<Object> cb = new ChoiceBox<>();
        for (int i = 0; i < Games.attendAthlete.size(); i++) {
            cb.getItems().addAll(FXCollections.observableArrayList(
                    Games.attendAthlete.get(i)[0] + " " + Games.attendAthlete.get(i)[1] + " "
                            + Games.attendAthlete.get(i)[2] + " " + Games.attendAthlete.get(i)[3] + " "
                            + Games.attendAthlete.get(i)[4]));
        }

        //set choice box default selected is first item
        cb.getSelectionModel().selectFirst();
        playerchoice = cb.getSelectionModel().getSelectedItem().toString().substring(0, 6);

        //and event handling to record the predict athlete
        cb.setOnAction(event -> {

            String selectedAthlete = cb.getSelectionModel().getSelectedItem().toString();
            playerchoice = selectedAthlete.substring(0, 6);

            System.out.println(playerchoice);
        });


        cb.setTooltip(new Tooltip("Select the winner!"));
        winnerSelect.getChildren().addAll(cb);

        //create the start button
        Button btnPredict = new Button("Predict");
        btnPredict.setMinWidth(100);


        //Put in all elements to a VBox
        VBox wholePredictStage = new VBox();
        wholePredictStage.setSpacing(10);
        wholePredictStage.setAlignment(Pos.TOP_CENTER);
        wholePredictStage.getChildren().addAll(titleInfo, winnerSelect, btnPredict);

        // Create and register the handler
        btnPredict.setOnAction((ActionEvent e) -> {
            //a vbox to hold all the game results
            vBox.setAlignment(Pos.TOP_LEFT);
            vBox.setPadding(new Insets(15));
            vBox.setSpacing(5);
            Label space = new Label("                            ");
            vBox.getChildren().addAll(gamedetailInfoShow(), showFirstThreeAthlete(), space);
            predict.close();
            getResultsTable();

        });

        Scene secondWindow = new Scene(wholePredictStage, 400, 180);
        predict.setTitle("Predict the winner!");
        predict.setScene(secondWindow);
        predict.setResizable(false);
        predict.show();
    }

    /**
     * a thread to control the progress bar
     */
    private final Service<Integer> thread = new Service<Integer>() {

       
		public Task<Integer> createTask() {
            return new Task<Integer>() {
                @Override
                protected Integer call() throws Exception {
                    int iterations;
                    for (iterations = 0; iterations <= 1000; iterations += 4) {
                        updateProgress(iterations, 1000);
                        Thread.sleep(10);
                        //System.out.println(iterations);
                    }
                    return iterations;
                }
            };
        }

    };

    /**
     * a progress bar initiation method
     *
     * @return HBox to put on the firstpage
     */
    private HBox initProgressBar() {
        //create a progress bar and progress indicatior
        ProgressBar progressBar = new ProgressBar();
        progressBar.setMinSize(200, 20);

        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setMinSize(20, 20);

        //set value of progress bar and progress indicator through thread
        progressBar.progressProperty().bind(thread.progressProperty());
        progressIndicator.progressProperty().bind(thread.progressProperty());

        HBox hb = new HBox();
        hb.setPadding(new Insets(15));
        hb.setAlignment(Pos.TOP_CENTER);
        hb.setSpacing(5);
        hb.getChildren().addAll(progressBar, progressIndicator);

        return hb;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
