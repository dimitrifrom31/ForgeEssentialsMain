package com.forgeessentials.snooper;

import com.forgeessentials.api.snooper.Response;
import com.forgeessentials.core.moduleLauncher.ModuleConfigBase;
import net.minecraft.command.ICommandSender;
import net.minecraftforge.common.config.Configuration;

public class ConfigSnooper extends ModuleConfigBase {
    private Configuration config;

    @Override
    public void init()
    {
        config = new Configuration(file, true);

        String cat = "Snooper";

        ModuleSnooper.port = config.get(cat, "port", 25565, "The query port").getInt();
        ModuleSnooper.hostname = config.get(cat, "hostname", "", "The query hostname/IP").getString();

        ModuleSnooper.enable = config.get(cat, "enable", false, "This one is obvious, don't you think?").getBoolean(false);

        for (Response response : ResponseRegistry.getAllResponses().values())
        {
            String subCat = cat + "." + response.getName();
            response.allowed = config.get(subCat, "enable", true, "If false, this response won't be allowed on this server.").getBoolean(true);
            response.readConfig(subCat, config);
        }
        config.save();
    }

    @Override
    public void forceSave()
    {
        String cat = "Snooper";

        config.get(cat, "port", 25565, "").set(ModuleSnooper.port);
        config.get(cat, "hostname", "", "The query hostname/IP").set(ModuleSnooper.hostname);
        config.get(cat, "enable", false, "This one is obvious, don't you think?").set(ModuleSnooper.enable);

        for (Response response : ResponseRegistry.getAllResponses().values())
        {
            String subCat = cat + "." + response.getName();
            config.get(subCat, "enable", true, "If false, this response won't be allowed on this server.").set(response.allowed);
            response.writeConfig(subCat, config);
        }
        config.save();
    }

    @Override
    public void forceLoad(ICommandSender sender)
    {
        config.load();

        String cat = "Snooper";

        ModuleSnooper.port = config.get(cat, "port", 25565, "The query port").getInt();
        ModuleSnooper.hostname = config.get(cat, "hostname", "", "The query hostname/IP").getString();

        ModuleSnooper.enable = config.get(cat, "enable", false, "This one is obvious, don't you think?").getBoolean(false);

        for (Response response : ResponseRegistry.getAllResponses().values())
        {
            String subCat = cat + "." + response.getName();
            response.allowed = config.get(subCat, "enable", true, "If false, this response won't be allowed on this server.").getBoolean(true);
            response.readConfig(subCat, config);
        }
    }

    public boolean universalConfigAllowed(){return true;}
}