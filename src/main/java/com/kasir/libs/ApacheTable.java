package com.kasir.libs;

import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;

import javafx.scene.paint.Color;

public class ApacheTable {
  PDDocument document;
  PDPageContentStream contentStream;

  private int[] colWidths;
  private int rowHeight;
  private int x;
  private int y;
  private int colPosition = 0;
  private int initialPosition;
  private float fontSize;
  private PDFont font;
  private Color color;

  public ApacheTable(PDDocument document, PDPageContentStream contentStream) {
    this.document = document;
    this.contentStream = contentStream;
  }

  public void setTable(int[] colWidths, int rowHeight, int x, int y) {
    this.colWidths = colWidths;
    this.rowHeight = rowHeight;
    this.x = x;
    this.y = y;
    this.initialPosition = x;
  }

  public void setTableFont(PDFont font, float fontSize, Color color) {
    this.font = font;
    this.fontSize = fontSize;
    this.color = color;
  }

  public void addRow(String text, Color fillColor) throws IOException {
    contentStream.setStrokingColor(1f);

    if (fillColor != null) {
      contentStream.setNonStrokingColor(
          (float) (fillColor.getRed()),
          (float) (fillColor.getGreen()),
          (float) (fillColor.getBlue()));
    }

    contentStream.addRect(x, y, colWidths[colPosition], rowHeight);

    if (fillColor == null) {
      contentStream.stroke();
    } else {
      contentStream.fillAndStroke();
    }

    contentStream.beginText();
    contentStream.setNonStrokingColor(
        (float) (color.getRed()),
        (float) (color.getGreen()),
        (float) (color.getBlue()));

    if (colPosition == 4 || colPosition == 5) {
      float fontWidth = font.getStringWidth(text) / 1000 * fontSize;
      contentStream.newLineAtOffset(x + colWidths[colPosition] - 5 - fontWidth, y + fontSize / 2);
    } else {
      contentStream.newLineAtOffset(x + 5, y + fontSize / 2);
    }

    contentStream.showText(text);
    contentStream.endText();

    x = x + colWidths[colPosition];
    colPosition++;

    if (colPosition == colWidths.length) {
      colPosition = 0;
      x = initialPosition;
      y -= rowHeight;
    }
  }
}
