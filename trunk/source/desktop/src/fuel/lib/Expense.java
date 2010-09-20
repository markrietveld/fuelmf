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
import java.sql.SQLException;
import java.util.Calendar;

/**
 *
 * @author Mark
 */
public class Expense implements Comparable{

    private Database database;
    private Category category;
    private Motorcycle motor;
    private int id;
    private String type;
    private String brand;
    private String name;
    private boolean hasBeenSaved;
    private boolean changed;
    private Date date;
    private Date checkDate;
    private double costs;
    private double totalDistance;
    private double checkTotalDistance;

    public Expense(Database database) {
        this.database = database;
        hasBeenSaved = false;
        Calendar cal = Calendar.getInstance();
        this.date = new Date(cal.getTime().getTime());
        costs = 0.0;
        this.totalDistance = 0.0;
        this.checkTotalDistance = 0.0;
        id = -1;
        name = "";
        brand = "";
        type = "";
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        if (this.brand == null || !this.brand.equals(brand)){
            changed = true;
        }
        this.brand = brand;        
    }

    public Category getCategory() {
        return category;
    }

    public boolean hasBeenSaved() {
        return hasBeenSaved;
    }

    public void setCategory(Category category) {
        if (this.category == null || !this.category.equals(category)){
            changed = true;
        }
        this.category = category;
        
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getCosts() {
        return costs;
    }

    public void setCosts(double costs) {
        if (this.costs != costs){
            changed = true;
        }
        this.costs = costs;
        
    }

    public double getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(double totalDistance) {
        if (this.totalDistance != totalDistance){
            changed = true;
        }
        this.totalDistance = totalDistance;
    }

    public double getCheckTotalDistance() {
        return checkTotalDistance;
    }

    public void setCheckTotalDistance(double checkTotalDistance) {
        if (this.checkTotalDistance != checkTotalDistance){
            changed = true;
        }
        this.checkTotalDistance = checkTotalDistance;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (this.name == null || !this.name.equals(name)){
            changed = true;
        }
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        if (this.type == null || !this.type.equals(type)){
            changed = true;
        }
        this.type = type;
    }

    public void setHasBeenSaved(boolean hasBeenSaved) {
        this.hasBeenSaved = hasBeenSaved;
    }

    public Motorcycle getMotor() {
        return motor;
    }

    public void setMotor(Motorcycle motor) {
        if (this.motor == null || !this.motor.equals(motor)){
            changed = true;
        }
        this.motor = motor;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        if (this.date == null || !this.date.equals(date)){
            changed = true;
        }
        this.date = date;
    }

    public Date getCheckDate() {
        return checkDate;
    }

    public void setCheckDate(Date checkDate) {
        if (this.checkDate == null || !this.checkDate.equals(checkDate)){
            changed = true;
        }
        this.checkDate = checkDate;
    }

    public boolean isChanged() {
        return changed;
    }

    public void setChanged(boolean changed) {
        this.changed = changed;
    }

    public void toDatabase() throws SQLException {
        if (hasBeenSaved) {
            database.Query("UPDATE expenses SET" +
                    " name = '" + database.encode(name) + "'," +
                    " type = '" + database.encode(type) + "'," +
                    " brand = '" + database.encode(brand) + "'," +
                    " date = '"+date+"'," +
                    " categoryid = " + this.category.getId() + "," +
                    " motorcycleid = " + motor.getId()+"," +
                    " costs = " + costs +"," +
                    " totaldistance = " + totalDistance +"," +
                    " checkTotalDistance = " + checkTotalDistance + ","+
                    " checkDate = " +(checkDate != null ? "'"+checkDate+"'" : "null")+
                    " WHERE id = " + id, false);
        } else {
            database.Query("INSERT INTO expenses (name,type,brand,date,categoryid,motorcycleid,costs,totaldistance,checktotaldistance,checkdate) VALUES ('" + 
                    database.encode(name) + "','" +
                    database.encode(type) + "','" +
                    database.encode(brand) + "','" +
                    date + "'," +
                    category.getId() + "," +
                    motor.getId() + ","+
                    costs+","+
                    totalDistance+","+
                    checkTotalDistance+","+
                    (checkDate != null ? "'"+checkDate+"'" : "null") +")", false);
        }
        if (id == -1){
            motor.updateExpenses(database);
        }
        changed = false;
    }

    public void delete() throws SQLException{
        database.Query("DELETE FROM expenses WHERE id = " + id, false);
    }

    public boolean isValid(){
        return date != null &&
                name.length()>0 &&
                brand.length() > 0 &&
                type.length() > 0;
    }

    public int compareTo(Object o) {
        Expense exp = (Expense)o;
        int result = 1;
        if (equals(exp)){
            result = 0;
        }
        return result;
    }
}
