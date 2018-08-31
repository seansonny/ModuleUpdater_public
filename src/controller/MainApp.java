package controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import view.*;
import model.*;
import utils.*;

public class MainApp extends Application {
	boolean isDrmOn;
	boolean newFilesExist = false;
	boolean allFilesChoosen = false;
	boolean tobePatched;
	boolean updated = false;
	boolean isChanged;
	double xOffset = 0.0;
	double yOffset = 0.0;
	Stage primaryStage;
	File selectedDirectory;
	String selectedDirectoryPath;
	public RootLayoutController rootLayoutController = new RootLayoutController();
	Timer timer;
	public ObservableList<FileMetaProperty> fileData;
	public ObservableList<FileMetaProperty> newFileData;
	Client client;
	public HashMap<FileMeta, String> newFileNameAndDirectory = new HashMap<FileMeta, String>();

	public static void main(String[] args) throws IOException, InterruptedException {
		// 여기에 file meta 두개 > FileMetaProperty (ObservableArrayList)
		Log.logInfo("프로그램 실행\r\n");
		launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		Log.logInfo("프로그램 실행");
		// set fxml files and controllers
		this.primaryStage = stage;
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(getClass().getResource("/view/RootLayout.fxml"));
		Parent root = loader.load();
		rootLayoutController = loader.getController();
		rootLayoutController.setMainApp(this);

		// Add new file + target directory to arraylist
		// newfiledata << list size
		rootLayoutController.getNewFileTableView()
				.setRowFactory(new Callback<TableView<FileMetaProperty>, TableRow<FileMetaProperty>>() {
					@Override
					public TableRow<FileMetaProperty> call(TableView<FileMetaProperty> tableView) {
						TableRow<FileMetaProperty> row = new TableRow<FileMetaProperty>(){
							@Override
							protected void updateItem(FileMetaProperty fm, boolean empty) {
								super.updateItem(fm, empty);

								if (empty) {
									setText(null);
								} else {
									boolean dup = fm.getSource().getCoreModule();
									if (dup) {
										super.setStyle("-fx-background-color: #ff8080;"); // red
									}
								}
							}
						};
						row.setOnMouseClicked(event -> {
							if (event.getClickCount() > 1 && row.getItem() != null) {
								FileMetaProperty meta = row.getItem();
								String targetDir = directoryChooserForNew();
								meta.setFileLocation(new SimpleStringProperty(targetDir));
								setStatus("Directory for " + meta.getFileName().get() + " has been chosen: "
										+ meta.getFileLocation().get());
								newFileNameAndDirectory.put(meta.getSource(), meta.getFileLocation().get());
							}
						});
						rootLayoutController.getNewFileTableView().refresh();
						return row;
					}
				});


		rootLayoutController.getFileTableView()
				.setRowFactory(new Callback<TableView<FileMetaProperty>, TableRow<FileMetaProperty>>() {
					@Override
					public TableRow<FileMetaProperty> call(TableView<FileMetaProperty> tableView) {
						TableRow<FileMetaProperty> row = new TableRow<FileMetaProperty>() {
							@Override
							protected void updateItem(FileMetaProperty fm, boolean empty) {
								super.updateItem(fm, empty);
								if (empty) {
									setText(null);
								} else {
									if (fm.getSource().getModifedDate() == -1) {
										super.setStyle("-fx-background-color: #ff8080;"); // red
									} else if (fm.getSource().getModifedDate() == 0) {
										super.setStyle("-fx-background-color: #cccccc;"); // grey
									} else {
										super.setStyle("-fx-background-color: #80ff80;"); // green
									}
								}
							}
						};
						return row;
					}
				});
		// Add listener to filetable view so when row is double clicked, target
		// directory is opened
		rootLayoutController.getLocationColumn().setCellFactory(
				new Callback<TableColumn<FileMetaProperty, String>, TableCell<FileMetaProperty, String>>() {
					@Override
					public TableCell<FileMetaProperty, String> call(TableColumn<FileMetaProperty, String> col) {
						final TableCell<FileMetaProperty, String> cell = new TableCell<FileMetaProperty, String>() {
							@Override
							public void updateItem(String firstName, boolean empty) {
								super.updateItem(firstName, empty);
								if (empty) {
									setText(null);
								} else {
									setText(firstName);
								}
							}
						};
						cell.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
							@Override
							public void handle(MouseEvent event) {
								TableRow<FileMetaProperty> row = cell.getTableRow();
								if (event.getClickCount() > 1 && row.getItem() != null) {
									FileMetaProperty meta = row.getItem();
									File file = new File(meta.getTarget().getfPath());
									try {
										Runtime.getRuntime().exec("explorer.exe /select," + file.getAbsolutePath());
									} catch (IOException e) {
										setStatus("FileTableView error: " + e.getMessage());
										System.out.println("Error: " + e.getMessage());
									}
								}
							}
						});
						rootLayoutController.getFileTableView().refresh();
						return cell;
					}
				});

		// Set offset X, Y values
		root.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				xOffset = event.getSceneX();
				yOffset = event.getSceneY();
			}

		});

		// Use offset X, Y values to move stage around
		root.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				stage.setX(event.getScreenX() - xOffset);
				stage.setY(event.getScreenY() - yOffset);
			}
		});

		// pressing stage = clears selected tables
		root.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				rootLayoutController.getFileTableView().getSelectionModel().clearSelection();
				rootLayoutController.getNewFileTableView().getSelectionModel().clearSelection();
			}
		});

		// setup
		client = new Client();
		client.readSourceDirectoryFromIni();
		if (client.getSource_dir() != null) {
			client = new Client();
			client.readSourceDirectoryFromIni();
			client.search();

			// Set data to view
			fileData = FXCollections.observableArrayList(client.printSearchResult());
			rootLayoutController.getFileTableView().setItems(fileData);

			// Set new file data to view
			newFileData = FXCollections.observableArrayList(client.printNewFile());
			rootLayoutController.getNewFileTableView().setItems(newFileData);
			// Set source directory label to settings.ini source directory
			rootLayoutController.getSourceFolderLabel().setText(client.getSource_dir());
		} else {
			if (isDrmOn) {
				setStatus("DRM이 켜져있습니다. DRM을 꺼주세요.");
			} else {
				rootLayoutController.getSourceFolderLabel().setText("Set Source Directory");
				setStatus("Source 다이렉토리를 설정해주세요...");
			}
		}

		// exits process when closed not using exit button
		// Platform.setImplicitExit(false);
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				shutdown();
			}
		});

		// Set listeners to Imageviews
		setListeners();

		// timer to check DRM is on/off
		setToggle();

		// set version
		rootLayoutController.getVersionLabel().setText("v1.4.0");

		// set primary stage initial values
		primaryStage.initStyle(StageStyle.TRANSPARENT);
		primaryStage.setTitle("Module Updater");
		primaryStage.setResizable(true);
		primaryStage.setScene(new Scene(root));
		primaryStage.show();
	}

	// Choose source directory
	public String directoryChooser() {
		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle("Select Source Directory");
		String userhome = System.getProperty("user.home");
		File defaultDirectory = new File(userhome + "\\Downloads");
		chooser.setInitialDirectory(defaultDirectory);
		selectedDirectory = chooser.showDialog(this.getPrimaryStage());
		if (selectedDirectory == null) {
			return null;

		} else {
			setStatus("Source directory set.");
			selectedDirectoryPath = selectedDirectory.getAbsolutePath();
			rootLayoutController.getSourceFolderLabel().setText(selectedDirectoryPath);
			return selectedDirectoryPath;
		}
	}

	// Choose directory for new files (default directory is user's download folder)
	public String directoryChooserForNew() {
		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle("Select Target Directory");
		String userhome = System.getProperty("user.home");
		File defaultDirectory = new File(userhome + "\\Downloads");
		chooser.setInitialDirectory(defaultDirectory);
		File newDirectory1 = chooser.showDialog(this.getPrimaryStage());
		if (newDirectory1 == null) {
			setStatus("Directory chooser has been canceled");
			return null;
		} else {
			return newDirectory1.getAbsolutePath();
		}
	}
	boolean isAllSelected = false;
	private void selectAllBoxes(ActionEvent e) {
		
		if(!isAllSelected)	{
			// Iterate through all items in ObservableList
			for (FileMetaProperty item : fileData) {
				// And change "selected" boolean
				item.getCheckBox().setSelected(((CheckBox) e.getSource()).isSelected());
			}
			setStatus(fileData.size() + "개의 파일이 모두 선택 되었습니다.");
			isAllSelected = true;
		} else {
			for (FileMetaProperty item : fileData) {
				// And change "selected" boolean
				item.getCheckBox().setSelected(false);
			}
			setStatus("전체 선택 취소.");
			isAllSelected = false;
		}
		
	}

	public void setListeners() {
		// select all column
		rootLayoutController.getSelect_all().setOnAction(e -> selectAllBoxes(e));
		// refresh button

		rootLayoutController.getRefreshButton().setOnMouseEntered(event -> {
			rootLayoutController.getRefreshImage()
					.setImage((new Image("/view/img/icons8_Synchronize_100px_1_blur.png")));
		});

		rootLayoutController.getRefreshButton().setOnMouseExited(event -> {
			rootLayoutController.getRefreshImage().setImage((new Image("/view/img/icons8_Synchronize_100px_1.png")));
		});

		rootLayoutController.getRefreshButton().setOnMouseClicked(event -> {
			String path = client.getSource_dir();
			if (path != null) {

				client.setSourceDirectory(path);
				client.setNull();
				client.search();

				// Set data to view
				fileData = FXCollections.observableArrayList(client.printSearchResult());
				rootLayoutController.getFileTableView().setItems(fileData);

				// Set new file data to view
				// rootLayoutController.getNewFileTableView().getItems().clear();

				newFileData = FXCollections.observableArrayList(client.printNewFile());
				rootLayoutController.getNewFileTableView().setItems(newFileData);
				Log.logInfo("새로고침 :" + path);

				setStatus("Source 다이렉토리 선택 완료. 업데이트 항목은 총: " + fileData.size() + "개 (matched) + " + newFileData.size()
						+ " (new)" + "입니다.");
			}

		});

		// Folder - blur images when mouse enters
		rootLayoutController.getFolderVBox().setOnMouseEntered(event -> {
			rootLayoutController.getSourceFolderLabel().setStyle("-fx-underline: true");
			rootLayoutController.getSelectFolderImageView().setImage(new Image("/view/img/folder_blurred.png"));
		});
		rootLayoutController.getFolderVBox().setOnMouseExited(event -> {
			rootLayoutController.getSourceFolderLabel().setStyle("-fx-underline: false");
			rootLayoutController.getSelectFolderImageView().setImage(new Image("/view/img/folder.png"));
		});

		rootLayoutController.getFolderVBox().setOnMouseClicked(event -> {
			client = new Client();
			String path = directoryChooser();
			if (path != null) {
				client.setSourceDirectory(path);
				client.search();

				// Set data to view
				fileData = FXCollections.observableArrayList(client.printSearchResult());
				rootLayoutController.getFileTableView().setItems(fileData);

				// Set new file data to view
				// rootLayoutController.getNewFileTableView().getItems().clear();
				newFileData = FXCollections.observableArrayList(client.printNewFile());
				rootLayoutController.getNewFileTableView().setItems(newFileData);

				setStatus("Source 다이렉토리 선택 완료. 업데이트 항목은 총: " + fileData.size() + "개 (matched) + " + newFileData.size()
						+ " (new)" + "입니다.");
			} else {
				setStatus("다이렉토리 선택이 취소되었습니다.");
			}
		});

		// Update - blur image when mouse enters
		rootLayoutController.getUpdateButton().setOnMouseEntered(event -> {
			rootLayoutController.getUpdateImage().setImage(new Image("/view/img/update_button_blurred.png"));
		});
		rootLayoutController.getUpdateButton().setOnMouseExited(event -> {
			rootLayoutController.getUpdateImage().setImage(new Image("/view/img/update_button.png"));
		});

		// mouse click listener for update button
		rootLayoutController.getUpdateButton().setOnMouseClicked(event -> {
			if (isDrmOn) {
				setStatus("업데이트 실패. DRM이 켜져있습니다.");
			} else {

				// need to check if items are selected before update is pressed (don't want
				// update with zero selected files)
				String message = rootLayoutController.showAlert(); // 업데이트 메세지
				if (message != null) {
					Log.logInfo("업데이트 메세지: " + message);
					int updateCount = 0;

					ArrayList<FileMetaProperty> dataToUpdate = new ArrayList<FileMetaProperty>(fileData);
					ArrayList<FileMetaProperty> dataListRemove = new ArrayList<FileMetaProperty>();
					for (int i = 0; i < fileData.size(); i++) {
						if (!fileData.get(i).getCheckBox().isSelected()) {
							dataListRemove.add(fileData.get(i));
						}
					}
					dataToUpdate.removeAll(dataListRemove);

					updateCount += client.update(dataToUpdate);

					// get key/values from newfiles and send to client
					Iterator<Entry<FileMeta, String>> it = newFileNameAndDirectory.entrySet().iterator();
					while (it.hasNext()) {
						Map.Entry<FileMeta, String> pair = (Map.Entry<FileMeta, String>) it.next();
						if ((String) pair.getValue() != null) {
							boolean successUpdate = client.addNewFile(pair.getKey(), pair.getValue());
							if (successUpdate) {
								updateCount++;
							}
						}
					}
					if (updateCount == 0) {
						setStatus("업데이트된 파일이 없습니다. Log파일을 확인해주세요.");
					} else {
						int failed = dataToUpdate.size() + newFileNameAndDirectory.size() - updateCount;
						setStatus(updateCount + "개의 파일이 업데이트 되었습니다. " + failed + "개의 파일은 실패했습니다.");
					}

					updated = true; // has been updated, can revert

				} else {
					setStatus("업데이트가 취소 되었습니다.");
				}

			}

		});

		// Listeners for revert image
		rootLayoutController.getRevertImage().setOnMouseClicked(event -> {
			if (updated == false) {
				setStatus("복원 실패. 업데이트부터 해주세요.");
			} else {

				int revertSuccess = client.revert();
				setStatus(revertSuccess + "개의 파일이 복원되었습니다.");
				Log.logInfo(revertSuccess + "개의 파일이 복원되었습니다.");
				updated = false;

			}
		});

		// Revert - blur image when mouse enters
		rootLayoutController.getRevertImage().setOnMouseEntered(event -> {
			rootLayoutController.getRevertImage().setImage(new Image("/view/img/icons8_Restart_100px_blur.png"));
		});
		rootLayoutController.getRevertImage().setOnMouseExited(event -> {
			rootLayoutController.getRevertImage().setImage(new Image("/view/img/icons8_Restart_100px.png"));
		});

		// Listeners for shutdown image
		rootLayoutController.getShutdownImage().setOnMouseClicked(event -> {
			shutdown();
		});

		// Shutdown - blur image when mouse enters
		rootLayoutController.getShutdownImage().setOnMouseEntered(event -> {
			rootLayoutController.getShutdownImage().setImage(new Image("/view/img/icons8_Close_Window_100px_blur.png"));
		});
		rootLayoutController.getShutdownImage().setOnMouseExited(event -> {
			rootLayoutController.getShutdownImage().setImage(new Image("/view/img/icons8_Close_Window_100px.png"));
		});

		// Listeners for minimize image
		rootLayoutController.getMinimizeImage().setOnMouseClicked(event -> {
			getPrimaryStage().setIconified(true);
		});

		// Minimize - blur image when mouse enters
		rootLayoutController.getMinimizeImage().setOnMouseEntered(event -> {
			rootLayoutController.getMinimizeImage()
					.setImage(new Image("/view/img/icons8_Minimize_Window_100px_3_blur.png"));
		});
		rootLayoutController.getMinimizeImage().setOnMouseExited(event -> {
			rootLayoutController.getMinimizeImage().setImage(new Image("/view/img/icons8_Minimize_Window_100px_3.png"));
		});

		// Folder - blur images when mouse enters
		rootLayoutController.getSettingsImage().setOnMouseEntered(event -> {
			rootLayoutController.getSettingsImage().setImage(new Image("/view/img/icons8_Settings_96px_2_blurred.png"));
		});
		rootLayoutController.getSettingsImage().setOnMouseExited(event -> {
			rootLayoutController.getSettingsImage().setImage(new Image("/view/img/icons8_Settings_96px_2.png"));
		});

		// settings.ini 실행시 MU창 선택하면 꺼짐
		// Listeners for settings image
		rootLayoutController.getSettingsImage().setOnMouseClicked(event -> {
			ProcessBuilder pb = new ProcessBuilder("Notepad.exe", client.getIni().getSettingFile());
			Process process;
			int exitCode;
			try {

				process = pb.start();
				this.getPrimaryStage().hide();
				exitCode = process.waitFor();
				if (exitCode != 1) {
					this.getPrimaryStage().show();
				}
				setStatus("환경설정 변경 완료.");
				Log.logInfo("환경설정 파일을 열었습니다.");
			} catch (IOException e) {
				exitCode = 1;
				setStatus("Error: " + e.getMessage());
			} catch (InterruptedException e) {
				exitCode = 1;
				setStatus("Error: " + e.getMessage());
				Log.logInfo(e.getMessage());
			}
			if (exitCode == 0) {
				// refresh window
				rootLayoutController.getFileTableView().refresh();
				rootLayoutController.getNewFileTableView().refresh();
			}
		});
	}

	public void shutdown() {
		try {
			if (updated != false) {
				if (selectedDirectory != null) {
					client.getIni().saveSourceDirectory(selectedDirectory.getAbsolutePath());
				}
				client.getIni().addTargetDir(newFileNameAndDirectory); // 정복이
			}
		} catch (IOException e) {
			setStatus("Error: " + e.getMessage());
			Log.logInfo(e.getMessage());
		}

		Log.logInfo("프로그램 종료\r\n");
		// 프로그램 종류 메소드 Log.class
		getPrimaryStage().close();
		System.exit(0);
	}

	// toggle setting (is Drm OFF or ON?)
	public void setToggle() {
		Timer timer = new Timer();
		timer.schedule(new checkToggle(), 0, 2500);
	}

	class checkToggle extends TimerTask {
		@Override
		public void run() {
			Platform.runLater(() -> {
				setToggleOffOn();
			});
		}
	}

	public void setToggleOffOn() {
		try {
			isDrmOn = client.isDRMRunning();
		} catch (IOException e) {
			setStatus("settToggleoffOn() Error: " + e.getMessage());
			Log.logInfo(e.getMessage());
		} catch (InterruptedException e) {
			setStatus("settToggleoffOn() Error: " + e.getMessage());
			Log.logInfo(e.getMessage());

		}

		if (isDrmOn) {
			rootLayoutController.getDrmToggle().setImage((new Image("/view/img/icons8_Stop_Sign_100px.png")));
			rootLayoutController.getDrmText().setText("DRM is ON");
		} else if (!isDrmOn) {
			rootLayoutController.getDrmToggle().setImage((new Image("/view/img/icons8_Go_100px.png")));
			rootLayoutController.getDrmText().setText("DRM is OFF");
		}
	}

	public void setStatus(String message) {
		rootLayoutController.getStatusText().setText(message);
	}

	public boolean isTobePatched() {
		return tobePatched;
	}

	public void setTobePatched(boolean tobePatched) {
		this.tobePatched = tobePatched;
	}

	public boolean isChanged() {
		return isChanged;
	}

	public void setChanged(boolean isChanged) {
		this.isChanged = isChanged;
	}

	public double getxOffset() {
		return xOffset;
	}

	public void setxOffset(double xOffset) {
		this.xOffset = xOffset;
	}

	public double getyOffset() {
		return yOffset;
	}

	public void setyOffset(double yOffset) {
		this.yOffset = yOffset;
	}

	public Stage getPrimaryStage() {
		return primaryStage;
	}

	public void setPrimaryStage(Stage primaryStage) {
		this.primaryStage = primaryStage;
	}

	public boolean isDrm() {
		return isDrmOn;
	}

	public void setDrm(boolean isDrm) {
		this.isDrmOn = isDrm;
	}

	public File getSelectedDiretory() {
		return selectedDirectory;
	}

	public void setSelectedDiretory(File selectedDiretory) {
		this.selectedDirectory = selectedDiretory;
	}

	public RootLayoutController getController() {
		return rootLayoutController;
	}

	public void setRootController(RootLayoutController rootLayoutController) {
		this.rootLayoutController = rootLayoutController;
	}

	public Timer getTimer() {
		return timer;
	}

	public void setTimer(Timer timer) {
		this.timer = timer;
	}

	public boolean isDrmOnOff() {
		return isDrmOn;
	}

	public void setDrmOnOff(boolean isDrmOnOff) {
		this.isDrmOn = isDrmOnOff;
	}

}
