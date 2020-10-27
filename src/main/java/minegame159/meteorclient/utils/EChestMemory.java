package minegame159.meteorclient.utils;

import me.zero.alpine.listener.Listener;
import minegame159.meteorclient.Meteor;
import minegame159.meteorclient.events.BlockActivateEvent;
import minegame159.meteorclient.events.OpenScreenEvent;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.util.collection.DefaultedList;

public class EChestMemory {
    private static final MinecraftClient MC = Meteor.INSTANCE.getMinecraft();

    private static int echestOpenedState;
    public static final DefaultedList<ItemStack> ITEMS = DefaultedList.ofSize(27, ItemStack.EMPTY);

    private static final Listener<BlockActivateEvent> onBlockActivate = new Listener<>(event -> {
        if (event.blockState.getBlock() instanceof EnderChestBlock && echestOpenedState == 0) echestOpenedState = 1;
    });

    private static final Listener<OpenScreenEvent> onOpenScreenEvent = new Listener<>(event -> {
        if (echestOpenedState == 1 && event.screen instanceof GenericContainerScreen) {
            echestOpenedState = 2;
            return;
        }
        if (echestOpenedState == 0) return;

        if (!(MC.currentScreen instanceof GenericContainerScreen)) return;
        GenericContainerScreenHandler container = ((GenericContainerScreen) MC.currentScreen).getScreenHandler();
        if (container == null) return;
        Inventory inv = container.getInventory();

        for (int i = 0; i < 27; i++) {
            ITEMS.set(i, inv.getStack(i));
        }

        echestOpenedState = 0;
    });

    static {
        Meteor.INSTANCE.getEventBus().subscribe(onBlockActivate);
        Meteor.INSTANCE.getEventBus().subscribe(onOpenScreenEvent);
    }
}
