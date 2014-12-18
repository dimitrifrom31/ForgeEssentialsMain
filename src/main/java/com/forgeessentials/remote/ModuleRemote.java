package com.forgeessentials.remote;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

import net.minecraftforge.common.config.Configuration;

import com.forgeessentials.api.remote.RemoteHandler;
import com.forgeessentials.api.remote.RemoteManager;
import com.forgeessentials.api.remote.RemoteSession;
import com.forgeessentials.core.ForgeEssentials;
import com.forgeessentials.core.moduleLauncher.FEModule;
import com.forgeessentials.core.moduleLauncher.config.IConfigLoader.ConfigLoaderBase;
import com.forgeessentials.util.OutputHandler;
import com.forgeessentials.util.events.FEModuleEvent.FEModuleInitEvent;
import com.forgeessentials.util.events.FEModuleEvent.FEModuleServerInitEvent;
import com.forgeessentials.util.events.FEModuleEvent.FEModuleServerStopEvent;
import com.google.gson.JsonObject;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

@FEModule(name = "Remote", parentMod = ForgeEssentials.class, canDisable = true)
public class ModuleRemote extends ConfigLoaderBase implements RemoteManager {

    private static final String CONFIG_CAT = "Remote";

    private int port;

    private String hostname;

    private boolean useSSL;

    private boolean allowUnauthenticatedAccess;

    @FEModule.Instance
    private static ModuleRemote instance;

    private Server server;

    private Map<String, RemoteHandler> handlers = new HashMap<>();

    /**
     * Get the instance of ModuleRemote
     */
    public static ModuleRemote getInstance()
    {
        return instance;
    }

    @SubscribeEvent
    public void load(FEModuleInitEvent e)
    {
        registerHandler(new RemoteHandler() {
            @Override
            public Object handle(RemoteSession session, JsonObject data)
            {
                OutputHandler.felog.info("[remote] test message = " + data.toString());

                JsonObject result = new JsonObject();
                result.addProperty("success", true);
                result.addProperty("state", "happy");
                return result;
            }

            @Override
            public String getID()
            {
                return "test";
            }
        });
    }

    @SubscribeEvent
    public void serverStarting(FEModuleServerInitEvent e)
    {
        try
        {
            if (useSSL)
            {
                SSLContextHelper sslCtxHelper = new SSLContextHelper();
                sslCtxHelper.loadSSLCertificate("private.cert", "somepass", "someotherpass");
                server = new Server(port, hostname, sslCtxHelper.getSSLCtx());
            }
            else
            {
                server = new Server(port, hostname);
            }
        }
        catch (IOException | GeneralSecurityException e1)
        {
            OutputHandler.felog.severe("Unable to start remote-server: " + e1.getMessage());
        }
    }

    @SubscribeEvent
    public void serverStopping(FEModuleServerStopEvent e)
    {
        if (server != null)
        {
            server.close();
            server = null;
        }
    }

    @Override
    public void load(Configuration config, boolean isReload)
    {
        hostname = config.get(CONFIG_CAT, "hostname", "localhost", "Hostname of the minecraft server").getString();
        port = config.get(CONFIG_CAT, "port", 27020, "Port to connect remotes to").getInt();
        // useSSL = config.get(CONFIG_CAT, "useSSL", false,
        // "Protect the communication with SSL").getBoolean();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.forgeessentials.api.remote.RemoteManager#registerHandler(com.forgeessentials.api.remote.RemoteHandler)
     */
    @Override
    public void registerHandler(RemoteHandler handler)
    {
        final String id = handler.getID();
        if (handlers.containsKey(id))
            throw new IllegalArgumentException(String.format("Handler with ID \"%s\" already registerd", id));
        handlers.put(id, handler);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.forgeessentials.api.remote.RemoteManager#getHandler(java.lang.String)
     */
    @Override
    public RemoteHandler getHandler(String id)
    {
        return handlers.get(id);
    }

    /**
     * Check, if unauthenticated access is allowed
     */
    public boolean allowUnauthenticatedAccess()
    {
        return allowUnauthenticatedAccess;
    }

}
