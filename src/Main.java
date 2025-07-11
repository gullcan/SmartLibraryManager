import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.User;
import util.DBUtil;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        DBUtil.createTables();
        DBUtil.insertUser(new User("u001", "Gülcan", "admin@example.com", "admin"), "admin");

        Parent root = FXMLLoader.load(getClass().getResource("view/login.fxml"));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Smart Library Login");
        stage.show();
    }

    public static void main(String[] args) {launch();
    }
}
