package fr.hyriode.hylios.metrics.data.money;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import fr.hyriode.hylios.metrics.data.IHyreosMetric;

import java.time.Instant;

@Measurement(name = "circulating_money")
public class CirculatingMoney implements IHyreosMetric {

    @Column(name = "money_type", tag = true)
    private final MoneyType type;
    @Column(name = "amount")
    private final long amount;
    @Column(timestamp = true)
    private final Instant time;

    public CirculatingMoney(MoneyType type, long amount) {
        this.type = type;
        this.amount = amount;

        this.time = Instant.now();
    }
}
