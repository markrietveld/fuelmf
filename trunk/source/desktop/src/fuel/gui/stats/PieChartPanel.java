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
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.data.general.DefaultPieDataset;

/**
 *
 * @author Mark
 */
public class PieChartPanel extends JPanel {

    public PieChartPanel(DefaultPieDataset pieDataset, String message) {
        JFreeChart pieChart = ChartFactory.createPieChart3D("", pieDataset, true, true, false);
        PiePlot3D plot1 = (PiePlot3D) pieChart.getPlot();
        plot1.setForegroundAlpha(0.6f);
        //plot3.setCircular(true);

        ChartPanel barChartPanel = new ChartPanel(pieChart);
        barChartPanel.getChartRenderingInfo().setEntityCollection(null);
        barChartPanel.setBorder(BorderFactory.createTitledBorder(message));
        barChartPanel.setPreferredSize(new java.awt.Dimension(320, 240));
        barChartPanel.setLayout(new BorderLayout());
        setLayout(new BorderLayout());
        add(barChartPanel);
    }
}
