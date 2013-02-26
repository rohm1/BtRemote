package org.rohm1.btserver;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

public class Screen {
	
	private final OutputStream os;
	private final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
	private int w = 600;
	private int h = 800;
	private final MonitorThread mt;
	private BufferedImage mouseCursor;

	public Screen(OutputStream os, int w, int h) {
		this.os = os;
		this.w = w > screen.width ? screen.width : w;
		this.h = h > screen.height ? screen.height : h;
		
		try {
			this.mouseCursor = ImageIO.read(new File("imgs/cursor.png"));
		} catch (IOException e) {}

		this.mt = new MonitorThread();
		this.mt.start();
	}
	
	public void stop() {
		this.mt._stop();
	}
	
	protected class MonitorThread extends Thread {
		
		private boolean blinker = true;

		public void _stop() {
			this.blinker  = false;
		}
		
		public void run() {
			Robot robot;
			try {
				robot = new Robot();
				Point location;
				int x, y;
				BufferedImage screenshot;
				
				while(this.blinker) {
					try {
						location = MouseInfo.getPointerInfo().getLocation();
						
						x = location.x - w / 2 < 0 ? 0 : (location.x + w / 2 > screen.width ? screen.width - w : location.x - w / 2);
						y = location.y - h / 2 < 0 ? 0 : (location.y + h / 2 > screen.height ? screen.height - h : location.y - h / 2);
						
						screenshot = robot.createScreenCapture( new Rectangle(x, y, w, h) );
						Graphics2D g2D = (Graphics2D) screenshot.getGraphics();
						g2D.drawImage(mouseCursor, location.x - x, location.y - y, null);
						g2D.dispose();
						
						ByteArrayOutputStream myBAOS = new ByteArrayOutputStream();
						JPEGImageEncoder myEncoder = JPEGCodec.createJPEGEncoder(myBAOS);
						JPEGEncodeParam param = myEncoder.getDefaultJPEGEncodeParam(screenshot);
						param.setQuality(0.1f, true);
						myEncoder.encode(screenshot, param);
						
						byte[] myByteArray = myBAOS.toByteArray();
						ByteArrayInputStream myIS = new ByteArrayInputStream(myByteArray);
						
						String s = "s:" + myIS.available();
						os.write(s.getBytes(), 0, s.getBytes().length);
						os.flush();
						
						byte[] buffer = new byte[2048];
						int len;
						while((len = myIS.read(buffer)) != -1)
								os.write(buffer, 0, len);
						os.flush();
				        
					} catch (Exception e) {}
						
					try {
						Thread.sleep(150);
					} catch (InterruptedException e) {}
				}
				
			} catch (AWTException e) {};
		}
		
	}

}
