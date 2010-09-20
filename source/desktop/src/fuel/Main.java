/*
 *Copyright 2009 Mark Rietveld
 * This file is part of Fuel.

Fuel is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Fuel is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Fuel.  If not, see <http://www.gnu.org/licenses/>.

 */
package fuel;

import fuel.gui.View;
import fuel.lib.Database;
import fuel.lib.Expense;
import fuel.lib.Motorcycle;
import fuel.lib.VersionChecker;
import java.awt.AWTException;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Observable;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import server.InputServer;
import server.Server;
import server.WebServer;

/**
 *
 * @author Mark
 */
public class Main extends Observable {

    public static String VERSION = "Beta 0.9 (1-10-2009)";
    public static int VERSIONID = 9;
    public static String DATE_FORMAT_NOW = "dd-MM HH:mm";
    private MenuItem showItem;
    private List<Server> servers;
    private Database database;
    private Main thisMain;
    private boolean viewVisible;
    private String servermessages;

    public Main(boolean service) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.out.println("Error getting look and feel from system");
        }
        servermessages = "";
        try {
            thisMain = this;
            database = new Database("data");
            servers = new ArrayList<Server>();
            servers.add(new WebServer(database, this));
            servers.add(new InputServer(database, this));
            if (!service) {
                new View(this, database, servers);
                viewVisible = true;
                ResultSet result = database.Query("SELECT * FROM settings", true);
                result.next();
                if (result.getInt("checkforupdates") == 1) {
                    try {
                        VersionChecker.doVersionCheck(true, false);
                    } catch (IOException ex) {
                    }
                }
            } else {
                for (Server server : servers) {
                    Thread t = new Thread(server);
                    t.start();
                }
            }
            try {
                if (SystemTray.isSupported()) {
                    TrayIcon trayIcon = null;
                    SystemTray tray = SystemTray.getSystemTray();
                    Image image = Toolkit.getDefaultToolkit().getImage("lib\\icon.png");
                    ActionListener listener = new ActionListener() {

                        public void actionPerformed(ActionEvent e) {
                            if (e.getActionCommand() == null || e.getActionCommand().equals("SHOW")) {
                                if (!viewVisible) {
                                    new View(thisMain, database, servers);
                                    showItem.setEnabled(false);
                                    viewVisible = true;
                                }
                            } else if (e.getActionCommand().equals("EXIT")) {
                                if (servers.get(0).isStarted()) {
                                    int sure = JOptionPane.showConfirmDialog(null, "De server draait, weet u zeker dat u wilt sluiten?", "Sluiten", JOptionPane.YES_NO_OPTION);
                                    if (sure == JOptionPane.YES_OPTION) {
                                        System.exit(0);
                                    }
                                } else {
                                    System.exit(0);
                                }
                            } else if (e.getActionCommand().equals("ABOUT")) {
                                JPanel aboutPanel = new JPanel(new GridLayout(0, 1));
                                aboutPanel.add(new JLabel("Fuel versie " + VERSION));
                                aboutPanel.add(new JLabel("Gemaakt door Mark Rietveld"));
                                aboutPanel.add(new JLabel("Contact: markrietveld@gmail.com"));
                                JOptionPane.showMessageDialog(null, aboutPanel, "Info", JOptionPane.INFORMATION_MESSAGE);
                            } else if (e.getActionCommand().equals("VERSIONCHECK")) {
                                try {
                                    VersionChecker.doVersionCheck();
                                } catch (IOException ex) {
                                    JOptionPane.showMessageDialog(null, "Kon geen verbinding maken met de server", "Fout", JOptionPane.ERROR_MESSAGE);
                                }
                            }
                        }
                    };
                    PopupMenu popup = new PopupMenu();

                    showItem = new MenuItem("Open hoofdscherm");
                    showItem.setActionCommand("SHOW");
                    showItem.addActionListener(listener);
                    showItem.setEnabled(service);
                    popup.add(showItem);
                    showItem.addActionListener(listener);

                    MenuItem aboutItem = new MenuItem("Info");
                    aboutItem.setActionCommand("ABOUT");
                    popup.add(aboutItem);
                    aboutItem.addActionListener(listener);

                    MenuItem versionItem = new MenuItem("Update programma");
                    versionItem.setActionCommand("VERSIONCHECK");
                    popup.add(versionItem);
                    versionItem.addActionListener(listener);

                    MenuItem exitItem = new MenuItem("Afsluiten");
                    exitItem.setActionCommand("EXIT");
                    popup.add(exitItem);
                    exitItem.addActionListener(listener);


                    trayIcon = new TrayIcon(image, "Fuel", popup);
                    trayIcon.addActionListener(listener);
                    try {
                        tray.add(trayIcon);
                    } catch (AWTException e) {
                        System.err.println(e);
                    }

                } else {
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            //check if any expense is set to generate a warning before or at this date
            Calendar cal = Calendar.getInstance();
            Date date = new Date(cal.getTimeInMillis());
            for (Motorcycle motor : database.getMotorcycles()) {
                ResultSet result = database.Query("SELECT max(totaldistance) FROM fuelrecords WHERE motorcycleid = " + motor.getId(), true);
                if (result.next()) {
                    double totalDistance = result.getDouble(1);
                    for (Expense expense : database.getExpensesByMotorId(motor)) {
                        if (expense.getCheckDate() != null && date.after(expense.getCheckDate())) {
                            int sure = JOptionPane.showConfirmDialog(null, "Een uitgave die u op " + expense.getDate() + " gedaan heeft voor uw  " + motor.toString() + " is aan controle toe.\n" +
                                    " U heeft " + (totalDistance - expense.getTotalDistance()) + "km afgelegd sinds deze uitgave.\n\n" +
                                    "Het betreft:\n" +
                                    "Onderdeel: " + expense.getName() + "\n" +
                                    "Merk: " + expense.getBrand() + "\n" +
                                    "Type: " + expense.getType() + "\n\n\n" +
                                    "Wilt u deze waarschuwing nu uitschakelen?", "Waarschuwing", JOptionPane.YES_NO_OPTION);
                            if (sure == JOptionPane.YES_OPTION) {
                                expense.setCheckTotalDistance(0.0);
                                expense.setCheckDate(null);
                                expense.toDatabase();
                            }
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Kan geen verbinding met de gegevens op de hardeschijf maken");
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            JOptionPane.showMessageDialog(null, "Kan de driver voor het ophalen van gegevens van de hardeschijf niet vinden");
        }
    }

    public void viewClosing() {
        if (showItem != null) {
            showItem.setEnabled(true);
        } else {
            System.exit(0);
        }
        viewVisible = false;
    }

    public synchronized void addServermessage(String message) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
        servermessages += sdf.format(cal.getTime()) + " - " + message + "\n";
        setChanged();
        notifyObservers();
    }

    public synchronized String getServermessages() {
        return servermessages;
    }

    public static void main(String[] args) {
        boolean service = false;
        for (String s : args) {
            if (s.contains("-service")) {
                service = true;
            }
        }
        new Main(service);
    }
}
