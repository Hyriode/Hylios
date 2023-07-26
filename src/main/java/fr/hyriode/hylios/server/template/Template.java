package fr.hyriode.hylios.server.template;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by AstFaster
 * on 03/05/2023 at 15:26
 */
public class Template {

    private String name;

    private Mode defaultMode;
    private Map<String, Mode> modes = new HashMap<>();

    public Template() {}

    public Template(String name, Mode defaultMode, Map<String, Mode> modes) {
        this.name = name;
        this.defaultMode = defaultMode;
        this.modes = modes;
    }

    public String getName() {
        return this.name;
    }

    public Mode getMode(String mode) {
        return this.getModes().getOrDefault(mode, this.defaultMode);
    }

    public Mode getDefaultMode() {
        return this.defaultMode;
    }

    public Map<String, Mode> getModes() {
        return this.modes == null ? this.modes = new HashMap<>() : this.modes;
    }

    public static class Mode {

        private Resources resources;
        private Resources hostResources;
        private int poolSize;

        private Mode() {}

        public Mode(Resources resources, Resources hostResources, int poolSize) {
            this.resources = resources;
            this.hostResources = hostResources;
            this.poolSize = poolSize;
        }

        public Resources getResources() {
            return this.resources;
        }

        public Resources getHostResources() {
            return this.hostResources;
        }

        public int getPoolSize() {
            return this.poolSize;
        }

    }

    public static class Resources {

        private String maxMemory;
        private String minMemory;
        private double cpus;

        public Resources() {}

        public Resources(String maxMemory, String minMemory, double cpus) {
            this.maxMemory = maxMemory;
            this.minMemory = minMemory;
            this.cpus = cpus;
        }

        public String getMaxMemory() {
            return this.maxMemory;
        }

        public String getMinMemory() {
            return this.minMemory;
        }

        public double getCpus() {
            return this.cpus;
        }

    }

}
