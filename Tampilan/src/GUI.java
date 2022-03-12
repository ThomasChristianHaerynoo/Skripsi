
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.SpringLayout;
import javax.swing.text.DefaultCaret;
import java.awt.Canvas;
import javax.swing.JScrollPane;

import javax.imageio.ImageIO;


//import com.fazecast.jSerialComm.*;
//import com.virtenio.commander.io.SerialPortSpec;

public class GUI{

	// atribut untuk UI
	JFrame frame;
	private JButton SendFrame, SendFrameIO, SendThreadFrameIO;
			//stopButton

	private static JTextArea log;

	private AppController controller;

	private String msg;
	private JButton btnNewButton_2;
	private JButton btnNewButton_3;
	private JButton FrameControl;
	private JButton RadioDriverFrameIO;
	private JButton BeaconFrameView;
	private JButton FilterFrameIO;

	/**
	 * Launch the application.
	 * 
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GUI window = new GUI();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

	}

	/**
	 * Create the application.
	 * @throws IOException 
	 */
	public GUI() throws IOException {
		this.controller = new AppController();
		this.print("\n");
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 * @throws IOException 
	 */
	private void initialize() throws IOException {
		
		frame = new JFrame();
		frame.setTitle("Preon32 GUI");
		frame.setBounds(100, 100, 837, 550);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);


		
		SendFrame = new JButton("Frame");
		SendFrame.setBounds(482, 131, 162, 23);
		frame.getContentPane().add(SendFrame);

		// menambahkan action listener untuk restart button
		//
		SendFrame.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				btnSensorPress(1);
			}
		});

		// inisialisasi untuk UI bagian bawah

		// inisialisasi untuk Scoll Pane
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(20, 371, 791, 129);
		frame.getContentPane().add(scrollPane);

		// inisialisasi untuk Text Are (log)
		log = new JTextArea();
		DefaultCaret caret = (DefaultCaret) log.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		scrollPane.setViewportView(log);

		SendFrameIO = new JButton("FrameIO");
		SendFrameIO.setBounds(482, 165, 162, 23);
		frame.getContentPane().add(SendFrameIO);

		// menambahkan action listener untuk upload button
		SendFrameIO.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				btnSensorPress(3);

			}
		});

		SendThreadFrameIO = new JButton("ThreadFrameIO");
		SendThreadFrameIO.setBounds(482, 97, 162, 23);
		frame.getContentPane().add(SendThreadFrameIO);
		SendThreadFrameIO.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnSensorPress(4);
			}
		});
		
//		JButton btnNewButton = new JButton("Sensing");
//		btnNewButton.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				btnSensorPress(5);
//			}
//		});
//		btnNewButton.setBounds(482, 63, 162, 23);
//		frame.getContentPane().add(btnNewButton);
		
		FilterFrameIO = new JButton("FilterFrameIO");
		FilterFrameIO .addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnSensorPress(6);
			}
		});
		FilterFrameIO .setBounds(654, 63, 157, 23);
		frame.getContentPane().add(FilterFrameIO );
		
		BeaconFrameView= new JButton("BeaconFrameView");
		BeaconFrameView.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnSensorPress(7);
			}
		});
		BeaconFrameView.setBounds(654, 97, 157, 23);
		frame.getContentPane().add(BeaconFrameView);
		
		FrameControl = new JButton("FrameControl");
		FrameControl.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnSensorPress(8);
			}
		});
		FrameControl.setBounds(654, 131, 157, 23);
		frame.getContentPane().add(FrameControl);
		
		RadioDriverFrameIO = new JButton("RadioDriverFrameIO");
		RadioDriverFrameIO.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnSensorPress(9);
			}
		});
		RadioDriverFrameIO.setBounds(654, 165, 157, 23);
		frame.getContentPane().add(RadioDriverFrameIO);
		
	}


	private void btnSensorPress(int input) {
		try {
			String msg = "";
//				if (input == 5) {
//					msg = "Preon32 akan melakukan sensing. \n";
//						
//				}else 
				if(input == 1) {
					msg = "Preon akan mengirimkan data. \n";
				} 
				else if(input == 3) {
					msg = "Preon akan mengirimkan data menggunakan Frame IO. \n";
				}
				else if(input == 4) {
					msg = "Preon akan mengirimkan data menggunakan ThreadFrame IO. \n";
				}
				
				else if(input == 6) {
					msg = "Preon akan mengirimkan data menggunakan Filter Frame IO. \n";
				} 
				else if(input == 7) {
					msg = "Preon akan mengecek beacon. \n";
				} 
				else if(input == 8) {
					msg = "Preon akan mengecek frame control. \n";
				} 
				this.print(msg);
				log.append(msg);
				this.controller.sensor(input);

				msg = this.controller.getPesan();
				this.print(msg);
				log.append(msg);
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}


	/**
	 * Method untuk print sebuah string, digunakan saat proses debug
	 * 
	 * @param s adalah string yang akan ditampilakn
	 */
	public void print(String s) {
		System.out.println(s);
	}
}
