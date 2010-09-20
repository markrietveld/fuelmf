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

import fuel.Main;
import fuel.lib.Database;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import server.Server;

/**
 *
 * @author Mark
 */
public class ServerPanel extends JPanel implements Observer{

    private Database database;
    private JPasswordField passwordField;
    private Controller controller;
    private JButton stopButton;
    private JButton startButton;
    private Main main;
    private JTextArea messages;

    public ServerPanel(Database database, Main main, List<Server> servers) {
        this.main = main;
        main.addObserver(this);
        controller = new Controller(servers);
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        JPanel inputs = new JPanel();//new GridLayout(0, 2));
        container.add(inputs);
        this.database = database;
        inputs.add(new JLabel("Wachtwoord voor de server:"));
        passwordField = new JPasswordField(servers.get(0).getPassword(),20);
        passwordField.setEnabled(!servers.get(0).isStarted());
        inputs.add(passwordField);

        JPanel buttonPanel = new JPanel();
        startButton = new JButton("Start server");
        startButton.setActionCommand("START");
        startButton.addActionListener(controller);
        startButton.setEnabled(!servers.get(0).isStarted());
        buttonPanel.add(startButton);

        stopButton = new JButton("Stop server");
        stopButton.setActionCommand("STOP");
        stopButton.addActionListener(controller);
        stopButton.setEnabled(servers.get(0).isStarted());
        buttonPanel.add(stopButton);
        container.add(buttonPanel);
        setLayout(new BorderLayout());
        add(container,BorderLayout.NORTH);
        messages = new JTextArea(60,40);
        messages.setEditable(false);
        JPanel messagesPanel = new JPanel(new BorderLayout());
        messagesPanel.add(new JLabel("Berichten van de server:"),BorderLayout.NORTH);
        messagesPanel.add(new JScrollPane(messages),BorderLayout.CENTER);
        add(messagesPanel,BorderLayout.CENTER);
        messages.setText(main.getServermessages());
        setVisible(true);
    }

    public void update(Observable o, Object arg) {
        messages.setText(main.getServermessages());
    }

    private class Controller implements ActionListener {

        private List<Server> servers;

        public Controller(List<Server> servers) {
            this.servers = servers;
        }

        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equals("START")) {
                for (Server server : servers){
                    server.setPassword(passwordField.getText());
                    Thread t = new Thread(server);
                    t.start();
                }
                stopButton.setEnabled(true);
                startButton.setEnabled(false);
                passwordField.setEnabled(false);
                main.addServermessage("Server gestart");
            } else if (e.getActionCommand().equals("STOP")) {
                for (Server server : servers){
                    server.stop();
                }
                stopButton.setEnabled(false);
                startButton.setEnabled(true);
                passwordField.setEnabled(true);
                main.addServermessage("Server gestopt");
            }
        }
    }
}
