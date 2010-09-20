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

import fuel.lib.*;
import info.clearthought.layout.TableLayout;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author Mark
 */
public class MotorPanel extends JPanel implements StatsContainer{

    private Motorcycle motor;
    private JTextField brandField;
    private JTextField typeField;
    private JTextField ccField;
    private JTextField weightField;
    private JTextField cilindersField;
    private JTextField tankSizeField;
    private JButton saveButton;
    private JButton cancelButton;
    private JButton deleteButton;
    private Controller controller;
    private boolean changed;
    private Database database;
    private View view;
    private JPanel statsContainer;
    private MotorInputPanel parent;

    public MotorPanel(Motorcycle motor, Database database, View view, MotorInputPanel parent) throws SQLException {
        this.motor = motor;
        this.database = database;
        this.changed = false;
        this.view = view;
        this.parent = parent;
        controller = new Controller();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        brandField = new JTextField(10);
        brandField.addKeyListener(controller);
        typeField = new JTextField(10);
        typeField.addKeyListener(controller);
        ccField = new JTextField(10);
        ccField.addKeyListener(controller);
        weightField = new JTextField(10);
        weightField.addKeyListener(controller);
        cilindersField = new JTextField(10);
        cilindersField.addKeyListener(controller);
        tankSizeField = new JTextField(10);
        tankSizeField.addKeyListener(controller);
        saveButton = new JButton("Voertuig opslaan");
        saveButton.setActionCommand("SAVE");
        saveButton.addActionListener(controller);
        cancelButton = new JButton("Verandering niet opslaan");
        cancelButton.setActionCommand("DISCARD");
        cancelButton.addActionListener(controller);
        deleteButton = new JButton("Verwijder voertuig");
        deleteButton.addActionListener(controller);
        deleteButton.setActionCommand("DELETE");
        fill();
    }

    public View getView() {
        return view;
    }

    public void fill() throws SQLException {
        removeAll();   
        JPanel container = new JPanel(new BorderLayout());
        double size[][] = {{0.25, TableLayout.FILL},
            {TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL}};

        JPanel inputs = new JPanel(new TableLayout(size));
        brandField.setText(motor.getBrand());        
        if (!motor.hasBeenSaved()) {
            brandField.setBackground(Color.RED);
        } else {
            brandField.setBackground(Color.GREEN);
        }
        inputs.add(new JLabel("Merk: "), "0,0");
        inputs.add(brandField, "1,0");
        //inputs.add(brandPanel);

        typeField.setText(motor.getType());        
        if (!motor.hasBeenSaved()) {
            typeField.setBackground(Color.RED);
        } else {
            typeField.setBackground(Color.GREEN);
        }
        inputs.add(new JLabel("Type: "), "0,1");
        inputs.add(typeField, "1,1");
        //inputs.add(typePanel);

        ccField.setText("" + motor.getCc());
        ccField.setBackground(Color.GREEN);

        inputs.add(new JLabel("Cilinderinhoud: "), "0,2");
        inputs.add(ccField, "1,2");
        //ccPanel.add(new JLabel("cc"));
        //inputs.add(ccPanel);

        weightField.setText("" + motor.getWeight());        
        weightField.setBackground(Color.GREEN);

        inputs.add(new JLabel("Gewicht: "), "0,3");
        inputs.add(weightField, "1,3");
        //weightPanel.add(new JLabel("kg"));
        //inputs.add(weightPanel);

        cilindersField.setText("" + motor.getCilinders());        
        cilindersField.setBackground(Color.GREEN);

        inputs.add(new JLabel("Aantal Cilinders: "), "0,4");
        inputs.add(cilindersField, "1,4");
        //inputs.add(cilinderPanel);

        tankSizeField.setText("" + motor.getTankSize());        
        tankSizeField.setBackground(Color.GREEN);

        inputs.add(new JLabel("Tank grootte: "), "0,5");
        inputs.add(tankSizeField, "1,5");
        //tankSizePanel.add(new JLabel("liter"));
        //inputs.add(tankSizePanel);
        if (!motor.isChanged() && motor.hasBeenSaved()) {
            saveButton.setEnabled(false);
        }    
        if (!motor.isChanged() && motor.hasBeenSaved()) {
            cancelButton.setEnabled(false);
        }
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        if (motor.hasBeenSaved()) {            
            buttonPanel.add(deleteButton);
        }
        container.add(inputs);
        container.add(buttonPanel, BorderLayout.SOUTH);
        container.setBorder(BorderFactory.createEtchedBorder());
        add(container);
        if (motor.hasBeenSaved()) {
            ResultSet results = database.Query("SELECT avg(liter)," + //1
                    "avg(cost)," + //2
                    "avg(distance)," + //3
                    "max(distance)," + //4
                    "(SUM(distance)/SUM(liter))," + //5
                    "min(distance/liter)," + //6
                    "max(distance/liter)," + //7
                    "SUM(distance)," + //8
                    "SUM(liter)," + //9
                    "(SUM(cost)/SUM(distance))," + //10
                    "sum(cost)"+ //11
                    " FROM fuelrecords WHERE motorcycleId = " + motor.getId()+
                    "AND date >= '" + parent.getStartDate()+"'"+
                    "AND date <= '" + parent.getEndDate()+"'", true);
            results.next();
            statsContainer =  new JPanel(new BorderLayout());
            JPanel statBox = new JPanel(new GridLayout(3, 3));
            statBox.setBorder(BorderFactory.createEtchedBorder());
            
            DecimalFormat df = new DecimalFormat("###.##");
            DecimalFormat df2 = new DecimalFormat("###.###");
            List<JLabel> helpList = new ArrayList<JLabel>();
            //helpList.add(new JLabel("Gemiddeld getankt: " + df.format(results.getDouble("1")) + " liter"));
            helpList.add(new JLabel("Gemiddelde afstand: " + df.format(results.getDouble("3")) + " km"));
            helpList.add(new JLabel("Hoogste afstand: " + df.format(results.getDouble("4")) + " km"));
            helpList.add(new JLabel("Gemiddelde kosten: " + df.format(results.getDouble("2")) + " euro"));
            helpList.add(new JLabel("Gemiddeld verbruik: " + df.format(results.getDouble("5")) + " km/l"));
            helpList.add(new JLabel("Gemiddelde kosten/km: " + df2.format(results.getDouble("10")) + " euro"));
            helpList.add(new JLabel("Beste verbruik: " + df.format(results.getDouble("7")) + " km/l"));
            helpList.add(new JLabel("Slechtste verbruik: " + df.format(results.getDouble("6")) + " km/l"));
            helpList.add(new JLabel("Totaal gereden: " + df.format(results.getDouble("8")) + " km"));
            helpList.add(new JLabel("Totaal getankt: " + df.format(results.getDouble("9")) + " liter"));
            if (motor.getTankSize() > 0){
                helpList.add(new JLabel("Gemiddelde actieradius: " + df.format(results.getDouble("5")*motor.getTankSize()) + " km"));
                helpList.add(new JLabel("Minimale actieradius: " + df.format(results.getDouble("6")*motor.getTankSize()) + " km"));
                helpList.add(new JLabel("Maximale actieradius: " + df.format(results.getDouble("7")*motor.getTankSize()) + " km"));
            }
            for (JLabel lab : helpList){
                JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                panel.setBorder(BorderFactory.createLoweredBevelBorder());
                panel.add(lab);
                statBox.add(panel);
            }
            statsContainer.add(statBox);
            add(statsContainer);
            add(new JLabel(" "));
            add(new JLabel(" "));

            JPanel expensesAllPanel = new JPanel(new BorderLayout());
            expensesAllPanel.setBorder(BorderFactory.createEtchedBorder());
            JPanel catButtonPanel = new JPanel(new BorderLayout());
            JButton newExpenseButton = new JButton("Nieuwe categorie");
            newExpenseButton.setActionCommand("NEWCATEGORY");
            newExpenseButton.addActionListener(controller);
            catButtonPanel.add(newExpenseButton,BorderLayout.EAST);
            JLabel uitgaveLabel = new JLabel("Uitgaven:");
            uitgaveLabel.setFont(new Font("verdana", Font.BOLD, 12));
            catButtonPanel.add(uitgaveLabel,BorderLayout.WEST);

            expensesAllPanel.add(catButtonPanel, BorderLayout.NORTH);
            JPanel expensesContainer = new JPanel();
            expensesContainer.setLayout(new BoxLayout(expensesContainer,BoxLayout.Y_AXIS));
            for (Category cat : database.getCategories()) {
                expensesContainer.add(new CategoryPanel(cat, this));
            }
            expensesAllPanel.add(expensesContainer);
            add(expensesAllPanel);

            ResultSet results2 = database.Query("SELECT SUM(costs)," + //1
                    "max(costs)" + //2
                    " FROM expenses WHERE motorcycleId = " + motor.getId()+
                    "AND date >= '" + parent.getStartDate()+"'"+
                    "AND date <= '" + parent.getEndDate()+"'", true);
            results2.next();
            JPanel expenseStatsContainer =  new JPanel(new BorderLayout());
            JPanel expenseStatBox = new JPanel(new GridLayout(0, 3));
            expenseStatBox.setBorder(BorderFactory.createEtchedBorder());

            List<JLabel> expenseHelpList = new ArrayList<JLabel>();
            //helpList.add(new JLabel("Gemiddeld getankt: " + df.format(results.getDouble("1")) + " liter"));
            expenseHelpList.add(new JLabel("Totale uitgaven: " + df.format(results2.getDouble("1")) + " euro"));
            expenseHelpList.add(new JLabel("Duurste uitgave: " + df.format(results2.getDouble("2")) + " euro"));
            expenseHelpList.add(new JLabel("Brandstof omvat  " + df.format(results.getDouble("11")/(results.getDouble("11")+results2.getDouble("1"))*100) + "% van kosten"));
            expenseHelpList.add(new JLabel("Uitgaven omvatten  " + df.format(results2.getDouble("1")/(results.getDouble("11")+results2.getDouble("1"))*100) + "% van kosten"));
            expenseHelpList.add(new JLabel("Totale kosten (inc brandstof): " + df.format((results.getDouble("11")+results2.getDouble("1")))));
            expenseHelpList.add(new JLabel("Prijs per km (inc brandstof): " + df2.format((results.getDouble("11")+results2.getDouble("1"))/results.getDouble("8"))));
            for (JLabel lab : expenseHelpList){
                JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                panel.setBorder(BorderFactory.createLoweredBevelBorder());
                panel.add(lab);
                expenseStatBox.add(panel);
            }
            expenseStatsContainer.add(expenseStatBox);
            add(expenseStatsContainer);
        }
        setVisible(true);
        revalidate();
        repaint();
    }

    public Motorcycle getMotor() {
        return motor;
    }

    private class Controller implements ActionListener, KeyListener {

        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equals("SAVE")) {
                if (motor.isValid()) {
                    try {
                        Motorcycle testMotor = new Motorcycle();
                        testMotor.setBrand(motor.getBrand());
                        testMotor.setType(motor.getType());
                        TreeSet<Motorcycle> motors = database.getMotorcycles();
                        Motorcycle realMotor = null;
                        if (motor.getId() != -1) {
                            realMotor = database.getMotorcycleById(motor.getId());
                        }
                        if (!motors.contains(testMotor) || motor.getId() != -1 && realMotor.getBrand().equals(motor.getBrand()) && realMotor.getType().equals(motor.getType())) {
                            ResultSet result = database.Query("SELECT * FROM settings", true);
                            result.next();
                            boolean confirm = result.getInt("askwhensaving") == 1;
                            if (!confirm || JOptionPane.showConfirmDialog(null, "Weet u zeker dat u dit voertuig wilt opslaan?", "Bevestiging", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                                motor.toDatabase(database);
                                view.refreshTankRecords();
                            }
                        } else {
                            JOptionPane.showMessageDialog(null, "dit voertuig bestaat al", "Fout bij opslaan", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(null, ex.getMessage(), "Fout bij versturen", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "dit voertuig is niet geldig, vul alle velden in", "Fout", JOptionPane.ERROR_MESSAGE);
                }
            } else if (e.getActionCommand().equals("DISCARD")) {
                if (JOptionPane.showConfirmDialog(null, "Weet u zeker dat u niet op wilt slaan?", "Bevestiging", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    try {
                        view.refreshTankRecords();
                    } catch (SQLException ex) {
                    }
                }
            } else if (e.getActionCommand().equals("DELETE")) {
                if (JOptionPane.showConfirmDialog(null, "Weet u zeker dat u wilt verwijderen?\nAlle tankbeurten die met deze motor zijn gedaan zullen ook worden verwijderd.", "Bevestiging", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    try {
                        motor.delete(database);
                        view.refreshTankRecords();
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(null, ex.getMessage(), "Fout bij verwijderen", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else if (e.getActionCommand().equals("NEWCATEGORY")){
                Category cat = new Category(database);
                boolean written = false;
                int count = 1;
                while (!written){
                    cat.setName("Nieuwe categorie " + count);
                    try {
                        cat.toDatabase();
                        written = true;
                    } catch (SQLException ex) {
                        count++;
                    }
                }
                try {
                    fill();
                } catch (SQLException ex) {
                    Logger.getLogger(MotorPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        public void keyTyped(KeyEvent e) {
        }

        public void keyPressed(KeyEvent e) {
        }

        public void keyReleased(KeyEvent e) {

            JComponent source = (JComponent) e.getSource();
            if (source == brandField) {
                try {
                    String newBrand = brandField.getText();
                    if (newBrand.length() > 0) {
                        motor.setBrand(newBrand);
                        brandField.setBackground(Color.GREEN);
                    } else {
                        throw new Exception();
                    }
                } catch (Exception ex) {
                    brandField.setBackground(Color.RED);
                }
            } else if (source == typeField) {
                try {
                    String newType = typeField.getText();
                    if (newType.length() > 0) {
                        motor.setType(newType);
                        typeField.setBackground(Color.GREEN);
                    } else {
                        throw new Exception();
                    }
                } catch (Exception ex) {
                    typeField.setBackground(Color.RED);
                }
            } else if (source == ccField) {
                try {
                    int newCc = Integer.parseInt(ccField.getText());
                    if (newCc >= 0) {
                        motor.setCc(newCc);
                        ccField.setBackground(Color.GREEN);
                    } else {
                        throw new Exception();
                    }
                } catch (Exception ex) {
                    ccField.setBackground(Color.RED);
                }
            } else if (source == weightField) {
                try {
                    int newWeight = Integer.parseInt(weightField.getText());
                    if (newWeight >= 0) {
                        motor.setWeight(newWeight);
                        weightField.setBackground(Color.GREEN);
                    } else {
                        throw new Exception();
                    }
                } catch (Exception ex) {
                    weightField.setBackground(Color.RED);
                }
            } else if (source == cilindersField) {
                try {
                    int newValue = Integer.parseInt(cilindersField.getText());
                    if (newValue >= 0) {
                        motor.setCilinders(newValue);
                        cilindersField.setBackground(Color.GREEN);
                    } else {
                        throw new Exception();
                    }
                } catch (Exception ex) {
                    cilindersField.setBackground(Color.RED);
                }
            } else if (source == tankSizeField) {
                try {
                    Double newValue = Double.parseDouble(tankSizeField.getText());
                    if (newValue >= 0) {
                        motor.setTankSize(newValue);
                        tankSizeField.setBackground(Color.GREEN);
                    } else {
                        throw new Exception();
                    }
                } catch (Exception ex) {
                    tankSizeField.setBackground(Color.RED);
                }
            }
            if (!motor.isChanged() && motor.hasBeenSaved()) {
                saveButton.setEnabled(false);
                cancelButton.setEnabled(false);
            } else {
                saveButton.setEnabled(true);
                cancelButton.setEnabled(true);
            }
        }
    }
}
