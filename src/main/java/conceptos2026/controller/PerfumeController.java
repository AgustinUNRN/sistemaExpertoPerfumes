package conceptos2026.controller;

import conceptos2026.dto.RecomendacionRequest;
import conceptos2026.dto.RecomendacionResponse;
import conceptos2026.service.CsvLoaderService;
import conceptos2026.service.PrologService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/perfumes")
@RequiredArgsConstructor
@Tag(
        name = "Perfumes",
        description = "Sistema experto de recomendación de perfumes"
)
public class PerfumeController {

    private final PrologService prologService;
    private final CsvLoaderService csvLoaderService;

    @Operation(summary = "Recargar base de conocimiento")
    @PostMapping("/recargar")
    public String recargar() {

        csvLoaderService.generarBaseConocimiento();

        return "Base de conocimiento regenerada";
    }

    @Operation(summary = "Buscar perfumes por familia olfativa")
    @GetMapping("/familia/{familia}")
    public String buscarPorFamilia(
            @PathVariable String familia
    ) {
        return prologService.buscarPorFamilia(familia);
    }

    @Operation(summary = "Obtener detalle de un perfume")
    @GetMapping("/{nombre}/detalle")
    public String detallePerfume(
            @PathVariable String nombre
    ) {
        return prologService.detallePerfume(nombre);
    }

    @Operation(summary = "Obtener perfumes similares")
    @GetMapping("/{nombre}/similares")
    public String perfumesSimilares(
            @PathVariable String nombre,
            @RequestParam(defaultValue = "5") Integer cantidad
    ) {
        return prologService.perfumesSimilares(
                nombre,
                cantidad
        );
    }

    @Operation(summary = "Obtener notas de un perfume")
    @GetMapping("/{nombre}/notas")
    public String notasPerfume(
            @PathVariable String nombre
    ) {
        return prologService.notasPerfume(nombre);
    }

    @Operation(summary = "Obtener recomendaciones personalizadas")
    @PostMapping("/recomendar")
    public List<RecomendacionResponse> recomendar(
            @RequestBody RecomendacionRequest request
    ) {
        return prologService.recomendar(request);
    }

    @GetMapping("/test-conexion")
    public String test() {
        return "FUNCIONA";
    }
}