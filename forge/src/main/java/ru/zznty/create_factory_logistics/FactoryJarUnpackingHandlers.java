package ru.zznty.create_factory_logistics;

import com.simibubi.create.api.registry.SimpleRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.Tags;
import ru.zznty.create_factory_logistics.logistics.jar.unpack.JarUnpackingHandler;

import java.util.Optional;

public class FactoryJarUnpackingHandlers {
    public static void register() {

        Optional<Fluid> ceiExperience = BuiltInRegistries.FLUID.getOptional(
                ResourceLocation.fromNamespaceAndPath("create_enchantment_industry", "experience"));
        ceiExperience.ifPresent(fluidExperience -> JarUnpackingHandler.REGISTRY.register(
                fluidExperience, (level, pos, fluid, player) -> {
                    // default is 1mb -> 1xp
                    int experience = fluid.getAmount();
                    if (player != null) {
                        player.giveExperiencePoints(experience);
                    } else {
                        ExperienceOrb.award(level, pos.getCenter(), experience);
                    }
                    return true;
                }));

        JarUnpackingHandler.REGISTRY.registerProvider(
                SimpleRegistry.Provider.forFluidTag(Tags.Fluids.EXPERIENCE, (level, pos, fluid, player) -> {
                    // default is 20mb -> 1xp
                    int experience = fluid.getAmount() / 20;
                    if (player != null) {
                        player.giveExperiencePoints(experience);
                    } else {
                        ExperienceOrb.award(level, pos.getCenter(), experience);
                    }
                    return true;
                }));
    }
}
