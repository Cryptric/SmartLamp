package ch.cryptric;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class Main {

    public enum OS {
        WINDOWS, LINUX, MAC, SOLARIS
    };// Operating systems.


    private static final Image lightOn;
    private static final Image lightOff;

    static {
        try {
            lightOn = ImageIO.read(Objects.requireNonNull(Main.class.getClassLoader().getResourceAsStream("light-on.png")));
            lightOff = ImageIO.read(Objects.requireNonNull(Main.class.getClassLoader().getResourceAsStream("light-off.png")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static void main(String[] args) {
        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray is not supported on this platform.");
            return;
        }

        // Create a system tray icon
        SystemTray tray = SystemTray.getSystemTray();

        PopupMenu popup = new PopupMenu();

        TrayIcon trayIcon = new TrayIcon(lightOn, "SmartLamp", popup);
        trayIcon.setImageAutoSize(true);

        trayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!(getOS() == OS.WINDOWS) || e.getButton() != MouseEvent.BUTTON1) {
                    return;
                }

                if (toggleLight(args[0])) {
                    trayIcon.setImage(lightOn);
                } else {
                    trayIcon.setImage(lightOff);
                }
            }
        });

        MenuItem quitMenuItem = new MenuItem("Quit SmartLamp");
        quitMenuItem.addActionListener(e -> System.exit(0));

        popup.add(quitMenuItem);

        try {
            tray.add(trayIcon);
        } catch (AWTException ex) {
            System.out.println("TrayIcon could not be added.");
        }

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (getStatus(args[0])) {
                    trayIcon.setImage(lightOn);
                } else {
                    trayIcon.setImage(lightOff);
                }
            }
        }, 0, 5000);
    }

    private static boolean toggleLight(String address) {
        return lightAPI("http://" + address + ":80/?m=1&o=1");
    }

    private static boolean getStatus(String address) {
        return lightAPI("http://" + address + ":80/?m=1");
    }

    private static boolean lightAPI(String urlStr) {
        String response = "";
        try {
            URL url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();

                response = content.toString();
                System.out.println("GET request successful. Response: " + response);
            } else {
                System.out.println("GET request failed. Response Code: " + responseCode);
            }
            connection.disconnect();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return response.contains("ON");
    }

    public static OS getOS() {
        OS os = null;
        String operSys = System.getProperty("os.name").toLowerCase();
        if (operSys.contains("win")) {
            os = OS.WINDOWS;
        } else if (operSys.contains("nix") || operSys.contains("nux")
                || operSys.contains("aix")) {
            os = OS.LINUX;
        } else if (operSys.contains("mac")) {
            os = OS.MAC;
        } else if (operSys.contains("sunos")) {
            os = OS.SOLARIS;
        }
        return os;
    }


}