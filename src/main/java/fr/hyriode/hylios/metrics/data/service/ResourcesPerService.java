package fr.hyriode.hylios.metrics.data.service;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import fr.hyriode.hyggdrasil.api.service.IHyggServiceResources;
import fr.hyriode.hylios.metrics.data.IHyreosMetric;

import java.time.Instant;
import java.util.List;

@Measurement(name = "resources_per_service")
public class ResourcesPerService implements IHyreosMetric {

    @Column(name = "service_type", tag = true)
    private final ServiceType type;
    @Column(name = "service_name", tag = true)
    private final String name;

    @Column(name = "total_cpu_usage")
    private final long totalCpuUsage;
    @Column(name = "cpu_usages")
    private final List<Long> cpuUsages;
    @Column(name = "system_cpu_usage")
    private final long systemCpuUsage;
    @Column(name = "available_cpus")
    private final long availableCpus;

    @Column(name = "memory_usage")
    private final long memoryUsage;
    @Column(name = "memory_max")
    private final long memoryMax;
    @Column(name = "memory_limit")
    private final long memoryLimit;

    @Column(timestamp = true)
    private final Instant time;

    public ResourcesPerService(ServiceType type, String name, IHyggServiceResources resources) {
        this.type = type;
        this.name = name;

        this.totalCpuUsage = resources.getTotalCpuUsage();
        this.cpuUsages = resources.getCpuUsages();
        this.systemCpuUsage = resources.getSystemCpuUsage();
        this.availableCpus = resources.getAvailableCpus();

        this.memoryUsage = resources.getMemoryUsage();
        this.memoryMax = resources.getMemoryMax();
        this.memoryLimit = resources.getMemoryLimit();

        this.time = Instant.now();
    }

    public ResourcesPerService(ServiceType type, String name, long totalCpuUsage, List<Long> cpuUsages, long systemCpuUsage, long availableCpus, long memoryUsage, long memoryMax, long memoryLimit) {
        this.type = type;
        this.name = name;
        this.totalCpuUsage = totalCpuUsage;
        this.cpuUsages = cpuUsages;
        this.systemCpuUsage = systemCpuUsage;
        this.availableCpus = availableCpus;
        this.memoryUsage = memoryUsage;
        this.memoryMax = memoryMax;
        this.memoryLimit = memoryLimit;

        this.time = Instant.now();
    }
}
