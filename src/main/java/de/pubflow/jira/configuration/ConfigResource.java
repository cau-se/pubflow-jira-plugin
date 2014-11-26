package de.pubflow.jira.configuration;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.sal.api.user.UserManager;

@Path("/")
public class ConfigResource{

	private static ConfigResource configResource; 

	@XmlRootElement
	@XmlAccessorType(XmlAccessType.FIELD)
	public static final class Config{
		@XmlElement private String homedir;

		public String getHomedir(){
			return homedir;
		}

		public void setHomedir(String homedir){
			this.homedir = homedir;
		}
	}

	private final UserManager userManager;
	private final PluginSettingsFactory pluginSettingsFactory;
	private final TransactionTemplate transactionTemplate;

	public ConfigResource(UserManager userManager, PluginSettingsFactory pluginSettingsFactory, 
			TransactionTemplate transactionTemplate){
		this.userManager = userManager;
		this.pluginSettingsFactory = pluginSettingsFactory;
		this.transactionTemplate = transactionTemplate;
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public Response put(final Config config, @Context HttpServletRequest request){
		String username = userManager.getRemoteUsername(request);
		if (username == null || !userManager.isSystemAdmin(username)){
			return Response.status(Status.UNAUTHORIZED).build();
		}
		writeToSettings(config);
		return Response.noContent().build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response get(@Context HttpServletRequest request){
		String username = userManager.getRemoteUsername(request);

		if (username == null || !userManager.isSystemAdmin(username)){
			return Response.status(Status.UNAUTHORIZED).build();
		}

		return Response.ok(loadFromSettings()).build();
	}

	public Config loadFromSettings(){
		Config config = (Config) transactionTemplate.execute(new TransactionCallback(){
			public Object doInTransaction(){
				PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
				Config config = new Config();
				config.setHomedir((String) settings.get(Config.class.getName() + ".homedir"));

				return config;
			}
		});

		return config;
	}

	public void writeToSettings(final Config config){
		transactionTemplate.execute(new TransactionCallback(){
			public Object doInTransaction(){
				PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
				pluginSettings.put(Config.class.getName() + ".homedir", config.getHomedir());
				return null;
			}
		});
	}

	public static ConfigResource getInstance() throws Exception {
		if(configResource != null){
			throw new Exception("ConfigResource hasn't been initialized yet!");
		}else{
			return configResource;
		}
	}
}
