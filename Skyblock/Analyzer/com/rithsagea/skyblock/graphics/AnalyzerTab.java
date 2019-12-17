package com.rithsagea.skyblock.graphics;

import javax.swing.JPanel;
import javax.swing.JTextArea;

public class AnalyzerTab extends JPanel {
	
	private static final long serialVersionUID = -9066713440294107459L;
	private JTextArea dummyArea;
	
	public AnalyzerTab() {
		dummyArea = new JTextArea("hi\n hi\n  hi\n   hi");
		
		add(dummyArea);
	}
}
