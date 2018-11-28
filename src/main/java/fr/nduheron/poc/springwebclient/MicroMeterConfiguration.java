package fr.nduheron.poc.springwebclient;

import java.util.stream.StreamSupport;

import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Statistic;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

@Configuration
public class MicroMeterConfiguration {

	@Autowired
	private MeterRegistry registry;

	private static final char SEPARATOR = ';';
	private static final char NEW_LINE = '\n';
	private static final char QUOTE = '"';
	private static final char SPACE = ' ';
	private static final String EMPTY = "";

	@Bean
	MeterRegistry meterRegistry() {
		return new SimpleMeterRegistry();
	}

	@PreDestroy
	public void logMonitoring() throws InterruptedException {

		// on attend pour être sur que la registry est alimenté
		Thread.sleep(500);

		StringBuilder sb = new StringBuilder();
		registry.getMeters().stream().filter(m -> m.getId().getName().equals("webclient")).forEach(meter -> {
			sb.append(QUOTE).append(meter.getId().getTag("method")).append(SPACE).append(meter.getId().getTag("uri"))
					.append(SPACE).append(meter.getId().getTag("status")).append(QUOTE).append(SEPARATOR);
			sb.append(QUOTE).append(meter.getId().getBaseUnit()).append(QUOTE).append(SEPARATOR);
			Iterable<Measurement> measure = meter.measure();
			sb.append(QUOTE).append(getStatisticValue(measure, Statistic.COUNT)).append(QUOTE).append(SEPARATOR);
			sb.append(QUOTE).append(getStatisticValue(measure, Statistic.TOTAL_TIME)).append(QUOTE).append(SEPARATOR);
			sb.append(QUOTE).append(getStatisticValue(measure, Statistic.MAX)).append(QUOTE).append(NEW_LINE);
		});
		System.out.println(sb.toString());
	}

	private String getStatisticValue(Iterable<Measurement> measure, Statistic statistic) {
		return StreamSupport.stream(measure.spliterator(), false).filter(m -> m.getStatistic() == statistic).findFirst()
				.map(m -> String.valueOf(m.getValue())).orElse(EMPTY);
	}

}
