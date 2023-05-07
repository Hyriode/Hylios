package fr.hyriode.hylios.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.hyriode.api.config.MongoDBConfig;
import fr.hyriode.api.config.RedisConfig;
import fr.hyriode.hylios.config.nested.InfluxConfig;
import fr.hyriode.hylios.util.IOUtil;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by AstFaster
 * on 14/07/2022 at 13:09
 */
public record HyliosConfig(RedisConfig redis, MongoDBConfig mongo, InfluxConfig influx, int minLobbies, int minProxies, int minLimbos) {

    public static final Path CONFIG_FILE = Paths.get("config.json");

    public static HyliosConfig load() {
        System.out.println("Loading configuration...");

        final Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .serializeNulls()
                .create();

        final String json = IOUtil.loadFile(CONFIG_FILE);

        if (!json.equals("")) {
            return gson.fromJson(json, HyliosConfig.class);
        } else {
            final HyliosConfig config = new HyliosConfig(new RedisConfig("127.0.0.1", 6379, ""), new MongoDBConfig(null, null, "127.0.0.1", 27017), new InfluxConfig("http://127.0.0.1:8086", "", "", ""), 1, 1, 1);

            IOUtil.save(CONFIG_FILE, gson.toJson(config));

            System.err.println("Please fill configuration file before continue!");
            System.exit(0);

            return config;
        }
    }
}
