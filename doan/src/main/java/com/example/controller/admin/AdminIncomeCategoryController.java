package com.example.controller.admin;

import com.example.dao.DanhMucDAO;
import com.example.model.DanhMuc;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller panel Danh mục Thu mặc định cho Admin.
 */
public class AdminIncomeCategoryController {

    private final DanhMucDAO danhMucDAO = new DanhMucDAO();

    private VBox panel;
    private TableView<DanhMuc> tableDanhMucThu;

    public AdminIncomeCategoryController() {
        buildPanel();
    }

    public VBox getPanel() {
        return panel;
    }

    public void refresh() {
        if (tableDanhMucThu != null) {
            tableDanhMucThu.getItems().setAll(danhMucDAO.layDanhMucTheoLoai(null, "thu"));
        }
    }

    private void buildPanel() {
        panel = new VBox(15);
        panel.setPadding(new Insets(5));

        Label title = new Label("Quản lý danh mục thu nhập mặc định");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        Label note = new Label("Danh mục Thu mặc định hiển thị cho TẤT CẢ người dùng khi nhận tiền. Chỉ Admin mới có thể thêm/sửa/xóa.");
        note.setStyle("-fx-text-fill: #7f8c8d; -fx-font-style: italic;");

        tableDanhMucThu = new TableView<>();
        VBox.setVgrow(tableDanhMucThu, Priority.ALWAYS);

        TableColumn<DanhMuc, String> colTen = new TableColumn<>("Tên danh mục");
        colTen.setCellValueFactory(new PropertyValueFactory<>("tenDanhMuc"));
        colTen.setPrefWidth(220);
        colTen.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setText(null);
                    setStyle("");
                    return;
                }
                DanhMuc dm = getTableView().getItems().get(getIndex());
                if (dm.isDanhMucCon()) {
                    setText("   ↳ " + (item != null ? item : "") + " (con)");
                    setStyle("-fx-text-fill: #2c3e50;");
                } else {
                    setText("▣ " + (item != null ? item : "") + " (cha)");
                    setStyle("-fx-font-weight: bold; -fx-text-fill: #1f6fb2;");
                }
            }
        });

        TableColumn<DanhMuc, String> colMoTa = new TableColumn<>("Mô tả");
        colMoTa.setCellValueFactory(new PropertyValueFactory<>("moTa"));
        colMoTa.setPrefWidth(360);

        TableColumn<DanhMuc, String> colCha = new TableColumn<>("Danh mục cha");
        colCha.setCellValueFactory(new PropertyValueFactory<>("tenDanhMucCha"));
        colCha.setPrefWidth(200);
        colCha.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setText(null);
                    setStyle("");
                    return;
                }
                DanhMuc dm = getTableView().getItems().get(getIndex());
                if (dm.isDanhMucCon()) {
                    setText(dm.getTenDanhMucCha() != null ? dm.getTenDanhMucCha() : "");
                    setStyle("-fx-text-fill: #5b6b7a;");
                } else {
                    setText("Danh mục cha");
                    setStyle("-fx-text-fill: #1f6fb2; -fx-font-weight: bold;");
                }
            }
        });

        tableDanhMucThu.getColumns().addAll(colTen, colMoTa, colCha);

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        Button btnThem = new Button("Thêm");
        btnThem.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
        btnThem.setOnAction(e -> handleThemDanhMucThu());

        Button btnSua = new Button("Sửa");
        btnSua.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
        btnSua.setOnAction(e -> handleSuaDanhMucThu());

        Button btnXoa = new Button("Xóa");
        btnXoa.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");
        btnXoa.setOnAction(e -> handleXoaDanhMucThu());

        buttonBox.getChildren().addAll(btnThem, btnSua, btnXoa);

        panel.getChildren().addAll(title, note, tableDanhMucThu, buttonBox);
        refresh();
    }

    private void handleThemDanhMucThu() {
        Dialog<DanhMuc> dialog = new Dialog<>();
        dialog.setTitle("Thêm danh mục Thu mặc định");
        dialog.setHeaderText("Tạo danh mục Thu nhập hiển thị cho tất cả người dùng");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField txtTen = new TextField();
        txtTen.setPromptText("Tên danh mục");
        txtTen.setPrefWidth(300);

        TextArea txtMoTa = new TextArea();
        txtMoTa.setPromptText("Mô tả chi tiết...");
        txtMoTa.setPrefRowCount(3);
        txtMoTa.setPrefWidth(300);

        ComboBox<DanhMuc> cbCha = new ComboBox<>();
        cbCha.setPrefWidth(300);
        cbCha.setPromptText("(Không có - danh mục gốc)");
        cbCha.getItems().setAll(layDanhMucChaMacDinh("thu", null));

        grid.add(new Label("Tên danh mục:"), 0, 0);
        grid.add(txtTen, 1, 0);
        grid.add(new Label("Mô tả:"), 0, 1);
        grid.add(txtMoTa, 1, 1);
        grid.add(new Label("Danh mục cha:"), 0, 2);
        grid.add(cbCha, 1, 2);

        dialog.getDialogPane().setContent(grid);

        ButtonType btnOK = new ButtonType("Thêm", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnOK, ButtonType.CANCEL);
        dialog.setResultConverter(btn -> {
            if (btn == btnOK) {
                String ten = txtTen.getText().trim();
                Integer parentId = cbCha.getValue() != null ? cbCha.getValue().getId() : null;
                return new DanhMuc(ten, txtMoTa.getText().trim(), "thu", null, parentId);
            }
            return null;
        });

        dialog.showAndWait().ifPresent(dm -> {
            boolean biTrung;
            if (dm.getParentId() != null) {
                biTrung = danhMucDAO.tonTaiTenDanhMucCon(dm.getTenDanhMuc(), dm.getLoai(), null, null);
            } else {
                biTrung = danhMucDAO.tonTaiTenDanhMuc(dm.getTenDanhMuc(), dm.getLoai(), null);
            }

            String err = validateInputDanhMuc(dm.getTenDanhMuc(), biTrung);
            if (err != null) { showAlert("Lỗi", err); return; }

            if (danhMucDAO.themDanhMuc(dm)) {
                showAlert("Thành công", "Đã thêm: " + dm.getTenDanhMuc());
                refresh();
            } else {
                showAlert("Lỗi", "Thêm thất bại!");
            }
        });
    }

    private void handleSuaDanhMucThu() {
        DanhMuc selected = tableDanhMucThu.getSelectionModel().getSelectedItem();
        String validationError = validateInputChonDanhMuc(selected, "sua");
        if (validationError != null) {
            showAlert("Lỗi", validationError);
            return;
        }
        final String tenCu = selected.getTenDanhMuc() != null ? selected.getTenDanhMuc().trim() : "";
        final Integer parentCu = selected.getParentId();

        Dialog<DanhMuc> dialog = new Dialog<>();
        dialog.setTitle("Sửa danh mục Thu");
        dialog.setHeaderText("Chỉnh sửa: " + selected.getTenDanhMuc());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField txtTen = new TextField(selected.getTenDanhMuc());
        txtTen.setPrefWidth(300);
        TextArea txtMoTa = new TextArea(selected.getMoTa() != null ? selected.getMoTa() : "");
        txtMoTa.setPrefRowCount(3);
        txtMoTa.setPrefWidth(300);

        ComboBox<DanhMuc> cbCha = new ComboBox<>();
        cbCha.setPrefWidth(300);
        cbCha.setPromptText("(Không có - danh mục gốc)");
        List<DanhMuc> dsCha = layDanhMucChaMacDinh("thu", selected.getId());
        cbCha.getItems().setAll(dsCha);
        if (selected.getParentId() != null) {
            for (DanhMuc dm : dsCha) {
                if (dm.getId() == selected.getParentId()) {
                    cbCha.setValue(dm);
                    break;
                }
            }
        }

        grid.add(new Label("Tên danh mục:"), 0, 0);
        grid.add(txtTen, 1, 0);
        grid.add(new Label("Mô tả:"), 0, 1);
        grid.add(txtMoTa, 1, 1);
        grid.add(new Label("Danh mục cha:"), 0, 2);
        grid.add(cbCha, 1, 2);
        dialog.getDialogPane().setContent(grid);

        ButtonType btnOK = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnOK, ButtonType.CANCEL);
        dialog.setResultConverter(btn -> {
            if (btn == btnOK) {
                String ten = txtTen.getText().trim();
                return new DanhMuc(
                        selected.getId(),
                        ten,
                        txtMoTa.getText().trim(),
                        selected.getLoai(),
                        selected.getSoTaiKhoan(),
                        cbCha.getValue() != null ? cbCha.getValue().getId() : null,
                        null
                );
            }
            return null;
        });

        dialog.showAndWait().ifPresent(dm -> {
            String tenMoi = dm.getTenDanhMuc() != null ? dm.getTenDanhMuc().trim() : "";
            boolean daDoiTen = !tenMoi.equalsIgnoreCase(tenCu);
            boolean daDoiCha = (parentCu == null && dm.getParentId() != null)
                    || (parentCu != null && !parentCu.equals(dm.getParentId()));

            boolean biTrung = dm.getParentId() != null
                    ? (daDoiTen || daDoiCha) && danhMucDAO.tonTaiTenDanhMucCon(tenMoi, dm.getLoai(), null, dm.getId())
                    : daDoiTen && danhMucDAO.tonTaiTenDanhMuc(tenMoi, dm.getLoai(), null);
            String err = validateInputDanhMuc(tenMoi, biTrung);
            if (err != null) { showAlert("Lỗi", err); return; }

            if (danhMucDAO.suaDanhMuc(dm)) {
                showAlert("Thành công", "Đã cập nhật!");
                refresh();
            } else {
                showAlert("Lỗi", "Cập nhật thất bại!");
            }
        });
    }

    private void handleXoaDanhMucThu() {
        DanhMuc selected = tableDanhMucThu.getSelectionModel().getSelectedItem();
        String validationError = validateInputChonDanhMuc(selected, "xoa");
        if (validationError != null) {
            showAlert("Lỗi", validationError);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText("Xóa: " + selected.getTenDanhMuc());
        confirm.setContentText("⚠️ Các giao dịch đã dùng danh mục này sẽ mất liên kết!\nBạn có chắc chắn muốn xóa?");
        confirm.showAndWait().ifPresent(resp -> {
            if (resp == ButtonType.OK) {
                if (danhMucDAO.xoaDanhMuc(selected.getId())) {
                    showAlert("Thành công", "Đã xóa!");
                    refresh();
                } else {
                    showAlert("Lỗi", "Xóa thất bại!");
                }
            }
        });
    }

    public static String validateInputDanhMuc(String tenDanhMuc) {
        if (tenDanhMuc == null || tenDanhMuc.trim().isEmpty()) {
            return "Vui lòng nhập tên danh mục!";
        }

        return null;
    }

    public static String validateInputDanhMuc(String tenDanhMuc, boolean daTrung) {
        String err = validateInputDanhMuc(tenDanhMuc);
        if (err != null) return err;

        if (daTrung) return "Tên danh mục đã tồn tại! Vui lòng nhập tên khác.";

        return null;
    }

    public static String validateInputChonDanhMuc(DanhMuc selected, String hanhDong) {
        if (selected == null) {
            if ("xoa".equals(hanhDong)) {
                return "Vui lòng chọn danh mục cần xóa!";
            }
            return "Vui lòng chọn danh mục cần sửa!";
        }

        return null;
    }

    private List<DanhMuc> layDanhMucChaMacDinh(String loai, Integer excludeId) {
        List<DanhMuc> all = danhMucDAO.layDanhMucTheoLoai(null, loai);
        List<DanhMuc> result = new ArrayList<>();
        for (DanhMuc dm : all) {
            if (excludeId != null && dm.getId() == excludeId) continue;
            if (dm.getParentId() == null) result.add(dm);
        }
        return result;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
