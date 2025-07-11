package util;

import model.Book;
import model.Loan;
import model.User;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

public class DBUtil {
    private static final String DB_URL = "jdbc:sqlite:library.db";


    public static Connection connect() {
        try {
            Class.forName("org.sqlite.JDBC"); // Driver'ı yükle
            return DriverManager.getConnection(DB_URL);
        } catch (ClassNotFoundException e) {
            System.out.println("SQLite JDBC driver bulunamadı!");
            e.printStackTrace();
            return null;
        } catch (SQLException e) {
            System.out.println("Veritabanına bağlanılamadı.");
            e.printStackTrace();
            return null;
        }
    }


    public static void createTables() {
        String createBooksTable = "CREATE TABLE IF NOT EXISTS books (" +
                "id TEXT PRIMARY KEY," +
                "title TEXT NOT NULL," +
                "author TEXT NOT NULL," +
                "available INTEGER NOT NULL DEFAULT 1" +
                ");";
        String createUsersTable = "CREATE TABLE IF NOT EXISTS users (" +
                "id TEXT PRIMARY KEY," +
                "name TEXT NOT NULL," +
                "email TEXT NOT NULL UNIQUE," +
                "password TEXT NOT NULL," +
                "role TEXT NOT NULL" +
                ");";
        String createLoansTable = "CREATE TABLE IF NOT EXISTS loans (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "book_id TEXT," +
                "user_id TEXT," +
                "borrow_date TEXT," +
                "return_date TEXT," +
                "FOREIGN KEY(book_id) REFERENCES books(id)," +
                "FOREIGN KEY(user_id) REFERENCES users(id)" +
                ");";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createUsersTable);
            stmt.execute(createBooksTable);
            stmt.execute(createLoansTable);

            System.out.println("Tablolar başarıyla oluşturuldu.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void insertBook(Book book) {
        String sql = "INSERT INTO books(id, title, author) VALUES(?, ?, ?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, book.getIsbn());
            pstmt.setString(2, book.getTitle());
            pstmt.setString(3, book.getAuthor());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Kitap eklenemedi.");
            e.printStackTrace();
        }
    }


    public static List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Book book = new Book(rs.getString("id"), rs.getString("title"), rs.getString("author"));
                books.add(book);
            }
            System.out.println("Kitap sayısı: " + books.size());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return books;
    }
    public static void insertLoan(Loan loan) {
        String sql = "INSERT INTO loans(book_id, user_id, borrow_date, return_date) VALUES(?, ?, ?, ?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, loan.getBook().getIsbn());
            pstmt.setString(2, loan.getUser().getUserId());
            pstmt.setString(3, loan.getBorrowDate().toString());
            pstmt.setString(4, null);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void returnLoan(Loan loan) {
        String sql = "UPDATE loans SET return_date = ? WHERE book_id = ? AND user_id = ? AND return_date IS NULL";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, LocalDate.now().toString());
            pstmt.setString(2, loan.getBook().getIsbn());
            pstmt.setString(3, loan.getUser().getUserId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void insertUser(User user, String password) {
        String sql = "INSERT INTO users(id, name, email, password, role) VALUES(?, ?, ?, ?, ?)";


        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUserId());
            pstmt.setString(2, user.getName());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, password);
            pstmt.setString(5, user.getRole());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static User getUserByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("role")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static User validateUser(String email, String password) {
        String sql = "SELECT * FROM users WHERE email = ? AND password = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("role")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Loan> getLoansByUser(String userId) {
        List<Loan> loans = new ArrayList<>();
        String sql = "SELECT * FROM loans WHERE user_id = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Book book = getBookById(rs.getString("book_id")); // Böyle bir metot da oluşturmalısın
                User user = getUserById(rs.getString("user_id")); // Böyle bir metot da oluşturmalısın
                Loan loan = new Loan(book, user);
                loan.setBorrowDate(LocalDate.parse(rs.getString("borrow_date")));
                String returnDateStr = rs.getString("return_date");
                if (returnDateStr != null) {
                    loan.setReturnDate(LocalDate.parse(returnDateStr));
                }
                loans.add(loan);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return loans;
    }

    public static List<Loan> getUnreturnedLoans() {
        List<Loan> loans = new ArrayList<>();
        String sql = "SELECT * FROM loans WHERE return_date IS NULL";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Book book = getBookById(rs.getString("book_id"));
                User user = getUserById(rs.getString("user_id"));
                Loan loan = new Loan(book, user);
                loan.setBorrowDate(LocalDate.parse(rs.getString("borrow_date")));
                loans.add(loan);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return loans;
    }
    public static Book getBookById(String bookId) {
        String sql = "SELECT * FROM books WHERE id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, bookId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Book(rs.getString("id"), rs.getString("title"), rs.getString("author"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static User getUserById(String userId) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new User(rs.getString("id"), rs.getString("name"), rs.getString("email"), rs.getString("role"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static List<Loan> getAllLoans() {
        List<Loan> loans = new ArrayList<>();
        String sql = "SELECT l.book_id, l.user_id, l.borrow_date, l.return_date, " +
                "b.title, b.author, u.name, u.email, u.role " +
                "FROM loans l " +
                "JOIN books b ON l.book_id = b.id " +
                "JOIN users u ON l.user_id = u.id";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Book book = new Book(rs.getString("book_id"),
                        rs.getString("title"),
                        rs.getString("author"));

                User user = new User(rs.getString("user_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("role"));

                Loan loan = new Loan(book, user);
                loan.setBorrowDate(LocalDate.parse(rs.getString("borrow_date")));
                String returnDate = rs.getString("return_date");
                if (returnDate != null) {
                    loan.setReturnDate(LocalDate.parse(returnDate));
                }

                loans.add(loan);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return loans;
    }
    public static void updateBookAvailability(String bookId, boolean available) {
        String sql = "UPDATE books SET available = ? WHERE id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, available ? 1 : 0);
            pstmt.setString(2, bookId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
