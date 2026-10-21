package com.vimspace.boh.panel;

import com.vimspace.boh.config.AppConfig;
import com.vimspace.boh.service.BohApiClient;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 * Renders the {@link TillSessionPanel} to a PNG so the layout can be inspected
 * without a human present.
 */
public class TillSessionPanelScreenshot {

    public static void main(String[] args) throws Exception {
        AppConfig config = new AppConfig();
        BohApiClient api = new BohApiClient(config);

        final JFrame[] holder = new JFrame[1];
        SwingUtilities.invokeAndWait(() -> {
            TillSessionPanel panel = new TillSessionPanel(api);
            JFrame frame = new JFrame("Till Sessions");
            frame.setContentPane(panel);
            frame.setSize(1150, 900);
            frame.setVisible(true);
            holder[0] = frame;
        });

        Thread.sleep(4000);

        final BufferedImage[] shot = new BufferedImage[1];
        final StringBuilder layout = new StringBuilder();
        SwingUtilities.invokeAndWait(() -> {
            JFrame frame = holder[0];
            frame.validate();
            BufferedImage img = new BufferedImage(frame.getWidth(), frame.getHeight(),
                    BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            frame.printAll(g);
            g.dispose();
            shot[0] = img;
            dump(frame.getContentPane(), 0, layout);
        });

        System.out.println(layout);

        File out = new File(args.length > 0 ? args[0] : "till-session-panel.png");
        ImageIO.write(shot[0], "png", out);
        System.out.println("Wrote " + out.getAbsolutePath());
        System.exit(0);
    }

    /** Prints the laid-out bounds of every component so the layout can be checked. */
    static void dump(Component c, int depth, StringBuilder sb) {
        for (int i = 0; i < depth; i++) {
            sb.append("  ");
        }
        sb.append(c.getClass().getSimpleName())
          .append(" bounds=").append(c.getBounds())
          .append(" visible=").append(c.isVisible());
        if (c instanceof JLabel) {
            sb.append(" text=\"").append(((JLabel) c).getText()).append("\"");
        }
        if (c instanceof JTable) {
            JTable t = (JTable) c;
            sb.append(" rows=").append(t.getRowCount())
              .append(" cols=").append(t.getColumnCount());
        }
        sb.append('\n');
        if (c instanceof java.awt.Container) {
            for (Component child : ((java.awt.Container) c).getComponents()) {
                dump(child, depth + 1, sb);
            }
        }
    }
}
