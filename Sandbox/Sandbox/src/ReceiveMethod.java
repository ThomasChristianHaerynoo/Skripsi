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
import com.virtenio.driver.device.at86rf231.AT86RF231RadioDriver;
import com.virtenio.driver.led.LED;
import com.virtenio.io.Console;
import com.virtenio.preon32.examples.common.Misc;
import com.virtenio.preon32.examples.common.RadioInit;
import com.virtenio.preon32.node.Node;
import com.virtenio.preon32.shuttle.Shuttle;
import com.virtenio.radio.ieee_802_15_4.Frame;
import com.virtenio.radio.ieee_802_15_4.FrameIO;
import com.virtenio.radio.ieee_802_15_4.RadioDriver;
import com.virtenio.radio.ieee_802_15_4.RadioDriverFrameIO;
import com.virtenio.radio.ieee_802_15_4.ThreadedFrameIO;

/**
 * Einfaches Beispiel der Funkübertragung mit Senden und Empfangen.
 */
public class ReceiveMethod {

	/** Interne Variablen */
	private int COMMON_CHANNEL = 24;
	private int COMMON_PANID = 0xCAFE;
	private int ADDR_SEND = 0xAFFE;
	private int ADDR_RESV = 0xBABE;
	
	public ReceiveMethod() {
		
	}
	
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
			runReceiver(fio);
		} catch (Exception e) {
			e.printStackTrace();
		}
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
//					Frame f2 = null;
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
	//					String hex_addr = Integer.toHexString((int) f.getSrcAddr());
						System.out.println("FROM:" + str);					
					}
				}
			}
		};
		reader.start();

		while (true) {
			Misc.sleep(1000);
		}
	}
	
	private void runReceiver(final FrameIO fio) {
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


	public static void main(String[] args) throws Exception {
		Console console = new Console();
		while (true) {
				new ReceiveMethod().prog_receiver();
			}
		}
	}

