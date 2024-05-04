//M06 Programming Assignment (2)
//Ivy Tech Community College
//SDEV 200 - Java
//Professor Bumgardner
//Nativida Muhammad
// 04 May 2024

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class Main extends Application {
    private DBConnectionPanel connectionPanel;
    private Connection connection;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Batch Update Demo");

        connectionPanel = new DBConnectionPanel();

        Button connectButton = new Button("Connect to Database");
        connectButton.setOnAction(e -> connectToDatabase());

        BorderPane layout = new BorderPane();
        layout.setCenter(connectionPanel);
        layout.setBottom(connectButton);
        BorderPane.setMargin(connectButton, new Insets(10, 0, 10, 0));

        Scene scene = new Scene(layout, 400, 200);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void connectToDatabase() {
        connection = connectionPanel.getConnection();
        if (connection != null) {
            performBatchUpdate();
        } else {
            showErrorAlert("Failed to connect to database.");
        }
    }

    private void performBatchUpdate() {
        try {
            Statement statement = null;
            PreparedStatement batchInsert = null;

            try {
                statement = connection.createStatement();

                // Create the table if not exists
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS Temp(num1 DOUBLE, num2 DOUBLE, num3 DOUBLE)");

                // Prepare batch insert statement
                batchInsert = connection.prepareStatement("INSERT INTO Temp(num1, num2, num3) VALUES (?, ?, ?)");

                // Perform batch inserts
                long startTime = System.currentTimeMillis();
                for (int i = 0; i < 1000; i++) {
                    batchInsert.setDouble(1, Math.random());
                    batchInsert.setDouble(2, Math.random());
                    batchInsert.setDouble(3, Math.random());
                    batchInsert.addBatch();
                }
                batchInsert.executeBatch();
                long endTime = System.currentTimeMillis();

                showAlert("Batch update completed in " + (endTime - startTime) + " milliseconds.");
            } finally {
                // Close resources in finally block to ensure they are always closed
                if (batchInsert != null) {
                    batchInsert.close();
                }
                if (statement != null) {
                    statement.close();
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            showErrorAlert("Error performing batch update.");
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

class DBConnectionPanel extends GridPane {
    private TextField urlField;
    private TextField userField;
    private PasswordField passwordField;

    public DBConnectionPanel() {
        setPadding(new Insets(10));
        setHgap(5);
        setVgap(5);

        add(new Label("Database URL:"), 0, 0);
        urlField = new TextField("jdbc:mysql://localhost:3306/mydatabase");
        add(urlField, 1, 0);

        add(new Label("Username:"), 0, 1);
        userField = new TextField("root");
        add(userField, 1, 1);

        add(new Label("Password:"), 0, 2);
        passwordField = new PasswordField();
        passwordField.setText("password");
        add(passwordField, 1, 2);
    }

    public Connection getConnection() {
        try {
            String url = urlField.getText();
            String user = userField.getText();
            String password = passwordField.getText();
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
