package fr.hyriode.hylios.server.template;

import fr.hyriode.hylios.util.IOUtil;
import fr.hyriode.hylios.util.References;
import fr.hyriode.hylios.util.YamlLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

/**
 * Created by AstFaster
 * on 30/10/2022 at 15:20
 */
public class TemplateManager {

    private final Map<String, Template> templates = new HashMap<>();

    public TemplateManager() {
        this.loadTemplates();
    }

    private void loadTemplates() {
        IOUtil.createDirectory(References.TEMPLATES_FOLDER);

        try (final Stream<Path> stream = Files.list(References.TEMPLATES_FOLDER)) {
            stream.forEach(this::loadTemplate);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private CompletableFuture<Template> loadTemplate(Path path) {
        if (path.toString().endsWith(".yml") || path.toString().endsWith(".yaml")) {
            final Template template = YamlLoader.load(path, Template.class);

            if (template != null) {
                this.templates.put(template.getName(), template);

                System.out.println("Loaded '" + template.getName() + "' template.");
            }
            return CompletableFuture.completedFuture(template);
        }
        return CompletableFuture.completedFuture(null);
    }

    public Template getTemplate(String name) {
        final Template template = this.templates.get(name);

        if (template == null) { // Try to load it from file
            Path path = Paths.get(References.TEMPLATES_FOLDER.toString(), name + ".yaml");

            if (!Files.exists(path)) {
                path = Paths.get(References.TEMPLATES_FOLDER.toString(), name + ".yml");
            }

            try {
                return this.loadTemplate(path).get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        return template;

    }

    public Map<String, Template> getTemplates() {
        return this.templates;
    }

}
