import java.awt.Color;
import java.io.IOException;

public class Test {

  public static void main(final String[] args) {

    final int rgb = new Color(255, 123, 21, 35).getRGB();
    System.out.println(String.format("#%06X", rgb & 0xFFFFFF));
  }

}
