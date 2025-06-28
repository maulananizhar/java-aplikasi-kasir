package com.kasir;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.kasir.model.Kasir;
import com.kasir.model.Products;
import com.kasir.services.DBConnection;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class ProductController {

  private Kasir kasir;
  @FXML
  private Label namaKasirLabel;
  @FXML
  private Label jabatanKasirLabel;

  @FXML
  private TableView<Products> tableView;
  @FXML
  private TableColumn<Products, String> idColumn;
  @FXML
  private TableColumn<Products, String> namaColumn;
  @FXML
  private TableColumn<Products, String> kategoriColumn;
  @FXML
  private TableColumn<Products, Float> hargaColumn;
  @FXML
  private TableColumn<Products, Integer> stokColumn;

  @FXML
  private TextField namaField;
  @FXML
  private TextField kategoriField;
  @FXML
  private TextField hargaField;
  @FXML
  private TextField stokField;

  @FXML
  private Button tambahProdukButton;
  @FXML
  private Button editProdukButton;
  @FXML
  private Button hapusProdukButton;
  @FXML
  private Button tambahStokButton;
  @FXML
  private Button kurangiStokButton;

  @FXML
  private void switchToCashierScene() throws IOException {
    FXMLLoader loader = new FXMLLoader(App.class.getResource("cashier.fxml"));
    Parent root = loader.load();
    CashierController controller = loader.getController();
    controller.setKasir(kasir);
    tableView.getScene().getWindow().setWidth(1280);
    tableView.getScene().getWindow().setHeight(800);
    tableView.getScene().setRoot(root);
  }

  @FXML
  private void switchToHistoryScene() throws IOException {
    FXMLLoader loader = new FXMLLoader(App.class.getResource("history.fxml"));
    Parent root = loader.load();
    HistoryController controller = loader.getController();
    controller.setKasir(kasir);
    tableView.getScene().getWindow().setWidth(1280);
    tableView.getScene().getWindow().setHeight(800);
    tableView.getScene().setRoot(root);
  }

  public void setKasir(Kasir kasir) {
    this.kasir = kasir;
    if (kasir != null) {
      namaKasirLabel.setText(kasir.getNama());
      jabatanKasirLabel.setText(kasir.getJabatan());
    } else {
      namaKasirLabel.setText("Tidak ada kasir");
      jabatanKasirLabel.setText("");
    }
  }

  @FXML
  public void initialize() {
    idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
    namaColumn.setCellValueFactory(new PropertyValueFactory<>("nama"));
    kategoriColumn.setCellValueFactory(new PropertyValueFactory<>("kategori"));
    hargaColumn.setCellValueFactory(new PropertyValueFactory<>("harga"));
    stokColumn.setCellValueFactory(new PropertyValueFactory<>("stok"));

    loadDataFromDatabase();
  }

  private void loadDataFromDatabase() {
    ObservableList<Products> data = FXCollections.observableArrayList();

    try (Connection conn = DBConnection.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM produk")) {

      while (rs.next()) {
        data.add(new Products(
            rs.getString("uuid"),
            rs.getString("nama"),
            rs.getString("kategori"),
            rs.getFloat("harga"),
            rs.getInt("stok")));
      }

    } catch (SQLException e) {
      e.printStackTrace();
    }

    tableView.setItems(data);
  }

  // Method to clear input fields
  private void clearFields() {
    namaField.clear();
    kategoriField.clear();
    hargaField.clear();
  }

  // Method to handle adding a new product
  @FXML
  private void tambahProduk() {
    String nama = namaField.getText();
    String kategori = kategoriField.getText();
    float harga = Float.parseFloat(hargaField.getText());

    if (nama.isEmpty() || kategori.isEmpty() || harga <= 0) {
      // Handle validation error (e.g., show an alert)
      System.out.println("Please fill all fields correctly.");
      clearFields(); // Clear input fields
      return;
    }

    if (isDuplicateProduct(nama)) {
      // Handle duplicate product error (e.g., show an alert)
      System.out.println("Product with this name already exists.");
      clearFields(); // Clear input fields
      return;
    }

    try (Connection conn = DBConnection.getConnection();
        Statement stmt = conn.createStatement()) {

      String sql = String.format(
          "INSERT INTO produk (uuid, nama, kategori, harga, stok) VALUES (UUID(), '%s', '%s', %.2f, 0)",
          nama, kategori, harga);
      stmt.executeUpdate(sql);

      loadDataFromDatabase(); // Refresh the table view
      clearFields(); // Clear input fields

    } catch (SQLException e) {
      System.out.println("Error adding product: " + e.getMessage());
      e.printStackTrace();
    }
  }

  // Method to handle editing an existing product
  @FXML
  private void editProduk() {
    Products selectedProduct = tableView.getSelectionModel().getSelectedItem();
    if (selectedProduct == null) {
      return; // No product selected
    }

    if (namaField.getText().isEmpty() || kategoriField.getText().isEmpty() || hargaField.getText().isEmpty()) {
      // Handle validation error (e.g., show an alert)
      System.out.println("Please fill all fields correctly.");
      return;
    }

    if (isDuplicateProduct(namaField.getText()) && !namaField.getText().equals(selectedProduct.getNama())) {
      // Handle duplicate product error (e.g., show an alert)
      System.out.println("Product with this name already exists.");
      return;
    }

    String nama = namaField.getText();
    String kategori = kategoriField.getText();
    float harga = Float.parseFloat(hargaField.getText());

    try (Connection conn = DBConnection.getConnection();
        Statement stmt = conn.createStatement()) {

      String sql = String.format("UPDATE produk SET nama='%s', kategori='%s', harga=%.2f WHERE uuid='%s'",
          nama, kategori, harga, selectedProduct.getId());
      stmt.executeUpdate(sql);

      loadDataFromDatabase(); // Refresh the table view
      clearFields(); // Clear input fields

    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  // Method to handle deleting a product
  @FXML
  private void hapusProduk() {
    Products selectedProduct = tableView.getSelectionModel().getSelectedItem();
    if (selectedProduct == null) {
      return; // No product selected
    }

    try (Connection conn = DBConnection.getConnection();
        Statement stmt = conn.createStatement()) {

      String sql = String.format("DELETE FROM produk WHERE uuid='%s'", selectedProduct.getId());
      stmt.executeUpdate(sql);

      loadDataFromDatabase(); // Refresh the table view
      clearFields(); // Clear input fields
      System.out.println("Selected product: " + selectedProduct.getId() + " has been deleted.");

    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  @FXML
  private void tambahStok() {
    Products selectedProduct = tableView.getSelectionModel().getSelectedItem();
    if (selectedProduct == null) {
      return; // No product selected
    }

    int stok = Integer.parseInt(stokField.getText());

    try (Connection conn = DBConnection.getConnection();
        Statement stmt = conn.createStatement()) {

      String sql = String.format("UPDATE produk SET stok=stok + %d WHERE uuid='%s'", stok, selectedProduct.getId());
      stmt.executeUpdate(sql);

      loadDataFromDatabase(); // Refresh the table view
      clearFields(); // Clear input fields

    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  @FXML
  private void kurangiStok() {
    Products selectedProduct = tableView.getSelectionModel().getSelectedItem();
    if (selectedProduct == null) {
      return; // No product selected
    }

    int stok = Integer.parseInt(stokField.getText());

    try (Connection conn = DBConnection.getConnection();
        Statement stmt = conn.createStatement()) {

      String sql = String.format("UPDATE produk SET stok=stok - %d WHERE uuid='%s'", stok, selectedProduct.getId());
      stmt.executeUpdate(sql);

      loadDataFromDatabase(); // Refresh the table view
      clearFields(); // Clear input fields

    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  // Method to check duplicate product names
  private boolean isDuplicateProduct(String nama) {
    try (Connection conn = DBConnection.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM produk WHERE nama='" + nama + "'")) {

      if (rs.next()) {
        return rs.getInt(1) > 0; // Return true if count is greater than 0
      }

    } catch (SQLException e) {
      e.printStackTrace();
    }
    return false;
  }

  // Handle click events on the table view
  @FXML
  private void onTableViewClick() {
    Products selectedProduct = tableView.getSelectionModel().getSelectedItem();
    if (selectedProduct != null) {
      namaField.setText(selectedProduct.getNama());
      kategoriField.setText(selectedProduct.getKategori());
      hargaField.setText(String.valueOf(selectedProduct.getHarga()));
      stokField.setText(String.valueOf(selectedProduct.getStok()));
    }
  }
}
