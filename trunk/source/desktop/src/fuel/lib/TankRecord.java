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
package fuel.lib;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author Mark
 */
public class TankRecord implements Comparable {

    private Station station;
    private String typeOfGas;
    private String comment = "";
    private double liters;
    private double cost;
    private double distanceTraveled;
    private double totalDistance;
    private int id;
    private Date date;
    private Motorcycle motorcycle;
    private boolean hasBeenSaved;
    private boolean changed;
    private Database database;

    public TankRecord(Database database) {
        this.database = database;
        id = -1;
        Calendar cal = Calendar.getInstance();
        date = new Date(cal.getTimeInMillis());
        try {
            if (database != null) {
                ResultSet result = database.Query("SELECT * FROM settings", true);
                result.next();
                typeOfGas = result.getString("defaultfueltype");
            } else {
                typeOfGas = "";
            }
        } catch (SQLException ex) {
            typeOfGas = "";
        }

    }

    public boolean hasBeenSaved() {
        return hasBeenSaved;
    }

    public boolean isChanged() {
        return changed;
    }

    public void setChanged(boolean changed) {
        this.changed = changed;
    }

    public void setHasBeenSaved(boolean hasBeenSaved) {
        this.hasBeenSaved = hasBeenSaved;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        if (this.date != null && !this.date.equals(date)) {
            changed = true;
        }
        this.date = date;
    }

    public Motorcycle getMotorcycle() {
        return motorcycle;
    }

    public void setMotorcycle(Motorcycle motorcycle) {
        if (this.motorcycle != null && !this.motorcycle.equals(motorcycle)) {
            changed = true;
        }
        this.motorcycle = motorcycle;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        if (this.comment != null && !this.comment.equals(comment)) {
            changed = true;
        }
        this.comment = comment;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        if (this.cost != 0 && !(this.cost == cost)) {
            changed = true;
        }
        this.cost = cost;
    }

    public double getDistanceTraveled() {
        return distanceTraveled;
    }

    public void setDistanceTraveled(double distanceTraveled) {
        if (!(this.distanceTraveled == distanceTraveled)) {
            changed = true;
        }
        this.distanceTraveled = distanceTraveled;
    }

    public double getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(double totalDistance) {
        if (!(this.totalDistance == totalDistance)) {
            changed = true;
        }
        this.totalDistance = totalDistance;
    }

    public double getLiters() {
        return liters;
    }

    public void setLiters(double liters) {
        if (!(this.liters == liters)) {
            changed = true;
        }
        this.liters = liters;
    }

    public Station getStation() {
        return station;
    }

    public void setStation(Station station) {
        if (this.station != null && !this.station.equals(station)) {
            changed = true;
        }
        this.station = station;
    }

    public String getTypeOfGas() {
        return typeOfGas;
    }

    public void setTypeOfGas(String typeOfGas) {
        if (this.typeOfGas != null && !this.typeOfGas.equals(typeOfGas)) {
            changed = true;
        }
        this.typeOfGas = typeOfGas;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isValid() {
        boolean valid = station != null &&
                station.isValid() &&
                typeOfGas != null &&
                comment != null &&
                liters > 0.0 &&
                distanceTraveled > 0.0 &&
                date != null &&
                motorcycle != null &&
                motorcycle.isValid();
        return valid;
    }

    public int compareTo(Object o) {
        TankRecord comp = (TankRecord) o;
        int temp = this.date.compareTo(comp.getDate());
        if (temp == 0) {
            if (!equals(comp)) {
                temp = -1;
            }
        }
        return temp;
    }

    @Override
    public boolean equals(Object o) {
        TankRecord other = (TankRecord) o;
        boolean result = false;
        if (o != null) {
            /*result = (this.station == null && other.station == null) || this.station.equals(other.getStation()) &&
            (this.comment == null && other.comment == null) || this.comment.equals(other.comment) &&
            this.cost == other.cost &&
            (this.date == null && other.date == null) || this.date.equals(other.date) &&
            this.distanceTraveled == other.distanceTraveled &&
            this.id == other.id &&
            this.liters == other.liters &&
            (this.motorcycle == null && other.motorcycle == null) || this.motorcycle.equals(other.motorcycle) &&
            this.totalDistance == other.totalDistance &&
            (this.typeOfGas == null && other.typeOfGas == null) || this.typeOfGas.equals(other.typeOfGas);*/
            result = (this.id == other.id);
        }
        return result;
    }

    public void toDatabase(Database database) throws SQLException {
        if (isValid()) {
            if (hasBeenSaved) {
                database.Query("UPDATE fuelrecords SET liter = " + liters + ",cost = " + cost + ",distance = " + distanceTraveled + ",totaldistance = " + totalDistance + ",stationId = " + station.getId() + ",motorcycleId = " + motorcycle.getId() + ",date = '" + date + "',comment = '" + database.encode(comment) + "',typeOfGas = '" + database.encode(typeOfGas) + "' WHERE ID = " + id, false);
            } else {
                database.Query("INSERT INTO fuelrecords (liter,cost,distance,totaldistance,stationId,motorcycleId,date,comment,typeOfGas) VALUES (" + liters + "," + cost + "," + distanceTraveled + "," + totalDistance + "," + station.getId() + "," + motorcycle.getId() + ",'" + date + "','" + database.encode(comment) + "','" + database.encode(typeOfGas) + "')", false);
            }
            Thread t = new Thread() {

                private Database database;

                public void set(Database database) {
                    this.database = database;
                }

                public void run() {
                    try {
                        giveMessage();
                    } catch (SQLException ex) {
                        Logger.getLogger(TankRecord.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            };
            t.start();
        } else {
            throw new NumberFormatException("0 is not valid");
        }
    }

    public void giveMessage() throws SQLException {
        changed = false;
        for (Expense exp : database.getExpensesByMotorId(motorcycle)) {
            boolean dist = false;
            boolean dat = false;
            if (exp.getCheckTotalDistance() > 0.0 && totalDistance > exp.getCheckTotalDistance()) {
                dist = true;
            }
            if (exp.getCheckDate() != null && date.after(exp.getCheckDate())) {
                dat = true;
            }
            if (dist || dat) {
                int sure = JOptionPane.showConfirmDialog(null, "Een uitgave die u op " + exp.getDate() + " gedaan heeft voor uw  " + motorcycle.toString() + " is aan controle toe.\n" +
                        " U heeft " + (totalDistance - exp.getTotalDistance()) + "km afgelegd sinds deze uitgave.\n\n" + "Het betreft:\n" + "Onderdeel: " + exp.getName() + "\n" + "Merk: " + exp.getBrand() + "\n" + "Type: " + exp.getType() + "\n\n\n" + "Wilt u deze waarschuwing nu uitschakelen?", "", JOptionPane.YES_NO_OPTION);
                if (sure == JOptionPane.YES_OPTION) {
                    exp.setCheckTotalDistance(0.0);
                    exp.setCheckDate(null);
                    exp.toDatabase();
                }
            }
        }
    }

    public void delete(Database database) throws SQLException {
        database.Query("DELETE FROM fuelrecords WHERE id = " + id, false);
    }

    @Override
    public String toString() {
        return date.toString() + " - " + motorcycle.toString() + " - " + station.toString();
    }
}
