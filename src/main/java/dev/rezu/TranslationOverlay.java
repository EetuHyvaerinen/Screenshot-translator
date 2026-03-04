package dev.rezu;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TranslationOverlay {

    private static final long OVERLAY_TIMEOUT_SECONDS = 10L;

    private TranslationOverlay() {
    }

    public static void show(String text,
                            Rectangle selection,
                            ScheduledExecutorService scheduler) {

        SwingUtilities.invokeLater(() -> {
            JWindow popup = new JWindow();
            popup.setAlwaysOnTop(true);

            JPanel panel = new JPanel(new BorderLayout());
            panel.setBackground(new Color(30, 30, 30, 230));
            panel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));

            JTextArea area = new JTextArea(text);
            area.setWrapStyleWord(true);
            area.setLineWrap(true);
            area.setEditable(false);
            area.setOpaque(false);
            area.setForeground(Color.WHITE);
            area.setFont(new Font("SansSerif", Font.PLAIN, 14));
            area.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

            int preferredWidth = Math.max(200, Math.min(selection.width, 500));
            area.setSize(new Dimension(preferredWidth, 100));

            panel.add(area, BorderLayout.CENTER);
            popup.add(panel);
            popup.pack();

            int px = selection.x;
            int py = selection.y - popup.getHeight() - 10;

            if (py < 10) {
                py = selection.y + selection.height + 10;
            }

            Rectangle screenBounds =
                    GraphicsEnvironment.getLocalGraphicsEnvironment()
                            .getMaximumWindowBounds();

            if (px + popup.getWidth() >
                    screenBounds.x + screenBounds.width) {
                px = screenBounds.x + screenBounds.width
                        - popup.getWidth() - 10;
            }

            popup.setLocation(px, py);
            popup.setVisible(true);

            scheduler.schedule(popup::dispose,
                    OVERLAY_TIMEOUT_SECONDS,
                    TimeUnit.SECONDS);
        });
    }
}