package com.ilummc.wayback;

import com.ilummc.wayback.cmd.WaybackTabCompleter;
import com.ilummc.wayback.schedules.WaybackSchedules;
import io.izzel.taboolib.loader.Plugin;
import io.izzel.taboolib.loader.PluginBoot;
import io.izzel.taboolib.module.dependency.Dependency;
import io.izzel.taboolib.module.inject.TInject;
import io.izzel.taboolib.module.locale.TLocale;
import io.izzel.taboolib.module.locale.logger.TLogger;
import org.bukkit.Bukkit;

@Dependency(
        maven = "it.sauronsoftware:ftp4j:1.7.2",
        url = "http://repo.ptms.ink/repository/maven-releases/public/ftp4j/1.7.2/ftp4j-1.7.2.jar"
)
@Dependency(
        maven = "org.codehaus.jackson:jackson-core-asl:1.9.13",
        url = "https://skymc.oss-cn-shanghai.aliyuncs.com/libs/jackson-core-asl-1.9.13.jar"
)
@Dependency(
        maven = "org.codehaus.jackson:jackson-mapper-asl:1.9.13",
        url = "https://skymc.oss-cn-shanghai.aliyuncs.com/libs/jackson-mapper-asl-1.9.13.jar"
)
@Dependency(
        maven = "net.lingala.zip4j:zip4j:1.3.2",
        url = "https://skymc.oss-cn-shanghai.aliyuncs.com/libs/zip4j-1.3.2.jar"
)
public final class Wayback extends Plugin {

    public static final Wayback INSTANCE = new Wayback();

    @TInject
    private static TLogger logger;

    private boolean loaded = false;

    private boolean disabling = false;

    public static WaybackSchedules getSchedules() {
        return WaybackSchedules.instance();
    }

    public static WaybackConf getConf() {
        return WaybackConf.getConf();
    }

    public static TLogger logger() {
        return logger;
    }

    public static PluginBoot instance() {
        return INSTANCE.getPlugin();
    }

    public static boolean isDisabling() {
        return INSTANCE.disabling;
    }

    public static boolean reload() {
        try {
            INSTANCE.onDisable();
            WaybackConf.getConf().cleanSchedules();
            WaybackSchedules.renew();
            Wayback.instance().reloadConfig();
            TLocale.reload();
            INSTANCE.onEnable();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onEnable() {
        if (!loaded)
            try {
                DelegatedWayback.onEnable();
                getPlugin().getCommand("wayback").setTabCompleter(new WaybackTabCompleter());
                loaded = true;
            } catch (Throwable t) {
                TLocale.Logger.fatal("ERR_LOAD_WAYBACK");
                t.printStackTrace();
                Bukkit.getPluginManager().disablePlugin(getPlugin());
            }
        else loaded = false;
    }

    @SuppressWarnings("BusyWait")
    @Override
    public void onDisable() {
        while (disabling) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ignored) {
            }
        }
        if (loaded) {
            loaded = false;
            disabling = true;
            DelegatedWayback.onDisable();
            disabling = false;
        }
    }
}
