package org.rohm1.btserver;

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
import java.io.OutputStream;
import java.util.HashMap;

import javax.microedition.io.*;

import org.rohm1.btserver.ui.Pointer;
import org.rohm1.btserver.ui.Screen;

public class RFCOMMServer {

	static final String serverUUID = "58cf9ce01a1611e2892e0800200c9a66";
	static final HashMap<Integer, Integer> codes = ascii2keys();

	@SuppressWarnings("static-access")
	public static void main( String args[] ) {
		StreamConnectionNotifier service = null;
		StreamConnection conn = null;
		InputStream is;
		OutputStream os;

		Robot robot;
		Toolkit toolkit =  Toolkit.getDefaultToolkit ();
		Dimension dim = toolkit.getScreenSize();
		int W = (int) dim.getWidth();
		int H = (int) dim.getHeight();
		int prevX = 0, prevY = 0;

		Pointer jf = null;
		Screen s = null;

		while(true) {
			try {
				service = (StreamConnectionNotifier) Connector.open("btspp://localhost:" + serverUUID + ";name=BtServerForBtRemote");

				conn = service.acceptAndOpen();
				is = conn.openInputStream();
				os = conn.openOutputStream();

				robot = new Robot();

				byte[] buffer;
				int bytes_read;
				String received;
				String[] params;

				while(true) {
					buffer = new byte[64];
					bytes_read = is.read( buffer );
					received = new String(buffer, 0, bytes_read);
					System.out.println("received: " + received);

					params = received.split("\\|");
					if(params[0].equals(BtCodes.MOUSE_LEFT_CLC)) {
						robot.mousePress(InputEvent.BUTTON1_MASK);
						robot.mouseRelease(InputEvent.BUTTON1_MASK);
					}
					else if(params[0].equals(BtCodes.MOUSE_ABS) || params[0].equals(BtCodes.PREZ_POINTER)) {
						int w = (int) Math.abs(Float.parseFloat(params[3]));
						int h = (int) Math.abs(Float.parseFloat(params[4]));
						int x = (int) Math.abs(Float.parseFloat(params[1]));
						int y = (int) Math.abs(Float.parseFloat(params[2]));

						x = (int) (((float)x/w)*W);
						x = x < 0 ? 0 : (x > W ? W : x);

						y = (int) (((float)y/h)*H);
						y = y < 0 ? 0 : (y > H ? H : y);

						if(params[0].equals(BtCodes.MOUSE_ABS))
							robot.mouseMove(x, y);
						else {
							if(jf == null)
								jf = new Pointer();
							else if(!jf.isVisible())
								jf.setVisible(true);
							jf.movePointer(x, y);
						}
					}
					else if(params[0].equals(BtCodes.MOUSE_REL)) {
						int x = Math.round(Float.parseFloat(params[1]));
						int y = Math.round(Float.parseFloat(params[2]));
						int stepX = x-prevX;
						int stepY = y-prevY;

						if(Math.abs(stepX) < 21 && Math.abs(stepY) < 21) {
							PointerInfo info = MouseInfo.getPointerInfo();
							Point location = info.getLocation();
							int[] speeds = new int[] {0, 1, 2, 3, 4, 5, 8, 12, 16, 20, 25, 30, 35, 43, 51, 59, 67, 75, 83, 91, 100};
							robot.mouseMove((int) (location.x + speeds[Math.abs(stepX)] * (stepX >= 0 ? 1 : -1)), (int) (location.y + speeds[Math.abs(stepY)] * (stepY >= 0 ? 1 : -1)));
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

					    if ((ascii > 64 && ascii < 91) || (ascii > 96 && ascii < 123))
					    	ascii -= 32;
					    else if(codes.containsKey(ascii))
					    	ascii = codes.get(ascii);

					    robot.keyPress(ascii);
		                robot.keyRelease(ascii);

//					    robot.keyRelease(KeyEvent.VK_ALT);
					}
					else if(params[0].equals(BtCodes.SPECIAL_KEY)) {
						if(params[1].equals(BtCodes.KEY_BACK_SP)) {
							robot.keyPress(KeyEvent.VK_BACK_SPACE);
							robot.keyRelease(KeyEvent.VK_BACK_SPACE);
						}
					}
					else if(params[0].equals(BtCodes.PREZ_PREV)) {
						robot.keyPress(KeyEvent.VK_LEFT);
						robot.keyRelease(KeyEvent.VK_LEFT);
					}
					else if(params[0].equals(BtCodes.PREZ_NEXT)) {
						robot.keyPress(KeyEvent.VK_RIGHT);
						robot.keyRelease(KeyEvent.VK_RIGHT);
					}
					else if(params[0].equals(BtCodes.PREZ_FULL0)) {
						robot.keyPress(KeyEvent.VK_ESCAPE);
						robot.keyRelease(KeyEvent.VK_ESCAPE);
					}
					else if(params[0].equals(BtCodes.PREZ_FULL1)) {
						robot.keyPress(KeyEvent.VK_F5);
						robot.keyRelease(KeyEvent.VK_F5);
					}
					else if(params[0].equals(BtCodes.PREZ_POINTER_RELEASE)) {
						if(jf != null) {
							jf.dispose();
							jf = null;
						}
					}
					else if(params[0].equals(BtCodes.START_SCREEN)) {
						int w = (int) Math.abs(Float.parseFloat(params[1]));
						int h = (int) Math.abs(Float.parseFloat(params[2]));
						s = new Screen(os, w, h);
					}
					else if(params[0].equals(BtCodes.STOP_SCREEN)) {
						s.stop();
						s = null;
					}
				}

			}
			catch ( Exception  e ) {}

			try {
				conn.close();
			} catch ( IOException  e ) {}

			try {
				service.close();
			} catch (IOException e) {}

			try {
				Thread.currentThread().sleep(20);
			} catch(InterruptedException ie){}

		}
	}

	private static HashMap<Integer, Integer> ascii2keys() {
		HashMap<Integer, Integer> codes = new HashMap<Integer, Integer>();
//		codes.put(8, KeyEvent.VK_BACK_SPACE);
		codes.put(10, KeyEvent.VK_ENTER);
		codes.put(32, KeyEvent.VK_SPACE);
		codes.put(33, KeyEvent.VK_EXCLAMATION_MARK);
		codes.put(34, KeyEvent.VK_QUOTEDBL);
		codes.put(35, KeyEvent.VK_NUMBER_SIGN); //#
		codes.put(36, KeyEvent.VK_DOLLAR);
//		codes.put(37, KeyEvent.VK_);%
		codes.put(38, KeyEvent.VK_AMPERSAND);
		codes.put(39, KeyEvent.VK_QUOTE);
		codes.put(40, KeyEvent.VK_LEFT_PARENTHESIS);
		codes.put(41, KeyEvent.VK_RIGHT_PARENTHESIS);
		codes.put(42, KeyEvent.VK_ASTERISK);
		codes.put(43, KeyEvent.VK_PLUS);
		codes.put(44, KeyEvent.VK_COMMA);
		codes.put(45, KeyEvent.VK_MINUS);
		codes.put(46, KeyEvent.VK_PERIOD);
		codes.put(47, KeyEvent.VK_SLASH);
		codes.put(58, KeyEvent.VK_COLON);
		codes.put(59, KeyEvent.VK_SEMICOLON);
		codes.put(60, KeyEvent.VK_LESS);
		codes.put(61, KeyEvent.VK_EQUALS);
		codes.put(62, KeyEvent.VK_GREATER);
//		codes.put(63, KeyEvent.); ?
		codes.put(64, KeyEvent.VK_AT);
		codes.put(91, KeyEvent.VK_OPEN_BRACKET);
		codes.put(92, KeyEvent.VK_BACK_SLASH);
		codes.put(93, KeyEvent.VK_CLOSE_BRACKET);
		codes.put(94, KeyEvent.VK_CIRCUMFLEX);
		codes.put(95, KeyEvent.VK_UNDERSCORE);
		codes.put(123, KeyEvent.VK_BRACELEFT);
//		codes.put(124, KeyEvent.VK_); |
		codes.put(125, KeyEvent.VK_BRACERIGHT);
		codes.put(126, KeyEvent.VK_DEAD_TILDE);
//		codes.put(127, KeyEvent.VK_DELETE);
		return codes;
	}
}
