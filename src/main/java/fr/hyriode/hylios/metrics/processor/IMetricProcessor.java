package fr.hyriode.hylios.metrics.processor;

import fr.hyriode.hylios.metrics.data.IHyreosMetric;

@FunctionalInterface
public interface IMetricProcessor {

    IHyreosMetric process();
}
