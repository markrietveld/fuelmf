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

import java.awt.FlowLayout;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 *
 * @author Mark
 */
public class Database {

    private Connection connection;
    private String location;
    public static final int CURRENTVERSION = 6;

    public Database(String location) throws SQLException, ClassNotFoundException {
        this.location = location;
        connect();
    }

    public void connect() throws SQLException, ClassNotFoundException {
        // Load the JDBC driver
        String driverName = "org.apache.derby.jdbc.EmbeddedDriver"; // MySQL MM JDBC driver
        Class.forName(driverName);

        //connection = DriverManager.getConnection("jdbc:mysql://" + hostname + "/" + database, username, password);
        connection = DriverManager.getConnection("jdbc:derby:"+location+";create=true");
        try {
            ResultSet results = Query("SELECT * FROM version", true);
            results.next();
            int databaseVersion = results.getInt("VERSION");
            if (databaseVersion < Database.CURRENTVERSION) {
                int sure = JOptionPane.showConfirmDialog(null, "Het programma staat op het punt uw gegevens te updaten naar de nieuwe versie.\nHierbij gaan in principe geen gegevens verloren," +
                        " maar u kunt voor de zekerheid nu een backup uitvoeren door een kopie te maken van de map 'Data' in de installatiemap\n\n" +
                        "Druk op OK om het updateproces te starten, of op cancel om het programma te sluiten zonder de update uit te voeren.", "Update", JOptionPane.OK_CANCEL_OPTION);
                if (sure == JOptionPane.OK_OPTION) {
                    if (databaseVersion == 1) {
                        updateVersion1to2();
                        results = Query("SELECT * FROM version", true);
                        results.next();
                        databaseVersion = results.getInt("VERSION");
                    }
                    if (databaseVersion == 2) {
                        updateVersion2to3();
                        databaseVersion = 3;
                    }
                    if (databaseVersion == 3) {
                        updateVersion3to4();
                        results = Query("SELECT COUNT(*) FROM fuelrecords", true);
                        results.next();
                        if (results.getInt("1") > 0 && JOptionPane.showConfirmDialog(null, "Deze nieuwe versie houdt de totale kilometerstand van uw motor bij.\n" +
                                "U kunt een kilometerstand opgeven voor een tankbeurt waarvan u de kilometerstand weet,\nhet programma zal dan automatisch de kilometerstand van de tankbeurten ervoor en erna uitrekenen.\n" +
                                "Als u geen gebruik maakt van deze functie zullen alle kilometerstanden 0 zijn, u kunt de standen altijd wijzigen. \n\n" +
                                "Wilt u een beginstand opgeven?", "Nieuwe versie", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
                                calculateTotalDistance(null);
                        }
                        databaseVersion = 4;
                    }                    
                    if (databaseVersion == 4) {
                        updateVersion4to5();
                        databaseVersion = 5;
                    }
                    if (databaseVersion == 5) {
                        updateVersion5to6();
                        databaseVersion = 6;
                    }
                    JOptionPane.showMessageDialog(null, "De gegevens in de het programma zijn succesvol aangepast aan de nieuwe versie.", "Update", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    System.exit(0);
                }
            }
        } catch (SQLException ex) {
            //ex.printStackTrace();
            createTables();
        }
    }

    public TreeSet<TankRecord> getRecords() throws SQLException {
        ResultSet results = Query("SELECT * FROM fuelrecords", true);
        TreeSet<TankRecord> records = new TreeSet<TankRecord>();
        while (results.next()) {
            TankRecord newRecord = new TankRecord(this);
            newRecord.setComment(decode(results.getString("comment")));
            newRecord.setCost(results.getDouble("cost"));
            newRecord.setDistanceTraveled(results.getDouble("distance"));
            newRecord.setTotalDistance(results.getDouble("totaldistance"));
            newRecord.setId(results.getInt("id"));
            newRecord.setLiters(results.getDouble("liter"));
            newRecord.setTypeOfGas(decode(results.getString("typeOfGas")));
            newRecord.setMotorcycle(this.getMotorcycleById(results.getInt("motorcycleId")));
            newRecord.setStation(this.getStationById(results.getInt("stationId")));
            newRecord.setDate(results.getDate("date"));
            newRecord.setHasBeenSaved(true);
            newRecord.setChanged(false);
            records.add(newRecord);
        }
        return records;
    }

    public TreeSet<TankRecord> getRecordsByMotorcycleId(int id) throws SQLException {
        ResultSet results = Query("SELECT * FROM fuelrecords WHERE motorcycleid = " +id, true);
        TreeSet<TankRecord> records = new TreeSet<TankRecord>();
        while (results.next()) {
            TankRecord newRecord = new TankRecord(this);
            newRecord.setComment(decode(results.getString("comment")));
            newRecord.setCost(results.getDouble("cost"));
            newRecord.setDistanceTraveled(results.getDouble("distance"));
            newRecord.setTotalDistance(results.getDouble("totaldistance"));
            newRecord.setId(results.getInt("id"));
            newRecord.setLiters(results.getDouble("liter"));
            newRecord.setTypeOfGas(decode(results.getString("typeOfGas")));
            newRecord.setMotorcycle(this.getMotorcycleById(id));
            newRecord.setStation(this.getStationById(results.getInt("stationId")));
            newRecord.setDate(results.getDate("date"));
            newRecord.setHasBeenSaved(true);
            newRecord.setChanged(false);
            records.add(newRecord);
        }
        return records;
    }

    public TreeSet<Motorcycle> getMotorcycles() throws SQLException {
        ResultSet results = Query("SELECT * FROM motorcycles", true);
        TreeSet<Motorcycle> motors = new TreeSet<Motorcycle>();
        while (results.next()) {
            Motorcycle motor = new Motorcycle();
            motor.setBrand(decode(results.getString("brand")));
            motor.setType(decode(results.getString("type")));
            motor.setId(results.getInt("id"));
            motor.setCc(results.getInt("cc"));
            motor.setCilinders(results.getInt("cilinders"));
            motor.setTankSize(results.getDouble("tankSize"));
            motor.setWeight(results.getInt("weight"));
            for (Expense expense : getExpensesByMotorId(motor)){
                motor.addExpense(expense);
            }
            motor.setHasBeenSaved(true);
            motors.add(motor);
        }
        return motors;
    }

    public TreeSet<Station> getStations() throws SQLException {
        ResultSet results = Query("SELECT * FROM stations", true);
        TreeSet<Station> stations = new TreeSet<Station>();
        while (results.next()) {
            Station station = new Station();
            station.setLocation(decode(results.getString("location")));
            station.setName(decode(results.getString("name")));
            station.setId(results.getInt("id"));
            station.setHasBeenSaved(true);
            stations.add(station);
        }
        return stations;
    }

    public TreeSet<Category> getCategories() throws SQLException {
        ResultSet results = Query("SELECT * FROM categories ORDER BY name asc", true);
        TreeSet<Category> categories = new TreeSet<Category>();
        while (results.next()) {
            Category category = new Category(this);
            category.setName(decode(results.getString("name")));
            category.setId(results.getInt("id"));
            category.setHasBeenSaved(true);
            categories.add(category);
        }
        return categories;
    }

    public Motorcycle getMotorcycleById(int id) throws SQLException {
        ResultSet results = Query("SELECT * FROM motorcycles WHERE id = " + id, true);
        results.next();
        Motorcycle motor = new Motorcycle();
        motor.setBrand(decode(results.getString("brand")));
        motor.setType(decode(results.getString("type")));
        motor.setId(results.getInt("id"));
        motor.setHasBeenSaved(true);
        return motor;
    }

    public Station getStationById(int id) throws SQLException {
        ResultSet results = Query("SELECT * FROM stations WHERE id = " + id, true);
        results.next();
        Station station = new Station();
        station.setLocation(decode(results.getString("location")));
        station.setName(decode(results.getString("name")));
        station.setId(results.getInt("id"));
        return station;
    }

    public Set<Expense> getExpensesByMotorId(Motorcycle motor) throws SQLException {
        ResultSet results = Query("SELECT * FROM expenses WHERE motorcycleid = " + motor.getId(), true);
        TreeSet<Expense> expenses = new TreeSet<Expense>();
        while (results.next()) {
            Expense expense = new Expense(this);
            expense.setType(decode(results.getString("type")));
            expense.setName(decode(results.getString("name")));
            expense.setBrand(decode(results.getString("brand")));
            expense.setDate(results.getDate("date"));
            expense.setCheckDate(results.getDate("checkdate"));
            expense.setCategory(this.getCategoryById(results.getInt("categoryId")));
            expense.setMotor(motor);
            expense.setId(results.getInt("id"));
            expense.setCosts(results.getDouble("costs"));
            expense.setTotalDistance(results.getDouble("totaldistance"));
            expense.setCheckTotalDistance(results.getDouble("checktotaldistance"));
            expense.setHasBeenSaved(true);
            expense.setChanged(false);
            if (expense.getCategory() == null){
                expense.delete();
            } else {
                expenses.add(expense);
            }
        }
        return expenses;
    }

    public Set<Expense> getExpensesByCategoryId(Category category) throws SQLException {
        ResultSet results = Query("SELECT * FROM expenses WHERE categoryid = " + category.getId(), true);
        TreeSet<Expense> expenses = new TreeSet<Expense>();
        while (results.next()) {
            Expense expense = new Expense(this);
            expense.setType(decode(results.getString("type")));
            expense.setName(decode(results.getString("name")));
            expense.setBrand(decode(results.getString("brand")));
            expense.setDate(results.getDate("date"));
            expense.setCheckDate(results.getDate("checkdate"));
            expense.setCategory(category);
            expense.setMotor(this.getMotorcycleById(results.getInt("motorcycleid")));
            expense.setId(results.getInt("id"));
            expense.setCosts(results.getDouble("costs"));
            expense.setTotalDistance(results.getDouble("totaldistance"));
            expense.setCheckTotalDistance(results.getDouble("checktotaldistance"));
            expense.setHasBeenSaved(true);
            expense.setChanged(false);
            expenses.add(expense);
        }
        return expenses;
    }

    public Expense getExpenseById(int id) throws SQLException {
        ResultSet results = Query("SELECT * FROM expenses WHERE id = " + id, true);
        results.next();
        Expense expense = new Expense(this);
        expense.setType(decode(results.getString("type")));
        expense.setName(decode(results.getString("name")));
        expense.setBrand(decode(results.getString("brand")));
        expense.setDate(results.getDate("date"));
        expense.setCheckDate(results.getDate("checkdate"));
        expense.setCategory(getCategoryById(results.getInt("categoryId")));
        expense.setMotor(this.getMotorcycleById(results.getInt("motorcycleid")));
        expense.setId(results.getInt("id"));
        expense.setCosts(results.getDouble("costs"));
        expense.setTotalDistance(results.getDouble("totaldistance"));
        expense.setCheckTotalDistance(results.getDouble("checktotaldistance"));
        expense.setHasBeenSaved(true);
        expense.setChanged(false);

        return expense;
    }

    public Category getCategoryById(int id) throws SQLException {
        ResultSet results = Query("SELECT * FROM categories WHERE id = " + id, true);
        Category category = null;
        if (results.next()){
            category = new Category(this);
            category.setName(decode(results.getString("name")));
            category.setId(results.getInt("id"));
            category.setHasBeenSaved(true);
        }

        return category;
    }

    /**
     * Executes a query, if it is a select statement, it will return the results, otherwise it returns null
     * @param query Query to execute
     * @param ret Should i return anything?
     * @return ResultSet or null
     * @throws SQLException
     */
    public ResultSet Query(String query, boolean ret) throws SQLException {
        Statement stmt;
        ResultSet rs = null;
        stmt = connection.createStatement();

        if (ret) {
            rs = stmt.executeQuery(query);
        } else {
            stmt.execute(query);
        }
        return rs;
    }

    public String encode(String message) {
        return message.replace("'", "''");
    }

    public String decode(String message) {
        return message;//.replace("''", "'");
    }

    private void createTables() throws SQLException {
        Query("create table FUELRECORDS(ID INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),LITER DOUBLE not null,COST DOUBLE not null,DISTANCE DOUBLE not null,STATIONID NUMERIC(5) not null,MOTORCYCLEID NUMERIC(5) not null,DATE DATE not null,COMMENT VARCHAR(60) not null,TYPEOFGAS VARCHAR(60) not null)", false);
        Query("create table MOTORCYCLES(ID INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),BRAND VARCHAR(60) not null,TYPE VARCHAR(60) not null,CC NUMERIC(5) not null,WEIGHT NUMERIC(5) not null,CILINDERS NUMERIC(5) not null,TANKSIZE DOUBLE not null)", false);
        Query("create table STATIONS(ID INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),NAME VARCHAR(60) not null,LOCATION VARCHAR(60) not null)", false);
        Query("create table VERSION (VERSION NUMERIC(5) not null)", false);
        Query("INSERT INTO version (version) VALUES (1)", false);
        updateVersion1to2();
        updateVersion2to3();
        updateVersion3to4();
        updateVersion4to5();
        updateVersion5to6();
    }

    private void updateVersion1to2() throws SQLException {
        Query("create table categories(ID INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),NAME VARCHAR(60) not null)", false);
        Query("create table Expenses(ID INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),NAME VARCHAR(60) not null,TYPE VARCHAR(60) not null,CATEGORYID NUMERIC(5) not null,MOTORCYCLEID NUMERIC(5) not null,DATE DATE not null)", false);
        Query("create table server (password VARCHAR(60) not null)", false);
        Query("INSERT INTO server (password) VALUES ('')", false);
        Query("update version set version = 2", false);
    }

    private void updateVersion2to3() throws SQLException {
        Query("ALTER TABLE Expenses ADD COLUMN costs DOUBLE not null default 0", false);
        Query("ALTER TABLE Expenses ADD COLUMN brand VARCHAR(60) not null default ''", false);
        Query("update version set version = 3", false);
    }

    private void updateVersion3to4() throws SQLException {
        Query("ALTER TABLE fuelrecords ADD COLUMN totaldistance DOUBLE not null default 0", false);
        Query("update version set version = 4", false);
    }
    private void updateVersion4to5() throws SQLException {
        Query("create table settings (defaultfueltype VARCHAR(60) not null,checkforupdates integer default 1,askwhensaving integer default 1)", false);
        Query("INSERT INTO settings (defaultfueltype,checkforupdates,askwhensaving) VALUES ('Euro 95',1,1)",false);
        Query("update version set version = 5", false);
    }
    private void updateVersion5to6() throws SQLException {
        Query("ALTER TABLE Expenses ADD COLUMN totaldistance DOUBLE not null default 0", false);
        Query("ALTER TABLE Expenses ADD COLUMN checktotaldistance DOUBLE not null default 0", false);
        Query("ALTER TABLE Expenses ADD COLUMN checkdate DATE", false);
        Query("update version set version = 6", false);
    }
    public void calculateTotalDistance(String message) throws SQLException{
        for (Motorcycle motor: getMotorcycles()){
            double totalDistance =0;
            String extra = message;
            boolean valid = false;
            
            JComboBox recordBox = new JComboBox(getRecordsByMotorcycleId(motor.getId()).toArray());
            boolean sure = true;
            while (!valid && sure){
                recordBox.setSelectedItem(null);
                JPanel returnPanel = new JPanel();
                returnPanel.setLayout(new BoxLayout(returnPanel,BoxLayout.Y_AXIS));
                if (extra != null){
                    JPanel extraPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                    extraPanel.add(new JLabel(extra));
                    returnPanel.add(extraPanel);
                }
                JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                labelPanel.add(new JLabel("Wat was de kilometerstand van uw "+motor.toString() +" bij"));
                returnPanel.add(labelPanel);
                JPanel tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                tempPanel.add(new JLabel("tankbeurt: "));
                tempPanel.add(recordBox);
                returnPanel.add(tempPanel);
                try {
                    totalDistance = Double.parseDouble(JOptionPane.showInputDialog(returnPanel));
                    valid = true;
                } catch (NumberFormatException e){
                    extra = "Uw invoer was ongeldig, voer aub een geldig getal in\n\n";
                } catch (NullPointerException n){
                    sure = false;
                }
            }
            if (sure){
                TankRecord knownRecord = (TankRecord)recordBox.getSelectedItem();
                knownRecord.setTotalDistance(totalDistance);
                ResultSet resultBefore = Query("SELECT id,distance FROM fuelrecords WHERE date < '"+knownRecord.getDate()+"' AND motorcycleid = "+ knownRecord.getMotorcycle().getId()+" ORDER BY date desc",true);
                ResultSet resultAfter = Query("SELECT id,distance FROM fuelrecords WHERE date > '"+knownRecord.getDate()+"' AND motorcycleid = "+ knownRecord.getMotorcycle().getId()+" ORDER BY date asc",true);
                Query("UPDATE fuelrecords set totaldistance = " + totalDistance +" WHERE id = " + knownRecord.getId(),false);
                double previousTotal = knownRecord.getTotalDistance();
                double previousDistance = knownRecord.getDistanceTraveled();
                while (resultBefore.next()){
                    previousTotal -= previousDistance;
                    Query("UPDATE fuelrecords set totaldistance = " + previousTotal +" WHERE id = " + resultBefore.getInt("id"),false);
                    previousDistance = resultBefore.getDouble("distance");
                }
                previousTotal = knownRecord.getTotalDistance();
                previousDistance = knownRecord.getDistanceTraveled();
                while (resultAfter.next()){
                    previousTotal += previousDistance;
                    Query("UPDATE fuelrecords set totaldistance = " + previousTotal +" WHERE id = " + resultAfter.getInt("id"),false);
                    previousDistance = resultAfter.getDouble("distance");
                }
            }
        }
    }
}
