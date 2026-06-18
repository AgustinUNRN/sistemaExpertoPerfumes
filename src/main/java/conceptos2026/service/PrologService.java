package conceptos2026.service;

import conceptos2026.dto.RecomendacionResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PrologService {

    @Value("${prolog.file}")
    private String prologFile;

    public String ejecutarConsulta(String consulta) {

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

            Process process = processBuilder.start();

            String resultado =
                    new BufferedReader(
                            new InputStreamReader(process.getInputStream())
                    )
                            .lines()
                            .collect(Collectors.joining("\n"));

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
            String texto) {

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
    public List<RecomendacionResponse> recomendar(
            String consulta) {

        String resultado =
                ejecutarConsulta(consulta);
        return parsearRecomendaciones(resultado);
    }

}