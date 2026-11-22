package com.evaluacion.josuelopez.repositorio;

import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.LockModeType;

import com.evaluacion.josuelopez.dominio.Cuenta;

import io.quarkus.hibernate.orm.panache.PanacheRepository;

@ApplicationScoped
public class CuentaRepository implements PanacheRepository<Cuenta> {

    public Optional<Cuenta> buscarPorNumero(String numeroCuenta) {
        return find("numeroCuenta", numeroCuenta).firstResultOptional();
    }

    public boolean existeNumero(String numeroCuenta) {
        return count("numeroCuenta", numeroCuenta) > 0;
    }

    public Cuenta bloquearPorId(Long id) {
        return find("id = ?1", id).withLock(LockModeType.PESSIMISTIC_WRITE).firstResult();
    }
}
