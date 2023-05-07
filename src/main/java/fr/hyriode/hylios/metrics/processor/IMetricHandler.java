package fr.hyriode.hylios.metrics.processor;

import fr.hyriode.hylios.metrics.data.IHyreosMetric;

import java.util.Set;

public interface IMetricHandler {

    Set<IHyreosMetric> process();
}
