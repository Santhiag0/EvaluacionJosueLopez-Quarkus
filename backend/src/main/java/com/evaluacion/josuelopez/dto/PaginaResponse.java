package com.evaluacion.josuelopez.dto;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Página de resultados")
public record PaginaResponse<T>(
        List<T> contenido,
        @Schema(description = "Índice de página, empezando en 0", example = "0") int pagina,
        @Schema(example = "10") int tamano,
        @Schema(example = "42") long totalElementos,
        @Schema(example = "5") int totalPaginas) {

    public static <T> PaginaResponse<T> de(List<T> contenido, int pagina, int tamano, long totalElementos) {
        int totalPaginas = tamano <= 0 ? 0 : (int) Math.ceil((double) totalElementos / tamano);
        return new PaginaResponse<>(contenido, pagina, tamano, totalElementos, totalPaginas);
    }
}
