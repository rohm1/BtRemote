package org.rohm1.btserver;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Pointer extends JFrame {

	private static final long serialVersionUID = -851546267258038198L;

	private final GraphicsDevice defaultScreenDevice;

	private Image background;
	
	private final MyJP jp = new MyJP();
	private final MyJP2 jp2 = new MyJP2();

	public Pointer() {
		super();

		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		defaultScreenDevice = ge.getDefaultScreenDevice(); 

		this.setResizable(false);
		this.setUndecorated(true);

		this.getContentPane().add(jp);

		this.setVisible(true);
	}

	@Override
	public void setVisible(boolean visible) {
		if(visible) {
			final Robot robot;
			try {
				BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
				Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
						cursorImg, new Point(0, 0), "blank cursor");
				this.getContentPane().setCursor(blankCursor);
				
				if(this.isVisible())
					super.setVisible(false);

				robot = new Robot();
				Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
				background = robot.createScreenCapture( new Rectangle(0, 0, (int) d.getWidth(), (int) d.getHeight()) );


				defaultScreenDevice.setFullScreenWindow(this);
				this.validate();
				super.setVisible(true);
				
				jp.repaint();
				Cursor.getDefaultCursor();

			} catch (AWTException e) {}
		}
		else {
			defaultScreenDevice.setFullScreenWindow(null);
			Cursor.getDefaultCursor();
			super.setVisible(false);
		}
	}

	public void movePointer(int x, int y) {
		this.jp2.setLocation(x, y);
	}

	public class MyJP extends JPanel {

		private static final long serialVersionUID = -7167622795604685590L;

		public MyJP() {
			super();
			
			this.setSize(Toolkit.getDefaultToolkit().getScreenSize());

			this.add(jp2);
		}

		@Override
		public void paintComponent(Graphics g){
			super.paintComponent(g);

			g.drawImage(background, 0, 0, null);
		}
	}

	public class MyJP2 extends JPanel {

		private static final long serialVersionUID = 8452024953181366555L;

		public MyJP2() {
			super();
			
			this.setOpaque(false);
			this.setSize(20, 20);
		}

		@Override
		public void paintComponent(Graphics g){
			super.paintComponent(g);

			g.setColor(Color.GREEN);
			g.fillOval(0 , 0, 10, 10);
		}
	}

}
