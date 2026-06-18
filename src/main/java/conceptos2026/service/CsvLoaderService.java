package conceptos2026.service;

import com.opencsv.CSVReader;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
public class CsvLoaderService {

    private static final String OUTPUT_FILE =
            "src/main/resources/prolog/base_conocimiento.pl";

    @PostConstruct
    public void generarBaseConocimiento() {

        try {

            StringBuilder prolog = new StringBuilder();

            cargarPerfumes(prolog);
            cargarNotas(prolog);
            cargarClasificaciones(prolog);

            Files.writeString(
                    Path.of(OUTPUT_FILE),
                    prolog.toString()
            );

            System.out.println(
                    "Base de conocimiento generada correctamente"
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Error generando base de conocimiento",
                    e
            );
        }
    }

    private void cargarPerfumes(
            StringBuilder prolog
    ) throws Exception {

        ClassPathResource resource =
                new ClassPathResource(
                        "csv/perfumes.csv"
                );

        try (
                CSVReader reader =
                        new CSVReader(
                                new FileReader(
                                        resource.getFile()
                                )
                        )
        ) {

            List<String[]> rows =
                    reader.readAll();

            for (int i = 1; i < rows.size(); i++) {

                String[] row = rows.get(i);

                prolog.append(
                        String.format(
                                "perfume(%s,%s,%s,%s,%s).%n",
                                row[0],
                                row[1],
                                row[2],
                                row[3],
                                row[4]
                        )
                );
            }
        }
    }

    private void cargarNotas(
            StringBuilder prolog
    ) throws Exception {

        ClassPathResource resource =
                new ClassPathResource(
                        "csv/notas.csv"
                );

        try (
                CSVReader reader =
                        new CSVReader(
                                new FileReader(
                                        resource.getFile()
                                )
                        )
        ) {

            List<String[]> rows =
                    reader.readAll();

            for (int i = 1; i < rows.size(); i++) {

                String[] row = rows.get(i);

                prolog.append(
                        String.format(
                                "nota_olfativa(%s,%s,%s).%n",
                                row[0],
                                row[1],
                                row[2]
                        )
                );
            }
        }
    }

    private void cargarClasificaciones(
            StringBuilder prolog
    ) throws Exception {

        ClassPathResource resource =
                new ClassPathResource(
                        "csv/clasificacion.csv"
                );

        try (
                CSVReader reader =
                        new CSVReader(
                                new FileReader(
                                        resource.getFile()
                                )
                        )
        ) {

            List<String[]> rows =
                    reader.readAll();

            for (int i = 1; i < rows.size(); i++) {

                String[] row = rows.get(i);

                prolog.append(
                        String.format(
                                "clasifica_nota(%s,%s).%n",
                                row[0],
                                row[1]
                        )
                );
            }
        }
    }
}