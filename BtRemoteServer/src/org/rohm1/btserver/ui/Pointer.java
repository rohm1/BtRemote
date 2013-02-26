package org.rohm1.btserver.ui;

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

public class Pointer extends JFrame{

	private static final long serialVersionUID = 2790065389143217609L;

	private final GraphicsDevice defaultScreenDevice;

	private Image background;

	private MyJP jp = new MyJP();
	JPanel ptr;

	public Pointer() {
		super();

		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		this.defaultScreenDevice = ge.getDefaultScreenDevice();

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

				this.defaultScreenDevice.setFullScreenWindow(this);
				this.validate();
				super.setVisible(true);

				jp.repaint();
				Cursor.getDefaultCursor();

			} catch (AWTException e) {}
		}
		else {
			this.defaultScreenDevice.setFullScreenWindow(null);
			Cursor.getDefaultCursor();
			super.setVisible(false);
		}
	}

	public void movePointer(int x, int y) {
		this.ptr.setLocation(x, y);
	}

	public class MyJP extends JPanel {

		private static final long serialVersionUID = -2084315096430318750L;

		public MyJP() {
			super();

			this.setSize(Toolkit.getDefaultToolkit().getScreenSize());

			ptr = new JPanel();
			ptr.setSize(20,20);
			ptr.setBackground(Color.GREEN);
			this.add(ptr);
		}

		@Override
		public void paintComponent(Graphics g){
			super.paintComponent(g);

			g.drawImage(background, 0, 0, null);
		}
	}

}
