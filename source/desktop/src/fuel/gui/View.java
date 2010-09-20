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
package fuel.gui;

import fuel.Main;
import fuel.lib.Database;
import java.awt.BorderLayout;
import java.sql.SQLException;
import java.util.Observable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.Observer;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import server.Server;

/**
 *
 * @author Mark
 */
public class View extends JFrame implements Observer {

    private JTabbedPane tabs;
    private RecordInputPanel recordInputPanel;
    private MotorInputPanel motorInputPanel;
    private StationInputPanel stationInputPanel;
    private StatisticsSelectionPanel statsPanel;
    private Main main;
    private List<Server> servers;
    private ServerPanel serverPanel;

    public View(Main main, Database database, List<Server> servers) {
        super("Fuel " + Main.VERSION);
        this.main = main;
        main.addObserver(this);
        this.servers = servers;
        try {
            setSize(1024, 500);
            tabs = new JTabbedPane();
            recordInputPanel = new RecordInputPanel(this, database);
            motorInputPanel = new MotorInputPanel(this, database);
            stationInputPanel = new StationInputPanel(this, database);
            statsPanel = new StatisticsSelectionPanel(database);
            tabs.add("Tankbeurten", recordInputPanel);
            tabs.add("Voertuigen", motorInputPanel);
            tabs.add("Tankstation", stationInputPanel);
            tabs.add("Statistieken", statsPanel);            
            serverPanel = new ServerPanel(database, main, servers);
            tabs.add("Server", serverPanel);
            tabs.add("Instellingen", new SettingsPanel(database));
            refreshTankRecords();

            setLayout(new BorderLayout());
            add(tabs, BorderLayout.CENTER);
            setVisible(true);

            setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            addWindowListener(new WindowAdapter() {

                @Override
                public void windowClosing(WindowEvent e) {
                    closing();
                }
            });

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Fout in verbinding met de database", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void closing() {
        if (servers.get(0).isStarted()) {
            JOptionPane.showMessageDialog(this, "De server draait, dus alleen het hoofdscherm wordt gesloten\nKlik met de rechter muisknop op het icoon rechtsonder voor meer informatie");
            dispose();
            tabs = null;
            recordInputPanel = null;
            motorInputPanel = null;
            stationInputPanel = null;
            statsPanel = null;
            try {
                finalize();
            } catch (Throwable ex) {
            }
            main.viewClosing();
        } else if (JOptionPane.showConfirmDialog(this, "Weet u zeker dat u het programma wilt sluiten?", "Afsluiten", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    public void refreshTankRecords() throws SQLException {
        try {
            recordInputPanel.refreshTankRecords();
        } catch (Exception e) {
        }
        try {
            motorInputPanel.refreshMotorRecords();
        } catch (Exception e) {
        }
        try {
            stationInputPanel.refreshStationRecords();
        } catch (Exception e) {
        }
    }

    public void update(Observable o, Object arg) {
        try {
            refreshTankRecords();
        } catch (SQLException ex) {
            Logger.getLogger(View.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
