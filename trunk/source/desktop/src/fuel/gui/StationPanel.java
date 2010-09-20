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

import fuel.lib.*;
import info.clearthought.layout.TableLayout;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author Mark
 */
public class StationPanel extends JPanel implements StatsContainer {

    private Station station;
    private JTextField locationField;
    private JTextField nameField;
    private Controller controller;
    private boolean changed;
    private Database database;
    private View view;
    private JButton saveButton;
    private JButton cancelButton;
    private JButton deleteButton;
    private StationInputPanel parent;

    public StationPanel(Station station, Database database, View view, StationInputPanel parent) throws SQLException {
        this.station = station;
        this.database = database;
        this.changed = false;
        this.view = view;
        this.parent = parent;
        controller = new Controller();
        setLayout(new BorderLayout());
        locationField = new JTextField(10);
        locationField.addKeyListener(controller);
        nameField = new JTextField(10);
        nameField.addKeyListener(controller);
        saveButton = new JButton("Tankstation opslaan");
        saveButton.setActionCommand("SAVE");
        saveButton.addActionListener(controller);
        cancelButton = new JButton("Verandering niet opslaan");
        cancelButton.setActionCommand("DISCARD");
        cancelButton.addActionListener(controller);
        deleteButton = new JButton("Verwijder Tankstation");
        deleteButton.addActionListener(controller);
        deleteButton.setActionCommand("DELETE");
        fill();
        setVisible(true);
        revalidate();
        repaint();
    }

    public Station getStation() {
        return station;
    }

    public void fill() throws SQLException {
        removeAll();
        JPanel container = new JPanel(new BorderLayout());
        double size[][] = {{0.25, TableLayout.FILL},
            {TableLayout.FILL, TableLayout.FILL}};

        JPanel inputs = new JPanel(new TableLayout(size));

        locationField.setText(this.station.getLocation());        
        if (!this.station.hasBeenSaved()) {
            locationField.setBackground(Color.RED);
        } else {
            locationField.setBackground(Color.GREEN);
        }
        inputs.add(new JLabel("Plaats: "), "0,0");
        inputs.add(locationField, "1,0");
        //inputs.add(brandPanel);

        nameField.setText(this.station.getName());
        if (!this.station.hasBeenSaved()) {
            nameField.setBackground(Color.RED);
        } else {
            nameField.setBackground(Color.GREEN);
        }
        inputs.add(new JLabel("Naam: "), "0,1");
        inputs.add(nameField, "1,1");
        //inputs.add(typePanel);

        
        saveButton.setEnabled(station.isChanged() || !station.hasBeenSaved());        
        cancelButton.setEnabled(station.isChanged() || !station.hasBeenSaved());
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        if (this.station.hasBeenSaved()) {            
            buttonPanel.add(deleteButton);
        }
        container.add(inputs, BorderLayout.CENTER);
        container.add(buttonPanel, BorderLayout.SOUTH);
        container.setBorder(BorderFactory.createEtchedBorder());
        add(container, BorderLayout.NORTH);
        if (this.station.hasBeenSaved()) {
            ResultSet results = database.Query("SELECT avg(liter)" +
                    ",(SUM(cost)/SUM(liter))" +
                    ",SUM(liter)" +
                    " FROM fuelrecords WHERE stationId = " + this.station.getId()+
                    "AND date >= '" + parent.getStartDate()+"'"+
                    "AND date <= '" + parent.getEndDate()+"'", true);
            if (results.next()) {
                JPanel statsContainer = new JPanel(new GridLayout(0, 1));
                statsContainer.setBorder(BorderFactory.createEtchedBorder());

                DecimalFormat df = new DecimalFormat("###.##");
                statsContainer.add(new JLabel("Gemiddeld getankt: " + df.format(results.getDouble("1")) + " liter"));
                statsContainer.add(new JLabel("Gemiddelde kosten: " + df.format(results.getDouble("2")) + " euro/liter"));
                statsContainer.add(new JLabel("Totaal getankt: " + df.format(results.getDouble("3")) + " liter"));
                ResultSet results2 = database.Query("SELECT cost/liter FROM fuelrecords WHERE stationId = " + this.station.getId() + " ORDER BY date DESC", true);
                if (results2.next()) {
                    statsContainer.add(new JLabel("Laatste prijs: " + df.format(results2.getDouble("1")) + " euro/liter"));
                    add(statsContainer, BorderLayout.SOUTH);
                }

            }
        }
        revalidate();
        repaint();
    }

    private class Controller implements ActionListener, KeyListener {

        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equals("SAVE")) {
                if (station.isValid()) {
                    Station testStation = new Station();
                    testStation.setLocation(station.getLocation());
                    testStation.setName(station.getName());
                    try {
                        if (!database.getStations().contains(testStation)) {
                            ResultSet result = database.Query("SELECT * FROM settings", true);
                            result.next();
                            boolean confirm = result.getInt("askwhensaving") == 1;
                            if (!confirm || JOptionPane.showConfirmDialog(null, "Weet u zeker dat u dit tankstation wilt opslaan?", "Bevestiging", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                                station.toDatabase(database);
                                view.refreshTankRecords();
                            }
                        } else {
                            JOptionPane.showMessageDialog(null, "Dit tankstation bestaat al", "Fout bij opslaan", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(null, ex.getMessage(), "Fout bij versturen", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Dit tankstation is niet geldig, vul alle velden in", "Fout", JOptionPane.ERROR_MESSAGE);
                }
            } else if (e.getActionCommand().equals("DISCARD")) {
                if (JOptionPane.showConfirmDialog(null, "Weet u zeker dat u niet op wilt slaan?", "Bevestiging", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    try {
                        view.refreshTankRecords();
                    } catch (SQLException ex) {
                    }
                }
            } else if (e.getActionCommand().equals("DELETE")) {
                if (JOptionPane.showConfirmDialog(null, "Weet u zeker dat u wilt verwijderen?\nAlle tankbeurten die bij dit tankstation zijn gedaan zullen ook worden verwijderd.", "Bevestiging", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    try {
                        station.delete(database);
                        view.refreshTankRecords();
                    //JOptionPane.showMessageDialog(null, "Tankstation verwijderd", "Succes", JOptionPane.INFORMATION_MESSAGE);
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(null, ex.getMessage(), "Fout bij verwijderen", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }

        public void keyTyped(KeyEvent e) {
        }

        public void keyPressed(KeyEvent e) {
        }

        public void keyReleased(KeyEvent e) {
            JComponent source = (JComponent) e.getSource();
            if (source == locationField) {
                try {
                    String newLocation = locationField.getText();
                    if (newLocation.length() > 0) {
                        station.setLocation(newLocation);
                        locationField.setBackground(Color.GREEN);
                    } else {
                        throw new Exception();
                    }
                } catch (Exception ex) {
                    locationField.setBackground(Color.RED);
                }
            } else if (source == nameField) {
                try {
                    String newName = nameField.getText();
                    if (newName.length() > 0) {
                        station.setName(newName);
                        nameField.setBackground(Color.GREEN);
                    } else {
                        throw new Exception();
                    }
                } catch (Exception ex) {
                    nameField.setBackground(Color.RED);
                }
            }
            saveButton.setEnabled(station.isChanged() || !station.hasBeenSaved());
            cancelButton.setEnabled(station.isChanged() || !station.hasBeenSaved());
        }
    }
}
