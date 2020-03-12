package controller.editor;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import modele.editor.Editor;
import modele.game.Game;
import modele.game.entities.StaticEntity;
import modele.game.enums.GameState;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML Button gate_bt;
    @FXML TextField end_tb;
    @FXML Button save_bt;
    @FXML Button fill_bt;
    @FXML Button go_to_bt;
    @FXML CheckBox default_map_cb;
    @FXML TextField lvl_tb;
    @FXML Button exit_bt;
    @FXML TextField start_tb;
    @FXML Button wall_bt;
    @FXML Button gum_bt;
    @FXML Button super_gum_bt;
    @FXML Button ghost_home_bt;
    @FXML Button ghost_spawn_bt;
    @FXML Button player_spawn_bt;
    @FXML Button item_spawn_bt;
    @FXML Button eraser_bt;
    @FXML Slider width_slider;
    @FXML Label width_label;
    @FXML Slider length_slider;
    @FXML Label length_label;

    @FXML public void width_slider_slide() {
        String str = "Width : " + (int) width_slider.getValue();
        width_label.setText(str);
        if (width_slider.getValue() != Editor.getInstance().getSizeX())
            Editor.getInstance().runLater(() -> Editor.getInstance().resize((int) width_slider.getValue(), (int) length_slider.getValue()));
    }

    @FXML public void length_slider_slide() {
        String str = "Height : " + (int) length_slider.getValue();
        length_label.setText(str);
        if (length_slider.getValue() != Editor.getInstance().getSizeY())
            Editor.getInstance().runLater(() -> Editor.getInstance().resize((int) width_slider.getValue(), (int) length_slider.getValue()));
    }

    @FXML public void builder_selected(){
        Editor.getInstance().setSelectedEntity(null);
    }

    @FXML public void options_selected(){
        Editor.getInstance().setSelectedEntity(null);
    }

    @FXML public void wall_bt_click(){
        Editor.getInstance().setSelectedEntity(StaticEntity.WALL);
    }

    @FXML public void gum_bt_click(){
        Editor.getInstance().setSelectedEntity(StaticEntity.GUM);
    }

    @FXML public void super_gum_bt_click(){
        Editor.getInstance().setSelectedEntity(StaticEntity.SUPER_GUM);
    }

    @FXML public void ghost_home_bt_click(){
        Editor.getInstance().setSelectedEntity(StaticEntity.GHOST_HOME);
    }

    @FXML public void ghost_spawn_bt_click(){
        Editor.getInstance().setSelectedEntity(StaticEntity.GHOST_SPAWN);
    }

    @FXML public void player_spawn_bt_click(){
        Editor.getInstance().setSelectedEntity(StaticEntity.PLAYER_SPAWN);
    }

    @FXML public void item_spawn_bt_click(){
        Editor.getInstance().setSelectedEntity(StaticEntity.ITEM_SPAWN);
    }

    @FXML public void gate_bt_click(){
        Editor.getInstance().setSelectedEntity(StaticEntity.GATE);
    }

    @FXML public void fill_bt_click(){
        Editor.getInstance().switchFillMode();
    }

    @FXML public void eraser_bt_click(){
        Editor.getInstance().setSelectedEntity(StaticEntity.EMPTY);
    }

    @FXML public void save_bt_clicked() {
        Editor editor = Editor.getInstance();
        editor.runLater(() -> editor.saveMap(Integer.parseInt(start_tb.getText()), Integer.parseInt(end_tb.getText()), default_map_cb.isSelected()));
    }

    @FXML public void go_to_bt_clicked() {
        Editor.getInstance().runLater(() ->  {
            Editor.getInstance().loadMap(Integer.parseInt(lvl_tb.getText()));
            Platform.runLater(() -> {
                start_tb.setText(String.valueOf(Editor.getInstance().getLevelInterval().x));
                end_tb.setText(String.valueOf(Editor.getInstance().getLevelInterval().y));
                default_map_cb.setSelected(Editor.getInstance().isDefault());

                width_label.setText("Width : " + Editor.getInstance().getSizeX());
                width_slider.setValue(Editor.getInstance().getSizeX());
                length_label.setText("Height : " + Editor.getInstance().getSizeY());
                length_slider.setValue(Editor.getInstance().getSizeY());
            });
        });
    }

    @FXML public void default_map_cb_clicked() { }

    @FXML public void exit_bt_clicked() {
        Stage stage = (Stage) exit_bt.getScene().getWindow();
        stage.close();
        Editor.getInstance().requestClose();
        Game.getInstance().runLater(() -> Game.getInstance().setGameState(GameState.MENU_SCREEN));
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        start_tb.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                start_tb.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        end_tb.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                end_tb.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        lvl_tb.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                lvl_tb.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        Editor.getInstance().runLater(() ->  {
            Editor.getInstance().loadMap(1);
            Platform.runLater(() -> {
                start_tb.setText(String.valueOf(Editor.getInstance().getLevelInterval().x));
                end_tb.setText(String.valueOf(Editor.getInstance().getLevelInterval().y));
                default_map_cb.setSelected(Editor.getInstance().isDefault());

                width_label.setText("Width : " + Editor.getInstance().getSizeX());
                width_slider.setValue(Editor.getInstance().getSizeX());
                length_label.setText("Height : " + Editor.getInstance().getSizeY());
                length_slider.setValue(Editor.getInstance().getSizeY());
            });
        });
    }
}