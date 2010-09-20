/*
 * Copyright 2009 Mark Rietveld
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
import java.net.Socket;
import java.sql.SQLException;

/**
 *
 * @author Mark
 */
public class InputServer extends Server{

    public static final int SERVER_PORT = 5831;

    public InputServer(Database database, Main main){
        super(database,main);
    }

    /**
     * Accepts new connections and starts a new instance of InputReceiver
     * for each client connection.
     */
    public void run(){
        try {
            database.Query("update server set password = '"+password+"'", false);
        } catch (SQLException ex) {ex.printStackTrace();}
        isStarted=true;
        try{
            serverSocket = new ServerSocket(SERVER_PORT);

            while(isStarted){
                try{
                    Socket clientSocket = serverSocket.accept();
                    InputReceiver inputReceiver = new InputReceiver(clientSocket, database,password,main);
                    Thread t = new Thread(inputReceiver);
                    t.start();
                }
                catch(Exception e){}
            }
            
        } catch (IOException e){}
    }
}
