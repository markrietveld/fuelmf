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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

/**
 *
 * @author Mark
 */
public class CSVhelper {

    public static void WriteToFile(String filePath, List<TankRecord> records) throws IOException {
        File file = new File(filePath);
        Boolean yes = true;
        if (file.exists()){
            yes = JOptionPane.showConfirmDialog(null, "Het bestand bestaat al, wilt u het overschrijven?", "Bevestiging", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
        }
        if (yes){
            FileWriter fstream = new FileWriter(filePath);
            BufferedWriter  out = new BufferedWriter(fstream);
            out.write("Datum;Getankte liters;Gemaakte kosten;Gereden afstand;Type brandstof;Commentaar;Voertuig;Tankstation;kilometerstand\n");
            for (TankRecord record : records){
                String toWrite = record.getDate().toString()+";"+record.getLiters()+";"+record.getCost()+
                        ";"+record.getDistanceTraveled()+";"+record.getTypeOfGas()+
                        ";"+record.getComment()+";"+record.getMotorcycle().toString()+
                        ";"+record.getStation().toString()+
                        ";"+record.getTotalDistance()+"\n";
                out.write(toWrite.replace(".", ","));
            }
            out.flush();
            out.close();
            JOptionPane.showMessageDialog(null, "Aantal geëxporteerde tankbeurten: "+records.size() +"\nBestand: " + filePath);
        }
    }

    public static List<TankRecord> ReadFromFile(String filePath, Motorcycle motor, Station station) throws FileNotFoundException, IOException {
        List<TankRecord> result = new ArrayList<TankRecord>();
        FileReader input = new FileReader(filePath);
        BufferedReader bufRead = new BufferedReader(input);
        String line = bufRead.readLine();
        int lineCount = 0;
        int errorCount = 0;
        String errorString = "";
        boolean first = true;
        while (line != null) {
            String[] s = line.split(";");
            if (s.length != 0) {
                lineCount++;
                line = line.replace(',', '.');
                String[] lineSplit = line.split(";",-2);
                String dateString = FormatDateString(lineSplit[0]);
                try {
                    TankRecord record = new TankRecord(null);
                    if (dateString == null) {
                        if (!first) {
                            errorString += line + " (Datum niet goed)\n";
                            errorCount++;
                        } else {
                            lineCount--;
                        }
                    } else {
                        record.setDate(Date.valueOf(dateString));
                        record.setLiters(Double.parseDouble(lineSplit[1]));
                        record.setCost(Double.parseDouble(lineSplit[2]));
                        record.setDistanceTraveled(Double.parseDouble(lineSplit[3]));
                        record.setTypeOfGas(lineSplit[4]);
                        record.setMotorcycle(motor);
                        record.setStation(station);
                        if (lineSplit.length > 4){
                            record.setComment(lineSplit[5]);
                        }
                        if (lineSplit.length > 8 && lineSplit[6].length() > 0){
                            record.setTotalDistance(Double.parseDouble(lineSplit[8]));
                        } else {
                            record.setTotalDistance(0);
                        }
                        result.add(record);
                    }
                } catch (NumberFormatException ne) {
                    errorString += line + " (Liters/afstand/kosten is geen geldig getal)\n";
                    errorCount++;
                } catch (ArrayIndexOutOfBoundsException e) {
                    errorString += line + " (Type brandstof is niet ingevuld)\n";
                    errorCount++;
                }
            }
            line = bufRead.readLine();
            first = false;
        }
        if (errorCount > 0) {
            String messageString = "";
            if (lineCount > errorCount) {
                messageString = "Er zijn " + errorCount + " regels niet ingeladen wegens fouten.\n";
                messageString += "De overige " + (lineCount - errorCount) + " regels zijn succesvol ingeladen";
                messageString += "\n\nFouten:\n" + errorString + "\n\nWilt u de ingeladen tankbeurten toch opslaan?";
                if (JOptionPane.showConfirmDialog(null, messageString, "Fouten", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
                    result.clear();
                }
            } else {
                messageString = "Alle " + lineCount + " regels konden niet worden ingeladen wegens fouten";
                messageString += "\n\nFouten:\n" + errorString;
                JOptionPane.showMessageDialog(null, messageString, "Fouten", JOptionPane.ERROR_MESSAGE);
            }

        } else if (lineCount > 0) {
            String messageString = "Alle " + lineCount + " regels zijn succesvol ingeladen";
            JOptionPane.showMessageDialog(null, messageString, "Succes", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null, "Het bestand bevat geen tankbeurten");
        }
        bufRead.close();
        return result;
    }

    public static String FormatDateString(String dateString) {
        String[] dateSplit = null;
        String result;
        if (dateString.contains("-")) {
            dateSplit = dateString.split("-");
        } else if (dateString.contains("/")) {
            dateSplit = dateString.split("/");
        }
        try {
            if (dateSplit[0].length() == 4) {
                result = dateSplit[0] + "-" + dateSplit[1] + "-" + dateSplit[2];
            } else if (dateSplit[2].length() == 4) {
                result = dateSplit[2] + "-" + dateSplit[1] + "-" + dateSplit[0];
            } else {
                result = null;
            }
        } catch (NullPointerException n) {
            result = null;
        }
        return result;
    }
}
