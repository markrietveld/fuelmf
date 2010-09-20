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
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Mark
 */
public class Motorcycle implements Comparable {

    private String brand;
    private String type;
    private int cc;
    private int weight;
    private int cilinders;
    private double tankSize;
    private int id;
    private boolean changed;
    private boolean hasBeenSaved;
    private List<Expense> expenses;

    public Motorcycle() {
        id = -1;
        expenses = new ArrayList<Expense>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        if (this.brand == null || !this.brand.equals(brand)) {
            changed = true;
        }
        this.brand = brand;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        if (this.type == null || !this.type.equals(type)) {
            changed = true;
        }
        this.type = type;
    }

    public int getCc() {
        return cc;
    }

    public void setCc(int cc) {
        if (!(this.cc == cc)) {
            changed = true;
        }
        this.cc = cc;
    }

    public int getCilinders() {
        return cilinders;
    }

    public void setCilinders(int cilinders) {
        if (!(this.cilinders == cilinders)) {
            changed = true;
        }
        this.cilinders = cilinders;
    }

    public double getTankSize() {
        return tankSize;
    }

    public void setTankSize(double tankSize) {
        if (!(this.tankSize == tankSize)) {
            changed = true;
        }
        this.tankSize = tankSize;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        if (!(this.weight == weight)) {
            changed = true;
        }
        this.weight = weight;
    }

    public void addExpense(Expense expense) {
        expenses.add(expense);
    }

    public List<Expense> getExpenses() {
        return expenses;
    }

    public Expense getExpenseById(int id) {
        Expense exp = null;
        boolean found = false;
        for (int i = 0; i < expenses.size() && !found; i++) {
            if (expenses.get(i).getId() == id) {
                exp = expenses.get(i);
                found = true;
            }
        }
        return exp;
    }

    public void removeExpense(Expense exp) {
        expenses.remove(exp);
    }

    public List<Expense> getExpensesByCategoryId(int catId) {
        List<Expense> list = new ArrayList<Expense>();
        for (Expense exp : expenses) {
            if (exp.getCategory().getId() == catId) {
                list.add(exp);
            }
        }
        return list;
    }

    public void updateExpenses(Database database) throws SQLException{
        expenses.clear();
        expenses.addAll(database.getExpensesByMotorId(this));
    }

    public boolean hasBeenSaved() {
        return hasBeenSaved;
    }

    public void setHasBeenSaved(boolean hasBeenSaved) {
        this.hasBeenSaved = hasBeenSaved;
        changed = false;
    }

    public boolean isChanged() {
        return changed;
    }

    public void setChanged(boolean changed) {
        this.changed = changed;
    }

    public boolean isValid() {
        return brand != null && brand.length() > 0 && type != null && type.length() > 0;
    }

    public void toDatabase(Database database) throws SQLException {
        if (id != -1) {
            database.Query("UPDATE motorcycles SET brand = '" + database.encode(brand) + "',type = '" + database.encode(type) + "', cc = " + cc + ", weight = " + weight + ", cilinders = " + cilinders + ", tankSize = " + tankSize + " WHERE id = " + id, false);
        } else {
            database.Query("INSERT INTO motorcycles (brand,type,cc,weight,cilinders,tankSize) VALUES ('" + database.encode(brand) + "','" + database.encode(type) + "'," + cc + "," + weight + "," + cilinders + "," + tankSize + ")", false);
        }
    }

    public void delete(Database database) throws SQLException {
        database.Query("DELETE FROM fuelrecords WHERE motorcycleId = " + id, false);
        database.Query("DELETE FROM expenses WHERE motorcycleId = " + id, false);
        database.Query("DELETE FROM motorcycles WHERE id = " + id, false);
    }

    public int compareTo(Object o) {
        Motorcycle other = (Motorcycle) o;
        int temp = brand.compareTo(other.getBrand());
        if (temp == 0) {
            temp = type.compareTo(other.getType());
        }
        return temp;
    }

    public boolean equals(Object o) {
        boolean temp = false;
        if (o instanceof Motorcycle) {
            Motorcycle other = (Motorcycle) o;
            temp = this.id == other.id;
        }
        return temp;
    }

    /**
     * Searches the brand and type of this motorcycle for the given string (full text search)
     * Returns true if the string was found
     * @param search the string to search for
     * @return true if the string was found in this motorcycle
     */
    public boolean relates(String search){
        search = search.toLowerCase();
        return (brand.toLowerCase().contains(search) || type.toLowerCase().contains(search));
    }

    public String toString() {
        return brand + " " + type;
    }
}
