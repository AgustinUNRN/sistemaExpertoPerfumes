package conceptos2026.dto;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Data
public class RecomendacionRequest {

    @Schema(
            example = "[\"citrico\"]",
            description = "Familias olfativas preferidas"
    )
    private List<String> familiasFavoritas;

    @Schema(
            example = "[\"dulce\"]",
            description = "Etiquetas o características a evitar"
    )
    private List<String> evitarTipos;

    @Schema(example = "0")
    private Integer precioMin;

    @Schema(example = "200000")
    private Integer precioMax;

    @Schema(
            example = "media",
            description = "baja, media o alta"
    )
    private String intensidad;

    @Schema(
            example = "salidas",
            description = "diario, oficina, trabajo, salidas, noche o formal"
    )
    private String ocasion;

    @Schema(example = "5")
    private Integer cantidad;
}