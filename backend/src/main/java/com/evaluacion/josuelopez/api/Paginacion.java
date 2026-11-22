package com.evaluacion.josuelopez.api;

import com.evaluacion.josuelopez.excepcion.ReglaNegocioException;

public final class Paginacion {

    private Paginacion() {
    }

    public static void validar(int pagina, int tamano) {
        if (pagina < 0) {
            throw new ReglaNegocioException(400, "La página no puede ser negativa");
        }
        if (tamano < 1 || tamano > 100) {
            throw new ReglaNegocioException(400, "El tamaño de página debe estar entre 1 y 100");
        }
    }
}
