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
import fuel.lib.Motorcycle;
import fuel.lib.Station;
import fuel.lib.TankRecord;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Date;
import java.sql.SQLException;

/**
 * This class communicates with one client.
 * It runs while the client is connected and shuts down after
 * @author Mark
 */
public class InputReceiver extends Receiver {

    /**
     * Constuctor for InputReceiver
     * @param socket the socket through which this class should communicate. This needs to be connected to the client before contruction
     * @param database the database to which this class should write incoming data
     * @param password the password the user has set. Will be checked with the clients password
     */
    public InputReceiver(Socket socket, Database database, String password,Main main) {
        super(socket,database,password,main);
    }

    /**
     * Communicates with the client using the protocol
     * Writes to the database if clients sends new entries
     */
    public void run() {
        try {
            out = new PrintWriter(socket.getOutputStream(), true);

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            sendMessage("DOYOURTHING");
            String responseLine;
            while ((responseLine = in.readLine()) != null) {
                String[] login = responseLine.split(";");
                if (login.length == 2 && login[0].endsWith("IMHERE") && login[1].equals(password) || login.length == 1 && password.equals("")) {

                    sendMessage("METOO");
                    main.addServermessage("Client " + socket.getInetAddress() + " is ingelogd op de mobiele interface");
                    while ((responseLine = in.readLine()) != null) {
                        if (responseLine.indexOf("DONE") != -1) {
                            break;
                        }
                        if (responseLine.startsWith("GETBIKES")) {
                            try {
                                boolean none = true;
                                for (Motorcycle motor : database.getMotorcycles()) {
                                    sendMessage("BIKE;" + motor.getBrand() + ";" + motor.getType() + ";" + motor.getId());
                                    none = false;
                                }
                                if (none){
                                    sendMessage("FAIL;no motorcycles");
                                } else {
                                    sendMessage("SUCCES");
                                }
                            } catch (SQLException ex) {
                                sendMessage("FAIL;No motorcycles");
                            }
                        } else if (responseLine.startsWith("GETSTATIONS")) {
                            try {
                                boolean none = true;
                                for (Station station : database.getStations()) {
                                    sendMessage("STATION;" + station.getName() + ";" + station.getLocation() + ";" + station.getId());
                                    none = false;
                                }
                                if (none){
                                    sendMessage("FAIL;no stations");
                                } else {
                                    sendMessage("SUCCES");
                                }
                            } catch (SQLException ex) {
                                sendMessage("FAIL;no stations");
                            }
                        } else if (responseLine.startsWith("RECORD")) {
                            String[] content = responseLine.split(";");
                            try {
                                TankRecord record = new TankRecord(database);
                                record.setDate(new Date(Long.parseLong(content[1])));
                                record.setMotorcycle(database.getMotorcycleById(Integer.parseInt(content[2])));
                                record.setStation(database.getStationById(Integer.parseInt(content[3])));
                                record.setLiters(Double.parseDouble(content[4]));
                                record.setCost(Double.parseDouble(content[5]));
                                record.setDistanceTraveled(Double.parseDouble(content[6]));
                                record.setTypeOfGas(content[7]);
                                record.setComment(content[8]);
                                record.toDatabase(database);
                                sendMessage("SUCCES");
                                main.addServermessage("Client " + socket.getInetAddress() + " heeft een nieuwe tankbeurt gemaakt met de mobiele interface");
                            } catch (SQLException ex) {
                                sendMessage("FAIL;writing to database failed");
                            } catch (Exception e) {
                                sendMessage("FAIL;Get it right fool");
                            }
                        } else if (responseLine.startsWith("STATION")) {
                            String[] content = responseLine.split(";");
                            try {
                                Station station = new Station();
                                station.setName(content[1]);
                                station.setLocation(content[2]);
                                station.toDatabase(database);
                                sendMessage("SUCCES");
                                main.addServermessage("Client " + socket.getInetAddress() + " heeft een nieuw tankstation gemaakt met de mobiele interface");
                            } catch (SQLException ex) {
                                sendMessage("FAIL;writing to database failed");
                            } catch (Exception e) {
                                sendMessage("FAIL;Get it right fool");
                            }
                        }
                    }
                } else {
                    main.addServermessage("Client " + socket.getInetAddress() + " probeerde in te loggen op de mobiele interface met een onjuist wachtwoord");
                }
            }


        } catch (IOException e) {}
    }
}
