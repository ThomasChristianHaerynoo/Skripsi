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

import java.io.IOException;

import com.virtenio.driver.button.Button;
import com.virtenio.driver.device.at86rf231.AT86RF231;
import com.virtenio.driver.led.LED;
import com.virtenio.io.Console;
import com.virtenio.preon32.examples.common.Misc;
import com.virtenio.preon32.examples.common.RadioInit;
import com.virtenio.preon32.shuttle.Shuttle;
import com.virtenio.radio.ieee_802_15_4.Frame;
import com.virtenio.radio.ieee_802_15_4.PayloadInputStream;
import com.virtenio.radio.ieee_802_15_4.PayloadOutputStream;
/**
 * Einfaches Beispiel der Funkübertragung mit Senden und Empfangen.
 */
public class PayloadReceive {

	/** Interne Variablen */
	private int COMMON_CHANNEL = 24;
	private int COMMON_PANID = 0xCAFE;
	private int ADDR_SEND = 0xAFFE;
	private int ADDR_RESV = 0xBABE;
	public PayloadInputStream pis;
	public PayloadOutputStream pos;
	public Frame frame;
	public byte[] buffer;
	public int offSet;
	public int count;
	public String hasil;
	
		
	public PayloadReceive() {
		frame = new Frame(Frame.TYPE_DATA | Frame.ACK_REQUEST
				| Frame.DST_ADDR_16 | Frame.INTRA_PAN | Frame.SRC_ADDR_16);
		this.pis = new PayloadInputStream(frame,0);		
		this.pos = new PayloadOutputStream(frame,0);
	}

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
			@Override
			public void run() {
				while (true) {
					Frame f = null;
					try {
						f = new Frame();
						radio.setState(AT86RF231.STATE_RX_AACK_ON);
						radio.waitForFrame(f);
						Misc.LedBlinker(orange, 100, false);
						
					} catch (Exception e) {
					}

					if (f != null) {
						
						
//						String str = new String(dg, 0, dg.length);
						pis.setFrame(f, 1);
						Frame frameBaru = pis.getFrame();
						byte[] dg = frameBaru.getPayload();
						buffer = dg;
						try {
							int read = pis.read(buffer, 1,buffer.length);
							hasil = Integer.toHexString(read);
							System.out.println(hasil);	
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
	//					String hex_addr = Integer.toHexString((int) f.getSrcAddr());
	//					System.out.println("FROM:" + str);					
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
				new PayloadReceive().prog_receiver();
			}
		}
	}

