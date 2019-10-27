package info.part4;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Timer;

public class Main extends Application {
    private static final String iconImageLoc =
            "http://icons.iconarchive.com/icons/scafer31000/bubble-circle-3/16/GameCenter-icon.png";

    // application stage is stored so that it can be shown and hidden based on system tray icon operations.
    private Stage stage;

    // a timer allowing the tray icon to provide a periodic notification event.
    private Timer notificationTimer = new Timer();

    // format used to display the current time in a tray icon notification.
    private DateFormat timeFormat = SimpleDateFormat.getTimeInstance();

    // sets up the javafx application.
    // a tray icon is setup for the icon, but the main stage remains invisible until the user
    // interacts with the tray icon.
    @Override public void start(final Stage stage) throws IOException {
        this.stage = stage;

        String fxmlFile = "/fxml/main.fxml";
        FXMLLoader loader = new FXMLLoader();
        Parent root = loader.load(getClass().getResourceAsStream(fxmlFile));
        stage.setTitle("DeviceInfo");
//        root.setStyle("-fx-background-radius: 6;" +
//                "-fx-background-radius: 6;" +
//                "-fx-background-color: rgb(45, 45, 50), rgb(60, 60, 65);" +
//                "-fx-background-insets: 0, 0 1 1 0;");
//        stage.setScene(new Scene(root));
//        stage.show();
        // stores a reference to the stage.

//
//        // instructs the javafx system not to exit implicitly when the last application window is shut.
        Platform.setImplicitExit(false);
//
//        // sets up the tray icon (using awt code run on the swing thread).
        javax.swing.SwingUtilities.invokeLater(this::addAppToTray);
//
//        // out stage will be translucent, so give it a transparent style.
//        stage.initStyle(StageStyle.TRANSPARENT);

//
//        // create the layout for the javafx stage.
//        StackPane layout = new StackPane(createContent());
//        layout.setStyle(
//                "-fx-background-color: rgba(255, 255, 255, 0.5);"
//        );
//        layout.setPrefSize(300, 200);
//
//        // this dummy app just hides itself when the app screen is clicked.
//        // a real app might have some interactive UI and a separate icon which hides the app window.
//        layout.setOnMouseClicked(event -> stage.hide());
//
//        // a scene with a transparent fill is necessary to implement the translucent app window.
//        Scene scene = new Scene(layout);
//        scene.setFill(Color.TRANSPARENT);
//

        Scene scene = new Scene(root, Color.TRANSPARENT);
        stage.initStyle(StageStyle.UTILITY);
        stage.setScene(scene);
//        primaryStage.show();
//        stage.setScene(scene);
        stage.setOnCloseRequest(event -> stage.hide());
    }

    /**
     * For this dummy app, the (JavaFX scenegraph) content, just says "hello, world".
     * A real app, might load an FXML or something like that.
     *
     * @return the main window application content.
     */
    private Node createContent() {
        Label hello = new Label("hello, world");
        hello.setStyle("-fx-font-size: 40px; -fx-text-fill: forestgreen;");
        Label instructions = new Label("(click to hide)");
        instructions.setStyle("-fx-font-size: 12px; -fx-text-fill: orange;");

        VBox content = new VBox(10, hello, instructions);
        content.setAlignment(Pos.CENTER);

        return content;
    }

    /**
     * Sets up a system tray icon for the application.
     */
    private void addAppToTray() {
        try {
            // ensure awt toolkit is initialized.
            java.awt.Toolkit.getDefaultToolkit();

            // app requires system tray support, just exit if there is no support.
            if (!java.awt.SystemTray.isSupported()) {
                System.out.println("No system tray support, application exiting.");
                Platform.exit();
            }

            // set up a system tray icon.
            java.awt.SystemTray tray = java.awt.SystemTray.getSystemTray();
            URL imageLoc = new URL(
                    iconImageLoc
            );
            java.awt.Image image = ImageIO.read(imageLoc);
            java.awt.TrayIcon trayIcon = new java.awt.TrayIcon(image);

            // if the user double-clicks on the tray icon, show the main app stage.
            trayIcon.addActionListener(event -> Platform.runLater(this::showStage));

            // if the user selects the default menu item (which includes the app name),
            // show the main app stage.
            java.awt.MenuItem openItem = new java.awt.MenuItem("Открыть");
            openItem.addActionListener(event -> Platform.runLater(this::showStage));

            // the convention for tray icons seems to be to set the default icon for opening
            // the application stage in a bold font.
            java.awt.Font defaultFont = java.awt.Font.decode(null);
            java.awt.Font boldFont = defaultFont.deriveFont(java.awt.Font.BOLD);
            openItem.setFont(boldFont);

            // to really exit the application, the user must go to the system tray icon
            // and select the exit option, this will shutdown JavaFX and remove the
            // tray icon (removing the tray icon will also shut down AWT).
            java.awt.MenuItem exitItem = new java.awt.MenuItem("Выход");
            exitItem.addActionListener(event -> {
                notificationTimer.cancel();
                Platform.exit();
                tray.remove(trayIcon);
            });

            // setup the popup menu for the application.
            final java.awt.PopupMenu popup = new java.awt.PopupMenu();
            popup.add(openItem);
            popup.addSeparator();
            popup.add(exitItem);
            trayIcon.setPopupMenu(popup);

            // create a timer which periodically displays a notification message.
//            notificationTimer.schedule(
//                    new TimerTask() {
//                        @Override
//                        public void run() {
//                            javax.swing.SwingUtilities.invokeLater(() ->
//                                    trayIcon.displayMessage(
//                                            "hello",
//                                            "The time is now " + timeFormat.format(new Date()),
//                                            java.awt.TrayIcon.MessageType.INFO
//                                    )
//                            );
//                        }
//                    },
//                    5_000,
//                    60_000
//            );

            // add the application tray icon to the system tray.
            tray.add(trayIcon);
        } catch (java.awt.AWTException | IOException e) {
            System.out.println("Unable to init system tray");
            e.printStackTrace();
        }
    }

    /**
     * Shows the application stage and ensures that it is brought ot the front of all stages.
     */
    private void showStage() {
        if (stage != null) {
            stage.show();
            stage.toFront();
        }
    }

//    @Override
//    public void start(Stage primaryStage) throws Exception {
//        String fxmlFile = "/fxml/main.fxml";
//        FXMLLoader loader = new FXMLLoader();
//        Parent root = loader.load(getClass().getResourceAsStream(fxmlFile));
//        primaryStage.setTitle("Device Info");
//        primaryStage.setScene(new Scene(root));
//        primaryStage.show();
//    }

    public static void main(String[] args) {
        launch(args);
    }
}
