# Module Updater

### 최소 JRE 사양  
+ java jre >= 1.8.0

### 코드 구성

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
+ 패치된 모듈이 있는 폴더 경로를 선택한다
+ 검색된 모듈 결과 
    + 검색 결과 교체되어야 할 대상(초록색) 교체 될 필요가 없는 대상(회색) 교체 될 모듈보다 더 최신의 모듈(빨간색)
+ NewFile에 빨간색이 켜져 있다면, 하나 이상의 모듈에서 발견된 core module 이라 교체 대상을 직접 설정해줘야 한다.
+ NewFile에 빨간색이 없다면 단순히 새로 추가된 모듈로 사용자가 직접 부여받은 경로에 추가해준다.
+ DRM 이 켜져있다면, QDRM.exe 으로 DRM을 종료 시켜준다.
+ Update 버튼을 누르고 update message를 입력하면, 몇개의 모듈들이 교체가 됐고 몇개의 모듈들이 교체가 실패 되었는지 나온다.
+ 기존의 모듈들은 mu.exe와 같은 경로에 생성된 MU_temp 폴더에 시간별로 백업 된다.
+ 실패된 모듈들은 Module_Updater.log에서 확인 할 수 있고, 직접 해당 된 모듈들을 수동으로 교체해준다.
+ 방금 교체한 모듈들을 다시 되돌리고 싶을때 revert 버튼을 누른다.
+ 예전의 모듈 구성으로 돌아가고 싶으면 모듈이 있는 폴더 경로를 백업 폴더(MU_temp)로 지정해준다.
+ mu.exe와 같은 폴더에 Module_Updater.log, settings.ini 가 생성된다
+ Module_Updater.log 는 로그 파일이다. 교체에 실패한 파일이 뭔지 확인할 용도.
+ 업데이트 하고 싶은 경로를 추가 하고 싶을 때는 setting.ini를 규칙에 맞게 설정 해준다.

### settings.ini
+ 시스템 64 환경에서의 경로 
    + [target_dirs_32]
    + [target_dirs_64]

+ 시스템 32 환경에서의 경로 
    + [system_32]

+ 패치 될 모듈의 경로(다운 받은)
    + [source_dir]
    + default = 

### Client.java
+ public static boolean system64()
    + Windows 시스템 환경이  64bit 인지 32bit 인지를 확인 해준다
+ public void search()
    +  해당 시스템 파일들의 이름과 수정 시간을 비교해준다
    + system32 일 경우 32bit 해당 파일
        + source32Meta, system32Meta
    + system64 일 경우 64/32bit 모두
        + source32Meta, target32Meta
        + source64Meta, target64Meta
    + 자료 구조 FileMeta.java 참조 
    +  searchForUpdate, searchForUpdate32 함수 호출
    
+ public int update(ArrayList<FileMetaProperty> data)  
    + temp folder 및  backup folder 생성 
    + 프로세스들을 꺼준다 
    + getUpdatedTargetName 을 이용해 타겟 파일의 이름을 바꿔준다
    +  바꿔준 파일을 백업 폴더에 저장한다 
    +  소스 파일들을 타겟 폴더에 저장한다
    +  실패/성공한 파일들을 로그에 기록한다 
    +  필요한 프로세스를 재시작 해준다


+ public boolean addNewFile(FileMeta newFile, String tDir)
   + 타겟 폴더에 없는 새로운 모듈이 소스 폴더에 있을 때 사용자로 부터 입력 받은 폴더에 복사해준다
   
+ private int revertMatched()
    + 	update 해준 파일들을 되돌린다
    
+ public ArrayList<FileMetaProperty> printNewFile()
    + view 에서 쓰는 StringProperty 로 변환 해주는 함수

+ 	private static ArrayList<FileMetaProperty> iterArraylisttoFMP(ArrayList<FileMeta> newFile) {
    + view 에서 쓰는 StringProperty 로 변환 해주는 함수

### MainApp.java
+ public void start(Stage stage) throws Exception 
    + controller 함수
    
### INIFile.java
+ ini 파일 세팅

### Log.java
+ 로그 파일
+ LogFilter.java
+ LogFormatter.java
+ LogHandler.java
