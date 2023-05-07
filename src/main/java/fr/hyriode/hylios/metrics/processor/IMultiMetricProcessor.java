package fr.hyriode.hylios.metrics.processor;

import fr.hyriode.hylios.metrics.data.IHyreosMetric;

import java.util.Set;

@FunctionalInterface
public interface IMultiMetricProcessor {

    Set<IHyreosMetric> process();
}
