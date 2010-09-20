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

import java.sql.SQLException;

/**
 * Models a gasstation
 * @author Mark
 */
public class Station implements Comparable {

    private String name;
    private String location;
    private int id;
    private boolean hasBeenSaved;
    private boolean changed;

    /**
     * Gets the ID given to this station
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the ID for this station
     * this method should only be used by the database class
     * since the database manages IDs
     * @param id the id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the location that was set
     * if it has not been set before it returns ""
     * @return the location
     */
    public String getLocation() {
        return location;
    }

    /**
     * Return wether this station has been stored in the database
     * @return true if this station is in the database, false otherwise
     */
    public boolean hasBeenSaved() {
        return hasBeenSaved;
    }

    /**
     * Sets wether this station is in the database
     * this method should only be used by the database class when reading stations from the database
     * @param hasBeenSaved
     */
    public void setHasBeenSaved(boolean hasBeenSaved) {
        this.hasBeenSaved = hasBeenSaved;
    }

    /**
     * Sets the location for this station
     * Also ensures changed() == true if location contains new information
     * @param location the new location
     */
    public void setLocation(String location) {
        if (this.location != null && !this.location.equals(location)){
            changed = true;
        }
        this.location = location;
    }

    /**
     * Returns the name for this station
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Return wether this station is changed significantly enough to save to database
     * @return true if changed
     */
    public boolean isChanged() {
        return changed;
    }

    /**
     * Sets the name for this station
     * Also ensures changed() == true if name contains new information
     * @param name the new name
     */
    public void setName(String name) {
        if (this.name != null && !this.name.equals(name)){
            changed = true;
        }
        this.name = name;
    }

    /**
     * Constructor for station
     */
    public Station() {
        id = -1;
    }

    /**
     * Checks if this station doesn't contain wrong information
     * @return true if this station can be saved to database, false otherwise
     */
    public boolean isValid() {
        return name != null && name.length() > 0 && location != null && location.length() > 0;
    }

    /**
     * Writes this station to the database
     * Creates a new entry if !hasBeenSaved() and updates the entry otherwise
     * @param database the database to which this station should be saved
     * @throws java.sql.SQLException
     */
    public void toDatabase(Database database) throws SQLException {
        if (id != -1) {
            database.Query("UPDATE stations SET name ='"+ database.encode(name) + "',location ='" + database.encode(location) + "' WHERE id = "+id, false);
        } else {
            database.Query("INSERT INTO stations (name,location) VALUES ('" + database.encode(name) + "','" + database.encode(location) + "')", false);
        }
    }

    /**
     * Removes this station from the database
     * Also removes all fuelrecords associated with this station
     * @param database the database this station should be removed from
     * @throws java.sql.SQLException
     */
    public void delete(Database database) throws SQLException {
        database.Query("DELETE FROM fuelrecords WHERE stationId = " + id, false);
        database.Query("DELETE FROM stations WHERE id = " + id, false);
    }

    /**
     * Returns a string representation of this station
     * @return getName()+ " " + getLocation()
     */
    public String toString() {
        return name + " " + location;
    }

    public int compareTo(Object o) {
        Station other = (Station) o;
        int temp = name.compareTo(other.getName());
        if (temp == 0) {
            temp = location.compareTo(other.getLocation());
        }
        return temp;
    }

    @Override
    public boolean equals(Object o) {
        boolean temp = false;

        if (o instanceof Station) {
            Station other = (Station) o;
            temp = other.id == id;
        }
        return temp;
    }
}
