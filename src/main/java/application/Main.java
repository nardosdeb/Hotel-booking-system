package application;

import dao.BookingDAO;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import util.DBConnection; // Import DBConnection
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main extends Application {
    private ScheduledExecutorService scheduler;

    @Override
    public void start(Stage primaryStage) {
        try {
            // Start the background task to cancel expired bookings
            startBookingCleanupTask();

            // Load FXML file from the 'view' package
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root);
            // Link the stylesheet
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

            primaryStage.setTitle("Hilton Hotel - Login");
            primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            // Show error dialog
            System.err.println("Error loading FXML file: " + e.getMessage());
            System.err.println("Current directory: " + System.getProperty("user.dir"));
            System.err.println("Classpath: " + System.getProperty("java.class.path"));
        }
    }

    private void startBookingCleanupTask() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        BookingDAO bookingDAO = new BookingDAO();
        
        // Run every 1 minute to check for expired bookings more frequently
        scheduler.scheduleAtFixedRate(() -> {
            try {
                int cancelledCount = bookingDAO.cancelExpiredBookings();
                if (cancelledCount > 0) {
                    System.out.println("Cancelled " + cancelledCount + " expired pending bookings.");
                }
            } catch (Exception e) {
                System.err.println("Error in booking cleanup task: " + e.getMessage());
                e.printStackTrace();
            }
        }, 0, 1, TimeUnit.MINUTES);
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
            }
        }
        DBConnection.closeConnection(); // Close the database connection when the application stops
    }

    public static void main(String[] args) {
        launch(args);
    }
}