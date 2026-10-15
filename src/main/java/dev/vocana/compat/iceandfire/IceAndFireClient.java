package dev.vocana.compat.iceandfire;

import com.mojang.blaze3d.platform.NativeImage;
import dev.vocana.compat.VocanaCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * The module's client half, reached only from {@link IceAndFireCompat#init} on the client. It makes the worn look of
 * wyrmsteel dragon armour: Ice and Fire's renderer asks for {@code iceandfire:textures/entity/dragon_armor/
 * armor_<part>_wyrmsteel.png}, and on every resource reload this reads Ice and Fire's own netherite texture of the part
 * from the installed mod (or a resource pack that replaces it), recolours it ({@link WyrmsteelPalette}) and registers the
 * result under that name as a dynamic texture. Nothing of Ice and Fire's art is shipped in this jar.
 */
final class IceAndFireClient {
    private IceAndFireClient() {}
    /** Ice and Fire's {@code DragonArmorPart} ids. */
    static final List<String> PARTS = List.of("head", "neck", "body", "tail");

    static void init(IEventBus modBus) {
        modBus.addListener((RegisterClientReloadListenersEvent e) -> e.registerReloadListener((ResourceManagerReloadListener) IceAndFireClient::makeDragonArmourTextures));
    }

    static ResourceLocation dragonArmourTexture(String part, String material) {
        return ResourceLocation.fromNamespaceAndPath("iceandfire", "textures/entity/dragon_armor/armor_" + part + "_" + material + ".png");
    }

    private static void makeDragonArmourTextures(ResourceManager resources) {
        var textures = Minecraft.getInstance().getTextureManager();
        for (String part : PARTS) {
            var source = dragonArmourTexture(part, "netherite");
            var resource = resources.getResource(source);
            if (resource.isEmpty()) { VocanaCompat.LOG.warn("iceandfire: no {} to draw wyrmsteel dragon armour from; it will show as missing", source); continue; }
            try (InputStream in = resource.get().open()) {
                NativeImage image = NativeImage.read(in);
                for (int y = 0; y < image.getHeight(); y++)
                    for (int x = 0; x < image.getWidth(); x++) image.setPixelRGBA(x, y, WyrmsteelPalette.recolourAbgr(image.getPixelRGBA(x, y)));
                textures.register(dragonArmourTexture(part, WyrmsteelDragonArmorItem.MATERIAL_NAME), new DynamicTexture(image));
            } catch (IOException ex) {
                VocanaCompat.LOG.warn("iceandfire: could not draw wyrmsteel dragon armour from {}", source, ex);
            }
        }
    }
}
