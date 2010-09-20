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

import fuel.lib.Category;
import fuel.lib.Expense;
import fuel.lib.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author Mark
 */
public class CategoryPanel extends JPanel {

    private Category category;
    private Controller controller;
    private MotorPanel motorPanel;
    private JPanel expenseContainer;
    private List<JButton> saveButtons;
    private JButton newExpenseButton;

    public CategoryPanel(Category category, MotorPanel motorPanel) throws SQLException {
        this.category = category;
        this.motorPanel = motorPanel;
        saveButtons = new ArrayList<JButton>();
        controller = new Controller();
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(category.getName()), BorderFactory.createLoweredBevelBorder()));
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        newExpenseButton = new JButton("Nieuwe uitgave");
        newExpenseButton.setActionCommand("NEWEXPENSE");
        newExpenseButton.addActionListener(controller);
        buttonPanel.add(newExpenseButton);

        JButton renameCat = new JButton("Hernoem categorie");
        renameCat.setActionCommand("RENAMECAT");
        renameCat.addActionListener(controller);
        buttonPanel.add(renameCat);

        JButton deleteCat = new JButton("Verwijder categorie");
        deleteCat.setActionCommand("DELETECAT");
        deleteCat.addActionListener(controller);
        buttonPanel.add(deleteCat);
        add(buttonPanel, BorderLayout.NORTH);

        expenseContainer = new JPanel(new MigLayout());
        expenseContainer.add(new JLabel("Datum"));
        expenseContainer.add(new JLabel("Tellerstand"));
        expenseContainer.add(new JLabel("Onderdeel"));
        expenseContainer.add(new JLabel("Merk"));
        expenseContainer.add(new JLabel("Type"));
        expenseContainer.add(new JLabel("Kosten"), "wrap");
        for (Expense expense : motorPanel.getMotor().getExpensesByCategoryId(category.getId())) {
            addExpense(expense);
        }
        add(expenseContainer, BorderLayout.CENTER);

        JPanel expenseStatsPanel = new JPanel();
        int total = 0;
        for (Expense exp : motorPanel.getMotor().getExpensesByCategoryId(category.getId())) {
            total += exp.getCosts();
        }
        expenseStatsPanel.add(new JLabel("Totaal in deze categorie: " + total + " euro"));
        add(expenseStatsPanel, BorderLayout.SOUTH);
        refreshSaveButtons();
    }

    private void refreshSaveButtons() {
        Expense expense;
        for (JButton button : saveButtons) {
            String[] split = button.getActionCommand().split(";");
            expense = motorPanel.getMotor().getExpenseById(Integer.parseInt(split[1]));
            button.setEnabled((expense.isChanged() || !expense.hasBeenSaved()) && expense.isValid());
        }
    }

    private void addExpense(Expense expense) {
        if (!expense.hasBeenSaved()) {
            newExpenseButton.setEnabled(false);
        }
        JTextField dateField = new JTextField(expense.getDate().toString(), 7);
        dateField.setActionCommand("DATE;" + expense.getId());
        dateField.addKeyListener(controller);
        dateField.setBackground(Color.GREEN);
        expenseContainer.add(dateField);

        JTextField totalDistanceField = new JTextField(expense.getTotalDistance() + "", 6);
        totalDistanceField.setActionCommand("TOTALDISTANCE;" + expense.getId());
        totalDistanceField.addKeyListener(controller);
        totalDistanceField.setBackground(Color.GREEN);
        expenseContainer.add(totalDistanceField);

        JTextField nameField = new JTextField(expense.getName(), 15);
        nameField.setActionCommand("NAME;" + expense.getId());
        nameField.addKeyListener(controller);
        if (expense.getName().length() > 0) {
            nameField.setBackground(Color.GREEN);
        } else {
            nameField.setBackground(Color.RED);
        }
        expenseContainer.add(nameField);

        JTextField brandField = new JTextField(expense.getBrand(), 15);
        brandField.setActionCommand("BRAND;" + expense.getId());
        brandField.addKeyListener(controller);
        if (expense.getBrand().length() > 0) {
            brandField.setBackground(Color.GREEN);
        } else {
            brandField.setBackground(Color.RED);
        }
        expenseContainer.add(brandField);

        JTextField typeField = new JTextField(expense.getType(), 15);
        typeField.setActionCommand("TYPE;" + expense.getId());
        typeField.addKeyListener(controller);
        if (expense.getType().length() > 0) {
            typeField.setBackground(Color.GREEN);
        } else {
            typeField.setBackground(Color.RED);
        }
        expenseContainer.add(typeField);

        JTextField costsField = new JTextField(expense.getCosts() + "", 6);
        costsField.setActionCommand("COSTS;" + expense.getId());
        costsField.addKeyListener(controller);
        costsField.setBackground(Color.GREEN);
        expenseContainer.add(costsField);

        JButton saveButton = new JButton("✓");
        saveButton.setActionCommand("SAVEEXPENSE;" + expense.getId());
        saveButton.addActionListener(controller);
        saveButtons.add(saveButton);

        JButton deleteButton = new JButton("X");
        deleteButton.setActionCommand("DELETEEXPENSE;" + expense.getId());
        deleteButton.addActionListener(controller);

        JButton settingsButton = new JButton("Waarschuwing");
        if (expense.getCheckDate() != null || expense.getCheckTotalDistance() > 0.0){
            settingsButton.setForeground(Color.BLUE);
        }
        settingsButton.setActionCommand("SETTINGS;" + expense.getId());
        settingsButton.addActionListener(controller);

        JPanel expenseButtonPanel = new JPanel();
        expenseButtonPanel.setLayout(new BoxLayout(expenseButtonPanel, BoxLayout.X_AXIS));
        expenseButtonPanel.add(saveButton);
        expenseButtonPanel.add(deleteButton);
        expenseButtonPanel.add(settingsButton);
        expenseContainer.add(expenseButtonPanel, "wrap");
    }

    private class Controller implements ActionListener, KeyListener {

        public void actionPerformed(ActionEvent e) {

            if (e.getActionCommand().equals("NEWEXPENSE")) {
                Expense exp = new Expense(category.getDatabase());
                exp.setCategory(category);
                exp.setMotor(motorPanel.getMotor());
                motorPanel.getMotor().addExpense(exp);
                try {
                    motorPanel.fill();
                } catch (SQLException ex) {
                }
            } else if (e.getActionCommand().startsWith("SAVEEXPENSE")) {
                String[] split = e.getActionCommand().split(";");
                try {
                    Expense exp = motorPanel.getMotor().getExpenseById(Integer.parseInt(split[1]));
                    exp.toDatabase();
                    motorPanel.fill();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            } else if (e.getActionCommand().startsWith("DELETEEXPENSE")) {
                int sure = JOptionPane.showConfirmDialog(expenseContainer, "Weet u zeker dat u deze uitgave wilt verwijderen?", "Uitgave verwijderen", JOptionPane.YES_NO_OPTION);
                if (sure == JOptionPane.YES_OPTION) {
                    String[] split = e.getActionCommand().split(";");
                    try {
                        if (motorPanel.getMotor().hasBeenSaved()) {
                            motorPanel.getMotor().getExpenseById(Integer.parseInt(split[1])).delete();
                        }
                        motorPanel.getMotor().removeExpense(motorPanel.getMotor().getExpenseById(Integer.parseInt(split[1])));
                        motorPanel.fill();
                    } catch (SQLException ex) {
                    }
                }
            } else if (e.getActionCommand().equals("RENAMECAT")) {
                JPanel renamePanel = new JPanel(new BorderLayout());
                JTextField newName = new JTextField(category.getName());
                renamePanel.add(new JLabel("Nieuwe naam: "), BorderLayout.NORTH);
                renamePanel.add(newName, BorderLayout.CENTER);
                int sure = JOptionPane.showConfirmDialog(expenseContainer, renamePanel, "Categorienaam wijzigen", JOptionPane.OK_CANCEL_OPTION);
                if (sure == JOptionPane.OK_OPTION) {
                    //check if the field wasn't empty
                    if (newName.getText().length() == 0) {
                        JOptionPane.showMessageDialog(expenseContainer, "Ongeldige naam");
                    } else {
                        try {
                            //check if this category didn't already exist
                            boolean clean = true;
                            for (Category cat : category.getDatabase().getCategories()) {
                                if (cat.getName().equalsIgnoreCase(newName.getText())) {
                                    clean = false;
                                }
                            }
                            if (clean) {
                                category.setName(newName.getText());
                                category.toDatabase();
                                motorPanel.fill();
                            }
                        } catch (SQLException ex) {
                            Logger.getLogger(CategoryPanel.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            } else if (e.getActionCommand().equals("DELETECAT")) {
                try {
                    int size = category.getDatabase().getExpensesByCategoryId(category).size();
                    String message = "Weet u zeker dat u deze categorie wilt verwijderen?";
                    if (size > 0) {
                        message += "\n\nLet op: alle uitgaven die bij deze categorie horen zullen ook worden verwijderd, ook die bij andere motoren";
                    }
                    if (JOptionPane.showConfirmDialog(expenseContainer, message, "Verwijderen", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                        category.delete();
                        motorPanel.fill();
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(CategoryPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (e.getActionCommand().startsWith("SETTINGS")) {
                String[] split = e.getActionCommand().split(";");
                Expense expense = motorPanel.getMotor().getExpenseById(Integer.parseInt(split[1]));
                JPanel panel = new JPanel(new BorderLayout());
                panel.add(new JLabel("Selecteer wanneer u een waarschuwing wilt ontvangen:"), BorderLayout.NORTH);
                JPanel options = new JPanel(new MigLayout());
                JCheckBox distanceCB = new JCheckBox("Kilometerstand: ");
                JTextField distance = new JTextField("", 10);
                if (expense.getCheckTotalDistance() > 0.0){
                    distance.setText(expense.getCheckTotalDistance()+"");
                    distanceCB.setSelected(true);
                }
                options.add(distanceCB);
                options.add(distance, "wrap");
                JCheckBox dateCB = new JCheckBox("Datum: ");
                Calendar cal = Calendar.getInstance();
                JTextField date = new JTextField(new Date(cal.getTimeInMillis()).toString(), 10);
                if (expense.getCheckDate() != null){
                    date.setText(expense.getCheckDate().toString());
                    dateCB.setSelected(true);
                }
                options.add(dateCB);
                options.add(date, "wrap");
                panel.add(options);
                if (JOptionPane.showConfirmDialog(null, panel, "Instellingen voor waarschuwingen", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                    try {                        
                        if (distanceCB.isSelected()) {
                            expense.setCheckTotalDistance(Double.parseDouble(distance.getText()));
                        } else {
                            expense.setCheckTotalDistance(0.0);
                        }
                        if (dateCB.isSelected()) {
                            expense.setCheckDate(Date.valueOf(date.getText()));
                        } else {
                            expense.setCheckDate(null);
                        }
                        expense.toDatabase();
                        motorPanel.fill();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "De ingestelde waarschuwing was ongeldig en is niet opgeslagen.", "Fout", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }

        public void keyTyped(KeyEvent e) {
        }

        public void keyPressed(KeyEvent e) {
        }

        public void keyReleased(KeyEvent e) {
            JTextField source = (JTextField) e.getSource();
            String[] split = source.getActionCommand().split(";");
            Expense expense = motorPanel.getMotor().getExpenseById(Integer.parseInt(split[1]));
            if (split[0].equals("DATE")) {
                try {
                    expense.setDate(Date.valueOf(source.getText()));
                    source.setBackground(Color.GREEN);
                } catch (IllegalArgumentException i) {
                    source.setBackground(Color.RED);
                }
            } else if (split[0].equals("NAME")) {
                expense.setName(source.getText());
                if (source.getText().length() > 0) {
                    source.setBackground(Color.GREEN);
                } else {
                    source.setBackground(Color.RED);
                }
            } else if (split[0].equals("BRAND")) {
                expense.setBrand(source.getText());
                if (source.getText().length() > 0) {
                    source.setBackground(Color.GREEN);
                } else {
                    source.setBackground(Color.RED);
                }
            } else if (split[0].equals("TYPE")) {
                expense.setType(source.getText());
                if (source.getText().length() > 0) {
                    source.setBackground(Color.GREEN);
                } else {
                    source.setBackground(Color.RED);
                }
            } else if (split[0].equals("COSTS")) {
                try {
                    expense.setCosts(Double.parseDouble(source.getText()));
                    source.setBackground(Color.GREEN);
                } catch (NumberFormatException ne) {
                    source.setBackground(Color.RED);
                }
            } else if (split[0].equals("TOTALDISTANCE")) {
                try {
                    expense.setTotalDistance(Double.parseDouble(source.getText()));
                    source.setBackground(Color.GREEN);
                } catch (NumberFormatException ne) {
                    source.setBackground(Color.RED);
                }
            }
            refreshSaveButtons();
        }
    }
}
