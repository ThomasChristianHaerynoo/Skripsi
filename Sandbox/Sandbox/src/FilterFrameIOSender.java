

import java.util.Arrays;

import com.virtenio.driver.device.ADT7410;
import com.virtenio.driver.device.ADXL345;
import com.virtenio.driver.device.MPL115A2;
import com.virtenio.driver.device.SHT21;
import com.virtenio.driver.device.at86rf231.AT86RF231;
import com.virtenio.driver.device.at86rf231.AT86RF231RadioDriver;
import com.virtenio.driver.gpio.GPIO;
import com.virtenio.driver.gpio.NativeGPIO;
import com.virtenio.driver.i2c.I2C;
import com.virtenio.driver.i2c.NativeI2C;
import com.virtenio.driver.spi.NativeSPI;
import com.virtenio.misc.PropertyHelper;
import com.virtenio.preon32.node.Node;
import com.virtenio.preon32.shuttle.Shuttle;
import com.virtenio.radio.ieee_802_15_4.Frame;
import com.virtenio.radio.ieee_802_15_4.FrameIO;
import com.virtenio.radio.ieee_802_15_4.RadioDriver;
import com.virtenio.radio.ieee_802_15_4.RadioDriverFrameIO;
import com.virtenio.radio.ieee_802_15_4.FilterFrameIO;

/**
 * Demonstrates radio transmit and receive using FrameIO classes which eases
 * eases transmit and receive and which is hardware independent.
 */
public class FilterFrameIOSender {
	
	private int COMMON_CHANNEL = 24;
	private int COMMON_PANID = 0xCAFE;
	private int ADDR_SEND = 0xAFFE;
	private int ADDR_RESV = 0xBABE;
	
	private NativeI2C i2c;
	private SHT21 sht21;
	private MPL115A2 pressureSensor;
	private ADXL345 accelerationSensor;
	private GPIO accelIrqPin1;
	private GPIO accelIrqPin2;
	private GPIO accelCs;
	private ADT7410 temperatureSensor;

	/**
	 * Method to run the example.
	 */
	public void run() {

		try {
			// open and configure the AT86RF231 transceiver
				AT86RF231 t = Node.getInstance().getTransceiver();
				t.open();
				t.setAddressFilter(COMMON_PANID , ADDR_SEND , ADDR_SEND , false);

			// Create a FrameIO instance based on the AT86RF231. After the
			// FrameIO is created transmit and receive is hardware independent
			// if you only use the FrameIO instance.
			final RadioDriver radioDriver = new AT86RF231RadioDriver(t);
			final FrameIO fio = new RadioDriverFrameIO(radioDriver);
			final FilterFrameIO filterframe = new FilterFrameIO(fio);

			// start transmitter
			startTransmitter(filterframe);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void init() throws Exception {
//		System.out.println("I2C(Init)");
		i2c = NativeI2C.getInstance(1);
		i2c.open(I2C.DATA_RATE_400);

//		System.out.println("ADT7410(Init)");
		temperatureSensor = new ADT7410(i2c, ADT7410.ADDR_0, null, null);
		temperatureSensor.open();
		temperatureSensor.setMode(ADT7410.CONFIG_MODE_CONTINUOUS);

		
//		System.out.println("GPIO(Init)");

		accelIrqPin1 = NativeGPIO.getInstance(37);
		accelIrqPin2 = NativeGPIO.getInstance(25);
		accelCs = NativeGPIO.getInstance(20);
		
		//SPI init
//		System.out.println("SPI(Init)");
		NativeSPI spi = NativeSPI.getInstance(0);
		spi.open(ADXL345.SPI_MODE, ADXL345.SPI_BIT_ORDER, ADXL345.SPI_MAX_SPEED);
		
//		System.out.println("ADXL345(Init)");
		accelerationSensor = new ADXL345(spi, accelCs);	
		accelerationSensor.open();
		accelerationSensor.setDataFormat(ADXL345.DATA_FORMAT_RANGE_2G);
		accelerationSensor.setDataRate(ADXL345.DATA_RATE_3200HZ);
		accelerationSensor.setPowerControl(ADXL345.POWER_CONTROL_MEASURE);

	
//		System.out.println("GPIO(Init)");
		GPIO resetPin = NativeGPIO.getInstance(24);
		GPIO shutDownPin = NativeGPIO.getInstance(12);

//		System.out.println("MPL115A2(Init)");
		pressureSensor = new MPL115A2(i2c, resetPin, shutDownPin);
		pressureSensor.open();
		pressureSensor.setReset(false);
		pressureSensor.setShutdown(false);


//		System.out.println("SHT21(Init)");
		sht21 = new SHT21(i2c);
		sht21.open();
		sht21.setResolution(SHT21.RESOLUTION_RH12_T14);
		sht21.reset();
		
//		System.out.println("Done(Init)");
	}
	
		
	public String sense() throws Exception {
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
				

				kata2 =  "Suhu : " + celsius + " [°C]" + ", raw=" + raw + " Acceleration : " + Arrays.toString(values) + " Pressure : " + pressure;
				Thread.sleep(1000 - MPL115A2.BOTH_CONVERSION_TIME);
				Thread.sleep(1000);
			} catch (Exception e) {
				kata2 = "error";
			}
			Thread.sleep(500);
			return kata2;
		}
	}
	
	/**
	 * Create and start a transmitter thread which uses the given FrameIO
	 * instance.
	 */
	public void startTransmitter(final FilterFrameIO filterframe)throws Exception  {
		final String msg = sense();
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

	/**
	 * Main method to execute as an application.
	 */
	public static void main(String[] args) {
		new FilterFrameIOSender().run();
	}
}
