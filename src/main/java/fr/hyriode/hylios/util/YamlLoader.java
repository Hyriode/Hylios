package fr.hyriode.hylios.util;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.BeanAccess;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

/**
 * Created by AstFaster
 * on 30/12/2022 at 13:10
 */
public class YamlLoader {

    private static final Yaml YAML;

    static {
        final DumperOptions options = new DumperOptions();

        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setIndent(2);
        options.setPrettyFlow(false);
        options.setCanonical(false);

        YAML = new Yaml(new MapRepresenter(options), options);
        YAML.setBeanAccess(BeanAccess.FIELD);
    }

    public static <T> T load(Path path, Class<T> output) {
        if (!Files.exists(path)) {
            return null;
        }

        try (final InputStream inputStream = Files.newInputStream(path)) {
            return YAML.loadAs(inputStream, output);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T> void save(Path path, T object) {
        try (final PrintWriter writer = new PrintWriter(Files.newOutputStream(path))) {
            YAML.dump(object, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class MapRepresenter extends Representer {

        public MapRepresenter(DumperOptions options) {
            super(options);
        }

        @Override
        protected MappingNode representJavaBean(Set<Property> properties, Object javaBean) {
            if (!this.classTags.containsKey(javaBean.getClass())) {
                this.addClassTag(javaBean.getClass(), Tag.MAP);
            }
            return super.representJavaBean(properties, javaBean);
        }
    }

}
