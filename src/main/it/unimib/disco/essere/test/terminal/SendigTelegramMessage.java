package it.unimib.disco.essere.test.terminal;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import me.shib.java.lib.telegram.bot.service.TelegramBot;
import me.shib.java.lib.telegram.bot.types.ChatId;
import me.shib.java.lib.telegram.bot.types.Message;
import me.shib.java.lib.telegram.bot.types.Update;
import oshi.SystemInfo;
import oshi.hardware.Display;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;
import oshi.hardware.PowerSource;
import oshi.hardware.UsbDevice;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;
import oshi.util.FormatUtil;
import oshi.util.Util;

/**
 * Created by Ruben Bermedez
 */
public class SendigTelegramMessage {
	private static final Logger logger = LogManager.getLogger(SendigTelegramMessage.class);
	public static final String BASEURL = "https://api.telegram.org/bot";
	public static final String TOKEN = "171609797:AAHdgKQO6cLtFIDtT7stI7OCPK-K-nvuTEQ";
	public static final String PATH = "sendmessage";


	/// Fields of Send Message
	public static final String CHATID_FIELD = "chat_id";
	private static Integer chatId; ///< Unique identifier for the message recepient: User or GroupChat id
	public static final String TEXT_FIELD = "text";
	private static String text; ///< Text of the message to be sent
	public static final String DISABLEWEBPAGEPREVIEW_FIELD = "disable_web_page_preview";
	private static Boolean disableWebPagePreview; ///< Disables link previews for links in this message
	public static final String REPLYTOMESSAGEID_FIELD = "reply_to_message_id";
	private static Integer replyToMessageId; ///< Optional. If the message is a reply, ID of the original message
	public static final String REPLYMARKUP_FIELD = "reply_markup";

	/// Fieldsof ReplyKeyboardMarkup
	public static final String KEYBOARD_FIELD = "keyboard";
	private static List<List<String>> keyboard; ///< Array of button rows, each represented by an Array of Strings
	public static final String RESIZEKEYBOARD_FIELD = "resize_keyboard";
	/**
	 * Optional.
	 * Requests clients to resize the keyboard vertically for optimal fit
	 * (e.g., make the keyboard smaller if there are just two rows of buttons).
	 * Defaults to false.
	 */
	private static Boolean resizeKeyboard;
	public static final String ONETIMEKEYBOARD_FIELD = "one_time_keyboard";
	private static Boolean oneTimeKeyboad; ///< Optional. Requests clients to hide the keyboard as soon as it's been used. Defaults to false.
	public static final String SELECTIVE_FIELD = "selective";
	/**
	 * Optional. Use this parameter if you want to show the keyboard to specific users only.
	 * Targets:
	 *      1) users that are @mentioned in the text of the Message object;
	 *      2) if the bot's message is a reply (has reply_to_message_id), sender of the original message.
	 */
	private static Boolean selective;



	int i = 0;

	public static void main(String[] args) throws IOException {
		TelegramBot bot = TelegramBot.getInstance(TOKEN);
		logger.info("Obtained instances");
		Update[] updates;
		while((updates = bot.getUpdates()) != null) {
			for (Update update : updates) {
				Message message = update.getMessage();
				if(message != null) {
					logger.info("Sending message: "+message.getText());
					bot.sendMessage(new ChatId(message.getChat().getId()), "This is a reply from the bot! :)");
				}
			}
		}
	}
	public static void start(Path path) throws IOException{
		TelegramBot bot = TelegramBot.getInstance(TOKEN);
		logger.info("Obtained instances");
		Update[] updates;

		while((updates = bot.getUpdates()) != null) {
			//			logger.info("Reading files from path: ");
			//			//			List<Path> files = PathUtilsExtension.collectFilesWithExtension(path, ".graphml");
			//			List<File> files = listOfAllFileWithSpecifiedEnding(path.toFile(),"sytem.graphml");
			//			logger.info("Read files from path: ");
			boolean first =true;
			for (Update update : updates) {
				Message message = update.getMessage();
				if(message != null) {
					List<File> files = new ArrayList<>();
					if(first){
						logger.info("Reading files from path: ");
						//			List<Path> files = PathUtilsExtension.collectFilesWithExtension(path, ".graphml");
						
						logger.info("Read files from path: ");
						logger.info("Sending message: "+first);
						logger.info("Sending message: "+message.getFrom());
						logger.info("Sending message: "+message.getText());
						if(!"francySant".equals(message.getFrom().getUsername())){
							bot.sendMessage(new ChatId(message.getChat().getId()), "Hi "+message.getFrom().getFirst_name()+"! This is a reply from the bot! :)");
						}else{
							bot.sendMessage(new ChatId(message.getChat().getId()), "Hi "+message.getFrom().getFirst_name()+"! I love you <3");
						}
						String messageString = message.getText();
						if(messageString!=null){
							messageString = messageString.toLowerCase();
						}
						if("files".equals(messageString)){
							logger.info("Readden files from path: "+first);
							files = listOfAllFileWithSpecifiedEnding(path.toFile(),"sytem.graphml");
							bot.sendMessage(new ChatId(message.getChat().getId()), "We have generated "+countHowManyExist(files)+" sytem.graphml of "+files.size());								
						}else if("htop".equals(messageString)){
							bot.sendMessage(new ChatId(message.getChat().getId()), "System status:\n"+printLogStatInfo());								
						}else{
							bot.sendMessage(new ChatId(message.getChat().getId()), "Hi "+message.getFrom().getFirst_name()+"! Type \"htop\" to receive status of system like HTOP bash command, or \"files\" to know how many files have been generated");
						}
						first=false;
					}else{
						logger.info("Sending message: "+first);
						logger.info("Sending message: "+message.getFrom());
						logger.info("Sending message: "+message.getText());
						if(!"francySant".equals(message.getFrom().getUsername())){
							bot.sendMessage(new ChatId(message.getChat().getId()), "Hi "+message.getFrom().getFirst_name()+"! This is a reply from the bot! :)");
						}else{
							bot.sendMessage(new ChatId(message.getChat().getId()), "Hi "+message.getFrom().getFirst_name()+"! I love you <3");
						}
						String messageString = message.getText();
						if(messageString!=null){
							messageString = messageString.toLowerCase();
						}
						if("files".equals(messageString)){
							logger.info("Readden files from path: "+first);
							bot.sendMessage(new ChatId(message.getChat().getId()), "We have generated "+countHowManyExist(files)+" sytem.graphml of "+files.size());								
						}else if("htop".equals(messageString)){
							logger.info("Readden files from path: "+first);
							bot.sendMessage(new ChatId(message.getChat().getId()), "System status:\n"+printLogStatInfo());								
						}else{
							bot.sendMessage(new ChatId(message.getChat().getId()), "Hi "+message.getFrom().getFirst_name()+"! Type \"htop\" to receive status of system like HTOP bash command, or \"files\" to know how many files have been generated");
						}
					}
				}
			}
		}
	}

	public static List<File> listOfAllFileWithSpecifiedEnding(File root, String ending){
		List<File> list = new ArrayList<File>();
		if(root!=null&&root.isDirectory()){
			for(File f :root.listFiles()){
				if(f.isDirectory()){
					list.add(Paths.get(f.getAbsolutePath(),ending).toFile());
				}
			}
		}
		return list;
	}

	public static int countHowManyExist(List<File> f){
		int r = 0;
		for(File i :f){
			if(i.exists()){
				r++;
			}
		}
		return r;
	}

	public static String printLogStatInfo(){
		SystemInfo si = new SystemInfo();
		HardwareAbstractionLayer hal = si.getHardware();
		StringBuilder procCpu = new StringBuilder("CPU load per processor:\n");
		double[] load = hal.getProcessor().getProcessorCpuLoadBetweenTicks();
		for (int cpu = 0; cpu < load.length; cpu++) {
			if(cpu==4){
				procCpu.append("\n");
			}
			procCpu.append(String.format(" %.1f%%", load[cpu] * 100));
		}
		StringBuilder s =new StringBuilder(procCpu.toString()+"\n");

		// Processes
		s.append("Processes: " + hal.getProcessor().getProcessCount() + ", Threads: "+ hal.getProcessor().getThreadCount()+"\n");
		List<OSProcess> procs = Arrays.asList(hal.getProcessor().getProcesses());
		// Sort by highest CPU
		Comparator<OSProcess> cpuDescOrder = new Comparator<OSProcess>() {
			@Override
			public int compare(OSProcess p1, OSProcess p2) {
				double diff = (p1.getKernelTime() + p1.getUserTime()) / (double) p1.getUpTime()
						- (p2.getKernelTime() + p2.getUserTime()) / (double) p2.getUpTime();
				if (diff < 0) {
					return 1;
				} else if (diff > 0) {
					return -1;
				} else {
					return 0;
				}
			}
		};
		Collections.sort(procs, cpuDescOrder);
		s.append("%CPU %MEM       VSZ       RSS Name\n");
		for (int i = 0; i < procs.size() && i < 5; i++) {
			OSProcess p = procs.get(i);
			s.append(String.format("%5.1f %4.1f %9s %9s %s%n",
					100d * (p.getKernelTime() + p.getUserTime()) / p.getUpTime(),
					100d * p.getResidentSetSize() / hal.getMemory().getTotal(),
					FormatUtil.formatBytes(p.getVirtualSize()), FormatUtil.formatBytes(p.getResidentSetSize()),
					p.getName()));
		}
		System.out.println(s.toString());
		return s.toString();
	}
	
	public static void main3(String[] args) {
        // Options: ERROR > WARN > INFO > DEBUG > TRACE

//        LOG.info("Initializing System...");
        SystemInfo si = new SystemInfo();
        // software
        // software: operating system
        OperatingSystem os = si.getOperatingSystem();
        System.out.println(os);

//        LOG.info("Initializing Hardware...");
        // hardware
        HardwareAbstractionLayer hal = si.getHardware();

        // hardware: processors
        System.out.println(hal.getProcessor());
        System.out.println(" " + hal.getProcessor().getPhysicalProcessorCount() + " physical CPU(s)");
        System.out.println(" " + hal.getProcessor().getLogicalProcessorCount() + " logical CPU(s)");

        System.out.println("Identifier: " + hal.getProcessor().getIdentifier());
        System.out.println("Serial Num: " + hal.getProcessor().getSystemSerialNumber());

        // hardware: memory
//        LOG.info("Checking Memory...");
        System.out.println("Memory: " + FormatUtil.formatBytes(hal.getMemory().getAvailable()) + "/"
                + FormatUtil.formatBytes(hal.getMemory().getTotal()));
        System.out.println("Swap used: " + FormatUtil.formatBytes(hal.getMemory().getSwapUsed()) + "/"
                + FormatUtil.formatBytes(hal.getMemory().getSwapTotal()));
        // uptime
//        LOG.info("Checking Uptime...");
        System.out.println("Uptime: " + FormatUtil.formatElapsedSecs(hal.getProcessor().getSystemUptime()));

        // CPU
//        LOG.info("Checking CPU...");
        long[] prevTicks = hal.getProcessor().getSystemCpuLoadTicks();
        System.out.println("CPU, IOWait, and IRQ ticks @ 0 sec:" + Arrays.toString(prevTicks) + ", "
                + hal.getProcessor().getSystemIOWaitTicks() + ", "
                + Arrays.toString(hal.getProcessor().getSystemIrqTicks()));
        // Wait a second...
        Util.sleep(1000);
        long[] ticks = hal.getProcessor().getSystemCpuLoadTicks();
        System.out.println("CPU, IOWait, and IRQ ticks @ 1 sec:" + Arrays.toString(ticks) + ", "
                + hal.getProcessor().getSystemIOWaitTicks() + ", "
                + Arrays.toString(hal.getProcessor().getSystemIrqTicks()));
        long user = ticks[0] - prevTicks[0];
        long nice = ticks[1] - prevTicks[1];
        long sys = ticks[2] - prevTicks[2];
        long idle = ticks[3] - prevTicks[3];
        long totalCpu = user + nice + sys + idle;

        System.out.format("User: %.1f%% Nice: %.1f%% System: %.1f%% Idle: %.1f%%%n", 100d * user / totalCpu,
                100d * nice / totalCpu, 100d * sys / totalCpu, 100d * idle / totalCpu);
        System.out.format("CPU load: %.1f%% (counting ticks)%n",
                hal.getProcessor().getSystemCpuLoadBetweenTicks() * 100);
        System.out.format("CPU load: %.1f%% (OS MXBean)%n", hal.getProcessor().getSystemCpuLoad() * 100);
        double[] loadAverage = hal.getProcessor().getSystemLoadAverage(3);
        System.out.println("CPU load averages:" + (loadAverage[0] < 0 ? " N/A" : String.format(" %.2f", loadAverage[0]))
                + (loadAverage[1] < 0 ? " N/A" : String.format(" %.2f", loadAverage[1]))
                + (loadAverage[2] < 0 ? " N/A" : String.format(" %.2f", loadAverage[2])));
        // per core CPU
        StringBuilder procCpu = new StringBuilder("CPU load per processor:");
        double[] load = hal.getProcessor().getProcessorCpuLoadBetweenTicks();
        for (int cpu = 0; cpu < load.length; cpu++) {
            procCpu.append(String.format(" %.1f%%", load[cpu] * 100));
        }
        System.out.println(procCpu.toString());

        // Processes
        System.out.println("Processes: " + hal.getProcessor().getProcessCount() + ", Threads: "
                + hal.getProcessor().getThreadCount());
        List<OSProcess> procs = Arrays.asList(hal.getProcessor().getProcesses());
        // Sort by highest CPU
        Comparator<OSProcess> cpuDescOrder = new Comparator<OSProcess>() {
            @Override
            public int compare(OSProcess p1, OSProcess p2) {
                double diff = (p1.getKernelTime() + p1.getUserTime()) / (double) p1.getUpTime()
                        - (p2.getKernelTime() + p2.getUserTime()) / (double) p2.getUpTime();
                if (diff < 0) {
                    return 1;
                } else if (diff > 0) {
                    return -1;
                } else {
                    return 0;
                }
            }
        };
        Collections.sort(procs, cpuDescOrder);
        System.out.println("   PID  %CPU %MEM       VSZ       RSS Name");
        for (int i = 0; i < procs.size() && i < 5; i++) {
            OSProcess p = procs.get(i);
            System.out.format(" %5d %5.1f %4.1f %9s %9s %s%n", p.getProcessID(),
                    100d * (p.getKernelTime() + p.getUserTime()) / p.getUpTime(),
                    100d * p.getResidentSetSize() / hal.getMemory().getTotal(),
                    FormatUtil.formatBytes(p.getVirtualSize()), FormatUtil.formatBytes(p.getResidentSetSize()),
                    p.getName());
        }

        // hardware: sensors
//        LOG.info("Checking Sensors...");
        System.out.println("Sensors:");
        System.out.format(" CPU Temperature: %.1f°C%n", hal.getSensors().getCpuTemperature());
        System.out.println(" Fan Speeds:" + Arrays.toString(hal.getSensors().getFanSpeeds()));
        System.out.format(" CPU Voltage: %.1fV%n", hal.getSensors().getCpuVoltage());

        // hardware: power
//        LOG.info("Checking Power sources...");
        StringBuilder sb = new StringBuilder("Power: ");
        if (hal.getPowerSources().length == 0) {
            sb.append("Unknown");
        } else {
            double timeRemaining = hal.getPowerSources()[0].getTimeRemaining();
            if (timeRemaining < -1d) {
                sb.append("Charging");
            } else if (timeRemaining < 0d) {
                sb.append("Calculating time remaining");
            } else {
                sb.append(String.format("%d:%02d remaining", (int) (timeRemaining / 3600),
                        (int) (timeRemaining / 60) % 60));
            }
        }
        for (PowerSource pSource : hal.getPowerSources()) {
            sb.append(String.format("%n %s @ %.1f%%", pSource.getName(), pSource.getRemainingCapacity() * 100d));
        }
        System.out.println(sb.toString());

        // hardware: file system
//        LOG.info("Checking File System...");
        System.out.println("File System:");

        FileSystem filesystem = hal.getFileSystem();
        System.out.format(" File Descriptors: %d/%d%n", filesystem.getOpenFileDescriptors(),
                filesystem.getMaxFileDescriptors());

        OSFileStore[] fsArray = hal.getFileStores();
        for (OSFileStore fs : fsArray) {
            long usable = fs.getUsableSpace();
            long total = fs.getTotalSpace();
            System.out.format(" %s (%s) [%s] %s of %s free (%.1f%%) is %s and is mounted at %s%n", fs.getName(),
                    fs.getDescription().isEmpty() ? "file system" : fs.getDescription(), fs.getType(),
                    FormatUtil.formatBytes(usable), FormatUtil.formatBytes(fs.getTotalSpace()), 100d * usable / total,
                    fs.getVolume(), fs.getMount());
        }

        // hardware: disks
//        LOG.info("Checking Disks...");
        System.out.println("Disks:");

        HWDiskStore[] dskArray = hal.getDiskStores();
        for (HWDiskStore dsk : dskArray) {
            boolean readwrite = dsk.getReads() > 0 || dsk.getWrites() > 0;
            System.out.format(" %s: (model: %s - S/N: %s) size: %s, reads: %s, writes: %s %n", dsk.getName(),
                    dsk.getModel(), dsk.getSerial(),
                    dsk.getSize() > 0 ? FormatUtil.formatBytesDecimal(dsk.getSize()) : "?",
                    readwrite ? FormatUtil.formatBytes(dsk.getReads()) : "?",
                    readwrite ? FormatUtil.formatBytes(dsk.getWrites()) : "?");
        }

        // hardware: network interfaces
//        LOG.info("Checking Network interfaces...");
        System.out.println("Network interfaces:");

        NetworkIF[] netArray = hal.getNetworkIFs();
        for (NetworkIF net : netArray) {
            System.out.format(" Name: %s (%s)%n", net.getName(), net.getDisplayName());
            System.out.format("   MAC Address: %s %n", net.getMacaddr());
            System.out.format("   MTU: %s, Speed: %s %n", net.getMTU(), FormatUtil.formatValue(net.getSpeed(), "bps"));
            System.out.format("   IPv4: %s %n", Arrays.toString(net.getIPv4addr()));
            System.out.format("   IPv6: %s %n", Arrays.toString(net.getIPv6addr()));
            boolean hasData = net.getBytesRecv() > 0 || net.getBytesSent() > 0 || net.getPacketsRecv() > 0
                    || net.getPacketsSent() > 0;
            System.out.format("   Traffic: received %s/%s; transmitted %s/%s %n",
                    hasData ? net.getPacketsRecv() + " packets" : "?",
                    hasData ? FormatUtil.formatBytes(net.getBytesRecv()) : "?",
                    hasData ? net.getPacketsSent() + " packets" : "?",
                    hasData ? FormatUtil.formatBytes(net.getBytesSent()) : "?");
        }

        // hardware: displays
//        LOG.info("Checking Displays...");
        System.out.println("Displays:");
        int i = 0;
        for (Display display : hal.getDisplays()) {
            System.out.println(" Display " + i + ":");
            System.out.println(display.toString());
            i++;
        }

        // hardware: USB devices
//        LOG.info("Checking USB Devices...");
        System.out.println("USB Devices:");
        for (UsbDevice usbDevice : hal.getUsbDevices()) {
            System.out.println(usbDevice.toString());
        }

        // LOG.info("Printing JSON:");
        // Compact JSON
        // System.out.println(si.toJSON().toString());

        // Pretty JSON
        // System.out.println(ParseUtil.jsonPrettyPrint(si.toJSON()));
    }
}
