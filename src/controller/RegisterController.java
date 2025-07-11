package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import model.User;
import util.DBUtil;

import java.util.UUID;

public class RegisterController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    @FXML
    private void handleRegister() {
        String name = nameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Lütfen tüm alanları doldurun.");
            return;
        }
        User newUser = new User(UUID.randomUUID().toString(), name, email, "user");

        if (DBUtil.getUserByEmail(email) != null) {
            showAlert("Uyarı", "Bu e-posta zaten kayıtlı.", Alert.AlertType.WARNING);
            return;
        }

        DBUtil.insertUser(newUser, password);


        // Kayıt başarılı mesajı yerine, kullanıcıyı giriş sayfasına yönlendir
        showAlert("Başarılı", "Kayıt başarılı! Giriş yapabilirsiniz.", Alert.AlertType.INFORMATION);

        try {
            Parent loginRoot = FXMLLoader.load(getClass().getResource("/view/login.fxml"));  // login sayfası yolu
            Stage stage = (Stage) nameField.getScene().getWindow();
            stage.setScene(new Scene(loginRoot));
            stage.setTitle("Giriş Yap");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
