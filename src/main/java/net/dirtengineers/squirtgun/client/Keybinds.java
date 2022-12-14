package net.dirtengineers.squirtgun.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.dirtengineers.squirtgun.Constants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import org.lwjgl.glfw.GLFW;

public class Keybinds {

    public static final KeyMapping GUN_AMMO_STATUS_DISPLAY_KEY =
            new KeyMapping(
                    Constants.KEY_GUN_AMMO_STATUS_DISPLAY,
                    KeyConflictContext.IN_GAME,
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_Y,
                    Constants.KEY_CATEGORY_MOD
            );

    public static final KeyMapping shiftClickGuiBinding =
            new KeyMapping(
                    Constants.openGunGui,
                    KeyConflictContext.IN_GAME,
                    KeyModifier.SHIFT,
                    InputConstants.Type.MOUSE,
                    InputConstants.MOUSE_BUTTON_LEFT,
                    Constants.KEY_CATEGORY_MOD
            );
    // X_Niter, I Was testing this below comment before the Potion update with success! :)
    /*new KeyMapping(
            Constants.KEY_SQUIRTGUN_MENU,
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_SHIFT,
            Constants.KEY_CATEGORY_MOD
            );*/
}
