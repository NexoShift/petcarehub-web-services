package com.nexoshift.petcarehub.aiassistance.application.internal.validation;

import com.nexoshift.petcarehub.aiassistance.domain.exceptions.ImageNotEvaluableException;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.ImageType;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.springframework.stereotype.Component;

/**
 * Rejects files that cannot be analyzed (422) before spending storage or credits.
 * The bytes are checked: the Content-Type sent by the browser is not trusted.
 */
@Component
public class ImageValidator {

  static final int MIN_SIDE_PX = 224;
  static final double MAX_RADIOGRAPH_SATURATION = 0.15;

  public void validate(byte[] content, ImageType imageType) {
    var image = read(content);
    if (image.getWidth() < MIN_SIDE_PX || image.getHeight() < MIN_SIDE_PX) {
      throw new ImageNotEvaluableException(
          "the image must be at least " + MIN_SIDE_PX + " px on each side");
    }
    if (imageType == ImageType.RADIOGRAPH && averageSaturation(image) > MAX_RADIOGRAPH_SATURATION) {
      throw new ImageNotEvaluableException("it does not look like a radiograph");
    }
  }

  private BufferedImage read(byte[] content) {
    BufferedImage image;
    try {
      image = ImageIO.read(new ByteArrayInputStream(content));
    } catch (IOException | RuntimeException exception) {
      throw new ImageNotEvaluableException("the file could not be read");
    }
    if (image == null) {
      throw new ImageNotEvaluableException("the file is not a valid image");
    }
    return image;
  }

  /** Mean color saturation (0 = gray, 1 = vivid), sampled every 10 px to stay fast. */
  double averageSaturation(BufferedImage image) {
    double total = 0;
    int samples = 0;
    for (int y = 0; y < image.getHeight(); y += 10) {
      for (int x = 0; x < image.getWidth(); x += 10) {
        int rgb = image.getRGB(x, y);
        int red = (rgb >> 16) & 0xff;
        int green = (rgb >> 8) & 0xff;
        int blue = rgb & 0xff;
        int max = Math.max(red, Math.max(green, blue));
        int min = Math.min(red, Math.min(green, blue));
        total += max == 0 ? 0 : (max - min) / (double) max;
        samples++;
      }
    }
    return samples == 0 ? 0 : total / samples;
  }
}
