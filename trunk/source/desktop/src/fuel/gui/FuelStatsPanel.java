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

import fuel.lib.Database;
import fuel.lib.Motorcycle;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;

/**
 *
 * @author Mark
 */
public class FuelStatsPanel extends JPanel{
    private JComboBox motorcycles;
    private Database database;
    private Controller controller;
    private JPanel statsContainer;
    private JPanel graphContainer;

    public FuelStatsPanel(Database database) throws SQLException{
        this.database = database;
        controller = new Controller();
        setLayout(new BorderLayout());
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left,BoxLayout.Y_AXIS));
        JPanel leftContainer = new JPanel();
        leftContainer.add(left);
        leftContainer.setBorder(BorderFactory.createEtchedBorder());
        add(leftContainer,BorderLayout.WEST);
        motorcycles = new JComboBox(database.getMotorcycles().toArray());
        motorcycles.setActionCommand("SELECTMOTOR");
        motorcycles.addActionListener(controller);
        statsContainer = new JPanel(new GridLayout(0,1));
        statsContainer.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        
        graphContainer = new JPanel();
        add(graphContainer,BorderLayout.CENTER);
        left.add(motorcycles);
        left.add(new JSeparator());
        left.add(statsContainer);
        fillStats();
    }

    public void fillStats() throws SQLException{
        ResultSet results = database.Query("SELECT avg(liter),avg(cost),avg(distance),max(distance),avg(distance/liter),min(distance/liter),max(distance/liter) FROM fuelrecords WHERE motorcycleId = " + ((Motorcycle)motorcycles.getSelectedItem()).getId(), true);
        statsContainer.removeAll();
        results.next();
        DecimalFormat df = new DecimalFormat("###.##");
        statsContainer.add(new JLabel("Gemiddeld getankt: " + df.format(results.getDouble("1"))+ " liter"));
        statsContainer.add(new JLabel("Gemiddelde afstand: " + df.format(results.getDouble("3"))+ " km"));
        statsContainer.add(new JLabel("Hoogste afstand: " + df.format(results.getDouble("4"))+ " km"));
        statsContainer.add(new JLabel("Gemiddelde kosten: " + df.format(results.getDouble("2"))+ " euro"));
        statsContainer.add(new JLabel("Gemiddeld verbruik: " + df.format(results.getDouble("5"))+ " km/l"));
        statsContainer.add(new JLabel("Laagste verbruik: " + df.format(results.getDouble("6"))+ " km/l"));
        statsContainer.add(new JLabel("Hoogste verbruik: " + df.format(results.getDouble("7"))+ " km/l"));

        statsContainer.revalidate();
        statsContainer.repaint();

    }

    private class Controller implements ActionListener{

        public void actionPerformed(ActionEvent e) {
            try {
                fillStats();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage() + ex.getCause());
            }
        }

    }

}
