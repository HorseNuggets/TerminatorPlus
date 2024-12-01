package net.nuggetmc.tplus.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.nuggetmc.tplus.TerminatorPlus;
import org.bukkit.Bukkit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;

/**
 * Used for debug logs.
 */
public class MCLogs {

    private static final String FORMAT =
            """
                    ====== TERMINATOR PLUS DEBUG INFO ======
                    Plugin Version: %s
                    Server Version: %s
                    Server Software: %s
                    Server Plugins: %s
                    Server TPS: %s
                    Memory: %s/%s
                                
                    Correct Version: %s
                    Required Version: %s
                    ====== TERMINATOR PLUS DEBUG INFO ======
                    """;

    public static String postInfo() throws IOException {
        String serverVersion = Bukkit.getVersion();
        String pluginVersion = TerminatorPlus.getVersion();
        String serverSoftware = Bukkit.getName();
        String serverPlugins = Arrays.stream(Bukkit.getPluginManager().getPlugins()).map(plugin -> plugin.getName() + " v" + plugin.getPluginMeta().getVersion()).reduce((s, s2) -> s + ", " + s2).orElse("No plugins");
        String serverTPS = Arrays.stream(Bukkit.getTPS()).mapToObj(tps -> String.format("%.2f", tps)).reduce((s, s2) -> s + ", " + s2).orElse("No TPS");
        String freeMemory = String.format("%.2f", (double) (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024) + "MB";
        String maxMemory = String.format("%.2f", (double) Runtime.getRuntime().maxMemory() / 1024 / 1024) + "MB";

        String info = String.format(FORMAT, pluginVersion, serverVersion, serverSoftware, serverPlugins, serverTPS, freeMemory, maxMemory, TerminatorPlus.isCorrectVersion(), TerminatorPlus.REQUIRED_VERSION);
        return pasteText(info);
    }

    private static String pasteText(String text) throws IOException {
        URL url = URI.create("https://api.mclo.gs/1/log").toURL(); // application/x-www-form-urlencoded
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            os.write(("content=" + text).getBytes());
        }

        String response = new BufferedReader(new InputStreamReader(connection.getInputStream())).readLine();
        JsonObject json = JsonParser.parseString(response).getAsJsonObject();
        return json.get("url").getAsString();
    }
}
