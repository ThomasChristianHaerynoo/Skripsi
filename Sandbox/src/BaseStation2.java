import com.virtenio.radio.ieee_802_15_4.Frame;
import com.virtenio.radio.ieee_802_15_4.FrameIO;
import com.virtenio.radio.ieee_802_15_4.RadioDriver;
import com.virtenio.radio.ieee_802_15_4.RadioDriverFrameIO;

import com.virtenio.preon32.examples.common.USARTConstants;
//import com.virtenio.preon32.examples.common.RadioInit;
import com.virtenio.preon32.node.Node;

import java.io.OutputStream;
import java.util.HashMap;

import com.virtenio.driver.device.at86rf231.*;

import com.virtenio.driver.usart.NativeUSART;
import com.virtenio.driver.usart.USARTParams;
import com.virtenio.driver.usart.USART;

import com.virtenio.misc.PropertyHelper;
//import com.virtenio.vm.Time;
//import java.io.OutputStream;

public class BaseStation2 {
	
	private final static int BASE_ADDR = PropertyHelper.getInt("local.addr",0xBABE);
	private final static int BROADCAST = PropertyHelper.getInt("remote.addr",0xFFFF);
	private final static int COMMON_PANID = PropertyHelper.getInt("radio.panid",0xCACE);
//	private final static int[] allAddr = { PropertyHelper.getInt("radio.panid", 0xDAAA), 
//			PropertyHelper.getInt("radio.panid", 0xDAAB), PropertyHelper.getInt("radio.panid", 0xDAAC),
//			PropertyHelper.getInt("radio.panid", 0xDAAD)};
	
	
	
	private static String hasil = Integer.toHexString(BASE_ADDR) + "#"; 
	
	private static int counterNode = 0;

	private static HashMap<String, String> tempNode = new HashMap<String, String>();
	
	private static USART usart;
	private static OutputStream out;
	
	public static void main(String[] args) throws Exception {
		BaseStation.useUSART();
		out = usart.getOutputStream();
		
		new Thread() {
			public void run() {
				runs();
			}
		}.start();
	}
	
	public static void runs() {
		try {
			AT86RF231 t = Node.getInstance().getTransceiver();
			t.open();
			t.setAddressFilter(COMMON_PANID, BASE_ADDR, BASE_ADDR, false);
			final RadioDriver radio = new AT86RF231RadioDriver(t);
			final FrameIO fio = new RadioDriverFrameIO(radio);
			
			Thread thread =new Thread() {
				public void run() {
					try {
						send(fio);
						receive(fio);
						receive2(fio);
					} catch (Exception e) {
					}
				}
			};
			thread.start();
		} catch (Exception e) {}
	} 
	
	public static void send(final FrameIO fio) throws Exception {
		new Thread() {
			public void run() {
				int input = 0;
				while (true) {
					try {
						input = usart.read();
						if(input == 1) {
							send("FRAME", fio);
							Thread.sleep(1000);
						}else if( input == 2) {
							send("FrameIO", fio);
							Thread.sleep(1000);
						}
					} catch (Exception e) {}
				}
			};
		}.start();
	}
	
	public static void receive(final FrameIO fio) throws Exception {
		Thread thread = new Thread() {
			public void run() {
				Frame frame = new Frame();
				while(true) {
					try {
						fio.receive(frame);
						byte[] dg = frame.getPayload();
						String str = new String(dg, 0, dg.length);
						String msg = "";
						
						if(str.substring(0,4).equalsIgnoreCase("frame")) {
							sendAddr(Integer.toHexString(BASE_ADDR) + "Frame", (int) frame.getSrcAddr(), fio);
							String key = str.substring(0, 4);
							if(counterNode==0) {
								if(!str.substring(0,4).equalsIgnoreCase(Integer.toHexString(BASE_ADDR))){
									tempNode.put(key, str.substring(0, 4));
									hasil += tempNode.get(key);
									counterNode++;
								}
							}
							else {
								if(!tempNode.containsValue(str.substring(0, 4))) {
									if(!str.substring(0,4).equalsIgnoreCase(Integer.toHexString(BASE_ADDR))){
										tempNode.put(key, str.substring(0, 4));
										hasil += "," + tempNode.get(key);
									}
								}
							}
							msg = hasil + "#" + "\n";	
							out.write(msg.getBytes(), 0, msg.length());
							usart.flush();
							Thread.sleep(1000);
						} 
					} catch (Exception e) {}
				}
			}
		};
		thread.start();
	}
	
	public static void receive2(final FrameIO fio) throws Exception {
		Thread thread = new Thread() {
			public void run() {
				Frame frame = new Frame();
				while(true) {
					try {
						fio.receive(frame);
						byte[] dg = frame.getPayload();
						String str = new String(dg, 0, dg.length);
						String msg = "";
						
						out.write(msg.getBytes(), 0, msg.length());
						usart.flush();			
						
						Thread.sleep(1000);
					} catch (Exception e) {}
				}
			}
		};
		thread.start();
	}
	
	
	private static void send(String msg, FrameIO fio) throws Exception {
		int frame = Frame.TYPE_DATA | Frame.DST_ADDR_16 | Frame.INTRA_PAN | Frame.ACK_REQUEST | Frame.SRC_ADDR_16;
		final Frame frameControl = new Frame(frame);
		frameControl.setDestPanId(COMMON_PANID);
		frameControl.setDestAddr(BROADCAST);
		frameControl.setSrcAddr(BASE_ADDR);
		frameControl.setPayload(msg.getBytes());
		try {
			fio.transmit(frameControl);
		} catch (Exception e) {}
		System.out.println(msg);
	}
	
	private static void sendAddr(String msg, int dest, final FrameIO fio) throws Exception {
		int frame = Frame.TYPE_DATA | Frame.DST_ADDR_16 | Frame.INTRA_PAN | Frame.ACK_REQUEST | Frame.SRC_ADDR_16;
		final Frame frameControl = new Frame(frame);
		frameControl.setDestPanId(COMMON_PANID);
		frameControl.setDestAddr(dest);
		frameControl.setSrcAddr(BASE_ADDR);
		frameControl.setPayload(msg.getBytes());
		try {
			fio.transmit(frameControl);
		} catch (Exception e) {}
	}
	
	public static void useUSART() throws Exception{
		usart = configUSART();
	}
	
	private static USART configUSART() {
		int instanceID = 0;
		USARTParams params = USARTConstants.PARAMS_115200;
		NativeUSART usart = NativeUSART.getInstance(instanceID);
		try {
			usart.close();
			usart.open(params);
			return usart;
		} catch (Exception e) {
			return null;
		}
	}
	
	
}