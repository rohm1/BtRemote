package org.rohm1.btserver.ui;

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
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageOutputStream;

import com.sun.imageio.plugins.jpeg.JPEGImageWriter;

public class Screen {
	
	private final OutputStream os;
	private final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
	private int w = 600;
	private int h = 800;
	private final MonitorThread mt;
	private BufferedImage mouseCursor;

	public Screen(OutputStream os, int w, int h) {
		this.os = os;
		this.w = (int) (0.8 * (w > screen.width ? screen.width : w));
		this.h = (int) (0.8 * (h > screen.height ? screen.height : h));
		
		try {
			this.mouseCursor = ImageIO.read(getClass().getResource("/cursor.png"));
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
				Graphics2D g2D;
				
				ByteArrayOutputStream myBAOS;
				JPEGImageWriter imageWriter;
				ImageOutputStream ios;
				IIOMetadata imageMetaData;
				ByteArrayInputStream myIS;
				
				byte[] buffer;
				int length;
				
				while(this.blinker) {
					try {
						location = MouseInfo.getPointerInfo().getLocation();
						
						x = location.x - w / 2 < 0 ? 0 : (location.x + w / 2 > screen.width ? screen.width - w : location.x - w / 2);
						y = location.y - h / 2 < 0 ? 0 : (location.y + h / 2 > screen.height ? screen.height - h : location.y - h / 2);
						
						screenshot = robot.createScreenCapture( new Rectangle(x, y, w, h) );
						g2D = (Graphics2D) screenshot.getGraphics();
						g2D.drawImage(mouseCursor, location.x - x, location.y - y, null);
						g2D.dispose();
						
						myBAOS = new ByteArrayOutputStream();
						imageWriter = (JPEGImageWriter) ImageIO.getImageWritersBySuffix("jpeg").next();
						ios = ImageIO.createImageOutputStream(myBAOS);
					    imageWriter.setOutput(ios);
					    imageMetaData = imageWriter.getDefaultImageMetadata(new ImageTypeSpecifier(screenshot), null);
					    imageWriter.write(imageMetaData, new IIOImage(screenshot, null, null), null);
					    
						myIS = new ByteArrayInputStream(myBAOS.toByteArray());
						String s = "s:" + myIS.available();
						os.write(s.getBytes(), 0, s.getBytes().length);
						os.flush();
						
						buffer = new byte[2048];
						while((length = myIS.read(buffer)) != -1)
								os.write(buffer, 0, length);
						os.flush();
						
						ios.close();
					    imageWriter.dispose();
				        
					} catch (Exception e) {}
						
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {}
				}
				
			} catch (AWTException e) {};
		}
		
	}

}
