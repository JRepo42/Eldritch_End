package elocindev.eldritch_end.corruption;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import elocindev.eldritch_end.api.CorruptionAPI;
import elocindev.eldritch_end.config.entries.CorruptionConfig;
import elocindev.eldritch_end.worldgen.util.TextUtils;
import elocindev.eldritch_end.worldgen.util.TextUtils.Styles;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

public class CorruptionDisplayTooltip extends Item {
    public CorruptionDisplayTooltip(Settings settings) {
        super(settings);
    }
    
    @Override
    public Text getName() {
        return CorruptionAPI.getCMenuTitle();
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if (!world.isClient()) return;
        CorruptionConfig.CorruptionEffects effects = CorruptionAPI.CONFIG.corruption_effects;
        String effectKey = "eldritch_end.corruption.gui.effect.name.";

        boolean isShiftDown = Screen.hasShiftDown();
        if (isShiftDown) effectKey = "eldritch_end.corruption.gui.effect.description.";

        int resistance = ClientCorruption.getCorruptionResistanceLevel(); int corruption = ClientCorruption.getTotalCorruptionLevel();
        
        Style descriptionStyle = Style.EMPTY.withColor(0xc95f1c);
        Text baseCorruption = Text.literal(" ("+ClientCorruption.getCorruptionLevel()+")").setStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY).withItalic(true));

        if (resistance == 0 || !isShiftDown)
            baseCorruption = Text.empty();
        

        Text resistText = Text.literal("\uAB01 ").append(Text.literal(resistance+" Corruption Resistance").setStyle(TextUtils.Styles.CORRUPTION_RESISTANCE));
        Text corruptionText = Text.literal("\uA999 ").append(Text.literal(corruption+" Total Corruption").setStyle(TextUtils.Styles.DAMAGE_CORRUPTION).append(baseCorruption));
        
        tooltip.add(Text.translatable("eldritch_end.corruption.gui.desc.1").setStyle(descriptionStyle));
        tooltip.add(Text.translatable("eldritch_end.corruption.gui.desc.2").setStyle(descriptionStyle));
        
        tooltip.add(newLine());
        
        tooltip.add(resistText);
        tooltip.add(corruptionText);
        
        tooltip.add(newLine());
        
        addCorruptionEffect(tooltip, effectKey+"1", corruption, effects.received_damage_increment.getStartingLevel());
        addCorruptionEffect(tooltip, effectKey+"2", corruption, effects.tentacle_spawn.getStartingLevel());
        addCorruptionEffect(tooltip, effectKey+"3", corruption, effects.madness_vision.getStartingLevel());
        addCorruptionEffect(tooltip, effectKey+"4", corruption, effects.non_corruption_damage_reduction.getStartingLevel());
        addCorruptionEffect(tooltip, effectKey+"5", corruption, effects.ominous_eye_spawn.getStartingLevel());
        addCorruptionEffect(tooltip, effectKey+"6", corruption, effects.madness_consumed.getStartingLevel());

        tooltip.add(newLine());
        
        if (!isShiftDown)
            tooltip.add(Text.translatable("eldritch_end.corruption.gui.shift").setStyle(Style.EMPTY.withColor(Formatting.GRAY))); else tooltip.add(newLine());
        
    }

    public static void addCorruptionEffect(List<Text> tooltip, String translationKey, int currentCorruptionLevel, int effectThreshold) {
        if (effectThreshold == -1) return;

        TextColor color;

        if (currentCorruptionLevel >= effectThreshold)
            color = Styles.DAMAGE_CORRUPTION.getColor();
        else
            color = TextColor.fromFormatting(Formatting.DARK_GRAY);
        
        MutableText translatedText = Text.translatable(translationKey);

        translatedText = handleClientsidePlaceholders(translatedText, effectThreshold);

        tooltip.add(translatedText.setStyle(Style.EMPTY.withColor(color)));
    }

    public static MutableText handleClientsidePlaceholders(MutableText text, int effectThreshold) {
        CorruptionConfig.CorruptionEffects effects = CorruptionAPI.CONFIG.corruption_effects;

        String placeholders = text.getString()
        .replace("%COST%", effectThreshold < 100 ? " "+String.valueOf(effectThreshold) : String.valueOf(effectThreshold))
        .replace("%CORRUPTION%", String.valueOf(ClientCorruption.getCorruptionLevel()))
        .replace("%CORRUPTION_RESISTANCE%", String.valueOf(ClientCorruption.getCorruptionResistanceLevel()))
        .replace("%TAKEN_DAMAGE%", effects.received_damage_increment.getStartingLevel() + "%")
        .replace("%TENTACLE_CHANCE%", (int)effects.tentacle_spawn.getSpawnChance()*100 + "%")
        .replace("%TENTACLE_UPDATE_RATE%", effects.tentacle_spawn.getEffectRateSeconds() + "")
        .replace("%DAMAGE_REDUCTION%", (int)effects.non_corruption_damage_reduction.getDamagePercentage()*100 + "%")
        .replace("%EYES_TENTACLE_RATE%", effects.ominous_eye_spawn.getEffectRateSeconds() + "")
        .replace("%EYES_CHANCE%", (int)effects.ominous_eye_spawn.getSpawnChance()*100 + "%")
        .replace("%MADNESS_DAMAGE%", effects.madness_consumed.getStartingLevel() + "%");

        return Text.literal(placeholders).setStyle(text.getStyle());
    }

    private static Text newLine() {
        return Text.literal(" ");
    }
}