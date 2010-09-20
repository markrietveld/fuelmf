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
import fuel.lib.Motorcycle;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Date;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.TreeSet;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author Mark
 */
public class MotorInputPanel extends JPanel {

    private JPanel motorPanelContainer;
    private JList motorJList;
    private DefaultListModel motorList;
    private View boss;
    private Database database;
    private Controller controller;
    private DateSelectionPanel dateSelectionPanel;

    public MotorInputPanel(View boss, Database database) throws SQLException {
        this.boss = boss;
        this.database = database;
        controller = new Controller(this);
        motorPanelContainer = new JPanel();
        motorJList = new JList();
        motorJList.addListSelectionListener(controller);
        motorList = new DefaultListModel();
        JPanel left = new JPanel(new BorderLayout());
        JButton newMotorButton = new JButton("Nieuw voertuig");
        newMotorButton.setActionCommand("NEWMOTOR");
        newMotorButton.addActionListener(controller);
        left.add(newMotorButton, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(motorJList);
        left.add(scroll);
        scroll.getVerticalScrollBar().setUnitIncrement(10);
        scroll.getHorizontalScrollBar().setUnitIncrement(10);

        JPanel dateAndMotor = new JPanel(new BorderLayout());
        dateAndMotor.setLayout(new BoxLayout(dateAndMotor,BoxLayout.Y_AXIS));
        dateSelectionPanel = new DateSelectionPanel(database,true);
        dateAndMotor.add(dateSelectionPanel);
        dateAndMotor.add(motorPanelContainer);
        JScrollPane scroll2 = new JScrollPane(dateAndMotor);
        scroll2.getVerticalScrollBar().setUnitIncrement(10);
        scroll2.getHorizontalScrollBar().setUnitIncrement(10);

        JSplitPane divide = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, scroll2);
        setLayout(new BorderLayout());
        add(divide, BorderLayout.CENTER);
        setVisible(true);
    }

    public void refreshMotorRecords() throws SQLException {
        motorPanelContainer.removeAll();
        motorPanelContainer.revalidate();
        motorPanelContainer.repaint();
        motorList.clear();
        TreeSet motors = database.getMotorcycles();
        Iterator it = motors.iterator();
        while (it.hasNext()) {
            motorList.addElement(it.next());            
        }
        motorJList.setModel(motorList);
        if (motorJList != null && motorList.size() > 0) {
            motorJList.setSelectedIndex(0);
        }
    }

    public void newMotor() {
        motorPanelContainer.removeAll();
        try {
            MotorPanel mm = new MotorPanel(new Motorcycle(), database, boss,this);
            dateSelectionPanel.setParent(mm);
            motorPanelContainer.add(mm);
            motorPanelContainer.revalidate();
            motorPanelContainer.repaint();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Fout bij maken van nieuw voertuig", JOptionPane.ERROR_MESSAGE);
        }
    }

    public Date getStartDate(){
        return dateSelectionPanel.getStartDate();
    }

    public Date getEndDate(){
        return dateSelectionPanel.getEndDate();
    }

    private class Controller implements ListSelectionListener, ActionListener {

        private MotorInputPanel p;
        public Controller(MotorInputPanel p){
            this.p = p;
        }

        public void valueChanged(ListSelectionEvent e) {
            JList source = (JList) e.getSource();
            motorPanelContainer.removeAll();
            Motorcycle selected = (Motorcycle) source.getSelectedValue();
            if (selected != null) {
                try {       
                    MotorPanel mm = new MotorPanel(selected, database, boss,p);
                    dateSelectionPanel.setParent(mm);
                    motorPanelContainer.add(mm);
                    motorPanelContainer.revalidate();
                    motorPanelContainer.repaint();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage(), "Voertuig niet gevonden", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        }

        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equals("NEWMOTOR")) {
                newMotor();
            }
        }
    }
}
