package com.nextorm.failover.oscommand;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public abstract class AbstractOSCommand {
	protected final String PS = "ps -ef";
	protected final String JPS = "jps -v";
	protected final String DELIMETER = " ";
	protected final String PROCESS_LIST_COMMAND = "cmd.exe /c jps -v";
	protected final String PROCESS_KILL_COMMAND = "cmd.exe /c taskkill /F /PID ";
	protected final String CMD = "cmd.exe /c ";

	public final short NO_DUMP = 9;
	public final short THREAD_DUMP = 3;
	public final short MEMORY_DUMP = 6;
	protected String osName = "";

	public String getOsName() {
		return osName;
	}

	/**
	 * 현재 운영체제에 맞는 Commander를 리턴합니다.
	 *
	 * @return
	 */
	public static AbstractOSCommand getFrameworkCommander() {
		AbstractOSCommand commander = null;
		String osName = System.getProperty("os.name");

		if (osName == null) {
			throw new RuntimeException("there is no the system property 'os.name'");
		}

		osName = osName.toUpperCase();

		if (osName.startsWith("WINDOW")) {
			commander = new WindowsOSCommand();
		} else {
			commander = new UnixOSCommand();
		}
		commander.osName = osName;

		return commander;
	}

	protected String output(InputStream inputStream) throws IOException {
		boolean isSuccess = false;
		StringBuilder sb = new StringBuilder();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(inputStream));
			String line = null;
			while ((line = br.readLine()) != null) {
				sb.append(line + System.getProperty("line.separator"));
			}
		} catch (Exception e) {
			throw e;
		} finally {
			br.close();
		}
		return sb.toString();
	}

	/**
	 * 자기 자신의 process ID를 리턴 합니다.
	 *
	 * @return
	 */
	public abstract String getMyProcessID();

	/**
	 * Process 실행 라인에 특정 이름이 포함되어 있는 Process의 ID List를 가져 옵니다.
	 * 요청한 이름이 포함되어 있는 process가 1개 이상일 수 있다는 가정입니다.
	 *
	 * @param identifier : Process를 찾기 위한 단어
	 * @return : Process ID List
	 * @throws Exception
	 */
	public abstract List<String> getProcessID(String identifier) throws Exception;

	/**
	 * Process 실행 라인에 특정 이름이 포함되어 있는 Process의 모든 실행 라인 List를 가져 옵니다.
	 * 요청한 이름이 포함되어 있는 process가 1개 이상일 수 있다는 가정입니다.
	 *
	 * @param identifier
	 * @return Process 실행 라인 List
	 * @throws Exception
	 */
	public abstract List<String> getProcessList(String identifier) throws Exception;

	/**
	 * 특정 Framework에 대한 PID 정보를 가져 옵니다.
	 *
	 * @param frameworkType
	 * @param frameworkGroupName
	 * @param FrameworkmenberName
	 * @return
	 */
	public abstract List<String> getFrameworkProcessID(
		String frameworkType,
		String frameworkGroupName,
		String FrameworkmenberName
	);

	/**
	 * 특정 Process를 시작 합니다.
	 *
	 * @param exeInfo
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws Exception
	 */
	public abstract boolean startProcess(ProcessExecuteInfo exeInfo) throws
		IOException,
		InterruptedException,
		Exception;

	/**
	 * 특정 Process를 강제 종료 합니다.
	 *
	 * @param pid      : 종료하기 위한 Process의 PID
	 * @param killType :NO_DUMP = 9,THREAD_DUMP = 3, MEMORY_DUMP = 6 중 선택을 합니다. 단, Windows OS에서는 Kill Type이 적용되지 않습니다.
	 * @return
	 * @throws Exception
	 */
	public abstract boolean stopProcess(
		String pid,
		short killType
	) throws Exception;

	/**
	 * 현재 System의 IP를 가져 옵니다.
	 * Multi Network Card가 적용되었을 수 있으므로, List 형태로 리턴합니다.
	 *
	 * @return
	 */
	public abstract List<String> getMyIPAddress();
}
