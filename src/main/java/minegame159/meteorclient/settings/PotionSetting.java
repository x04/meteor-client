package minegame159.meteorclient.settings;

import minegame159.meteorclient.Meteor;
import minegame159.meteorclient.gui.screens.settings.PotionSettingScreen;
import minegame159.meteorclient.gui.widgets.WButton;
import minegame159.meteorclient.gui.widgets.WItemWithLabel;
import minegame159.meteorclient.utils.MyPotion;

import java.util.function.Consumer;

public class PotionSetting extends EnumSetting<MyPotion> {
    public PotionSetting(String name, String description, MyPotion defaultValue, Consumer<MyPotion> onChanged, Consumer<Setting<MyPotion>> onModuleActivated) {
        super(name, description, defaultValue, onChanged, onModuleActivated);

        widget = new WItemWithLabel(get().potion);
        widget.add(new WButton("Select")).getWidget().action = () -> Meteor.INSTANCE.getMinecraft().openScreen(new PotionSettingScreen(this));
    }

    @Override
    public void resetWidget() {
        ((WItemWithLabel) widget).set(get().potion);
    }

    public static class Builder extends EnumSetting.Builder<MyPotion> {
        @Override
        public EnumSetting<MyPotion> build() {
            return new PotionSetting(name, description, defaultValue, onChanged, onModuleActivated);
        }
    }
}
