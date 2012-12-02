package org.romainp.btserver;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;

import javax.microedition.io.*;


public class RFCOMMServer {

	static final String serverUUID = "58cf9ce01a1611e2892e0800200c9a66";

	@SuppressWarnings("static-access")
	public static void main( String args[] ) {
		StreamConnectionNotifier service = null;
		StreamConnection conn = null;
		InputStream is;

		Robot robot;
		Toolkit toolkit =  Toolkit.getDefaultToolkit ();
		Dimension dim = toolkit.getScreenSize();
		int W = (int) dim.getWidth();
		int H = (int) dim.getHeight();
		int prevX = 0, prevY = 0;
		
		while(true) {
			try {

				service = (StreamConnectionNotifier) Connector.open( "btspp://localhost:" + serverUUID + ";name=BtServerForBtRemote");

				conn = service.acceptAndOpen();
				is = conn.openInputStream();
				
				robot = new Robot();

				byte[] buffer;
				int bytes_read;
				String received;
				String[] params;

				while(true) {
					buffer = new byte[100];
					bytes_read = is.read( buffer );
					received = new String(buffer, 0, bytes_read);
					System.out.println("received: " + received);

					params = received.split("\\|");
					if(params[0].equals(BtCodes.MOUSE_LEFT_CLC)) {
						robot.mousePress(InputEvent.BUTTON1_MASK);
						robot.mouseRelease(InputEvent.BUTTON1_MASK);
					}
					else if(params[0].equals(BtCodes.MOUSE_ABS)) {
						int w = (int) Math.abs(Float.parseFloat(params[3]));
						int h = (int) Math.abs(Float.parseFloat(params[4]));
						int x = (int) Math.abs(Float.parseFloat(params[1]));
						int y = (int) Math.abs(Float.parseFloat(params[2]));

						x = (int) (((float)x/w)*W);
						x = y < 0 ? 0 : (x > W ? W : x);

						y = (int) (((float)y/h)*H);
						y = y < 0 ? 0 : (y > H ? H : y);

						robot.mouseMove(x, y);
					}
					else if(params[0].equals(BtCodes.MOUSE_REL)) {
						int x = Math.round(Float.parseFloat(params[1]));
						int y = Math.round(Float.parseFloat(params[2]));
						int stepX = x-prevX;
						int stepY = y-prevY;
						int speed = 5;

						if(Math.abs(stepX) < 20 && Math.abs(stepY) < 20) {
							PointerInfo info = MouseInfo.getPointerInfo();
							Point location = info.getLocation();

							robot.mouseMove(location.x+stepX*speed, location.y+stepY*speed);
						}

						prevX = x;
						prevY = y;
					}
					else if(params[0].equals(BtCodes.KEY)) {
//					    robot.keyPress(KeyEvent.VK_ALT);
					    
//					    int ascii = Integer.parseInt(params[1]);
//					    
//					    for(int i = params[1].length()-1; i >= 0; i--)  {
//					        int numpad_kc = ascii / (int) (Math.pow(10, i)) % 10 + KeyEvent.VK_NUMPAD0;
//					        System.out.println(numpad_kc+"");
//					        ascii -= (ascii / (int) (Math.pow(10, i)) % 10) * Math.pow(10, i);
//					        robot.keyPress(numpad_kc);
//					        robot.keyRelease(numpad_kc);
//					    }

//					    byte[] bytes = params[1].getBytes();
//			            for (byte b : bytes)
//			            {
//			                int code = b;
//			                // keycode only handles [A-Z] (which is ASCII decimal [65-90])
//			                if (code > 96 && code < 123) code = code - 32;
//			                robot.delay(40);
//			                robot.keyPress(code);
//			                robot.keyRelease(code);
//			            }
					    
					    int ascii = Integer.parseInt(params[1]);
					    
					    if (ascii > 96 && ascii < 123) ascii -= 32;
					    
					    robot.keyPress(ascii);
		                robot.keyRelease(ascii);

//					    robot.keyRelease(KeyEvent.VK_ALT);
					}
					else if(params[0].equals(BtCodes.SPECIAL_KEY)) {
						if(params[1].equals(BtCodes.KEY_DEL)) {
							robot.keyPress(KeyEvent.VK_BACK_SPACE);
							robot.keyRelease(KeyEvent.VK_BACK_SPACE);
						}
						
					}
				}

			}
			catch ( IOException  e ) {}
			catch ( AWTException e ) {}
			catch ( StringIndexOutOfBoundsException e) {}
			
			try {
				conn.close();
			} catch ( IOException  e ) {}
			
			try {
				service.close();
			} catch (IOException e) {}
			
			try {
				Thread.currentThread().sleep(5000);
			} catch(InterruptedException ie){}
			
		}
	}
}