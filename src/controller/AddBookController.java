package controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import model.Book;
import util.DBUtil;

public class AddBookController {

    @FXML
    private TextField isbnField;
    @FXML
    private TextField titleField;
    @FXML
    private TextField authorField;

    private DashboardController dashboardController;

    public void setDashboardController(DashboardController controller) {
        this.dashboardController = controller;
    }

    @FXML
    public void handleAddBook() {
        String isbn = isbnField.getText().trim();
        String title = titleField.getText().trim();
        String author = authorField.getText().trim();

        if (isbn.isEmpty() || title.isEmpty() || author.isEmpty()) {
            showAlert("Hata","Lütfen tüm alanları doldurun!", Alert.AlertType.ERROR);
            return;
        }

        Book newBook = new Book(isbn, title, author);
        DBUtil.insertBook(newBook);
        dashboardController.addBookToList(newBook);

        showAlert("Başarışı", "Kitap başarıyla eklendi!", Alert.AlertType.INFORMATION);


        Stage stage = (Stage) isbnField.getScene().getWindow();
        stage.close();
    }
    private void showAlert(String title, String message, Alert.AlertType type){
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
