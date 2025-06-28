package com.kasir;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Statement;

import org.apache.pdfbox.pdmodel.PDDocument;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import com.kasir.services.DBConnection;

public class KonfirmasiController {
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

      // PDF
      PDDocument document = new PDDocument();
      document.save("D:\\" + uuid + ".pdf");
      document.close();

    } catch (Exception e) {
      e.printStackTrace();
    }

    closeDialog();
    App.setRoot("history");
  }
}
