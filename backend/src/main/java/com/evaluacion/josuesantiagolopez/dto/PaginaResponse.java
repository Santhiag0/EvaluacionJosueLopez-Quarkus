package com.evaluacion.josuesantiagolopez.dto;

import java.util.List;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Página de resultados")
public class PaginaResponse<T> {

    public List<T> content;

    @Schema(example = "0")
    public int page;

    @Schema(example = "20")
    public int size;

    @Schema(example = "42")
    public long totalElements;

    @Schema(example = "3")
    public int totalPages;

    public static <T> PaginaResponse<T> of(List<T> content, int page, int size, long totalElements) {
        PaginaResponse<T> pagina = new PaginaResponse<>();
        pagina.content = content;
        pagina.page = page;
        pagina.size = size;
        pagina.totalElements = totalElements;
        pagina.totalPages = size == 0 ? 0 : (int) Math.ceil(totalElements / (double) size);
        return pagina;
    }
}
