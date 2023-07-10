package ch.cryptric;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.Objects;

public class Main {

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
        TrayIcon trayIcon = new TrayIcon(lightOn, "SystemTray Menu");
        trayIcon.setImageAutoSize(true);

        if (getStatus(args[0])) {
            trayIcon.setImage(lightOn);
        } else {
            trayIcon.setImage(lightOff);
        }

        trayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (toggleLight(args[0])) {
                    trayIcon.setImage(lightOn);
                } else {
                    trayIcon.setImage(lightOff);
                }
            }
        });

        try {
            tray.add(trayIcon);
        } catch (AWTException ex) {
            System.out.println("TrayIcon could not be added.");
        }
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
}