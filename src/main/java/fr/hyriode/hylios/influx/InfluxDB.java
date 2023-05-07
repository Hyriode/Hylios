package fr.hyriode.hylios.influx;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import fr.hyriode.api.HyriAPI;
import fr.hyriode.hylios.config.nested.InfluxConfig;
import fr.hyriode.hylios.util.References;
import fr.hyriode.hylios.metrics.data.IHyreosMetric;

import java.util.function.Consumer;

/**
 * Created by AstFaster
 * on 14/10/2022 at 19:40
 */
public class InfluxDB {

    private final WriteApiBlocking writeApi;
    private final InfluxDBClient client;

    public InfluxDB(InfluxConfig config) {
        this.client = InfluxDBClientFactory.create(config.getUrl(), config.getToken().toCharArray(), config.getOrganization(), config.getBucket());

        try {
            if (this.client.ping()) {
                System.out.println("Connection set between " + References.NAME + " and InfluxDB");
            } else {
                System.err.println("Couldn't connect to InfluxDB");
            }
        } catch (Exception ignored) {
            System.exit(-1);
        }

        this.writeApi = this.client.getWriteApiBlocking();
    }

    public void write(Consumer<WriteApiBlocking> consumer) {
        HyriAPI.get().getScheduler().runAsync(() -> consumer.accept(this.writeApi));
    }

    public void sendMetric(IHyreosMetric metric) {
        this.write(api -> api.writeMeasurement(WritePrecision.MS, metric));
    }

    public void stop() {
        this.client.close();
    }
}
