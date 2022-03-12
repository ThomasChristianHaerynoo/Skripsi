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
 * Ein Vertrieb oder eine Ver�ffentlichung in jeglicher Form ist nicht ohne Zustimmung von Virtenio erlaubt.
 * F�r andere Formen der Lizenz nehmen Sie bitte Kontakt mit info@virtenio.de auf.
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
import com.virtenio.radio.ieee_802_15_4.PayloadOutputStream;
import com.virtenio.radio.ieee_802_15_4.PayloadInputStream;
/**
 * Einfaches Beispiel der Funk�bertragung mit Senden und Empfangen.
 */
public class Readandwrite {

	/** Interne Variablen */
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
	
	public PayloadInputStream pis;
	public PayloadOutputStream pos;
	public Frame frame;
	
	public Readandwrite() {
		 frame = new Frame(Frame.TYPE_DATA | Frame.ACK_REQUEST
					| Frame.DST_ADDR_16 | Frame.INTRA_PAN | Frame.SRC_ADDR_16);
		 pis = new PayloadInputStream(frame);
		 pos = new PayloadOutputStream(frame);
		 
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
//				sht21.startRelativeHumidityConversion();
//				Thread.sleep(1000);
//				
//				int rawRH = sht21.getRelativeHumidityRaw();
//				float rh = SHT21.convertRawRHToRHw(rawRH);
//				
//				// temperature conversion
//				sht21.startTemperatureConversion();
//				Thread.sleep(1000);
//				int rawT = sht21.getTemperatureRaw();
//				float t = SHT21.convertRawTemperatureToCelsius(rawT);
//				
//
//				kata2 =  "Suhu : " + celsius + " [�C]" + ", raw=" + raw + "Acceleration : " + Arrays.toString(values) + " 	Pressure : " + pressure;
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
//	 q
	public void bacaTulis() throws Exception {
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
		String msg = "Test";
						
						byte[] buffer = new byte[1024];
						buffer = msg.getBytes();
						
						 try {
							 this.pis.setFrame(frame, 1000);
							 int baca = this.pis.read(buffer, 1, 10000);
							 this.pos.setFrame(frame, 1000);
							 this.pos.write(buffer, 1, 1000);
							 
							 System.out.println(baca);
						 } catch (Exception e) {
								System.out.println("gagal");
						} 
						
	}

		
		
	

	public static void main(String[] args) throws Exception {
		Console console = new Console();
		while (true) {
				new Readandwrite().bacaTulis();
			}
		
		}
}