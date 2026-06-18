package com.example.controller;

import com.example.dao.DanhMucDAO;
import com.example.dao.GiaoDichDAO;
import com.example.dao.NganSachDAO;
import com.example.dao.NguoiDungDAO;
import com.example.model.DanhMuc;
import com.example.model.GiaoDich;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller cho màn hình Dashboard (JavaFX thuần)
 */
public class DashboardController {

    private static final String LOC_TAT_CA = "Tất cả các ngày";
    private static final String LOC_THANG = "Chọn tháng";
    private static final String LOC_NGAY = "Chọn ngày";

    private Stage stage;
    private Scene scene;
    private Label lblXinChao;
    private Label lblSoTaiKhoan;
    private Label lblSoDu;
    private Label lblSoDuTienMat;
    private TableView<GiaoDichInfo> tableGiaoDich;
    private Button btnGiaoDich;
    private Button btnRefresh;
    private Button btnDangXuat;
    private ComboBox<String> cbLoaiLoc;
    private ComboBox<Integer> cbThangLoc;
    private ComboBox<Integer> cbNamLoc;
    private ComboBox<Integer> cbNgayLoc;
    private ComboBox<Integer> cbThangNgayLoc;
    private ComboBox<Integer> cbNamNgayLoc;
    private HBox boxLocThang;
    private HBox boxLocNgay;
    private boolean dangCapNhatBoLoc = false;
    private GiaoDichDAO giaoDichDAO = new GiaoDichDAO();
    private NguoiDungDAO nguoiDungDAO = new NguoiDungDAO();
    private DanhMucDAO danhMucDAO = new DanhMucDAO();
    private NganSachDAO nganSachDAO = new NganSachDAO();
    private DecimalFormat df = new DecimalFormat("#,###");
    // Lưu danh sách GiaoDich gốc để sử dụng khi đổi danh mục
    private List<GiaoDich> rawGiaoDichList = new ArrayList<>();

    // Inner class để hiển thị giao dịch
    public static class GiaoDichInfo {
        private int maGiaoDich;
        private String ngay;
        private String loai;
        private String taiKhoan;
        private String soTien;
        private String noiDung;
        private String danhMuc;       // Tên danh mục để hiển thị
        // Internal fields (không phải column)
        private Integer danhMucId;    // ID danh mục chi (nếu Gùi)
        private Integer danhMucThuId; // ID danh mục thu (nếu Nhạn)
        private BigDecimal soTienRaw;
        private java.sql.Timestamp ngayGiaoDichRaw;

        public GiaoDichInfo(int maGiaoDich, String ngay, String loai, String taiKhoan,
                            String soTien, String noiDung, String danhMuc,
                            Integer danhMucId, Integer danhMucThuId,
                            BigDecimal soTienRaw, java.sql.Timestamp ngayGiaoDichRaw) {
            this.maGiaoDich = maGiaoDich;
            this.ngay = ngay;
            this.loai = loai;
            this.taiKhoan = taiKhoan;
            this.soTien = soTien;
            this.noiDung = noiDung;
            this.danhMuc = danhMuc;
            this.danhMucId = danhMucId;
            this.danhMucThuId = danhMucThuId;
            this.soTienRaw = soTienRaw;
            this.ngayGiaoDichRaw = ngayGiaoDichRaw;
        }

        // Backward-compat constructor (legacy code)
        public GiaoDichInfo(String ngay, String loai, String taiKhoan, String soTien, String noiDung) {
            this(0, ngay, loai, taiKhoan, soTien, noiDung, "", null, null, BigDecimal.ZERO, null);
        }

        public int getMaGiaoDich() { return maGiaoDich; }
        public String getNgay() { return ngay; }
        public String getLoai() { return loai; }
        public String getTaiKhoan() { return taiKhoan; }
        public String getSoTien() { return soTien; }
        public String getNoiDung() { return noiDung; }
        public String getDanhMuc() { return danhMuc; }
        public void setDanhMuc(String danhMuc) { this.danhMuc = danhMuc; }
        public Integer getDanhMucId() { return danhMucId; }
        public void setDanhMucId(Integer id) { this.danhMucId = id; }
        public Integer getDanhMucThuId() { return danhMucThuId; }
        public void setDanhMucThuId(Integer id) { this.danhMucThuId = id; }
        public BigDecimal getSoTienRaw() { return soTienRaw; }
        public java.sql.Timestamp getNgayGiaoDichRaw() { return ngayGiaoDichRaw; }
    }

    public DashboardController(Stage stage) {
        this.stage = stage;
        createUI();
        loadDashboardData();
    }

    private void createUI() {
        // Root layout
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f5f5;");

        // === TOP: Header ===
        VBox header = createHeader();
        root.setTop(header);

        // === CENTER: Table ===
        VBox center = createCenter();
        root.setCenter(center);

        // Tạo scene
        scene = new Scene(root, 1200, 800);
    }

    private VBox createHeader() {
        VBox header = new VBox(15);
        header.setPadding(new Insets(20));
        header.setStyle("-fx-background-color: #2c3e50;");

        // Hàng 1: Xin chào + Đăng xuất
        HBox row1 = new HBox();
        row1.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(row1, Priority.ALWAYS);

        lblXinChao = new Label("Xin chào, " + LoginController.currentUser.getHoTen());
        lblXinChao.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        lblXinChao.setStyle("-fx-text-fill: white;");

        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);

        Button btnDoiMatKhau = new Button("Đổi mật khẩu");
        btnDoiMatKhau.setStyle("-fx-background-color: #8e44ad; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");
        btnDoiMatKhau.setPrefHeight(35);
        btnDoiMatKhau.setPrefWidth(130);
        btnDoiMatKhau.setOnAction(e -> handleDoiMatKhau());

        btnDangXuat = new Button("Đăng xuất");
        btnDangXuat.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");
        btnDangXuat.setPrefHeight(35);
        btnDangXuat.setPrefWidth(120);
        btnDangXuat.setOnAction(e -> handleDangXuat());

        HBox boxActionTop = new HBox(10);
        boxActionTop.setAlignment(Pos.CENTER_RIGHT);
        boxActionTop.getChildren().addAll(btnDoiMatKhau, btnDangXuat);

        row1.getChildren().addAll(lblXinChao, spacer1, boxActionTop);

        // Hàng 2: Số tài khoản + số dư tài khoản + ví tiền mặt
        HBox row2 = new HBox(30);
        row2.setAlignment(Pos.CENTER_LEFT);

        lblSoTaiKhoan = new Label("STK: " + LoginController.currentUser.getSoTaiKhoan());
        lblSoTaiKhoan.setFont(Font.font("Arial", 16));
        lblSoTaiKhoan.setStyle("-fx-text-fill: #ecf0f1;");

        Label lblSoDuTitle = new Label("Số dư TK: ");
        lblSoDuTitle.setFont(Font.font("Arial", 16));
        lblSoDuTitle.setStyle("-fx-text-fill: #ecf0f1;");

        lblSoDu = new Label("0 đ");
        lblSoDu.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        lblSoDu.setStyle("-fx-text-fill: #2ecc71;");

        Label lblSoDuTienMatTitle = new Label("Ví tiền mặt: ");
        lblSoDuTienMatTitle.setFont(Font.font("Arial", 16));
        lblSoDuTienMatTitle.setStyle("-fx-text-fill: #ecf0f1;");

        lblSoDuTienMat = new Label("0 đ");
        lblSoDuTienMat.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        lblSoDuTienMat.setStyle("-fx-text-fill: #f1c40f;");

        row2.getChildren().addAll(lblSoTaiKhoan, lblSoDuTitle, lblSoDu, lblSoDuTienMatTitle, lblSoDuTienMat);

        // Hàng 3: Các nút chức năng
        HBox row3 = new HBox(15);
        row3.setAlignment(Pos.CENTER_LEFT);

        btnGiaoDich = new Button("Chuyển tiền");
        btnGiaoDich.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        btnGiaoDich.setPrefHeight(40);
        btnGiaoDich.setPrefWidth(150);
        btnGiaoDich.setOnAction(e -> handleGiaoDich());

        Button btnNganSach = new Button("Ngân sách");
        btnNganSach.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        btnNganSach.setPrefHeight(40);
        btnNganSach.setPrefWidth(150);
        btnNganSach.setOnAction(e -> handleNganSach());
        
        Button btnThongKe = new Button("Thống kê");
        btnThongKe.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        btnThongKe.setPrefHeight(40);
        btnThongKe.setPrefWidth(150);
        btnThongKe.setOnAction(e -> handleThongKe());
        
        Button btnDanhMuc = new Button("Danh mục");
        btnDanhMuc.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        btnDanhMuc.setPrefHeight(40);
        btnDanhMuc.setPrefWidth(150);
        btnDanhMuc.setOnAction(e -> handleDanhMuc());

        row3.getChildren().addAll(btnGiaoDich, btnNganSach, btnThongKe, btnDanhMuc);

        header.getChildren().addAll(row1, row2, row3);
        return header;
    }

    private VBox createCenter() {
        VBox center = new VBox(10);
        center.setPadding(new Insets(20));

        // Tiêu đề
        Label lblTitle = new Label("Lịch sử giao dịch");
        lblTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        lblTitle.setStyle("-fx-text-fill: #2c3e50;");

        // Nút đổi danh mục
        Button btnDoiDanhMuc = new Button("Đổi danh mục");
        btnDoiDanhMuc.setStyle("-fx-background-color: #16a085; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");
        btnDoiDanhMuc.setPrefHeight(35);
        btnDoiDanhMuc.setOnAction(e -> handleDoiDanhMuc());

        btnRefresh = new Button("Làm mới");
        btnRefresh.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");
        btnRefresh.setPrefHeight(35);
        btnRefresh.setPrefWidth(120);
        btnRefresh.setOnAction(e -> handleRefresh());

        HBox titleRow = new HBox(15);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        titleRow.getChildren().addAll(lblTitle, sp, btnRefresh, btnDoiDanhMuc);

        // Bộ lọc lịch sử giao dịch
        HBox filterRow = createFilterRow();

        // TableView
        tableGiaoDich = createTable();

        center.getChildren().addAll(titleRow, filterRow, tableGiaoDich);
        VBox.setVgrow(tableGiaoDich, Priority.ALWAYS);

        return center;
    }

    private HBox createFilterRow() {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        Label lblLoc = new Label("Lọc giao dịch:");
        lblLoc.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        cbLoaiLoc = new ComboBox<>();
        cbLoaiLoc.getItems().addAll(LOC_TAT_CA, LOC_THANG, LOC_NGAY);
        cbLoaiLoc.setValue(LOC_TAT_CA);
        cbLoaiLoc.setPrefWidth(170);

        cbThangLoc = new ComboBox<>();
        cbThangLoc.setPrefWidth(90);

        cbNamLoc = new ComboBox<>();
        int namHienTai = LocalDate.now().getYear();
        for (int y = namHienTai - 4; y <= namHienTai; y++) cbNamLoc.getItems().add(y);
        cbNamLoc.setPrefWidth(100);

        cbNamLoc.setValue(namHienTai);
        capNhatDanhSachThangLoc();
        cbThangLoc.setValue(LocalDate.now().getMonthValue());

        cbNgayLoc = new ComboBox<>();
        cbNgayLoc.setPrefWidth(90);

        cbThangNgayLoc = new ComboBox<>();
        cbThangNgayLoc.setPrefWidth(90);

        cbNamNgayLoc = new ComboBox<>();
        for (int y = namHienTai - 4; y <= namHienTai; y++) cbNamNgayLoc.getItems().add(y);
        cbNamNgayLoc.setPrefWidth(100);

        cbNamNgayLoc.setValue(namHienTai);
        capNhatDanhSachThangNgayLoc();
        cbThangNgayLoc.setValue(LocalDate.now().getMonthValue());
        capNhatDanhSachNgayLoc();
        cbNgayLoc.setValue(LocalDate.now().getDayOfMonth());

        boxLocThang = new HBox(8, new Label("Tháng"), cbThangLoc, new Label("Năm"), cbNamLoc);
        boxLocThang.setAlignment(Pos.CENTER_LEFT);

        boxLocNgay = new HBox(8,
            new Label("Ngày"), cbNgayLoc,
            new Label("Tháng"), cbThangNgayLoc,
            new Label("Năm"), cbNamNgayLoc);
        boxLocNgay.setAlignment(Pos.CENTER_LEFT);

        cbLoaiLoc.setOnAction(e -> {
            if (dangCapNhatBoLoc) return;
            capNhatHienThiBoLoc();
            loadDashboardData();
        });
        cbThangLoc.setOnAction(e -> {
            if (dangCapNhatBoLoc) return;
            if (LOC_THANG.equals(cbLoaiLoc.getValue())) loadDashboardData();
        });
        cbNamLoc.setOnAction(e -> {
            if (dangCapNhatBoLoc) return;
            capNhatDanhSachThangLoc();
            if (LOC_THANG.equals(cbLoaiLoc.getValue())) loadDashboardData();
        });

        cbNgayLoc.setOnAction(e -> {
            if (dangCapNhatBoLoc) return;
            if (LOC_NGAY.equals(cbLoaiLoc.getValue())) loadDashboardData();
        });

        cbThangNgayLoc.setOnAction(e -> {
            if (dangCapNhatBoLoc) return;
            capNhatDanhSachNgayLoc();
            if (LOC_NGAY.equals(cbLoaiLoc.getValue())) loadDashboardData();
        });

        cbNamNgayLoc.setOnAction(e -> {
            if (dangCapNhatBoLoc) return;
            capNhatDanhSachThangNgayLoc();
            capNhatDanhSachNgayLoc();
            if (LOC_NGAY.equals(cbLoaiLoc.getValue())) loadDashboardData();
        });

        row.getChildren().addAll(lblLoc, cbLoaiLoc, boxLocThang, boxLocNgay);
        capNhatHienThiBoLoc();
        return row;
    }

    private void capNhatHienThiBoLoc() {
        if (cbLoaiLoc == null || boxLocThang == null || boxLocNgay == null) return;
        boolean locThang = LOC_THANG.equals(cbLoaiLoc.getValue());
        boolean locNgay = LOC_NGAY.equals(cbLoaiLoc.getValue());

        boxLocThang.setVisible(locThang);
        boxLocThang.setManaged(locThang);

        boxLocNgay.setVisible(locNgay);
        boxLocNgay.setManaged(locNgay);
    }

    private void capNhatDanhSachThangLoc() {
        if (cbThangLoc == null || cbNamLoc == null || cbNamLoc.getValue() == null) return;

        boolean oldFlag = dangCapNhatBoLoc;
        dangCapNhatBoLoc = true;
        try {
            int nam = cbNamLoc.getValue();
            int namHienTai = LocalDate.now().getYear();
            int thangMax = (nam == namHienTai) ? LocalDate.now().getMonthValue() : 12;

            Integer thangCu = cbThangLoc.getValue();
            cbThangLoc.getItems().clear();
            for (int i = 1; i <= thangMax; i++) cbThangLoc.getItems().add(i);

            int thangMoi = (thangCu != null) ? Math.min(thangCu, thangMax) : thangMax;
            cbThangLoc.getSelectionModel().select(Integer.valueOf(thangMoi));
            if (cbThangLoc.getValue() == null && !cbThangLoc.getItems().isEmpty()) {
                cbThangLoc.getSelectionModel().selectLast();
            }
        } finally {
            dangCapNhatBoLoc = oldFlag;
        }
    }

    private void capNhatDanhSachThangNgayLoc() {
        if (cbThangNgayLoc == null || cbNamNgayLoc == null || cbNamNgayLoc.getValue() == null) return;

        boolean oldFlag = dangCapNhatBoLoc;
        dangCapNhatBoLoc = true;
        try {
            int nam = cbNamNgayLoc.getValue();
            int namHienTai = LocalDate.now().getYear();
            int thangMax = (nam == namHienTai) ? LocalDate.now().getMonthValue() : 12;

            Integer thangCu = cbThangNgayLoc.getValue();
            cbThangNgayLoc.getItems().clear();
            for (int i = 1; i <= thangMax; i++) cbThangNgayLoc.getItems().add(i);

            int thangMoi = (thangCu != null) ? Math.min(thangCu, thangMax) : thangMax;
            cbThangNgayLoc.getSelectionModel().select(Integer.valueOf(thangMoi));
            if (cbThangNgayLoc.getValue() == null && !cbThangNgayLoc.getItems().isEmpty()) {
                cbThangNgayLoc.getSelectionModel().selectLast();
            }
        } finally {
            dangCapNhatBoLoc = oldFlag;
        }
    }

    private void capNhatDanhSachNgayLoc() {
        if (cbNgayLoc == null || cbThangNgayLoc == null || cbNamNgayLoc == null) return;
        Integer thang = cbThangNgayLoc.getValue();
        Integer nam = cbNamNgayLoc.getValue();
        if (thang == null || nam == null) return;

        boolean oldFlag = dangCapNhatBoLoc;
        dangCapNhatBoLoc = true;
        try {
            Integer ngayCu = cbNgayLoc.getValue();
            int maxNgay = LocalDate.of(nam, thang, 1).lengthOfMonth();
            LocalDate homNay = LocalDate.now();
            if (nam == homNay.getYear() && thang == homNay.getMonthValue()) {
                maxNgay = Math.min(maxNgay, homNay.getDayOfMonth());
            }

            cbNgayLoc.getItems().clear();
            for (int d = 1; d <= maxNgay; d++) cbNgayLoc.getItems().add(d);

            int ngayMoi = (ngayCu != null) ? Math.min(ngayCu, maxNgay) : 1;
            cbNgayLoc.getSelectionModel().select(Integer.valueOf(ngayMoi));
            if (cbNgayLoc.getValue() == null && !cbNgayLoc.getItems().isEmpty()) {
                cbNgayLoc.getSelectionModel().selectLast();
            }
        } finally {
            dangCapNhatBoLoc = oldFlag;
        }
    }

    @SuppressWarnings("unchecked")
    private TableView<GiaoDichInfo> createTable() {
        TableView<GiaoDichInfo> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Các cột
        TableColumn<GiaoDichInfo, String> colNgay = new TableColumn<>("Ngày giờ");
        colNgay.setCellValueFactory(new PropertyValueFactory<>("ngay"));
        colNgay.setPrefWidth(150);

        TableColumn<GiaoDichInfo, String> colLoai = new TableColumn<>("Loại");
        colLoai.setCellValueFactory(new PropertyValueFactory<>("loai"));
        colLoai.setPrefWidth(80);
        // Format màu cho loại giao dịch
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
        colTaiKhoan.setPrefWidth(250);

        TableColumn<GiaoDichInfo, String> colSoTien = new TableColumn<>("Số tiền");
        colSoTien.setCellValueFactory(new PropertyValueFactory<>("soTien"));
        colSoTien.setPrefWidth(150);
        // Format căn phải cho số tiền
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

        TableColumn<GiaoDichInfo, String> colDanhMuc = new TableColumn<>("Danh mục");
        colDanhMuc.setCellValueFactory(new PropertyValueFactory<>("danhMuc"));
        colDanhMuc.setPrefWidth(160);
        colDanhMuc.setCellFactory(col -> new TableCell<GiaoDichInfo, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                boolean blank = item.isBlank();
                setText(blank ? "— Chưa phân loại" : item);
                if (blank) setStyle("-fx-text-fill: #bdc3c7; -fx-font-style: italic;");
                else {
                    // Thu thì xanh lá, Chi thì cam
                    GiaoDichInfo row = getTableView().getItems().get(getIndex());
                    if ("Nhận".equals(row.getLoai()) || "Thu TM".equals(row.getLoai()))
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    else
                        setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold;");
                }
            }
        });

        table.getColumns().addAll(colNgay, colLoai, colTaiKhoan, colSoTien, colNoiDung, colDanhMuc);
        return table;
    }

    private void loadDashboardData() {
        try {
            String soTaiKhoan = LoginController.currentUser.getSoTaiKhoan();
            
            // Lấy số dư tài khoản + ví tiền mặt
            BigDecimal soDu = giaoDichDAO.laySoDu(soTaiKhoan);
            BigDecimal soDuTienMat = giaoDichDAO.laySoDuTienMat(soTaiKhoan);

            lblSoDu.setText(df.format(soDu) + " đ");
            lblSoDu.setStyle(soDu.compareTo(BigDecimal.ZERO) >= 0 ?
                "-fx-text-fill: #2ecc71; -fx-font-weight: bold; -fx-font-size: 20px;" : 
                "-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-font-size: 20px;");

            lblSoDuTienMat.setText(df.format(soDuTienMat) + " đ");
            lblSoDuTienMat.setStyle(soDuTienMat.compareTo(BigDecimal.ZERO) >= 0 ?
                "-fx-text-fill: #f1c40f; -fx-font-weight: bold; -fx-font-size: 20px;" :
                "-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-font-size: 20px;");
            
            // Load lịch sử giao dịch theo bộ lọc
            List<GiaoDich> danhSach;
            String loaiLoc = cbLoaiLoc != null ? cbLoaiLoc.getValue() : LOC_TAT_CA;
            if (LOC_THANG.equals(loaiLoc)) {
                int thang = cbThangLoc != null && cbThangLoc.getValue() != null
                        ? cbThangLoc.getValue() : LocalDate.now().getMonthValue();
                int nam = cbNamLoc != null && cbNamLoc.getValue() != null
                        ? cbNamLoc.getValue() : LocalDate.now().getYear();
                danhSach = giaoDichDAO.layLichSuGiaoDichTheoThang(soTaiKhoan, thang, nam);
            } else if (LOC_NGAY.equals(loaiLoc)) {
                int thang = cbThangNgayLoc != null && cbThangNgayLoc.getValue() != null
                    ? cbThangNgayLoc.getValue() : LocalDate.now().getMonthValue();
                int nam = cbNamNgayLoc != null && cbNamNgayLoc.getValue() != null
                    ? cbNamNgayLoc.getValue() : LocalDate.now().getYear();

                LocalDate homNay = LocalDate.now();
                int maxNgay = LocalDate.of(nam, thang, 1).lengthOfMonth();
                if (nam == homNay.getYear() && thang == homNay.getMonthValue()) {
                    maxNgay = Math.min(maxNgay, homNay.getDayOfMonth());
                }

                Integer ngayDaChon = cbNgayLoc != null ? cbNgayLoc.getValue() : null;
                int ngay = (ngayDaChon != null)
                        ? ngayDaChon
                        : Math.min(homNay.getDayOfMonth(), maxNgay);
                if (ngay < 1) ngay = 1;
                if (ngay > maxNgay) ngay = maxNgay;

                // Đồng bộ lại UI nếu ngày đang chọn không hợp lệ sau khi đổi tháng/năm.
                if (cbNgayLoc != null && (ngayDaChon == null || !ngayDaChon.equals(ngay))) {
                    cbNgayLoc.setValue(ngay);
                }

                LocalDate ngayLoc = LocalDate.of(nam, thang, ngay);
                danhSach = giaoDichDAO.layLichSuGiaoDichTheoNgay(soTaiKhoan, ngayLoc);
            } else {
                danhSach = giaoDichDAO.layLichSuGiaoDich(soTaiKhoan);
            }

            rawGiaoDichList = new ArrayList<>(danhSach);
            List<GiaoDichInfo> displayList = new ArrayList<>();
            
            for (GiaoDich gd : danhSach) {
                String ngay = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(gd.getNgayGiaoDich());
                String loai;
                String taiKhoan;
                String danhMucHienThi;
                String noiDungGoc = gd.getNoiDung() != null ? gd.getNoiDung() : "";
                boolean laChiTienMat = GiaoDichDAO.isGiaoDichTienMatChi(noiDungGoc);
                boolean laThuTienMat = GiaoDichDAO.isGiaoDichTienMatThu(noiDungGoc);

                if (laChiTienMat) {
                    loai = "Chi TM";
                    taiKhoan = "Tiền mặt";
                    danhMucHienThi = gd.getTenDanhMucChi() != null ? gd.getTenDanhMucChi() : "";
                } else if (laThuTienMat) {
                    loai = "Thu TM";
                    taiKhoan = "Tiền mặt";
                    danhMucHienThi = gd.getTenDanhMucThu() != null ? gd.getTenDanhMucThu() : "";
                } else if (gd.getSoTaiKhoanGui().equals(soTaiKhoan)) {
                    loai = "Gửi";
                    taiKhoan = gd.getSoTaiKhoanNhan();
                    String tenNguoiNhan = giaoDichDAO.layTenNguoiDung(gd.getSoTaiKhoanNhan());
                    if (tenNguoiNhan != null) taiKhoan += " (" + tenNguoiNhan + ")";
                    danhMucHienThi = gd.getTenDanhMucChi() != null ? gd.getTenDanhMucChi() : "";
                } else {
                    loai = "Nhận";
                    taiKhoan = gd.getSoTaiKhoanGui();
                    String tenNguoiGui = giaoDichDAO.layTenNguoiDung(gd.getSoTaiKhoanGui());
                    if (tenNguoiGui != null) taiKhoan += " (" + tenNguoiGui + ")";
                    danhMucHienThi = gd.getTenDanhMucThu() != null ? gd.getTenDanhMucThu() : "";
                }
                
                String soTien = df.format(gd.getSoTien()) + " đ";
                String noiDung = GiaoDichDAO.boPrefixTienMat(noiDungGoc);
                
                displayList.add(new GiaoDichInfo(
                    gd.getMaGiaoDich(), ngay, loai, taiKhoan, soTien, noiDung,
                    danhMucHienThi, gd.getDanhMucId(), gd.getDanhMucThuId(),
                    gd.getSoTien(), gd.getNgayGiaoDich()));
            }
            
            ObservableList<GiaoDichInfo> data = FXCollections.observableArrayList(displayList);
            tableGiaoDich.setItems(data);
            
        } catch (Exception e) {
            showError("Lỗi tải dữ liệu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleGiaoDich() {
        TransactionController transactionController = new TransactionController(stage);
        stage.setScene(transactionController.getScene());
        stage.setTitle("Chuyển Tiền");
        stage.setResizable(false);
        stage.setWidth(1200);
        stage.setHeight(830);
        stage.centerOnScreen();
    }
    
    private void handleNganSach() {
        try {
            String soTaiKhoan = LoginController.currentUser.getSoTaiKhoan();
            BudgetController budgetController = new BudgetController(stage, soTaiKhoan);
            
            stage.setScene(budgetController.getScene());
            stage.setTitle("Quản Lý Ngân Sách");
            stage.setResizable(false);
            stage.setWidth(1200);
            stage.setHeight(830);
            stage.centerOnScreen();
        } catch (Exception e) {
            showError("Lỗi khi mở Ngân sách: " + e.getMessage());
        }
    }
    
    private void handleThongKe() {
        try {
            String soTaiKhoan = LoginController.currentUser.getSoTaiKhoan();
            StatisticsController statisticsController = new StatisticsController(stage, soTaiKhoan);
            
            stage.setScene(statisticsController.getScene());
            stage.setTitle("Thống Kê Chi Tiêu");
            stage.setResizable(false);
            stage.setWidth(1200);
            stage.setHeight(830);
            stage.centerOnScreen();
        } catch (Exception e) {
            showError("Lỗi khi mở Thống kê: " + e.getMessage());
        }
    }
    
    private void handleDanhMuc() {
        try {
            String soTaiKhoan = LoginController.currentUser.getSoTaiKhoan();
            CategoryController categoryController = new CategoryController(stage, soTaiKhoan);
            stage.setScene(categoryController.getScene());
            stage.setTitle("Quản Lý Danh Mục");
            stage.setResizable(false);
            stage.setWidth(1200);
            stage.setHeight(830);
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi khi mở Danh mục: " + e.getMessage());
        }
    }

    private void handleDangXuat() {
        LoginController.currentUser = null;
        LoginController loginController = new LoginController(stage);
        stage.setScene(loginController.getScene());
        stage.setTitle("Đăng nhập");
        stage.setResizable(false);
        stage.setWidth(520);
        stage.setHeight(590);
        stage.centerOnScreen();
    }

    private void handleRefresh() {
        loadDashboardData();
    }

    /** Đổi danh mục của giao dịch đang chọn, có cảnh báo ngân sách khi đổi loại chi */
    private void handleDoiDanhMuc() {
        GiaoDichInfo selected = tableGiaoDich.getSelectionModel().getSelectedItem();
        String validationError = validateInputDoiDanhMuc(selected);
        if (validationError != null) {
            new Alert(Alert.AlertType.WARNING, validationError, ButtonType.OK).showAndWait();
            return;
        }

        boolean isChi = "Gửi".equals(selected.getLoai()) || "Chi TM".equals(selected.getLoai());
        String soTaiKhoan = LoginController.currentUser.getSoTaiKhoan();
        String loaiDM = isChi ? "chi" : "thu";
        List<DanhMuc> dsDanhMuc = danhMucDAO.layDanhMucConTheoLoai(soTaiKhoan, loaiDM);

        if (dsDanhMuc.isEmpty()) {
            new Alert(Alert.AlertType.WARNING,
                "Chưa có danh mục con loại " + (isChi ? "Chi" : "Thu") + ".\n" +
                "Vui lòng tạo danh mục con trước khi đổi danh mục giao dịch.",
                ButtonType.OK).showAndWait();
            return;
        }

        Dialog<DanhMuc> dialog = new Dialog<>();
        dialog.setTitle("Đổi danh mục");
        dialog.setHeaderText((isChi ? "Danh mục Chi" : "Danh mục Thu") +
                " — Giao dịch ngày " + selected.getNgay());

        ComboBox<DanhMuc> cbDM = new ComboBox<>();
        cbDM.getItems().addAll(dsDanhMuc);
        cbDM.setPrefWidth(300);

        // Chọn danh mục hiện tại
        Integer curId = isChi ? selected.getDanhMucId() : selected.getDanhMucThuId();
        if (curId != null) {
            dsDanhMuc.stream().filter(d -> d.getId() == curId).findFirst()
                    .ifPresent(cbDM::setValue);
        }

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.getChildren().addAll(new Label("Chọn danh mục mới:"), cbDM);
        dialog.getDialogPane().setContent(content);
        ButtonType btnOK = new ButtonType("✔ Xác nhận", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnOK, ButtonType.CANCEL);
        dialog.setResultConverter(btn -> btn == btnOK ? cbDM.getValue() : null);

        dialog.showAndWait().ifPresent(newDM -> {
            if (newDM == null) return;

            // === Kiểm tra ngân sách (chỉ áp dụng cho giao dịch gửi = chi tiêu) ===
            if (isChi) {
                try {
                    // Lấy tháng/năm từ giao dịch — KHÔNG dùng tháng hiện tại
                    java.sql.Timestamp ts = selected.getNgayGiaoDichRaw();
                    LocalDate ngayGD = ts != null ? ts.toLocalDateTime().toLocalDate() : LocalDate.now();
                    int thang = ngayGD.getMonthValue();
                    int nam   = ngayGD.getYear();

                    if (!xacNhanNeuVuotNganSach(soTaiKhoan, newDM, selected.getSoTienRaw(), thang, nam)) return;

                    giaoDichDAO.capNhatDanhMucChi(selected.getMaGiaoDich(), newDM.getId());
                } catch (Exception ex) {
                    showError("Lỗi cập nhật: " + ex.getMessage());
                    return;
                }
            } else {
                try {
                    giaoDichDAO.capNhatDanhMucThu(selected.getMaGiaoDich(), newDM.getId());
                } catch (Exception ex) {
                    showError("Lỗi cập nhật: " + ex.getMessage());
                    return;
                }
            }

            Alert ok = new Alert(Alert.AlertType.INFORMATION,
                    "Đã đổi danh mục thành công: " + newDM.getTenDanhMuc(), ButtonType.OK);
            ok.setHeaderText(null);
            ok.showAndWait();
            loadDashboardData();
        });
    }

    private void handleDoiMatKhau() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Đổi mật khẩu");
        dialog.setHeaderText("Đổi mật khẩu tài khoản: " + LoginController.currentUser.getTenDangNhap());

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        PasswordField txtCu = new PasswordField();
        txtCu.setPromptText("Nhập mật khẩu hiện tại");
        txtCu.setPrefWidth(280);

        PasswordField txtMoi = new PasswordField();
        txtMoi.setPromptText("Tối thiểu 6 ký tự");
        txtMoi.setPrefWidth(280);

        PasswordField txtXacNhan = new PasswordField();
        txtXacNhan.setPromptText("Nhập lại mật khẩu mới");
        txtXacNhan.setPrefWidth(280);

        Label lblKetQua = new Label("");
        lblKetQua.setWrapText(true);

        grid.add(new Label("Mật khẩu hiện tại:"), 0, 0);
        grid.add(txtCu, 1, 0);
        grid.add(new Label("Mật khẩu mới:"), 0, 1);
        grid.add(txtMoi, 1, 1);
        grid.add(new Label("Xác nhận mật khẩu:"), 0, 2);
        grid.add(txtXacNhan, 1, 2);
        grid.add(lblKetQua, 1, 3);

        dialog.getDialogPane().setContent(grid);

        ButtonType btnLuu = new ButtonType("Lưu mật khẩu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnLuu, ButtonType.CANCEL);

        // Validate khi nhấn Lưu (dùng event filter để chặn dialog đóng khi lỗi)
        javafx.scene.Node btnLuuNode = dialog.getDialogPane().lookupButton(btnLuu);
        btnLuuNode.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            String cu = txtCu.getText();
            String moi = txtMoi.getText();
            String xn = txtXacNhan.getText();

            String validationError = validateInputDoiMatKhau(cu, moi, xn);
            if (validationError != null) {
                lblKetQua.setText(validationError);
                lblKetQua.setStyle("-fx-text-fill: red;");
                ev.consume();
                return;
            }

            try {
                if (nguoiDungDAO.laMatKhauTrungHienTai(LoginController.currentUser.getMaNguoiDung(), moi)) {
                    lblKetQua.setText("Mật khẩu mới trùng với mật khẩu hiện tại!");
                    lblKetQua.setStyle("-fx-text-fill: red;");
                    ev.consume();
                    return;
                }

                boolean ok = nguoiDungDAO.doiMatKhau(
                        LoginController.currentUser.getMaNguoiDung(), cu, moi);
                if (!ok) {
                    lblKetQua.setText("Mật khẩu hiện tại không đúng!");
                    lblKetQua.setStyle("-fx-text-fill: red;");
                    ev.consume();
                }
            } catch (Exception ex) {
                lblKetQua.setText("Lỗi: " + ex.getMessage());
                lblKetQua.setStyle("-fx-text-fill: red;");
                ev.consume();
            }
        });

        dialog.setResultConverter(btn -> null);

        boolean[] changed = {false};
        btnLuuNode.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            // nếu đạt đây thì đã pass validation (event không bị consumed)
            changed[0] = !ev.isConsumed();
        });

        dialog.showAndWait();
        if (changed[0]) {
            Alert ok = new Alert(Alert.AlertType.INFORMATION);
            ok.setTitle("Đổi mật khẩu");
            ok.setHeaderText(null);
            ok.setContentText("✅ Đổi mật khẩu thành công!");
            ok.showAndWait();
        }
    }

    // Helper kiểm tra vượt ngân sách — nhận thang/nam từ ngoài vì có thể là tháng của giao dịch cũ
    // Trả về true = tiếp tục, false = hủy
    public static String validateInputDoiMatKhau(String matKhauCu,
                                                 String matKhauMoi,
                                                 String xacNhanMatKhau) {
        if (matKhauCu == null || matKhauCu.isEmpty()
                || matKhauMoi == null || matKhauMoi.isEmpty()
                || xacNhanMatKhau == null || xacNhanMatKhau.isEmpty()) {
            return "Vui lòng điền đầy đủ thông tin!";
        }

        if (matKhauMoi.length() < 6) {
            return "Mật khẩu mới phải ít nhất 6 ký tự!";
        }

        if (!matKhauMoi.equals(xacNhanMatKhau)) {
            return "Mật khẩu xác nhận không khớp!";
        }

        return null;
    }

    public static String validateInputDoiDanhMuc(GiaoDichInfo selected) {
        if (selected == null) {
            return "Vui lòng chọn giao dịch cần đổi danh mục!";
        }

        return null;
    }

    private boolean xacNhanNeuVuotNganSach(String soTaiKhoan, DanhMuc danhMuc, BigDecimal soTien, int thang, int nam) {
        BigDecimal gioiHan = nganSachDAO.layGioiHanNganSach(soTaiKhoan, danhMuc.getId(), thang, nam);
        if (gioiHan == null) return true; // Không đặt ngân sách → cho phép tiếp tục

        double daChi = nganSachDAO.layTongChiTheoDanhMuc(soTaiKhoan, danhMuc.getId(), thang, nam);
        BigDecimal tongSau = BigDecimal.valueOf(daChi).add(soTien);

        if (tongSau.compareTo(gioiHan) <= 0) return true; // Không vượt → cho phép tiếp tục

        String msg = String.format(
                "Cảnh báo ngân sách!%n" +
                "Danh mục: %s%n" +
                "Hạn mức tháng %d/%d: %s đ%n" +
                "Đã chi: %s đ%n" +
                "Giao dịch này: +%s đ%n" +
                "→ Tổng sau khi chuyển: %s đ (vượt %s đ)%n%n" +
                "Bạn vẫn muốn chuyển sang danh mục này?",
                danhMuc.getTenDanhMuc(), thang, nam,
                df.format(gioiHan),
                df.format(daChi),
                df.format(soTien),
                df.format(tongSau),
                df.format(tongSau.subtract(gioiHan)));
        Alert warn = new Alert(Alert.AlertType.CONFIRMATION, msg, ButtonType.YES, ButtonType.NO);
        warn.setTitle("Vượt hạn mức ngân sách");
        warn.setHeaderText(null);
        return warn.showAndWait().orElse(ButtonType.NO) == ButtonType.YES;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public Scene getScene() {
        return scene;
    }
}
