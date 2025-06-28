package com.kasir.libs;

import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;

import javafx.scene.paint.Color;

public class ApacheText {
  PDDocument document;
  PDPageContentStream contentStream;

  public ApacheText(PDDocument document, PDPageContentStream contentStream) {
    this.document = document;
    this.contentStream = contentStream;
  }

  public void addSingleLineText(PDPage page, String text, float x, float y, PDFont font, float fontSize, Color color,
      String align)
      throws IOException {
    // Implementation for adding single line text
    contentStream.beginText();
    contentStream.setFont(font, fontSize);
    contentStream.setNonStrokingColor(
        (int) (color.getRed() * 255),
        (int) (color.getGreen() * 255),
        (int) (color.getBlue() * 255));
    if ("center".equalsIgnoreCase(align)) {
      float titleWidth = font.getStringWidth(text) / 1000 * fontSize;
      contentStream.newLineAtOffset((page.getMediaBox().getWidth() - titleWidth) / 2, y);
    } else if ("right".equalsIgnoreCase(align)) {
      float textWidth = getTextWidth(text, font, fontSize);
      x -= textWidth; // Right alignment
    } else if ("left".equalsIgnoreCase(align)) {
      contentStream.newLineAtOffset(x, y);
    }
    contentStream.showText(text);
    contentStream.endText();
    contentStream.moveTo(0, 0);
  }

  public void addMultilineText(String[] textArray, float leading, float x, float y, PDFont font, float fontSize,
      Color color) throws IOException {
    // Implementation for adding multiline text
    contentStream.beginText();
    contentStream.setFont(font, fontSize);
    contentStream.setNonStrokingColor(
        (int) (color.getRed() * 255),
        (int) (color.getGreen() * 255),
        (int) (color.getBlue() * 255));
    contentStream.setLeading(leading);
    contentStream.newLineAtOffset(x, y);
    for (String text : textArray) {
      contentStream.showText(text);
      contentStream.newLineAtOffset(0, -leading);
    }
    contentStream.endText();
    contentStream.moveTo(0, 0);
  }

  public float getTextWidth(String text, PDFont font, float fontSize) throws IOException {
    // Implementation for calculating text width
    return font.getStringWidth(text) / 1000 * fontSize;
  }

}
