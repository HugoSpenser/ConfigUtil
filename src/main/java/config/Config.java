package config;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import props.Env;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

/**
 * Утилитарный класс для работы с конфигурационными файлами с использованием в качестве ключей экземпляров
 * перечисляемого типа. Такой подход позволяет исключить ошибки доступа к свойствам в рантайме за счет
 * загрузки и валидации свойств на старте приложения
 *
 * @author Andrei Ogoltsov
 */
public class Config {

    private static final Logger LOG = LoggerFactory.getLogger(Config.class);
    private static final Map<Class<? extends Properties>, Map<Properties, String>> PROPS_BY_CLASS = new IdentityHashMap<>();
    private static final Map<Properties, String> ALL_PROPS = new IdentityHashMap<>();

    public static Map<Properties, String> getConfig(Class<? extends Properties> configClass) {
        return PROPS_BY_CLASS.get(configClass);
    }

    public static String getOrDefault(Properties prop, String defaultValue) {
        return ALL_PROPS.getOrDefault(prop, defaultValue);
    }

    public static String get(Properties prop) {
        return getOrDefault(prop, "");
    }

    public static boolean init() {
        List<String> errors = new ArrayList<>();
        Consumer<String> error = errors::add;

        // Ищем неаонотированные @ConfigFileName конфиг-классы
        final Map<Boolean, List<Class<? extends Properties>>> propClasses =
                new Reflections("", new SubTypesScanner(false)).getSubTypesOf(Properties.class)
                        .stream().collect(Collectors.partitioningBy(cl -> cl.isAnnotationPresent(ConfigFileName.class)));
        propClasses.get(false).stream().map(nac -> String.format(
                "Config class %s declared must be annotated via @ConfigFileName", nac.getTypeName())).forEach(error);

        // Обработка файлов свойств
        for (Class<? extends Properties> propClass : propClasses.get(true)) {
            String fileName;
            if (propClass.equals(Env.class)) {
                fileName = "env/" + System.getProperty("env", "dev");
            } else {
                fileName = propClass.getAnnotation(ConfigFileName.class).value();
            }

            final URL configFile = Config.class.getClassLoader().getResource(fileName + ".properties");
            if (configFile != null) {
                // Считываем свойства из файла и преобразуем в словарь
                List<String> lines;
                try {
                    lines = Files.readAllLines(
                            Paths.get(configFile.toExternalForm().replace("file:/", "")));
                } catch (IOException e) {
                    error.accept(String.format("Configuration file %s is not readable", fileName));
                    continue;
                }
                final Map<String, String> props = lines.stream().map(str -> str.split("=", 2))
                        .filter(arr -> arr.length > 1).collect(toMap(arr -> arr[0].trim(), arr -> arr[1].trim()));

                // Проверяеми наличие в файле всех обязательных свойств
                Arrays.stream(propClass.getEnumConstants()).filter(Properties::isRequired)
                        .map(Properties::getPropName).filter(propName -> !props.containsKey(propName))
                        .map(propName -> "Property %s not exists in file " + propName)
                        .forEach(error);

                final Map<Properties, String> properties = Arrays.stream(propClass.getEnumConstants())
                        .filter(p -> props.containsKey(p.getPropName()))
                        .collect(toMap(p -> p, p -> props.get(p.getPropName())));
                PROPS_BY_CLASS.put(propClass, properties);
                ALL_PROPS.putAll(properties);

            } else {
                error.accept(String.format("Configuration file %s is not exists", fileName));
            }
        }

        if (errors.isEmpty()) {
            LOG.info("All configs were successfully loaded!");
            return true;
        } else {
            LOG.warn("There were errors during config initialization!");
            errors.forEach(LOG::error);
            return false;
        }
    }

}
