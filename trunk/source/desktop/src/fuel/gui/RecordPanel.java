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
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import main.DatepickerPopup;
import sub.DateSelectionEvent;
import sub.DateSelectionListener;

/**
 *
 * @author Mark
 */
public class RecordPanel extends JPanel {

    private TankRecord record;
    private DatepickerPopup dateField;
    private JComboBox motorChoice;
    private JComboBox stationChoice;
    private JTextField liter;
    private JTextField cost;
    private JTextField distance;
    private JTextField totalDistance;
    private JMenu distanceMenu;
    private JMenu totalDistanceMenu;
    private JTextField typeOfGas;
    private JTextField comment;
    private Controller controller;
    private boolean changed;
    private Database database;
    private View parent;
    private JButton saveButton;
    private JButton cancelButton;
    private JMenuItem calculateDistanceButton;
    private JMenuItem calculateTotalDistanceButton;

    public RecordPanel(TankRecord record, Database database, View parent) throws SQLException, IOException {
        this.record = record;
        this.database = database;
        this.changed = false;
        this.parent = parent;
        controller = new Controller();
        setLayout(new BorderLayout());
        JPanel container = new JPanel(new BorderLayout());
        double size[][] = {{0.25, TableLayout.FILL},
            {TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL}};

        JPanel inputs = new JPanel(new TableLayout(size));
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File("lib/tinyCalendar.gif"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        dateField = new DatepickerPopup();
        dateField.setOpenCalendarButton(img);
        dateField.setSelectionCommand("DATE");
        dateField.getLook().setShowWeeksBar(true);
        dateField.setDisplayEditable(false);
        dateField.setSelectedDate(record.getDate());
        dateField.addDateSelectionListener(controller);
        dateField.setBackground(Color.GREEN);

        inputs.add(new JLabel("Datum van tankbeurt: "), "0,0");
        inputs.add(dateField, "1,0");

        motorChoice = new JComboBox(database.getMotorcycles().toArray());
        motorChoice.addActionListener(controller);
        motorChoice.setSelectedItem(record.getMotorcycle());
        //motorChoice.setSelectedIndex(0);
        if (!record.hasBeenSaved()) {
            motorChoice.setBackground(Color.RED);
        } else {
            motorChoice.setBackground(Color.GREEN);
        }
        inputs.add(new JLabel("Gereden voertuig: "), "0,1");
        inputs.add(motorChoice, "1,1");
        //inputs.add(motorPanel);

        stationChoice = new JComboBox(database.getStations().toArray());
        stationChoice.addActionListener(controller);
        stationChoice.setSelectedItem(record.getStation());
        if (!record.hasBeenSaved()) {
            stationChoice.setBackground(Color.RED);
        } else {
            stationChoice.setBackground(Color.GREEN);
        }
        inputs.add(new JLabel("Tankstation: "), "0,2");
        inputs.add(stationChoice, "1,2");
        //inputs.add(stationPanel);

        liter = new JTextField("" + record.getLiters(), 10);
        liter.addKeyListener(controller);
        if (!record.hasBeenSaved()) {
            liter.setBackground(Color.RED);
        } else {
            liter.setBackground(Color.GREEN);
        }
        inputs.add(new JLabel("Getankte hoeveelheid: "), "0,3");
        inputs.add(liter, "1,3");
        //literPanel.add(new JLabel("liter"));
        //inputs.add(literPanel);

        cost = new JTextField("" + record.getCost(), 10);
        cost.addKeyListener(controller);
        if (!record.hasBeenSaved()) {
            cost.setBackground(Color.RED);
        } else {
            cost.setBackground(Color.GREEN);
        }
        inputs.add(new JLabel("Gemaakte kosten: "), "0,4");
        inputs.add(cost, "1,4");
        //costPanel.add(new JLabel("euro"));
        //inputs.add(costPanel);

        distance = new JTextField("" + record.getDistanceTraveled(), 10);
        distance.addKeyListener(controller);
        if (!record.hasBeenSaved()) {
            distance.setBackground(Color.RED);
        } else {
            distance.setBackground(Color.GREEN);
        }
        distanceMenu = new JMenu("Opties >");
        calculateDistanceButton = new JMenuItem("Bereken gereden afstand met de kilometerstand");
        calculateDistanceButton.setActionCommand("CALCDISTANCE");
        calculateDistanceButton.setEnabled(record.hasBeenSaved() && record.isChanged());
        calculateDistanceButton.addActionListener(controller);
        distanceMenu.add(calculateDistanceButton);

        JMenuItem convertDistanceButton = new JMenuItem("Reken om van mijlen naar kilometers");
        convertDistanceButton.setActionCommand("CONVERTDISTANCE");
        convertDistanceButton.addActionListener(controller);
        distanceMenu.add(convertDistanceButton);
        JMenuBar distanceBar = new JMenuBar();
        distanceBar.add(distanceMenu);

        JPanel cdp = new JPanel(new BorderLayout());
        cdp.add(distance, BorderLayout.CENTER);
        //cdp.add(calculateDistanceButton);
        
        cdp.add(distanceBar, BorderLayout.EAST);
        inputs.add(new JLabel("Gereden afstand: "), "0,5");
        inputs.add(cdp, "1,5");

        totalDistance = new JTextField("" + record.getTotalDistance(), 10);
        totalDistance.addKeyListener(controller);
        totalDistance.setBackground(Color.GREEN);

        totalDistanceMenu = new JMenu("Opties >");
        calculateTotalDistanceButton = new JMenuItem("Bereken kilometerstand met de gereden afstand");
        calculateTotalDistanceButton.setActionCommand("CALCTOTALDISTANCE");
        calculateTotalDistanceButton.setEnabled(record.hasBeenSaved() && record.isChanged());
        calculateTotalDistanceButton.addActionListener(controller);
        totalDistanceMenu.add(calculateTotalDistanceButton);

        JMenuItem convertTotalDistanceButton = new JMenuItem("Reken om van mijlen naar kilometers");
        convertTotalDistanceButton.setActionCommand("CONVERTTOTALDISTANCE");
        convertTotalDistanceButton.addActionListener(controller);
        totalDistanceMenu.add(convertTotalDistanceButton);
        JMenuBar totalDistanceBar = new JMenuBar();
        totalDistanceBar.add(totalDistanceMenu);

        JPanel ctdp = new JPanel(new BorderLayout());
        ctdp.add(totalDistance, BorderLayout.CENTER);
        JPanel buttons = new JPanel(new BorderLayout());
        ctdp.add(totalDistanceBar, BorderLayout.EAST);
        inputs.add(new JLabel("Kilometerstand: "), "0,6");
        inputs.add(ctdp, "1,6");
        //distancePanel.add(new JLabel("km"));
        //inputs.add(distancePanel);

        typeOfGas = new JTextField(record.getTypeOfGas(), 10);
        typeOfGas.addKeyListener(controller);
        typeOfGas.setBackground(Color.GREEN);
        inputs.add(new JLabel("Type brandstof: "), "0,7");
        inputs.add(typeOfGas, "1,7");

        comment = new JTextField(record.getComment(), 10);
        comment.addKeyListener(controller);
        comment.setBackground(Color.GREEN);
        inputs.add(new JLabel("Commentaar: "), "0,8");
        inputs.add(comment, "1,8");
        //inputs.add(commentPanel);

        saveButton = new JButton("Tankbeurt opslaan");
        saveButton.setActionCommand("SAVE");
        saveButton.addActionListener(controller);
        saveButton.setEnabled(record.isChanged() || !record.hasBeenSaved());
        cancelButton = new JButton("Verandering niet opslaan");
        cancelButton.setActionCommand("DISCARD");
        cancelButton.addActionListener(controller);
        cancelButton.setEnabled(record.isChanged() || !record.hasBeenSaved());
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        if (record.hasBeenSaved()) {
            JButton deleteButton = new JButton("Verwijder tankbeurt");
            deleteButton.addActionListener(controller);
            deleteButton.setActionCommand("DELETE");
            buttonPanel.add(deleteButton);
        }
        container.add(inputs, BorderLayout.CENTER);
        container.add(buttonPanel, BorderLayout.SOUTH);
        container.setBorder(BorderFactory.createEtchedBorder());
        add(container, BorderLayout.WEST);
        if (record.hasBeenSaved()) {
            ResultSet results = database.Query("SELECT cost/liter,distance/liter,cost/distance FROM fuelrecords WHERE Id = " + record.getId(), true);
            JPanel statsContainer = new JPanel(new GridLayout(0, 1));
            statsContainer.setBorder(BorderFactory.createEtchedBorder());
            results.next();
            DecimalFormat df = new DecimalFormat("###.##");
            statsContainer.add(new JLabel("Kosten per liter: " + df.format(results.getDouble("1")) + " euro/liter"));
            statsContainer.add(new JLabel("Verbruik: " + df.format(results.getDouble("2")) + " km/liter"));
            statsContainer.add(new JLabel("Kosten per km: " + df.format(results.getDouble("3")) + " euro"));
            add(statsContainer, BorderLayout.SOUTH);
        }

        setVisible(true);
        revalidate();
        repaint();
    }

    public TankRecord getRecord() {
        return record;
    }

    public void setCalcButtons(){
        if (calculateTotalDistanceButton != null){
            calculateTotalDistanceButton.setEnabled(record.getDistanceTraveled()>0.0 && record.getMotorcycle()!=null);
            calculateDistanceButton.setEnabled(record.getTotalDistance()>0.0 && record.getMotorcycle()!=null);
        }
    }

    private class Controller implements ActionListener, KeyListener, DateSelectionListener {

        private final double KILOMETERSPERMILE = 1.609344;

        public void actionPerformed(ActionEvent e) {
            JComponent source = (JComponent) e.getSource();
            if (source == motorChoice) {
                record.setMotorcycle((Motorcycle) motorChoice.getSelectedItem());
                if (motorChoice.getSelectedItem() != null) {
                    motorChoice.setBackground(Color.GREEN);
                }
            } else if (source == stationChoice) {
                record.setStation((Station) stationChoice.getSelectedItem());
                if (stationChoice.getSelectedItem() != null) {
                    stationChoice.setBackground(Color.GREEN);
                }
            } else if (e.getActionCommand().equals("SAVE")) {
                if (record.isValid()) {
                    try {
                        ResultSet result = database.Query("SELECT * FROM settings", true);
                        result.next();
                        boolean confirm = result.getInt("askwhensaving") == 1;
                        if (!confirm || JOptionPane.showConfirmDialog(null, "Weet u zeker dat u deze tankbeurt wilt opslaan?", "Bevestiging", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                            record.toDatabase(database);
                            parent.refreshTankRecords();
                        }
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(null, ex.getMessage(), "Fout bij versturen", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Deze tankbeurt is niet geldig, vul alle velden in", "Fout", JOptionPane.ERROR_MESSAGE);
                }
            } else if (e.getActionCommand().equals("DISCARD")) {
                if (JOptionPane.showConfirmDialog(null, "Weet u zeker dat u niet op wilt slaan?", "Bevestiging", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    try {
                        parent.refreshTankRecords();
                    } catch (SQLException ex) {
                    }
                }
            } else if (e.getActionCommand().equals("DELETE")) {
                if (JOptionPane.showConfirmDialog(null, "Weet u zeker dat u wilt verwijderen?", "Bevestiging", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    try {
                        record.delete(database);
                        parent.refreshTankRecords();
                    //JOptionPane.showMessageDialog(null, "Tankbeurt verwijderd", "Succes", JOptionPane.INFORMATION_MESSAGE);
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(null, ex.getMessage(), "Fout bij verwijderen", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else if (e.getActionCommand().equals("CALCDISTANCE")) {
                try {
                    ResultSet result = database.Query("SELECT totaldistance FROM fuelrecords WHERE date <= '" + record.getDate() + "' AND motorcycleid = " + record.getMotorcycle().getId() + " AND NOT id = " + record.getId() + "ORDER BY date desc,totaldistance desc", true);
                    Double newDistance = 0.0;
                    if (result.next()) {
                        newDistance = record.getTotalDistance() - result.getDouble("totaldistance");
                    } else {
                        newDistance = record.getTotalDistance();
                    }
                    record.setDistanceTraveled(newDistance);
                    distance.setText(newDistance + "");
                    distance.setBackground(Color.GREEN);
                } catch (SQLException ex) {
                    Logger.getLogger(RecordPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (e.getActionCommand().equals("CONVERTDISTANCE")) {
                Double newDistance = record.getDistanceTraveled() * KILOMETERSPERMILE;
                DecimalFormat fmt = new DecimalFormat();
                fmt.setGroupingUsed(false);
                fmt.setMinimumFractionDigits(0);
                fmt.setMaximumFractionDigits(2);
                newDistance = Double.valueOf(fmt.format(newDistance).replaceAll(",", "."));
                record.setDistanceTraveled(newDistance);
                distance.setText(newDistance + "");
                distance.setBackground(Color.GREEN);
            } else if (e.getActionCommand().equals("CALCTOTALDISTANCE")) {
                try {
                    ResultSet result = database.Query("SELECT totaldistance FROM fuelrecords WHERE date <= '" + record.getDate() + "' AND motorcycleid = " + record.getMotorcycle().getId() + " AND NOT id = " + record.getId() + " ORDER BY date desc,totaldistance desc", true);
                    Double newDistance = 0.0;
                    if (result.next()) {
                        newDistance = result.getDouble("totaldistance") + record.getDistanceTraveled();
                    } else {
                        newDistance = record.getDistanceTraveled();
                    }
                    record.setTotalDistance(newDistance);
                    totalDistance.setText(newDistance + "");
                    totalDistance.setBackground(Color.GREEN);
                } catch (SQLException ex) {
                    Logger.getLogger(RecordPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (e.getActionCommand().equals("CONVERTTOTALDISTANCE")) {
                double newDistance = record.getTotalDistance() * KILOMETERSPERMILE;
                DecimalFormat fmt = new DecimalFormat();
                fmt.setGroupingUsed(false);
                fmt.setMinimumFractionDigits(0);
                fmt.setMaximumFractionDigits(2);
                newDistance = Double.valueOf(fmt.format(newDistance).replaceAll(",", "."));
                record.setTotalDistance(newDistance);
                totalDistance.setText(newDistance + "");
                totalDistance.setBackground(Color.GREEN);
            }
            if (saveButton != null) {
                saveButton.setEnabled(record.isChanged() || !record.hasBeenSaved());
            }
            if (cancelButton != null) {
                cancelButton.setEnabled(record.isChanged() || !record.hasBeenSaved());
            }
            setCalcButtons();
        }

        public void keyTyped(KeyEvent e) {
        }

        public void keyPressed(KeyEvent e) {
        }

        public void keyReleased(KeyEvent e) {
            JComponent source = (JComponent) e.getSource();
            if (source == liter) {
                try {
                    double newLiters = Double.parseDouble(liter.getText());
                    record.setLiters(newLiters);
                    liter.setBackground(Color.GREEN);
                    if (newLiters <= 0.0) {
                        throw new Exception();
                    }
                } catch (Exception ex) {
                    liter.setBackground(Color.RED);
                }
            } else if (source == cost) {
                try {
                    double newCost = Double.parseDouble(cost.getText());
                    record.setCost(newCost);
                    cost.setBackground(Color.GREEN);
                    if (newCost <= 0.0) {
                        throw new Exception();
                    }
                } catch (Exception ex) {
                    cost.setBackground(Color.RED);
                }
            } else if (source == distance) {
                try {
                    double newDistance = Double.parseDouble(distance.getText());
                    record.setDistanceTraveled(newDistance);
                    distance.setBackground(Color.GREEN);
                    if (newDistance <= 0.0) {
                        throw new Exception();
                    }
                } catch (Exception ex) {
                    distance.setBackground(Color.RED);
                }
            } else if (source == totalDistance) {
                try {
                    double newDistance = Double.parseDouble(totalDistance.getText());
                    record.setTotalDistance(newDistance);
                    totalDistance.setBackground(Color.GREEN);
                    if (newDistance < 0.0) {
                        throw new Exception();
                    }
                } catch (Exception ex) {
                    totalDistance.setBackground(Color.RED);
                }
            } else if (source == typeOfGas) {
                record.setTypeOfGas(typeOfGas.getText());
            } else if (source == comment) {
                record.setComment(comment.getText());
            }
            saveButton.setEnabled(record.isChanged() || !record.hasBeenSaved());
            cancelButton.setEnabled(record.isChanged() || !record.hasBeenSaved());
            setCalcButtons();
        }

        public void dateSelected(DateSelectionEvent arg0) {
            if (arg0.getSelectionCommand().equals("DATE")) {
                try {
                    record.setDate(new Date(dateField.getSelectedDate().getTime()));
                    dateField.setBackground(Color.GREEN);
                } catch (Exception ex) {
                    dateField.setBackground(Color.RED);
                }
            }
            saveButton.setEnabled(record.isChanged() || !record.hasBeenSaved());
            cancelButton.setEnabled(record.isChanged() || !record.hasBeenSaved());
        }
    }
}
