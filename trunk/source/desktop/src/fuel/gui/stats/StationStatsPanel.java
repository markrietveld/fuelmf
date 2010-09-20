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
import fuel.lib.Station;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
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
public class StationStatsPanel extends JPanel {

    private Database database;
    private JPanel graphContainer;

    public StationStatsPanel(Database database) throws SQLException {
        this.database = database;
        JPanel container = new JPanel(new BorderLayout());
        //container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        graphContainer = new JPanel();
        graphContainer.setLayout(new BoxLayout(graphContainer,BoxLayout.Y_AXIS));

        container.add(graphContainer,BorderLayout.CENTER);
        setLayout(new BorderLayout());
        refreshGraphs();
        add(container);
        setVisible(true);
    }

    private void refreshGraphs() {
        graphContainer.removeAll();
        DefaultPieDataset usageDataset = new DefaultPieDataset();
        try {
            for (Station station:database.getStations()){
                ResultSet thisStation = database.Query("SELECT SUM(liter) FROM fuelrecords WHERE stationId = " + station.getId(), true);
                thisStation.next();
                usageDataset.setValue(station.toString(), thisStation.getInt("1"));
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage() + ex.getCause());
        }
        JFreeChart usagePieChart = ChartFactory.createPieChart3D("", usageDataset, true, true, false);
        PiePlot3D plot3 = (PiePlot3D) usagePieChart.getPlot();
        plot3.setForegroundAlpha(0.6f);
        //plot3.setCircular(true);

        JPanel usagePieChartPanel = new ChartPanel(usagePieChart);
        usagePieChartPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createTitledBorder("Tankstation verhouding")));
        usagePieChartPanel.setPreferredSize(new java.awt.Dimension(320, 240));

        DefaultPieDataset fuelDataset = new DefaultPieDataset();
        try {
            ResultSet numberResults = database.Query("SELECT DISTINCT typeOfGas FROM fuelrecords", true);
            while(numberResults.next()){
                ResultSet thisStation = database.Query("SELECT SUM(liter) FROM fuelrecords WHERE typeOfGas = '" + numberResults.getString("typeOfGas") + "'", true);
                thisStation.next();
                fuelDataset.setValue(numberResults.getString("typeOfGas"), thisStation.getInt("1"));
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage() + ex.getCause());
        }
        JFreeChart fuelPieChart = ChartFactory.createPieChart3D("", fuelDataset, true, true, false);
        PiePlot3D plot2 = (PiePlot3D) fuelPieChart.getPlot();
        plot2.setForegroundAlpha(0.6f);
        //plot3.setCircular(true);

        JPanel fuelPieChartPanel = new ChartPanel(fuelPieChart);
        fuelPieChartPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createTitledBorder("Brandstof verhouding")));
        fuelPieChartPanel.setPreferredSize(new java.awt.Dimension(320, 240));



        DefaultCategoryDataset barDataset = new DefaultCategoryDataset();
        try {
            ResultSet motorThing = database.Query("SELECT cost/liter,date FROM fuelrecords ORDER BY date ASC", true);
            while (motorThing.next()) {
                barDataset.addValue(motorThing.getDouble("1"),motorThing.getString("date") , "Prijs per liter");
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
        barChartPanel.setBorder(BorderFactory.createTitledBorder("Prijs per liter"));
        barChartPanel.setPreferredSize(new java.awt.Dimension(320, 240));


        JPanel piePanel = new JPanel(new GridLayout(0,2));
        piePanel.add(usagePieChartPanel);
        piePanel.add(fuelPieChartPanel);
        graphContainer.add(piePanel);
        graphContainer.add(barChartPanel);
        revalidate();
        repaint();
    }
}
