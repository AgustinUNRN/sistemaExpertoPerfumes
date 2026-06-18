package conceptos2026.controller;

import conceptos2026.dto.RecomendacionRequest;
import conceptos2026.dto.RecomendacionResponse;
import conceptos2026.service.CsvLoaderService;
import conceptos2026.service.PrologService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/perfumes")
@RequiredArgsConstructor
@Tag(name = "Perfumes", description = "Sistema experto de recomendación de perfumes")
public class PerfumeController {

    private final PrologService prologService;
    private final CsvLoaderService csvLoaderService;

    private String atom(String valor) {
        return valor
                .trim()
                .toLowerCase()
                .replace(" ", "_")
                .replace("-", "_");
    }

    @Operation(
            summary = "Recargar base de conocimiento"
    )
    @PostMapping("/recargar")
    public String recargar() {

        csvLoaderService.generarBaseConocimiento();

        return "Base de conocimiento regenerada";
    }

    @Operation(
            summary = "Buscar perfumes por familia olfativa"
    )
    @GetMapping("/familia/{familia}")
    public String buscarPorFamilia(
            @PathVariable String familia
    ) {

        String consulta =
                String.format(
                        "buscar_familia(%s,R),writeln(R)",
                        familia
                );

        return prologService.ejecutarConsulta(
                consulta
        );
    }

    @Operation(
            summary = "Obtener detalle de un perfume"
    )
    @GetMapping("/{nombre}/detalle")
    public String detallePerfume(
            @PathVariable String nombre
    ) {

        String consulta =
                String.format(
                        "detalle_perfume(%s,D),writeln(D)",
                        nombre
                );

        return prologService.ejecutarConsulta(
                consulta
        );
    }

    @Operation(
            summary = "Obtener perfumes similares"
    )
    @GetMapping("/{nombre}/similares")
    public String perfumesSimilares(
            @PathVariable String nombre,
            @RequestParam(defaultValue = "5") Integer cantidad
    ) {

        String consulta =
                String.format(
                        "perfumes_similares(%s,%d,R),writeln(R)",
                        nombre,
                        cantidad
                );

        return prologService.ejecutarConsulta(
                consulta
        );
    }

    @Operation(
            summary = "Obtener recomendaciones personalizadas"
    )
    @PostMapping("/recomendar")
    public List<RecomendacionResponse> recomendar(
            @RequestBody RecomendacionRequest request) {

        String familias =
                "[" +
                        request.getFamiliasFavoritas()
                                .stream()
                                .map(this::atom)
                                .collect(Collectors.joining(","))
                        +
                        "]";

        String evitar =
                "[" +
                        request.getEvitarTipos()
                                .stream()
                                .map(this::atom)
                                .collect(Collectors.joining(","))
                        +
                        "]";

        String consulta =
                String.format(
                        "recomendar(%s,%s,%d,%d,%s,%s,%d,R),writeln(R)",
                        familias,
                        evitar,
                        request.getPrecioMin(),
                        request.getPrecioMax(),
                        atom(request.getIntensidad()),
                        atom(request.getOcasion()),
                        request.getCantidad()
                );

        System.out.println("===== CONSULTA PROLOG =====");
        System.out.println(consulta);

        return prologService.recomendar(consulta);
    }

    @Operation(
            summary = "Obtener notas de un perfume"
    )
    @GetMapping("/{nombre}/notas")
    public String notasPerfume(
            @PathVariable String nombre
    ) {

        String consulta =
                String.format(
                        "todas_notas(%s,R),writeln(R)",
                        atom(nombre)
                );

        return prologService.ejecutarConsulta(
                consulta
        );
    }


    @GetMapping("/test-conexion")
    public String test() {
        return "FUNCIONA";
    }
}