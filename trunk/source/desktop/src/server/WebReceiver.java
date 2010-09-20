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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Mark
 */
public class WebReceiver extends Receiver {

    public static int TOTALDISTANCE = 0;
    public static int DISTANCE = 1;
    public static final String DATE_FORMAT = "EEE, d MMM yyyy hh:mm:ss z";
    public static final String INTERNAL_DATE_FORMAT = "yyyy-MM-dd";
    private final String successfulStation = "<html><body>Het tankstation is succesvol toegevoegd</body></html>";
    private final String failureStation = "<html><body>Toevoegen van het tankstation is mislukt</body></html>";
    private final String successfulRecord = "<html><body>De tankbeurt is succesvol toegevoegd</body></html>";
    private final String failureRecord = "<html><body>Toevoegen van de tankbeurt is mislukt</body></html>";
    private final String notANumber = "<html><body>Toevoegen van de tankbeurt is mislukt, de tankbeurt bevatte ongeldige getallen</body></html>";
    private final String stationForm = "<html><body><form action=\"\" method=\"get\">" +
            "Nieuw tankstation<BR>" +
            "Naam: <input type=\"text\" name=\"name\" value=\"\" size=\"20\"><br>" +
            "Plaats: <input type=\"text\" name=\"location\" value=\"\" size=\"20\"><br>" +
            "<input type=\"submit\" value=\"Submit\"></form></body></html>";
    private String internetAddress;
    public WebReceiver(Socket socket, Database database, String password, Main main) {
        super(socket, database, password, main);
        //Get the internet address from possible source
        try {            
            Socket sock = new Socket("servem.student.utwente.nl", 9909);
            in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            String responseLine = in.readLine();
            internetAddress = responseLine;
            if (internetAddress.startsWith("/")){
                    internetAddress = internetAddress.substring(1,internetAddress.length());
            }
        } catch (Exception ex) {
        }
        System.out.println(internetAddress);
    }

    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String responseLine;
            boolean done = false;
            String message = "";
            while (!done && (responseLine = in.readLine()) != null) {
                System.out.println("Response: " + responseLine);

                if (responseLine.contains("GET /" + password + "/station?")) {
                    int firstDelim = responseLine.indexOf("?");
                    int secondDelim = responseLine.indexOf(' ', firstDelim);
                    String response = responseLine.substring(firstDelim + 1, secondDelim);
                    String[] variables = response.split("\\&");
                    try {
                        Station newStation = new Station();
                        newStation.setLocation(variables[1].substring(variables[1].indexOf("=") + 1).replaceAll("\\+", " "));
                        newStation.setName(variables[0].substring(variables[0].indexOf("=") + 1).replaceAll("\\+", " "));
                        newStation.toDatabase(database);
                        message = addHeader(successfulStation);
                        main.addServermessage("Client " + socket.getInetAddress() + " heeft een nieuw tankstation gemaakt met de webinterface");
                    } catch (SQLException e) {
                        main.addServermessage("Client " + socket.getInetAddress() + " heeft een ongeldig tankstation aangemaakt");
                        message = addHeader(failureStation);
                    }
                } else if (responseLine.contains("GET /" + password + "/tankbeurt?")) {
                    int firstDelim = responseLine.indexOf("?");
                    int secondDelim = responseLine.indexOf(' ', firstDelim);
                    String response = responseLine.substring(firstDelim + 1, secondDelim);
                    String[] variables = response.split("\\&");
                    try {
                        TankRecord newRecord = new TankRecord(database);
                        newRecord.setDate(Date.valueOf(variables[0].substring(variables[0].indexOf("=") + 1).replaceAll("\\+", " ")));
                        newRecord.setMotorcycle(database.getMotorcycleById(Integer.parseInt(variables[1].substring(variables[1].indexOf("=") + 1).replaceAll("\\+", " "))));
                        newRecord.setStation(database.getStationById(Integer.parseInt(variables[2].substring(variables[2].indexOf("=") + 1).replaceAll("\\+", " "))));
                        newRecord.setLiters(Double.parseDouble(variables[3].substring(variables[3].indexOf("=") + 1).replaceAll("\\+", " ")));
                        newRecord.setCost(Double.parseDouble(variables[4].substring(variables[4].indexOf("=") + 1).replaceAll("\\+", " ")));
                        int choice = Integer.parseInt(variables[5].substring(variables[5].indexOf("=") + 1).replaceAll("\\+", " "));
                        Double distance = Double.parseDouble(variables[6].substring(variables[6].indexOf("=") + 1).replaceAll("\\+", " "));
                        if (choice == DISTANCE) {
                            newRecord.setDistanceTraveled(distance);
                            ResultSet result = database.Query("SELECT totaldistance FROM fuelrecords WHERE date <= '" + newRecord.getDate() + "' AND motorcycleid = " + newRecord.getMotorcycle().getId() + " AND NOT id = " + newRecord.getId() + " ORDER BY date desc,totaldistance desc", true);
                            if (result.next()) {
                                newRecord.setTotalDistance(result.getDouble("totaldistance") + newRecord.getDistanceTraveled());
                            } else {
                                newRecord.setTotalDistance(newRecord.getDistanceTraveled());
                            }
                        } else if (choice == TOTALDISTANCE) {
                            newRecord.setTotalDistance(distance);
                            ResultSet result = database.Query("SELECT totaldistance FROM fuelrecords WHERE date <= '" + newRecord.getDate() + "' AND motorcycleid = " + newRecord.getMotorcycle().getId() + " AND NOT id = " + newRecord.getId() + "ORDER BY date desc,totaldistance desc", true);
                            if (result.next()) {
                                newRecord.setDistanceTraveled(newRecord.getTotalDistance() - result.getDouble("totaldistance"));
                            } else {
                                newRecord.setDistanceTraveled(distance);
                            }
                        }
                        newRecord.setTypeOfGas(variables[7].substring(variables[7].indexOf("=") + 1).replaceAll("\\+", " "));
                        newRecord.setComment(variables[8].substring(variables[8].indexOf("=") + 1).replaceAll("\\+", " "));
                        newRecord.toDatabase(database);
                        message = addHeader(successfulRecord);
                        main.addServermessage("Client " + socket.getInetAddress() + " heeft een nieuwe tankbeurt gemaakt met de webinterface");
                    } catch (SQLException e) {
                        main.addServermessage("Client " + socket.getInetAddress() + " heeft een ongeldige tankbeurt aangemaakt");
                        message = addHeader(failureRecord);
                    } catch (NumberFormatException e) {
                        main.addServermessage("Client " + socket.getInetAddress() + " heeft een ongeldig getal opgegeven via de webinterface");
                        message = addHeader(notANumber);
                    }
                } else if (responseLine.contains("GET /" + password + "/station")) {
                    message = (addHeader(stationForm));
                    main.addServermessage("Client " + socket.getInetAddress() + " is ingelogd op de webinterface");
                } else if (responseLine.contains("GET /" + password + "/tankbeurt")) {
                    try {
                        message = addHeader(recordForm());
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                    main.addServermessage("Client " + socket.getInetAddress() + " is ingelogd op de webinterface");
                } else if (responseLine.contains("GET /")) {
                    String pw = null;
                    if (responseLine.contains("/"+password+"/")){
                        pw = password;
                    }
                    message = (addHeader(requestPasswordMessage(pw)));
                    main.addServermessage("Client " + socket.getInetAddress() + " vroeg een ongeldige pagina op via de webinterface");
                } else if (responseLine.length() == 0) {
                    done = true;
                }

            }
            out = new PrintWriter(socket.getOutputStream(), true);
            sendMessage(message);
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(WebReceiver.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private String addHeader(String message) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        return "HTTP/1.1 200 OK\n" +
                "date: " + sdf.format(cal.getTime()) + "\n" +
                "Server: Fuel " + Main.VERSION + "\n" +
                "Accept-Ranges: bytes\n" +
                "Connection: close\n" +
                "Content-Type: text/html\n\n" + message;
    }

    private String requestPasswordMessage(String password) {        
        String stationLink = "";
        String recordLink = "";
        if (internetAddress == null || password == null){
            internetAddress = "uw internet ip:";
            stationLink = "http://" + internetAddress + WebServer.SERVER_PORT + "/wachtwoord/station<BR>";
            recordLink = "http://" + internetAddress + WebServer.SERVER_PORT + "/wachtwoord/tankbeurt<BR>";
        } else {
            stationLink = "<A HREF=http://" + internetAddress +":"+ WebServer.SERVER_PORT + "/"+password+"/station>http://" + internetAddress +":"+ WebServer.SERVER_PORT + "/"+password+"/station</A><BR>";
            recordLink = "<A HREF=http://" + internetAddress +":"+ WebServer.SERVER_PORT + "/"+password+"/tankbeurt>http://" + internetAddress +":"+ WebServer.SERVER_PORT + "/"+password+"/tankbeurt</A><BR>";
        }
        return "<html>\n<body>\n Onjuist wachtwoord, of ongeldige link<BR>\n" +
                "Geldige links zijn:<BR>\n" +
                stationLink +
                recordLink +
                "(Vervang wachtwoord door het wachtwoord dat u ingevuld heeft in het hoofdprogramma)<BR>" +
                "</body>" +
                "</html>";
    }

    private String recordForm() throws SQLException {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(INTERNAL_DATE_FORMAT);
        String result;
        if (database.getMotorcycles().size() > 0 && database.getStations().size() > 0) {
            result = "<html><body><form action=\"\" method=\"get\">" +
                    "Nieuwe tankbeurt<BR>" +
                    "Datum: <input type=\"text\" name=\"date\" value=\"" + sdf.format(cal.getTime()) + "\" size=\"20\">*<br>" +
                    "<select name=\"motor\">";
            for (Motorcycle motor : database.getMotorcycles()) {
                result += "<option value=\"" + motor.getId() + "\">" + motor.toString() + "</option>";
            }
            result += "</select>*<BR> " +
                    "<select name=\"station\">";
            for (Station station : database.getStations()) {
                result += "<option value=\"" + station.getId() + "\">" + station.toString() + "</option>";
            }
            ResultSet result2 = database.Query("SELECT * FROM settings", true);
            result2.next();
            result += "</select>*<BR>" +
                    "Liters: <input type=\"text\" name=\"liter\" value=\"0.0\" size=\"20\">*<br>" +
                    "Kosten: <input type=\"text\" name=\"cost\" value=\"0.0\" size=\"20\">*<br>" +
                    "Ik wil invullen: <BR>" +
                    "<input type=\"radio\" name=\"inputtype\" value=\"" + DISTANCE + "\" checked=\"checked\"/> Gereden afstand<br />" +
                    "<input type=\"radio\" name=\"inputtype\" value=\"" + TOTALDISTANCE + "\" /> Kilometerstand<BR>" +
                    "Kilometers: <input type=\"text\" name=\"distance\" value=\"0.0\" size=\"20\">*<br>" +
                    "Type brandstof: <input type=\"text\" name=\"typeofgas\" value=\"" + result2.getString("defaultfueltype") + "\" size=\"20\"><br>" +
                    "Commentaar: <input type=\"text\" name=\"comment\" value=\"\" size=\"20\"><br>" +
                    "<input type=\"submit\" value=\"Submit\"></form></body></html>";

        } else {
            result = "<html><body>Maak eerst een tankstation en een voertuig</body></html>";
        }
        return result;
    }
}
