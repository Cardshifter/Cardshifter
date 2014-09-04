package com.cardshifter.fx;

import com.cardshifter.core.TargetAction;
import com.cardshifter.core.Targetable;
import com.cardshifter.core.UsableAction;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class ChoiceBoxNode {
    
    private final double sizeX;
    private final double sizeY;
    private final String name;
    private final UsableAction action;
    private final FXMLGameController controller;
    
    private final Group choiceBoxGroup;
    
    public ChoiceBoxNode(double sizeX, double sizeY, String name, UsableAction action, FXMLGameController controller) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.name = name;
        this.action = action;
        this.controller = controller;
        this.choiceBoxGroup = new Group();
        this.createChoiceBox();
    }
    
    public Group getChoiceBoxGroup() {
        return this.choiceBoxGroup;
    }
    
    private void createChoiceBox() {
        this.createChoiceBoxBackground();
        this.createChoiceBoxFace();
        this.createChoiceBoxText();
        this.createActivateButton();
    }
    
    private void createChoiceBoxBackground() {
        //could be changed to be some kind of animated background or jpeg
        Rectangle activeBackground = new Rectangle(-this.sizeX*0.02, -this.sizeX*0.02, this.sizeX, this.sizeY);
        activeBackground.setFill(Color.YELLOW);
        choiceBoxGroup.getChildren().add(activeBackground);
    }
    
    private void createChoiceBoxFace() {
        Rectangle choiceBoxFace = new Rectangle(0,0,this.sizeX*0.96,this.sizeY*0.96);
        choiceBoxFace.setFill(Color.FIREBRICK);
        choiceBoxGroup.getChildren().add(choiceBoxFace);
    }
    
    private void createChoiceBoxText() {
        Label choiceBoxText = new Label();
        choiceBoxText.setText(String.format("Action = \n %s", this.action.getName()));
        choiceBoxText.setTextFill(Color.WHITE);
        choiceBoxGroup.getChildren().add(choiceBoxText);
    }
   
    private void createActivateButton() {
        Button button = new Button();
        //button.setStyle("-fx-background-color:transparent");
        //button.minWidth(100);
        //button.minHeight(100);
        //button.prefWidth(100);
        //button.prefHeight(100);
        button.setOnAction(this::buttonClick);
        choiceBoxGroup.getChildren().add(button);
    }
    
    private void buttonClick(ActionEvent event) {
        System.out.println("Trying to Perform Choice");
        if (this.action.isAllowed()) {
            if (this.action instanceof TargetAction) {
                TargetAction targetAction = (TargetAction) this.action;
                List<Targetable> targets = targetAction.findTargets();
                if (targets.isEmpty()) {
                    return;
                }

                //int targetIndex = Integer.parseInt(input.nextLine());
                int targetIndex = 0;
                        
                //TODO: add a check to make sure the target is valid//
                Targetable target = targets.get(targetIndex);
                targetAction.setTarget(target);
                targetAction.perform();
            }
            else this.action.perform();
        }
        this.controller.render();
    }
}