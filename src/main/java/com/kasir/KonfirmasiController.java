package com.kasir;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Statement;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import com.kasir.model.Kasir;
import com.kasir.services.DBConnection;

public class KonfirmasiController {

  private Kasir kasir;

  @FXML
  private Label totalHargaLabel;
  @FXML
  private Label uuidLabel;
  @FXML
  private Button konfirmasiButton;
  @FXML
  private Button batalButton;

  // Method untuk merubah label total harga
  public void setTotalHarga(String totalHarga) {
    totalHargaLabel.setText(totalHarga);
  }

  public void setUuid(String uuid) {
    uuidLabel.setText(uuid);
  }

  public void setKasir(Kasir kasir) {
    this.kasir = kasir;
  }

  // Method untuk close dialog konfirmasi
  @FXML
  private void closeDialog() {
    // Close the dialog or window
    batalButton.getScene().getWindow().hide();
  }

  // Method untuk konfirmasi pembayaran
  @FXML
  private void konfirmasiPembayaran() throws IOException {
    // Logic untuk konfirmasi pembayaran

    // Ubah status transaksi menjadi "Selesai"
    try (Connection conn = DBConnection.getConnection();
        Statement stmt = conn.createStatement()) {

      // Ambil UUID dari label
      String uuid = uuidLabel.getText();
      // Update status transaksi menjadi "Selesai"
      String sql = "UPDATE transaksi SET status = 'Selesai' WHERE uuid = '" + uuid + "'";
      stmt.executeUpdate(sql);
    } catch (Exception e) {
      e.printStackTrace();
    }

    closeDialog();

    FXMLLoader loader = new FXMLLoader(App.class.getResource("history.fxml"));
    Parent root = loader.load();
    HistoryController controller = loader.getController();
    controller.setKasir(kasir);

    App.rootScene.setRoot(root);
  }
}
