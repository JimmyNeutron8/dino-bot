import java.awt.Robot;
import java.awt.Window;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Point;
import java.lang.Thread;
import java.awt.Rectangle;
import java.lang.InterruptedException;
import java.awt.MouseInfo;
import java.awt.AWTException;
import java.awt.event.MouseAdapter;
import java.awt.image.BufferedImage;
import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;

public class Dino {

  // How often we should check for obstacles
  private static final int scanInterval = 10;

  // The rectangle we should be scanning
  private static Rectangle scanRect = null;

  // The luminosity of halfway between day and night
  private static float dayThreshold = 0.4157f;

  // Obstacle position relative to origin of rectangle
  private static int obstX = 80;
  private static int obstY = 5;
  private static int obstLength = 90;
  private static int obstHeight = 10;

  // Should the game start or are we setting up?
  private static boolean ready = false;

  // The robot instance
  private static Robot robot = null;

  public static void main(String[] args) throws InterruptedException, AWTException {
    robot = new Robot();

    Window window = new Window(null) {
      public void paint (Graphics g) {
        if (scanRect == null) {
          return;
        }
        g.setColor(Color.green);
        g.drawRect(0, 0, scanRect.width, scanRect.height);
        g.setColor(Color.red);
        g.drawRect(obstX, obstY, obstLength, obstHeight);
      }
    };
    window.setBackground(new Color(0, true));
    window.setAlwaysOnTop(true);
    window.setVisible(true);
    window.addMouseListener(new MouseAdapter () {
      public void mouseClicked (MouseEvent e) {
        ready = true;
      }
    });

    while (!ready) {
      Point point = MouseInfo.getPointerInfo().getLocation();
      scanRect = new Rectangle(point.x, point.y, obstX + obstLength + 1, obstY + obstHeight + 1);
      window.setBounds(scanRect);
      Thread.sleep(40);
    }

    window.setVisible(false);

    while (ready) {
      BufferedImage screenCap = robot.createScreenCapture(scanRect);

      // Get the color of the day pixel
      float dayLum = getLum(screenCap.getRGB(0, 0));

      // Is it day?
      boolean day = false;
      if (dayLum > dayThreshold) {
        day = true;
      } // Otherwise, will stay false

      // Scan a range of pixels for obstacles
      boolean obstacle = false;
      for (int x = 0; x < obstLength; x++) {
        for (int y = 0; y < obstHeight; y++) {
          float pixelLum = getLum(screenCap.getRGB(obstX + x, obstY + y));

          if (day) {
            if (pixelLum <= dayThreshold) {
              obstacle = true;
              break;
            }
          }else{
            if (pixelLum > dayThreshold) {
              obstacle = true;
              break;
            }
          }
        }
      }

      // Finally, jump if there is an obstacle
      if (obstacle) {
        jump();
      }

      Thread.sleep(scanInterval);
    }
  }

  private static void jump () {
    robot.keyPress(KeyEvent.VK_UP);
  }

  private static float getLum (int rgb) {
    // Split pixel into RGB values
    int red = (rgb >>> 16) & 0xFF;
    int green = (rgb >>> 8) & 0xFF;
    int blue = (rgb >>> 0) & 0xFF;

    return (red * 0.2126f + green * 0.7152f + blue * 0.0722f) / 255;
  }

}
