package com.cardshifter.gdx.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.cardshifter.api.incoming.StartGameRequest;
import com.cardshifter.api.outgoing.UserStatusMessage;
import com.cardshifter.gdx.CardshifterClient;

import java.util.HashMap;
import java.util.Map;

public class UsersList {

    private final Table table;
    private final Skin skin;
    private final Map<Integer, UserTable> userMap = new HashMap<Integer, UserTable>();
    private UserTable selected;

    public UsersList(Skin skin) {
        this.skin = skin;
        this.table = new Table(skin);
    }

    public void handleUserStatus(UserStatusMessage message) {
        switch (message.getStatus()) {
            case OFFLINE:
                UserTable table = userMap.get(message.getUserId());
                if (table != null) {
                    table.remove();
                }
                break;
            case ONLINE:
                table = userMap.get(message.getUserId());
                if (table == null) {
                    final UserTable userTable = new UserTable(skin, message.getUserId(), message.getName());
                    userMap.put(message.getUserId(), userTable);
                    userTable.getTable().addListener(new ClickListener() {
                        @Override
                        public void clicked(InputEvent event, float x, float y) {
                            selected = userTable;
                            Gdx.app.log("UsersList", "Selected " + userTable);
                        }
                    });
                    this.table.add(userTable.getTable()).expandX().fill().row();
                }
                break;
        }
    }

    public void inviteSelected(String[] availableMods, Stage stage, final CardshifterClient client) {
        if (selected == null) {
            return;
        }

        Dialog dialog = new Dialog("Invite " + selected.getName(), skin) {
            @Override
            protected void result(Object object) {
                client.send(new StartGameRequest(selected.getId(), (String) object));
            }
        };
        dialog.text("Which mod do you want to play?");
        for (String mod : availableMods) {
            dialog.button(mod, mod);
        }
        dialog.show(stage);


    }

    public Table getTable() {
        return table;
    }
}
