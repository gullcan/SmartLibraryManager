package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import model.Loan;
import util.DBUtil;

import java.util.List;

public class LoanListController {


    @FXML
    private ListView<Loan> loanListView;

    private List<Loan> loans;

    public void setLoans(List<Loan> loans) {
        this.loans = loans;
        updateLoanList();
    }

    private void updateLoanList() {
        loanListView.getItems().clear();
        for (Loan loan : loans) {
            if (!loan.isReturned()) {
                loanListView.getItems().add(loan);
            }
        }
    }

    @FXML
    public void handleReturnBook() {
        Loan selectedLoan = loanListView.getSelectionModel().getSelectedItem();

        if (selectedLoan != null && !selectedLoan.isReturned()) {
            selectedLoan.returnBook();
            selectedLoan.getBook().returnBook();
            DBUtil.returnLoan(selectedLoan);
            updateLoanList();
            showAlert("Başarılı", "Kitap iade edildi: " + selectedLoan.getBook().getTitle(), Alert.AlertType.INFORMATION);
        } else {
            showAlert("Hata", "Lütfen iade edilecek kitabı seçin.", Alert.AlertType.ERROR);
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
