package com.example.controller.admin;

import com.example.controller.LoginController;
import com.example.dao.NguoiDungDAO;
import com.example.model.NguoiDung;
import com.example.util.MoneyInputUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Controller panel đổi mật khẩu cho Admin.
 */
public class AdminPasswordController {

    private final NguoiDungDAO nguoiDungDAO = new NguoiDungDAO();
    private VBox panel;

    public AdminPasswordController() {
        buildPanel();
    }

    public VBox getPanel() {
        return panel;
    }

    private void buildPanel() {
        VBox outer = new VBox(20);
        outer.setAlignment(Pos.TOP_CENTER);
        outer.setPadding(new Insets(30));

        Label title = new Label("Đổi mật khẩu Admin");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(25));
        grid.setMaxWidth(580);
        grid.setStyle("-fx-background-color: white; -fx-background-radius: 8;");

        PasswordField txtCu = new PasswordField();
        txtCu.setPromptText("Nhập mật khẩu hiện tại");
        txtCu.setPrefWidth(300);

        PasswordField txtMoi = new PasswordField();
        txtMoi.setPromptText("Tối thiểu 6 ký tự");
        txtMoi.setPrefWidth(300);

        PasswordField txtXacNhan = new PasswordField();
        txtXacNhan.setPromptText("Nhập lại mật khẩu mới");
        txtXacNhan.setPrefWidth(300);

        Label lblKetQua = new Label("");
        lblKetQua.setWrapText(true);

        Button btnDoi = new Button("Đổi mật khẩu");
        btnDoi.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        btnDoi.setPrefWidth(200);

        btnDoi.setOnAction(e -> {
            String cu = txtCu.getText();
            String moi = txtMoi.getText();
            String xacNhan = txtXacNhan.getText();

            String validationError = validateInputDoiMatKhau(cu, moi, xacNhan);
            if (validationError != null) {
                lblKetQua.setText(validationError);
                lblKetQua.setStyle("-fx-text-fill: red;");
                return;
            }


            try {
                if (nguoiDungDAO.laMatKhauTrungHienTai(LoginController.currentUser.getMaNguoiDung(), moi)) {
                    lblKetQua.setText("Mật khẩu mới trùng với mật khẩu hiện tại!");
                    lblKetQua.setStyle("-fx-text-fill: red;");
                    return;
                }

                boolean ok = nguoiDungDAO.doiMatKhau(LoginController.currentUser.getMaNguoiDung(), cu, moi);
                if (ok) {
                    lblKetQua.setText("Đổi mật khẩu thành công!");
                    lblKetQua.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    txtCu.clear();
                    txtMoi.clear();
                    txtXacNhan.clear();
                } else {
                    lblKetQua.setText("Mật khẩu hiện tại không đúng!");
                    lblKetQua.setStyle("-fx-text-fill: red;");
                }
            } catch (Exception ex) {
                lblKetQua.setText("Lỗi: " + ex.getMessage());
                lblKetQua.setStyle("-fx-text-fill: red;");
            }
        });

        grid.add(new Label("Mật khẩu hiện tại:"), 0, 0);
        grid.add(txtCu, 1, 0);
        grid.add(new Label("Mật khẩu mới:"), 0, 1);
        grid.add(txtMoi, 1, 1);
        grid.add(new Label("Xác nhận mật khẩu:"), 0, 2);
        grid.add(txtXacNhan, 1, 2);
        grid.add(btnDoi, 1, 3);
        grid.add(lblKetQua, 1, 4);

        Separator separator = new Separator();
        separator.setMaxWidth(620);

        Label titleReset = new Label("Đặt lại mật khẩu cho User (quên mật khẩu)");
        titleReset.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        GridPane gridReset = new GridPane();
        gridReset.setHgap(15);
        gridReset.setVgap(15);
        gridReset.setPadding(new Insets(25));
        gridReset.setMaxWidth(620);
        gridReset.setStyle("-fx-background-color: white; -fx-background-radius: 8;");

        TextField txtStkUser = new TextField();
        txtStkUser.setPromptText("Nhập STK user cần đặt lại mật khẩu");
        txtStkUser.setPrefWidth(320);
        MoneyInputUtil.attachDigitsOnly(txtStkUser);

        Label lblUserInfo = new Label("Nhập STK để kiểm tra user...");
        lblUserInfo.setStyle("-fx-text-fill: #7f8c8d;");

        PasswordField txtMatKhauMoiUser = new PasswordField();
        txtMatKhauMoiUser.setPromptText("Mật khẩu mới cho user (tối thiểu 6 ký tự)");
        txtMatKhauMoiUser.setPrefWidth(320);

        PasswordField txtXacNhanUser = new PasswordField();
        txtXacNhanUser.setPromptText("Nhập lại mật khẩu mới");
        txtXacNhanUser.setPrefWidth(320);

        Label lblKetQuaReset = new Label("");
        lblKetQuaReset.setWrapText(true);

        final NguoiDung[] selectedUser = new NguoiDung[1];
        txtStkUser.textProperty().addListener((obs, oldVal, newVal) -> {
            String stk = newVal != null ? newVal.trim() : "";
            if (stk.isEmpty()) {
                selectedUser[0] = null;
                lblUserInfo.setText("Nhập STK để kiểm tra user...");
                lblUserInfo.setStyle("-fx-text-fill: #7f8c8d;");
                return;
            }
            try {
                NguoiDung nd = nguoiDungDAO.layNguoiDungThuongTheoSoTaiKhoan(stk);
                if (nd == null) {
                    selectedUser[0] = null;
                    lblUserInfo.setText("Không tìm thấy user với STK này.");
                    lblUserInfo.setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold;");
                } else {
                    selectedUser[0] = nd;
                    lblUserInfo.setText(nd.getTenDangNhap() + " - " + nd.getHoTen());
                    lblUserInfo.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                }
            } catch (Exception ex) {
                selectedUser[0] = null;
                lblUserInfo.setText("Lỗi kiểm tra STK.");
                lblUserInfo.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            }
        });

        Button btnReset = new Button("Đặt lại mật khẩu user");
        btnReset.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        btnReset.setPrefWidth(250);

        btnReset.setOnAction(e -> {
            NguoiDung nd = selectedUser[0];
            String mkMoi = txtMatKhauMoiUser.getText();
            String mkXn = txtXacNhanUser.getText();

            String validationError = validateInputDatLaiMatKhau(nd, mkMoi, mkXn);
            if (validationError != null) {
                lblKetQuaReset.setText(validationError);
                lblKetQuaReset.setStyle("-fx-text-fill: red;");
                return;
            }


            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Xác nhận đặt lại mật khẩu");
            confirm.setHeaderText("Đặt lại mật khẩu cho user: " + nd.getTenDangNhap());
            confirm.setContentText("STK: " + nd.getSoTaiKhoan() + "\nHọ tên: " + nd.getHoTen() + "\n\nBạn có chắc chắn muốn tiếp tục?");
            if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                return;
            }

            try {
                if (nguoiDungDAO.laMatKhauTrungHienTai(nd.getMaNguoiDung(), mkMoi)) {
                    lblKetQuaReset.setText("Mật khẩu mới trùng với mật khẩu hiện tại của user!");
                    lblKetQuaReset.setStyle("-fx-text-fill: red;");
                    return;
                }

                boolean ok = nguoiDungDAO.datLaiMatKhauChoUser(nd.getMaNguoiDung(), mkMoi);
                if (ok) {
                    lblKetQuaReset.setText("Đặt lại mật khẩu thành công cho user " + nd.getTenDangNhap() + "!");
                    lblKetQuaReset.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    txtMatKhauMoiUser.clear();
                    txtXacNhanUser.clear();
                } else {
                    lblKetQuaReset.setText("Đặt lại mật khẩu thất bại!");
                    lblKetQuaReset.setStyle("-fx-text-fill: red;");
                }
            } catch (Exception ex) {
                lblKetQuaReset.setText("Lỗi: " + ex.getMessage());
                lblKetQuaReset.setStyle("-fx-text-fill: red;");
            }
        });

        gridReset.add(new Label("STK User:"), 0, 0);
        gridReset.add(txtStkUser, 1, 0);
        gridReset.add(lblUserInfo, 1, 1);
        gridReset.add(new Label("Mật khẩu mới:"), 0, 2);
        gridReset.add(txtMatKhauMoiUser, 1, 2);
        gridReset.add(new Label("Xác nhận mật khẩu:"), 0, 3);
        gridReset.add(txtXacNhanUser, 1, 3);
        gridReset.add(btnReset, 1, 4);
        gridReset.add(lblKetQuaReset, 1, 5);

        outer.getChildren().addAll(title, grid, separator, titleReset, gridReset);
        panel = outer;
    }

    public static String validateInputDoiMatKhau(String matKhauCu,
                                                 String matKhauMoi,
                                                 String xacNhanMatKhau) {
        if (matKhauCu == null || matKhauCu.isEmpty()
                || matKhauMoi == null || matKhauMoi.isEmpty()
                || xacNhanMatKhau == null || xacNhanMatKhau.isEmpty()) {
            return "Vui lòng điền đầy đủ thông tin!";
        }

        if (matKhauMoi.length() < 6) {
            return "Mật khẩu mới phải có ít nhất 6 ký tự!";
        }

        if (!matKhauMoi.equals(xacNhanMatKhau)) {
            return "Mật khẩu xác nhận không khớp!";
        }

        return null;
    }

    public static String validateInputDatLaiMatKhau(NguoiDung nguoiDung,
                                                    String matKhauMoi,
                                                    String xacNhanMatKhau) {
        if (nguoiDung == null) {
            return "Vui lòng nhập STK user hợp lệ!";
        }

        if (matKhauMoi == null || matKhauMoi.isBlank()
                || xacNhanMatKhau == null || xacNhanMatKhau.isBlank()) {
            return "Vui lòng nhập đầy đủ mật khẩu mới và xác nhận!";
        }

        if (matKhauMoi.length() < 6) {
            return "Mật khẩu mới phải có ít nhất 6 ký tự!";
        }

        if (!matKhauMoi.equals(xacNhanMatKhau)) {
            return "Mật khẩu xác nhận không khớp!";
        }

        return null;
    }
}
