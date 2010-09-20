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
import fuel.lib.Station;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Date;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.TreeSet;
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
public class StationInputPanel extends JPanel {

    private JPanel stationPanelContainer;
    private JList stationJList;
    private DefaultListModel stationList;
    private View boss;
    private Database database;
    private Controller controller;
    private DateSelectionPanel dateSelectionPanel;

    public StationInputPanel(View boss, Database database) throws SQLException {
        this.boss = boss;
        this.database = database;
        dateSelectionPanel = new DateSelectionPanel(database,true);
        controller = new Controller(this);
        stationPanelContainer = new JPanel();
        stationJList = new JList();
        stationJList.addListSelectionListener(controller);
        stationList = new DefaultListModel();
        JPanel left = new JPanel(new BorderLayout());
        JButton newStationButton = new JButton("Nieuw tankstation");
        newStationButton.setActionCommand("NEWSTATION");
        newStationButton.addActionListener(controller);
        left.add(newStationButton,BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(stationJList);
        left.add(scroll);
        scroll.getVerticalScrollBar().setUnitIncrement(10);
        scroll.getHorizontalScrollBar().setUnitIncrement(10);
        JPanel dateAndStation = new JPanel(new BorderLayout());
        dateAndStation.add(dateSelectionPanel,BorderLayout.NORTH);
        dateAndStation.add(stationPanelContainer,BorderLayout.CENTER);
        JScrollPane scroll2 = new JScrollPane(dateAndStation);
        scroll2.getVerticalScrollBar().setUnitIncrement(10);
        scroll2.getHorizontalScrollBar().setUnitIncrement(10);
        JSplitPane divide = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, scroll2);
        setLayout(new BorderLayout());
        add(divide, BorderLayout.CENTER);
        setVisible(true);
    }

    public void refreshStationRecords() throws SQLException {
        stationPanelContainer.removeAll();
        stationPanelContainer.revalidate();
        stationPanelContainer.repaint();
        stationList.clear();
        TreeSet stations = database.getStations();
        Iterator it = stations.iterator();
        while (it.hasNext()) {
            stationList.addElement(it.next());
        }
        stationJList.setModel(stationList);
        if (stationJList != null && stationList.size() > 0) {
            stationJList.setSelectedIndex(0);
        }
    }

    public void newStation() {
        stationPanelContainer.removeAll();
        try {
            StationPanel ss = new StationPanel(new Station(), database, boss,this);
            dateSelectionPanel.setParent(ss);
            stationPanelContainer.add(ss);
            stationPanelContainer.revalidate();
            stationPanelContainer.repaint();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Fout bij maken van nieuwe stationfiets", JOptionPane.ERROR_MESSAGE);
        }
    }
    public Date getStartDate(){
        return dateSelectionPanel.getStartDate();
    }

    public Date getEndDate(){
        return dateSelectionPanel.getEndDate();
    }


    private class Controller implements ListSelectionListener, ActionListener{

        private StationInputPanel p;
        public Controller(StationInputPanel p){
            this.p = p;
        }

        public void valueChanged(ListSelectionEvent e) {
            JList source = (JList) e.getSource();
            stationPanelContainer.removeAll();
            Station selected = (Station) source.getSelectedValue();
            if (selected != null) {
                try {
                    StationPanel ss = new StationPanel(selected, database, boss,p);
                    dateSelectionPanel.setParent(ss);
                    stationPanelContainer.add(ss);
                    stationPanelContainer.revalidate();
                    stationPanelContainer.repaint();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage(), "Stationfiets niet gevonden", JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equals("NEWSTATION")){
                newStation();
            }
        }
    }
}
