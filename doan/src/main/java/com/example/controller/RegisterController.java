package com.example.controller;

import com.example.dao.NguoiDungDAO;
import com.example.model.NguoiDung;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.sql.SQLException;

/**
 * Controller cho màn hình đăng ký (JavaFX thuần)
 */
public class RegisterController {

    private Stage stage;
    private Scene scene;
    private TextField txtTenDangNhap;
    private TextField txtEmail;
    private TextField txtHoTen;
    private PasswordField txtMatKhau;
    private PasswordField txtXacNhanMatKhau;
    private Label lblThongBao;
    private Button btnDangKy;
    private Hyperlink linkDangNhap;
    private NguoiDungDAO nguoiDungDAO = new NguoiDungDAO();

    public RegisterController(Stage stage) {
        this.stage = stage;
        createUI();
    }

    private void createUI() {
        // Root layout
        VBox root = new VBox(12);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #f0f0f0;");

        // Tiêu đề
        Label lblTieuDe = new Label("ĐĂNG KÝ TÀI KHOẢN");
        lblTieuDe.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        lblTieuDe.setStyle("-fx-text-fill: #2c3e50;");

        // Form đăng ký
        Label lblHoTen = new Label("Họ tên:");
        lblHoTen.setFont(Font.font("Arial", 13));
        txtHoTen = new TextField();
        txtHoTen.setPromptText("Nhập họ tên đầy đủ");
        txtHoTen.setPrefWidth(350);
        txtHoTen.setStyle("-fx-font-size: 13px;");

        Label lblTenDangNhap = new Label("Tên đăng nhập:");
        lblTenDangNhap.setFont(Font.font("Arial", 13));
        txtTenDangNhap = new TextField();
        txtTenDangNhap.setPromptText("Nhập tên đăng nhập");
        txtTenDangNhap.setPrefWidth(350);
        txtTenDangNhap.setStyle("-fx-font-size: 13px;");

        Label lblEmail = new Label("Email:");
        lblEmail.setFont(Font.font("Arial", 13));
        txtEmail = new TextField();
        txtEmail.setPromptText("Nhập email");
        txtEmail.setPrefWidth(350);
        txtEmail.setStyle("-fx-font-size: 13px;");

        Label lblMatKhau = new Label("Mật khẩu:");
        lblMatKhau.setFont(Font.font("Arial", 13));
        txtMatKhau = new PasswordField();
        txtMatKhau.setPromptText("Nhập mật khẩu (ít nhất 6 ký tự)");
        txtMatKhau.setPrefWidth(350);
        txtMatKhau.setStyle("-fx-font-size: 13px;");

        Label lblXacNhan = new Label("Xác nhận mật khẩu:");
        lblXacNhan.setFont(Font.font("Arial", 13));
        txtXacNhanMatKhau = new PasswordField();
        txtXacNhanMatKhau.setPromptText("Nhập lại mật khẩu");
        txtXacNhanMatKhau.setPrefWidth(350);
        txtXacNhanMatKhau.setStyle("-fx-font-size: 13px;");

        // Hỗ trợ thao tác Enter liên tục không cần dùng chuột
        txtHoTen.setOnAction(e -> txtTenDangNhap.requestFocus());
        txtTenDangNhap.setOnAction(e -> txtEmail.requestFocus());
        txtEmail.setOnAction(e -> txtMatKhau.requestFocus());
        txtMatKhau.setOnAction(e -> txtXacNhanMatKhau.requestFocus());
        txtXacNhanMatKhau.setOnAction(e -> handleDangKy());

        // Nút đăng ký
        btnDangKy = new Button("Đăng ký");
        btnDangKy.setPrefWidth(350);
        btnDangKy.setPrefHeight(40);
        btnDangKy.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        btnDangKy.setOnAction(e -> handleDangKy());

        // Thông báo
        lblThongBao = new Label("");
        lblThongBao.setFont(Font.font("Arial", 12));
        lblThongBao.setWrapText(true);
        lblThongBao.setMaxWidth(350);

        // Link quay lại đăng nhập
        linkDangNhap = new Hyperlink("Đã có tài khoản? Đăng nhập");
        linkDangNhap.setStyle("-fx-font-size: 12px;");
        linkDangNhap.setOnAction(e -> handleQuayLai());

        // Thêm vào layout
        root.getChildren().addAll(
            lblTieuDe,
            new Label(""), // spacer
            lblHoTen,
            txtHoTen,
            lblTenDangNhap,
            txtTenDangNhap,
            lblEmail,
            txtEmail,
            lblMatKhau,
            txtMatKhau,
            lblXacNhan,
            txtXacNhanMatKhau,
            btnDangKy,
            lblThongBao,
            linkDangNhap
        );

        // Tạo scene
        scene = new Scene(root, 500, 650);
    }

    private void handleDangKy() {
        String tenDangNhap = txtTenDangNhap.getText().trim();
        String email = txtEmail.getText().trim();
        String hoTen = txtHoTen.getText().trim();
        String matKhau = txtMatKhau.getText();
        String xacNhanMatKhau = txtXacNhanMatKhau.getText();

        String validationError = validateInputDangKy(hoTen, tenDangNhap, email, matKhau, xacNhanMatKhau);
        if (validationError != null) {
            lblThongBao.setText(validationError);
            lblThongBao.setStyle("-fx-text-fill: red;");
            return;
        }
        try {
            if (nguoiDungDAO.tonTaiTenDangNhap(tenDangNhap)) {
                lblThongBao.setText("Tên đăng nhập đã tồn tại!");
                lblThongBao.setStyle("-fx-text-fill: red;");
                return;
            }

            if (nguoiDungDAO.tonTaiEmail(email)) {
                lblThongBao.setText("Email đã tồn tại!");
                lblThongBao.setStyle("-fx-text-fill: red;");
                return;
            }

            // Tạo đối tượng người dùng mới
            NguoiDung nguoiDung = new NguoiDung(tenDangNhap, matKhau, hoTen, email);

            boolean thanhCong = nguoiDungDAO.dangKy(nguoiDung);

            if (thanhCong) {
                lblThongBao.setText("Đăng ký thành công! Đang chuyển về đăng nhập...");
                lblThongBao.setStyle("-fx-text-fill: green;");

                // Delay 1.5s rồi chuyển về màn hình đăng nhập (dùng PauseTransition như LoginController)
                javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(
                        javafx.util.Duration.millis(1500));
                pause.setOnFinished(ev -> chuyenVeDangNhap());
                pause.play();
            } else {
                lblThongBao.setText("Đăng ký thất bại. Vui lòng thử lại!");
                lblThongBao.setStyle("-fx-text-fill: red;");
            }
        } catch (SQLException e) {
            String message = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (message.contains("duplicate entry") && message.contains("ten_dang_nhap")) {
                lblThongBao.setText("Tên đăng nhập đã tồn tại!");
            } else if (message.contains("duplicate entry") && message.contains("email")) {
                lblThongBao.setText("Email đã tồn tại!");
            } else if (message.contains("duplicate entry")) {
                lblThongBao.setText("Tên đăng nhập hoặc email đã tồn tại!");
            } else {
                lblThongBao.setText("Lỗi đăng ký. Vui lòng thử lại!");
            }
            lblThongBao.setStyle("-fx-text-fill: red;");
        }
    }

    public static String validateInputDangKy(String hoTen,
                                             String tenDangNhap,
                                             String email,
                                             String matKhau,
                                             String xacNhanMatKhau) {
        if (tenDangNhap == null || tenDangNhap.trim().isEmpty()
                || email == null || email.trim().isEmpty()
                || hoTen == null || hoTen.trim().isEmpty()
                || matKhau == null || matKhau.isEmpty()
                || xacNhanMatKhau == null || xacNhanMatKhau.isEmpty()) {
            return "Vui lòng nhập đầy đủ thông tin!";
        }

        if (matKhau.length() < 6) {
            return "Mật khẩu phải có ít nhất 6 ký tự!";
        }

        if (!matKhau.equals(xacNhanMatKhau)) {
            return "Mật khẩu xác nhận không khớp!";
        }

        return null;
    }

    private void handleQuayLai() {
        chuyenVeDangNhap();
    }

    private void chuyenVeDangNhap() {
        LoginController loginController = new LoginController(stage);
        stage.setScene(loginController.getScene());
        stage.setTitle("Đăng nhập");
        stage.setResizable(false);
        stage.setWidth(520);
        stage.setHeight(590);
        stage.centerOnScreen();
    }

    public Scene getScene() {
        return scene;
    }
}
