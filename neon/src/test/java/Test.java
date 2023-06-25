import java.awt.Color;
import java.io.IOException;

public class Test {

  public static void main(final String[] args) {

    final int r = 69;
    final int g = 69;
    final int b = 69;
    final int a = 69;
    final int bgra = (a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF);




    final int rgba = (bgra & 0x00ff0000) >> 16 | (bgra & 0xff00ff00) | (bgra & 0x000000ff) << 16; // this works to convert bgra to rgba

    final int rgb = removeAlpha(rgba);
    final int red = rgb >> 16 & 0xFF;
    final int green = rgb >> 8 & 0xFF;
    final int blue = rgb & 0xFF;

    System.out.println("r: " + red + " g: " + green + " b: " + blue);
  }

  // Write a function that takes an int rgba and removes the alpha channel from it
  private static int removeAlpha(final int rgba) {
    return rgba & 0x00ffffff;
  }
}
