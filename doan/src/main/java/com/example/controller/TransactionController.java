package com.example.controller;

import com.example.dao.GiaoDichDAO;
import com.example.dao.DanhMucDAO;
import com.example.dao.NganSachDAO;
import com.example.model.GiaoDich;
import com.example.model.DanhMuc;
import com.example.util.MoneyInputUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller cho màn hình chuyển tiền (JavaFX thuần)
 */
public class TransactionController {

    private Stage stage;
    private Scene scene;
    private TextField txtSoTaiKhoanNhan;
    private TextField txtSoTien;
    private TextArea txtNoiDung;
    private ComboBox<DanhMuc> cbDanhMuc;
    private Button btnChuyenTien;
    private Button btnGhiTienMat;
    private Button btnQuayLai;
    private Label lblSoTaiKhoan;
    private Label lblSoDu;
    private Label lblSoDuTienMat;
    private Label lblTenNguoiNhan;
    private TableView<GiaoDichInfo> tableGiaoDich;
    private GiaoDichDAO giaoDichDAO = new GiaoDichDAO();
    private DanhMucDAO danhMucDAO = new DanhMucDAO();
    private NganSachDAO nganSachDAO = new NganSachDAO();
    private DecimalFormat df = new DecimalFormat("#,###");

    // Inner class cho hiển thị giao dịch
    public static class GiaoDichInfo {
        private String ngay;
        private String loai;
        private String taiKhoan;
        private String soTien;
        private String noiDung;

        public GiaoDichInfo(String ngay, String loai, String taiKhoan, String soTien, String noiDung) {
            this.ngay = ngay;
            this.loai = loai;
            this.taiKhoan = taiKhoan;
            this.soTien = soTien;
            this.noiDung = noiDung;
        }

        public String getNgay() { return ngay; }
        public String getLoai() { return loai; }
        public String getTaiKhoan() { return taiKhoan; }
        public String getSoTien() { return soTien; }
        public String getNoiDung() { return noiDung; }
    }

    public TransactionController(Stage stage) {
        this.stage = stage;
        createUI();
        loadData();
    }

    private void createUI() {
        // Root layout
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f5f5;");

        // === TOP: Header + Form ===
        VBox top = createTopSection();
        root.setTop(top);

        // === CENTER: Table ===
        VBox center = createCenterSection();
        root.setCenter(center);

        // Tạo scene
        scene = new Scene(root, 1200, 800);
    }

    private VBox createTopSection() {
        VBox top = new VBox(15);
        top.setPadding(new Insets(20));
        top.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-width: 0 0 2 0;");

        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(header, Priority.ALWAYS);

        Label lblTitle = new Label("CHUYỂN TIỀN");
        lblTitle.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        lblTitle.setStyle("-fx-text-fill: #2c3e50;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        btnQuayLai = new Button("Quay lại");
        btnQuayLai.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");
        btnQuayLai.setPrefHeight(35);
        btnQuayLai.setPrefWidth(110);
        btnQuayLai.setOnAction(e -> handleQuayLai());

        header.getChildren().addAll(lblTitle, spacer, btnQuayLai);

        // Thông tin tài khoản
        HBox accountInfo = new HBox(30);
        accountInfo.setAlignment(Pos.CENTER_LEFT);

        String soTaiKhoan = LoginController.currentUser.getSoTaiKhoan();
        lblSoTaiKhoan = new Label("STK: " + soTaiKhoan);
        lblSoTaiKhoan.setFont(Font.font("Arial", FontWeight.BOLD, 15));

        Label lblSoDuTitle = new Label("Số dư TK:");
        lblSoDuTitle.setFont(Font.font("Arial", 15));

        lblSoDu = new Label("0 đ");
        lblSoDu.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        lblSoDu.setStyle("-fx-text-fill: #27ae60;");

        Label lblSoDuTienMatTitle = new Label("Ví tiền mặt:");
        lblSoDuTienMatTitle.setFont(Font.font("Arial", 15));

        lblSoDuTienMat = new Label("0 đ");
        lblSoDuTienMat.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        lblSoDuTienMat.setStyle("-fx-text-fill: #f39c12;");

        accountInfo.getChildren().addAll(lblSoTaiKhoan, lblSoDuTitle, lblSoDu, lblSoDuTienMatTitle, lblSoDuTienMat);

        // Form chuyển tiền
        GridPane form = new GridPane();
        form.setHgap(15);
        form.setVgap(12);
        form.setPadding(new Insets(10, 0, 0, 0));

        // STK người nhận
        Label lbl1 = new Label("Số tài khoản người nhận:");
        lbl1.setFont(Font.font("Arial", 13));
        txtSoTaiKhoanNhan = new TextField();
        txtSoTaiKhoanNhan.setPromptText("Nhập số tài khoản");
        txtSoTaiKhoanNhan.setPrefWidth(300);
        MoneyInputUtil.attachDigitsOnly(txtSoTaiKhoanNhan);

        lblTenNguoiNhan = new Label("Người nhận: (chưa nhập STK)");
        lblTenNguoiNhan.setStyle("-fx-text-fill: #7f8c8d; -fx-font-style: italic;");

        txtSoTaiKhoanNhan.textProperty().addListener((obs, oldVal, newVal) -> {
            String stk = newVal != null ? newVal.trim() : "";
            if (stk.isEmpty()) {
                lblTenNguoiNhan.setText("Người nhận: (chưa nhập STK)");
                lblTenNguoiNhan.setStyle("-fx-text-fill: #7f8c8d; -fx-font-style: italic;");
                return;
            }
            try {
                String ten = giaoDichDAO.layTenNguoiDungThuong(stk);
                if (ten == null || ten.isBlank()) {
                    lblTenNguoiNhan.setText("Người nhận: không hợp lệ hoặc không tìm thấy");
                    lblTenNguoiNhan.setStyle("-fx-text-fill: #e67e22;");
                } else {
                    lblTenNguoiNhan.setText("Người nhận: " + ten);
                    lblTenNguoiNhan.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                }
            } catch (Exception ex) {
                lblTenNguoiNhan.setText("Người nhận: lỗi tra cứu");
                lblTenNguoiNhan.setStyle("-fx-text-fill: #e74c3c;");
            }
        });

        // Số tiền
        Label lbl2 = new Label("Số tiền:");
        lbl2.setFont(Font.font("Arial", 13));
        txtSoTien = new TextField();
        txtSoTien.setPromptText("Nhập số tiền (VNĐ)");
        txtSoTien.setPrefWidth(300);
        MoneyInputUtil.attachMoneyFormatter(txtSoTien);

        // Danh mục
        Label lblDanhMuc = new Label("Danh mục:");
        lblDanhMuc.setFont(Font.font("Arial", 13));
        cbDanhMuc = new ComboBox<>();
        
        // Lấy CHỈ danh mục CON loại CHI của user (mặc định + riêng)
        String userSoTaiKhoan = LoginController.currentUser.getSoTaiKhoan();
        List<DanhMuc> danhSachDanhMuc = danhMucDAO.layDanhMucConTheoLoai(userSoTaiKhoan, "chi");
        cbDanhMuc.getItems().addAll(danhSachDanhMuc);
        if (!danhSachDanhMuc.isEmpty()) {
            cbDanhMuc.setValue(danhSachDanhMuc.get(0)); // Chọn mặc định
        }
        cbDanhMuc.setPlaceholder(new Label("Chưa có danh mục con loại Chi"));
        cbDanhMuc.setPrefWidth(300);

        // Nội dung
        Label lbl3 = new Label("Nội dung:");
        lbl3.setFont(Font.font("Arial", 13));
        txtNoiDung = new TextArea();
        txtNoiDung.setPromptText("Nhập nội dung chuyển tiền (tùy chọn)");
        txtNoiDung.setPrefWidth(300);
        txtNoiDung.setPrefHeight(60);

        // Nút chuyển tiền
        btnChuyenTien = new Button("Chuyển tiền");
        btnChuyenTien.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        btnChuyenTien.setPrefWidth(300);
        btnChuyenTien.setPrefHeight(40);
        btnChuyenTien.setOnAction(e -> handleChuyenTien());

        btnGhiTienMat = new Button("Ghi thu/chi tiền mặt");
        btnGhiTienMat.setStyle("-fx-background-color: #16a085; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        btnGhiTienMat.setPrefWidth(300);
        btnGhiTienMat.setPrefHeight(40);
        btnGhiTienMat.setOnAction(e -> handleGhiTienMat());

        form.add(lbl1, 0, 0);
        form.add(txtSoTaiKhoanNhan, 0, 1);
        form.add(lblTenNguoiNhan, 0, 2);
        form.add(lbl2, 0, 3);
        form.add(txtSoTien, 0, 4);
        form.add(lblDanhMuc, 0, 5);
        form.add(cbDanhMuc, 0, 6);
        form.add(lbl3, 0, 7);
        form.add(txtNoiDung, 0, 8);
        form.add(btnChuyenTien, 0, 9);
        form.add(btnGhiTienMat, 0, 10);

        top.getChildren().addAll(header, accountInfo, form);
        return top;
    }

    private VBox createCenterSection() {
        VBox center = new VBox(10);
        center.setPadding(new Insets(20));

        Label lblTitle = new Label("Lịch sử giao dịch");
        lblTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        lblTitle.setStyle("-fx-text-fill: #2c3e50;");

        tableGiaoDich = createTable();

        center.getChildren().addAll(lblTitle, tableGiaoDich);
        VBox.setVgrow(tableGiaoDich, Priority.ALWAYS);

        return center;
    }

    @SuppressWarnings("unchecked")
    private TableView<GiaoDichInfo> createTable() {
        TableView<GiaoDichInfo> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<GiaoDichInfo, String> colNgay = new TableColumn<>("Ngày giờ");
        colNgay.setCellValueFactory(new PropertyValueFactory<>("ngay"));
        colNgay.setPrefWidth(140);

        TableColumn<GiaoDichInfo, String> colLoai = new TableColumn<>("Loại");
        colLoai.setCellValueFactory(new PropertyValueFactory<>("loai"));
        colLoai.setPrefWidth(70);
        colLoai.setCellFactory(column -> new TableCell<GiaoDichInfo, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle((item.equals("Gửi") || item.equals("Chi TM")) ? 
                        "-fx-text-fill: red; -fx-font-weight: bold;" : 
                        "-fx-text-fill: green; -fx-font-weight: bold;");
                }
            }
        });

        TableColumn<GiaoDichInfo, String> colTaiKhoan = new TableColumn<>("Tài khoản");
        colTaiKhoan.setCellValueFactory(new PropertyValueFactory<>("taiKhoan"));
        colTaiKhoan.setPrefWidth(220);

        TableColumn<GiaoDichInfo, String> colSoTien = new TableColumn<>("Số tiền");
        colSoTien.setCellValueFactory(new PropertyValueFactory<>("soTien"));
        colSoTien.setPrefWidth(130);
        colSoTien.setCellFactory(column -> new TableCell<GiaoDichInfo, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold;");
                }
            }
        });

        TableColumn<GiaoDichInfo, String> colNoiDung = new TableColumn<>("Nội dung");
        colNoiDung.setCellValueFactory(new PropertyValueFactory<>("noiDung"));
        colNoiDung.setPrefWidth(200);

        table.getColumns().addAll(colNgay, colLoai, colTaiKhoan, colSoTien, colNoiDung);
        return table;
    }

    private void loadData() {
        try {
            String soTaiKhoan = LoginController.currentUser.getSoTaiKhoan();
            
            // Lấy số dư tài khoản + ví tiền mặt
            BigDecimal soDu = giaoDichDAO.laySoDu(soTaiKhoan);
            BigDecimal soDuTienMat = giaoDichDAO.laySoDuTienMat(soTaiKhoan);
            lblSoDu.setText(df.format(soDu) + " đ");
            lblSoDu.setStyle(soDu.compareTo(BigDecimal.ZERO) >= 0 ? 
                "-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-size: 16px;" : 
                "-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-font-size: 16px;");

            lblSoDuTienMat.setText(df.format(soDuTienMat) + " đ");
            lblSoDuTienMat.setStyle(soDuTienMat.compareTo(BigDecimal.ZERO) >= 0 ?
                "-fx-text-fill: #f39c12; -fx-font-weight: bold; -fx-font-size: 16px;" :
                "-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-font-size: 16px;");
            
            // Load lịch sử giao dịch
            List<GiaoDich> danhSach = giaoDichDAO.layLichSuGiaoDich(soTaiKhoan);
            List<GiaoDichInfo> displayList = new ArrayList<>();
            
            for (GiaoDich gd : danhSach.stream().limit(20).collect(Collectors.toList())) {
                String ngay = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(gd.getNgayGiaoDich());
                String loai;
                String taiKhoan;
                String noiDungGoc = gd.getNoiDung() != null ? gd.getNoiDung() : "";
                boolean laChiTienMat = GiaoDichDAO.isGiaoDichTienMatChi(noiDungGoc);
                boolean laThuTienMat = GiaoDichDAO.isGiaoDichTienMatThu(noiDungGoc);
                
                if (laChiTienMat) {
                    loai = "Chi TM";
                    taiKhoan = "Tiền mặt";
                } else if (laThuTienMat) {
                    loai = "Thu TM";
                    taiKhoan = "Tiền mặt";
                } else if (gd.getSoTaiKhoanGui().equals(soTaiKhoan)) {
                    loai = "Gửi";
                    taiKhoan = gd.getSoTaiKhoanNhan();
                    String tenNguoiNhan = giaoDichDAO.layTenNguoiDung(gd.getSoTaiKhoanNhan());
                    if (tenNguoiNhan != null) {
                        taiKhoan += " (" + tenNguoiNhan + ")";
                    }
                } else {
                    loai = "Nhận";
                    taiKhoan = gd.getSoTaiKhoanGui();
                    String tenNguoiGui = giaoDichDAO.layTenNguoiDung(gd.getSoTaiKhoanGui());
                    if (tenNguoiGui != null) {
                        taiKhoan += " (" + tenNguoiGui + ")";
                    }
                }
                
                String soTien = df.format(gd.getSoTien()) + " đ";
                String noiDung = GiaoDichDAO.boPrefixTienMat(noiDungGoc);
                
                displayList.add(new GiaoDichInfo(ngay, loai, taiKhoan, soTien, noiDung));
            }
            
            ObservableList<GiaoDichInfo> data = FXCollections.observableArrayList(displayList);
            tableGiaoDich.setItems(data);
            
        } catch (Exception e) {
            showError("Lỗi tải dữ liệu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleChuyenTien() {
        try {
            String soTaiKhoanNhan = txtSoTaiKhoanNhan.getText();
            String soTienStr = txtSoTien.getText();
            String noiDung = txtNoiDung.getText() != null ? txtNoiDung.getText().trim() : "";
            String soTaiKhoanGui = LoginController.currentUser.getSoTaiKhoan();

            String validationError = validateInputChuyenTien(
                    soTaiKhoanNhan,
                    soTienStr,
                    soTaiKhoanGui
            );
            if (validationError != null) {
                showError(validationError);
                return;
            }

            BigDecimal soTien = MoneyInputUtil.parseMoney(soTienStr);
            String soTaiKhoanNhanHopLe = soTaiKhoanNhan.trim();
            
            // Kiểm tra tài khoản người nhận có tồn tại
            String tenNguoiNhan = giaoDichDAO.layTenNguoiDungThuong(soTaiKhoanNhanHopLe);
            if (tenNguoiNhan == null) {
                showError("Số tài khoản người nhận không hợp lệ hoặc không tồn tại!");
                return;
            }
            
            // Lấy danh mục đã chọn
            DanhMuc danhMuc = cbDanhMuc.getValue();
            Integer danhMucId = (danhMuc != null) ? danhMuc.getId() : null;
            
            // Kiểm tra vượt ngân sách (nếu có đặt ngân sách cho danh mục này)
            if (!xacNhanNeuVuotNganSach(soTaiKhoanGui, danhMuc, soTien)) return;
            
            // Xác nhận chuyển tiền
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Xác nhận chuyển tiền");
            confirmAlert.setHeaderText("Bạn có chắc chắn muốn chuyển tiền?");
            confirmAlert.setContentText(
                "Người nhận: " + tenNguoiNhan + " (" + soTaiKhoanNhanHopLe + ")\n" +
                "Số tiền: " + df.format(soTien) + " đ\n" +
                "Nội dung: " + (noiDung.isEmpty() ? "(Không có)" : noiDung)
            );
            
            if (confirmAlert.showAndWait().get() == ButtonType.OK) {
                // Thực hiện chuyển tiền
                boolean success = giaoDichDAO.chuyenTien(soTaiKhoanGui, soTaiKhoanNhanHopLe, soTien, noiDung, danhMucId);
                
                if (success) {
                    showSuccess("Chuyển tiền thành công!");
                    clearForm();
                    loadData(); // Reload dữ liệu
                } else {
                    showError("Chuyển tiền thất bại! Vui lòng kiểm tra số dư.");
                }
            }
            
        } catch (Exception e) {
            showError("Lỗi: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleQuayLai() {
        DashboardController dashboardController = new DashboardController(stage);
        stage.setScene(dashboardController.getScene());
        stage.setTitle("Trang Chủ");
        stage.setResizable(false);
        stage.setWidth(1200);
        stage.setHeight(830);
        stage.centerOnScreen();
    }

    private void handleGhiTienMat() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Ghi nhận thu/chi tiền mặt");
        dialog.setHeaderText("Thêm khoản tiền mặt để thống kê (không cần chuyển khoản)");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        ComboBox<String> cbLoai = new ComboBox<>();
        cbLoai.getItems().addAll("Chi tiền mặt", "Thu tiền mặt");
        cbLoai.setValue("Chi tiền mặt");
        cbLoai.setPrefWidth(300);

        ComboBox<DanhMuc> cbDanhMucTienMat = new ComboBox<>();
        cbDanhMucTienMat.setPrefWidth(300);

        TextField txtSoTienTienMat = new TextField();
        txtSoTienTienMat.setPromptText("Nhập số tiền (VD: 3000)");
        txtSoTienTienMat.setPrefWidth(300);
        MoneyInputUtil.attachMoneyFormatter(txtSoTienTienMat);

        TextArea txtNoiDungTienMat = new TextArea();
        txtNoiDungTienMat.setPromptText("Ví dụ: Trả tiền giữ xe / Đi chợ / Bán đồ cũ...");
        txtNoiDungTienMat.setPrefRowCount(3);
        txtNoiDungTienMat.setPrefWidth(300);

        Runnable reloadDanhMuc = () -> {
            String loai = "Chi tiền mặt".equals(cbLoai.getValue()) ? "chi" : "thu";
            String userSoTaiKhoan = LoginController.currentUser.getSoTaiKhoan();
            List<DanhMuc> ds = danhMucDAO.layDanhMucConTheoLoai(userSoTaiKhoan, loai);
            cbDanhMucTienMat.getItems().setAll(ds);
            cbDanhMucTienMat.setValue(ds.isEmpty() ? null : ds.get(0));
        };
        cbLoai.setOnAction(e -> reloadDanhMuc.run());
        reloadDanhMuc.run();

        grid.add(new Label("Loại giao dịch:"), 0, 0);
        grid.add(cbLoai, 1, 0);
        grid.add(new Label("Danh mục:"), 0, 1);
        grid.add(cbDanhMucTienMat, 1, 1);
        grid.add(new Label("Số tiền:"), 0, 2);
        grid.add(txtSoTienTienMat, 1, 2);
        grid.add(new Label("Nội dung:"), 0, 3);
        grid.add(txtNoiDungTienMat, 1, 3);

        dialog.getDialogPane().setContent(grid);
        cbDanhMucTienMat.setPlaceholder(new Label("Chưa có danh mục con phù hợp"));
        ButtonType btnLuu = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnLuu, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(clicked -> {
            if (clicked != btnLuu) return;

            try {
                String loai = "Chi tiền mặt".equals(cbLoai.getValue()) ? "chi" : "thu";

                String soTienStr = txtSoTienTienMat.getText();
                DanhMuc danhMuc = cbDanhMucTienMat.getValue();
                String validationError = validateInputTienMat(loai, soTienStr, danhMuc);
                if (validationError != null) {
                    showError(validationError);
                    return;
                }

                BigDecimal soTien = MoneyInputUtil.parseMoney(soTienStr);
                String soTaiKhoan = LoginController.currentUser.getSoTaiKhoan();
                Integer danhMucId = danhMuc.getId();

                // Cảnh báo vượt ngân sách chỉ cho khoản CHI tiền mặt
                if ("chi".equals(loai) && !xacNhanNeuVuotNganSach(soTaiKhoan, danhMuc, soTien)) return;

                String noiDung = txtNoiDungTienMat.getText() != null ? txtNoiDungTienMat.getText().trim() : "";
                boolean ok = giaoDichDAO.ghiNhanTienMat(soTaiKhoan, soTien, noiDung, loai, danhMucId);
                if (ok) {
                    showSuccess("Đã ghi nhận " + ("chi".equals(loai) ? "khoản chi" : "khoản thu") + " tiền mặt thành công!");
                    loadData();
                } else {
                    showError("Ghi nhận giao dịch tiền mặt thất bại!");
                }
            } catch (Exception ex) {
                showError("Lỗi: " + ex.getMessage());
            }
        });
    }

    // Helper dùng chung: kiểm tra vượt ngân sách và hỏi user có tiếp tục không
    // Trả về true = tiếp tục, false = hủy
    public static String validateInputChuyenTien(String soTaiKhoanNhan,
                                                String soTienStr,
                                                String soTaiKhoanGui) {
        if (soTaiKhoanNhan == null || soTaiKhoanNhan.trim().isEmpty()) {
            return "Vui lòng nhập số tài khoản người nhận!";
        }

        if (!soTaiKhoanNhan.trim().matches("\\d+")) {
            return "Sá»‘ tÃ i khoáº£n ngÆ°á»i nháº­n khÃ´ng há»£p lá»‡!";
        }

        if (soTienStr == null || soTienStr.trim().isEmpty()) {
            return "Vui lòng nhập số tiền!";
        }


        if (!soTienStr.trim().matches("[0-9.]+")) {
            return "Số tiền không hợp lệ!";
        }

        try {
            BigDecimal soTien = MoneyInputUtil.parseMoney(soTienStr);
            if (soTien == null) {
                return "Số tiền không hợp lệ!";
            }
            if (soTien.compareTo(BigDecimal.ZERO) <= 0) {
                return "Số tiền phải lớn hơn 0!";
            }
        } catch (NumberFormatException e) {
            return "Số tiền không hợp lệ!";
        }

        if (soTaiKhoanGui == null || soTaiKhoanGui.trim().isEmpty()) {
            return "KhÃ´ng xÃ¡c Ä‘á»‹nh Ä‘Æ°á»£c tÃ i khoáº£n ngÆ°á»i gá»­i!";
        }

        if (soTaiKhoanGui.equals(soTaiKhoanNhan.trim())) {
            return "Không thể chuyển tiền cho chính mình!";
        }

        return null;
    }

    public static String validateInputTienMat(String soTienStr, DanhMuc danhMuc) {
        return validateInputTienMat(null, soTienStr, danhMuc);
    }

    public static String validateInputTienMat(String loai, String soTienStr, DanhMuc danhMuc) {
        if (loai != null && !"chi".equals(loai) && !"thu".equals(loai)) {
            return "Loại giao dịch không hợp lệ!";
        }

        if (soTienStr == null || soTienStr.trim().isEmpty()) {
            return "Vui lòng nhập số tiền!";
        }

        try {
            BigDecimal soTien = MoneyInputUtil.parseMoney(soTienStr);
            if (soTien == null || soTien.compareTo(BigDecimal.ZERO) <= 0) {
                return "Số tiền không hợp lệ!";
            }
        } catch (NumberFormatException e) {
            return "Số tiền không hợp lệ!";
        }

        if (danhMuc == null) {
            return "Vui lòng chọn danh mục!";
        }

        return null;
    }

    private boolean xacNhanNeuVuotNganSach(String soTaiKhoan, DanhMuc danhMuc, BigDecimal soTien) {
        if (danhMuc == null) return true;

        java.time.LocalDate today = java.time.LocalDate.now();
        int thang = today.getMonthValue();
        int nam = today.getYear();

        // Lấy giới hạn và tổng đã chi — dùng cho cả check lẫn hiển thị dialog (2 query, không gọi lại)
        BigDecimal gioiHan = nganSachDAO.layGioiHanNganSach(soTaiKhoan, danhMuc.getId(), thang, nam);
        if (gioiHan == null) return true; // Không đặt ngân sách → cho phép tiếp tục

        double daChi = nganSachDAO.layTongChiTheoDanhMuc(soTaiKhoan, danhMuc.getId(), thang, nam);
        double tongChiSau = daChi + soTien.doubleValue();

        if (tongChiSau <= gioiHan.doubleValue()) return true; // Không vượt → cho phép tiếp tục

        Alert warning = new Alert(Alert.AlertType.WARNING);
        warning.setTitle("CẢNH BÁO VƯỢT NGÂN SÁCH");
        warning.setHeaderText("Giao dịch này sẽ vượt quá ngân sách đã đặt!");
        warning.setContentText(
            "Danh mục: " + danhMuc.getTenDanhMuc() + " (Tháng " + thang + "/" + nam + ")\n\n" +
            "Giới hạn: " + df.format(gioiHan.doubleValue()) + " đ\n" +
            "Đã chi: " + df.format(daChi) + " đ\n" +
            "Số tiền này: " + df.format(soTien) + " đ\n" +
            "━━━━━━━━━━━━━━━━━━━━━━\n" +
            "Tổng sau giao dịch: " + df.format(tongChiSau) + " đ\n" +
            "Vượt mức: " + df.format(tongChiSau - gioiHan.doubleValue()) + " đ\n\n" +
            "Bạn có chắc chắn muốn tiếp tục?"
        );
        warning.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        return warning.showAndWait().orElse(ButtonType.NO) == ButtonType.YES;
    }

    private void clearForm() {
        txtSoTaiKhoanNhan.clear();
        lblTenNguoiNhan.setText("Người nhận: (chưa nhập STK)");
        lblTenNguoiNhan.setStyle("-fx-text-fill: #7f8c8d; -fx-font-style: italic;");
        txtSoTien.clear();
        txtNoiDung.clear();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thành công");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public Scene getScene() {
        return scene;
    }
}
