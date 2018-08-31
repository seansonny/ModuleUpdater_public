# Module Updater

### Intro
This program is the in-house tool at Fasoo to update the module automatically at the quality assurance team when software development team hand over the patched modules. Updated modules are parts of the Fasoo's commercial program for windows. 

### Minimum JRE
+ java jre >= 1.8.0

### MVC Pattern

src > model
+ Client.java
+ program logic

src > view
+ RootLayoutController.java
+ javaFx

src > controller
+ MainApp.java
+ Run this file

src > utils
+ INIFile.java
    +  ini file setter

+ Log.java
    + (LogFilter, LogFormatter, LogHandler)
    + log file setter

module_updater_packaged
+ mu.exe
+ mu.manifest

### mu.exe

+ Select folder with patched modules
+ Search results for matched files 
    +  Green : files need to be updated (patched file is newer than the installed file). 
    +  Red : files that do not need to be updated (installed files are newer than patched files.) 
    +  Gray : Patched and installed files are the same file.
+ If a file is colored red in the NewFile section, the file is a core module and has multiple installed locations.
	+ 
+ If a file is not colored in the NewFile section, it is a new file and the user must manually select the install location.
+ If the DRM module is running, use QDRM.exe to turn off the module to enable the update button.
+ When the update button is pressed, the user is prompted to enter a message. Then, the status bar (lower bottom of the window) shows the number of files that have succeeded/failed.
+ The program creates a temporary folder called MU_temp (where the mu.exe file is located) and backs up files that have been installed.
+ Failed installations can be found in the Module_Updater.log file. The user must manually switch these files.
+ An update can be reverted using the revert button (back up files will be created in the MU_temp file under the timestamp of the updated date).
+ The user can revert to a previous session by selecting one of the folders, generated with timestamp in MU_temp as a source folder.
+ A log file (Module_Updater.log) and settings file (settings.ini) will be created in the same location as the program file (mu.exe).
+ The Module_Updater.log is a log file that keeps the record of successful/failed attempts.
+ The user will be able to edit the settings.ini file which will then change the target directories when searching for files.

### Format in settings.ini
+ Target directories for system 64bit
    + [target_dirs_32]
    + [target_dirs_64]

+ Target directories for system 32bit
    + [system_32]

+ Directory of patched modules
    + [source_dir]
    + default = 

### Client.java
+ public static boolean system64()
    + Check if the windows are 64bit or 32bit.
+ public void search()
    +  Find the files of the same name and check the last modified date.
    + If the OS is 32bit, use FileMeta class for 32bit
        + source32Meta, system32Meta
    + If the OS is 64bit, use FileMeta class for both 32bit and 64bit
        + source32Meta, target32Meta
        + source64Meta, target64Meta
    + See for the data structure: FileMeta.java
    + Search calls searchForUpdate
    
+ public int update(ArrayList<FileMetaProperty> data)  
    + Generate temporary and backup folder.
    + Turn off the processes that use the same file about to update.
    + Using getUpdatedTargetName and change the file name.
    + Saved the file whose name is just changed. 
    + Copy patched files from source directories to target directories.
    + Record the details of the file change (Sucess, Fail, how many and why failed)
    + Restart the process that had shut down.


+ public boolean addNewFile(FileMeta newFile, String tDir)
	+ When a new file(module) that have not installed in the target folder, user designate the target directory. Copy the new file to the input directory. 
   
+ private int revertMatched()
    + Revert the file changes that just happened.
    
+ public ArrayList<FileMetaProperty> printNewFile()
    + For view, javaFx, convert String to StringProperty.

+ 	private static ArrayList<FileMetaProperty> iterArraylisttoFMP(ArrayList<FileMeta> newFile) {
    + For view, javaFx, convert String to StringProperty.

### MainApp.java
+ public void start(Stage stage) throws Exception 
    + controller file
    
### INIFile.java
+ ini file setting

### Log.java
+ LogFilter.java
+ LogFormatter.java
+ LogHandler.java

This program is developed by interns of 2018 at Fasoo. All the rights belong to Fasoo.
