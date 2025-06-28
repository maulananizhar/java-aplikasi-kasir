package com.kasir;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import com.kasir.model.Kasir;
import com.kasir.model.Keranjang;
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
import javafx.stage.Stage;

public class CashierController {

  private Kasir kasir;
  @FXML
  private Label namaKasirLabel;
  @FXML
  private Label jabatanKasirLabel;

  private String selectedKeranjangId;
  @FXML
  private TableView<Keranjang> keranjangView;
  @FXML
  private TableColumn<Keranjang, String> idColumn;
  @FXML
  private TableColumn<Keranjang, String> namaColumn;
  @FXML
  private TableColumn<Keranjang, Float> hargaColumn;
  @FXML
  private TableColumn<Keranjang, Integer> kuantitasColumn;
  @FXML
  private TableColumn<Keranjang, Float> jumlahColumn;

  private String selectedProdukId;
  private String transaksiId; // Default transaksi ID
  @FXML
  private TableView<Products> produkView;
  @FXML
  private TableColumn<Products, String> namaProdukColumn;
  @FXML
  private TableColumn<Products, Integer> stokProdukColumn;
  @FXML
  private Label transaksiIdLabel;
  @FXML
  private TextField cariField;
  @FXML
  private Label totalText;

  @FXML
  private Button transaksiBaruButton;
  @FXML
  private Button cariButton;
  @FXML
  private Button tambahKeranjangButton;
  @FXML
  private Button hapusProdukButton;
  @FXML
  private Button tambahKuantitasButton;
  @FXML
  private Button kurangKuantitasButton;
  @FXML
  private Button cetakStrukButton;

  @FXML
  private void switchToProductScene() throws IOException {
    FXMLLoader loader = new FXMLLoader(App.class.getResource("product.fxml"));
    Parent root = loader.load();
    ProductController controller = loader.getController();
    controller.setKasir(kasir);
    produkView.getScene().getWindow().setWidth(1280);
    produkView.getScene().getWindow().setHeight(800);
    produkView.getScene().setRoot(root);
  }

  @FXML
  private void switchToHistoryScene() throws IOException {
    FXMLLoader loader = new FXMLLoader(App.class.getResource("history.fxml"));
    Parent root = loader.load();
    HistoryController controller = loader.getController();
    controller.setKasir(kasir);
    produkView.getScene().getWindow().setWidth(1280);
    produkView.getScene().getWindow().setHeight(800);
    produkView.getScene().setRoot(root);
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

  public void setTransaksiId(String uuid) {
    this.transaksiId = uuid;
    transaksiIdLabel.setText(uuid);
  }

  @FXML
  public void initialize() {
    // Set default transaksi ID
    if (transaksiId == null || transaksiId.isEmpty()) {
      transaksiId = ""; // Example default ID
      transaksiIdLabel.setText(transaksiId);
    }
    // Set nama dan jabatan kasir
    if (kasir != null) {
      namaKasirLabel.setText(kasir.getNama());
      jabatanKasirLabel.setText(kasir.getJabatan());
    }

    // Initialize Keranjang TableView
    idColumn.setCellValueFactory(new PropertyValueFactory<>("uuid"));
    namaColumn.setCellValueFactory(new PropertyValueFactory<>("namaProduk"));
    hargaColumn.setCellValueFactory(new PropertyValueFactory<>("harga"));
    kuantitasColumn.setCellValueFactory(new PropertyValueFactory<>("kuantitas"));
    jumlahColumn.setCellValueFactory(new PropertyValueFactory<>("jumlah"));

    // Initialize Produk TableView
    produkView.setPlaceholder(new Label("Silakan cari produk..."));
    namaProdukColumn.setCellValueFactory(new PropertyValueFactory<>("nama"));
    stokProdukColumn.setCellValueFactory(new PropertyValueFactory<>("stok"));

    // Load keranjang data di thread terpisah agar tidak blocking UI dengan delay
    // setengah detik
    new Thread(() -> {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      javafx.application.Platform.runLater(() -> loadKeranjangData());
    }).start();
  }

  // Method ketika produk dipilih
  @FXML
  private void onProductSelected() {
    Products selectedProduct = produkView.getSelectionModel().getSelectedItem();
    if (selectedProduct != null) {
      selectedProdukId = selectedProduct.getId();
    }
  }

  // Merhod ketika keranjang dipilih
  @FXML
  private void onKeranjangSelected() {
    Keranjang selectedItem = keranjangView.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      selectedKeranjangId = selectedItem.getUuid();
      cariField.setText(selectedItem.getNamaProduk());
      updateTotalHarga();
      searchProducts();
    }
  }

  // Method untuk update total harga di label
  private void updateTotalHarga() {
    float total = 0;
    for (Keranjang item : keranjangView.getItems()) {
      total += item.getJumlah();
    }
    totalText.setText("Rp" + String.format("%,.0f", total).replace(',', '.'));

    // Update table transaksi di mysql
    try (Connection conn = DBConnection.getConnection();
        Statement stmt = conn.createStatement()) {
      String query = "UPDATE transaksi SET total_harga = " + total
          + " WHERE uuid = '" + transaksiId + "'";
      stmt.executeUpdate(query);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // Method untuk mencari produk
  @FXML
  private void searchProducts() {
    ObservableList<Products> data = FXCollections.observableArrayList();
    String keyword = cariField.getText();
    String query = "SELECT * FROM produk WHERE nama LIKE '%" + keyword + "%'";
    try (Connection conn = DBConnection.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query)) {
      while (rs.next()) {
        data.add(new Products(
            rs.getString("uuid"),
            rs.getString("nama"),
            rs.getString("kategori"),
            rs.getFloat("harga"),
            rs.getInt("stok")));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    produkView.setItems(data);
  }

  // load data keranjang dari database
  private void loadKeranjangData() {
    ObservableList<Keranjang> data = FXCollections.observableArrayList();
    String query = "SELECT * FROM keranjang WHERE transaksi_uuid = '" + transaksiId + "'";
    try (Connection conn = DBConnection.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query)) {
      while (rs.next()) {
        data.add(new Keranjang(
            rs.getString("uuid"),
            rs.getString("nama_produk"),
            rs.getFloat("harga"),
            rs.getInt("kuantitas"),
            rs.getFloat("jumlah"),
            rs.getString("transaksi_uuid"),
            rs.getString("produk_uuid")));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    keranjangView.setItems(data);
    updateTotalHarga();
  }

  // Method untuk menambahkan produk ke keranjang
  @FXML
  private void tambahKeranjang() {
    if (selectedProdukId == null)
      return;

    String namaProduk = "";
    float hargaProduk = 0;

    try (Connection conn = DBConnection.getConnection();
        Statement stmt = conn.createStatement()) {

      // Ambil nama dan harga produk
      try (ResultSet rs = stmt.executeQuery(
          "SELECT nama, harga, stok FROM produk WHERE uuid = '" + selectedProdukId + "'")) {
        if (rs.next()) {
          if (rs.getInt("stok") <= 0) {
            System.out.println("Stok produk habis, tidak bisa menambah ke keranjang.");
            return;
          }
          namaProduk = rs.getString("nama");
          hargaProduk = rs.getFloat("harga");
        }
      }

      // Cek apakah produk sudah ada di keranjang
      try (ResultSet rs = stmt.executeQuery(
          "SELECT uuid FROM keranjang WHERE transaksi_uuid = '" + transaksiId + "' AND produk_uuid = '"
              + selectedProdukId + "'")) {
        if (rs.next())
          return;
      }

      // Tambah ke keranjang
      stmt.executeUpdate(
          String.format(
              "INSERT INTO keranjang (uuid, transaksi_uuid, produk_uuid, nama_produk, harga, kuantitas) VALUES (UUID(), '%s', '%s', '%s', %.2f, 1)",
              transaksiId, selectedProdukId, namaProduk, hargaProduk));

      // Kurangi stok produk
      stmt.executeUpdate(
          "UPDATE produk SET stok = stok - 1 WHERE uuid = '" + selectedProdukId + "'");

      searchProducts();
      loadKeranjangData();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // Method untuk menghapus produk dari keranjang
  @FXML
  private void hapusProdukKeranjang() {
    if (selectedKeranjangId == null)
      return;

    try (Connection conn = DBConnection.getConnection();
        Statement stmt = conn.createStatement()) {

      int kuantitas = 0;
      String produkUuid = "";

      // Ambil kuantitas dan produk_uuid sekaligus
      String sql = String.format(
          "SELECT kuantitas, produk_uuid FROM keranjang WHERE uuid = '%s'", selectedKeranjangId);
      try (ResultSet rs = stmt.executeQuery(sql)) {
        if (rs.next()) {
          kuantitas = rs.getInt("kuantitas");
          produkUuid = rs.getString("produk_uuid");
        }
      }

      // Hapus dari keranjang
      stmt.executeUpdate(
          String.format("DELETE FROM keranjang WHERE uuid = '%s'", selectedKeranjangId));

      // Tambah stok produk
      stmt.executeUpdate(
          String.format("UPDATE produk SET stok = stok + %d WHERE uuid = '%s'", kuantitas, produkUuid));

      searchProducts();
      loadKeranjangData();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // Method untuk menambah kuantitas produk di keranjang
  @FXML
  private void tambahKuantitas() {
    if (selectedKeranjangId == null)
      return;
    try (Connection conn = DBConnection.getConnection();
        Statement stmt = conn.createStatement()) {

      // Ambil stok produk terkait
      String stokSql = "SELECT p.stok, k.produk_uuid FROM keranjang k JOIN produk p ON k.produk_uuid = p.uuid WHERE k.uuid = '"
          + selectedKeranjangId + "'";
      try (ResultSet rs = stmt.executeQuery(stokSql)) {
        if (rs.next()) {
          int stok = rs.getInt("stok");
          String produkUuid = rs.getString("produk_uuid");
          if (stok <= 0) {
            System.out.println("Stok produk habis, tidak bisa menambah kuantitas.");
            return;
          }
          // Tambah kuantitas di keranjang
          stmt.executeUpdate(
              "UPDATE keranjang SET kuantitas = kuantitas + 1 WHERE uuid = '" + selectedKeranjangId + "'");
          // Kurangi stok produk
          stmt.executeUpdate("UPDATE produk SET stok = stok - 1 WHERE uuid = '" + produkUuid + "'");
        }
      }

      searchProducts();
      loadKeranjangData();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // Method untuk mengurangi kuantitas produk di keranjang
  @FXML
  private void kurangKuantitas() {
    if (selectedKeranjangId == null)
      return;

    try (Connection conn = DBConnection.getConnection();
        Statement stmt = conn.createStatement()) {

      // Cek kuantitas produk di keranjang
      int kuantitas = 0;
      String produkUuid = "";
      try (ResultSet rs = stmt.executeQuery(
          "SELECT kuantitas, produk_uuid FROM keranjang WHERE uuid = '" + selectedKeranjangId + "'")) {
        if (rs.next()) {
          kuantitas = rs.getInt("kuantitas");
          produkUuid = rs.getString("produk_uuid");
        }
      }

      if (kuantitas <= 1) {
        hapusProdukKeranjang();
        return;
      }

      // Kurangi kuantitas di keranjang
      stmt.executeUpdate(
          "UPDATE keranjang SET kuantitas = kuantitas - 1 WHERE uuid = '" + selectedKeranjangId + "'");

      // Tambah stok produk
      stmt.executeUpdate(
          "UPDATE produk SET stok = stok + 1 WHERE uuid = '" + produkUuid + "'");

      searchProducts();
      loadKeranjangData();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // Method untuk membuat transaksi baru
  @FXML
  private void createTransaction() {
    try (Connection conn = DBConnection.getConnection();
        Statement stmt = conn.createStatement()) {
      // Buat transaksi baru dengan UUID
      String uuid = java.util.UUID.randomUUID().toString();
      String query = "INSERT INTO transaksi (uuid, tanggal, total_harga, kasir) VALUES ('"
          + uuid + "', NOW(), 0, '0438f356-4eb0-11f0-81b6-1a8a3d16facd')";
      stmt.executeUpdate(query);

      // Set transaksi ID baru
      setTransaksiId(uuid);
      loadKeranjangData();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // Method untuk mencetak struk transaksi
  @FXML
  private void konfirmasiPembayaran() {
    try {
      javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/kasir/konfirmasi.fxml"));
      javafx.scene.Parent root = loader.load();
      Stage dialog = new Stage();
      dialog.setTitle("Konfirmasi Pembayaran");
      dialog.setResizable(false);
      dialog.setScene(new javafx.scene.Scene(root));
      KonfirmasiController controller = loader.getController();
      controller.setTotalHarga(totalText.getText());
      controller.setUuid(transaksiId);
      dialog.show();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
