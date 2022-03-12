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
//import com.virtenio.driver.device.ADT7410;
//import com.virtenio.driver.device.ADXL345;
//import com.virtenio.driver.device.MPL115A2;
//import com.virtenio.driver.device.SHT21;
//import com.virtenio.driver.device.at86rf231.AT86RF231;
//import com.virtenio.driver.gpio.GPIO;
//import com.virtenio.driver.gpio.NativeGPIO;
//import com.virtenio.driver.i2c.I2C;
//import com.virtenio.driver.i2c.NativeI2C;
//import com.virtenio.driver.led.LED;
//import com.virtenio.driver.spi.NativeSPI;
import com.virtenio.io.Console;
//import com.virtenio.preon32.examples.common.Misc;
//import com.virtenio.preon32.examples.common.RadioInit;
//import com.virtenio.preon32.shuttle.Shuttle;
import com.virtenio.radio.ieee_802_15_4.Address;
import com.virtenio.radio.ieee_802_15_4.PANAddress;

/**
 * Einfaches Beispiel der Funkübertragung mit Senden und Empfangen.
 */
public class Alamat{

	/** Interne Variablen */
	public Address ads;
	public long alamat;
	public PANAddress pan;
	
	public Alamat() {
		ads = new Address(100);
		pan = new PANAddress(0xCAFE,ads);
	}
	public long getAddressLong() {
		long res = 0;
		res = ads.getValue();
		return res;
		
	}
	
	public boolean tipeAddress() {
		boolean res = false;
		res = ads.isLong();
		return res;
	}
	
	public String alamatBaru() {
		String res = "";
		res = ads.toString();
		return res;
	}
	
	public Address getAlamatDalamPANID() {
		Address res;
		res = pan.getAddress();
		return res;
	}
	
	public int panid() {
		int res;
		res = pan.getPanId();
		return res;
	}
		
	public boolean tipeAddressPan() {
		boolean res;
		res = pan.isLong();
		return res;
	}
	
	public String alamatBarupPan() {
		String res = "";
		res = pan.toString();
		return res;
	}
//	public void returnValue() throws Exception {	
//		System.out.println("Alamat = " + this.ads.getValue());
//		System.out.println("Apakah Long = " + this.ads.isLong());
//		System.out.println("Alamat baru = " + this.ads.toString());
//		
//		
//	}
	
//	public void returnPan() throws Exception{
//		System.out.println("PANID = " + this.pan.getPanId());
//		System.out.println("Alamat di dalam PANID = " + this.pan.getAddress());
//		System.out.println("Hexadecimal = " + this.pan.toString());
//	}

	public static void main(String[] args) throws Exception {
		Console console = new Console();
		Alamat alamat = new Alamat();
		while (true) {
			System.out.println("alamat =  "+ alamat.getAddressLong());
			System.out.println("type alamat = " +alamat.tipeAddress());
			System.out.println("alamat baru = " +alamat.alamatBaru());
			System.out.println("PANID = " +alamat.getAlamatDalamPANID());
			System.out.println("alamat di dalam PANID = "+alamat.panid());
			System.out.println("tipe alamat di dalam PAN = " +alamat.tipeAddressPan());
			System.out.println("Alamat baru di PAN = " + alamat.alamatBarupPan());
			}
		
		}
}
