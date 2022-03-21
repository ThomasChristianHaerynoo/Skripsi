import com.virtenio.driver.device.ADT7410;
import com.virtenio.driver.i2c.I2C;
import com.virtenio.driver.i2c.NativeI2C;
import java.util.Arrays;
import com.virtenio.driver.device.ADXL345;
import com.virtenio.driver.gpio.GPIO;
import com.virtenio.driver.gpio.NativeGPIO;
import com.virtenio.driver.spi.NativeSPI;
import com.virtenio.preon32.node.Node;
import com.virtenio.radio.ieee_802_15_4.FrameIO;
import com.virtenio.radio.ieee_802_15_4.RadioDriver;
import com.virtenio.radio.ieee_802_15_4.RadioDriverFrameIO;
import com.virtenio.driver.device.MPL115A2;
import com.virtenio.driver.device.SHT21;
import com.virtenio.driver.device.at86rf231.AT86RF231;
import com.virtenio.driver.device.at86rf231.AT86RF231RadioDriver;


public class Sense {
	private NativeI2C i2c;
	private SHT21 sht21;
	
	private MPL115A2 pressureSensor;
	
	private ADXL345 accelerationSensor;
	private GPIO accelIrqPin1;
	private GPIO accelIrqPin2;
	private GPIO accelCs;
	
	private ADT7410 temperatureSensor;
	
	
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


//		System.out.println("Done(Init)");
	}
	
		
	public String sensing() throws Exception {
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
	

	public static void main(String[] args) throws Exception {
		new Sense().sensing();
	}

}
