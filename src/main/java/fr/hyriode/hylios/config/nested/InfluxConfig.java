package fr.hyriode.hylios.config.nested;

/**
 * Created by AstFaster
 * on 14/10/2022 at 19:47
 */
public class InfluxConfig {

    private final String url;
    private final String token;
    private final String organization;
    private final String bucket;

    public InfluxConfig(String url, String token, String organization, String bucket) {
        this.url = url;
        this.token = token;
        this.organization = organization;
        this.bucket = bucket;
    }

    public String getUrl() {
        return this.url;
    }

    public String getToken() {
        return this.token;
    }

    public String getOrganization() {
        return this.organization;
    }

    public String getBucket() {
        return this.bucket;
    }
}
