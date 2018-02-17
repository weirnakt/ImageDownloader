package my.home.uploader.utils;

import java.io.FileReader;
import java.util.Map;
import java.util.Properties;

import com.esotericsoftware.yamlbeans.YamlReader;

/**
 *
 */
public class ModYamlReader {

    public Properties read(String filePath) {
        Properties result = new Properties();
        try (FileReader fileReader = new FileReader(filePath)) {
            YamlReader reader = new YamlReader(fileReader);
            Map map = (Map) reader.read();
            result.putAll(map);
        } catch (Exception e) {
            throw new RuntimeException("Не удалось прочитать yaml файл.", e);
        }
        return result;
    }
}
