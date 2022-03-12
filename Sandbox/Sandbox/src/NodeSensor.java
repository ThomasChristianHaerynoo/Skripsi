import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
//import java.util.Date;
import java.util.HashMap;

import com.virtenio.driver.adc.ADCException;
import com.virtenio.driver.adc.NativeADC;
import com.virtenio.driver.can.CANException;
import com.virtenio.driver.device.ADT7410;
import com.virtenio.driver.device.ADXL345;
//import com.virtenio.driver.device.BH1710FVC;
import com.virtenio.driver.device.MPL115A2;
import com.virtenio.driver.device.SHT21;
import com.virtenio.driver.device.at86rf231.AT86RF231;
import com.virtenio.driver.device.at86rf231.AT86RF231RadioDriver;
import com.virtenio.driver.flash.Flash;
import com.virtenio.driver.flash.FlashException;
import com.virtenio.driver.gpio.GPIO;
import com.virtenio.driver.gpio.GPIOException;
import com.virtenio.driver.gpio.NativeGPIO;
import com.virtenio.driver.i2c.I2C;
import com.virtenio.driver.i2c.I2CException;
import com.virtenio.driver.i2c.NativeI2C;
import com.virtenio.driver.irq.IRQException;
import com.virtenio.driver.led.LED;
import com.virtenio.driver.ram.NativeRAM;
import com.virtenio.driver.ram.RAMException;
import com.virtenio.driver.spi.NativeSPI;
import com.virtenio.driver.spi.SPIException;
import com.virtenio.driver.usart.NativeUSART;
import com.virtenio.driver.usart.USART;
import com.virtenio.driver.usart.USARTException;
import com.virtenio.driver.usart.USARTParams;
import com.virtenio.io.Console;
import com.virtenio.misc.PropertyHelper;
import com.virtenio.preon32.cpu.CPUConstants;
import com.virtenio.preon32.cpu.CPUHelper;
import com.virtenio.preon32.examples.common.Misc;
import com.virtenio.preon32.examples.common.RadioInit;
import com.virtenio.preon32.examples.common.USARTConstants;
import com.virtenio.preon32.node.Node;
import com.virtenio.preon32.shuttle.Shuttle;
import com.virtenio.radio.RadioDriverException;
import com.virtenio.radio.ieee_802_15_4.FilterFrameIO;
import com.virtenio.radio.ieee_802_15_4.Frame;
import com.virtenio.radio.ieee_802_15_4.FrameIO;
import com.virtenio.radio.ieee_802_15_4.RadioDriver;
import com.virtenio.radio.ieee_802_15_4.RadioDriverFrameIO;
import com.virtenio.radio.ieee_802_15_4.ThreadedFrameIO;
//import com.virtenio.vm.Time;
import com.virtenio.vm.Time;



public class NodeSensor {
	
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
	static int counterNode = 0 ;
	static String pesan;
	
	
	
	/**
	 * Method untuk meng-upload data berupa text ke dalam memory flash
	 * 
	 * @throws Exception
	 */
	
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
	
		
	public static String pesan() throws Exception {
		init();
		short[] values = new short[3];
		String kata2 = "";
		while (true) {
			try {
				int raw = temperatureSensor.getTemperatureRaw();
				float celsius = temperatureSensor.getTemperatureCelsius();
				accelerationSensor.getValuesRaw(values, 0);
				pressureSensor.startBothConversion();
				Thread.sleep(MPL115A2.BOTH_CONVERSION_TIME);
				
				int pressurePr = pressureSensor.getPressureRaw();
				int tempRaw = pressureSensor.getTemperatureRaw();
				float pressure = pressureSensor.compensate(pressurePr, tempRaw);


				kata2 =  "Suhu : " + celsius + " [°C]" + ", raw=" + raw + "Acceleration : " + Arrays.toString(values) + " 	Pressure : " + pressure;

				Thread.sleep(1000 - MPL115A2.BOTH_CONVERSION_TIME);
				Thread.sleep(1000);
			} catch (Exception e) {
				kata2 = "error";
			}
			Thread.sleep(500);
			return kata2;
		}
	}
	
//	Menerima dengan receive biasa
	public static void receive() throws Exception {
		System.out.println("Text_Receiver");

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
					try {
						Frame f = null;
						try {
							f = new Frame();
							radio.setState(AT86RF231.STATE_RX_AACK_ON);
							radio.waitForFrame(f);
							Misc.LedBlinker(orange, 100, false);
							
						} catch (Exception e) {
						}
	
						if (f != null) {
							byte[] dg = f.getPayload();
							String str = new String(dg, 0, dg.length);
//							send(str,BASE,f);
//							broadcastMSG(str,f);
//							Thread.sleep(800);
							
							System.out.println("FROM:" + str);
//							send(str,ADDR_SEND,f);
							
						}
					}catch(Exception e) {}
				}
			}
		};
		reader.start();
		
		while (true) {
			Misc.sleep(1000);
		}
	}
	
	//Pengiriman Send Biasa
	public static void send(String msg,int dest, Frame frame) throws Exception {
		final Shuttle shuttle = Shuttle.getInstance();

		AT86RF231 radio = RadioInit.initRadio();
		radio.setChannel(COMMON_CHANNEL);
		radio.setPANId(COMMON_PANID);
		radio.setShortAddress(ADDR_RESV);

		LED green = shuttle.getLED(Shuttle.LED_GREEN);
		green.open();

		LED red = shuttle.getLED(Shuttle.LED_GREEN);
		red.open();

		Console console = new Console();
		msg = pesan();
		
		System.out.print("MASUK KE WHILE PERTAMA DI NODE");
		while (true) {
			
			for (int i = 0; i < Integer.MAX_VALUE; i++) {
				boolean isOK = false;
				while (!isOK) {
					try {
						// ///////////////////////////////////////////////////////////////////////
						frame = new Frame(Frame.TYPE_DATA | Frame.ACK_REQUEST
								| Frame.DST_ADDR_16 | Frame.INTRA_PAN | Frame.SRC_ADDR_16);
						
						frame.setSrcAddr(ADDR_RESV);
						frame.setSrcPanId(COMMON_PANID);
						frame.setDestAddr(ADDR_SEND);
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
	//mengirim memakai FilterFrame
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
	
	 //menerima memakai filter Frame IO
	private void receiveFilterFrame(final FilterFrameIO filterframe) {
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
	
	//mengirim memakai Thread Frame IO
	public void sendThreadFrame(final ThreadedFrameIO threadIO)throws Exception  {
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
	
	//menerima memakai Thread Frame IO
	private void receiveThreadFrame(final ThreadedFrameIO threadIO) {
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
	
	//mengirim memakai FrameIO
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
	
	//menerima memakai FrameIO
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
	
	public static void main(String[] args) throws Exception {
		BaseStation.useUSART();
		final Frame frame = new Frame();
		AT86RF231 t = Node.getInstance().getTransceiver();
		t.open();
		t.setAddressFilter(COMMON_PANID , ADDR_RESV, ADDR_RESV, false);


		final RadioDriver radioDriver = new AT86RF231RadioDriver(t);
		final FrameIO fio = new RadioDriverFrameIO(radioDriver);
		final FilterFrameIO filterframe = new FilterFrameIO(fio);
		final ThreadedFrameIO threadframe = new ThreadedFrameIO(fio);
		pesan = pesan();
		new Thread() {
			public void run() {
				try {
					receive();
					send(pesan(),ADDR_SEND,frame);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}
} 	

	