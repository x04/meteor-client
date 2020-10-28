package minegame159.meteorclient.accounts.gui;

import minegame159.meteorclient.Meteor;
import minegame159.meteorclient.accounts.Account;
import minegame159.meteorclient.accounts.AccountManager;
import minegame159.meteorclient.gui.GuiConfig;
import minegame159.meteorclient.gui.widgets.*;
import minegame159.meteorclient.utils.MeteorExecutor;

public class WAccount extends WTable {
    public WAccount(AccountsScreen screen, Account<?> account) {
        // Head
        add(new WTexture(16, 16, 90, account.getCache().getHeadTexture()));

        // Name
        WLabel name = add(new WLabel(account.getUsername())).getWidget();
        if (Meteor.INSTANCE.getMinecraft().getSession().getUsername().equalsIgnoreCase(account.getUsername())) {
            name.color = GuiConfig.INSTANCE.loggedInText;
        }

        // Account Type
        WLabel type = add(new WLabel("(" + account.getType() + ")")).fillX().right().getWidget();
        type.color = GuiConfig.INSTANCE.accountTypeText;

        // Login
        WButton login = add(new WButton("Login")).getWidget();
        login.action = () -> {
            login.freezeWidth();
            login.setText("...");
            screen.locked = true;

            MeteorExecutor.INSTANCE.execute(() -> {
                if (account.login()) {
                    name.setText(account.getUsername());

                    AccountManager.INSTANCE.save();

                    screen.clear();
                    screen.initWidgets();
                }

                login.setText("Login");
                screen.locked = false;
            });
        };

        // Remove
        WMinus minus = add(new WMinus()).getWidget();
        minus.action = () -> AccountManager.INSTANCE.remove(account);
    }
}
