package com.evaluacion.josuelopez.config;

import java.time.Clock;
import java.time.ZoneId;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class RelojProducer {

    @ConfigProperty(name = "app.business.zone", defaultValue = "America/Bogota")
    String zona;

    @Produces
    @Singleton
    public Clock relojDeNegocio() {
        return Clock.system(ZoneId.of(zona));
    }
}
