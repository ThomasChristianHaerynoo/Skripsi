/*
 * Copyright (c) 2011., Virtenio GmbH
 * All rights reserved.
 *
 * Commercial software license.
 * Only for test and evaluation purposes.
 * Use in commercial products prohibited.
 * No distribution without permission by Virtenio.
 * Ask Virtenio for other type of license at info@virtenio.de
 *
 * Kommerzielle Softwarelizenz.
 * Nur zum Test und Evaluierung zu verwenden.
 * Der Einsatz in kommerziellen Produkten ist verboten.
 * Ein Vertrieb oder eine Veröffentlichung in jeglicher Form ist nicht ohne Zustimmung von Virtenio erlaubt.
 * Für andere Formen der Lizenz nehmen Sie bitte Kontakt mit info@virtenio.de auf.
 */

import java.util.Arrays;

//import com.virtenio.driver.button.Button;
import com.virtenio.driver.device.ADT7410;
import com.virtenio.driver.device.ADXL345;
import com.virtenio.driver.device.MPL115A2;
import com.virtenio.driver.device.SHT21;
import com.virtenio.driver.device.at86rf231.AT86RF231;
import com.virtenio.driver.gpio.GPIO;
import com.virtenio.driver.gpio.NativeGPIO;
import com.virtenio.driver.i2c.I2C;
import com.virtenio.driver.i2c.NativeI2C;
import com.virtenio.driver.led.LED;
import com.virtenio.driver.spi.NativeSPI;
import com.virtenio.io.Console;
import com.virtenio.preon32.examples.common.Misc;
import com.virtenio.preon32.examples.common.RadioInit;
import com.virtenio.preon32.shuttle.Shuttle;
import com.virtenio.radio.ieee_802_15_4.Frame;
import com.virtenio.radio.ieee_802_15_4.FrameControl;
import com.virtenio.radio.ieee_802_15_4.BeaconFrameView;
import com.virtenio.radio.ieee_802_15_4.BeaconFrameView.GTSDirectionsMask;
import com.virtenio.radio.ieee_802_15_4.FrameView;

/**
 * Einfaches Beispiel der Funkübertragung mit Senden und Empfangen.
 */
public class Beacon extends FrameView{

	/** Interne Variablen */
//	private static int COMMON_CHANNEL = 24;
//	private static int COMMON_PANID = 0xCAFE;
//	private static int ADDR_SEND = 0xAFFE;
//	private static int ADDR_RESV = 0xBABE;
	private NativeI2C i2c;
	private SHT21 sht21;
	private MPL115A2 pressureSensor;
	private ADXL345 accelerationSensor;
	private GPIO accelIrqPin1;
	private GPIO accelIrqPin2;
	private GPIO accelCs;
	private ADT7410 temperatureSensor;
	
	public  BeaconFrameView bfv;
	public Frame frame;
	public BeaconFrameView.GTSDirectionsMask bfvDir;
	public BeaconFrameView.GTSDescriptor bfvDes;
	public BeaconFrameView.GTSSpec spec;
	public BeaconFrameView.PendingAddrSpec pend;
	public BeaconFrameView.SuperFrameSpec sup;
	
	public Beacon() {
		this.frame =new Frame(Frame.TYPE_DATA | Frame.ACK_REQUEST
				| Frame.DST_ADDR_16 | Frame.INTRA_PAN | Frame.SRC_ADDR_16);
		this.bfv = new BeaconFrameView(frame);
		this.bfvDir = new BeaconFrameView.GTSDirectionsMask(1);
		this.bfvDes = new BeaconFrameView.GTSDescriptor(1);
		this.spec = new BeaconFrameView.GTSSpec(1);
		this.pend = new BeaconFrameView.PendingAddrSpec(1);
		this.sup = new BeaconFrameView.SuperFrameSpec(1);
//		this.frameview = new FrameView();
	}
	
//	public void init() throws Exception {
//
//		i2c = NativeI2C.getInstance(1);
//		i2c.open(I2C.DATA_RATE_400);
//
//
//		temperatureSensor = new ADT7410(i2c, ADT7410.ADDR_0, null, null);
//		temperatureSensor.open();
//		temperatureSensor.setMode(ADT7410.CONFIG_MODE_CONTINUOUS);
//
//		
//
//
//		accelIrqPin1 = NativeGPIO.getInstance(37);
//		accelIrqPin2 = NativeGPIO.getInstance(25);
//		accelCs = NativeGPIO.getInstance(20);
//		
//
//		NativeSPI spi = NativeSPI.getInstance(0);
//		spi.open(ADXL345.SPI_MODE, ADXL345.SPI_BIT_ORDER, ADXL345.SPI_MAX_SPEED);
//		
//
//		accelerationSensor = new ADXL345(spi, accelCs);
//		
//		accelerationSensor.open();
//		accelerationSensor.setDataFormat(ADXL345.DATA_FORMAT_RANGE_2G);
//		accelerationSensor.setDataRate(ADXL345.DATA_RATE_3200HZ);
//		accelerationSensor.setPowerControl(ADXL345.POWER_CONTROL_MEASURE);
//
//	
//
//		GPIO resetPin = NativeGPIO.getInstance(24);
//		GPIO shutDownPin = NativeGPIO.getInstance(12);
//
//
//		pressureSensor = new MPL115A2(i2c, resetPin, shutDownPin);
//		pressureSensor.open();
//		pressureSensor.setReset(false);
//		pressureSensor.setShutdown(false);
//		
//
//	}
//	
//		
//	public String pesan() throws Exception {
//		init();
//		short[] values = new short[3];
//		String kata2 = "";
//		while (true) {
//			try {
//				int raw = temperatureSensor.getTemperatureRaw();
//				float celsius = temperatureSensor.getTemperatureCelsius();
//				accelerationSensor.getValuesRaw(values, 0);
//				pressureSensor.startBothConversion();
//				Thread.sleep(MPL115A2.BOTH_CONVERSION_TIME);
//				
//				int pressurePr = pressureSensor.getPressureRaw();
//				int tempRaw = pressureSensor.getTemperatureRaw();
//				float pressure = pressureSensor.compensate(pressurePr, tempRaw);
//				Thread.sleep(1000);
//				
//				kata2 =  "Suhu : " + celsius + " [°C]" + ", raw=" + raw + "Acceleration : " + Arrays.toString(values) + " 	Pressure : " + pressure;
//
//				Thread.sleep(1000 - MPL115A2.BOTH_CONVERSION_TIME);
//				Thread.sleep(1000);
//			} catch (Exception e) {
//				kata2 = "error";
//			}
//			Thread.sleep(500);
//			return kata2;
//		}
//	}
	
	public void returnValue() throws Exception {
		final Shuttle shuttle = Shuttle.getInstance();

		AT86RF231 radio = RadioInit.initRadio();
//		radio.setChannel(COMMON_CHANNEL);
//		radio.setPANId(COMMON_PANID);
//		radio.setShortAddress(ADDR_SEND);

		LED green = shuttle.getLED(Shuttle.LED_GREEN);
		green.open();

		LED red = shuttle.getLED(Shuttle.LED_GREEN);
		red.open();
		
		
		String msg = "Test";
//		this.bfv.setFrame(frame);		
		System.out.println("Value DirectionMask = " + this.bfvDir.getValue());
		
		
		System.out.println("Short Address Device = " + this.bfvDes.getDeviceShortAddr());
		System.out.println("GTS Length = " + this.bfvDes.getGTSLength());
		System.out.println("GTS Starting Slot = " + this.bfvDes.getGTSStartingSlot());
		System.out.println("Descriptor Value = " + this.bfvDes.getValue());
		
		System.out.println("Spec Count  = " + this.spec.getDescriptorCount());
		System.out.println("Info Length GTS  = " + this.spec.getInfoLength());
		System.out.println("Spec Value  = " + this.spec.getValue());
		System.out.println("Permit  = " + this.spec.isPermit());
		
		System.out.println("Pending Short and Long Address  = " + this.pend.getAddrCount());
		System.out.println("Jumlah byte Pending Address  = " + this.pend.getInfoLength());
		System.out.println("Jumlah Pending Long Address   = " + this.pend.getLongAddrCount());
		System.out.println("Jumlah Pending Short Address  = " + this.pend.getShortAddrCount());
		System.out.println("Value Pending Address Specification  = " + this.pend.getLongAddrCount());
		
		System.out.println("Beacon Order  = " + this.sup.getBeaconOrder());
		System.out.println("final CAP slot  = " + this.sup.getFinalCAPSlot());
		System.out.println("Super Frame Order Value   = " + this.sup.getSuperframeOrder());
		System.out.println("Super Frame Spec Value  = " + this.sup.getValue());
		System.out.println("Association bit set  = " + this.sup.isAssociationPermit());
		System.out.println("Battery Life Extension  = " + this.sup.isBatteryLifeExtension());
		System.out.println("Pan Coordinator  = " + this.sup.isPANCoordinator());
		
		
//		this.bfv.setGTSDirectionsMask(bfvDir);
		this.bfv.setPayload(msg.getBytes());
		System.out.println("SuperFrameSpec = " + this.bfv.getSuperFrameSpec());
		
		System.out.println("GTSDirectionMask = " + this.bfv.getGTSDirectionsMask());
		System.out.println("MaxPayloadLength = " + this.bfv.getMaxPayloadLength());
		System.out.println("Payload = " + this.bfv.getPayload());
		System.out.println("GTSSPEC = " + this.bfv.getGTSSpec());
		System.out.println("Payload Length = " + this.bfv.getPayloadLength());
		System.out.println("Payload Offset = " + this.bfv.getPayloadOffset());
		System.out.println("Pending Address Specification = " + this.bfv.getPendingAddrSpec());
		System.out.println("Super Frame Spec = " + this.bfv.getSuperFrameSpec());
		
//		System.out.println("Address Pending = " + this.bfv.getPendingAddr(1));
//		System.out.println("GTSDescriptor = " + this.bfv.getGTSDescriptor(1));
//		System.out.println("Receive Only  = " + this.bfvDir.isReceiveOnly(1));
//		System.out.println("Transmit Only  = " + this.bfvDir.isTransmitOnly(1));
		
		
		System.out.println("enter");
	}

	public static void main(String[] args) throws Exception {
		Console console = new Console();
			while(true) {
			new Beacon().returnValue();
			}
		
		}
}
