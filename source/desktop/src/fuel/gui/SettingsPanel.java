/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fuel.gui;

import fuel.lib.Database;
import fuel.lib.JTextField;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author Mark
 */
public class SettingsPanel extends JPanel{
    private Controller controller;
    private Database database;
    private JTextField defaultFuelType;
    private JCheckBox checkVersion;
    private JCheckBox askWhenSaving;

    public SettingsPanel(Database database) {
        controller = new Controller();
        this.database = database;
        setLayout(new MigLayout());
        add(new JLabel("Instellingen"),"wrap");
        defaultFuelType = new JTextField("",20);
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel("Standaard brandstof type: "));
        panel.add(defaultFuelType);
        add(panel,"wrap");
        checkVersion = new JCheckBox("Controleer automatisch of er een nieuwe versie beschikbaar is");
        add(checkVersion,"wrap");
        askWhenSaving = new JCheckBox("Vraag om bevestiging bij opslaan van voertuigen, tankstations en tankbeurten");
        add(askWhenSaving,"wrap");
        JButton saveButton = new JButton("Opslaan");
        saveButton.setActionCommand("SAVE");
        saveButton.addActionListener(controller);
        JButton discardButton = new JButton("Reset");
        discardButton.setActionCommand("DISCARD");
        discardButton.addActionListener(controller);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(saveButton);
        buttonPanel.add(discardButton);
        add(buttonPanel);
        discard();
        setVisible(true);
    }

    private void discard(){
        try {
            ResultSet result = database.Query("SELECT * FROM settings", true);
            result.next();
            boolean temp;
            int tempint = result.getInt("askwhensaving");
            temp = tempint == 1;
            askWhenSaving.setSelected(temp);
            tempint = result.getInt("checkforupdates");
            temp = tempint == 1;
            checkVersion.setSelected(temp);
            defaultFuelType.setText(result.getString("defaultfueltype"));
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

    }

    private class Controller implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equals("SAVE")) {
                int savingInt = 0;
                if (askWhenSaving.isSelected()){
                    savingInt = 1;
                }
                int checkInt = 0;
                if (checkVersion.isSelected()){
                    checkInt = 1;
                }
                try {
                    database.Query("UPDATE settings SET defaultfueltype = '" + defaultFuelType.getText() + "', checkforupdates = " + checkInt + ", askwhensaving = " + savingInt, false);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            } else if (e.getActionCommand().equals("DISCARD")) {
                discard();
            }
        }
    }
}
