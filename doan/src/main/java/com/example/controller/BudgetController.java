package com.example.controller;

import com.example.dao.DanhMucDAO;
import com.example.dao.NganSachDAO;
import com.example.model.DanhMuc;
import com.example.model.NganSach;
import com.example.util.MoneyInputUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class BudgetController {
    private Stage stage;
    private Scene scene;
    private String soTaiKhoan;
    
    private TableView<NganSach> table;
    private ComboBox<Integer> cbThang;
    private ComboBox<Integer> cbNam;
    private NganSachDAO nganSachDAO;
    private DanhMucDAO danhMucDAO;
    
    public BudgetController(Stage stage, String soTaiKhoan) {
        this.stage = stage;
        this.soTaiKhoan = soTaiKhoan;
        this.nganSachDAO = new NganSachDAO();
        this.danhMucDAO = new DanhMucDAO();
        
        createUI();
    }
    
    private void createUI() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_CENTER);
        
        // Header
        Label title = new Label("QUẢN LÝ NGÂN SÁCH");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        // Bộ lọc tháng/năm
        HBox filterBox = createFilterBox();
        
        // Bảng ngân sách
        table = createTable();
        VBox.setVgrow(table, Priority.ALWAYS);
        
        // Nút chức năng
        HBox buttonBox = createButtonBox();
        
        root.getChildren().addAll(title, filterBox, table, buttonBox);
        
        scene = new Scene(root, 1200, 800);
        loadData();
    }
    
    private HBox createFilterBox() {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER);
        
        Label lblThang = new Label("Tháng:");
        cbThang = new ComboBox<>();
        for (int i = 1; i <= 12; i++) {
            cbThang.getItems().add(i);
        }
        cbThang.setValue(LocalDate.now().getMonthValue());
        cbThang.setOnAction(e -> loadData()); // Tự động load khi thay đổi
        
        Label lblNam = new Label("Năm:");
        cbNam = new ComboBox<>();
        int currentYear = LocalDate.now().getYear();
        for (int i = currentYear - 2; i <= currentYear + 2; i++) {
            cbNam.getItems().add(i);
        }
        cbNam.setValue(currentYear);
        cbNam.setOnAction(e -> loadData()); // Tự động load khi thay đổi
        
        box.getChildren().addAll(lblThang, cbThang, lblNam, cbNam);
        return box;
    }
    
    private TableView<NganSach> createTable() {
        TableView<NganSach> table = new TableView<>();
        
        TableColumn<NganSach, String> colDanhMuc = new TableColumn<>("Danh Mục");
        colDanhMuc.setCellValueFactory(new PropertyValueFactory<>("tenDanhMuc"));
        colDanhMuc.setPrefWidth(200);
        
        TableColumn<NganSach, BigDecimal> colGioiHan = new TableColumn<>("Giới Hạn (VNĐ)");
        colGioiHan.setCellValueFactory(new PropertyValueFactory<>("gioiHan"));
        colGioiHan.setPrefWidth(150);
        colGioiHan.setCellFactory(col -> new TableCell<NganSach, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%,.0f", item.doubleValue()));
                }
            }
        });
        
        TableColumn<NganSach, Void> colDaChi = new TableColumn<>("Đã Chi (VNĐ)");
        colDaChi.setPrefWidth(150);
        colDaChi.setCellFactory(col -> new TableCell<NganSach, Void>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    NganSach ns = getTableView().getItems().get(getIndex());
                    double daChi = nganSachDAO.layTongChiTheoDanhMuc(
                        ns.getSoTaiKhoan(), 
                        ns.getDanhMucId(), 
                        ns.getThang(), 
                        ns.getNam()
                    );
                    setText(String.format("%,.0f", daChi));
                    
                    // Đổi màu nếu vượt ngân sách
                    BigDecimal gioiHan = ns.getGioiHan();
                    if (daChi >= gioiHan.doubleValue()) {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: green;");
                    }
                }
            }
        });
        
        TableColumn<NganSach, Void> colConLai = new TableColumn<>("Còn Lại (VNĐ)");
        colConLai.setPrefWidth(150);
        colConLai.setCellFactory(col -> new TableCell<NganSach, Void>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    NganSach ns = getTableView().getItems().get(getIndex());
                    double daChi = nganSachDAO.layTongChiTheoDanhMuc(
                        ns.getSoTaiKhoan(), 
                        ns.getDanhMucId(), 
                        ns.getThang(), 
                        ns.getNam()
                    );
                    BigDecimal gioiHan = ns.getGioiHan();
                    double conLai = gioiHan.doubleValue() - daChi;
                    setText(String.format("%,.0f", conLai));
                    
                    if (conLai < 0) {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: blue;");
                    }
                }
            }
        });
        
        TableColumn<NganSach, Integer> colThang = new TableColumn<>("Tháng");
        colThang.setCellValueFactory(new PropertyValueFactory<>("thang"));
        colThang.setPrefWidth(80);
        
        TableColumn<NganSach, Integer> colNam = new TableColumn<>("Năm");
        colNam.setCellValueFactory(new PropertyValueFactory<>("nam"));
        colNam.setPrefWidth(80);
        
        table.getColumns().addAll(colDanhMuc, colGioiHan, colDaChi, colConLai, colThang, colNam);
        return table;
    }
    
    private HBox createButtonBox() {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER);
        
        Button btnThem = new Button("Thêm Ngân Sách");
        btnThem.setOnAction(e -> handleThem());
        
        Button btnSua = new Button("Sửa");
        btnSua.setOnAction(e -> handleSua());
        
        Button btnXoa = new Button("Xóa");
        btnXoa.setOnAction(e -> handleXoa());
        
        Button btnQuayLai = new Button("Quay Lại");
        btnQuayLai.setOnAction(e -> {
            DashboardController dashboard = new DashboardController(stage);
            stage.setScene(dashboard.getScene());
            stage.setResizable(false);
            stage.setWidth(1200);
            stage.setHeight(830);
            stage.centerOnScreen();
        });
        
        box.getChildren().addAll(btnThem, btnSua, btnXoa, btnQuayLai);
        return box;
    }
    
    private void loadData() {
        int thang = cbThang.getValue();
        int nam = cbNam.getValue();
        List<NganSach> danhSach = nganSachDAO.layNganSachTheoUser(soTaiKhoan, thang, nam);
        table.getItems().setAll(danhSach);
    }
    
    private void handleThem() {
        Dialog<NganSach> dialog = new Dialog<>();
        dialog.setTitle("Thêm Ngân Sách");
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        
        ComboBox<DanhMuc> cbDanhMuc = new ComboBox<>();
        List<DanhMuc> danhSachDM = danhMucDAO.layDanhMucConTheoLoai(soTaiKhoan, "chi");
        cbDanhMuc.getItems().addAll(danhSachDM);
        cbDanhMuc.setPlaceholder(new Label("Chưa có danh mục con loại Chi"));
        
        TextField txtGioiHan = new TextField();
        txtGioiHan.setPromptText("Nhập số tiền giới hạn (tối đa 9,999,999,999)");
        MoneyInputUtil.attachMoneyFormatter(txtGioiHan);
        
        // Thêm chọn tháng/năm - CHỈ TỪ THÁNG HIỆN TẠI TRỞ ĐI
        LocalDate today = LocalDate.now();
        int thangHienTai = today.getMonthValue();
        int namHienTai = today.getYear();
        
        ComboBox<Integer> cbThangMoi = new ComboBox<>();
        ComboBox<Integer> cbNamMoi = new ComboBox<>();
        
        // Thêm năm hiện tại và 2 năm tiếp theo
        for (int i = namHienTai; i <= namHienTai + 2; i++) {
            cbNamMoi.getItems().add(i);
        }
        cbNamMoi.setValue(namHienTai);
        
        // Listener để cập nhật danh sách tháng khi đổi năm
        cbNamMoi.setOnAction(e -> {
            int namChon = cbNamMoi.getValue();
            cbThangMoi.getItems().clear();
            
            if (namChon == namHienTai) {
                // Năm hiện tại: chỉ tháng hiện tại trở đi
                for (int i = thangHienTai; i <= 12; i++) {
                    cbThangMoi.getItems().add(i);
                }
            } else {
                // Năm tương lai: tất cả tháng
                for (int i = 1; i <= 12; i++) {
                    cbThangMoi.getItems().add(i);
                }
            }
            
            if (!cbThangMoi.getItems().isEmpty()) {
                cbThangMoi.setValue(cbThangMoi.getItems().get(0));
            }
        });
        
        // Khởi tạo tháng ban đầu
        for (int i = thangHienTai; i <= 12; i++) {
            cbThangMoi.getItems().add(i);
        }
        cbThangMoi.setValue(thangHienTai);
        
        grid.add(new Label("Danh Mục:"), 0, 0);
        grid.add(cbDanhMuc, 1, 0);
        grid.add(new Label("Giới Hạn:"), 0, 1);
        grid.add(txtGioiHan, 1, 1);
        grid.add(new Label("Tháng:"), 0, 2);
        grid.add(cbThangMoi, 1, 2);
        grid.add(new Label("Năm:"), 0, 3);
        grid.add(cbNamMoi, 1, 3);
        
        dialog.getDialogPane().setContent(grid);
        
        ButtonType btnOK = new ButtonType("Thêm", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnOK, ButtonType.CANCEL);
        
        dialog.setResultConverter(btn -> {
            if (btn == btnOK) {
                try {
                    DanhMuc dm = cbDanhMuc.getValue();
                    String gioiHanStr = txtGioiHan.getText().trim();
                    Integer thang = cbThangMoi.getValue();
                    Integer nam = cbNamMoi.getValue();

                    String validationError = validateInputThemNganSach(dm, gioiHanStr, thang, nam);
                    if (validationError != null) {
                        showAlert("Lỗi", validationError);
                        return null;
                    }
                    BigDecimal gioiHanBD = MoneyInputUtil.parseMoney(gioiHanStr);

                    if (nganSachDAO.kiemTraTonTai(soTaiKhoan, dm.getId(), thang, nam)) {
                        showAlert("Lỗi", "Ngân sách cho danh mục \"" + dm.getTenDanhMuc() + 
                                 "\" tháng " + thang + "/" + nam + " đã tồn tại!\n" +
                                 "Vui lòng chọn danh mục hoặc tháng khác.");
                        return null;
                    }
                    
                    NganSach ns = new NganSach();
                    ns.setDanhMucId(dm.getId());
                    ns.setSoTaiKhoan(soTaiKhoan);
                    ns.setGioiHan(gioiHanBD);
                    ns.setThang(thang);
                    ns.setNam(nam);
                    return ns;
                } catch (NumberFormatException e) {
                    showAlert("Lỗi", "Số tiền không hợp lệ! Vui lòng nhập số.");
                    return null;
                }
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(ns -> {
            if (nganSachDAO.themNganSach(ns)) {
                showAlert("Thành công", "Thêm ngân sách thành công!");
                loadData();
            } else {
                showAlert("Lỗi", "Thêm ngân sách thất bại!");
            }
        });
    }
    
    private void handleSua() {
        NganSach selected = table.getSelectionModel().getSelectedItem();
        String validationError = validateInputChonNganSach(selected, "sua");
        if (validationError != null) {
            showAlert("Lỗi", validationError);
            return;
        }
        
        TextInputDialog dialog = new TextInputDialog(String.format("%,.0f", selected.getGioiHan().doubleValue()).replace(',', '.'));
        dialog.setTitle("Sửa Ngân Sách");
        dialog.setHeaderText("Sửa giới hạn cho: " + selected.getTenDanhMuc() + " (Tháng " + 
                            selected.getThang() + "/" + selected.getNam() + ")");
        dialog.setContentText("Giới hạn mới (tối đa 9,999,999,999):");

        TextField txtEditor = dialog.getEditor();
        MoneyInputUtil.attachMoneyFormatter(txtEditor);
        
        dialog.showAndWait().ifPresent(gioiHanStr -> {
            try {
                String inputError = validateInputSuaNganSach(gioiHanStr);
                if (inputError != null) {
                    showAlert("Lỗi", inputError);
                    return;
                }

                BigDecimal gioiHanBD = MoneyInputUtil.parseMoney(gioiHanStr.trim());

                selected.setGioiHan(gioiHanBD);
                if (nganSachDAO.suaNganSach(selected)) {
                    showAlert("Thành công", "Sửa ngân sách thành công!");
                    loadData();
                } else {
                    showAlert("Lỗi", "Sửa ngân sách thất bại!");
                }
            } catch (NumberFormatException e) {
                showAlert("Lỗi", "Số tiền không hợp lệ! Vui lòng nhập số.");
            }
        });
    }
    
    public static String validateInputThemNganSach(DanhMuc danhMuc,
                                                   String gioiHanStr,
                                                   Integer thang,
                                                   Integer nam) {
        if (danhMuc == null || gioiHanStr == null || gioiHanStr.trim().isEmpty()
                || thang == null || nam == null) {
            return "Vui lòng điền đầy đủ thông tin!";
        }

        if (thang < 1 || thang > 12) {
            return "Tháng không hợp lệ!";
        }

        return validateInputSuaNganSach(gioiHanStr);
    }

    public static String validateInputSuaNganSach(String gioiHanStr) {
        if (gioiHanStr == null || gioiHanStr.trim().isEmpty()) {
            return "Vui lòng nhập giới hạn ngân sách!";
        }

        try {
            BigDecimal gioiHan = MoneyInputUtil.parseMoney(gioiHanStr);
            if (gioiHan == null) {
                return "Số tiền không hợp lệ! Vui lòng nhập số.";
            }
            if (gioiHan.compareTo(BigDecimal.ZERO) <= 0) {
                return "Giới hạn phải lớn hơn 0!";
            }
            if (gioiHan.compareTo(new BigDecimal("9999999999")) > 0) {
                return "Giới hạn quá lớn! Vui lòng nhập số tiền nhỏ hơn 10 tỷ.";
            }
        } catch (NumberFormatException e) {
            return "Số tiền không hợp lệ! Vui lòng nhập số.";
        }

        return null;
    }

    public static String validateInputChonNganSach(NganSach selected, String hanhDong) {
        if (selected == null) {
            if ("xoa".equals(hanhDong)) {
                return "Vui lòng chọn ngân sách cần xóa!";
            }
            return "Vui lòng chọn ngân sách cần sửa!";
        }

        return null;
    }

    private void handleXoa() {
        NganSach selected = table.getSelectionModel().getSelectedItem();
        String validationError = validateInputChonNganSach(selected, "xoa");
        if (validationError != null) {
            showAlert("Lỗi", validationError);
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận");
        confirm.setHeaderText("Xóa ngân sách: " + selected.getTenDanhMuc());
        confirm.setContentText("Bạn có chắc chắn muốn xóa?");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (nganSachDAO.xoaNganSach(selected.getId())) {
                    showAlert("Thành công", "Xóa ngân sách thành công!");
                    loadData();
                } else {
                    showAlert("Lỗi", "Xóa ngân sách thất bại!");
                }
            }
        });
    }
    
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    public Scene getScene() {
        return scene;
    }
}
