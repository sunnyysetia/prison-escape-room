package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.GameState;
import nz.ac.auckland.se206.SceneManager;
import nz.ac.auckland.se206.SceneManager.AppUi;
import nz.ac.auckland.se206.gpt.ChatMessage;
import nz.ac.auckland.se206.gpt.GptPromptEngineering;
import nz.ac.auckland.se206.gpt.openai.ApiProxyException;
import nz.ac.auckland.se206.gpt.openai.ChatCompletionRequest;
import nz.ac.auckland.se206.gpt.openai.ChatCompletionResult;
import nz.ac.auckland.se206.gpt.openai.ChatCompletionResult.Choice;
import nz.ac.auckland.se206.speech.TextToSpeech;

/** Controller class for the room view. */
public class EscapeRoomController {

  // This pane contains all the three panes below, we move this pane left and right
  @FXML private Pane roomCollectionPane;

  // These three panes below are the individual rooms
  @FXML private Pane prisonCellPane;
  @FXML private Pane kitchenPane;
  @FXML private Pane guardRoomPane;

  // Shared FXML
  @FXML private Group chatGroup;
  @FXML private Group computerGroup;
  @FXML private Label timerLabel;
  @FXML private Label hintLabel;
  @FXML private Rectangle dimScreen;
  @FXML private ImageView leftButton;
  @FXML private ImageView rightButton;
  @FXML private Circle notifCircle;
  @FXML private ImageView torchButton;
  @FXML private SVGPath uvLightEffect;

  // Kitchen FXML
  @FXML private Rectangle cuttingboard;
  @FXML private Rectangle oven;
  @FXML private Rectangle plates;
  @FXML private Rectangle extinguisher;
  @FXML private Rectangle kettle;
  @FXML private Rectangle clock;
  @FXML private Rectangle toaster;
  @FXML private Group torchGetGroup;

  // Cell FXML
  @FXML private Text uvLightText;
  @FXML private Rectangle sink;
  @FXML private Rectangle toilet;
  @FXML private Rectangle shelf;
  @FXML private Rectangle pillow;
  @FXML private Rectangle newspaper;
  @FXML private Rectangle table;

  // Guard's Room FXML
  @FXML private Rectangle circuit;
  @FXML private Rectangle computer;
  @FXML private Button computerCloseButton;
  @FXML private Rectangle computerDimScreen;
  @FXML private TextField computerPasswordField;
  @FXML private TextArea computerConsoleTextArea;
  @FXML private Button computerLoginButton;
  @FXML private AnchorPane endingControlAnchorPane;
  @FXML private AnchorPane computerConsoleAnchorPane;

  @FXML private ImageView guardRoomDarkness;
  @FXML private Group circuitGroup;
  @FXML private Label memoryCountdownLabel;
  @FXML private Button goBackMemory;
  @FXML private Button checkGuessMemory;
  @FXML private Rectangle a1;
  @FXML private Rectangle b1;
  @FXML private Rectangle c1;
  @FXML private Rectangle a2;
  @FXML private Rectangle b2;
  @FXML private Rectangle c2;
  @FXML private Rectangle a3;
  @FXML private Rectangle b3;
  @FXML private Rectangle c3;
  @FXML private Rectangle a4;
  @FXML private Rectangle b4;
  @FXML private Rectangle c4;
  @FXML private Rectangle a5;
  @FXML private Rectangle b5;
  @FXML private Rectangle c5;

  private List<String> allSwitches =
      new ArrayList<>(
          List.of(
              "a1", "b1", "c1", "a2", "b2", "c2", "a3", "b3", "c3", "a4", "b4", "c4", "a5", "b5",
              "c5"));
  private List<String> switchesToRecall = new ArrayList<>();
  private List<String> playerChoices = new ArrayList<>();

  // Chat fxml
  @FXML private Button sendButton;
  @FXML private TextField messagesTextField;
  @FXML private VBox messagesVertBox;
  @FXML private ScrollPane chatScrollPane;
  @FXML private Label phoneNameLabel;

  // Shared
  private int remainingSeconds = 120;
  private Timeline timer;

  // Chat
  private HashMap<GameState.State, ChatCompletionRequest> chatCompletionRequests =
      new HashMap<GameState.State, ChatCompletionRequest>();

  // UV code
  private HashMap<Integer, int[]> uvCodeLocations =
      new HashMap<Integer, int[]>() {
        {
          put(-45, new int[] {733, 300});
          put(-30, new int[] {734, 187});
          put(30, new int[] {233, 623});
          put(31, new int[] {33, 221});
          put(66, new int[] {558, 374});
          put(19, new int[] {361, 623});
          put(32, new int[] {362, 148});
          put(-23, new int[] {809, 136});
        }
      };

  private Tooltip currentTooltip; // Maintain a reference to the current tooltip

  ///////////////
  // Shared
  ///////////////
  /**
   * Initializes the room view, it is called when the room loads.
   *
   * @throws ApiProxyException
   */
  public void initialize() throws ApiProxyException {
    // Configure the timer length based on what the user selected.
    remainingSeconds = GameState.time;

    // Set the state of the game.
    GameState.state = GameState.State.INTRO;

    // Start a timer for the game.
    startTimer();

    // Update the UI label to display the timer.
    updateTimerLabel();

    // Initialise memory recall game.
    initialiseMemoryGame();

    // Binds send button so that it is disabled while gpt is writing a message.
    sendButton.disableProperty().bind(GameState.gptThinking);

    // Set the hint label to display 0 hints.
    hintLabel.setText("0");

    // Generate a different passcode everytime
    Object[] uvRotateAngles = uvCodeLocations.keySet().toArray();
    int randomAngle = (int) uvRotateAngles[(int) (Math.random() * uvRotateAngles.length)];
    int[] uvCodeLocation = uvCodeLocations.get(randomAngle);
    uvLightText.setRotate(randomAngle);
    uvLightText.setManaged(false);
    uvLightText.xProperty().setValue(uvCodeLocation[0]);
    uvLightText.yProperty().setValue(uvCodeLocation[1]);
    // debugging
    System.out.println("uvLightText R: " + uvLightText.getRotate() + " Value: " + randomAngle);
    System.out.println("uvLightText X: " + uvLightText.getX() + " Value: " + uvCodeLocation[0]);
    System.out.println("uvLightText Y: " + uvLightText.getY() + " Value: " + uvCodeLocation[1]);
    System.out.println("uvLightText Parent: " + uvLightText.getParent());

    GameState.uvPassword = (int) (Math.random() * 100000000);
    uvLightText.setText(Integer.toString(GameState.uvPassword));
    System.out.println("uvPassword: " + GameState.uvPassword);

    chatScrollPane.setFitToWidth(true);
    messagesVertBox
        .heightProperty()
        .addListener(
            new ChangeListener<Number>() {
              @Override
              public void changed(
                  ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                chatScrollPane.setVvalue((Double) newValue);
              }
            });

    computerLoginButton.setOnAction(
        (EventHandler<ActionEvent>)
            event -> {
              // Only runs if the computer is not logged in
              if (!GameState.computerLoggedIn) {
                if (computerPasswordField.getText().isEmpty()) {
                  return;
                }
                String password = computerPasswordField.getText();
                computerPasswordField.clear();
                computerConsoleTextArea.setText(
                    computerConsoleTextArea.getText() + "\nC:\\PrisonPC\\>" + password);
                if (password.equals(GameState.uvPassword + "")) {
                  computerConsoleAnchorPane.setVisible(false);
                  computerConsoleAnchorPane.setDisable(true);
                  endingControlAnchorPane.setVisible(true);
                  endingControlAnchorPane.setDisable(false);
                  GameState.computerLoggedIn = true;
                } else {
                  Thread writerThread =
                      new Thread(
                          () -> {
                            typeWrite(
                                computerConsoleTextArea,
                                "\n" + "System:>Incorrect Password!\n" + "System:>Enter Password:",
                                15);
                          });
                  writerThread.setDaemon(true);
                  writerThread.start();
                }
              }
            });

    phoneNameLabel
        .textProperty()
        .bind(
            Bindings.createStringBinding(
                () -> {
                  String string;
                  if (GameState.gptThinking.get()) {
                    string = "Typing. . .";
                  } else {
                    string = "Prison Guard";
                  }
                  return string;
                },
                GameState.gptThinking));

    computer.setOnMouseClicked(
        (EventHandler<MouseEvent>)
            event -> {
              if (GameState.togglingComputer) {
                return;
              } else {
                toggleComputer();
              }
            });

    computerCloseButton.setOnAction(
        (EventHandler<ActionEvent>)
            event -> {
              if (GameState.togglingComputer) {
                return;
              } else {
                toggleComputer();
              }
            });

    // On send message
    sendButton.setOnAction(
        (EventHandler<ActionEvent>)
            new EventHandler<ActionEvent>() {
              @Override
              public void handle(ActionEvent event) {
                System.out.println("Send Button clicked");
                String message = messagesTextField.getText();
                if (!message.isEmpty() && !GameState.gptThinking.getValue()) {
                  HBox horiBox = new HBox();
                  horiBox.setAlignment(Pos.CENTER_RIGHT);
                  horiBox.setPadding(new Insets(5, 5, 5, 10));
                  Text text = new Text(message);
                  TextFlow textFlow = new TextFlow(text);
                  textFlow.setStyle(
                      "-fx-color: rgb(239,242,255); "
                          + "-fx-background-color: rgb(15,125,242); "
                          + "-fx-background-radius: 10px; ");
                  textFlow.setPadding(new Insets(5, 10, 5, 10));
                  text.setFill(Color.color(0.934, 0.945, 0.996));
                  horiBox.getChildren().add(textFlow);
                  messagesVertBox.getChildren().add(horiBox);
                  messagesTextField.clear();
                  try {
                    runGpt(new ChatMessage("user", message));
                  } catch (ApiProxyException e) {
                    e.printStackTrace();
                  }
                }
              }
            });

    torchButton.setOnMouseClicked(
        event -> {
          GameState.torchIsOn.setValue(!GameState.torchIsOn.getValue());
        });
    uvLightText.visibleProperty().bind(GameState.torchIsOn);

    uvLightEffect.visibleProperty().bind(GameState.torchIsOn);

    // Configure settings for the riddle's chat completion request.
    for (GameState.State state : GameState.State.values()) {
      chatCompletionRequests.put(
          state,
          new ChatCompletionRequest().setN(1).setTemperature(0.3).setTopP(0.5).setMaxTokens(100));
    }

    // Run a GPT-based instruction for the introduction.
    runGpt(new ChatMessage("user", GptPromptEngineering.getIntroInstruction()));
  }

  private void typeWrite(TextArea sceneTextArea, String message, int interval) {
    int i = 0;
    while (i < message.length()) {
      sceneTextArea.setText(sceneTextArea.getText() + message.charAt(i));
      sceneTextArea.appendText("");
      sceneTextArea.setScrollTop(Double.MAX_VALUE);
      i++;
      try {
        Thread.sleep(interval);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  // on recieve message, run in different thread
  private void addLabel(String messageFromGpt, VBox vbox) {
    System.out.println("GPT sent user a message");
    HBox horiBox = new HBox();
    horiBox.setAlignment(Pos.CENTER_LEFT);
    horiBox.setPadding(new Insets(5, 5, 5, 10));
    Text text = new Text(messageFromGpt);
    TextFlow textFlow = new TextFlow(text);
    textFlow.setStyle("-fx-background-color: rgb(233,233,235); " + "-fx-background-radius: 10px; ");
    textFlow.setPadding(new Insets(5, 10, 5, 10));
    textFlow.setMaxWidth(450);
    horiBox.getChildren().add(textFlow);
    Platform.runLater(
        new Runnable() {
          @Override
          public void run() {
            vbox.getChildren().add(horiBox);
          }
        });
  }

  // Navigation
  private void togglePhone() {
    System.out.println("toggling phone");
    GameState.togglingPhone = true;
    wait(
        500,
        () -> {
          GameState.togglingPhone = false;
        });
    final TranslateTransition phoneSwitch = new TranslateTransition();
    phoneSwitch.setNode(chatGroup);
    phoneSwitch.setDuration(Duration.millis(500));
    if (GameState.phoneIsOpen) {
      phoneSwitch.setByY(-550);
      GameState.phoneIsOpen = false;
      dimScreen.setDisable(true);
      dimScreen.setVisible(false);
    } else {
      phoneSwitch.setByY(550);
      GameState.phoneIsOpen = true;
      GameState.torchIsOn.setValue(false);
      dimScreen.setDisable(false);
      dimScreen.setVisible(true);
      notifCircle.setVisible(false);
    }
    phoneSwitch.play();
  }

  private void toggleComputer() {
    System.out.println("toggling computer");
    GameState.togglingComputer = true;
    wait(
        500,
        () -> {
          GameState.togglingComputer = false;
        });
    final TranslateTransition computerSwitch = new TranslateTransition();
    computerSwitch.setNode(computerGroup);
    computerSwitch.setDuration(Duration.millis(500));
    if (GameState.computerIsOpen) {
      computerSwitch.setByY(-650);
      GameState.computerIsOpen = false;
      computerDimScreen.setDisable(true);
      computerDimScreen.setVisible(false);
    } else {
      computerSwitch.setByY(650);
      GameState.computerIsOpen = true;
      computerPasswordField.requestFocus();
      GameState.torchIsOn.setValue(false);
      computerDimScreen.setDisable(false);
      computerDimScreen.setVisible(true);
    }
    computerSwitch.play();
  }

  @FXML
  public void openPhone(MouseEvent event) {
    System.out.println("Phone clicked");
    if (GameState.togglingPhone) {
      return;
    } else {
      togglePhone();
    }
  }

  private void switchRoom(int nextRoom) {
    GameState.switchingRoom = true;
    // use a new method to switch between rooms to prevent spamming and causing visual glitches
    wait(
        700,
        () -> {
          GameState.switchingRoom = false;
        });
    final TranslateTransition roomSwitch = new TranslateTransition();
    roomSwitch.setNode(roomCollectionPane);
    roomSwitch.setDuration(Duration.millis(500));
    if (nextRoom > GameState.currentRoom) {
      roomSwitch.setByX(-1022);
    } else {
      roomSwitch.setByX(1022);
    }
    roomSwitch.play();
    GameState.currentRoom = nextRoom;

    if (GameState.currentRoom == 0) {
      if (GameState.state == GameState.State.INTRO) {
        GameState.state = GameState.State.RIDDLE;
        try {
          runGpt(
              new ChatMessage(
                  "user",
                  GptPromptEngineering.getRiddleWithGivenWord(
                      GameState.wordToGuess, GameState.difficulty)));
        } catch (ApiProxyException e) {
          e.printStackTrace();
        }
      }
      leftButton.setVisible(false);
    } else if (GameState.currentRoom == 2) {
      rightButton.setVisible(false);
    } else {
      leftButton.setVisible(true);
      rightButton.setVisible(true);
    }
  }

  // plays the animation for moving left
  @FXML
  public void leftPane(MouseEvent event) {
    System.out.println("Left switch clicked");
    if (GameState.currentRoom == 0 || GameState.switchingRoom) {
      return;
    } else {
      switchRoom(GameState.currentRoom - 1);
    }
  }

  // plays the animation for moving right
  @FXML
  public void rightPane(MouseEvent event) {
    System.out.println("Right switch clicked");
    if (GameState.currentRoom == 2 || GameState.switchingRoom) {
      return;
    } else {
      switchRoom(GameState.currentRoom + 1);
    }
  }

  private void returnToWaitingLobby() throws IOException {
    timer.stop();
    SceneManager.delUi(SceneManager.AppUi.WAITING_LOBBY);
    SceneManager.addUi(SceneManager.AppUi.WAITING_LOBBY, App.loadFxml("waitinglobby"));
    App.setUi(AppUi.WAITING_LOBBY);
  }

  // Timer
  private void startTimer() {
    timer =
        new Timeline(
            new KeyFrame(
                Duration.seconds(1),
                event -> {
                  if (remainingSeconds > 0) {
                    remainingSeconds--;
                    updateTimerLabel();
                    if (remainingSeconds == 60) {
                      textToSpeech("You have 1 minute remaining");
                    }
                  } else {
                    timer.stop();
                    handleTimerExpired();
                  }
                }));
    timer.setCycleCount(Animation.INDEFINITE);
    timer.play();
  }

  private void updateTimerLabel() {
    int minutes = remainingSeconds / 60;
    int seconds = remainingSeconds % 60;
    String timeText = String.format("%02d:%02d", minutes, seconds);

    // Change timer color from black to red as time runs out
    double progress = 1.0 - (double) remainingSeconds / (GameState.time);
    Color textColor = Color.BLACK.interpolate(Color.RED, progress);

    timerLabel.setTextFill(textColor);
    timerLabel.setText(timeText);
  }

  private void handleTimerExpired() {
    Platform.runLater(
        () -> {
          textToSpeech("Time's up! You ran out of time!");
          try {
            returnToWaitingLobby();
          } catch (IOException e) {
            e.printStackTrace();
          }
        });
  }

  // Key Presses
  /**
   * Handles the key pressed event in the input text field. If the Enter key is pressed and the
   * input is not blank, triggers the onSendMessage function.
   *
   * @param event the key event
   * @throws ApiProxyException if there is an error communicating with the API proxy
   * @throws IOException if there is an I/O error
   */
  @FXML
  private void onKeyPressed(KeyEvent event) {
    if (event.getCode() == KeyCode.ENTER) {
      // Prevent the Enter key event from propagating further
      if (GameState.phoneIsOpen) {
        sendButton.fire();
      }
      if (GameState.computerIsOpen) {
        computerLoginButton.fire();
      }
    }
  }

  // TTS
  private void textToSpeech(String message) {

    Task<Void> speechTask =
        new Task<Void>() {
          @Override
          public Void call() throws Exception {
            TextToSpeech textToSpeech = new TextToSpeech();
            textToSpeech.speak(message);
            return null;
          }
        };

    new Thread(speechTask).start();
  }

  ////////////////////////
  // Objects Interaction
  ///////////////////////

  @FXML
  private void showObjectName(MouseEvent event) {
    Node source = (Node) event.getSource();
    source.getScene().setCursor(Cursor.HAND);
    if (source instanceof Rectangle) {
      String rectangleName = ((Rectangle) source).getId();
      Tooltip tooltip = new Tooltip(rectangleName);

      // Set a shorter tooltip delay (in milliseconds)
      tooltip.setShowDelay(Duration.millis(100));

      Tooltip.install(source, tooltip);

      // Add mouse move listener to update tooltip position
      source.setOnMouseMoved(
          mouseEvent -> {
            double horiOffset = 10; // X-offset from the cursor
            double vertOffset = 10; // Y-offset from the cursor
            tooltip.show(
                source, mouseEvent.getScreenX() + horiOffset, mouseEvent.getScreenY() + vertOffset);
          });

      // Set the current tooltip
      currentTooltip = tooltip;
    }
  }

  @FXML
  private void changeCursorToHand(MouseEvent event) {
    Node source = (Node) event.getSource();
    if (!source.isDisabled()) {
      source.getScene().setCursor(Cursor.HAND);
    }
  }

  @FXML
  private void resetCursor(MouseEvent event) {
    Node source = (Node) event.getSource();
    if (currentTooltip != null) {
      currentTooltip.hide(); // Hide the current tooltip
      currentTooltip = null; // Remove the reference
    }
    if (!source.isDisabled()) {
      source.getScene().setCursor(null);
    }
  }

  @FXML
  private void clickObject(MouseEvent event) {
    // Find which object was clicked
    Rectangle clickedRectangle = (Rectangle) event.getSource();
    String rectangleId = clickedRectangle.getId();
    System.out.println("Object clicked: " + rectangleId);
    System.out.println("Riddle solved: " + GameState.riddleSolved);

    if (GameState.riddleSolved
        && rectangleId.equals(GameState.wordToGuess)
        && !GameState.torchFound) {
      GameState.torchFound = true;
      torchButton.setVisible(true);
      // insert torch retrieved animation

      Thread animationThread =
          new Thread(
              () -> {
                TranslateTransition torchGet = new TranslateTransition();
                torchGet.setNode(torchGetGroup);
                torchGet.setDuration(javafx.util.Duration.millis(500));
                torchGet.setByY(600);
                FadeTransition torchFade = new FadeTransition();
                torchFade.setNode(torchGetGroup);
                torchFade.setDuration(javafx.util.Duration.millis(1000));
                torchFade.setFromValue(0);
                torchFade.setToValue(1);
                torchFade.play();
                torchGet.play();
              });

      Thread disappearThread =
          new Thread(
              () -> {
                try {
                  Thread.sleep(5000);
                } catch (InterruptedException e) {
                  e.printStackTrace();
                }
                TranslateTransition torchGet = new TranslateTransition();
                torchGet.setNode(torchGetGroup);
                torchGet.setDuration(javafx.util.Duration.millis(500));
                torchGet.setByY(600);
                FadeTransition torchFade = new FadeTransition();
                torchFade.setNode(torchGetGroup);
                torchFade.setDuration(javafx.util.Duration.millis(500));
                torchFade.setFromValue(1);
                torchFade.setToValue(0);
                torchFade.play();
                torchGet.play();
              });
      animationThread.setDaemon(true);
      disappearThread.setDaemon(true);
      animationThread.start();
      disappearThread.start();
    }
  }

  ///////////////
  // Guard's Room
  ///////////////
  @FXML
  private void openCircuit(MouseEvent event) {
    System.out.println("Circuit clicked");
    circuitGroup.setDisable(false);
    circuitGroup.setVisible(true);
    startMemoryRecallGame();
  }

  @FXML
  private void closeCircuit(MouseEvent event) {
    System.out.println("Circuit clicked");
    circuitGroup.setDisable(true);
    circuitGroup.setVisible(false);
  }

  @FXML
  private void clickSwitch(MouseEvent event) {
    // Find which object was clicked
    Rectangle clickedRectangle = (Rectangle) event.getSource();
    String rectangleId = clickedRectangle.getId();
    System.out.println("Object clicked: " + rectangleId);

    // Toggle the switch
    toggleSwitch(rectangleId);
  }

  private void initialiseMemoryGame() {
    System.out.println("Initialising memory game");
    // Clear the switchesToRecall array
    switchesToRecall.clear();
    playerChoices.clear();

    // Set all switches to red
    for (String fxid : allSwitches) {
      setSwitchToRed(fxid);
    }

    // Create a copy of allSwitches to avoid modifying the original list
    List<String> availableSwitches = new ArrayList<>(allSwitches);

    // Initialize a random number generator
    Random random = new Random();

    // Choose and toggle 7 random switches
    for (int i = 0; i < 7 && !availableSwitches.isEmpty(); i++) {
      // Generate a random index within the availableSwitches list
      int randomIndex = random.nextInt(availableSwitches.size());

      // Get the fx:id at the random index
      String randomSwitch = availableSwitches.get(randomIndex);

      // Call setSwitchToGreen on the selected switch
      setSwitchToGreen(randomSwitch);

      // Add it to the switchesToRecall list
      switchesToRecall.add(randomSwitch);

      // Remove the selected switch from the availableSwitches list
      availableSwitches.remove(randomIndex);
    }
  }

  private void toggleSwitch(String fxid) {
    Node node = circuitGroup.lookup("#" + fxid);
    if (node instanceof Rectangle) {
      Rectangle switchRect = (Rectangle) node;

      // Check the current fill color and toggle it
      if (switchRect.getFill().equals(Color.rgb(255, 0, 0))) {
        // Changing from red to green
        switchRect.setFill(Color.rgb(0, 255, 0));

        // Add the id to playerChoices if it doesn't exist
        if (!playerChoices.contains(fxid)) {
          playerChoices.add(fxid);
        }
      } else {
        // Changing from green to red
        switchRect.setFill(Color.rgb(255, 0, 0));

        // Remove the id from playerChoices if it exists
        playerChoices.remove(fxid);
      }
    }
  }

  private void setSwitchToGreen(String fxid) {
    Node node = circuitGroup.lookup("#" + fxid);
    if (node instanceof Rectangle) {
      Rectangle switchRect = (Rectangle) node;
      switchRect.setFill(Color.rgb(0, 255, 0));
    }
  }

  private void setSwitchToRed(String fxid) {
    Node node = circuitGroup.lookup("#" + fxid);
    if (node instanceof Rectangle) {
      Rectangle switchRect = (Rectangle) node;
      switchRect.setFill(Color.rgb(255, 0, 0));
    }
  }

  private void disableAllSwitches(boolean disable) {
    for (String fxid : allSwitches) {
      Node node = circuitGroup.lookup("#" + fxid);
      if (node instanceof Rectangle) {
        Rectangle switchRect = (Rectangle) node;
        switchRect.setDisable(disable);
      }
    }
  }

  @FXML
  private void checkIfUserInputCorrect() {
    // Convert both lists to HashSet for comparison
    HashSet<String> switchesToRecallSet = new HashSet<>(switchesToRecall);
    HashSet<String> playerChoicesSet = new HashSet<>(playerChoices);

    // Check if the sets are equal (order doesn't matter)
    boolean areEqual = switchesToRecallSet.equals(playerChoicesSet);

    // Now, 'areEqual' will be true if both sets have the same elements, regardless of order.
    System.out.println("Are equal: " + areEqual);

    if (areEqual) {
      closeCircuit(null);
      circuit.setDisable(true);
      guardRoomDarkness.setVisible(false);
    } else {
      initialiseMemoryGame();
      startMemoryRecallGame();
    }
  }

  private void startMemoryRecallGame() {
    goBackMemory.setVisible(false);
    checkGuessMemory.setVisible(false);
    disableAllSwitches(true);
    memoryCountdownLabel.setVisible(true);
    // Countdown Label
    int countdownSeconds = 5;

    new Thread(
            () -> {
              for (int i = countdownSeconds; i >= 0; i--) {
                final int remainingTime = i;
                Platform.runLater(
                    () -> {
                      if (remainingTime != 1) {
                        memoryCountdownLabel.setText(
                            "You have " + remainingTime + " seconds to remember");
                      } else {
                        memoryCountdownLabel.setText(
                            "You have " + remainingTime + " second to remember");
                      }
                    });

                try {
                  Thread.sleep(1000);
                } catch (InterruptedException e) {
                  e.printStackTrace();
                }
              }
            })
        .start();

    // Execute code after countdown
    CompletableFuture.delayedExecutor(countdownSeconds, TimeUnit.SECONDS)
        .execute(
            () -> {
              Platform.runLater(
                  () -> {
                    memoryCountdownLabel.setVisible(false);
                    for (String fxid : allSwitches) {
                      setSwitchToRed(fxid);
                    }
                    goBackMemory.setVisible(true);
                    checkGuessMemory.setVisible(true);
                    disableAllSwitches(false);
                  });
            });
  }

  ///////////////
  // Chat
  ///////////////

  /**
   * Runs a GPT-based riddle using the provided chat message.
   *
   * @param msg The input chat message for the riddle.
   * @return The response chat message from GPT.
   * @throws ApiProxyException If there's an issue with the API proxy.
   */
  private ChatMessage runGpt(ChatMessage msg) throws ApiProxyException {
    // Add the input message to the chat completion request.
    ChatCompletionRequest chatCompletionRequest =
        chatCompletionRequests.get(GameState.state).addMessage(msg);
    GameState.gptThinking.setValue(true);

    // Create a task for GPT processing.
    Task<ChatMessage> gptTask =
        new Task<ChatMessage>() {
          @Override
          protected ChatMessage call() throws Exception {
            try {
              // Execute the chat completion request.
              ChatCompletionResult chatCompletionResult = chatCompletionRequest.execute();
              // Get the first choice from the result.
              Choice result = chatCompletionResult.getChoices().iterator().next();
              // Add the result message to the chat completion request.
              chatCompletionRequest.addMessage(result.getChatMessage());
              // Return the GPT-generated response message.
              return result.getChatMessage();
            } catch (ApiProxyException e) {
              e.printStackTrace();
              return null;
            }
          }
        };

    // Define actions to be performed when the task succeeds.
    gptTask.setOnSucceeded(
        e -> {
          ChatMessage resultMessage = gptTask.getValue();
          if (resultMessage != null) {
            // Append the GPT response message to the chat.
            addLabel(resultMessage.getContent(), messagesVertBox);
            // Check if the response indicates a correct riddle answer.
            if (!GameState.phoneIsOpen) {
              notifCircle.setVisible(true);
            }
            if (resultMessage.getRole().equals("assistant")
                && resultMessage.getContent().startsWith("Correct")) {
              GameState.riddleSolved = true;
            }
            if (resultMessage.getContent().contains("hint")) {
              try {
                int hint = Integer.parseInt(hintLabel.getText());
                hint++;
                hintLabel.setText("" + hint);
              } catch (NumberFormatException ex) {
                hintLabel.setText("Error");
              }
            }
            GameState.gptThinking.setValue(false);
          } else {
            // When an error occurs, print a message suggesting fixes to the user.
            String apology =
                "Sorry, it seems like you cannot receive messages at this time. Maybe try to check"
                    + " your internet connection or your apiproxy.config file in order to see what"
                    + " is causing this problem, and then restart the application when you are"
                    + " ready to retry. You cannot escape from this facility without assistance.";
            wait(
                3500,
                () -> {
                  addLabel(apology, messagesVertBox);
                  if (!GameState.phoneIsOpen) {
                    notifCircle.setVisible(true);
                  }
                  phoneNameLabel.textProperty().unbind();
                  phoneNameLabel.setText("Prison Guard");
                });
          }
        });

    // Create a new thread for running the GPT task.
    Thread gptThread = new Thread(gptTask, "Gpt Thread");
    gptThread.start();

    // Return null for now (the actual return value is not used).
    return null;
  }

  ///////////////
  // Helper
  ///////////////

  /**
   * Waits for the specified amount of time before executing a task.
   *
   * @param time The amount of time in milliseconds to wait for.
   * @param process The process to be completed afterwards.
   */
  private void wait(int time, Runnable process) {
    Thread waitThread =
        new Thread(
            () -> {
              try {
                Thread.sleep(time);
              } catch (InterruptedException e) {
                e.printStackTrace();
              }
              Platform.runLater(process);
            });
    waitThread.setDaemon(true);
    waitThread.start();
  }
}
