//package com.korosensei.simplerenderlib.mixin.early;
//
//import com.gtnewhorizon.gtnhmixins.IEarlyMixinLoader;
//import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
//@SuppressWarnings("unused")
//@IFMLLoadingPlugin.MCVersion("1.7.10")
//@IFMLLoadingPlugin.Name("SimpleAnimationLib")
//public class earlyPlugin implements IEarlyMixinLoader, IFMLLoadingPlugin {
//    @Override
//    public String getMixinConfig() {
//        return "mixins.simplerenderlib.early.json";
//    }
//
//    @Override
//    public List<String> getMixins(Set<String> loadedCoreMods) {
//        return new ArrayList<>();
//    }
//
//    @Override
//    public String[] getASMTransformerClass() {
//        return null;
//    }
//
//    @Override
//    public String getModContainerClass() {
//        return null;
//    }
//
//    @Override
//    public String getSetupClass() {
//        return "com.korosensei.simpleanimationlib.core.SimpleAnimationCore";
//    }
//
//    @Override
//    public void injectData(Map<String, Object> data) {}
//
//    @Override
//    public String getAccessTransformerClass() {
//        return null;
//    }
//}
