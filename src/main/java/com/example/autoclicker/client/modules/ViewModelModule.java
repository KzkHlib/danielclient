package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Module;
import com.example.autoclicker.client.Setting;

/**
 * ViewModel : décale / redimensionne / oriente l'item tenu en première personne.
 * Lu par MixinItemRenderer via les champs statiques.
 */
public class ViewModelModule extends Module {

    public static volatile boolean active = false;
    public static volatile float posX, posY, posZ;
    public static volatile float scale = 1f;
    public static volatile float rotX, rotY, rotZ;

    private final Setting.Number sx = (Setting.Number) add(new Setting.Number("Pos X", 0, -2, 2, 0.05, false));
    private final Setting.Number sy = (Setting.Number) add(new Setting.Number("Pos Y", 0, -2, 2, 0.05, false));
    private final Setting.Number sz = (Setting.Number) add(new Setting.Number("Pos Z", 0, -2, 2, 0.05, false));
    private final Setting.Number ss = (Setting.Number) add(new Setting.Number("Echelle", 1, 0.5, 2, 0.05, false));
    private final Setting.Number rx = (Setting.Number) add(new Setting.Number("Rot X", 0, -180, 180, 5, true));
    private final Setting.Number ry = (Setting.Number) add(new Setting.Number("Rot Y", 0, -180, 180, 5, true));
    private final Setting.Number rz = (Setting.Number) add(new Setting.Number("Rot Z", 0, -180, 180, 5, true));

    public ViewModelModule() {
        super("ViewModel", Category.VISUAL);
    }

    @Override public void onEnable()  { active = true; push(); }
    @Override public void onDisable() { active = false; }
    @Override public void onTick()    { push(); }

    private void push() {
        posX = (float) sx.value; posY = (float) sy.value; posZ = (float) sz.value;
        scale = (float) ss.value;
        rotX = (float) rx.value; rotY = (float) ry.value; rotZ = (float) rz.value;
    }
}
