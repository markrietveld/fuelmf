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

import fuel.Main;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import javax.swing.JOptionPane;

/**
 *
 * @author Mark
 */
public class VersionChecker {

    public static void doVersionCheck() throws IOException{
        doVersionCheck(true,true);
    }

    /**
     * Checks if the version in http://servem.student.utwente.nl/store/version.txt is greater than Main.VERSIONID
     * Can open a dialog to inform the user either way, gives the user the option to open the download website
     * (http://servem.student.utwente.nl/store/index.html) if there is a new version
     * @param notifySucces tells this method if it should show the user in case there is a new version
     * @param notifyFail tells this method if it should show the user in case there isn't new version
     * @throws java.io.IOException
     */
    public static boolean doVersionCheck(boolean notifySucces,boolean notifyFail) throws IOException {
        boolean newVersion = false;
        URL url = new URL("http://servem.student.utwente.nl/store/version.txt");
        InputStream in = url.openStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        int latestVersion = Integer.parseInt(br.readLine());
        br.close();
        if (latestVersion > Main.VERSIONID){
            if (notifySucces){
                int sure = JOptionPane.showConfirmDialog(null, "Er is een nieuwere versie beschikbaar, op de downloadwebsite kunt de vernieuwingen lezen.\n\nwilt u de downloadwebsite openen?", "Nieuwe versie", JOptionPane.YES_NO_OPTION);
                if (sure == JOptionPane.YES_OPTION){
                    try {
                        java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
                        java.net.URI uri = new java.net.URI("http://servem.student.utwente.nl/store/index.html");
                        desktop.browse(uri);
                    } catch (Exception ef) {
                        JOptionPane.showMessageDialog(null, "Fout bij het openen van uw browser.\n\n U kunt de website handmatig openen via: http://servem.student.utwente.nl/store/", "Fout", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
            newVersion = true;
        } else {
            if (notifyFail){
                JOptionPane.showMessageDialog(null, "U gebruikt de nieuwste versie van Fuel", "Nieuwste versie", JOptionPane.INFORMATION_MESSAGE);
            }
        }
        return newVersion;
    }
}
