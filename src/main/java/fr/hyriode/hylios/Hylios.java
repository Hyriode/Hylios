package fr.hyriode.hylios;

import fr.hyriode.hylios.util.IOUtil;
import fr.hyriode.hylios.util.References;
import fr.hyriode.hylios.util.logger.ColoredLogger;

/**
 * Created by AstFaster
 * on 06/07/2022 at 20:42
 */
public class Hylios {

    private ColoredLogger logger;

    public void start() {
        ColoredLogger.printHeaderMessage();
        IOUtil.createDirectory(References.LOG_FOLDER);

        this.logger = new ColoredLogger(References.NAME, References.LOG_FILE);
    }

    public ColoredLogger getLogger() {
        return this.logger;
    }

}
