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

//package com.virtenio.preon32.examples.advanced.radio.sendreceive;

import com.virtenio.driver.button.Button;
import com.virtenio.driver.device.at86rf231.AT86RF231;
import com.virtenio.driver.led.LED;
import com.virtenio.io.Console;
import com.virtenio.preon32.examples.common.Misc;
import com.virtenio.preon32.examples.common.RadioInit;
import com.virtenio.preon32.shuttle.Shuttle;
import com.virtenio.radio.ieee_802_15_4.Frame;
import com.virtenio.radio.ieee_802_15_4.AES128Provider;
/**
 * Einfaches Beispiel der Funkübertragung mit Senden und Empfangen.
 */
public class  ReceiveDecrypt {

	/** Interne Variablen */
	private int COMMON_CHANNEL = 24;
	private int COMMON_PANID = 0xCAFE;
	private int ADDR_SEND = 0xAFFE;
	private int ADDR_RESV = 0xBABE;
	
	public AES128Provider aes;
	public String result;
//	public byte[] src = new byte[1024];
//	private static byte[] dest = new byte[1024];
//	public byte[] hasil = new byte[1024];
//	public int offSrc;
//	public int offDest;
//	public int length;

	
//	public ReceiveDecrypt() {
//		this.offDest = 0;
//		this.offSrc = 0;
//		this.length = 1024;
//	}
	
	/** Ein Programme, dass über das Startmenu aufgerufen werden kann */

	public void prog_receiver() throws Exception {
		System.out.println("Text_Receiver");

		final Shuttle shuttle = Shuttle.getInstance();

		final AT86RF231 radio = RadioInit.initRadio();
		radio.setChannel(COMMON_CHANNEL);
		radio.setPANId(COMMON_PANID);
		radio.setShortAddress(ADDR_RESV);

		final LED orange = shuttle.getLED(Shuttle.LED_ORANGE);
		orange.open();

		Thread reader = new Thread() {
			
			private byte[] dest;
			public byte[] hasil = new byte[1024];
			public int offSrc;
			public int offDest;
			public int length;
			
			@Override
			public void run() {
				while (true) {
					Frame f = null;
					this.offDest = 0;
					this.offSrc = 0;
					this.length = 1024;
					try {
						f = new Frame();
						radio.setState(AT86RF231.STATE_RX_AACK_ON);
						radio.waitForFrame(f);
						Misc.LedBlinker(orange, 100, false);
						
					} catch (Exception e) {
					}

					if (f != null) {
						byte[] dg = f.getPayload();
						this.dest = dg;
						try {
							aes.decrypt(this.dest,this.offSrc, this.hasil, this.offDest, this.length);
							result = String.valueOf(hasil);
							System.out.println(hasil);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
//						String str = new String(dg, 0, dg.length);
//	//					String hex_addr = Integer.toHexString((int) f.getSrcAddr());
//						System.out.println("FROM:" + str);					
					}
				}
			}
		};
		reader.start();

		while (true) {
			Misc.sleep(1000);
		}
	}

	public static void main(String[] args) throws Exception {
		Console console = new Console();
		while (true) {
				new ReceiveDecrypt().prog_receiver();
			}
		}
	}

