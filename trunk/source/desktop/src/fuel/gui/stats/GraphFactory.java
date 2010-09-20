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

import fuel.lib.Category;
import fuel.lib.Database;
import fuel.lib.Expense;
import fuel.lib.Motorcycle;
import fuel.lib.Station;
import java.awt.BorderLayout;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

/**
 *
 * @author Mark
 */
public class GraphFactory {

    public static final String[] months = {"Januari", "Februari", "Maart", "April", "Mei", "Juni", "Juli", "Augustus", "September", "Oktober", "November", "December"};
    public static final int BY_YEAR = 2;
    public static final int BY_MONTH = 1;
    public static final int BY_DAY = 0;

    public static JComponent createDistanceTraveled(Database database, boolean divide, boolean cumulative, Date startDate, Date endDate) throws SQLException {
        JPanel container = new JPanel(new BorderLayout());
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        if (divide) {
            double total = 0;
            for (Motorcycle motor : database.getMotorcycles()) {
                total = 0;
                DefaultCategoryDataset barDataset = new DefaultCategoryDataset();
                try {
                    ResultSet motorThing = database.Query("SELECT sum(distance),Month(date),Year(date) FROM fuelrecords" +
                            " WHERE motorcycleId = " + motor.getId() +
                            " AND date >= '" + startDate + "'" +
                            " AND date <= '" + endDate + "'" +
                            " GROUP BY Year (date), Month(date)", true);
                    while (motorThing.next()) {
                        if (cumulative) {
                            total += motorThing.getDouble("1");
                        } else {
                            total = motorThing.getDouble("1");
                        }
                        barDataset.addValue(total, motor.toString(), months[motorThing.getInt("2") - 1] + " " + motorThing.getString("3"));
                    }

                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage() + ex.getCause());
                }
                panel.add(new BarChartPanel(barDataset, "Gereden afstand met " + motor.toString() + " tussen " + startDate + " en " + endDate, false));
            }
        } else {
            DefaultCategoryDataset barDataset = new DefaultCategoryDataset();
            try {
                if (cumulative) {
                    for (Motorcycle motor : database.getMotorcycles()) {
                        double total = 0;
                        ResultSet motorThing = database.Query("SELECT sum(distance),Month(date),Year(date),motorcycleid FROM fuelrecords" +
                                " WHERE date >= '" + startDate + "'" +
                                " AND date <= '" + endDate + "'" +
                                " GROUP BY Year (date), Month(date), motorcycleid", true);
                        while (motorThing.next()) {
                            if (motorThing.getInt("motorcycleid") == motor.getId()) {
                                total += motorThing.getDouble("1");
                            }
                            if (total > 0) {
                                barDataset.addValue(total, motor.toString(), months[motorThing.getInt("2") - 1] + " " + motorThing.getString("3"));
                            }
                        }
                    }
                } else {
                    ResultSet motorThing = database.Query("SELECT sum(distance),Month(date),Year(date),motorcycleid FROM fuelrecords" +
                            " WHERE date >= '" + startDate + "'" +
                            " AND date <= '" + endDate + "'" +
                            " GROUP BY motorcycleid,Year (date), Month(date)", true);
                    while (motorThing.next()) {
                        barDataset.addValue(motorThing.getDouble("1"), database.getMotorcycleById(motorThing.getInt("motorcycleid")).toString(), months[motorThing.getInt("2") - 1] + " " + motorThing.getString("3"));
                    }
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage() + ex.getCause());
            }
            panel.add(new BarChartPanel(barDataset, "Gereden afstand tussen " + startDate + " en " + endDate, true));

        }
        container.add(panel);
        return new JScrollPane(container);
    }

    public static JComponent createFuelUsage(boolean divide, Database database, Date startDate, Date endDate) throws SQLException {
        JPanel container = new JPanel(new BorderLayout());
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        for (Motorcycle motor : database.getMotorcycles()) {
            DefaultCategoryDataset barDataset = new DefaultCategoryDataset();
            try {
                if (divide) {
                    ResultSet motorThing = database.Query("SELECT sum(distance)/sum(liter),Month(date),Year(date) FROM fuelrecords" +
                            " WHERE motorcycleId = " + motor.getId() + "" +
                            " AND date >= '" + startDate + "'" +
                            " AND date <= '" + endDate + "'" +
                            " GROUP BY Month(date),Year(date)", true);
                    while (motorThing.next()) {
                        barDataset.addValue(motorThing.getDouble("1"), motor.toString(), months[motorThing.getInt("2") - 1] + "");
                    }
                } else {
                    ResultSet motorThing = database.Query("SELECT distance/liter,date FROM fuelrecords" +
                            " WHERE motorcycleId = " + motor.getId() + "" +
                            " AND date >= '" + startDate + "'" +
                            " AND date <= '" + endDate + "'" +
                            " ORDER BY date ASC", true);
                    int count = 0;

                    while (motorThing.next()) {
                        barDataset.addValue(motorThing.getDouble("1"), motor.toString(), motorThing.getDate("date"));
                        count++;
                    }
                }

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage() + ex.getCause());
            }
            panel.add(new BarChartPanel(barDataset, "Kilometer per liter van " + motor.toString() + " tussen " + startDate + " en " + endDate, false));
        }

        container.add(panel);
        return new JScrollPane(container);
    }

    public static JComponent createMotorUsage(Database database, Date startDate, Date endDate) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        DefaultPieDataset motorDataset = new DefaultPieDataset();
        try {
            for (Motorcycle motor : database.getMotorcycles()) {
                ResultSet result = database.Query("SELECT SUM(distance) FROM fuelrecords" +
                        " WHERE motorcycleId = " + motor.getId() +
                        " AND date >= '" + startDate + "'" +
                        " AND date <= '" + endDate + "'", true);
                result.next();
                if (result.getDouble("1") > 0) {
                    motorDataset.setValue(motor.toString(), result.getDouble("1"));
                }
            }
            panel.add(new PieChartPanel(motorDataset, "Motorgebruik tussen " + startDate + " en " + endDate));
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage() + ex.getCause());
        }
        return new JScrollPane(panel);
    }

    public static JComponent createFuelTypes(boolean divide, Database database, Date startDate, Date endDate) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        try {
            if (divide) {
                for (Motorcycle motor : database.getMotorcycles()) {
                    DefaultPieDataset fuelDataset = new DefaultPieDataset();
                    ResultSet types = database.Query("SELECT DISTINCT lower(typeOfGas) FROM fuelrecords" +
                            " WHERE motorcycleId = " + motor.getId() +
                            " AND date >= '" + startDate + "'" +
                            " AND date <= '" + endDate + "'", true);
                    while (types.next()) {
                        ResultSet result = database.Query("SELECT SUM(liter) FROM fuelrecords" +
                                " WHERE motorcycleId = " + motor.getId() + "" +
                                " AND lower(typeOfGas) = '" + types.getString(1).toLowerCase() + "'" +
                                " AND date >= '" + startDate + "'" +
                                " AND date <= '" + endDate + "'", true);
                        result.next();
                        if (result.getDouble("1") > 0) {
                            fuelDataset.setValue(types.getString(1), result.getDouble("1"));
                        }
                    }
                    panel.add(new PieChartPanel(fuelDataset, "Brandstof verhouding met " + motor.toString()));
                }
            } else {
                DefaultPieDataset fuelDataset = new DefaultPieDataset();
                ResultSet types = database.Query("SELECT DISTINCT lower(typeOfGas) FROM fuelrecords" +
                        " WHERE date >= '" + startDate + "'" +
                        " AND date <= '" + endDate + "'", true);
                while (types.next()) {
                    ResultSet result = database.Query("SELECT SUM(liter) FROM fuelrecords" +
                            " WHERE lower(typeOfGas) = '" + types.getString(1).toLowerCase() + "'" +
                            " AND date >= '" + startDate + "'" +
                            " AND date <= '" + endDate + "'", true);
                    result.next();
                    if (result.getDouble("1") > 0) {
                        fuelDataset.setValue(types.getString(1), result.getDouble("1"));
                    }
                }
                panel.add(new PieChartPanel(fuelDataset, "Brandstof verhouding tussen " + startDate + " en " + endDate));
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage() + ex.getCause());
        }
        return new JScrollPane(panel);
    }

    public static JComponent createExpenses(Database database, boolean includeFuel, Date startDate, Date endDate) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        try {

            for (Motorcycle motor : database.getMotorcycles()) {
                DefaultPieDataset fuelDataset = new DefaultPieDataset();
                if (includeFuel) {
                    ResultSet result = database.Query("SELECT SUM(cost) FROM fuelrecords" +
                            " WHERE motorcycleId = " + motor.getId() +
                            " AND date >= '" + startDate + "'" +
                            " AND date <= '" + endDate + "'", true);
                    result.next();
                    if (result.getDouble("1") > 0) {
                        fuelDataset.setValue("Brandstof", result.getDouble("1"));
                    }
                }
                for (Category cat : database.getCategories()) {
                    ResultSet result = database.Query("SELECT SUM(costs) FROM expenses" +
                            " WHERE motorcycleId = " + motor.getId() + "" +
                            " AND categoryid = " + cat.getId() +
                            " AND date >= '" + startDate + "'" +
                            " AND date <= '" + endDate + "'", true);
                    result.next();
                    if (result.getDouble("1") > 0) {
                        fuelDataset.setValue(cat.getName(), result.getDouble("1"));
                    }
                }

                panel.add(new PieChartPanel(fuelDataset, "Brandstof verhouding met " + motor.toString() + " tussen " + startDate + " en " + endDate));
            }


        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage() + ex.getCause());
        }
        return new JScrollPane(panel);
    }

    public static JComponent createStationUsage(Database database, boolean divide, Date startDate, Date endDate) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        DefaultPieDataset motorDataset = new DefaultPieDataset();
        try {
            if (divide) {
                for (Motorcycle motor : database.getMotorcycles()) {
                    motorDataset = new DefaultPieDataset();
                    for (Station station : database.getStations()) {
                        ResultSet result = database.Query("SELECT SUM(liter) FROM fuelrecords " +
                                "WHERE stationid = " + station.getId() +
                                " AND motorcycleid = " + motor.getId() +
                                " AND date >= '" + startDate + "'" +
                                " AND date <= '" + endDate + "'", true);
                        result.next();
                        if (result.getDouble("1") > 0) {
                            motorDataset.setValue(station.toString(), result.getDouble("1"));
                        }
                    }
                    panel.add(new PieChartPanel(motorDataset, "Tankstation gebruik met " + motor.toString() + " tussen " + startDate + " en " + endDate));
                }

            } else {
                for (Station station : database.getStations()) {
                    ResultSet result = database.Query("SELECT SUM(liter) FROM fuelrecords" +
                            " WHERE stationid = " + station.getId() +
                            " AND date >= '" + startDate + "'" +
                            " AND date <= '" + endDate + "'", true);
                    result.next();
                    if (result.getDouble("1") > 0) {
                        motorDataset.setValue(station.toString(), result.getDouble("1"));
                    }
                }
                panel.add(new PieChartPanel(motorDataset, "Tankstation gebruik tussen " + startDate + " en " + endDate));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage() + ex.getCause());
        }
        return new JScrollPane(panel);
    }

    public static JComponent createFuelPrice(Database database, boolean divide, Date startDate, Date endDate) {
        JPanel container = new JPanel(new BorderLayout());
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        try {
            if (divide) {
                double total = 0;
                for (Station station : database.getStations()) {
                    total = 0;
                    DefaultCategoryDataset barDataset = new DefaultCategoryDataset();
                    try {
                        ResultSet motorThing = database.Query("SELECT cost/liter,date FROM fuelrecords" +
                                " WHERE stationid = " + station.getId() + "" +
                                " AND date >= '" + startDate + "'" +
                                " AND date <= '" + endDate + "'" +
                                " ORDER BY date asc", true);
                        while (motorThing.next()) {
                            total = motorThing.getDouble("1");
                            barDataset.addValue(total, station.toString(), motorThing.getString("date"));
                        }
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(null, ex.getMessage() + ex.getCause());
                    }
                    panel.add(new BarChartPanel(barDataset, "Prijs per liter bij " + station.toString() + " tussen " + startDate + " en " + endDate, false));
                }
            } else {
                DefaultCategoryDataset barDataset = new DefaultCategoryDataset();
                try {
                    ResultSet motorThing = database.Query("SELECT cost/liter,stationid,id,date FROM fuelrecords" +
                            " WHERE date >= '" + startDate + "'" +
                            " AND date <= '" + endDate + "'" +
                            " ORDER BY date asc", true);
                    while (motorThing.next()) {
                        barDataset.addValue(motorThing.getDouble("1"), database.getStationById(motorThing.getInt("stationid")).toString(), motorThing.getString("date") + " " + motorThing.getString("id"));
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage() + ex.getCause());
                }
                panel.add(new BarChartPanel(barDataset, "Prijs per liter tussen " + startDate + " en " + endDate, true));

            }
        } catch (SQLException ex) {
        }
        container.add(panel);
        return new JScrollPane(container);
    }

    public static JComponent createCost(Database database, int grouping, Date startDate, Date endDate) {
        JPanel container = new JPanel(new BorderLayout());
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        try {

            double total = 0;
            String groupString;
            if (grouping == BY_DAY) {
                groupString = "YEAR(date),MONTH(date),DAY(date)";
            } else if (grouping == BY_YEAR) {
                groupString = "YEAR(date)";
            } else {
                groupString = "YEAR(date),MONTH(date)";
            }
            for (Motorcycle motor : database.getMotorcycles()) {
                total = 0;
                DefaultCategoryDataset barDataset = new DefaultCategoryDataset();
                try {
                    String reading = "";
                    ResultSet motorThing = database.Query("SELECT sum(cost)," + groupString + " FROM fuelrecords" +
                            " WHERE motorcycleid = " + motor.getId() +
                            " AND date >= '" + startDate + "'" +
                            " AND date <= '" + endDate + "'" +
                            " GROUP BY " + groupString, true);
                    while (motorThing.next()) {
                        if (grouping == BY_DAY) {
                            reading = motorThing.getString(4) + "-" + months[motorThing.getInt(3) - 1] + "-" + motorThing.getString(2);
                        } else if (grouping == BY_YEAR) {
                            reading = motorThing.getString(2);
                        } else {
                            reading = months[motorThing.getInt(3) - 1] + "-" + motorThing.getString(2);
                        }
                        total = motorThing.getDouble(1);
                        barDataset.addValue(total, "Brandstof", reading);
                    }
                    for (Category cat : database.getCategories()) {
                        motorThing = database.Query("SELECT sum(costs)," + groupString + " FROM expenses" +
                                " WHERE motorcycleid = " + motor.getId() +
                                " AND categoryid = " + cat.getId() +
                                " AND date >= '" + startDate + "'" +
                                " AND date <= '" + endDate + "'" +
                                " GROUP BY " + groupString, true);
                        while (motorThing.next()) {
                            if (grouping == BY_DAY) {
                                reading = motorThing.getString(4) + "-" + months[motorThing.getInt(3) - 1] + "-" + motorThing.getString(2);
                            } else if (grouping == BY_YEAR) {
                                reading = motorThing.getString(2);
                            } else {
                                reading = months[motorThing.getInt(3) - 1] + "-" + motorThing.getString(2);
                            }
                            total = motorThing.getDouble(1);
                            barDataset.addValue(total, cat.getName(), reading);
                        }
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage() + ex.getCause());
                }
                panel.add(new BarChartPanel(barDataset, "Totale kosten van " + motor.toString() + " tussen " + startDate + " en " + endDate, true));
            }

        } catch (SQLException ex) {
        }
        container.add(panel);
        return new JScrollPane(container);
    }
}
