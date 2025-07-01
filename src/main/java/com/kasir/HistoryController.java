package com.kasir;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Locale;

import com.kasir.libs.CetakLaporan;
import com.kasir.libs.CetakStruk;
import com.kasir.model.Kasir;
import com.kasir.model.Transaksi;
import com.kasir.services.DBConnection;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class HistoryController {

  private Kasir kasir;
  @FXML
  private Label namaKasirLabel;
  @FXML
  private Label jabatanKasirLabel;

  @FXML
  private TableView<Transaksi> historyTableView;
  @FXML
  private TableColumn<Transaksi, String> uuidColumn;
  @FXML
  private TableColumn<Transaksi, String> tanggalColumn;
  @FXML
  private TableColumn<Transaksi, Float> totalHargaColumn;
  @FXML
  private TableColumn<Transaksi, String> kasirColumn;
  @FXML
  private TableColumn<Transaksi, String> statusColumn;

  @FXML
  private DatePicker tanggalAwal;
  @FXML
  private DatePicker tanggalAkhir;

  @FXML
  private void switchToCashierScene() throws IOException {
    FXMLLoader loader = new FXMLLoader(App.class.getResource("cashier.fxml"));
    Parent root = loader.load();
    CashierController controller = loader.getController();
    controller.setKasir(kasir);

    Scene scene = historyTableView.getScene();
    scene.setRoot(root);

    Stage stage = (Stage) scene.getWindow();
    stage.sizeToScene();
  }

  @FXML
  private void switchToProductScene() throws IOException {
    FXMLLoader loader = new FXMLLoader(App.class.getResource("product.fxml"));
    Parent root = loader.load();
    ProductController controller = loader.getController();
    controller.setKasir(kasir);

    Scene scene = historyTableView.getScene();
    scene.setRoot(root);

    Stage stage = (Stage) scene.getWindow();
    stage.sizeToScene();
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
    // Initialize History TableView
    uuidColumn.setCellValueFactory(new PropertyValueFactory<>("uuid"));
    tanggalColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
        cellData.getValue().getTanggal() == null ? ""
            : java.time.format.DateTimeFormatter
                .ofPattern("EEEE, dd MMMM yyyy - HH:mm", java.util.Locale.forLanguageTag("id-ID"))
                .withZone(java.time.ZoneId.of("GMT+14"))
                .format(cellData.getValue().getTanggal().toInstant())));
    totalHargaColumn.setCellValueFactory(new PropertyValueFactory<>("totalHarga"));
    kasirColumn.setCellValueFactory(new PropertyValueFactory<>("namaKasir"));
    statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

    // Load history data
    new Thread(() -> {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      javafx.application.Platform.runLater(() -> loadHistoryData());
    }).start();

    historyTableView.getSortOrder().add(tanggalColumn);
    tanggalColumn.setSortType(TableColumn.SortType.DESCENDING);
  }

  // Method untuk select baris pada tabel
  @FXML
  private void onHistorySelected() {
    Transaksi selectedTransaksi = historyTableView.getSelectionModel().getSelectedItem();
    if (selectedTransaksi != null) {
    }
  }

  // Method to load history data from the database
  private void loadHistoryData() {
    ObservableList<Transaksi> historyData = FXCollections.observableArrayList();

    try (Connection conn = DBConnection.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(
            "SELECT " +
                "t.uuid AS uuid, " +
                "t.tanggal AS tanggal, " +
                "t.total_harga, " +
                "t.kasir, " +
                "k.nama AS nama_kasir, " +
                "t.status " +
                "FROM transaksi t " +
                "JOIN kasir k ON t.kasir = k.uuid " +
                "WHERE t.kasir = '" + kasir.getUuid() + "' ")) {

      while (rs.next()) {
        historyData.add(new Transaksi(
            rs.getString("uuid"),
            rs.getTimestamp("tanggal"),
            rs.getFloat("total_harga"),
            rs.getString("kasir"),
            rs.getString("nama_kasir"),
            rs.getString("status")));
      }

      historyTableView.setItems(historyData);
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  // Method untuk hapus transaksi
  @FXML
  private void hapusTransaksi() {
    Transaksi selectedTransaksi = historyTableView.getSelectionModel().getSelectedItem();
    if (selectedTransaksi != null) {
      try (Connection conn = DBConnection.getConnection();
          Statement stmt = conn.createStatement()) {

        // Jika transaksi sudah dibayar, tampilkan pesan error
        if (selectedTransaksi.getStatus().equals("Selesai")) {
          System.out.println("Transaksi sudah dibayar, tidak bisa dihapus.");
          return;
        }

        // Hapus transaksi dari database
        stmt.executeUpdate("DELETE FROM transaksi WHERE uuid = '" + selectedTransaksi.getUuid() + "'");

        // Refresh data
        loadHistoryData();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  // Detail Transaksi
  @FXML
  private void detailTransaksi() {
    Transaksi selectedTransaksi = historyTableView.getSelectionModel().getSelectedItem();
    try {

      // Jika transaksi sudah dibayar, tampilkan pesan error
      if (selectedTransaksi.getStatus().equals("Selesai")) {
        System.out.println("Transaksi sudah dibayar, tidak bisa diedit.");
        return;
      }

      FXMLLoader loader = new FXMLLoader(App.class.getResource("cashier.fxml"));
      Parent root = loader.load();
      CashierController controller = loader.getController();
      controller.setTransaksiId(selectedTransaksi.getUuid());
      controller.setKasir(kasir);
      historyTableView.getScene().setRoot(root);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @FXML
  private void printStruk() throws IOException {
    Transaksi selectedTransaksi = historyTableView.getSelectionModel().getSelectedItem();
    if (selectedTransaksi == null) {
      System.out.println("Tidak ada transaksi yang dipilih.");
      return;
    }
    CetakStruk.cetakStruk(selectedTransaksi, kasir);
  }

  @FXML
  private void cetakLaporan() throws IOException {
    if (tanggalAwal.getValue() == null || tanggalAkhir.getValue() == null) {
      System.out.println("Tanggal awal dan akhir harus diisi.");
      return;
    }
    CetakLaporan.cetakLaporan(tanggalAwal.getValue(), tanggalAkhir.getValue(), kasir);
    System.out.println(
        "Cetak laporan dari " + formatTanggalString(tanggalAwal.getValue()) + " sampai "
            + formatTanggalString(tanggalAkhir.getValue()));
  }

  @FXML
  private void logout() throws IOException {
    // Kembali ke halaman login
    FXMLLoader loader = new FXMLLoader(App.class.getResource("login.fxml"));
    Parent root = loader.load();

    Scene scene = historyTableView.getScene();
    scene.setRoot(root);

    Stage stage = (Stage) scene.getWindow();
    stage.sizeToScene();
  }

  private static String formatTanggalString(LocalDate tanggalStr) {
    try {
      SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
      SimpleDateFormat outputFormat = new SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.forLanguageTag("id-ID"));
      outputFormat.setTimeZone(java.util.TimeZone.getTimeZone("GMT+14"));
      java.util.Date date = inputFormat.parse(tanggalStr.toString());
      return outputFormat.format(date);
    } catch (Exception e) {
      return tanggalStr.toString();
    }
  }

}
