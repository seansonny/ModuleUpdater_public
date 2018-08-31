package utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.ini4j.InvalidFileFormatException;
import org.ini4j.Wini;
import model.*;

public class INIFile {

	private ArrayList<String> target64Dir;
	private ArrayList<String> target32Dir;
	private ArrayList<String> system32Dir;
	private String sourceDir = null;
	private File settings;
	final String settingFile = System.getProperty("user.dir") + "\\settings.ini";
	// final File settings = new File(settingFile);

	public String getSettingFile() {
		return settingFile;
	}

	public INIFile() {
		this.target64Dir = new ArrayList<String>();
		this.target32Dir = new ArrayList<String>();
		this.system32Dir = new ArrayList<String>();
		settings = new File(settingFile);
		if (!settings.exists()) {
			this.createIniFile(settingFile);
		}

		try {
			Wini ini = new Wini(settings);
			readINI(ini);
		} catch (InvalidFileFormatException e) {
			Log.logInfo(e.getMessage());
		} catch (IOException e) {
			Log.logInfo(e.getMessage());
		}
	}

	public void saveSourceDirectory(String newSourceDir) throws IOException {
		Wini writer = new Wini(settings);
		writer.load();
		writer.add("source_dir", "default", newSourceDir);
		writer.store();
	}

	public boolean dirExist(String dir, Boolean is64) {
		boolean existingDir = false;
		if (!Client.system64()) { // if system architecture is 32bit
			for (int i = 0; i < system32Dir.size(); i++) {
				if (system32Dir.get(i).equals(dir)) {
					existingDir = true;
				}
			}
		}else {
			if (is64) {
				for (int i = 0; i < target64Dir.size(); i++) {
					if (target64Dir.get(i).equals(dir)) {
						existingDir = true;
					}
				}
			} else {
				for (int i = 0; i < target32Dir.size(); i++) {
					if (target32Dir.get(i).equals(dir)) {
						existingDir = true;
					}
				}
			}
		}
		return existingDir;
	}

	public void createIniFile(String settingsPath) {
		File createIni = new File(settingsPath);

		try {
			boolean success = createIni.createNewFile();

			Wini writer = new Wini(createIni);
			writer.add("target_dirs_32", "dir1", "C:\\\\Program Files (x86)\\\\Fasoo DRM");
			writer.add("target_dirs_32", "dir2", "C:\\\\Program Files (x86)\\\\Fasoo DRM\\\\Batch Manager");
			writer.add("target_dirs_32", "dir3", "C:\\\\Program Files (x86)\\\\Fasoo Secure Exchange FED5");
			writer.add("target_dirs_32", "dir4", "C:\\\\Program Files (x86)\\\\Fasoo.com\\\\Fasoo ePrint\\\\bin");
			writer.add("target_dirs_32", "dir5", "C:\\\\Program Files (x86)\\\\Fasoo.com\\\\Fasoo Secure Node\\\\bin");
			writer.add("target_dirs_32", "dir6", "C:\\\\Program Files (x86)\\\\Fasoo.com\\\\PII Manager\\\\bin");
			writer.add("target_dirs_32", "dir7", "C:\\\\Windows\\\\SysWOW64");
			writer.add("target_dirs_32", "dir8", "C:\\\\Windows\\\\SysWOW64\\\\drivers");
			writer.add("target_dirs_32", "dir9", "C:\\\\Program Files (x86)\\\\Wrapsody");

			writer.add("target_dirs_64", "dir1", "C:\\\\Program Files\\\\Fasoo DRM");
			writer.add("target_dirs_64", "dir2", "C:\\\\Program Files\\\\Fasoo Secure Exchange FED5");
			writer.add("target_dirs_64", "dir3", "C:\\\\Program Files\\\\Fasoo.com\\\\Fasoo ePrint\\\\bin");
			writer.add("target_dirs_64", "dir4", "C:\\\\Program Files\\\\Fasoo.com\\\\PII Manager\\\\bin");
			writer.add("target_dirs_64", "dir5", "C:\\\\Windows\\\\System32");
			writer.add("target_dirs_64", "dir6", "C:\\\\Windows\\\\System32\\\\drivers");
			writer.add("target_dirs_64", "dir7", "C:\\\\Program Files\\\\Wrapsody");

			writer.add("system_32", "dir1", "C:\\\\Fasoo DRM");
			writer.add("system_32", "dir2", "C:\\\\Program Files\\\\Fasoo DRM");
			writer.add("system_32", "dir3", "C:\\\\Program Files\\\\Fasoo Secure Exchange FED5");
			writer.add("system_32", "dir4", "C:\\\\Program Files\\\\Fasoo.com\\\\Fasoo ePrint\\\\bin");
			writer.add("system_32", "dir5", "C:\\\\Program Files\\\\Fasoo.com\\\\Fasoo Secure Node\\\\bin");
			writer.add("system_32", "dir6", "C:\\\\Program Files\\\\Fasoo.com\\\\PII Manager\\\\bin");
			writer.add("system_32", "dir7", "C:\\\\Windows\\\\System32");
			writer.add("system_32", "dir8", "C:\\\\Windows\\\\System32\\\\drivers");
			writer.add("system_32", "dir9", "C:\\\\Program Files\\\\Wrapsody");

			writer.add("source_dir", "default", null);
			writer.store();

		} catch (IOException e) {
			Log.logInfo(e.getMessage());
			e.printStackTrace();
		}

		settings = createIni;
		// return createIni;
	}

	// newFile directory �꽕�젙�떆 ini target dir �뿉 異붽�
	public void addTargetDir(HashMap<FileMeta, String> newTargetDirs) throws IOException {
		Map<FileMeta, String> dirMap = new HashMap<FileMeta, String>();
		dirMap = newTargetDirs;
		for (Map.Entry<FileMeta, String> entry : dirMap.entrySet()) {
			FileMeta srcMeta = entry.getKey();
			String addedDir = entry.getValue();
			Wini writer = new Wini(settings);
			String optionName = null;

			if(dirExist(addedDir, srcMeta.getIs64()))
				continue;

			if (!Client.system64()) {
				optionName = "dir" + String.valueOf(system32Dir.size() + 1);
				writer.add("system_32", optionName, addedDir);
				writer.store();
				system32Dir.add(addedDir);
			} else if (srcMeta.getIs64()) {
				optionName = "dir" + String.valueOf(target64Dir.size() + 1);
				writer.add("target_dirs_64", optionName, addedDir);
				writer.store();
				target64Dir.add(addedDir);
			} else {
				optionName = "dir" + String.valueOf(target32Dir.size() + 1);
				writer.add("target_dirs_32", optionName, addedDir);
				writer.store();
				target32Dir.add(addedDir);
			}
		}
	}

	public void readINI(Wini ini) throws IOException {
		if (!ini.isEmpty()) {
			for (String key : ini.get("target_dirs_64").keySet()) {
				target64Dir.add(ini.get("target_dirs_64").fetch(key));
			}

			for (String key : ini.get("target_dirs_32").keySet()) {
				target32Dir.add(ini.get("target_dirs_32").fetch(key));
			}

			for (String key : ini.get("system_32").keySet()) {
				system32Dir.add(ini.get("system_32").fetch(key));
			}

			this.sourceDir = ini.get("source_dir", "default");
		} else {
			Log.logInfo("Ini file is empty");
		}
	}

	public String getSourceDir() {
		return this.sourceDir;
	}

	public ArrayList<String> getTarget64() {
		return this.target64Dir;
	}

	public ArrayList<String> getTarget32() {
		return this.target32Dir;
	}

	public ArrayList<String> getSystem32() {
		return this.system32Dir;
	}
}