package model;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import utils.*;

public class Client {
    INIFile ini;
    private ArrayList<String> target_dirs_64;
    private ArrayList<String> target_dirs_32;
    private ArrayList<String> system_32;
    private String source_dir;
    private ArrayList<FileMeta> source64Meta;
    private ArrayList<FileMeta> source32Meta;
    private ArrayList<FileMeta> target64Meta;
    private ArrayList<FileMeta> target32Meta;
    private ArrayList<FileMeta> system32Meta;
    private HashMap<FileMeta, FileMeta> searchResult64;
    private HashMap<FileMeta, FileMeta> searchResult32;
    private ArrayList<HashMap<FileMeta, FileMeta>> searchResult;
    private ArrayList<FileMeta> newFiles64;
    private ArrayList<FileMeta> newFiles32;
    private ArrayList<String> newFileCopiedDir;
    private HashMap<Path, Path> changedBackup;
    private boolean found32;
    private boolean found64;
    
    public Client() {
        this.ini = new INIFile();
        this.target_dirs_32 = ini.getTarget32();
        this.target_dirs_64 = ini.getTarget64();
        this.system_32 = ini.getSystem32();
        this.source64Meta = new ArrayList<FileMeta>();
        this.source32Meta = new ArrayList<FileMeta>();
        this.target64Meta = new ArrayList<FileMeta>();
        this.target32Meta = new ArrayList<FileMeta>();
        this.system32Meta = new ArrayList<FileMeta>();
        this.searchResult32 = new HashMap<FileMeta, FileMeta>();
        this.searchResult64 = new HashMap<FileMeta, FileMeta>();
        this.searchResult = new ArrayList<HashMap<FileMeta, FileMeta>>();
        this.newFiles32 = new ArrayList<FileMeta>();
        this.newFiles64 = new ArrayList<FileMeta>();
        this.newFileCopiedDir = new ArrayList<String>();
        this.changedBackup = new HashMap<Path, Path>();
        setTarget64Meta();
        setTarget32Meta();
        setSystem32Meta();
        found32 = false;
        found64 = false;
    }
    
    public INIFile getIni() {
        return ini;
    }
    
    public String getSource_dir() {
        return source_dir;
    }
    
    public void readSourceDirectoryFromIni() {
        if (ini.getSourceDir().length() != 0) {
            this.source_dir = ini.getSourceDir();
            this.setSourceMeta();
        }
    }
    
    public void setSourceDirectory(String source_dir) {
        try {
            ini.saveSourceDirectory(source_dir);
        } catch (IOException e) {
            System.out.println("Problem setting source_dir to ini file");
        }
        this.source_dir = source_dir;
        this.setSourceMeta();
    }
    
    public void setSourceMeta() {
        source64Meta.clear();
        source32Meta.clear();
        system_32.clear();
        File folder = new File(source_dir);
        File[] listOfFiles = folder.listFiles();
        for (File file : listOfFiles) {
            if (file.isFile()) {
                if (is64(file.getName())) {
                    // System.out.println(file);
                    // change name -- delete _64 at the end of file name
                    FileMeta source64file = new FileMeta(file, true);
                    source64file.setfName(file.getName());
                    source64Meta.add(source64file);
                } else {
                    FileMeta source32file = new FileMeta(file, false);
                    source32file.setfName(file.getName());
                    source32Meta.add(source32file);
                }
            }
        }
    }
    
    public boolean is64(String fName) {
        for (int i = 0; i < fName.length(); i++) {
            if (fName.charAt(i) == '_' && fName.length() - i >= 3)
                if (fName.charAt(i + 1) == '6' && fName.length() - i >= 2)
                    if (fName.charAt(i + 2) == '4')
                        return true;
        }
        return false;
    }
    
    public static boolean system64() {
        String arch = System.getenv("PROCESSOR_ARCHITECTURE");
        String wow64Arch = System.getenv("PROCESSOR_ARCHITEW6432");
        return arch != null && arch.endsWith("64") || wow64Arch != null && wow64Arch.endsWith("64") ? true : false;
    }
    
    public void search() {
        ArrayList<HashMap<FileMeta, FileMeta>> result = new ArrayList<>();
        
        if (!Client.system64()) {
            this.searchResult32 = this.searchForUpdate(this.source32Meta, this.system32Meta);
        } else {
            this.searchResult32 = this.searchForUpdate(this.source32Meta, this.target32Meta);
            this.searchResult64 = this.searchForUpdate(this.source64Meta, this.target64Meta);
        }
        
        if (searchResult32 != null) {
            result.add(searchResult32);
            found32 = true;
        } else {
            found32 = false;
        }
        
        if (searchResult64 != null) {
            result.add(searchResult64);
            found64 = true;
        } else {
            found64 = false;
        }
        
        this.searchResult = result;
    }
    
    private ArrayList<FileMeta> TargetMeta(boolean isSystem64, boolean isTarget64){
        ArrayList<String> target_dirs = null;
        ArrayList<FileMeta> ret = new ArrayList<FileMeta>();
        if (isSystem64 && isTarget64){
            target_dirs = target_dirs_64;
        } else if (isSystem64 && !isTarget64){
            target_dirs = target_dirs_32;
        } else {
            target_dirs = system_32;
        }
        for (int i = 0; i < target_dirs.size(); i++) {
            File file = new File(target_dirs.get(i));
            ArrayList<FileMeta> fm = fromDirtoMeta(file.getAbsolutePath(), true);
            if (fm != null)
                ret.addAll(fm);
        }
        return ret;
    }
    
    private void setTarget64Meta() {
        this.target64Meta = TargetMeta(true, true);
    }
    
    private void setTarget32Meta() {
        this.target32Meta = TargetMeta(true, false);
    }
    
    private void setSystem32Meta() {
        this.system32Meta = TargetMeta(false, false);
    }
    
    private ArrayList<FileMeta> fromDirtoMeta(String dir, boolean bit64) {
        File folder = new File(dir);
        if (!folder.exists()) {
            return null;
        }
        File[] listOfFiles = folder.listFiles();
        ArrayList<FileMeta> listOfMeta = new ArrayList<>();
        for (File file : listOfFiles) {
            if (file.isFile()) {
                if (bit64 == true) {
                    listOfMeta.add(new FileMeta(file, true));
                } else {
                    listOfMeta.add(new FileMeta(file, false));
                }
            }
        }
        return listOfMeta;
    }
    
    
    private HashMap<FileMeta, FileMeta> searchForUpdate(ArrayList<FileMeta> sourceFiles,
                                                        ArrayList<FileMeta> targetFiles) {
        if (sourceFiles.isEmpty()) {// if sourceFiles is not empty
            return null;
        }
        HashMap<FileMeta, FileMeta> source_target_dirs = new HashMap<FileMeta, FileMeta>();
        // Search for matched file for each source file to the multiple targets
        for (FileMeta sourceMeta : sourceFiles) {
            boolean MatchedOnetoOne = false;
            for (FileMeta targetMeta : targetFiles) {
                if (sourceMeta.getfName().equalsIgnoreCase(targetMeta.getfName())) {
                    MatchedOnetoOne = true;
                    
                    boolean ModDateP30 = sourceMeta.getfDate() < targetMeta.getfDate() + 30000;
                    boolean ModDateM30 = sourceMeta.getfDate() > targetMeta.getfDate() - 30000;
                    if (ModDateP30 && ModDateM30) {
                        sourceMeta.setModifiedDate(0);
                    } else if (!ModDateP30) {
                        sourceMeta.setModifiedDate(1);
                    } else {
                        sourceMeta.setModifiedDate(-1);
                    }
                    
                    FileMeta value = source_target_dirs.get(sourceMeta);
                    if (value == null && !newFiles64.contains(sourceMeta) && !newFiles32.contains(sourceMeta)) {
                        source_target_dirs.put(sourceMeta, targetMeta);
                    } else if(newFiles64.contains(sourceMeta) || newFiles32.contains(sourceMeta)) {
                        sourceMeta.setCoreModule();
                        continue;
                    } else {
                        source_target_dirs.remove(sourceMeta);
                        sourceMeta.setCoreModule();
                        MatchedOnetoOne = false;
                    }
                }
            }
            
            if (!MatchedOnetoOne) { // not matchedOnetoOne means new files or core modules.
                if (sourceMeta.getIs64()) {
                    // 64----delete _64 at the end of file name
                    newFiles64.add(sourceMeta);
                } else {
                    newFiles32.add(sourceMeta);
                }
            }
        }
        
        return source_target_dirs;
    }
    
    public boolean isDRMRunning() throws IOException, InterruptedException {
        String processName = "f_LPS.exe";
        ProcessBuilder processBuilder = new ProcessBuilder("tasklist.exe");
        Process process = processBuilder.start();
        String tasksList = decorder(process.getInputStream());
        
        return tasksList.contains(processName);
    }
    
    private String decorder(InputStream inputStream) {
        Scanner scanner = new Scanner(inputStream, "UTF-8").useDelimiter("\\A");
        String string = scanner.hasNext() ? scanner.next() : "";
        scanner.close();
        return string;
    }
    
    public int update(ArrayList<FileMetaProperty> data) {// Client.searchResult
        int updateSuccessCount = 0;
        ArrayList<FileMetaProperty> searched = new ArrayList<FileMetaProperty>(data); // cast an ObservableList to an
        // ArrayList
        FileMeta srcMeta, targetMeta;
        if (searched.size() == 0) {
            System.out.println(data.size());
            return updateSuccessCount;
        }
        
        // create MU_temp Folder or check if it is there
        String tmp_dir = System.getProperty("user.dir") + "\\MU_temp\\";
        File tempFolder = new File(tmp_dir);
        
        // no need to make a file if MU_temp folder exists.
        if (!tempFolder.exists()) {
            new File(tmp_dir).mkdir();
        }
        
        // create a new folder with time stamp inside the tempFolder
        DateFormat df = new SimpleDateFormat("yyyy MM dd _HH mm ss");
        String dateTime = df.format(new Date());
        String instanceDate = tmp_dir + dateTime + "\\";
        File timeStampFolder = new File(instanceDate);
        boolean timeStampSuccess = timeStampFolder.mkdir();
        // copy files
        
        Runtime runtime = Runtime.getRuntime();
        try {
            runtime.exec("taskkill /F /IM explorer.exe /T").waitFor();
            runtime.exec("taskkill /F /IM fdso.exe /T").waitFor();
            runtime.exec("taskkill /F /IM f_batmgr.exe /T").waitFor();
            runtime.exec("taskkill /F /f_logsvc.exe /T").waitFor();
        }catch(Exception e){
            Log.logInfo("process is not killed");
        }
        
        for (FileMetaProperty metaPair : searched) {
            srcMeta = metaPair.getSource();
            targetMeta = metaPair.getTarget();
            // tmp_dir -> target file folder
            
            File src = new File(srcMeta.getfPath());
            File target = new File(targetMeta.getfPath());
            
            String fileNameUpdated = metaPair.getUpdatedTargetName(targetMeta); //if _64 file ? append "_64" and append "_tmp" for all.
            Path tempFolderDir = Paths.get(instanceDate, fileNameUpdated);
            boolean copySuccess = false;
            
            try {
                Files.copy(target.toPath(), tempFolderDir, StandardCopyOption.REPLACE_EXISTING);
                
                Files.copy(src.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
                copySuccess = true;
                
            } catch (Exception e) {
                Log.logInfo("업데이트 실패: " + srcMeta.getfName() + " ++ " + e.getLocalizedMessage());
                copySuccess = false;
            }
            
            if (copySuccess) {
                updateSuccessCount++;
                Log.logInfo("업데이트 성공! 파일명 : " + srcMeta.getfName() + ":  " + srcMeta.getfPath() + " 에서  "
                            + targetMeta.getfPath() + "로 파일을 복사했습니다.");
                this.changedBackup.put(tempFolderDir, target.toPath());
            } else  {// delete backup file in MUtemp that is not updated.
                Path notUpdatedPath = target.toPath().resolve(tempFolderDir.toString());
                File notUpdated = new File(notUpdatedPath.toString());
                notUpdated.delete();
            }
        }
        if(timeStampFolder.length() < 1) {
            timeStampFolder.delete();
        }
        try {
            runtime.exec("explorer.exe");
        }catch(Exception e){
            Log.logInfo("process is not killed");
        }
        
        return updateSuccessCount;
    }
    
    // @ function to add a new file to a target directory(tDir) that has to be given
    // by the user through UI
    // As an output, ArrayList<Path> stores it's directory that the user decided
    // so that the program can track/ revert if needed.
    
    public boolean addNewFile(FileMeta newFile, String tDir) {
        boolean updateSuccessCount = false;
        // disgards null values for target directory
        if (tDir != null) {
            File sourceFile = new File(newFile.getfPath());
            String newFilesDir = tDir + "\\" + newFile.getfName();
            updateSuccessCount = copyNewFile(sourceFile, newFilesDir);
            this.newFileCopiedDir.add(newFilesDir);
        }
        
        return updateSuccessCount;
    }
    
    // copy the source file to the directory that the user gave.
    // only for the new files -> only called by addNewFile function
    private boolean copyNewFile(File source, String destPath) {
        try {
            Files.copy(source.toPath(), Paths.get(destPath), StandardCopyOption.REPLACE_EXISTING);
            Log.logInfo(source.getName() + " has been moved from " + source.getAbsolutePath() + " to " + destPath);
            return true;
        } catch (IOException e) {
            Log.logInfo(e.getMessage());
            return false;
        }
    }
    
    public int revert() {
        return revertMatched() + revertNewFile();
    }
    
    private int revertMatched() {
        int revertCount = 0;
        Iterator iter = changedBackup.entrySet().iterator();
        int i = 0;
        while (iter.hasNext()) {
            Map.Entry pair = (Map.Entry) iter.next();
            Path tmp_src = (Path) pair.getKey();
            Path target = (Path) pair.getValue();
            try {
                Files.copy(tmp_src, target, StandardCopyOption.REPLACE_EXISTING);
                Log.logInfo("복원 파일명 : + " + target.toString());
                i++;
                revertCount++;
            } catch (IOException e) {
                e.printStackTrace();
            }
            iter.remove();
        }
        changedBackup.clear();
        return revertCount;
    }
    
    // Function to revertNewFile
    // input: ArrayList <String(File Path)>
    // output: boolean
    // delete the new files in the given path
    private int revertNewFile() {
        int revertSuccess = 0;
        if (this.newFileCopiedDir.size() > 0) {
            for (String pathName : this.newFileCopiedDir) {
                File addedFile = new File(pathName);
                Log.logInfo("복원 파일명 : + " + addedFile.getName());
                addedFile.delete();
                revertSuccess++;
            }
        }
        this.newFileCopiedDir.clear();
        return revertSuccess;
    }
    
    public ArrayList<FileMetaProperty> printSearchResult() {
        ArrayList<FileMetaProperty> ret = new ArrayList<>();
        
        if (found32) {
            HashMap<FileMeta, FileMeta> result32 = this.searchResult.get(0);
            ret.addAll(iterHashFiletoFMP(result32, false));
        }
        
        if (found64) {
            HashMap<FileMeta, FileMeta> result64 = this.searchResult.get(1);
            ret.addAll(iterHashFiletoFMP(result64, true));
        }
        return ret;
    }
    
    private static ArrayList<FileMetaProperty> iterHashFiletoFMP(HashMap<FileMeta, FileMeta> result, boolean is64) {
        Iterator iter = result.entrySet().iterator();
        ArrayList<FileMetaProperty> ret = new ArrayList<>();
        while (iter.hasNext()) {
            Map.Entry pair = (Map.Entry) iter.next();
            FileMeta src = (FileMeta) pair.getKey();
            src.setis64(is64);
            FileMeta target = (FileMeta) pair.getValue();
            target.setis64(is64);
            FileMetaProperty fmp = new FileMetaProperty(src, target);
            if (src.getModifedDate() == 1) {
                fmp.getCheckBox().setSelected(true);
            }
            ret.add(fmp);
            iter.remove();
        }
        return ret;
    }
    
    public ArrayList<FileMetaProperty> printNewFile() {
        ArrayList<FileMetaProperty> result = new ArrayList<>();
        result.addAll(iterArraylisttoFMP(this.newFiles64));
        result.addAll(iterArraylisttoFMP(this.newFiles32));
        return result;
    }
    
    private static ArrayList<FileMetaProperty> iterArraylisttoFMP(ArrayList<FileMeta> newFile) {
        ArrayList<FileMetaProperty> result = new ArrayList<>();
        for (int i = 0; i < newFile.size(); i++) {
            result.add(new FileMetaProperty(newFile.get(i)));
        }
        return result;
    }
    
    // @Temp Folder Delete function
    // need to fix
    public boolean deleteTemp(HashMap<File, File> updated) {
        boolean tempDelete = false;
        Iterator iter = updated.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry pair = (Map.Entry) iter.next();
            Path tmp_src = (Path) pair.getKey();
            Path MU_temp = tmp_src.getParent();
            File temp_folder = new File(MU_temp.toString());
            
            File tmp_file = new File(tmp_src.toString());
            tmp_file.delete();
            
            if (temp_folder.list().length == 0) {
                temp_folder.delete();
                tempDelete = true;
            } else {
                tempDelete = false;
            }
            iter.remove();
        }
        return tempDelete;
    }
    
    public void setNull() {
        this.searchResult = new ArrayList<HashMap<FileMeta, FileMeta>>();
        this.searchResult32 = new HashMap<FileMeta, FileMeta>();
        this.searchResult64 = new HashMap<FileMeta, FileMeta>();
        this.newFiles32 = new ArrayList<FileMeta>();
        this.newFiles64 = new ArrayList<FileMeta>();
    }
    
    public ArrayList<FileMeta> getNewFiles64() {
        return newFiles64;
    }
    
    public ArrayList<FileMeta> getNewFiles32() {
        return newFiles32;
    }
    
    public void setNewFiles64(ArrayList<FileMeta> newFiles64) {
        this.newFiles64 = newFiles64;
    }
    
    public void setNewFiles32(ArrayList<FileMeta> newFiles32) {
        this.newFiles32 = newFiles32;
    }
}
