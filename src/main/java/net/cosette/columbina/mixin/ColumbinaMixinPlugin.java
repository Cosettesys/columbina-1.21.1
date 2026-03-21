package net.cosette.columbina.mixin;

import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class ColumbinaMixinPlugin implements IMixinConfigPlugin {

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.contains("SafariSessionManagerMixin")
                || mixinClassName.contains("SafariCommandMixin")) {
            return FabricLoader.getInstance().isModLoaded("safari");
        }
        if (mixinClassName.contains("CobblemonListenersMixin")) {
            return FabricLoader.getInstance().isModLoaded("cobblemon-economy");
        }
        if (mixinClassName.contains("CobbleSafariMixin")) {
            return FabricLoader.getInstance().isModLoaded("cobblesafari");
        }
        return true;
    }
    @Override public void onLoad(String mixinPackage) {}
    @Override public String getRefMapperConfig() { return null; }
    @Override public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}
    @Override public List<String> getMixins() { return null; }
    @Override public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
    @Override public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}