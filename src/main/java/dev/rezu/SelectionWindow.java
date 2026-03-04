package dev.rezu;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.CountDownLatch;

public class SelectionWindow extends JWindow {
    private final Point start = new Point();
    private final Point end = new Point();
    private Rectangle selection = null;
    final JComponent canvas = new JComponent() {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (selection != null) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(0, 0, 0, 100));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setComposite(AlphaComposite.Clear);
                g2.fillRect(selection.x, selection.y, selection.width, selection.height);
                g2.setComposite(AlphaComposite.SrcOver);
                g2.setColor(new Color(0, 200, 0));
                g2.setStroke(new BasicStroke(2));
                g2.drawRect(selection.x, selection.y, selection.width - 1, selection.height - 1);
                g2.dispose();
            } else {
                g.setColor(new Color(0, 0, 0, 150));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        }
    };
    private final CountDownLatch latch = new CountDownLatch(1);

    private SelectionWindow() {
        setAlwaysOnTop(true);
        setBackground(new Color(0, 0, 0, 0));
        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));

        // Multi-monitor support calculation
        Rectangle virtualBounds = new Rectangle();
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        for (GraphicsDevice gd : ge.getScreenDevices()) {
            virtualBounds = virtualBounds.union(gd.getDefaultConfiguration().getBounds());
        }
        setBounds(virtualBounds.x, virtualBounds.y, virtualBounds.width, virtualBounds.height);

        add(canvas);

        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                start.setLocation(e.getX(), e.getY());
                selection = new Rectangle(start);
                repaint();
            }
            @Override
            public void mouseDragged(MouseEvent e) {
                end.setLocation(e.getX(), e.getY());
                updateSelection();
                repaint();
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                end.setLocation(e.getX(), e.getY());
                updateSelection();
                latch.countDown();
                setVisible(false);
                dispose();
            }
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    selection = null;
                    latch.countDown();
                    setVisible(false);
                    dispose();
                }
            }
        };
        canvas.addMouseListener(ma);
        canvas.addMouseMotionListener(ma);

        canvas.setFocusable(true);
        canvas.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    selection = null;
                    latch.countDown();
                    setVisible(false);
                    dispose();
                }
            }
        });
    }

    private void updateSelection() {
        int x = Math.min(start.x, end.x);
        int y = Math.min(start.y, end.y);
        int w = Math.abs(start.x - end.x);
        int h = Math.abs(start.y - end.y);
        selection = new Rectangle(x, y, w, h);
    }

    public static Rectangle selectArea() {
        final SelectionWindow w = new SelectionWindow();
        try {
            SwingUtilities.invokeAndWait(() -> {
                w.setVisible(true);
                w.canvas.requestFocusInWindow();
            });
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        try {
            w.latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }

        Rectangle r = w.selection;
        if (r == null || r.width < 5 || r.height < 5) return null;

        // Offset the local coordinates back to global screen coordinates
        Rectangle screenRect = new Rectangle(r);
        screenRect.x += w.getX();
        screenRect.y += w.getY();

        return screenRect;
    }
}