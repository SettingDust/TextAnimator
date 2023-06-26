package snownee.textanimator.mixin;

import java.util.Stack;

import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.StringDecomposer;
import snownee.textanimator.duck.TAStyle;
import snownee.textanimator.effect.Effect;

@Mixin(value = StringDecomposer.class, priority = 1200)
public abstract class StringDecomposerMixin {

	@Inject(method = "iterateFormatted(Ljava/lang/String;ILnet/minecraft/network/chat/Style;Lnet/minecraft/network/chat/Style;Lnet/minecraft/util/FormattedCharSink;)Z", at = @At("HEAD"), cancellable = true)
	private static void textanimator$iterateFormatted(String string, int i, Style style, Style style2, FormattedCharSink formattedCharSink, CallbackInfoReturnable<Boolean> cir) {
		int j = string.length();
		Style style3 = style;
		Stack<Effect> effects = new Stack<>();
		main:
		for (int k = i; k < j; ++k) {
			char d;
			char c = string.charAt(k);
			if (c == '§') {
				if (k + 1 >= j) break;
				d = string.charAt(k + 1);
				ChatFormatting chatFormatting = ChatFormatting.getByCode(d);
				if (chatFormatting != null) {
					style3 = chatFormatting == ChatFormatting.RESET ? style2 : style3.applyLegacyFormat(chatFormatting);
				}
				++k;
				continue;
			}
			if (c == '<') {
				StringBuilder sb = new StringBuilder();
				for (int l = k + 1; l < j; ++l) {
					char ch = string.charAt(l);
					if (ch == '>') {
						String[] split = StringUtils.split(sb.toString(), ' ');
						if (split.length == 0 || split[0].isEmpty()) break;
						boolean matched = false;
						if (split[0].charAt(0) == '/') {
							if (!effects.isEmpty() && effects.peek().getName().equals(sb.substring(1))) {
								effects.pop();
								matched = true;
							}
						} else {
							Effect effect = Effect.create(split);
							if (effect != null) {
								effects.push(effect);
								matched = true;
							}
						}
						if (matched) {
							style3 = style3.withClickEvent(style3.getClickEvent()); // clone a new one
							((TAStyle) style3).textanimator$setEffects(effects.stream().toList());
							k = l;
							continue main;
						} else {
							break;
						}
					}
					sb.append(ch);
				}
			}
			if (Character.isHighSurrogate(c)) {
				if (k + 1 >= j) {
					if (formattedCharSink.accept(k, style3, 65533)) break;
					cir.setReturnValue(false);
					return;
				}
				d = string.charAt(k + 1);
				if (Character.isLowSurrogate(d)) {
					if (!formattedCharSink.accept(k, style3, Character.toCodePoint(c, d))) {
						cir.setReturnValue(false);
						return;
					}
					++k;
					continue;
				}
				if (formattedCharSink.accept(k, style3, 65533)) continue;
				cir.setReturnValue(false);
				return;
			}
			if (feedChar(style3, formattedCharSink, k, c)) continue;
			cir.setReturnValue(false);
			return;
		}
		cir.setReturnValue(true);
	}

	@Shadow
	private static boolean feedChar(Style style, FormattedCharSink formattedCharSink, int i, char c) {
		throw new AssertionError();
	}
}
