package fr.hyriode.hylios.util;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by AstFaster
 * on 06/07/2022 at 20:41
 */
public class References {

    public static final String NAME = "Hylios";
    public static final String USER_DIR = System.getProperty("user.dir");

    /** Files */
    public static final Path LOG_FOLDER = Paths.get(USER_DIR, "logs");
    public static final Path LOG_FILE = Paths.get(LOG_FOLDER.toString(), "hylios.log");

}
