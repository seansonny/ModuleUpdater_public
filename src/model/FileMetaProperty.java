package model;

import java.io.File;
import java.text.SimpleDateFormat;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.CheckBox;

public class FileMetaProperty  {
    private StringProperty fileName;
    private StringProperty beforeDate;
    private StringProperty afterDate;
    private StringProperty fileLocation;
    private FileMeta source;
    private FileMeta target;
    private boolean is64;
    private SimpleDateFormat sdf ;
    private CheckBox checkBox;
    public FileMetaProperty(FileMeta source, FileMeta target){
    	this.sdf = new SimpleDateFormat("yyyy-MM-dd a hh:mm");
    	this.source = source;
    	this.target = target;
    	this.is64 = source.getIs64();
    	this.fileName = new SimpleStringProperty(source.getfName());
    	this.beforeDate = new SimpleStringProperty(sdf.format(source.getfDate()));
    	this.afterDate = new SimpleStringProperty(sdf.format(target.getfDate()));
    	File f1 = new File(target.getfPath());
    	this.fileLocation = new SimpleStringProperty(f1.getParent());
    	this.checkBox = new CheckBox();
    }
    
    public FileMetaProperty(FileMeta source){
    	this.source = source;
    	this.target = null;
    	this.is64 = source.getIs64();
    	this.fileName = new SimpleStringProperty(source.getfName());
    	this.beforeDate = null;
    	this.afterDate = null;
    	this.fileLocation = null;
    	this.checkBox = new CheckBox();
    }

	public StringProperty getFileName() {
		String string = this.fileName.get();
		if(this.is64) { 
			string += " (64 bit)";
		} else {
			string += " (32 bit)";
		}
		return new SimpleStringProperty(string);
	}

	public StringProperty getBeforeDate() {
		return beforeDate;
	}

	public void setBeforeDate(StringProperty beforeDate) {
		this.beforeDate = beforeDate;
	}

	public StringProperty getAfterDate() {
		return afterDate;
	}

	public void setAfterDate(StringProperty afterDate) {
		this.afterDate = afterDate;
	}

	public StringProperty getFileLocation() {
		return fileLocation;
	}

	public void setFileLocation(StringProperty fileLocation) {
		this.fileLocation = fileLocation;
	}

	public FileMeta getSource() {
		return this.source;
	}
	
	public FileMeta getTarget() {
		return this.target;
	}

	public void setSource(FileMeta source) {
		this.source = source;
	}

	public CheckBox getCheckBox() {
		return checkBox;
	}
	
	public String getUpdatedTargetName(FileMeta fm) {
		String updatedName= ""; //don't use new String()
		String fileName = fm.getfName();
		if(fm.getIs64()) {
			int indexDot = fileName.indexOf(".");
			updatedName = fileName.substring(0, indexDot) + "_64" + fileName.substring(indexDot);
		}
		else {
			updatedName = fileName;
		}	
		
		return updatedName + "_tmp";
	}

}