import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
//import java.util.Scanner;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

//import com.fazecast.jSerialComm.SerialPort;
import com.virtenio.commander.io.DataConnection;
import com.virtenio.commander.toolsets.preon32.Preon32Helper;

public class AppController {

	private static DataConnection dataC;
	private static BufferedInputStream inputBuffer;
	private static int input;
	private static String pesan;
	
	public AppController() {
		try {
			this.init();
//			this.context_set("context.set.1");
//			this.time_synchronize();
//			this.listPort = SerialPort.getCommPorts();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}



	public void sensor(int temp) throws IOException, InterruptedException {
		input = temp;
		send(input); 	//dataConnection.write(input);
		Thread.sleep(3000);
		input = 0;
		pesan = read(inputBuffer);
//		messageReceive = read(in);
	}

	private static String read(BufferedInputStream bfs) throws IOException {
		String res = "";
		int i = 0;
		// membaca yang di print dari sensor
		while (i != -1 && bfs.available() > 0) {
			i = bfs.read();
			res += (char) i;
		}
		return res;
	}

	public DataConnection getDataConnection() {
		return dataC;
	}

	public int getInput() {
		return input;
	}

	public String getPesan() {
		return pesan;
	}


	private void send(int input) throws IOException, InterruptedException {
		dataC.write(input);
		dataC.flush();
		Thread.sleep(3000);
	}

	/**
	 * Method untuk menulis ant log
	 * 
	 * @return kelas DefaultLogger
	 */
	private static DefaultLogger getConsoleLogger() {
		DefaultLogger consoleLogger = new DefaultLogger();
		consoleLogger.setErrorPrintStream(System.err);
		consoleLogger.setOutputPrintStream(System.out);
		consoleLogger.setMessageOutputLevel(Project.MSG_INFO);
		return consoleLogger;
	}

	/**
	 * Method untuk meng-synchronize waktu dengan sensor
	 * 
	 * @throws Exception
	 */
//	public void time_synchronize() throws Exception {
//		DefaultLogger consoleLogger = getConsoleLogger();
//		File buildFile = new File("C:\\Users\\THOMAS\\OneDrive\\Documents\\Sandbox\\Sandbox\\build.xml");
//		Project antProject = new Project();
//		antProject.setUserProperty("ant.file", buildFile.getAbsolutePath());
//		antProject.addBuildListener(consoleLogger);
//		try {
//			antProject.fireBuildStarted();
//			antProject.init();
//			ProjectHelper helper = ProjectHelper.getProjectHelper();
//			antProject.addReference("ant.ProjectHelper", helper);
//			helper.parse(antProject, buildFile);
//			String target = "cmd.time.synchronize";
//			antProject.executeTarget(target);
//			antProject.fireBuildFinished(null);
//		} catch (BuildException e) {
//		}
//	}
//
	/**
	 * Method untuk mengubah context yang ada pada buildUser pada Sandbox
	 * 
	 * @param target string target yang akan dirubah
	 * @throws Exception
	 */
	public void context_set(String target) throws Exception {
		DefaultLogger consoleLogger = getConsoleLogger();
		File buildFile = new File("C:\\Users\\THOMAS\\OneDrive\\Documents\\Sandbox\\Sandbox\\buildUser.xml");
		Project antProject = new Project();
		antProject.setUserProperty("ant.file", buildFile.getAbsolutePath());
		antProject.addBuildListener(consoleLogger);
		try {
			antProject.fireBuildStarted();
			antProject.init();
			ProjectHelper helper = ProjectHelper.getProjectHelper();
			antProject.addReference("ant.ProjectHelper", helper);
			helper.parse(antProject, buildFile);
			antProject.executeTarget(target);
			antProject.fireBuildFinished(null);
		} catch (BuildException e) {
		}
	}

	public void init() throws Exception {
		Preon32Helper nodeHelper = new Preon32Helper("COM6", 115200);
		dataC = nodeHelper.runModule("BaseStation");
		inputBuffer = new BufferedInputStream(dataC.getInputStream());

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
