package org.mifosng.platform.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

import org.mifosng.platform.infrastructure.ThreadLocalContextUtil;

public class FileUtils {

	public static final String MIFOSX_BASE_DIR = System
			.getProperty("user.home") + File.separator + ".mifosx";

	public static Random random = new Random();

	/**
	 * Generate a random String
	 * 
	 * @return
	 */
	public static String generateRandomString() {
		String characters = "abcdefghijklmnopqrstuvwxyz123456789";
		int length = generateRandomNumber();
		char[] text = new char[length];
		for (int i = 0; i < length; i++) {
			text[i] = characters.charAt(random.nextInt(characters.length()));
		}
		return new String(text);
	}

	/**
	 * Generate a random number between 5 to 16
	 * 
	 * @return
	 */
	public static int generateRandomNumber() {
		Random randomGenerator = new Random();
		return randomGenerator.nextInt(11) + 5;
	}

	/**
	 * Generate the directory path for storing the new document Type
	 * 
	 * @param entityType
	 * @param entityId
	 * @return
	 */
	public static String generateFileParentDirectory(String entityType,
			Long entityId) {
		return FileUtils.MIFOSX_BASE_DIR
				+ File.separator
				+ ThreadLocalContextUtil.getTenant().getName()
						.replaceAll(" ", "").trim() + File.separator
				+ entityType + File.separator + entityId + File.separator
				+ FileUtils.generateRandomString();
	}

	/**
	 * @param uploadedInputStream
	 * @param uploadedFileLocation
	 * @return
	 * @throws IOException
	 */
	public static String saveToFileSystem(InputStream uploadedInputStream,
			String uploadedFileLocation, String fileName) throws IOException {
		String fileLocation = uploadedFileLocation + File.separator + fileName;
		OutputStream out = new FileOutputStream(new File(fileLocation));
		int read = 0;
		byte[] bytes = new byte[1024];

		while ((read = uploadedInputStream.read(bytes)) != -1) {
			out.write(bytes, 0, read);
		}
		out.flush();
		out.close();
		return fileLocation;
	}
}
