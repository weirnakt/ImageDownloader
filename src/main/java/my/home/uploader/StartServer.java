package my.home.uploader;

import java.io.File;
import java.util.Optional;
import java.util.Properties;

import my.home.uploader.rest.RestController;
import my.home.uploader.service.DownloadManagerService;
import my.home.uploader.service.impl.DownloadManagerServiceImpl;
import my.home.uploader.service.parser.HtmlParser;
import my.home.uploader.utils.CommonUtils;
import my.home.uploader.utils.ModYamlReader;
import my.home.uploader.utils.Validator;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.wildfly.swarm.Swarm;
import org.wildfly.swarm.jaxrs.JAXRSArchive;

import static my.home.uploader.utils.CommonUtils.*;

/**
 * Класс запуска сервера.
 */
public class StartServer {

    private static JAXRSArchive createDeployment() throws Exception {
        JAXRSArchive deployment = ShrinkWrap.create(JAXRSArchive.class, WAR_NAME);
        ClassLoader classLoader = StartServer.class.getClassLoader();
        deployment.addPackage(StartServer.class.getPackage())
                // обязательное добавление классов сущностей
                .addAsWebResource(new ClassLoaderAsset("index.jsp", classLoader), "index.jsp")
                .addClasses(CommonUtils.class, RestController.class, ModYamlReader.class)
                .addPackage(DownloadManagerService.class.getPackage())
                .addPackage(DownloadManagerServiceImpl.class.getPackage())
                .addPackage(HtmlParser.class.getPackage())
                .setContextRoot(CONTEXT_ROOT)
                .staticContent()
                .addAllDependencies();
        return deployment;
    }

    public static void main(String[] args) throws Exception {
        // валидация и чтение настроек
        Validator validator = new Validator();
        Properties settings = validator.validate();
        // задаём порты для запуска
        String httpPort = Optional.ofNullable(settings.getProperty(SWARM_HTTP_PORT_SETTING))
                .orElse(SWARM_HTTP_PORT_DEFAULT);
        String managmentPort = Optional.ofNullable(settings.getProperty(SWARM_MANAGMENT_PORT_SETTING))
                .orElse(SWARM_MANAGMENT_PORT_DEFAULT);
        System.setProperty(SWARM_HTTP_PORT_PROPERTY, httpPort);
        System.setProperty(SWARM_MANAGMENT_PORT_PROPERTY, managmentPort);
        // запуск
        new Swarm(false)
                .withConfig(new File(System.getProperty(CommonUtils.SETTINGS_FILE_PROPERY,
                        CommonUtils.SETTINGS_FILE)).toURI().toURL())
                .fraction(createUndertowFraction())
                .fraction(createMessageFraction())
                .fraction(createLoggingFraction(settings.getProperty(SWARM_LOG_SETTING)))
                .start()
                .deploy(createDeployment());
    }
}
