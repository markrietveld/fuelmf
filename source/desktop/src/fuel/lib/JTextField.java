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


/**
 * @author Mark
 * javax.swing.JTextField has a method of storing and setting actionCommands,
 * but not for getting them. That's why I wrote this simple wrapper
 */
public class JTextField extends javax.swing.JTextField{
    private String ACom;

    public JTextField(String s,int i){
        super(s,i);
    }

    public JTextField(String s){
        super(s);
    }
    @Override
    public void setActionCommand(String ACom){
        this.ACom = ACom;
    }

    public String getActionCommand(){
        return ACom;
    }
}
