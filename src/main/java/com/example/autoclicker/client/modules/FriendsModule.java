package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Client;
import com.example.autoclicker.client.FriendManager;
import com.example.autoclicker.client.Module;

import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.input.Keyboard;

/**
 * Friends : pointe un joueur et appuie sur la touche liée (B par défaut) pour
 * l'ajouter/retirer de tes amis. Les amis sont ignorés par l'AimAssist et
 * colorés à part par l'ESP / les Nametags. Le module reste "actif" en
 * permanence ; seule la touche compte (holdKey gère le bind nous-mêmes).
 */
public class FriendsModule extends Module {

    private boolean prevDown = false;

    public FriendsModule() {
        super("Friends", Category.PLAYER);
        holdKey = true;                 // on gère la touche nous-mêmes (pas un toggle)
        key = Keyboard.KEY_B;
        showInArrayList = false;
    }

    @Override
    public void onTick() {
        boolean down = key != 0 && Keyboard.isKeyDown(key)
                && mc.currentScreen == null && mc.thePlayer != null;
        if (down && !prevDown) addLooked();
        prevDown = down;
    }

    private void addLooked() {
        if (mc.objectMouseOver == null) return;
        if (!(mc.objectMouseOver.entityHit instanceof EntityPlayer)) return;
        EntityPlayer p = (EntityPlayer) mc.objectMouseOver.entityHit;
        String name = p.getName();
        boolean nowFriend = FriendManager.toggle(name);
        if (Client.INSTANCE != null) {
            Client.INSTANCE.notifications.add(
                    new Client.Notif(name + (nowFriend ? " ajouté" : " retiré"), nowFriend));
            Client.INSTANCE.save();
        }
    }
}
