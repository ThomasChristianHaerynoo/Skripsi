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



public class BaseStation {
	private static USART usart;
	private static int  input;
	private static OutputStream  out;
	
	public static Node node;

	private static int COMMON_CHANNEL = 24;
	private static int COMMON_PANID = 0xCAFE;
	private static int ADDR_SEND = 0xAFFE;
	private static int ADDR_RESV = 0xBABE;
	private static NativeI2C i2c;
	private static MPL115A2 pressureSensor;
	private static ADXL345 accelerationSensor;
	private static GPIO accelIrqPin1;
	private static GPIO accelIrqPin2;
	private static GPIO accelCs;
	private static ADT7410 temperatureSensor;
	
	public static BeaconFrameView bfv;
	public static BeaconFrameView.GTSDirectionsMask bfvDir;
	public static BeaconFrameView.GTSDescriptor bfvDes;
	public static BeaconFrameView.GTSSpec spec;
	public static BeaconFrameView.PendingAddrSpec pend;
	public static BeaconFrameView.SuperFrameSpec sup;
	
	static String pesanKata;
	

	public static void main(String[] args) throws Exception {
		BaseStation.useUSART();
		new Thread() {
			public void run() {
				try {
					receive();
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	public static void receive() throws SPIException, IRQException, RadioDriverException, IOException {
		final Frame frame = new Frame();
		AT86RF231 t = Node.getInstance().getTransceiver();
		t.open();
		t.setAddressFilter(COMMON_PANID , ADDR_SEND , ADDR_SEND , false);
		final RadioDriver radioDriver = new AT86RF231RadioDriver(t);
		final FrameIO fio = new RadioDriverFrameIO(radioDriver);
		final FilterFrameIO filterframe = new FilterFrameIO(fio);
		final ThreadedFrameIO threadframe = new ThreadedFrameIO(fio);
		
		new Thread() {
			public void run() {
				
				while (usart != null) {
					try {
						input = usart.read();	
						if (input == 1) {
							send(frame);
							receiveData();	
						}
						else if (input == 3) {
							sendFrameIO(fio);
							receiveFrameIO(fio);
						}
						else if (input == 4) {
							sendThreadFrame(threadframe);
							receiveThreadFrame(threadframe);
						}
						else if (input == 6) {
							sendFilterFrame(filterframe);
							receiveFilterFrame(filterframe);
						}
						else if (input == 7) {
							beaconValue();
						}
						
						else if(input == 8) {
							control();
						}
					} catch (Exception e) {
					}
				}
			};
		}.start();
	}
	
	

	private static void write(byte [] res) throws USARTException, IOException {
		out = usart.getOutputStream();
		out.write(res);
		out.flush();
		out.close();

	}
	
	//Method inisialisasi sensing
	public static void init() throws Exception {

		i2c = NativeI2C.getInstance(1);
		i2c.open(I2C.DATA_RATE_400);


		temperatureSensor = new ADT7410(i2c, ADT7410.ADDR_0, null, null);
		temperatureSensor.open();
		temperatureSensor.setMode(ADT7410.CONFIG_MODE_CONTINUOUS);

		


		accelIrqPin1 = NativeGPIO.getInstance(37);
		accelIrqPin2 = NativeGPIO.getInstance(25);
		accelCs = NativeGPIO.getInstance(20);
		

		NativeSPI spi = NativeSPI.getInstance(0);
		spi.open(ADXL345.SPI_MODE, ADXL345.SPI_BIT_ORDER, ADXL345.SPI_MAX_SPEED);
		

		accelerationSensor = new ADXL345(spi, accelCs);
		
		accelerationSensor.open();
		accelerationSensor.setDataFormat(ADXL345.DATA_FORMAT_RANGE_2G);
		accelerationSensor.setDataRate(ADXL345.DATA_RATE_3200HZ);
		accelerationSensor.setPowerControl(ADXL345.POWER_CONTROL_MEASURE);

	

		GPIO resetPin = NativeGPIO.getInstance(24);
		GPIO shutDownPin = NativeGPIO.getInstance(12);


		pressureSensor = new MPL115A2(i2c, resetPin, shutDownPin);
		pressureSensor.open();
		pressureSensor.setReset(false);
		pressureSensor.setShutdown(false);

	}
	
	public static void send(Frame frame) throws Exception {
		final Shuttle shuttle = Shuttle.getInstance();

		AT86RF231 radio = RadioInit.initRadio();
		radio.setChannel(COMMON_CHANNEL);
		radio.setPANId(COMMON_PANID);
		radio.setShortAddress(ADDR_SEND);

		LED green = shuttle.getLED(Shuttle.LED_GREEN);
		green.open();	

		LED red = shuttle.getLED(Shuttle.LED_GREEN);
		red.open();

		Console console = new Console();
		String msg = pesan();
		
		while (true) {
			
			for (int i = 0; i < Integer.MAX_VALUE; i++) {
				boolean isOK = false;
				while (!isOK) {
					try {
						System.out.print("MASUK KE TRY DI WHILE 3");
						// ///////////////////////////////////////////////////////////////////////
						frame = new Frame(Frame.TYPE_DATA | Frame.ACK_REQUEST
								| Frame.DST_ADDR_16 | Frame.INTRA_PAN | Frame.SRC_ADDR_16);
						
						frame.setSrcAddr(ADDR_SEND);
						frame.setSrcPanId(COMMON_PANID);
						frame.setDestAddr(ADDR_RESV);
						frame.setDestPanId(COMMON_PANID);
						radio.setState(AT86RF231.STATE_TX_ARET_ON);
						frame.setPayload(msg.getBytes());
						radio.transmitFrame(frame);
						
						// ///////////////////////////////////////////////////////////////////////
						Misc.LedBlinker(green, 100, false);
						System.out.println(msg);
	
						isOK = true;
					} catch (Exception e) {
						Misc.LedBlinker(red, 100, false);
						System.out.println("ERROR: no receiver");
					}
				}
			}

		}
	}
	
	public static void sendFrameIO(final FrameIO fio)throws Exception  {
		final String msg = pesan();
		new Thread() {
			@Override
			public void run() {
				// Create a testframe which is used by the transmitter
				int frameControl = Frame.TYPE_DATA | Frame.ACK_REQUEST | Frame.DST_ADDR_16
						| Frame.INTRA_PAN | Frame.SRC_ADDR_16;
				
				final Frame testFrame = new Frame(frameControl);
				testFrame.setSequenceNumber(0);
				testFrame.setDestPanId(COMMON_PANID);
				testFrame.setDestAddr(ADDR_RESV);
				testFrame.setSrcAddr(ADDR_SEND);
				testFrame.setPayload(msg.getBytes());

				for (;;) {
					try {
						// transmit a frame
						fio.transmit(testFrame);
						System.out.println(msg);

						Thread.sleep(50);
						
					} catch (Exception e) {
						System.out.println("Error transmitting frame " + testFrame.getSequenceNumber());
					}
				}
			}
		}.start();
	}
	
	private static void receiveFrameIO(final FrameIO fio) {
		// create empty frame used by the receiver
		Frame frame = new Frame();
		for (;;) {
			try {
				// receive a frame
				fio.receive(frame);
				byte[] res = frame.getBytes();
				String kata = new String(res);
				String katadipotong = kata.substring(10);

				
				// print the frame
				System.out.println(katadipotong);
			} catch (Exception e) {
				System.out.println("Error receiving frame");
			}
		}
	}
	public static void sendFilterFrame(final FilterFrameIO filterframe)throws Exception  {
		final String msg = pesan();
		new Thread() {
			@Override
			public void run() {
				// Create a testframe which is used by the transmitter
				int frameControl = Frame.TYPE_DATA | Frame.ACK_REQUEST | Frame.DST_ADDR_16
						| Frame.INTRA_PAN | Frame.SRC_ADDR_16;
				
				final Frame testFrame = new Frame(frameControl);
				testFrame.setSequenceNumber(0);
				testFrame.setDestPanId(COMMON_PANID);
				testFrame.setDestAddr(ADDR_RESV);
				testFrame.setSrcAddr(ADDR_SEND);
				testFrame.setPayload(msg.getBytes());

				for (;;) {
					try {
						// transmit a frame
						filterframe.transmit(testFrame);
						System.out.println(msg);

						Thread.sleep(50);
						
					} catch (Exception e) {
						System.out.println("Error transmitting frame " + testFrame.getSequenceNumber());
					}
				}
			}
		}.start();
	}
	
	private static void receiveFilterFrame(final FilterFrameIO filterframe) {
		// create empty frame used by the receiver
		Frame frame = new Frame();
		for (;;) {
			try {
				// receive a frame
				filterframe.receive(frame);
				byte[] res = frame.getBytes();
				String kata = new String(res);
				String katadipotong = kata.substring(10);

				
				// print the frame
				System.out.println(katadipotong);
			} catch (Exception e) {
				System.out.println("Error receiving frame");
			}
		}
	}
	
	public static void sendThreadFrame(final ThreadedFrameIO threadIO)throws Exception  {
		final String msg = pesan();
		new Thread() {
			@Override
			public void run() {
				// Create a testframe which is used by the transmitter
				int frameControl = Frame.TYPE_DATA | Frame.ACK_REQUEST | Frame.DST_ADDR_16
						| Frame.INTRA_PAN | Frame.SRC_ADDR_16;
				
				final Frame testFrame = new Frame(frameControl);
				testFrame.setSequenceNumber(0);
				testFrame.setDestPanId(COMMON_PANID);
				testFrame.setDestAddr(ADDR_RESV);
				testFrame.setSrcAddr(ADDR_SEND);
				testFrame.setPayload(msg.getBytes());
				

				for (;;) {
					try {
						// transmit a frame
						threadIO.start();
						threadIO.transmit(testFrame);

//						threadIO.getOutputBufferSize();
						System.out.println(msg);

						Thread.sleep(50);
						
					} catch (Exception e) {
						System.out.println("Error transmitting frame " + testFrame.getSequenceNumber());
					}
				}
			}
		}.start();
	}
	
	private static void receiveThreadFrame(final ThreadedFrameIO threadIO) {
		// create empty frame used by the receiver
		Frame frame = new Frame();
		for (;;) {
			try {
				// receive a frame
				threadIO.start();
				threadIO.receive(frame);

				byte[] res = frame.getBytes();
				String kata = new String(res);
				String katadipotong = kata.substring(10);

				
				// print the frame
				System.out.println(katadipotong);
			} catch (Exception e) {
				System.out.println("Error receiving frame");
				threadIO.setReceiveTimeout(20);
			}
		}
	}
public static void control() throws Exception {
		FrameControl fc = new FrameControl(Integer.MAX_VALUE);
		
		FrameControl frame = new FrameControl(FrameControl.TYPE_DATA | FrameControl.ACK_REQUEST
				| FrameControl.DST_ADDR_16 | FrameControl.INTRA_PAN | FrameControl.SRC_ADDR_16);
		
		String msg = "";
		msg = "Destination Address Mode " + fc.getDestAddrMode() + "\n" 
			  + "Source Address Mode " + fc.getSrcAddrMode() + "\n" + "Tipenya " + fc.getType() + "\n" +
			  "Value " + fc.getValue() + "\n" + "ACK " + fc.isAck() + "\n" + "Requst Ack " + fc.isAckRequest() +
			  "\n" + "Beacon " + fc.isBeacon() + "\n" + "Data " + fc.isData() + "\n" + "Dest Addr Mode" +
			  fc.isDestAddrMode(fc.getDestAddrMode()) + "\n" + "Pending Frame "+ fc.isFramePending() + "\n" + 
			  "IntraPan "  + fc.isIntraPAN() + "\n" + "Security Enable "
			  + fc.isSecurityEnabled() + "\n"+ "Tipenya " +fc.isType(fc.getType()) + "\n";
		
		byte[] res = msg.getBytes("UTF-8");
		write(res);
	}

	
	//Method untuk mendapatkan sensing
//	public static void dataSensing() throws Exception {
//		init();
//		short[] values = new short[3];
//		String kata2 = "";
//	
//				int raw = temperatureSensor.getTemperatureRaw();
//				float celsius = temperatureSensor.getTemperatureCelsius();
//				accelerationSensor.getValuesRaw(values, 0);
//				pressureSensor.startBothConversion();
//				Thread.sleep(MPL115A2.BOTH_CONVERSION_TIME);
//				
//				int pressurePr = pressureSensor.getPressureRaw();
//				int tempRaw = pressureSensor.getTemperatureRaw();
//				float pressure = pressureSensor.compensate(pressurePr, tempRaw);
//				String tekanan = Float.toString(pressure);
//				
//				
//				kata2 =  "Suhu : " + celsius + " [°C]" + ", raw=" + raw + ", Acceleration : " + Arrays.toString(values) + ", Pressure : " + tekanan.substring(0,7);
//
//			Thread.sleep(1000 - MPL115A2.BOTH_CONVERSION_TIME);
//			Thread.sleep(1000);
//			Thread.sleep(500);
//			byte[] sensing = kata2.getBytes("UTF-8");
//			write(sensing);
//	}
	
		
	public static String pesan() throws Exception {
		init();
		short[] values = new short[3];
		String kata2 = "";
	
				int raw = temperatureSensor.getTemperatureRaw();
				float celsius = temperatureSensor.getTemperatureCelsius();
				accelerationSensor.getValuesRaw(values, 0);
				pressureSensor.startBothConversion();
				Thread.sleep(MPL115A2.BOTH_CONVERSION_TIME);
				
				int pressurePr = pressureSensor.getPressureRaw();
				int tempRaw = pressureSensor.getTemperatureRaw();
				float pressure = pressureSensor.compensate(pressurePr, tempRaw);
				String tekanan = Float.toString(pressure);
				
				
				kata2 =  "Suhu : " + celsius + " [°C]" + ", raw=" + raw + ", Acceleration : " + Arrays.toString(values) + ", Pressure : " + tekanan.substring(0,7);

				Thread.sleep(1000 - MPL115A2.BOTH_CONVERSION_TIME);
				Thread.sleep(1000);
			Thread.sleep(500);
			return kata2;
	}
	
	//Menerima dengan receive biasa
	public static void receiveData() throws Exception {
		System.out.println("Text_Receiver12");

		final Shuttle shuttle = Shuttle.getInstance();

		final AT86RF231 radio = RadioInit.initRadio();
		radio.setChannel(COMMON_CHANNEL);
		radio.setPANId(COMMON_PANID);
		radio.setShortAddress(ADDR_RESV);

		final LED orange = shuttle.getLED(Shuttle.LED_ORANGE);
		orange.open();

		Thread reader = new Thread() {
			@Override
			public void run() {
				while (true) {
					Frame f = null;
					try {
						f = new Frame();
						radio.setState(AT86RF231.STATE_RX_AACK_ON);
						radio.waitForFrame(f);
						Misc.LedBlinker(orange, 100, false);
						
						if (f != null) {
							byte[] dg = f.getPayload();
							String str = new String(dg, 0, dg.length);
							System.out.println("FROM:" + str);							
							byte[] res = str.getBytes("UTF-8");							
							out.write(res);
							
						}
						
					} catch (Exception e) {
					}
					
				}
			}
		};
		reader.start();

		while (true) {
			Misc.sleep(1000);
		}
	}
	
	public static void beaconValue() throws Exception {
		
		String msg = "Test";	
		System.out.println("Value DirectionMask = " + bfvDir.getValue());
		
		
		System.out.println("Short Address Device = " + bfvDes.getDeviceShortAddr());
		System.out.println("GTS Length = " +  bfvDes.getGTSLength());
		System.out.println("GTS Starting Slot = " +  bfvDes.getGTSStartingSlot());
		System.out.println("Descriptor Value = " +  bfvDes.getValue());
		
		System.out.println("Spec Count  = " +  spec.getDescriptorCount());
		System.out.println("Info Length GTS  = " +  spec.getInfoLength());
		System.out.println("Spec Value  = " +  spec.getValue());
		System.out.println("Permit  = " +  spec.isPermit());
		
		System.out.println("Pending Short and Long Address  = " +  pend.getAddrCount());
		System.out.println("Jumlah byte Pending Address  = " +  pend.getInfoLength());
		System.out.println("Jumlah Pending Long Address   = " +  pend.getLongAddrCount());
		System.out.println("Jumlah Pending Short Address  = " +  pend.getShortAddrCount());
		System.out.println("Value Pending Address Specification  = " +  pend.getLongAddrCount());
		
		System.out.println("Beacon Order  = " +  sup.getBeaconOrder());
		System.out.println("final CAP slot  = " +  sup.getFinalCAPSlot());
		System.out.println("Super Frame Order Value   = " +  sup.getSuperframeOrder());
		System.out.println("Super Frame Spec Value  = " +  sup.getValue());
		System.out.println("Association bit set  = " +  sup.isAssociationPermit());
		System.out.println("Battery Life Extension  = " +  sup.isBatteryLifeExtension());
		System.out.println("Pan Coordinator  = " +  sup.isPANCoordinator());
		
		
//		 .bfv.setGTSDirectionsMask(bfvDir);
		 bfv.setPayload(msg.getBytes());
		System.out.println("SuperFrameSpec = " +  bfv.getSuperFrameSpec());
		
		System.out.println("GTSDirectionMask = " +  bfv.getGTSDirectionsMask());
		System.out.println("MaxPayloadLength = " +  bfv.getMaxPayloadLength());
		System.out.println("Payload = " +  bfv.getPayload());
		System.out.println("GTSSPEC = " +  bfv.getGTSSpec());
		System.out.println("Payload Length = " +  bfv.getPayloadLength());
		System.out.println("Payload Offset = " +  bfv.getPayloadOffset());
		System.out.println("Pending Address Specification = " +  bfv.getPendingAddrSpec());
		System.out.println("Super Frame Spec = " +  bfv.getSuperFrameSpec());
		
		
		
		System.out.println("enter");
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

	static void useUSART() throws Exception {
		usart = configUSART();
	}
}
