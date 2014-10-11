
package com.cardshifter.core.integration;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

/**
 *
 * @author Frank van Heeswijk
 */
final class RuntimeJarHelper {
	private RuntimeJarHelper() {
		throw new UnsupportedOperationException();
	}
	
	static String createModSourceString(final String className, final String extraImports, final String setupGameSource) {
		Objects.requireNonNull(className, "className");
		Objects.requireNonNull(extraImports, "extraImports");
		Objects.requireNonNull(setupGameSource, "setupGameSource");
		
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("package com.cardshifter.core.integration.throwaway.runtimemod;\n");
		stringBuilder.append("import com.cardshifter.modapi.base.ECSGame;\n");
		stringBuilder.append("import com.cardshifter.modapi.base.ECSMod;\n");
		stringBuilder.append(extraImports);
		stringBuilder.append("public final class ").append(className).append(" implements ECSMod {\n");
		stringBuilder.append("    @Override\n");
		stringBuilder.append("    public void setupGame(final ECSGame game) {\n");
		stringBuilder.append(setupGameSource);
		stringBuilder.append("    }\n");
		stringBuilder.append("}\n");
		return stringBuilder.toString();
	}
	
	static Path compileJavaSource(final Path sourceFile, final Path outputDirectory) throws IOException {
		Objects.requireNonNull(sourceFile, "sourceFile");
		Objects.requireNonNull(outputDirectory, "outputDirectory");
		if (sourceFile.getFileName().endsWith(".java")) {
			throw new IllegalArgumentException("sourceFile " + sourceFile + " must have .java as extension");
		}
		if (!Files.isDirectory(outputDirectory)) {
			throw new IllegalArgumentException("outputDirectory " + outputDirectory + " must be a directory");
		}
		
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, Locale.ENGLISH, StandardCharsets.UTF_8)) {
			fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Arrays.asList(outputDirectory.toFile()));
			
			Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjects(sourceFile.toFile());
			CompilationTask compilationTask = compiler.getTask(null, fileManager, null, null, null, compilationUnits);
			
			
			if (compilationTask.call()) {
				return outputDirectory.resolve("com/cardshifter/core/integration/throwaway/runtimemod").resolve(sourceFile.getFileName().toString().replace(".java", ".class"));
			}
			throw new IllegalStateException("Failed to compile java source file, compilation task failed");
		}
	}
	
	static void createJar(final Path jarFile, final List<Path> classes) throws IOException {
		Objects.requireNonNull(jarFile, "jarFile");
		Objects.requireNonNull(classes, "classes");
		
		try (FileOutputStream fileOutputStream = new FileOutputStream(jarFile.toFile())) {
			try (JarOutputStream jarOutputStream = new JarOutputStream(fileOutputStream)) {
				jarOutputStream.putNextEntry(new ZipEntry("com/cardshifter/core/integration/throwaway/runtimemod/"));
				for (Path clazz : classes) {
					jarOutputStream.putNextEntry(new ZipEntry("com/cardshifter/core/integration/throwaway/runtimemod/" + clazz.getFileName().toString()));
					jarOutputStream.write(Files.readAllBytes(clazz));
					jarOutputStream.closeEntry();
				}
			}
		}
	}
	
	static Path createProperties(final Path directory, final Properties properties) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, IOException {
		Objects.requireNonNull(directory, "directory");
		Objects.requireNonNull(properties, "properties");
		if (!Files.isDirectory(directory)) {
			throw new IllegalArgumentException("directory " + directory + " must be a directory");
		}
		Path configurationFile = directory.resolve(getConfigurationFileName());
		properties.store(Files.newOutputStream(configurationFile), "configuration");
		return configurationFile;
	}
	
	private static String getConfigurationFileName() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Class<?> clazz = Class.forName("com.cardshifter.core.modloader.ModLoaderHelper");
		Method method = clazz.getDeclaredMethod("getConfigurationFileName");
		method.setAccessible(true);
		return (String)method.invoke(null);
	}
}
