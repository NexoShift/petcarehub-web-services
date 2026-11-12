package com.nexoshift.petcarehub.aiassistance.support;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

/** Imágenes PNG generadas en memoria para los tests de la IA (no hacen falta archivos). */
public final class TestImages {

  private TestImages() {
  }

  /** Foto "de piel": color, del tamaño pedido. */
  public static byte[] skin(int width, int height) {
    return png(width, height, new Color(220, 160, 140), new Color(180, 60, 60));
  }

  /** "Radiografía": solo grises (saturación 0). */
  public static byte[] radiograph(int width, int height) {
    return png(width, height, new Color(40, 40, 40), new Color(200, 200, 200));
  }

  private static byte[] png(int width, int height, Color background, Color spot) {
    var image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    var graphics = image.createGraphics();
    graphics.setColor(background);
    graphics.fillRect(0, 0, width, height);
    graphics.setColor(spot);
    graphics.fillOval(width / 3, height / 3, width / 4, height / 5);
    graphics.dispose();
    var bytes = new ByteArrayOutputStream();
    try {
      ImageIO.write(image, "png", bytes);
    } catch (IOException exception) {
      throw new UncheckedIOException(exception);
    }
    return bytes.toByteArray();
  }
}
