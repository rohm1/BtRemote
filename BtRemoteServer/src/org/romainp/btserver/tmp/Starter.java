package org.romainp.btserver.tmp;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;


public class Starter extends JFrame implements ActionListener {
	
	JTextField txtf;
	BtServer bts = new BtServer();
	
	public Starter() {
		super();
		
		JPanel jp = new JPanel();
		jp.setLayout(new BorderLayout());
		JButton jbtn = new JButton("start server");
		jbtn.addActionListener(this);
		jp.add(jbtn, BorderLayout.NORTH);
		txtf = new JTextField();
		jp.add(txtf, BorderLayout.SOUTH);
		
		Container cp = this.getContentPane();
		cp.add(jp);
		
		int w = Toolkit.getDefaultToolkit().getScreenSize().width;
		int h = Toolkit.getDefaultToolkit().getScreenSize().height;
		int x = 500;
		int y = 200;
		this.setPreferredSize(new Dimension(x, y));
		this.setLocation((w-x)/2, (h-y)/2-100);
		this.setTitle("BtTest");
		this.pack();
		this.setVisible(true);
	}
	
//	public static void main(String[] args) {
//		new Starter();
//	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if(cmd.equals("start server")) {
			bts.init();
			if(bts.getSate())
				txtf.setText("server started. " + bts.devicesDiscovered.size() + " device(s) discovered.");
			else
				txtf.setText("unable to start server. is bluetooth enabled?");
		}
	}
}
