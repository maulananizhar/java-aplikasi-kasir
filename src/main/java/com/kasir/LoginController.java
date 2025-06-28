package com.kasir;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import com.kasir.model.Kasir;
import com.kasir.services.DBConnection;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class LoginController {

  @FXML
  private TextField usernameField;
  @FXML
  private TextField passwordField;
  @FXML
  private Button loginButton;

  @FXML
  private void loginHandler() {
    String username = usernameField.getText();
    String password = passwordField.getText();

    String query = "SELECT * FROM kasir WHERE username = ? AND password = ?";

    try (Connection conn = DBConnection.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(query)) {

      pstmt.setString(1, username);
      pstmt.setString(2, password);

      ResultSet rs = pstmt.executeQuery();

      if (rs.next()) {
        System.out.println("Login successful for user: " + username);
        Kasir kasir = new Kasir(
            rs.getString("uuid"),
            rs.getString("nama"),
            rs.getString("username"),
            rs.getString("password"),
            rs.getString("jabatan"));

        FXMLLoader loader = new FXMLLoader(App.class.getResource("cashier.fxml"));
        Parent root = loader.load();
        CashierController controller = loader.getController();
        controller.setKasir(kasir);
        loginButton.getScene().getWindow().setWidth(1280);
        loginButton.getScene().getWindow().setHeight(800);
        loginButton.getScene().setRoot(root);

      } else {
        System.out.println("Invalid username or password");
      }

    } catch (Exception e) {
      System.out.println("Error during login: " + e.getMessage());
      e.printStackTrace();
    } finally {
      System.out.println("Login attempt with username: " + username);
      System.out.println("Login attempt with password: [HIDDEN]");
    }
  }
}
