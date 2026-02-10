package com.nextorm.failover.oscommand;

import lombok.extern.slf4j.Slf4j;

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

@Slf4j
public class UnixOSCommand extends AbstractOSCommand {
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
	public List<String> getProcessID(String identifer) throws Exception {
		List<String> retList = new ArrayList<String>();
		int num = 0;
		String[] arr = null;
		try {
			List<String> tmpList = getProcessList(identifer);
			if (tmpList != null && !tmpList.isEmpty()) {
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
		}
		return retList;
	}

	@Override
	public List<String> getFrameworkProcessID(
		String frameworkType,
		String frameworkGroupName,
		String FrameworkmenberName
	) {
		List<String> processList = new ArrayList<String>();
		List<String> retProcList = new ArrayList<String>();
		int num = 0;
		String[] arr = null;
		try {
			processList = getProcessList(frameworkType + " ");
			if (processList != null && processList.size() > 0) {
				for (String line : processList) {
					if (line.indexOf(frameworkGroupName) > 0 && line.indexOf(FrameworkmenberName) > 0) {
						arr = line.split(DELIMETER);
						if (arr != null) {
							num = Integer.parseInt(arr[0]);
							retProcList.add("" + num);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return retProcList;
	}

	@Override
	public List<String> getProcessList(String identifer) throws Exception {
		List<String> retList = new ArrayList<String>();
		String l_line = "";
		int l_index = -1;
		BufferedReader l_reader = null;
		Process l_proc = null;

		try {
			l_proc = Runtime.getRuntime()
							.exec(JPS);
			int errcode = l_proc.waitFor();
			if (errcode > 0) {
				throw new BeaverProcessException(output(l_proc.getErrorStream()));
			}
			l_reader = new BufferedReader(new InputStreamReader(l_proc.getInputStream()));
			while ((l_line = l_reader.readLine()) != null) {
				log.info(l_line);
				if (l_line.indexOf(identifer) != -1) {
					l_index = l_line.indexOf(identifer);
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
	public boolean startProcess(ProcessExecuteInfo exeInfo) throws Exception {
		boolean isSuccess = false;
		Process process = null;
		ProcessBuilder pb = null;
		if (exeInfo != null) {
			try {
				String path = exeInfo.getExecutePath();
				int index = path.lastIndexOf("/");
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
				if (!isSuccess) {
					throw new BeaverProcessException(output(process.getErrorStream()));
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
		String pid,
		short killtype
	) {
		Process l_proc = null;
		BufferedReader l_reader = null;
		boolean isSuccess = false;
		try {
			l_proc = Runtime.getRuntime()
							.exec("kill -" + killtype + " " + pid);
			l_reader = new BufferedReader(new InputStreamReader(l_proc.getInputStream()));
			String l_line = "";
			while ((l_line = l_reader.readLine()) != null) {
				log.info(l_line);
			}
			isSuccess = true;
		} catch (Exception e) {
			e.printStackTrace();
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
