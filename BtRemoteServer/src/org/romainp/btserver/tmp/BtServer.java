package org.romainp.btserver.tmp;

import java.io.IOException;
import java.util.Vector;

import javax.bluetooth.*;

public class BtServer {
	
	public Vector<RemoteDevice> devicesDiscovered = new Vector<RemoteDevice>();
	protected Boolean state;

	public BtServer() {
	}
	
	public Boolean getSate() {
		return this.state;
	}
	
	public void init() {
		this.state = false;
		
		try {
			this.discover();
			this.state = true;
		} catch (BluetoothStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	public void discover() throws IOException, InterruptedException, BluetoothStateException {
		final Object inquiryCompletedEvent = new Object();

        devicesDiscovered.clear();

        DiscoveryListener listener = new DiscoveryListener() {

            @Override
			public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
//                System.out.println("Device " + btDevice.getBluetoothAddress() + " found");
                devicesDiscovered.addElement(btDevice);
//                try {
//                    System.out.println("     name " + btDevice.getFriendlyName(false));
//                } catch (IOException cantGetDeviceName) {
//                }
            }

            @Override
			public void inquiryCompleted(int discType) {
//                System.out.println("Device Inquiry completed!");
                synchronized(inquiryCompletedEvent){
                    inquiryCompletedEvent.notifyAll();
                }
            }

            @Override
			public void serviceSearchCompleted(int transID, int respCode) {
            }

            @Override
			public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
            }
        };

        synchronized(inquiryCompletedEvent) {
            boolean started = LocalDevice.getLocalDevice().getDiscoveryAgent().startInquiry(DiscoveryAgent.GIAC, listener);
            if (started) {
                System.out.println("wait for device inquiry to complete...");
                inquiryCompletedEvent.wait();
                System.out.println(devicesDiscovered.size() +  " device(s) found");
            }
        }
	}
}
