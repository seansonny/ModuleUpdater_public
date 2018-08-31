package model;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileMeta {
	private String fPath;
	private String fName;
	private long fDate;
	private boolean is64;
	private boolean checkBox;
	private int modifedDate;
	private boolean isCore; // module that is used in more than two product.

	// private CheckBox checkBox2;
	public FileMeta(File file, boolean is64) {
		this.fPath = file.getAbsolutePath();
		this.fName = file.getName();
		this.fDate = file.lastModified();
		this.is64 = is64;
		this.checkBox = false;
		this.isCore = false;
	}

	public void setfName(String f_name) {
		String setNewName = regexParentheses(f_name);
		if (setNewName.endsWith("_tmp")) {
			setNewName = setNewName.replaceAll("_tmp", "");
		}
		if (setNewName.contains("_nt")) {
			setNewName = setNewName.replaceAll("_nt", "");
		}
		if (setNewName.contains("_vista")) {
			setNewName = setNewName.replaceAll("_vista", "");
		}
		if (setNewName.contains("_64")) {
			setNewName = setNewName.replaceAll("_64", "");
		}
		fName = setNewName;
	}

	public void setCoreModule() {
		this.isCore = true;
	}
	
	public void setModifiedDate(int i) {
		this.setModifedDate(i);
	}

	public void setis64(boolean is64) {
		this.is64 = is64;
	}

	public boolean getCoreModule() {
		return this.isCore;
	}
	
	public boolean getCheckBox() {
		return this.checkBox;
	}

	public String getfPath() {
		return this.fPath;
	}

	public String getfName() {
		return this.fName;
	}

	public long getfDate() {
		return this.fDate;
	}

	public String toString() {
		return this.fName;
	}
	
	public boolean getIs64() {
		return is64;
	}

	public String regexParentheses(String s) {
		String temp = s;
		final String regex = "\\((\\d+)\\)";
		Pattern patt = Pattern.compile(regex);
		Matcher matt = patt.matcher(s);

		if (matt.find()) {
			temp = matt.replaceFirst("");
		}
		return temp;
	}

	public int getModifedDate() {
		return modifedDate;
	}

	public void setModifedDate(int modifedDate) {
		this.modifedDate = modifedDate;
	}
}