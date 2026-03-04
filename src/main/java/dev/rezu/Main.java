package dev.rezu;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.net.http.HttpClient;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main implements NativeKeyListener {

    private final Translator translator;
    private final OCRService ocrService;
    private final Robot robot;

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();

    private final AtomicBoolean isCapturing = new AtomicBoolean(false);

    private TrayIcon trayIcon;

    public Main() throws Exception {
        robot = new Robot();
        ocrService = new OCRService();

        HttpClient httpClient = HttpClient.newHttpClient();
        translator = new Translator(httpClient);

        setupTray();
        setupGlobalHotkey();
    }

    private void setupTray() throws AWTException {
        if (!SystemTray.isSupported()) {
            System.out.println("System tray not supported; running without tray.");
            return;
        }

        SystemTray tray = SystemTray.getSystemTray();
        PopupMenu popup = new PopupMenu();
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.addActionListener((ActionEvent e) -> {
            shutdown();
            System.exit(0);
        });
        popup.add(exitItem);

        Image img = new ImageIcon(
                getClass().getResource("/screenshot_translator.png")
        ).getImage();

        trayIcon = new TrayIcon(img, "Screen Translator", popup);
        trayIcon.setImageAutoSize(true);
        tray.add(trayIcon);
    }

    private void setupGlobalHotkey() {
        Logger logger = Logger.getLogger(
                GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.OFF);

        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException ex) {
            ex.printStackTrace();
            return;
        }

        GlobalScreen.addNativeKeyListener(this);
        System.out.println("READY! Press Ctrl+Alt+S to capture.");
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        int keyCode = e.getKeyCode();
        int modifiers = e.getModifiers();

        boolean ctrl = (modifiers & NativeKeyEvent.CTRL_MASK) != 0;
        boolean alt = (modifiers & NativeKeyEvent.ALT_MASK) != 0;

        if (keyCode == NativeKeyEvent.VC_S && ctrl && alt) {
            if (isCapturing.compareAndSet(false, true)) {
                Thread.ofVirtual().start(() -> {
                    try {
                        doCaptureAndTranslate();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    } finally {
                        isCapturing.set(false);
                    }
                });
            }
        }
    }

    private void doCaptureAndTranslate() {
        Rectangle sel = SelectionWindow.selectArea();
        if (sel == null) {
            System.out.println("Selection cancelled.");
            return;
        }

        try {
            BufferedImage shot = robot.createScreenCapture(sel);
            String extracted = ocrService.extractText(shot);

            if (extracted.length() < 2) {
                System.out.println("No text found.");
                return;
            }

            System.out.println("OCR: " +
                    extracted.replaceAll("\\r?\\n", " "));

            String translation =
                    translator.translateToEnglish(extracted);

            System.out.println("Translation: " + translation);

            TranslationOverlay.show(
                    translation,
                    sel,
                    scheduler
            );

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void shutdown() {
        try {
            GlobalScreen.unregisterNativeHook();
        } catch (Exception ignored) {}

        scheduler.shutdownNow();

        if (trayIcon != null) {
            SystemTray.getSystemTray().remove(trayIcon);
        }
    }

    @Override public void nativeKeyReleased(NativeKeyEvent e) {}
    @Override public void nativeKeyTyped(NativeKeyEvent e) {}

    public static void main(String[] args) throws Exception {
        new Main();
    }
}