package com.evaluacion.josuesantiagolopez.repository;

import com.evaluacion.josuesantiagolopez.domain.Cuenta;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.LockModeType;
import java.util.Optional;

@ApplicationScoped
public class CuentaRepository implements PanacheRepository<Cuenta> {

    public Optional<Cuenta> findByNumeroCuenta(String numeroCuenta) {
        return find("numeroCuenta", numeroCuenta).firstResultOptional();
    }

    public Optional<Cuenta> findByIdForUpdate(Long id) {
        return find("id", id).withLock(LockModeType.PESSIMISTIC_WRITE).firstResultOptional();
    }
}
