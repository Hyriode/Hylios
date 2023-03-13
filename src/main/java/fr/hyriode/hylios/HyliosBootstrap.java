package fr.hyriode.hylios;

import fr.hyriode.hylios.util.References;

import java.io.IOException;

/**
 * Created by AstFaster
 * on 06/07/2022 at 20:40
 */
public class HyliosBootstrap {

    public static void main(String[] args) throws IOException {
        if (Float.parseFloat(System.getProperty("java.class.version")) < 62.0D) {
            System.err.println("*** ERROR *** " + References.NAME + " requires Java >= 18 to function!");
            return;
        }

        new Hylios().start();
    }

}
