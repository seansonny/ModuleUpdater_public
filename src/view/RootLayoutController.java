package view;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import controller.MainApp;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import model.FileMetaProperty;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.StageStyle;

public class RootLayoutController implements Initializable {
	@FXML
	private VBox folderVBox;
	@FXML
	private ImageView refreshImage;
	@FXML
	private VBox refreshButton;
	@FXML
	private Label versionLabel;
	@FXML
	private ImageView fasooLogo;
	@FXML
	private Label statusText;
	@FXML
	private Label sourceFolderLabel;
	@FXML
	private ImageView drmToggle;
	@FXML
	private Label drmText;
	@FXML
	private ImageView shutdownImage;
	@FXML
	private ImageView minimizeImage;
	@FXML
	private ImageView settingsImage;
	@FXML
	private VBox updateButton;
	@FXML
	private ImageView updateImage;
	@FXML
	private ImageView selectFolderImageView;
	@FXML
	private ImageView revertImage;
	@FXML
	private TableView<FileMetaProperty> fileTableView;
	@FXML 
	private TableColumn<FileMetaProperty, CheckBox> checkBoxColumn;
	@FXML
	private TableColumn<FileMetaProperty, String> fileNameColumn;
	@FXML
	private TableColumn<FileMetaProperty, String> beforeColumn;
	@FXML
	private TableColumn<FileMetaProperty, String> afterColumn;
	@FXML
	private TableColumn<FileMetaProperty, String> locationColumn;
	@FXML
	private TableView<FileMetaProperty> newFileTableView;
	@FXML
	private TableColumn<FileMetaProperty, String> newFileColumn;
	@FXML
	private TableColumn<FileMetaProperty, String> selectedLocationColumn;
	@FXML
	private AnchorPane homePane;
	public MainApp mainApp;
	boolean drmOffOn = true;
	double xOffset = 0.0;
	double yOffset = 0.0;
	Popup popUp;
	CheckBox select_all;


	@Override
	public void initialize(URL location, ResourceBundle resources) {

		// Initialize filetableview and newfiletableview with blank values
		fileTableView.setPlaceholder(new Label(""));
		newFileTableView.setPlaceholder(new Label(""));

		// Set tableview columns with filemetaproperty variables
		fileNameColumn.setCellValueFactory(cellData -> cellData.getValue().getFileName());
		beforeColumn.setCellValueFactory(cellData -> cellData.getValue().getBeforeDate());
		afterColumn.setCellValueFactory(cellData -> cellData.getValue().getAfterDate());
		locationColumn.setCellValueFactory(cellData -> cellData.getValue().getFileLocation());
		checkBoxColumn.setCellValueFactory(new PropertyValueFactory<FileMetaProperty, CheckBox>("checkBox")); 
		
		// Disable resizing tableview columns
		fileNameColumn.setResizable(false);
		beforeColumn.setResizable(false);
		afterColumn.setResizable(false);
		locationColumn.setResizable(false);

		// Set data for new file tableview
		newFileColumn.setCellValueFactory(cellData -> cellData.getValue().getFileName());
		selectedLocationColumn.setCellValueFactory(cellData -> cellData.getValue().getFileLocation());

		// Disable new file tableview's columns
		newFileColumn.setResizable(false);
		selectedLocationColumn.setResizable(false);
		newFileTableView.setEditable(false);
		
		select_all = new CheckBox();
		
		checkBoxColumn.setGraphic(select_all);
		
		
	}



	public void setMainApp(MainApp mainApp) {
		this.mainApp = mainApp;
	}

	public MainApp getMainApp() {
		return mainApp;
	}

	public ImageView getDrmToggle() {
		return drmToggle;
	}

	public Label getDrmText() {
		return drmText;
	}

	public ImageView getSelectFolderImageView() {
		return selectFolderImageView;
	}

	public Label getSourceFolderLabel() {
		return sourceFolderLabel;
	}

	public VBox getUpdateButton() {
		return updateButton;
	}

	public TableView<FileMetaProperty> getFileTableView() {
		return fileTableView;
	}

	public TableView<FileMetaProperty> getNewFileTableView() {
		return newFileTableView;
	}

	public ImageView getRevertImage() {
		return revertImage;
	}

	public ImageView getShutdownImage() {
		return shutdownImage;
	}

	public void setShutdownImage(ImageView shutdownImage) {
		this.shutdownImage = shutdownImage;
	}

	public ImageView getMinimizeImage() {
		return minimizeImage;
	}

	public void setMinimizeImage(ImageView minimizeImage) {
		this.minimizeImage = minimizeImage;
	}

	public ImageView getSettingsImage() {
		return settingsImage;
	}

	public Label getStatusText() {
		return statusText;
	}
	
	public TableColumn<FileMetaProperty, CheckBox> getCheckBoxColumn() {
		return checkBoxColumn;
	}

	public TableColumn<FileMetaProperty, String> getLocationColumn() {
		return locationColumn;
	}

	public TableColumn<FileMetaProperty, String> getSelectedLocationColumn() {
		return selectedLocationColumn;
	}

	public ImageView getFasooLogo() {
		return fasooLogo;
	}

	public String showAlert() {
		TextInputDialog dialog = new TextInputDialog();
		dialog.initStyle(StageStyle.UTILITY);
		dialog.initModality(Modality.APPLICATION_MODAL);
		dialog.setTitle("Text Input Dialog");
		dialog.setHeaderText("업데이트 하시겠습니까?"); 
		dialog.setContentText("메세지 (200자 이내):");

		// Traditional way to get the response value.
		Optional<String> result = dialog.showAndWait();
		if (result.isPresent()){
		    return result.get();
		} else {
			return null;
		}
	}

	public ImageView getUpdateImage() {
		return updateImage;
	}

	public Label getVersionLabel() {
		return versionLabel;
	}

	public VBox getRefreshButton() {
		return refreshButton;
	}

	public ImageView getRefreshImage() {
		return refreshImage;
	}

	public CheckBox getSelect_all() {
		return select_all;
	}

	public VBox getFolderVBox() {
		return folderVBox;
	}

}
