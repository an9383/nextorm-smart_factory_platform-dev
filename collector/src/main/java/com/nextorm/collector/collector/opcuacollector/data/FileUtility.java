package com.nextorm.collector.collector.opcuacollector.data;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class FileUtility {
	public static String readFileContents(String p_path) throws Throwable {
		FileInputStream stream = null;
		String data = "";
		try {
			stream = new FileInputStream(p_path);
			if (stream != null) {
				data = readFile(stream);
			}
		} catch (Throwable ex) {
			throw ex;
		} finally {
			if (stream != null) {
				stream.close();
				stream = null;
			}
		}
		return data;
	}

	public static void writeFileContents(
		String p_path,
		String p_contents,
		boolean p_isBackup
	) throws Throwable {
		writeFileContents(p_path, p_contents.getBytes(Charset.forName("UTF-8")), p_isBackup);
	}

	public static void writeFileContents(
		String p_path,
		byte[] p_contents,
		boolean p_isBackup
	) throws Throwable {
		FileOutputStream stream = null;
		String data = "";
		try {

			backupFile(p_path, p_isBackup);
			stream = new FileOutputStream(p_path);
			stream.write(p_contents, 0, p_contents.length);
			stream.flush();

		} catch (Throwable ex) {
			throw ex;
		} finally {
			if (stream != null) {
				stream.close();
				stream = null;
			}
		}
	}

	public static List<String[]> readCSVFile(String path) {
		return FileUtility.readCSVFile(path, ",");
	}

	public static List<String[]> readCSVFile(
		String path,
		String spliter
	) {
		List<String[]> retList = new ArrayList<>();
		String line = "";
		try (BufferedReader br = new BufferedReader(new FileReader(path))) {
			while ((line = br.readLine()) != null) {
				retList.add(line.split(spliter));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return retList;
	}

	private static void backupFile(
		String p_path,
		boolean p_isBackup
	) throws Throwable {
		File file = null;
		FileInputStream fis = null;
		FileOutputStream fos = null;
		try {
			file = new File(p_path);
			if (file.exists()) {
				if (p_isBackup) {
					fis = new FileInputStream(p_path);
					fos = new FileOutputStream(p_path + "_backup");
					int data = 0;
					while ((data = fis.read()) != -1) {
						fos.write(data);
					}
					fis.close();
					fis = null;
					fos.close();
					fos = null;
				}
				file.delete();
			}
		} catch (Exception ex) {
			throw ex;
		} finally {
			if (file != null) {
				file = null;
			}

			if (fis != null) {
				fis.close();
				fis = null;
			}

			if (fos != null) {
				fos.close();
				fos = null;
			}
		}

	}

	public static String removeCRString(String p_fileContents) {
		return p_fileContents.replaceAll("\r\n", "");
	}

	public static String removeTABString(String p_fileContents) {
		return p_fileContents.replaceAll("\t", "");
	}

	private static String readFile(InputStream p_fis) throws Throwable {
		byte[] data = null;
		String retVal = "";
		try {
			try {
				data = new byte[p_fis.available()];
				while (p_fis.read(data) != -1) {
					;
				}
				retVal = new String(data);
			} catch (IOException e) {
				throw e;
			}
		} catch (Throwable e) {
		} finally {
			if (p_fis != null) {
				try {
					p_fis.close();
				} catch (IOException e) {
					throw e;
				}
			}
			p_fis = null;
		}
		return retVal;
	}

	public static byte[] loadFileData(String p_path) throws Throwable {
		byte[] retval = null;
		String fileContents = FileUtility.readFileContents(p_path);
		if (fileContents != null && fileContents != "") {
			retval = fileContents.getBytes(Charset.forName("UTF-8"));
		}
		return retval;
	}

	public static byte[] loadJar(String p_path) throws IOException {
		File file = new File(p_path);
		byte[] data = Files.readAllBytes(file.toPath());
		return data;
	}

	public static byte[] loadImage(String p_path) throws IOException {
		File file = new File(p_path);
		byte[] data = Files.readAllBytes(file.toPath());
		return data;
	}

	/**
	 * 폴더에서 마지막 수정 파일 얻기
	 */
	public static File getLastFileModified(String dir) {
		File fl = new File(dir);
		File[] files = fl.listFiles(new FileFilter() {
			public boolean accept(File file) {
				return file.isFile();
			}
		});
		long lastMod = Long.MIN_VALUE;
		File choice = null;
		for (File file : files) {
			if (file.lastModified() > lastMod) {
				choice = file;
				lastMod = file.lastModified();
			}
		}
		return choice;
	}
}
