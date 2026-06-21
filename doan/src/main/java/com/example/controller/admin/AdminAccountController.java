package com.example.controller.admin;

import com.example.controller.LoginController;
import com.example.dao.NguoiDungDAO;
import com.example.model.NguoiDung;
import com.example.util.MoneyInputUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller panel Quản lý tài khoản cho Admin.
 */
public class AdminAccountController {

    private final NguoiDungDAO nguoiDungDAO = new NguoiDungDAO();
    private final DecimalFormat df = new DecimalFormat("#,###");

    private VBox panelTaiKhoan;
    private TableView<NguoiDung> tableTaiKhoan;
    private TextField txtTimStkTaiKhoan;

    public AdminAccountController() {
        buildPanelTaiKhoan();
    }

    public VBox getPanel() {
        return panelTaiKhoan;
    }

    public void refresh() {
        loadDanhSachTaiKhoan();
    }

    private void buildPanelTaiKhoan() {
        panelTaiKhoan = new VBox(15);
        panelTaiKhoan.setPadding(new Insets(5));

        Label title = new Label("Quản lý tài khoản người dùng");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);

        Label lblTim = new Label("Tìm theo STK:");
        lblTim.setStyle("-fx-font-weight: bold;");

        txtTimStkTaiKhoan = new TextField();
        txtTimStkTaiKhoan.setPromptText("Nhập số tài khoản user...");
        txtTimStkTaiKhoan.setPrefWidth(260);
        MoneyInputUtil.attachDigitsOnly(txtTimStkTaiKhoan);

        Button btnXoaLoc = new Button("Xóa lọc");
        btnXoaLoc.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white; -fx-font-weight: bold;");
        btnXoaLoc.setOnAction(e -> {
            txtTimStkTaiKhoan.clear();
            loadDanhSachTaiKhoan();
        });

        txtTimStkTaiKhoan.textProperty().addListener((obs, oldV, newV) -> loadDanhSachTaiKhoan(newV));
        searchBox.getChildren().addAll(lblTim, txtTimStkTaiKhoan, btnXoaLoc);

        tableTaiKhoan = new TableView<>();
        VBox.setVgrow(tableTaiKhoan, Priority.ALWAYS);

        TableColumn<NguoiDung, String> colSTK = new TableColumn<>("Số TK");
        colSTK.setCellValueFactory(new PropertyValueFactory<>("soTaiKhoan"));
        colSTK.setPrefWidth(110);

        TableColumn<NguoiDung, String> colTenDN = new TableColumn<>("Tên đăng nhập");
        colTenDN.setCellValueFactory(new PropertyValueFactory<>("tenDangNhap"));
        colTenDN.setPrefWidth(150);

        TableColumn<NguoiDung, String> colHoTen = new TableColumn<>("Họ tên");
        colHoTen.setCellValueFactory(new PropertyValueFactory<>("hoTen"));
        colHoTen.setPrefWidth(180);

        TableColumn<NguoiDung, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colEmail.setPrefWidth(200);

        TableColumn<NguoiDung, String> colVaiTro = new TableColumn<>("Vai trò");
        colVaiTro.setCellValueFactory(new PropertyValueFactory<>("vaiTro"));
        colVaiTro.setPrefWidth(100);
        colVaiTro.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    return;
                }
                if ("quan_ly".equals(item)) {
                    setText("Quản lý");
                } else if ("nguoi_dung".equals(item)) {
                    setText("Người dùng");
                } else {
                    setText(item);
                }
            }
        });

        TableColumn<NguoiDung, String> colTrangThai = new TableColumn<>("Trạng thái");
        colTrangThai.setPrefWidth(130);
        colTrangThai.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setText(null);
                    setStyle("");
                } else {
                    NguoiDung nd = getTableView().getItems().get(getIndex());
                    if ("hoat_dong".equals(nd.getTrangThai())) {
                        setText("Hoạt động");
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    } else {
                        setText("Bị khóa");
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    }
                }
            }
        });

        tableTaiKhoan.getColumns().addAll(colSTK, colTenDN, colHoTen, colEmail, colVaiTro, colTrangThai);

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        Button btnRefresh = new Button("Làm mới");
        btnRefresh.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        btnRefresh.setOnAction(e -> loadDanhSachTaiKhoan());

        Button btnKhoa = new Button("Khóa tài khoản");
        btnKhoa.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-font-weight: bold;");
        btnKhoa.setOnAction(e -> handleKhoaTaiKhoan());

        Button btnMoKhoa = new Button("Mở khóa");
        btnMoKhoa.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
        btnMoKhoa.setOnAction(e -> handleMoKhoaTaiKhoan());

        Button btnNapTien = new Button("Nạp tiền cho user");
        btnNapTien.setStyle("-fx-background-color: #16a085; -fx-text-fill: white; -fx-font-weight: bold;");
        btnNapTien.setOnAction(e -> handleNapTienChoUser());

        Button btnXemThongTin = new Button("Xem thông tin user");
        btnXemThongTin.setStyle("-fx-background-color: #8e44ad; -fx-text-fill: white; -fx-font-weight: bold;");
        btnXemThongTin.setOnAction(e -> handleXemThongTinTaiKhoan());

        buttonBox.getChildren().addAll(btnRefresh, btnKhoa, btnMoKhoa, btnNapTien, btnXemThongTin);

        loadDanhSachTaiKhoan();
        panelTaiKhoan.getChildren().addAll(title, searchBox, tableTaiKhoan, buttonBox);
    }

    private void loadDanhSachTaiKhoan() {
        loadDanhSachTaiKhoan(txtTimStkTaiKhoan != null ? txtTimStkTaiKhoan.getText() : null);
    }

    private void loadDanhSachTaiKhoan(String keywordStk) {
        try {
            List<NguoiDung> list = nguoiDungDAO.layTatCa();
            String keyword = keywordStk != null ? keywordStk.trim() : "";
            if (!keyword.isEmpty()) {
                List<NguoiDung> filtered = new ArrayList<>();
                for (NguoiDung nd : list) {
                    String stk = nd.getSoTaiKhoan() != null ? nd.getSoTaiKhoan() : "";
                    if (stk.contains(keyword)) {
                        filtered.add(nd);
                    }
                }
                tableTaiKhoan.getItems().setAll(filtered);
            } else {
                tableTaiKhoan.getItems().setAll(list);
            }
        } catch (Exception e) {
            showAlert("Lỗi", "Không thể tải danh sách: " + e.getMessage());
        }
    }

    private static class KhoaInfo {
        String lyDo;
        Timestamp thoiGianMoKhoa;

        KhoaInfo(String lyDo, Timestamp thoiGianMoKhoa) {
            this.lyDo = lyDo;
            this.thoiGianMoKhoa = thoiGianMoKhoa;
        }
    }

    private void handleKhoaTaiKhoan() {
        NguoiDung selected = tableTaiKhoan.getSelectionModel().getSelectedItem();
        String validationError = validateInputKhoaTaiKhoan(selected);
        if (validationError != null) {
            showAlert("Lỗi", validationError);
            return;
        }

        Dialog<KhoaInfo> dialog = new Dialog<>();
        dialog.setTitle("Khóa tài khoản");
        dialog.setHeaderText("Khóa tài khoản: " + selected.getTenDangNhap());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        TextArea txtLyDo = new TextArea();
        txtLyDo.setPromptText("Nhập lý do khóa để thông báo tới người dùng...");
        txtLyDo.setPrefRowCount(3);
        txtLyDo.setPrefWidth(380);
        txtLyDo.setWrapText(true);

        ComboBox<String> cbThoiGian = new ComboBox<>();
        cbThoiGian.getItems().addAll(
                "Vĩnh viễn",
                "1 giờ",
                "6 giờ",
                "1 ngày",
                "3 ngày",
                "7 ngày",
                "30 ngày"
        );
        cbThoiGian.setValue("Vĩnh viễn");
        cbThoiGian.setPrefWidth(250);

        grid.add(new Label("Lý do khóa:"), 0, 0);
        grid.add(txtLyDo, 0, 1);
        grid.add(new Label("Thời gian khóa:"), 0, 2);
        grid.add(cbThoiGian, 0, 3);

        dialog.getDialogPane().setContent(grid);
        ButtonType btnKhoaOK = new ButtonType("Xác nhận khóa", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnKhoaOK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == btnKhoaOK) {
                String lyDo = txtLyDo.getText().trim();
                if (lyDo.isEmpty()) lyDo = "(Không có lý do cụ thể)";

                Timestamp thoiGianMoKhoa;
                String sel = cbThoiGian.getValue();
                LocalDateTime now = LocalDateTime.now();
                switch (sel) {
                    case "1 giờ" -> thoiGianMoKhoa = Timestamp.valueOf(now.plusHours(1));
                    case "6 giờ" -> thoiGianMoKhoa = Timestamp.valueOf(now.plusHours(6));
                    case "1 ngày" -> thoiGianMoKhoa = Timestamp.valueOf(now.plusDays(1));
                    case "3 ngày" -> thoiGianMoKhoa = Timestamp.valueOf(now.plusDays(3));
                    case "7 ngày" -> thoiGianMoKhoa = Timestamp.valueOf(now.plusDays(7));
                    case "30 ngày" -> thoiGianMoKhoa = Timestamp.valueOf(now.plusDays(30));
                    default -> thoiGianMoKhoa = null;
                }
                return new KhoaInfo(lyDo, thoiGianMoKhoa);
            }
            return null;
        });

        dialog.showAndWait().ifPresent(info -> {
            try {
                nguoiDungDAO.capNhatTrangThai(selected.getMaNguoiDung(), "bi_khoa", info.lyDo, info.thoiGianMoKhoa);
                String msg = "Đã khóa tài khoản: " + selected.getTenDangNhap() + "\nLý do: " + info.lyDo;
                if (info.thoiGianMoKhoa != null) {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
                    msg += "\nTự mở khóa lúc: " + sdf.format(info.thoiGianMoKhoa);
                } else {
                    msg += "\nKhóa vĩnh viễn.";
                }
                showAlert("Thành công", msg);
                loadDanhSachTaiKhoan();
            } catch (Exception ex) {
                showAlert("Lỗi", ex.getMessage());
            }
        });
    }

    private void handleMoKhoaTaiKhoan() {
        NguoiDung selected = tableTaiKhoan.getSelectionModel().getSelectedItem();
        String validationError = validateInputMoKhoaTaiKhoan(selected);
        if (validationError != null) {
            showAlert("Lỗi", validationError);
            return;
        }

        try {
            nguoiDungDAO.capNhatTrangThai(selected.getMaNguoiDung(), "hoat_dong");
            showAlert("Thành công", "Đã mở khóa tài khoản: " + selected.getTenDangNhap());
            loadDanhSachTaiKhoan();
        } catch (Exception ex) {
            showAlert("Lỗi", ex.getMessage());
        }
    }

    private void handleNapTienChoUser() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Nạp tiền cho user");
        dialog.setHeaderText("Nhập STK để tự động hiển thị người nhận");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField txtSoTaiKhoan = new TextField();
        txtSoTaiKhoan.setPromptText("Nhập STK user cần nạp");
        txtSoTaiKhoan.setPrefWidth(220);
        MoneyInputUtil.attachDigitsOnly(txtSoTaiKhoan);

        Label lblXacNhan = new Label("Nhập STK để tra cứu người dùng...");
        lblXacNhan.setStyle("-fx-text-fill: #7f8c8d;");

        Label lblSoDu = new Label("Số dư hiện tại: -");
        lblSoDu.setStyle("-fx-text-fill: #2c3e50; -fx-font-weight: bold;");

        final NguoiDung[] userDaXacNhan = new NguoiDung[1];

        TextField txtSoTien = new TextField();
        txtSoTien.setPromptText("Nhập số tiền cần nạp (VD: 500000)");
        txtSoTien.setPrefWidth(320);
        MoneyInputUtil.attachMoneyFormatter(txtSoTien);

        grid.add(new Label("Số tài khoản:"), 0, 0);
        grid.add(txtSoTaiKhoan, 1, 0);
        grid.add(lblXacNhan, 1, 1);
        grid.add(lblSoDu, 1, 2);
        grid.add(new Label("Số tiền nạp:"), 0, 3);
        grid.add(txtSoTien, 1, 3);

        dialog.getDialogPane().setContent(grid);
        ButtonType btnNapType = new ButtonType("Nạp tiền", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnNapType, ButtonType.CANCEL);

        Button btnNap = (Button) dialog.getDialogPane().lookupButton(btnNapType);
        btnNap.setDisable(true);

        txtSoTaiKhoan.textProperty().addListener((obs, oldVal, newVal) -> {
            String stk = newVal != null ? newVal.trim() : "";
            if (stk.isEmpty()) {
                userDaXacNhan[0] = null;
                lblXacNhan.setText("Nhập STK để tra cứu người dùng...");
                lblXacNhan.setStyle("-fx-text-fill: #7f8c8d;");
                lblSoDu.setText("Số dư hiện tại: -");
                btnNap.setDisable(true);
                return;
            }

            try {
                NguoiDung nd = nguoiDungDAO.layNguoiDungThuongTheoSoTaiKhoan(stk);
                if (nd == null) {
                    userDaXacNhan[0] = null;
                    lblXacNhan.setText("Không tìm thấy user với STK này.");
                    lblXacNhan.setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold;");
                    lblSoDu.setText("Số dư hiện tại: -");
                    btnNap.setDisable(true);
                    return;
                }

                userDaXacNhan[0] = nd;
                lblXacNhan.setText(nd.getTenDangNhap() + " - " + nd.getHoTen());
                lblXacNhan.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                lblSoDu.setText("Số dư hiện tại: " + df.format(nd.getSoDu()) + " đ");
                btnNap.setDisable(false);
            } catch (Exception ex) {
                userDaXacNhan[0] = null;
                lblXacNhan.setText("Lỗi tra cứu STK.");
                lblXacNhan.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                lblSoDu.setText("Số dư hiện tại: -");
                btnNap.setDisable(true);
                showAlert("Lỗi", "Không thể tra cứu STK: " + ex.getMessage());
            }
        });

        NguoiDung selected = tableTaiKhoan.getSelectionModel().getSelectedItem();
        if (selected != null && "nguoi_dung".equals(selected.getVaiTro()) && selected.getSoTaiKhoan() != null) {
            txtSoTaiKhoan.setText(selected.getSoTaiKhoan());
        }

        dialog.setResultConverter(btn -> {
            if (btn == btnNapType) {
                return txtSoTien.getText() != null ? txtSoTien.getText().trim() : "";
            }
            return null;
        });

        dialog.showAndWait().ifPresent(raw -> {
            try {
                NguoiDung target = userDaXacNhan[0];
                String soTaiKhoanAdmin = LoginController.currentUser != null ? LoginController.currentUser.getSoTaiKhoan() : null;
                String validationError = validateInputNapTien(target, raw, soTaiKhoanAdmin);
                if (validationError != null) {
                    showAlert("Lỗi", validationError);
                    return;
                }

                BigDecimal soTienNap = MoneyInputUtil.parseMoney(raw);
                boolean ok = nguoiDungDAO.napTienVaoTaiKhoanVaTaoGiaoDich(
                        soTaiKhoanAdmin,
                        target.getMaNguoiDung(),
                        soTienNap,
                        "Ngân hàng chuyển tiền"
                );
                if (ok) {
                    showAlert("Thành công", "Đã nạp " + df.format(soTienNap) + " đ cho user " + target.getTenDangNhap() +
                            "!\nLịch sử giao dịch đã ghi nội dung: Ngân hàng chuyển tiền.");
                    loadDanhSachTaiKhoan();
                } else {
                    showAlert("Lỗi", "Nạp tiền thất bại. Vui lòng thử lại!");
                }
            } catch (Exception ex) {
                showAlert("Lỗi", "Không thể nạp tiền: " + ex.getMessage());
            }
        });
    }

    private void handleXemThongTinTaiKhoan() {
        NguoiDung selected = tableTaiKhoan.getSelectionModel().getSelectedItem();

        try {
            NguoiDung nd = selected != null ? nguoiDungDAO.layTheoId(selected.getMaNguoiDung()) : null;
            String validationError = validateInputXemThongTinTaiKhoan(selected, nd);
            if (validationError != null) {
                showAlert("Lỗi", validationError);
                return;
            }

            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Thông tin người dùng");
            dialog.setHeaderText("Chi tiết tài khoản STK: " + nd.getSoTaiKhoan());
            dialog.getDialogPane().setPrefWidth(680);

            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm:ss dd/MM/yyyy");

            VBox content = new VBox(14);
            content.setPadding(new Insets(16));
            content.setStyle("-fx-background-color: white;");

            Label secCoBan = createInfoSectionTitle("Thông tin cơ bản");
            GridPane gridCoBan = new GridPane();
            gridCoBan.setHgap(14);
            gridCoBan.setVgap(10);
            int rCoBan = 0;
            addInfoRow(gridCoBan, rCoBan++, "Số tài khoản", safeText(nd.getSoTaiKhoan()));
            addInfoRow(gridCoBan, rCoBan++, "Tên đăng nhập", safeText(nd.getTenDangNhap()));
            addInfoRow(gridCoBan, rCoBan++, "Họ tên", safeText(nd.getHoTen()));
            addInfoRow(gridCoBan, rCoBan++, "Email", safeText(nd.getEmail()));
            addInfoRow(gridCoBan, rCoBan++, "Vai trò", formatVaiTro(nd.getVaiTro()));
            addInfoRow(gridCoBan, rCoBan++, "Trạng thái", formatTrangThai(nd));
            if (!"quan_ly".equals(nd.getVaiTro())) {
                addInfoRow(gridCoBan, rCoBan, "Số dư tài khoản", df.format(nd.getSoDu() != null ? nd.getSoDu() : BigDecimal.ZERO) + " đ");
            }

            content.getChildren().addAll(secCoBan, gridCoBan);

            if ("bi_khoa".equals(nd.getTrangThai())) {
                Label secKhoa = createInfoSectionTitle("Thông tin khóa");
                GridPane gridKhoa = new GridPane();
                gridKhoa.setHgap(14);
                gridKhoa.setVgap(10);
                addInfoRow(gridKhoa, 0, "Lý do khóa", safeText(nd.getLyDoKhoa()));
                addInfoRow(gridKhoa, 1, "Hết khóa lúc", nd.getThoiGianMoKhoa() != null ? sdf.format(nd.getThoiGianMoKhoa()) : "Khóa vĩnh viễn");
                content.getChildren().addAll(secKhoa, gridKhoa);
            }

            Label secMoc = createInfoSectionTitle("Mốc thời gian");
            GridPane gridMoc = new GridPane();
            gridMoc.setHgap(14);
            gridMoc.setVgap(10);
            addInfoRow(gridMoc, 0, "Lần đăng nhập cuối", nd.getLanDangNhapCuoi() != null ? sdf.format(nd.getLanDangNhapCuoi()) : "-");
            addInfoRow(gridMoc, 1, "Ngày tạo", nd.getNgayTao() != null ? sdf.format(nd.getNgayTao()) : "-");
            content.getChildren().addAll(secMoc, gridMoc);

            ScrollPane scroll = new ScrollPane(content);
            scroll.setFitToWidth(true);
            scroll.setPrefViewportHeight(430);

            dialog.getDialogPane().setContent(scroll);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            dialog.showAndWait();
        } catch (Exception ex) {
            showAlert("Lỗi", "Không thể tải thông tin user: " + ex.getMessage());
        }
    }

    public static String validateInputKhoaTaiKhoan(NguoiDung selected) {
        if (selected == null) {
            return "Vui lòng chọn tài khoản cần khóa!";
        }

        if ("quan_ly".equals(selected.getVaiTro())) {
            return "Không thể khóa tài khoản Admin!";
        }

        if ("bi_khoa".equals(selected.getTrangThai())) {
            return "Tài khoản này đã bị khóa!";
        }

        return null;
    }

    public static String validateInputMoKhoaTaiKhoan(NguoiDung selected) {
        if (selected == null) {
            return "Vui lòng chọn tài khoản cần mở khóa!";
        }

        if ("hoat_dong".equals(selected.getTrangThai())) {
            return "Tài khoản này đang hoạt động bình thường!";
        }

        return null;
    }

    public static String validateInputNapTien(NguoiDung target, String soTienStr, String soTaiKhoanAdmin) {
        if (target == null) {
            return "Vui lòng nhập STK hợp lệ trước khi nạp tiền!";
        }

        if (soTienStr == null || soTienStr.trim().isEmpty()) {
            return "Vui lòng nhập số tiền cần nạp!";
        }

        try {
            BigDecimal soTienNap = MoneyInputUtil.parseMoney(soTienStr);
            if (soTienNap == null) {
                return "Số tiền không hợp lệ!";
            }
            if (soTienNap.compareTo(BigDecimal.ZERO) <= 0) {
                return "Số tiền nạp phải lớn hơn 0!";
            }
            if (soTienNap.compareTo(new BigDecimal("999999999999")) > 0) {
                return "Số tiền nạp quá lớn!";
            }
        } catch (NumberFormatException e) {
            return "Số tiền không hợp lệ!";
        }

        if (soTaiKhoanAdmin == null || soTaiKhoanAdmin.isBlank()) {
            return "Không xác định được tài khoản Admin để ghi lịch sử giao dịch!";
        }

        return null;
    }

    public static String validateInputXemThongTinTaiKhoan(NguoiDung selected, NguoiDung thongTinNguoiDung) {
        if (selected == null) {
            return "Vui lòng chọn user cần xem thông tin!";
        }

        if (thongTinNguoiDung == null) {
            return "Không tìm thấy thông tin người dùng trong cơ sở dữ liệu!";
        }

        return null;
    }

    private String safeText(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private Label createInfoSectionTitle(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        return label;
    }

    private void addInfoRow(GridPane grid, int row, String key, String value) {
        Label lblKey = new Label(key + ":");
        lblKey.setStyle("-fx-text-fill: #5d6d7e; -fx-font-weight: bold;");

        Label lblVal = new Label(value);
        lblVal.setStyle("-fx-text-fill: #2c3e50;");
        lblVal.setWrapText(true);
        lblVal.setMaxWidth(460);

        grid.add(lblKey, 0, row);
        grid.add(lblVal, 1, row);
    }

    private String formatVaiTro(String vaiTro) {
        if ("quan_ly".equals(vaiTro)) return "Quản lý";
        if ("nguoi_dung".equals(vaiTro)) return "Người dùng";
        return safeText(vaiTro);
    }

    private String formatTrangThai(NguoiDung nd) {
        if (nd == null) return "-";
        if ("hoat_dong".equals(nd.getTrangThai())) return "Hoạt động";
        if ("bi_khoa".equals(nd.getTrangThai())) return "Bị khóa";
        return safeText(nd.getTrangThai());
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
