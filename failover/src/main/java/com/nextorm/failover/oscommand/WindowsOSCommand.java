package com.nextorm.failover.oscommand;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class WindowsOSCommand extends AbstractOSCommand {
	@Override
	public String getMyProcessID() {
		String processID = "";
		RuntimeMXBean rt = ManagementFactory.getRuntimeMXBean();
		processID = rt.getName();
		if (processID.indexOf('@') > 0) {
			processID = processID.substring(0, processID.indexOf('@'));
		}
		return processID;
	}

	@Override
	public List<String> getProcessID(String p_identifer) throws Exception {
		List<String> retList = new ArrayList<String>();
		int num = 0;
		String[] arr = null;
		try {
			List<String> tmpList = getProcessList(p_identifer);
			if (tmpList != null && tmpList.size() > 0) {
				for (String line : tmpList) {
					arr = line.split(DELIMETER);
					if (arr != null) {
						num = Integer.parseInt(arr[0]);
						retList.add(String.valueOf(num));
					}
				}
			}
		} catch (Exception e) {
			throw e;
		} finally {

		}
		return retList;
	}

	@Override
	public List<String> getProcessList(String p_identifer) throws Exception {
		List<String> retList = new ArrayList<String>();
		String l_line = "";
		int l_index = -1;
		BufferedReader l_reader = null;
		Process l_proc = null;
		try {
			l_proc = Runtime.getRuntime()
							.exec(PROCESS_LIST_COMMAND);
			l_reader = new BufferedReader(new InputStreamReader(l_proc.getInputStream()));
			while ((l_line = l_reader.readLine()) != null) {
				if (l_line.indexOf(p_identifer) != -1) {
					l_index = l_line.indexOf(p_identifer);
					if (l_index != -1) {
						retList.add(l_line);
					}
				}
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (l_proc != null) {
				if (l_reader != null) {
					try {
						l_reader.close();
					} catch (Exception e) {
					}
				}
				try {
					l_proc.destroy();
				} catch (Exception e) {
				}
			}
		}
		return retList;
	}

	@Override
	public List<String> getFrameworkProcessID(
		String p_frameworkType,
		String p_frameworkGroupName,
		String p_FrameworkmenberName
	) {
		List<String> processList = new ArrayList<String>();
		List<String> retProcList = new ArrayList<String>();
		int num = 0;
		String[] arr = null;
		try {
			processList = getProcessList(p_frameworkType + " ");
			if (processList != null && processList.size() > 0) {
				for (String line : processList) {
					if (line.indexOf(p_frameworkGroupName) > 0 && line.indexOf(p_FrameworkmenberName) > 0) {
						arr = line.split(DELIMETER);
						if (arr != null) {
							num = Integer.parseInt(arr[0]);
							retProcList.add("" + num);
							break;
						}
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return retProcList;
	}

	@Override
	public boolean startProcess(ProcessExecuteInfo p_exeInfo) throws Exception {
		boolean isSuccess = false;
		Process process = null;
		ProcessBuilder pb = null;
		if (p_exeInfo != null) {
			try {
				String path = p_exeInfo.getExecutePath();
				int index = path.lastIndexOf("\\");
				String directory = path.substring(0, index);
				File dir = new File(directory);
				if (!(new File(path)).exists()) {
					throw new BeaverProcessException("Not Find File");
				}
				pb = new ProcessBuilder(path);
				pb.directory(dir);
				Thread.sleep(500);
				process = pb.start();
				Thread.sleep(500);
				isSuccess = process.isAlive();
				if (isSuccess == false) {
					BeaverProcessException ex = new BeaverProcessException(output(process.getErrorStream()));
					throw ex;
				}
			} catch (Exception e) {
				throw e;
			} finally {
				if (process != null) {
					try {
						process.destroy();
					} catch (Exception e) {
					}
				}
			}
		}

		return isSuccess;
	}

	@Override
	public boolean stopProcess(
		String p_PID,
		short p_killType
	) throws Exception {
		Process proc = null;
		BufferedReader reader = null;
		boolean isSuccess = false;
		try {
			String line = "";
			proc = Runtime.getRuntime()
						  .exec(PROCESS_KILL_COMMAND + p_PID);
			Thread.sleep(1000);
			if (proc.isAlive()) {
				throw new BeaverProcessException("Cannot kill the Requested Process : " + p_PID);
			} else {
				isSuccess = true;
			}
			reader = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
			while ((line = reader.readLine()) != null) {
				System.out.println(line);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (proc != null) {
				if (reader != null) {
					try {
						reader.close();
					} catch (Exception e) {
					}
				}
				try {
					proc.destroy();
				} catch (Exception e) {
				}
			}
		}
		return isSuccess;
	}

	@Override
	public List<String> getMyIPAddress() {
		List<String> addressList = new ArrayList<String>();
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress() && inetAddress.isSiteLocalAddress()) {
						addressList.add(inetAddress.getHostAddress()
												   .toString());
					}
				}
			}
		} catch (SocketException ex) {
		}
		return addressList;
	}
}
