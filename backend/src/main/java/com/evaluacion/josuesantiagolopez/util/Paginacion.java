package com.evaluacion.josuesantiagolopez.util;

import com.evaluacion.josuesantiagolopez.exception.BusinessException;

public final class Paginacion {

    public static final int TAMANO_MAXIMO = 100;

    private Paginacion() {
    }

    public static void validar(int page, int size) {
        if (page < 0) {
            throw new BusinessException(400, "PAGINA_INVALIDA", "La página debe ser mayor o igual a cero");
        }
        if (size < 1 || size > TAMANO_MAXIMO) {
            throw new BusinessException(400, "TAMANO_INVALIDO", "El tamaño de página debe estar entre 1 y 100");
        }
    }
}
