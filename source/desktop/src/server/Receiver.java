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
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * This class communicates with one client.
 * It runs while the client is connected and shuts down after
 * @author Mark
 */
public abstract class Receiver implements Runnable {

    public static int BADCLIENT = 0;
    public static int LOGGEDIN = 1;
    public static int NEWSTATION = 2;
    public static int NEWRECORD = 3;
    
    protected Socket socket;
    protected Database database;
    protected PrintWriter out;
    protected BufferedReader in;
    protected String password;
    protected Main main;
    private String otherIP;

    /**
     * Constuctor for InputReceiver
     * @param socket the socket through which this class should communicate. This needs to be connected to the client before contruction
     * @param database the database to which this class should write incoming data
     * @param password the password the user has set. Will be checked with the clients password
     */
    public Receiver(Socket socket, Database database, String password, Main main) {
        this.socket = socket;
        this.database = database;
        this.password = password;
        this.otherIP = socket.getInetAddress().toString();
        this.main = main;
    }

    /**
     * Sends the message over the outputstream to the client
     * Requires that the outputstream is connected
     * @param message the message that should be sent
     */
    public void sendMessage(String message){
        if (socket.isConnected()){
            out.println(message);
            out.flush();
        }
    }

    public String getOtherIP(){
        return otherIP;
    }
}
