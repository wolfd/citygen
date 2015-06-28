package com.wolfd.citygen.util;

import javax.swing.JOptionPane;

public class Popup {
	static public boolean confirm(String question, String title){
		if(JOptionPane.showConfirmDialog(null, question, title, JOptionPane.OK_CANCEL_OPTION) == 0){
			return true;
		}
		return false;
	}
}
