package livr.validation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Load schema from file or classpath
 *
 * @author Gábor KOLÁROVICS
 * @since 2020/10/16
 */
public final class SchemaLoader {

	public static final String CLASSPATH_URL_PREFIX = "classpath:";

	public static final String FILE_URL_PREFIX = "file:";

	public static final String JSON_PREFIX = "{";

	private static final String PROPERTY_PREFIX = "$";

	private static Logger log = LoggerFactory.getLogger(SchemaLoader.class);

	private SchemaLoader() {
		throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
	}


	public static String load(@NotNull final String schema, Properties env) {
		if (schema.startsWith(JSON_PREFIX)) {
			return schema;
		} else if (schema.startsWith(CLASSPATH_URL_PREFIX)) {
			return getSchemaByClasspath(schema);
		} else if (schema.startsWith(FILE_URL_PREFIX)) {
			return getSchemaByFile(schema);
		} else if (env != null && schema.startsWith(PROPERTY_PREFIX)) {
			String property = env.getProperty(schema
					.replaceAll("\\$", "")
					.replaceAll("\\{", "")
					.replaceAll("}", "")
					.replaceAll(":", ""));
			if (property.startsWith(FILE_URL_PREFIX)) {
				return getSchemaByFile(property);
			} else if (property.startsWith(CLASSPATH_URL_PREFIX)) {
				return getSchemaByClasspath(property);
			}
		}
		log.warn(
				"LIVR Schema format can not recognize! Use 'classpath:', 'file:', external property inject character " +
						"'$' or json object open character.");
		return "{}";
	}

	private static String getSchemaByClasspath(@NotNull String schema) {
		InputStream stream = ClassLoader
				.getSystemResourceAsStream(schema.substring(CLASSPATH_URL_PREFIX.length(), schema.length()));
		final InputStreamReader in = new InputStreamReader(stream, StandardCharsets.UTF_8);
		return new BufferedReader(in).lines().collect(Collectors.joining("\n"));
	}

	private static String getSchemaByFile(@NotNull String schema) {
		try {
			return new String(
					Files.readAllBytes(Paths.get(schema.substring(FILE_URL_PREFIX.length(), schema.length()))),
					StandardCharsets.UTF_8);
		} catch (IOException e) {
			log.warn(e.getMessage(), e.getCause());
			return "{}";
		}
	}

}
