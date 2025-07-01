package com.kasir.libs;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.awt.Desktop;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import com.kasir.model.Kasir;
import com.kasir.model.Laporan;
import com.kasir.services.DBConnection;

import javafx.scene.paint.Color;

public class CetakLaporan {
  public static void cetakLaporan(LocalDate tanggalAwal, LocalDate tanggalAkhir, Kasir kasir) throws IOException {
    PDDocument document = new PDDocument();
    PDPage page = new PDPage(PDRectangle.A6);
    document.addPage(page);

    int pageWidth = (int) page.getTrimBox().getWidth();
    int pageHeight = (int) page.getTrimBox().getHeight();

    PDPageContentStream contentStream = new PDPageContentStream(document, page);
    ApacheText text = new ApacheText(document, contentStream);
    ApacheTable table = new ApacheTable(document, contentStream);

    PDFont font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

    text.addSingleLineText(page, "Laporan Penjualan", 10, pageHeight - 10, font, 10, Color.BLACK, "center");
    text.addSingleLineText(page, "Daifuku Tree Mochi", 10, pageHeight - 22, font, 10, Color.BLACK, "center");

    text.addSingleLineText(page, "Kasir: " + kasir.getNama(), 10, pageHeight - 52, font, 6, Color.BLACK, "left");
    text.addSingleLineText(page, "Tanggal Awal: " + formatTanggal(tanggalAwal), 10, pageHeight - 60, font, 6,
        Color.BLACK, "left");
    text.addSingleLineText(page, "Tanggal Akhir: " + formatTanggal(tanggalAkhir), 10, pageHeight - 68, font,
        6, Color.BLACK, "left");

    int[] columnWidths = { 150, 50, 75 };
    table.setTable(columnWidths, 12, 10, pageHeight - 100);
    table.setTableFont(font, 6, Color.BLACK);

    Color tableHeadColor = Color.LIGHTGRAY;
    Color tableRowColor = Color.WHITE;

    table.addRow("Nama", tableHeadColor);
    table.addRow("Kuantitas", tableHeadColor);
    table.addRow("Harga", tableHeadColor);

    List<Laporan> laporan = new java.util.ArrayList<>();
    String query = "SELECT k.nama_produk, "
        + "SUM(k.kuantitas) AS total_terjual, "
        + "SUM(k.kuantitas * k.harga) AS total_harga "
        + "FROM keranjang k "
        + "JOIN transaksi t ON k.transaksi_uuid = t.uuid "
        + "JOIN kasir kas ON t.kasir = kas.uuid "
        + "WHERE t.status = 'Selesai' "
        + "AND t.kasir = '" + kasir.getUuid() + "' "
        + "AND k.tanggal BETWEEN '" + tanggalAwal + " 00:00:00' AND '" + tanggalAkhir + " 23:59:59' "
        + "GROUP BY nama_produk";

    // SQL untuk mengambil data keranjang berdasarkan UUID transaksi
    try (Connection conn = DBConnection.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query)) {
      while (rs.next()) {
        laporan.add(new Laporan(
            rs.getString("nama_produk"),
            rs.getInt("total_terjual"),
            rs.getFloat("total_harga")));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    for (Laporan item : laporan) {
      table.addRow(item.getNama(), tableRowColor);
      table.addRow(String.valueOf(item.getTotalTerjual()), tableRowColor);
      table.addRow(String.format("Rp%s", String.format("%,.0f",
          item.getTotalHarga()).replace(',', '.')),
          tableRowColor);
    }

    float totalHarga = 0;
    for (Laporan item : laporan) {
      totalHarga += item.getTotalHarga();
    }

    table.addRow("", tableRowColor);
    table.addRow("Total", tableHeadColor);
    table.addRow(String.format("Rp%s", String.format("%,.0f",
        totalHarga).replace(',', '.')),
        tableHeadColor);

    contentStream.close();
    document.save(
        "C:\\Users\\Nizhar Maulana\\OneDrive - Universitas Negeri Jakarta (UNJ)\\College\\122 - Pemrograman Berorientasi Objek Lanjut\\kasir\\target\\pdf\\"
            + "Laporan transaksi dari kasir " + kasir.getNama() + " tanggal " + tanggalAwal + " s.d. " + tanggalAkhir
            + ".pdf");
    document.close();
    Desktop.getDesktop().browse(
        java.nio.file.Paths.get(
            "C:\\Users\\Nizhar Maulana\\OneDrive - Universitas Negeri Jakarta (UNJ)\\College\\122 - Pemrograman Berorientasi Objek Lanjut\\kasir\\target\\pdf\\"
                + "Laporan transaksi dari kasir " + kasir.getNama() + " tanggal " + tanggalAwal + " s.d. "
                + tanggalAkhir + ".pdf")
            .toUri());
  }

  private static String formatTanggal(LocalDate tanggalStr) {
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
