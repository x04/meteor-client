package minegame159.meteorclient.accounts;

import com.mojang.authlib.yggdrasil.YggdrasilEnvironment;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import minegame159.meteorclient.Meteor;
import minegame159.meteorclient.mixininterface.IMinecraftClient;
import minegame159.meteorclient.utils.ISerializable;
import minegame159.meteorclient.utils.NbtException;
import net.minecraft.client.util.Session;
import net.minecraft.nbt.CompoundTag;

public abstract class Account<T extends Account<?>> implements ISerializable<T> {
    protected final AccountCache cache;
    protected AccountType type;
    protected String name;

    public Account(AccountType type, String name) {
        this.type = type;
        this.name = name;
        this.cache = new AccountCache(type == AccountType.Cracked);
    }

    public abstract boolean fetchInfo();

    public abstract boolean fetchHead();

    public boolean login() {
        YggdrasilMinecraftSessionService service = (YggdrasilMinecraftSessionService) Meteor.INSTANCE.getMinecraft().getSessionService();
        AccountUtils.setBaseUrl(service, YggdrasilEnvironment.PROD.getSessionHost() + "/session/minecraft/");
        AccountUtils.setJoinUrl(service, YggdrasilEnvironment.PROD.getSessionHost() + "/session/minecraft/join");
        AccountUtils.setCheckUrl(service, YggdrasilEnvironment.PROD.getSessionHost() + "/session/minecraft/hasJoined");

        return true;
    }

    public String getUsername() {
        if (cache.username.isEmpty()) {
            return name;
        }
        return cache.username;
    }

    public AccountType getType() {
        return type;
    }

    public AccountCache getCache() {
        return cache;
    }

    protected void setSession(Session session) {
        ((IMinecraftClient) Meteor.INSTANCE.getMinecraft()).setSession(session);
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();

        tag.putString("type", type.name());
        tag.putString("name", name);
        tag.put("cache", cache.toTag());

        return tag;
    }

    @Override
    public T fromTag(CompoundTag tag) {
        if (!tag.contains("name") || !tag.contains("cache")) {
            throw new NbtException();
        }

        name = tag.getString("name");
        cache.fromTag(tag.getCompound("cache"));

        return (T) this;
    }
}
