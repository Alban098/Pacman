package controller.editor.fxml;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import modele.game.entities.StaticEntity;

public class Controller {
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

    public Controller(){
    }

    private static StaticEntity selectedEntity = null;

    public static StaticEntity getSelectedEntity() { return selectedEntity; }

    @FXML public void width_slider_slide() {
        String str = "Width : " + (int) width_slider.getValue();
        width_label.setText(str);
    }

    @FXML public void length_slider_slide() {
        String str = "Length : " + (int) length_slider.getValue();
        length_label.setText(str);
    }

    @FXML public void builder_selected(){
        selectedEntity = null;
    }

    @FXML public void options_selected(){
        selectedEntity = null;
    }

    @FXML public void wall_bt_click(){
        selectedEntity = StaticEntity.WALL;
    }

    @FXML public void gum_bt_click(){
        selectedEntity = StaticEntity.GUM;
    }

    @FXML public void super_gum_bt_click(){
        selectedEntity = StaticEntity.SUPER_GUM;
    }

    @FXML public void ghost_home_bt_click(){
        selectedEntity = StaticEntity.GHOST_HOME;
    }

    @FXML public void ghost_spawn_bt_click(){
        selectedEntity = StaticEntity.GHOST_SPAWN;
    }

    @FXML public void player_spawn_bt_click(){
        selectedEntity = StaticEntity.PLAYER_SPAWN;
    }

    @FXML public void item_spawn_bt_click(){
        selectedEntity = StaticEntity.ITEM_SPAWN;
    }

    @FXML public void eraser_bt_click(){
        selectedEntity = StaticEntity.EMPTY;
    }
}
