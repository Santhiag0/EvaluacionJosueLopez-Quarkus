package com.evaluacion.josuesantiagolopez.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import java.time.Clock;
import java.time.ZoneId;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class RelojProducer {

    @Produces
    @ApplicationScoped
    public Clock reloj(@ConfigProperty(name = "app.zona-horaria", defaultValue = "America/Guayaquil") String zona) {
        return Clock.system(ZoneId.of(zona));
    }
}
