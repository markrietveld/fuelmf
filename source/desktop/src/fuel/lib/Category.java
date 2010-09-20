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
 *
 * @author Mark
 */
public class Category implements Comparable{

    private Database database;
    private int id;
    private String name;
    private boolean hasBeenSaved;

    public Category(Database database){
        this.database = database;
        hasBeenSaved = false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setHasBeenSaved(boolean hasBeenSaved) {
        this.hasBeenSaved = hasBeenSaved;
    }

    public Database getDatabase() {
        return database;
    }

    public void setDatabase(Database database) {
        this.database = database;
    }

    public void toDatabase() throws SQLException{
        int otherId = -1;
        for (Category cat: database.getCategories()){
            if (cat.getName().equals(name)){
                otherId = cat.getId();
            }
        }
        if (hasBeenSaved){
            if (otherId == -1 || otherId == this.id){
                database.Query("UPDATE categories SET name = '" + database.encode(name) + "' WHERE id = " + id, false);
            } else {
                throw new SQLException("Die categorie bestaat al");
            }
        } else {
            if (otherId == -1){
                database.Query("INSERT INTO categories (name) VALUES ('"+database.encode(name)+"')", false);
            } else {
                throw new SQLException("Die categorie bestaat al");
            }
        }
    }
    
    public void delete() throws SQLException{
        database.Query("DELETE FROM expenses WHERE categoryid = " + id, false);
        database.Query("DELETE FROM categories WHERE id = " + id, false);
    }

    public int compareTo(Object o) {
        Category cat = (Category)o;
        int result = 1;
        if (equals(cat)){
            result = 0;
        }
        return result;
    }

}
