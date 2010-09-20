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
package fuel.gui;

import fuel.lib.Database;
import fuel.lib.JTextField;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import main.DatepickerPopup;
import sub.DateSelectionEvent;
import sub.DateSelectionListener;

/**
 *
 * @author Mark
 */
public class DateSelectionPanel extends JPanel {

    private DatepickerPopup startDate;
    private DatepickerPopup endDate;
    private Database database;
    private Controller controller;
    private Date start;
    private Date end;
    private JButton resetButton;
    private JButton applyButton;
    private StatsContainer parent;

    public DateSelectionPanel(Database database,boolean wantApplyButton) {
        Calendar cal = Calendar.getInstance();
        Date date = new Date(cal.getTimeInMillis());
        start = date;
        end = date;
        this.database = database;
        controller = new Controller();
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File("lib/tinyCalendar.gif"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        startDate = new DatepickerPopup();
        startDate.setSelectedDate(start);
        startDate.setSelectionCommand("START");
        startDate.getLook().setShowWeeksBar(true);
        startDate.addDateSelectionListener(controller);
        startDate.setDisplayEditable(false);
        startDate.setOpenCalendarButton(img);
        endDate = new DatepickerPopup();
        endDate.setSelectedDate(end);
        endDate.setSelectionCommand("END");
        endDate.getLook().setShowWeeksBar(true);
        endDate.addDateSelectionListener(controller);
        endDate.setDisplayEditable(false);
        endDate.setOpenCalendarButton(img);
        resetDates();
        //setLayout(new MigLayout());
        setBorder(BorderFactory.createTitledBorder("Selecteer de periode voor statistieken"));
        add(new JLabel("Begin: "));
        add(startDate);
        add(new JLabel("Eind: "));
        add(endDate, "wrap");
        if (wantApplyButton){
            applyButton = new JButton("Toepassen");
            applyButton.setActionCommand("APPLY");
            applyButton.addActionListener(controller);            
            add(applyButton);
        }
        resetButton = new JButton("Reset");
        resetButton.setActionCommand("RESET");
        resetButton.addActionListener(controller);
        add(resetButton);
        resetDates();

    }

    private void resetDates() {
        try {
            Date newStartDate = null;
            Date newEndDate = null;
            ResultSet result = database.Query("SELECT MIN(date) FROM fuelrecords", true);
            result.next();
            if (result.getDate(1) != null) {
                newStartDate = result.getDate(1);
                result = database.Query("SELECT MAX(date) FROM fuelrecords", true);
                result.next();
                newEndDate = result.getDate(1);                
            }
            result = database.Query("SELECT MIN(date) FROM expenses", true);
            result.next();
            if (result.getDate(1) != null) {
                if (newStartDate == null || result.getDate(1).before(newStartDate)){
                    newStartDate = result.getDate(1);
                }
                result = database.Query("SELECT MAX(date) FROM expenses", true);
                result.next();
                if (newEndDate == null || result.getDate(1).after(newEndDate)){
                    newEndDate = result.getDate(1);
                }
            }
            if (newStartDate != null && newEndDate != null){
                startDate.setSelectedDate(newStartDate);
                endDate.setSelectedDate(newEndDate);
            } else {
                Calendar cal = Calendar.getInstance();
                startDate.setSelectedDate(cal.getTime());
                endDate.setSelectedDate(cal.getTime());
            }
            setDates();
            saveDates();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public Date getStartDate() {
        return start;
    }

    public Date getEndDate() {
        return end;
    }

    public void setParent(StatsContainer parent) {
        this.parent = parent;
    }

    public void setDates() {
        try {
            start = new Date(startDate.getSelectedDate().getTime());
            startDate.setBackground(Color.GREEN);
        } catch (Exception ex) {
            startDate.setBackground(Color.RED);
        }
        try {
            end = new Date(endDate.getSelectedDate().getTime());
            endDate.setBackground(Color.GREEN);
        } catch (Exception ex) {
            endDate.setBackground(Color.RED);
        }
        if (applyButton != null){
            applyButton.setEnabled(true);
        }


    }

    public void saveDates(){
        try {
            if (parent != null) {
                parent.fill();
                applyButton.setEnabled(false);
            }
        } catch (SQLException ex) {
            Logger.getLogger(DateSelectionPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private class Controller implements ActionListener, DateSelectionListener {

        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equals("RESET")) {
                resetDates();
            } else if (e.getActionCommand().equals("APPLY")) {
                saveDates();
            }
        }

        public void dateSelected(DateSelectionEvent evt) {
            setDates();
        }
    }
}
