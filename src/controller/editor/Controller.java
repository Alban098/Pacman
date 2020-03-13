package controller.editor;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import modele.editor.Editor;
import modele.game.Game;
import modele.game.entities.StaticEntity;
import modele.game.enums.GameState;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML ToggleButton gate_bt;
    @FXML TextField end_tb;
    @FXML Button save_bt;
    @FXML ToggleButton fill_bt;
    @FXML Button go_to_bt;
    @FXML CheckBox default_map_cb;
    @FXML TextField lvl_tb;
    @FXML Button exit_bt;
    @FXML Button randomize_btn;
    @FXML TextField start_tb;
    @FXML ToggleButton wall_bt;
    @FXML ToggleButton gum_bt;
    @FXML ToggleButton super_gum_bt;
    @FXML ToggleButton ghost_home_bt;
    @FXML ToggleButton ghost_spawn_bt;
    @FXML ToggleButton player_spawn_bt;
    @FXML ToggleButton item_spawn_bt;
    @FXML ToggleButton eraser_bt;
    @FXML Slider width_slider;
    @FXML Label width_label;
    @FXML Slider length_slider;
    @FXML Label length_label;

    @FXML public void width_slider_slide() {
        String str = "Width : " + ((int) width_slider.getValue() * 2 + 1);
        width_label.setText(str);
        if (width_slider.getValue() != Editor.getInstance().getSizeX())
            Editor.getInstance().runLater(() -> Editor.getInstance().resize((int) width_slider.getValue() * 2 + 1, (int) length_slider.getValue() * 2 + 1));
    }

    @FXML public void length_slider_slide() {
        String str = "Height : " + ((int) length_slider.getValue() * 2 - 1);
        length_label.setText(str);
        if (length_slider.getValue() != Editor.getInstance().getSizeY())
            Editor.getInstance().runLater(() -> Editor.getInstance().resize((int) width_slider.getValue() * 2 + 1, (int) length_slider.getValue() * 2 + 1));
    }

    @FXML public void builder_selected(){
    }

    @FXML public void options_selected(){
    }

    @FXML public void randomize_btn_click(){
        Editor.getInstance().runLater(() -> Editor.getInstance().generate((int) width_slider.getValue() * 2 - 3, (int) length_slider.getValue() * 2 - 5));
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
                width_slider.setValue((Editor.getInstance().getSizeX() - 1) / 2);
                length_label.setText("Height : " + (Editor.getInstance().getSizeY() - 2));
                length_slider.setValue((Editor.getInstance().getSizeY() - 1) / 2);
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
                width_slider.setValue((Editor.getInstance().getSizeX() - 1) / 2);
                length_label.setText("Height : " + (Editor.getInstance().getSizeY() - 2));
                length_slider.setValue((Editor.getInstance().getSizeY() - 1) / 2);
            });
        });
        ToggleGroup group = new ToggleGroup();
        wall_bt.setToggleGroup(group);
        gum_bt.setToggleGroup(group);
        super_gum_bt.setToggleGroup(group);
        ghost_home_bt.setToggleGroup(group);
        ghost_spawn_bt.setToggleGroup(group);
        player_spawn_bt.setToggleGroup(group);
        item_spawn_bt.setToggleGroup(group);
        eraser_bt.setToggleGroup(group);
        gate_bt.setToggleGroup(group);
    }
}