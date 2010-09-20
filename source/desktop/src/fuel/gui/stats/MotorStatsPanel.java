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

package fuel.gui.stats;

import fuel.lib.Database;
import fuel.lib.Motorcycle;
import fuel.lib.Station;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer3D;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

/**
 *
 * @author Mark
 */
public class MotorStatsPanel extends JPanel {

    private Database database;
    private Controller controller;
    private JPanel motorSpecsPanel;
    private JPanel graphContainer;

    public MotorStatsPanel(Database database) throws SQLException {
        this.database = database;
        controller = new Controller();
        JPanel container = new JPanel(new BorderLayout());
        //container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        JPanel motorSelectionPanel = new JPanel();
        motorSelectionPanel.setLayout(new BoxLayout(motorSelectionPanel, BoxLayout.X_AXIS));
        motorSpecsPanel = new JPanel(new GridLayout(2, 3));
        motorSpecsPanel.setBorder(BorderFactory.createTitledBorder("Specs"));
        graphContainer = new JPanel();
        graphContainer.setLayout(new BoxLayout(graphContainer, BoxLayout.Y_AXIS));
        JComboBox motorSelector = new JComboBox(database.getMotorcycles().toArray());
        motorSelector.setActionCommand("SELECTMOTOR");
        motorSelector.addActionListener(controller);
        motorSelectionPanel.add(motorSelector);


        //motorSelector.setSelectedIndex(0);

        motorSelectionPanel.add(motorSpecsPanel);
        refreshMotorSpecs((Motorcycle) motorSelector.getSelectedItem());

        container.add(motorSelectionPanel, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(graphContainer);
        scroll.getHorizontalScrollBar().setUnitIncrement(10);
        scroll.getVerticalScrollBar().setUnitIncrement(10);
        container.add(scroll, BorderLayout.CENTER);


        refreshGraphs((Motorcycle) motorSelector.getSelectedItem());
        setLayout(new BorderLayout());
        add(container);

        setVisible(true);
    }

    private void refreshMotorSpecs(Motorcycle motor) {
        motorSpecsPanel.removeAll();
        if (motor != null) {
            if (motor.getBrand() != null) {
                motorSpecsPanel.add(new JLabel("Merk: " + motor.getBrand()));
            }
            if (motor.getType() != null) {
                motorSpecsPanel.add(new JLabel("Type: " + motor.getType()));
            }
            if (motor.getCc() != 0) {
                motorSpecsPanel.add(new JLabel("Cilinderinhoud: " + motor.getCc()));
            }
            if (motor.getWeight() != 0) {
                motorSpecsPanel.add(new JLabel("Gewicht: " + motor.getWeight()));
            }
            if (motor.getCilinders() != 0) {
                motorSpecsPanel.add(new JLabel("Aantal cilinders: " + motor.getCilinders()));
            }
            if (motor.getTankSize() != 0.0) {
                motorSpecsPanel.add(new JLabel("Tank grootte: " + motor.getTankSize()));
            }
        }
        revalidate();
        repaint();
    }

    private void refreshGraphs(Motorcycle motor) {
        graphContainer.removeAll();
        if (motor != null) {
            DefaultPieDataset usageDataset = new DefaultPieDataset();
            try {
                ResultSet thisMotor = database.Query("SELECT SUM(distance) FROM fuelrecords WHERE motorcycleId = " + motor.getId(), true);
                ResultSet otherMotors = database.Query("SELECT SUM(distance) FROM fuelrecords WHERE NOT motorcycleId = " + motor.getId(), true);
                thisMotor.next();
                otherMotors.next();
                
                usageDataset.setValue(motor.toString(), thisMotor.getInt("1"));
                usageDataset.setValue("Andere motoren", otherMotors.getInt("1"));

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage() + ex.getCause());
            }
            JFreeChart usagePiechart = ChartFactory.createPieChart3D("", usageDataset, true, true, false);
            PiePlot3D plot3 = (PiePlot3D) usagePiechart.getPlot();
            plot3.setForegroundAlpha(0.6f);
            //plot3.setCircular(true);

            JPanel usagePiechartPanel = new ChartPanel(usagePiechart);
            usagePiechartPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createTitledBorder("Motorgebruik")));
            usagePiechartPanel.setPreferredSize(new java.awt.Dimension(240, 240));
            usagePiechartPanel.setLayout(new BorderLayout());

            DefaultPieDataset stationDataset = new DefaultPieDataset();
            try {
                for (Station station : database.getStations()) {
                    ResultSet numberStations = database.Query("SELECT DISTINCT stationId FROM fuelrecords WHERE stationId = " + station.getId(), true);
                    if (numberStations.next()) {
                        ResultSet otherMotors = database.Query("SELECT COUNT(*) FROM fuelrecords WHERE stationId = " + station.getId() + " AND motorcycleId = " + motor.getId(), true);
                        otherMotors.next();
                        if (otherMotors.getInt("1") > 0){
                            stationDataset.setValue(station.toString(), otherMotors.getInt("1"));
                        }
                    }
                }

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage() + ex.getCause());
            }
            JFreeChart stationPiechart = ChartFactory.createPieChart3D("", stationDataset, true, true, false);
            PiePlot3D plot2 = (PiePlot3D) stationPiechart.getPlot();
            plot2.setForegroundAlpha(0.6f);
            //plot3.setCircular(true);

            JPanel stationPiechartPanel = new ChartPanel(stationPiechart);
            stationPiechartPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createTitledBorder("Tankstation verhouding")));
            stationPiechartPanel.setPreferredSize(new java.awt.Dimension(240, 240));
            stationPiechartPanel.setLayout(new BorderLayout());

            DefaultPieDataset fuelDataset = new DefaultPieDataset();
            try {
                ResultSet numberResults = database.Query("SELECT DISTINCT typeOfGas FROM fuelrecords", true);
                while (numberResults.next()) {
                    ResultSet thisStation = database.Query("SELECT SUM(liter) FROM fuelrecords WHERE typeOfGas = '" + numberResults.getString("typeOfGas") + "'AND motorcycleId = " + motor.getId(), true);
                    thisStation.next();
                    if (thisStation.getDouble("1") > 0){
                        fuelDataset.setValue(numberResults.getString("TYPEOFGAS"), thisStation.getDouble("1"));
                    }
                }

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage() + ex.getCause());
            }
            JFreeChart fuelPieChart = ChartFactory.createPieChart3D("", fuelDataset, true, true, false);
            PiePlot3D plot1 = (PiePlot3D) fuelPieChart.getPlot();
            plot1.setForegroundAlpha(0.6f);
            //plot3.setCircular(true);

            JPanel fuelPieChartPanel = new ChartPanel(fuelPieChart);
            fuelPieChartPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createTitledBorder("Brandstof verhouding")));
            fuelPieChartPanel.setPreferredSize(new java.awt.Dimension(240, 240));


            DefaultCategoryDataset barDataset = new DefaultCategoryDataset();
            try {
                ResultSet motorThing = database.Query("SELECT distance/liter,date FROM fuelrecords WHERE motorcycleId = " + motor.getId() + " ORDER BY date ASC", true);
                while (motorThing.next()) {
                    barDataset.addValue(motorThing.getDouble("1"), motorThing.getString("DATE"), "Verbruik");
                }

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage() + ex.getCause());
            }

            JFreeChart barChart = ChartFactory.createBarChart3D(
                    "", // chart title
                    "", // domain axis label
                    "Aantal", // range axis label
                    barDataset, // data
                    PlotOrientation.VERTICAL,
                    false, // include legend
                    true, // tooltips?
                    false // URLs?
                    );
            CategoryPlot plot = barChart.getCategoryPlot();
            BarRenderer3D renderer = (BarRenderer3D) plot.getRenderer();
            renderer.setDrawBarOutline(false);

            ChartPanel barChartPanel = new ChartPanel(barChart);
            barChartPanel.getChartRenderingInfo().setEntityCollection(null);
            barChartPanel.setBorder(BorderFactory.createTitledBorder("Verbruik"));
            barChartPanel.setPreferredSize(new java.awt.Dimension(320, 240));
            barChartPanel.setLayout(new BorderLayout());


            JPanel piePanel = new JPanel(new GridLayout(0, 3));
            piePanel.add(usagePiechartPanel);
            piePanel.add(stationPiechartPanel);
            piePanel.add(fuelPieChartPanel);
            
            //uitgaven
            DefaultPieDataset expensesDataset = new DefaultPieDataset();
            try {
                Map<String,ResultSet> allCosts = new HashMap<String,ResultSet>();
                ResultSet fuelCosts = database.Query("SELECT SUM(cost) FROM fuelrecords WHERE motorcycleId = " + motor.getId(), true);
                allCosts.put("Brandstof",fuelCosts);
                ResultSet expenses = database.Query("SELECT DISTINCT categoryid FROM expenses", true);
                while (expenses.next()){
                    ResultSet set = database.Query("SELECT SUM(costs) FROM expenses WHERE categoryid = " + expenses.getInt("categoryid")+" AND motorcycleid = " + motor.getId(), true);
                    ResultSet set2 = database.Query("SELECT name FROM categories WHERE id = " + expenses.getInt("categoryid"), true);
                    set2.next();
                    allCosts.put(set2.getString("name"),set);
                }

                for(Map.Entry<String, ResultSet> element : allCosts.entrySet()){
                    element.getValue().next();
                    if (element.getValue().getInt("1") > 0){
                        expensesDataset.setValue(element.getKey(), element.getValue().getInt("1"));
                    }
                }

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage() + ex.getCause());
            }
            JFreeChart expensesPiechart = ChartFactory.createPieChart3D("", expensesDataset, true, true, false);
            PiePlot3D plot4 = (PiePlot3D) expensesPiechart.getPlot();
            plot4.setForegroundAlpha(0.6f);

            JPanel expensesPiePanel = new ChartPanel(expensesPiechart);
            expensesPiePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createTitledBorder("Uitgaven")));
            expensesPiePanel.setPreferredSize(new java.awt.Dimension(240, 240));
            expensesPiePanel.setLayout(new BorderLayout());

            graphContainer.add(piePanel);
            graphContainer.add(barChartPanel);
            graphContainer.add(expensesPiePanel);
        }
        revalidate();
        repaint();
    }

    private class Controller implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equals("SELECTMOTOR")) {
                JComboBox source = (JComboBox) e.getSource();
                refreshMotorSpecs((Motorcycle) source.getSelectedItem());
                refreshGraphs((Motorcycle) source.getSelectedItem());
            }
        }
    }
}
