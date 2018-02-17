package my.home.uploader.rest;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import my.home.uploader.service.ConfigurationService;
import my.home.uploader.service.DownloadManagerService;
import my.home.uploader.service.MessageService;
import net.sf.json.JSONObject;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

/**
 * Основной rest-контроллер.
 */
@Path("/download")
public class RestController {

    private static final Logger LOGGER = Logger.getLogger(RestController.class.getName());

    @Inject
    private DownloadManagerService downloadManager;

    @Inject
    private ConfigurationService configuration;

    @Inject
    private MessageService messageService;

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response post(MultipartFormDataInput formData) {
        try {
            String result = downloadManager.download(formData);
            return Response.ok().entity(result).build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, null, e);
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("test")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get() {
        JSONObject result = new JSONObject();
        try {
            result.element("test", "ok");
            result.element("port", configuration.getHttpPort());
            messageService.sendMessage(result.toString());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, null, e);
            return Response.serverError().entity(e.getMessage()).build();
        }
        return Response.ok().entity(result).build();
    }
}
