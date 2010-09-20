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

import fuel.gui.stats.GraphFrame;
import fuel.gui.stats.GraphFactory;
import fuel.lib.Database;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author Mark
 */
public class StatisticsSelectionPanel extends JPanel {

    private Controller controller;
    private Database database;
    private JCheckBox distanceTraveledMotor;
    private JCheckBox distanceTraveledCumulative;
    private JCheckBox fuelUsageMonth;
    private JCheckBox fuelTypeMotor;
    private JCheckBox expensesIncludeFuel;
    private JCheckBox stationUsageMotor;
    private JCheckBox fuelPriceStation;
    private JComboBox costDateType;
    private DateSelectionPanel dateSelectionPanel;
    

    public StatisticsSelectionPanel(Database database) {
        controller = new Controller();
        this.database = database;
        setLayout(new BorderLayout());
        JPanel topPanel = new JPanel(new BorderLayout());
        dateSelectionPanel = new DateSelectionPanel(database,false);
        topPanel.add(dateSelectionPanel,BorderLayout.NORTH);
        topPanel.add(new JLabel("Kies een grafiek:"), BorderLayout.CENTER);
        add(dateSelectionPanel,BorderLayout.NORTH);
        JPanel graphSelectors = new JPanel(new GridLayout(4,0));

        JPanel distanceTraveledPanel = new JPanel();
        distanceTraveledPanel.setLayout(new BoxLayout(distanceTraveledPanel, BoxLayout.Y_AXIS));
        distanceTraveledPanel.setBorder(BorderFactory.createTitledBorder("Gereden afstand"));
        distanceTraveledMotor = new JCheckBox("Deel op per voertuig");
        distanceTraveledPanel.add(distanceTraveledMotor);
        distanceTraveledCumulative = new JCheckBox("Cumulatief");
        distanceTraveledPanel.add(distanceTraveledCumulative);
        JButton distanceTraveledButton = new JButton("Open grafiek");
        distanceTraveledButton.addActionListener(controller);
        distanceTraveledButton.setActionCommand("DISTANCETRAVELED");
        distanceTraveledPanel.add(distanceTraveledButton);
        graphSelectors.add(distanceTraveledPanel);

        JPanel fuelUsagePanel = new JPanel();
        fuelUsagePanel.setLayout(new BoxLayout(fuelUsagePanel, BoxLayout.Y_AXIS));
        fuelUsagePanel.setBorder(BorderFactory.createTitledBorder("Brandstofverbruik"));
        fuelUsageMonth = new JCheckBox("Deel op per maand");
        fuelUsagePanel.add(fuelUsageMonth);
        JButton fuelUsageButton = new JButton("Open grafiek");
        fuelUsageButton.addActionListener(controller);
        fuelUsageButton.setActionCommand("FUELUSAGE");
        fuelUsagePanel.add(fuelUsageButton);
        graphSelectors.add(fuelUsagePanel);

        JPanel motorUsagePanel = new JPanel();
        motorUsagePanel.setLayout(new BoxLayout(motorUsagePanel, BoxLayout.Y_AXIS));
        motorUsagePanel.setBorder(BorderFactory.createTitledBorder("Motorgebruik"));
        JButton motorUsageButton = new JButton("Open grafiek");
        motorUsageButton.addActionListener(controller);
        motorUsageButton.setActionCommand("MOTORUSAGE");
        motorUsagePanel.add(motorUsageButton);
        graphSelectors.add(motorUsagePanel);

        JPanel stationUsagePanel = new JPanel();
        stationUsagePanel.setLayout(new BoxLayout(stationUsagePanel, BoxLayout.Y_AXIS));
        stationUsagePanel.setBorder(BorderFactory.createTitledBorder("Tankstationgebruik"));
        stationUsageMotor = new JCheckBox("Deel op per motor");
        stationUsagePanel.add(stationUsageMotor);
        JButton stationUsageButton = new JButton("Open grafiek");
        stationUsageButton.addActionListener(controller);
        stationUsageButton.setActionCommand("STATIONUSAGE");
        stationUsagePanel.add(stationUsageButton);
        graphSelectors.add(stationUsagePanel,"wrap");

        JPanel fuelPricePanel = new JPanel();
        fuelPricePanel.setLayout(new BoxLayout(fuelPricePanel, BoxLayout.Y_AXIS));
        fuelPricePanel.setBorder(BorderFactory.createTitledBorder("Brandstofprijs"));
        fuelPriceStation = new JCheckBox("Deel op per tankstation");
        fuelPricePanel.add(fuelPriceStation);
        JButton fuelPriceButton = new JButton("Open grafiek");
        fuelPriceButton.addActionListener(controller);
        fuelPriceButton.setActionCommand("FUELPRICE");
        fuelPricePanel.add(fuelPriceButton);
        graphSelectors.add(fuelPricePanel);

        JPanel fuelTypePanel = new JPanel();
        fuelTypePanel.setLayout(new BoxLayout(fuelTypePanel, BoxLayout.Y_AXIS));
        fuelTypePanel.setBorder(BorderFactory.createTitledBorder("Brandstof verhouding"));
        fuelTypeMotor = new JCheckBox("Deel op per motor");
        fuelTypePanel.add(fuelTypeMotor);
        JButton fuelTypeButton = new JButton("Open grafiek");
        fuelTypeButton.addActionListener(controller);
        fuelTypeButton.setActionCommand("FUELTYPES");
        fuelTypePanel.add(fuelTypeButton);
        graphSelectors.add(fuelTypePanel);
        
        JPanel expensesPanel = new JPanel();
        expensesPanel.setLayout(new BoxLayout(expensesPanel, BoxLayout.Y_AXIS));
        expensesPanel.setBorder(BorderFactory.createTitledBorder("Uitgaven"));
        expensesIncludeFuel = new JCheckBox("Inclusief brandstof");
        expensesPanel.add(expensesIncludeFuel);
        JButton expensesButton = new JButton("Open grafiek");
        expensesButton.addActionListener(controller);
        expensesButton.setActionCommand("EXPENSES");
        expensesPanel.add(expensesButton);
        graphSelectors.add(expensesPanel);

        JPanel costPanel = new JPanel();
        costPanel.setLayout(new BoxLayout(costPanel, BoxLayout.Y_AXIS));
        costPanel.setBorder(BorderFactory.createTitledBorder("Kosten"));
        costDateType = new JComboBox(new String[]{"Dag","Maand","Jaar"});
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.add(new JLabel("Laat zien per: "));
        p.add(costDateType);
        costPanel.add(p);
        JButton costButton = new JButton("Open grafiek");
        costButton.addActionListener(controller);
        costButton.setActionCommand("COST");
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(costButton);
        costPanel.add(buttonPanel);
        graphSelectors.add(costPanel);
        JScrollPane scroll = new JScrollPane(graphSelectors);
        scroll.getVerticalScrollBar().setUnitIncrement(10);
        scroll.getHorizontalScrollBar().setUnitIncrement(10);
        add(scroll);
    }

    private class Controller implements ActionListener {        

        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equals("DISTANCETRAVELED")) {
                try {
                    new GraphFrame(GraphFactory.createDistanceTraveled(database,distanceTraveledMotor.isSelected(),distanceTraveledCumulative.isSelected(),dateSelectionPanel.getStartDate(),dateSelectionPanel.getEndDate()), "Afgelegde afstand");
                } catch (SQLException ex) {
                    Logger.getLogger(StatisticsSelectionPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (e.getActionCommand().equals("FUELUSAGE")) {
                try {
                    new GraphFrame(GraphFactory.createFuelUsage(fuelUsageMonth.isSelected(),database,dateSelectionPanel.getStartDate(),dateSelectionPanel.getEndDate()), "Brandstofverbruik");
                } catch (SQLException ex) {
                    Logger.getLogger(StatisticsSelectionPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (e.getActionCommand().equals("MOTORUSAGE")) {
                new GraphFrame(GraphFactory.createMotorUsage(database,dateSelectionPanel.getStartDate(),dateSelectionPanel.getEndDate()), "Motorgebruik");
            } else if (e.getActionCommand().equals("STATIONUSAGE")) {
                new GraphFrame(GraphFactory.createStationUsage(database,stationUsageMotor.isSelected(),dateSelectionPanel.getStartDate(),dateSelectionPanel.getEndDate()), "Tankstationgebruik");
            } else if (e.getActionCommand().equals("FUELPRICE")) {
                new GraphFrame(GraphFactory.createFuelPrice(database,fuelPriceStation.isSelected(),dateSelectionPanel.getStartDate(),dateSelectionPanel.getEndDate()), "Brandstofprijs");
            } else if (e.getActionCommand().equals("FUELTYPES")) {
                new GraphFrame(GraphFactory.createFuelTypes(fuelTypeMotor.isSelected(),database,dateSelectionPanel.getStartDate(),dateSelectionPanel.getEndDate()), "Brandstof verhouding");
            } else if (e.getActionCommand().equals("EXPENSES")) {
                new GraphFrame(GraphFactory.createExpenses(database,expensesIncludeFuel.isSelected(),dateSelectionPanel.getStartDate(),dateSelectionPanel.getEndDate()), "Uitgaven");
            } else if (e.getActionCommand().equals("COST")) {
                new GraphFrame(GraphFactory.createCost(database,costDateType.getSelectedIndex(),dateSelectionPanel.getStartDate(),dateSelectionPanel.getEndDate()), "Kosten");
            }

        }
    }
}
