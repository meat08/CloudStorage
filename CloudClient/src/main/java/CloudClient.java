import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ru.cloudstorage.server.network.Network;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class CloudClient extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/main.fxml"));
        primaryStage.setTitle("Cloud storage");
        primaryStage.setScene(new Scene(root, 1280, 600));
        primaryStage.show();
    }

    public static void main(String[] args) throws Exception {
        CountDownLatch networkStarter = new CountDownLatch(1);
        new Thread(() -> {
            try {
                Network.getInstance().start(networkStarter);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        networkStarter.await();
        launch(args);
    }
}
