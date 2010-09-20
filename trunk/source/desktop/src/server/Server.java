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

package server;

import fuel.Main;
import fuel.lib.Database;
import java.io.IOException;
import java.net.ServerSocket;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author Mark
 */
public abstract class Server implements Runnable{
    protected ServerSocket serverSocket;
    protected Database database;
    protected boolean isStarted;
    protected String password;
    protected Main main;

    public Server(Database database, Main main){
        this.database = database;
        this.main = main;
        String pass = "";
        try {
            ResultSet result = database.Query("SELECT * FROM server", true);
            result.next();
            pass = result.getString("password");
        } catch (SQLException ex) {}
        this.password = pass;
    }

    public String getPassword() {
        return password;
    }

    public void stop(){
        isStarted = false;
        try {
            serverSocket.close();
        } catch (IOException ex) {}
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isStarted() {
        return isStarted;
    }
}