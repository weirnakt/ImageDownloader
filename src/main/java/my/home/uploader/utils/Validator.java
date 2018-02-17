package my.home.uploader.utils;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;

/**
 * Валидатор возможности запуска приложения.
 * Пока проверяет только наличие файлов конфигурации в каталоге запуска.
 */
public class Validator {

    public Validator() {
        //
    }

    private void checkPropertiesFile(File file, StringBuilder errors) {
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream(file)) {
            properties.load(fis);
            if (properties.isEmpty()) {
                errors.append(String.format("Файл свойств \"%s\" пустой.%n", file.getName()));
            }
        } catch (Exception e) {
            errors.append(String.format("Во время чтения файла свойств \"%s\" произошла ошибка: %s%n", file.getName(),
                    e.toString()));
        }
    }

    private void checkYmlFile(String fileName, StringBuilder errors) {
        Properties properties = null;
        try {
            properties = new ModYamlReader().read(fileName);
            if (properties.isEmpty()) {
                errors.append(String.format("Файл конфигурации \"%s\" пустой.%n", fileName));
            }
        } catch (Exception e) {
            errors.append(String.format("Во время чтения файла конфигурации \"%s\" произошла ошибка: %s%n", fileName,
                    e.toString()));
        }
    }

    private boolean checkExistFile(File file, StringBuilder errors) {
        if (!file.exists()) {
            errors.append(String.format("Отсуствует обязательный файл \"%s\".%n", file.getName()));
        }
        return file.exists();
    }

    private String validateFile(List<String> fileNames) {
        StringBuilder errors = new StringBuilder();
        fileNames.stream().forEach(fileName -> {
            File checkFile = new File(fileName);
            switch (FilenameUtils.getExtension(fileName)) {
                case "properties":
                    if (checkExistFile(checkFile, errors)) {
                        checkPropertiesFile(checkFile, errors);
                    }
                    break;
                case "yml":
                    if (checkExistFile(checkFile, errors)) {
                        checkYmlFile(checkFile.getAbsolutePath(), errors);
                    }
                    break;
                default:
                    checkExistFile(checkFile, errors);
                    break;
            }
        });
        return errors.toString();
    }

    private String validateSettings(Properties settings, List<String> requireds) {
        StringBuilder errors = new StringBuilder();
        requireds.stream().forEach(required -> {
            if (!settings.containsKey(required)) {
                errors.append(String.format("Отсуствует обязательная настройка \"%s\" в файле настроек%n", required));
            }
        });
        return errors.toString();
    }

    public Properties validate() {
        String settingsFile = System.getProperty(CommonUtils.SETTINGS_FILE_PROPERY, CommonUtils.SETTINGS_FILE);
        List<String> validateFiles = new ArrayList<>();
        validateFiles.add(settingsFile);
        String errors = validateFile(validateFiles);
        if (!errors.isEmpty()) {
            throw new RuntimeException(errors);
        }
        Properties result = new ModYamlReader().read(settingsFile);
        List<String> requiredSettings = new ArrayList<>();
        requiredSettings.add(CommonUtils.SWARM_LOG_SETTING);
        String errorsSettings = validateSettings(result, requiredSettings);
        if (!errorsSettings.isEmpty()) {
            throw new RuntimeException(errorsSettings);
        }
        return result;
    }

}
