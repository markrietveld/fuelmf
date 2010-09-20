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

import fuel.lib.CSVhelper;
import fuel.lib.Database;
import fuel.lib.JLabel;
import fuel.lib.JTextField;
import fuel.lib.Motorcycle;
import fuel.lib.TankRecord;
import fuel.lib.Station;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author Mark
 */
public class RecordInputPanel extends JPanel {

    private JPanel recordPanelContainer;
    private JList recordsJList;
    private DefaultListModel tankRecordList;
    private View boss;
    private Database database;
    private Controller controller;
    private JTextField searchField;

    public RecordInputPanel(View boss, Database database) throws SQLException {
        this.boss = boss;
        this.database = database;
        controller = new Controller();
        recordPanelContainer = new JPanel();
        recordsJList = new JList();
        recordsJList.addListSelectionListener(controller);
        tankRecordList = new DefaultListModel();

        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Klik hier voor opties v");
        menuBar.add(menu);
        JMenuItem newRecordButton = new JMenuItem("Nieuwe tankbeurt");
        newRecordButton.setActionCommand("NEWRECORD");
        newRecordButton.addActionListener(controller);
        JPanel left = new JPanel(new BorderLayout());


        menu.add(newRecordButton);

        JMenuItem importRecordsButton = new JMenuItem("Importeer tankbeurten");
        importRecordsButton.setActionCommand("IMPORTRECORDS");
        importRecordsButton.addActionListener(controller);
        menu.add(importRecordsButton);

        //left.add(topLeftPanel, BorderLayout.NORTH);
        left.add(menuBar, BorderLayout.NORTH);
        searchField = new JTextField("", 10);
        java.net.URL imgURL = getClass().getResource("/images/question.png");
        JLabel description;
        String toolTip = "Met dit invoerveld kunt u zoeken naar tankbeurten.\n" +
                "Het programma zoekt in de datum, het commentaar, het type brandstof, de motor en het tankstion.\n" +
                "U kunt meerdere zoekwoorden invullen, gescheiden door een spatie.\n" +
                "Het programma geeft dan alleen resultaten waar alle zoekwoorden in voorkomen.\n" +
                "De zoekwoorden zijn niet hoofdlettergevoelig";
        if (imgURL != null) {
            ImageIcon image = new ImageIcon(imgURL, "Uitleg");
            description = new JLabel(image);
        } else {
            description = new JLabel("?");
        }
        description.setToolTipText(toolTip);
        searchField.addKeyListener(new KeyListener() {

            public void keyTyped(KeyEvent e) {
                //do nothing
            }

            public void keyPressed(KeyEvent e) {
                //do nothing
            }

            public void keyReleased(KeyEvent e) {
                try {
                    refreshTankRecords();
                } catch (SQLException ex) {
                    Logger.getLogger(RecordInputPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        JPanel searchPanel = new JPanel(new BorderLayout());
        JPanel searchLabel = new JPanel();
        JLabel preSearch = new JLabel("Zoeken:");
        preSearch.setToolTipText(toolTip);
        searchLabel.add(preSearch);
        searchLabel.add(searchField);
        searchLabel.add(description);
        searchPanel.add(searchLabel, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(recordsJList);
        scroll.getVerticalScrollBar().setUnitIncrement(10);
        scroll.getHorizontalScrollBar().setUnitIncrement(10);
        searchPanel.add(scroll, BorderLayout.CENTER);
        left.add(searchPanel);

        JMenuItem exportRecordsButton = new JMenuItem("Export tankbeurten");
        exportRecordsButton.setActionCommand("EXPORTRECORDS");
        exportRecordsButton.addActionListener(controller);
        menu.add(exportRecordsButton);

        JMenuItem calcTotalItem = new JMenuItem("Bereken kilometerstanden aan\n de hand van gereden afstanden");
        calcTotalItem.setActionCommand("CALCTOTAL");
        calcTotalItem.addActionListener(controller);
        menu.add(calcTotalItem);

        JMenuItem clearItem = new JMenuItem("Verwijder alle tankbeurten");
        clearItem.setActionCommand("DELETEALLRECORDS");
        clearItem.addActionListener(controller);
        menu.add(clearItem);

        JScrollPane scroll2 = new JScrollPane(recordPanelContainer);
        scroll2.getVerticalScrollBar().setUnitIncrement(10);
        scroll2.getHorizontalScrollBar().setUnitIncrement(10);
        JSplitPane divide = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, scroll2);
        setLayout(new BorderLayout());
        add(divide, BorderLayout.CENTER);
        refreshTankRecords();
        setVisible(true);
    }

    public void refreshTankRecords() throws SQLException {
        recordPanelContainer.removeAll();
        recordPanelContainer.revalidate();
        recordPanelContainer.repaint();
        tankRecordList.clear();
        for (TankRecord record : database.getRecords()) {
            String searchString = searchField.getText().toLowerCase();
            boolean valid = true;
            String[] searches = searchString.split(" ");
            for (String search : searches) {
                if (!((search.length() == 0) ||
                        record.getMotorcycle().relates(search) ||
                        record.getStation().relates(search) ||
                        record.getComment().toLowerCase().contains(search) ||
                        record.toString().toLowerCase().contains(search) ||
                        record.getTypeOfGas().toLowerCase().contains(search))) {
                    valid = false;
                }
            }
            if (valid) {
                tankRecordList.addElement(record);
            }
        }
        recordsJList.setModel(tankRecordList);
        recordsJList.setModel(tankRecordList);
        if (recordsJList != null && tankRecordList.size() > 0) {
            recordsJList.setSelectedIndex(0);
        }
    }

    public void newRecord() {
        recordPanelContainer.removeAll();
        try {
            recordPanelContainer.add(new RecordPanel(new TankRecord(database), database, boss));
            recordPanelContainer.revalidate();
            recordPanelContainer.repaint();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Fout bij maken van nieuwe tankbeurt", JOptionPane.ERROR_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Fout bij maken van nieuwe tankbeurt", JOptionPane.ERROR_MESSAGE);
        }
    }

    private class Controller implements ListSelectionListener, ActionListener {

        public void valueChanged(ListSelectionEvent e) {
            JList source = (JList) e.getSource();
            recordPanelContainer.removeAll();
            TankRecord selected = (TankRecord) source.getSelectedValue();
            if (selected != null) {
                try {
                    recordPanelContainer.add(new RecordPanel(selected, database, boss));
                    recordPanelContainer.revalidate();
                    recordPanelContainer.repaint();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage(), "Tankbeurt niet gevonden", JOptionPane.ERROR_MESSAGE);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage(), "Tankbeurt niet gevonden", JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equals("NEWRECORD")) {
                int nmbMotorcycles = 0;
                int nmbStations = 0;
                try {
                    nmbMotorcycles = database.getMotorcycles().size();
                    nmbStations = database.getStations().size();
                } catch (SQLException ex) {
                }

                if (nmbMotorcycles == 0 || nmbStations == 0) {
                    JPanel errorPanel = new JPanel(new GridLayout(0, 1));
                    if (nmbMotorcycles == 0) {
                        errorPanel.add(new JLabel("Maak eerst een voertuig"));
                    }
                    if (nmbStations == 0) {
                        errorPanel.add(new JLabel("Maak eerst een tankstation"));
                    }
                    JOptionPane.showMessageDialog(boss, errorPanel, "Kan geen tankbeurt maken", JOptionPane.ERROR_MESSAGE);
                } else {
                    newRecord();
                }
            } else if (e.getActionCommand().equals("IMPORTRECORDS")) {
                TreeSet<Motorcycle> motors;
                TreeSet<Station> stations;
                try {
                    motors = database.getMotorcycles();
                    stations = database.getStations();
                    if (motors.size() == 0 || stations.size() == 0) {
                        JPanel errorPanel = new JPanel(new GridLayout(0, 1));
                        if (motors.size() == 0) {
                            errorPanel.add(new JLabel("Maak eerst een voertuig"));
                        }
                        if (stations.size() == 0) {
                            errorPanel.add(new JLabel("Maak eerst een tankstation"));
                        }
                        JOptionPane.showMessageDialog(boss, errorPanel, "Kan geen tankbeurt maken", JOptionPane.ERROR_MESSAGE);
                    } else {
                        ImportPanel importPanel = new ImportPanel(motors, stations);
                        if (JOptionPane.showConfirmDialog(boss, importPanel, "Bestand importeren", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                            try {
                                List<TankRecord> imports = CSVhelper.ReadFromFile(importPanel.getFileName(), importPanel.getMotor(), importPanel.getStation());
                                for (TankRecord tank : imports) {
                                    tank.toDatabase(database);
                                }
                                boss.refreshTankRecords();
                            } catch (FileNotFoundException ex) {
                                Logger.getLogger(RecordInputPanel.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (IOException ex) {
                                Logger.getLogger(RecordInputPanel.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }

                    }
                } catch (SQLException ex) {
                }

            } else if (e.getActionCommand().equals("EXPORTRECORDS")) {
                try {
                    ExportPanel exportPanel = new ExportPanel();
                    if (JOptionPane.showConfirmDialog(boss, exportPanel, "Tankbeurten exporteren", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {

                        List<TankRecord> writeList = new ArrayList<TankRecord>();
                        String extra = "";
                        if (exportPanel.getEverything()) {
                            extra = "motorcycleid";
                        } else {
                            extra = "WHERE motorcycleid = " + exportPanel.getMotor().getId() + " AND stationid = " + exportPanel.getStation().getId();
                        }
                        ResultSet result = database.Query("SELECT * FROM fuelrecords " + extra + " ORDER BY date asc", true);
                        while (result.next()) {
                            TankRecord record = new TankRecord(database);
                            record.setDate(result.getDate("date"));
                            record.setCost(result.getDouble("cost"));
                            record.setComment(result.getString("comment"));
                            record.setId(result.getInt("id"));
                            record.setLiters(result.getDouble("liter"));
                            record.setMotorcycle(database.getMotorcycleById(result.getInt("motorcycleid")));
                            record.setStation(database.getStationById(result.getInt("stationid")));
                            record.setTypeOfGas(result.getString("typeofgas"));
                            record.setDistanceTraveled(result.getDouble("distance"));
                            record.setTotalDistance(result.getDouble("totaldistance"));
                            writeList.add(record);
                        }
                        CSVhelper.WriteToFile(exportPanel.getFileName(), writeList);
                    }
                } catch (FileNotFoundException ex) {
                    JOptionPane.showMessageDialog(null, "Ongeldig bestand ingesteld", "Fout", JOptionPane.ERROR_MESSAGE);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, "Schrijven naar bestand mislukt", "Fout", JOptionPane.ERROR_MESSAGE);
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(null, "Ophalen van gegevens mislukt", "Fout", JOptionPane.ERROR_MESSAGE);
                }
            } else if (e.getActionCommand().equals("CALCTOTAL")) {
                try {
                    database.calculateTotalDistance("LET OP: deze actie is niet omkeerbaar.\n Bestaande kilometerstanden zullen worden overschreven \n\n");
                    boss.refreshTankRecords();
                } catch (SQLException ex) {
                    Logger.getLogger(RecordInputPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (e.getActionCommand().equals("DELETEALLRECORDS")) {
                if (JOptionPane.showConfirmDialog(boss, "Weet u zeker dat u alle tankbeurten van al uw voertuigen wilt verwijderen?", "Bevestiging", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    if (JOptionPane.showConfirmDialog(boss, "Weet u heel zeker dat u alle tankbeurten van al uw voertuigen wilt verwijderen?\n Deze actie is niet omkeerbaar!", "Bevestiging", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                        try {
                            for (TankRecord record : database.getRecords()) {
                                record.delete(database);
                            }
                            boss.refreshTankRecords();
                        } catch (SQLException ex) {
                            Logger.getLogger(RecordInputPanel.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        }
    }

    private class ImportPanel extends JPanel {

        private JComboBox motorSelect;
        private JComboBox stationSelect;
        private JTextField filePath;

        public ImportPanel(Set<Motorcycle> motors, Set<Station> stations) {
            setLayout(new BorderLayout());
            JPanel first = new JPanel(new MigLayout());
            first.add(new JLabel("Tankbeurten importeren"), "wrap");
            first.add(new JLabel("Selecteer voertuig: "));
            motorSelect = new JComboBox(motors.toArray());
            first.add(motorSelect, "wrap");
            stationSelect = new JComboBox(stations.toArray());
            first.add(new JLabel("Selecteer tankstation: "));
            first.add(stationSelect, "wrap");
            first.add(new JLabel("Bestand: "));
            filePath = new JTextField("");
            filePath.setEditable(false);

            JButton browseButton = new JButton("Selecteer bestand");
            browseButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.addChoosableFileFilter(new FileFilterCsv());
                    if (fileChooser.showDialog(null, "Selecteer bestand") == JFileChooser.APPROVE_OPTION) {
                        File file = fileChooser.getSelectedFile();
                        filePath.setText(fileChooser.getCurrentDirectory() + File.separator + file.getName());
                    }
                }
            });
            first.add(browseButton, "wrap");
            add(first, BorderLayout.CENTER);
            add(filePath, BorderLayout.SOUTH);
        }

        public String getFileName() {
            return filePath.getText();
        }

        public Motorcycle getMotor() {
            return (Motorcycle) motorSelect.getSelectedItem();
        }

        public Station getStation() {
            return (Station) stationSelect.getSelectedItem();
        }
    }

    private class ExportPanel extends JPanel {

        private JComboBox motorSelect;
        private JComboBox stationSelect;
        private JCheckBox everything;
        private JTextField filePath;

        public ExportPanel() throws SQLException {
            setLayout(new BorderLayout());
            JPanel first = new JPanel(new MigLayout());
            first.add(new JLabel("Tankbeurten exporteren"), "wrap");
            everything = new JCheckBox("Exporteer alle tankbeurten");
            everything.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    setComboBoxes(!getEverything());
                }
            });
            everything.setSelected(true);
            first.add(everything, "wrap");

            first.add(new JLabel("Alleen van voertuig: "));
            motorSelect = new JComboBox(database.getMotorcycles().toArray());
            motorSelect.setEnabled(!everything.isSelected());
            first.add(motorSelect, "wrap");

            first.add(new JLabel("Alleen van tankstation: "));
            stationSelect = new JComboBox(database.getStations().toArray());
            stationSelect.setEnabled(!everything.isSelected());
            first.add(stationSelect, "wrap");

            first.add(new JLabel("Bestand: "));
            filePath = new JTextField("");
            filePath.setEditable(false);

            JButton browseButton = new JButton("Selecteer bestand");
            browseButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.addChoosableFileFilter(new FileFilterCsv());
                    fileChooser.setSelectedFile(new File("tankbeurten export.csv"));
                    if (fileChooser.showDialog(null, "Selecteer bestand") == JFileChooser.APPROVE_OPTION) {
                        File file = fileChooser.getSelectedFile();
                        filePath.setText(fileChooser.getCurrentDirectory() + File.separator + file.getName());
                    }
                }
            });
            first.add(browseButton, "wrap");
            add(first, BorderLayout.CENTER);
            add(filePath, BorderLayout.SOUTH);
        }

        public void setComboBoxes(boolean enabled) {
            motorSelect.setEnabled(enabled);
            stationSelect.setEnabled(enabled);
        }

        public String getFileName() {
            return filePath.getText();
        }

        public Motorcycle getMotor() {
            return (Motorcycle) motorSelect.getSelectedItem();
        }

        public Station getStation() {
            return (Station) stationSelect.getSelectedItem();
        }

        public boolean getEverything() {
            return everything.isSelected();
        }
    }

    private class FileFilterCsv extends FileFilter {

        public String getDescription() {
            return ".csv files";
        }

        public boolean accept(File pathname) {
            return pathname.isDirectory() || pathname.getName().toLowerCase().endsWith(".csv");
        }
    }
}
