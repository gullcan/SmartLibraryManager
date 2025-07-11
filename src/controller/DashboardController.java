package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Label;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import model.Loan;
import model.User;
import util.DBUtil;
import model.Book;

public class DashboardController {

    private List<Loan> loans = new ArrayList<>();
    private User currentUser;

    @FXML
    private ListView<Book> bookListView;

    @FXML
    Button addBookButton;

    @FXML
    private Button showLoansButton;

    @FXML
    public void initialize() {
        List<Book> booksFromDB = DBUtil.getAllBooks();
        bookListView.getItems().addAll(booksFromDB);
    }

    @FXML
    private Button logoutButton;

    @FXML
    public void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
            Parent loginRoot = loader.load();

            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(new Scene(loginRoot));
            stage.setTitle("Giriş Yap");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    public void openAddBookWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/add_book.fxml"));
            Parent root = loader.load();

            AddBookController addBookController = loader.getController();
            addBookController.setDashboardController(this);

            Stage stage = new Stage();
            stage.setTitle("Yeni Kitap");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleBorrowBook() {
        Book selectedBook = bookListView.getSelectionModel().getSelectedItem();

        if (selectedBook == null) {
            showAlert("Uyarı", "Lütfen bir kitap seçin.", Alert.AlertType.WARNING);
            return;
        }

        if (!selectedBook.isAvailable()) {
            showAlert("Uyarı", "Seçili kitap zaten ödünç verilmiş.", Alert.AlertType.WARNING);
            return;
        }


        if (currentUser != null) {
            selectedBook.borrowBook();
            Loan loan = new Loan(selectedBook, currentUser);
            loans.add(loan);
            DBUtil.insertLoan(loan);
            DBUtil.updateBookAvailability(selectedBook.getIsbn(), false);
            loadLoans();
            showAlert("Başarılı", "Kitap ödünç alındı: " + selectedBook.getTitle(), Alert.AlertType.INFORMATION);
        }

    }

    @FXML
    public void openLoanList() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/loan_list.fxml"));
            Parent root = loader.load();

            LoanListController loanListController = loader.getController();

            // 🔐 Sadece admin tüm veriyi görsün, diğer kullanıcılar sadece kendi verisini
            if (currentUser != null && "admin".equals(currentUser.getRole())) {
                List<Loan> allLoans = DBUtil.getAllLoans();  // herkesin ödünç aldığı kitaplar
                loanListController.setLoans(allLoans);
            } else {
                List<Loan> userLoans = DBUtil.getLoansByUser(currentUser.getUserId());  // sadece bu kullanıcının
                loanListController.setLoans(userLoans);
            }

            Stage stage = new Stage();
            stage.setTitle("Ödünç Alınan Kitaplar");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void addBookToList(Book book) {
        bookListView.getItems().add(book);
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;

        if ("admin".equals(user.getRole())) {
            setupLoanTable();
            loadLoans();
            loanTable.setVisible(true);
            loanTableLabel.setVisible(true);
            addBookButton.setVisible(true);
            showLoansButton.setVisible(true);
            showUnreturnedButton.setVisible(true);
            loans = DBUtil.getAllLoans();
            loanTable.getItems().setAll(loans);
        } else {
            addBookButton.setVisible(true);
            showLoansButton.setVisible(true);
            showUnreturnedButton.setVisible(false);
            loanTable.setVisible(false);
            loanTableLabel.setVisible(false);
            loans = DBUtil.getUnreturnedLoans();
        }
    }


    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML private TableView<Loan> loanTable;
    @FXML private TableColumn<Loan, String> bookCol;
    @FXML private TableColumn<Loan, String> userCol;
    @FXML private TableColumn<Loan, String> emailCol;
    @FXML private TableColumn<Loan, String> borrowDateCol;
    @FXML private TableColumn<Loan, String> returnDateCol;
    @FXML private Label loanTableLabel;

    private void setupLoanTable() {
        bookCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBook().getTitle()));
        userCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUser().getName()));
        emailCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUser().getEmail()));
        borrowDateCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBorrowDate().toString()));
        returnDateCol.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getReturnDate() != null ? cellData.getValue().getReturnDate().toString() : "Henüz değil"));
    }

    private void loadLoans() {
        loanTable.getItems().setAll(DBUtil.getAllLoans());
    }

    @FXML private Button showUnreturnedButton;

    @FXML
    private void handleShowUnreturned() {
        if (currentUser != null && currentUser.getRole().equals("admin")) {
            List<Loan> allLoans = DBUtil.getAllLoans();
            List<Loan> unreturnedLoans = allLoans.stream()
                    .filter(loan -> loan.getReturnDate() == null)
                    .toList();

            loanTable.getItems().setAll(unreturnedLoans);
            loanTableLabel.setText("İade Edilmeyen Kitaplar");
        }
    }

    @FXML
    private void handleShowAllLoans() {
        loadLoans();
        loanTableLabel.setText("Tüm Ödünç Alınan Kitaplar");
    }
}


