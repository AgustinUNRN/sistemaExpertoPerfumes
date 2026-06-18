package conceptos2026.service;

import conceptos2026.dto.RecomendacionRequest;
import conceptos2026.dto.RecomendacionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PrologService {

    @Value("${prolog.file}")
    private String prologFile;

    public String buscarPorFamilia(
            String familia
    ) {

        return ejecutarConsulta(
                String.format(
                        "buscar_familia(%s,R),writeln(R)",
                        atom(familia)
                )
        );
    }

    public String detallePerfume(
            String nombre
    ) {

        return ejecutarConsulta(
                String.format(
                        "detalle_perfume(%s,D),writeln(D)",
                        atom(nombre)
                )
        );
    }

    public String perfumesSimilares(
            String nombre,
            Integer cantidad
    ) {

        return ejecutarConsulta(
                String.format(
                        "perfumes_similares(%s,%d,R),writeln(R)",
                        atom(nombre),
                        cantidad
                )
        );
    }

    public String notasPerfume(
            String nombre
    ) {

        return ejecutarConsulta(
                String.format(
                        "todas_notas(%s,R),writeln(R)",
                        atom(nombre)
                )
        );
    }

    public List<RecomendacionResponse> recomendar(
            RecomendacionRequest request
    ) {

        String familias =
                construirListaProlog(
                        request.getFamiliasFavoritas()
                );

        String evitar =
                construirListaProlog(
                        request.getEvitarTipos()
                );

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

        log.info("Consulta Prolog: {}", consulta);

        return parsearRecomendaciones(
                ejecutarConsulta(
                        consulta
                )
        );
    }

    private String construirListaProlog(
            List<String> valores
    ) {

        if (valores == null || valores.isEmpty()) {
            return "[]";
        }

        return "[" +
                valores.stream()
                        .map(this::atom)
                        .collect(Collectors.joining(","))
                + "]";
    }

    private String atom(
            String valor
    ) {

        if (valor == null) {
            return "";
        }

        return valor
                .trim()
                .toLowerCase()
                .replace(" ", "_")
                .replace("-", "_");
    }

    public String ejecutarConsulta(
            String consulta
    ) {

        try {

            ProcessBuilder processBuilder =
                    new ProcessBuilder(
                            "swipl",
                            "-s",
                            prologFile,
                            "-g",
                            consulta,
                            "-t",
                            "halt"
                    );

            processBuilder.redirectErrorStream(true);

            Process process =
                    processBuilder.start();

            String resultado =
                    new BufferedReader(
                            new InputStreamReader(
                                    process.getInputStream()
                            )
                    )
                            .lines()
                            .collect(
                                    Collectors.joining("\n")
                            );

            process.waitFor();

            return resultado;

        } catch (Exception e) {

            throw new RuntimeException(
                    "Error ejecutando consulta Prolog",
                    e
            );
        }
    }

    private List<RecomendacionResponse> parsearRecomendaciones(
            String texto
    ) {

        List<RecomendacionResponse> recomendaciones =
                new ArrayList<>();

        Pattern pattern =
                Pattern.compile(
                        "recomendacion\\(([^,]+),([0-9.]+)\\)"
                );

        Matcher matcher =
                pattern.matcher(texto);

        while (matcher.find()) {

            recomendaciones.add(
                    new RecomendacionResponse(
                            matcher.group(1).trim(),
                            Double.parseDouble(
                                    matcher.group(2)
                            )
                    )
            );
        }

        return recomendaciones;
    }
}