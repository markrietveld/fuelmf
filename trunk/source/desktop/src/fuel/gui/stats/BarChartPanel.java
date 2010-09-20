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

import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer3D;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 *
 * @author Mark
 */
public class BarChartPanel extends JPanel {

    public BarChartPanel(DefaultCategoryDataset barDataset, String message, boolean stacked) {
        JFreeChart barChart ;
        if (stacked) {
            barChart = ChartFactory.createStackedBarChart3D(
                "", // chart title
                "", // domain axis label
                "", // range axis label
                barDataset, // data
                PlotOrientation.VERTICAL,
                true, // include legend
                true, // tooltips?
                true // URLs?
                );
        } else {
            barChart = ChartFactory.createBarChart3D(
                "", // chart title
                "", // domain axis label
                "", // range axis label
                barDataset, // data
                PlotOrientation.VERTICAL,
                false, // include legend
                true, // tooltips?
                true // URLs?
                );
        }
        CategoryPlot plot = barChart.getCategoryPlot();
        BarRenderer3D renderer = (BarRenderer3D) plot.getRenderer();
        renderer.setDrawBarOutline(false);
        final CategoryAxis domainAxis = plot.getDomainAxis();
        double count = barDataset.getColumnCount();
        double extra = 16/count;
        domainAxis.setCategoryLabelPositions(
            CategoryLabelPositions.createUpRotationLabelPositions(Math.PI /(2 + extra))
        );

        ChartPanel barChartPanel = new ChartPanel(barChart);
        barChartPanel.getChartRenderingInfo().setEntityCollection(null);
        barChartPanel.setBorder(BorderFactory.createTitledBorder(message));
        int wider = barDataset.getColumnCount() * 12;
        barChartPanel.setPreferredSize(new java.awt.Dimension(192+wider, 240));
        barChartPanel.setLayout(new BorderLayout());
        setLayout(new BorderLayout());
        add(barChartPanel);
    }
}
