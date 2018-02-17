package my.home.uploader.service.impl;


import my.home.uploader.service.parser.ImageInfo;
import my.home.uploader.utils.CommonUtils;
import org.apache.commons.io.FileUtils;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.*;
import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@MessageDriven(name = "MyTopicMDB", activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = CommonUtils.MY_TOPIC),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic")
})
public class MessageCatcher implements MessageListener {

    private static final Logger LOGGER = Logger.getLogger(MessageCatcher.class.getName());

    @Override
    public void onMessage(Message message) {
        try {
            if (message instanceof TextMessage) {
                LOGGER.log(Level.INFO, ((TextMessage) message).getText());
            } else if (message instanceof MapMessage) {
                processMapMessage((MapMessage) message);
            }
        } catch (JMSException e) {
            LOGGER.log(Level.SEVERE, null, e);
        }
    }

    @SuppressWarnings("unchecked")
    private void processMapMessage(MapMessage message) throws JMSException {
        StringBuilder result = new StringBuilder();
        List<ImageInfo> images = (ArrayList<ImageInfo>) message.getObject("images");
        List<String> errorsLoad = (ArrayList<String>) message.getObject("errorsLoad");
        for (ImageInfo image : images) {
            String status = errorsLoad.contains(image.getFullName()) ? "ERROR" : "LOAD";
            result.append(String.format("-- link: %s%n--tags: %s%n--fileName: %s%n--status: %s%n%n", image.getLink(),
                    String.join(",", image.getTags()), image.getFullName(), status));
        }
        String saveDirectory = message.getString("saveDirectory");
        try {
            File resultFile = new File(saveDirectory, "result.txt");
            FileUtils.writeStringToFile(resultFile, result.toString(), Charset.forName("utf-8"));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, null, e);
        }
    }
}
