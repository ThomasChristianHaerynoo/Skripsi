

import com.virtenio.driver.device.at86rf231.AT86RF231;
import com.virtenio.driver.device.at86rf231.AT86RF231RadioDriver;
import com.virtenio.preon32.node.Node;
import com.virtenio.radio.ieee_802_15_4.Frame;
import com.virtenio.radio.ieee_802_15_4.FrameIO;
import com.virtenio.radio.ieee_802_15_4.RadioDriver;
import com.virtenio.radio.ieee_802_15_4.RadioDriverFrameIO;
import com.virtenio.radio.ieee_802_15_4.ThreadedFrameIO;

/**
 * Demonstrates radio transmit and receive using FrameIO classes which eases
 * eases transmit and receive and which is hardware independent.
 */
public class ThreadFrameIOReceive{
	
	private int COMMON_PANID = 0xCAFE;
	private int ADDR_RESV = 0xBABE;
	
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
			final ThreadedFrameIO threadIO = new ThreadedFrameIO(fio);
			// start receiver
			receive(threadIO);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create and start a receiver thread which uses the given FrameIO instance.
	 */
	private void receive(final ThreadedFrameIO threadIO) {
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


	/**
	 * Main method to execute as an application.
	 */
	public static void main(String[] args) {
		new ThreadFrameIOReceive().run();
	}
}
