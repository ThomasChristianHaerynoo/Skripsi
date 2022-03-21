import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import com.virtenio.driver.device.ADT7410;
import com.virtenio.driver.device.ADXL345;

import com.virtenio.driver.device.MPL115A2;

import com.virtenio.driver.device.at86rf231.AT86RF231;
import com.virtenio.driver.device.at86rf231.AT86RF231RadioDriver;
import com.virtenio.driver.gpio.GPIO;
import com.virtenio.driver.gpio.NativeGPIO;
import com.virtenio.driver.i2c.I2C;
import com.virtenio.driver.i2c.NativeI2C;
import com.virtenio.driver.irq.IRQException;
import com.virtenio.driver.led.LED;
import com.virtenio.driver.spi.NativeSPI;
import com.virtenio.driver.spi.SPIException;
import com.virtenio.driver.usart.NativeUSART;
import com.virtenio.driver.usart.USART;
import com.virtenio.driver.usart.USARTException;
import com.virtenio.driver.usart.USARTParams;
import com.virtenio.io.Console;
import com.virtenio.misc.PropertyHelper;
import com.virtenio.preon32.examples.common.Misc;
import com.virtenio.preon32.examples.common.RadioInit;
import com.virtenio.preon32.examples.common.USARTConstants;
import com.virtenio.preon32.node.Node;
import com.virtenio.preon32.shuttle.Shuttle;
import com.virtenio.radio.RadioDriverException;
import com.virtenio.radio.ieee_802_15_4.BeaconFrameView;
import com.virtenio.radio.ieee_802_15_4.FilterFrameIO;
import com.virtenio.radio.ieee_802_15_4.Frame;
import com.virtenio.radio.ieee_802_15_4.FrameControl;
import com.virtenio.radio.ieee_802_15_4.FrameIO;
import com.virtenio.radio.ieee_802_15_4.RadioDriver;
import com.virtenio.radio.ieee_802_15_4.RadioDriverFrameIO;
import com.virtenio.radio.ieee_802_15_4.ThreadedFrameIO;
import java.util.HashMap;


public class BaseStation {
	private static USART usart;
	private static int  input;
	private static OutputStream  out;
	
	public static Node node;

	private static int COMMON_CHANNEL = 24;
	private static int COMMON_PANID = PropertyHelper.getInt("radio.panid",0xCACE);
//	private static int ADDR_SEND = 0xAFFE; //alamat pengirim
	private static int BASE_STATION = PropertyHelper.getInt("local.addr",0xAFFE); //alamat Base
//	private static int NODE_SATU = 0xDAAC; //alamat penerima
	private final static int NODE = PropertyHelper.getInt("remote.addr",0xBABE); //alamat Node
	
	private static HashMap<String, String> tempNode = new HashMap<String, String>();
	
	public Send send;
	
	static String pesanKata;
	static int counterNode;
	static String hasil;
	
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
			t.setAddressFilter(COMMON_PANID, BASE_STATION, BASE_STATION, false); //set untuk alamat sensor
			final RadioDriver radio = new AT86RF231RadioDriver(t);
			final FrameIO fio = new RadioDriverFrameIO(radio);
			
			Thread thread =new Thread() {
				public void run() {
					try {
						send(fio);
						receive(fio);
					} catch (Exception e) {
					}
				}
			};
			thread.start();
		} catch (Exception e) {}
	} 
	
	public static void send(final FrameIO fio) throws Exception {
				int input = 0;
					try {
						input = usart.read();
						System.out.println("Thomas1");
						send("FrameIO",  fio);
						System.out.println("Thomas2");
//						if(input == 1) {
////							send("Frame",  fio);
//							}
//						else if(input == 3 ) {
//							
//						}
//						else if (input == 4) {
////							send("ThreadFrameIO", fio);
//							
//						}
//						else if(input == 6) {
////							send("FilterFrameIO", fio);
//							
//						}
//						else if(input == 7) {
////							send("Beacon", fio);
//							
//						}
//						else if(input == 8) {
////							send("FrameControl", fio);
//						}
					} catch (Exception e) { System.out.println("Thomas3" + e);}
				
			};
	
	public static void receive(final FrameIO fio) throws Exception {
		Thread thread = new Thread() {
			public void run() {
				Frame frame = new Frame();
				while(true) {
					try {
						fio.receive(frame);
						System.out.println("Thomas1");
						byte[] res = frame.getBytes();
						System.out.println("Thomas2");
						String kata = new String(res);
						System.out.println("Thomas3");
						String kataDiPotong = kata.substring(10);
						System.out.println("Thomas" + kataDiPotong);
						
						out.write(kataDiPotong.getBytes(), 0, kataDiPotong.length());
						usart.flush();
						Thread.sleep(100000);
					} catch (Exception e) { System.out.println("Thomas3" + e);}
				}
			}
		};
		thread.start();
	}
	
	private static void send(String msg,FrameIO fio) throws Exception {
		int frame = Frame.TYPE_DATA | Frame.DST_ADDR_16 | Frame.INTRA_PAN | Frame.ACK_REQUEST | Frame.SRC_ADDR_16;
		final Frame frameControl = new Frame(frame);
		frameControl.setDestPanId(COMMON_PANID);
		frameControl.setDestAddr(NODE);
		frameControl.setSrcAddr(BASE_STATION);
		frameControl.setPayload(msg.getBytes());
		try {
			fio.transmit(frameControl);
		} catch (Exception e) {}
		System.out.println(msg);
	}
	
//	private static void sendAddr(String msg, int dest, final FrameIO fio) throws Exception {
//		int frame = Frame.TYPE_DATA | Frame.DST_ADDR_16 | Frame.INTRA_PAN | Frame.ACK_REQUEST | Frame.SRC_ADDR_16;
//		final Frame frameControl = new Frame(frame);
//		frameControl.setDestPanId(COMMON_PANID);
//		frameControl.setDestAddr(dest);
//		frameControl.setSrcAddr(BASE_STATION);
//		frameControl.setPayload(msg.getBytes());
//		try {
//			fio.transmit(frameControl);
//		} catch (Exception e) {}
//	}
	
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
