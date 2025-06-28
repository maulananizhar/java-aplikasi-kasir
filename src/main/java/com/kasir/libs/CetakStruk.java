package com.kasir.libs;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import com.kasir.model.Keranjang;
import com.kasir.model.Transaksi;
import com.kasir.services.DBConnection;

import javafx.scene.paint.Color;

public class CetakStruk {
  public static void cetakStruk(Transaksi selectedTransaksi) throws IOException {
    PDDocument document = new PDDocument();
    PDPage page = new PDPage(PDRectangle.A6);
    document.addPage(page);

    String namaKasir = "Codsworth";

    int pageWidth = (int) page.getTrimBox().getWidth();
    int pageHeight = (int) page.getTrimBox().getHeight();

    PDPageContentStream contentStream = new PDPageContentStream(document, page);
    ApacheText text = new ApacheText(document, contentStream);
    ApacheTable table = new ApacheTable(document, contentStream);

    PDFont font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    // PDFont italicFont = new
    // PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);

    text.addSingleLineText(page, "Struk Belanja", 10, pageHeight - 10, font, 10, Color.BLACK, "center");
    text.addSingleLineText(page, "Daifuku Satrio Mochi", 10, pageHeight - 22, font, 10, Color.BLACK, "center");

    text.addSingleLineText(page, "Nama Kasir: " + namaKasir, 10, pageHeight - 60, font, 6, Color.BLACK, "left");
    text.addSingleLineText(page, "UUID Transaksi: " + selectedTransaksi.getUuid(), 10, pageHeight - 68, font,
        6, Color.BLACK, "left");

    text.addSingleLineText(page, formatTanggal(selectedTransaksi.getTanggal()),
        pageWidth - font.getStringWidth(formatTanggal(selectedTransaksi.getTanggal())) / 1000 * 5 - 20,
        pageHeight - 60,
        font,
        6,
        Color.BLACK, "left");

    text.addSingleLineText(page, getWaktu(selectedTransaksi.getTanggal()),
        pageWidth - font.getStringWidth(getWaktu(selectedTransaksi.getTanggal())) / 1000 * 5 - 15,
        pageHeight - 68,
        font,
        6,
        Color.BLACK, "left");

    int[] columnWidths = { 100, 55, 50, 70 };
    table.setTable(columnWidths, 12, 10, pageHeight - 100);
    table.setTableFont(font, 6, Color.BLACK);

    Color tableHeadColor = Color.LIGHTGRAY;
    Color tableRowColor = Color.WHITE;

    table.addRow("Nama", tableHeadColor);
    table.addRow("Harga", tableHeadColor);
    table.addRow("Kuantiatas", tableHeadColor);
    table.addRow("Jumlah", tableHeadColor);

    List<Keranjang> keranjang = new java.util.ArrayList<>();
    String query = "SELECT * FROM keranjang WHERE transaksi_uuid = '" + selectedTransaksi.getUuid() + "'";
    // SQL untuk mengambil data keranjang berdasarkan UUID transaksi
    try (Connection conn = DBConnection.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query)) {
      while (rs.next()) {
        keranjang.add(new Keranjang(
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

    for (Keranjang item : keranjang) {
      table.addRow(item.getNamaProduk(), tableRowColor);
      table.addRow(String.format("Rp%s", String.format("%,.0f", item.getHarga()).replace(',', '.')), tableRowColor);
      table.addRow(String.valueOf(item.getKuantitas()), tableRowColor);
      table.addRow(String.format("Rp%s", String.format("%,.0f", item.getJumlah()).replace(',', '.')), tableRowColor);
    }

    table.addRow("", tableRowColor);
    table.addRow("", tableRowColor);
    table.addRow("Sub Total", tableRowColor);
    table.addRow(String.format("Rp%s", String.format("%,.0f", selectedTransaksi.getTotalHarga()).replace(',', '.')),
        tableRowColor);

    table.addRow("", tableRowColor);
    table.addRow("", tableRowColor);
    table.addRow("Pajak", tableRowColor);
    table.addRow("11.00%", tableRowColor);

    table.addRow("", tableRowColor);
    table.addRow("", tableRowColor);
    table.addRow("Total", tableHeadColor);
    table.addRow(
        String.format("Rp%s", String.format("%,.0f", selectedTransaksi.getTotalHarga() * 1.11).replace(',', '.')),
        tableHeadColor);

    contentStream.close();
    document.save(
        "C:\\Users\\Nizhar Maulana\\OneDrive - Universitas Negeri Jakarta (UNJ)\\College\\122 - Pemrograman Berorientasi Objek Lanjut\\kasir\\target\\pdf\\"
            + selectedTransaksi.getUuid() + ".pdf");

    document.close();
  }

  private static String formatTanggal(Timestamp tanggal) {
    SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.forLanguageTag("id-ID"));
    sdf.setTimeZone(java.util.TimeZone.getTimeZone("GMT+14"));
    return sdf.format(tanggal);
  }

  private static String getWaktu(Timestamp tanggal) {
    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.forLanguageTag("id-ID"));
    sdf.setTimeZone(java.util.TimeZone.getTimeZone("GMT+14"));
    return sdf.format(tanggal);
  }
}
