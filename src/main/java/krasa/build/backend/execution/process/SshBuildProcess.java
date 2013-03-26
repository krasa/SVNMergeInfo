package krasa.build.backend.execution.process;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import krasa.build.backend.domain.Status;
import krasa.build.backend.execution.ProcessLog;
import krasa.build.backend.execution.ProcessStatus;
import krasa.build.backend.execution.ssh.SCPInfo;
import krasa.build.backend.execution.ssh.SSHManager;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.jcraft.jsch.Channel;

public class SshBuildProcess implements Process {
	protected final static Logger log = LoggerFactory.getLogger(SshBuildProcess.class);
	@Value("${ssh.username}")
	String userName;
	@Value("${ssh.password}")
	String password;
	@Value("${ssh.connectionIP}")
	String connectionIP;
	protected SSHManager instance;
	protected ProcessLog stringBufferTail;
	protected List<String> command;
	ProcessStatus processStatus = new ProcessStatus();
	List<ProcessResultListener> processResultListeners = new ArrayList<ProcessResultListener>();

	public SshBuildProcess(ProcessLog stringBufferTail, List<String> command) {
		this.stringBufferTail = stringBufferTail;
		this.command = command;
	}

	public boolean addListener(ProcessResultListener processResultListener) {
		return processResultListeners.add(processResultListener);
	}

	private Properties getProperties() {
		Properties config = new Properties();
		config.put("StrictHostKeyChecking", "no");
		return config;
	}

	@Override
	public void run() {
		processStatus.setStatus(Status.IN_PROGRESS);
		try {
			stringBufferTail.newLine().append("--- PROCESS STARTED ---");
			int i = doWork();

			if (i == 0) {
				stringBufferTail.newLine().append("--- PROCESS FINISHED ---");
				processStatus.setStatus(Status.SUCCESS);
			} else {
				processStatus.setStatus(Status.FAILED);
				stringBufferTail.newLine().append("--- PROCESS FAILED ---");
			}
			// close only after all commands are sent
		} catch (Exception e) {
			processStatus.setStatus(Status.EXCEPTION);
			stringBufferTail.newLine().append("--- PROCESS FAILED ---");
			log.error(e.getMessage(), e);
			processStatus.setException(e);
		} finally {
			onFinally();
		}
	}

	protected void onFinally() {
		instance.close();
		notifyListeners();
	}

	protected int doWork() throws IOException {
		instance = new SSHManager(new SCPInfo(userName, password, connectionIP));
		// call sendCommand for each command and the output
		// (without prompts) is returned
		// stringBuilder.printingThread(System.out).start();
		Channel channel = instance.runCommands(command);
		stringBufferTail.receiveUntilLineEquals(channel.getInputStream(), "logout");

		channel.disconnect();

		int exitStatus = channel.getExitStatus();

		log.debug("exit status: " + exitStatus);
		exitStatus = getExitStatusFromLog(exitStatus, stringBufferTail.toString());
		return exitStatus;
	}

	protected static int getExitStatusFromLog(int exitStatus, String logContent) {
		int start1 = logContent.length() - 100;
		String substring = StringUtils.substring(logContent, start1 > 0 ? start1 : 0);
		if (substring.contains("returned code [")) {
			int start = substring.indexOf("returned code [") + "returned code [".length();
			int end = substring.indexOf("]", start);
			String substring1 = substring.substring(start, end);
			exitStatus = Integer.parseInt(substring1.trim());
			log.debug("exit status from log: " + exitStatus);
		}
		return exitStatus;
	}

	protected void notifyListeners() {
		for (ProcessResultListener processResultListener : processResultListeners) {
			processResultListener.onResult(processStatus);
		}
	}

	@Override
	public void stop() {
		processStatus.setStatus(Status.KILLED);
		onFinally();
	}

	@Override
	public ProcessStatus getStatus() {
		if (instance != null) {
			if (processStatus.getStatus() == null || processStatus.getStatus() == Status.IN_PROGRESS) {
				processStatus.setStatus(instance.isConnected() ? Status.IN_PROGRESS : Status.KILLED);
			}
		}
		return processStatus;
	}
}
