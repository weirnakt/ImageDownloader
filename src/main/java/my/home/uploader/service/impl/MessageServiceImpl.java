package my.home.uploader.service.impl;

import my.home.uploader.service.MessageService;
import my.home.uploader.service.parser.ImageInfo;
import my.home.uploader.utils.CommonUtils;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Topic;
import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MessageServiceImpl implements MessageService {

    private static final Logger LOGGER = Logger.getLogger(MessageServiceImpl.class.getName());

    @Inject
    private JMSContext context;

    @Resource(lookup = CommonUtils.MY_TOPIC)
    private Topic topic;

    @Override
    public void sendMessage(String message) {
        context.createProducer().send(topic, context.createTextMessage(message));
    }

    @Override
    public void createResultFile(Set<ImageInfo> images, DownloadInfo downloadInfo) {
        MapMessage message = context.createMapMessage();
        try {
            message.setObject("images", images);
            message.setObject("errorsLoad", downloadInfo.getErrorLoad());
            message.setString("saveDirectory", downloadInfo.getSaveDirectory());
            context.createProducer().send(topic, message);
        } catch (JMSException e) {
            LOGGER.log(Level.SEVERE, null, e);
        }
    }
}
