

import com.virtenio.driver.device.ADT7410;
import com.virtenio.driver.device.ADXL345;
import com.virtenio.driver.device.MPL115A2;
import com.virtenio.driver.device.SHT21;
import com.virtenio.driver.device.at86rf231.AT86RF231;
import com.virtenio.driver.device.at86rf231.AT86RF231RadioDriver;
import com.virtenio.driver.gpio.GPIO;
import com.virtenio.driver.i2c.NativeI2C;
import com.virtenio.misc.PropertyHelper;
import com.virtenio.preon32.node.Node;
import com.virtenio.preon32.shuttle.Shuttle;
import com.virtenio.radio.ieee_802_15_4.Frame;
import com.virtenio.radio.ieee_802_15_4.FrameIO;
import com.virtenio.radio.ieee_802_15_4.RadioDriver;
import com.virtenio.radio.ieee_802_15_4.RadioDriverFrameIO;

/**
 * Demonstrates radio transmit and receive using FrameIO classes which eases
 * eases transmit and receive and which is hardware independent.
 */
public class RadioDriverFrameIOReceive{
	
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
			t.setAddressFilter(COMMON_PANID , ADDR_RESV, ADDR_RESV, false);


			final RadioDriver radioDriver = new AT86RF231RadioDriver(t);
			final FrameIO fio = new RadioDriverFrameIO(radioDriver);
			final RadioDriverFrameIO radioFrame = new RadioDriverFrameIO(radioDriver);
			// start receiver
			runReceiver(radioFrame);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create and start a receiver thread which uses the given FrameIO instance.
	 */
	private void runReceiver(final RadioDriverFrameIO radioDriver) {
		// create empty frame used by the receiver
		Frame frame = new Frame();
		for (;;) {
			try {
				// receive a frame
				radioDriver.receive(frame);
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


	/**
	 * Main method to execute as an application.
	 */
	public static void main(String[] args) {
		new RadioDriverFrameIOReceive().run();
	}
}
