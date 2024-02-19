package snownee.textanimator.util;

import java.util.Locale;
import java.util.function.Function;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkConstants;
import snownee.textanimator.TextAnimatorClient;
import snownee.textanimator.compat.HermesCompat;
import snownee.textanimator.effect.Effect;
import snownee.textanimator.effect.params.Params;

@Mod("textanimator")
public class ClientProxy {
	private static final boolean hermes = ModList.get().isLoaded("hermes");

	public static void onEffectTypeRegistered(String type, Function<Params, Effect> factory) {
		if (hermes) {
			HermesCompat.onEffectTypeRegistered(type, factory);
		}
	}

	public ClientProxy() {
		ModLoadingContext.get().registerExtensionPoint(
				IExtensionPoint.DisplayTest.class,
				() -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (a, b) -> true));
		if (FMLEnvironment.dist.isClient()) {
			TextAnimatorClient.init();
		}
	}

	public static Locale getLocale() {
		return Minecraft.getInstance().getLocale();
	}
}
