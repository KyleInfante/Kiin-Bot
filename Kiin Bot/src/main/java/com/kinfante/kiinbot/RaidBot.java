package com.kinfante.kiinbot;

import com.google.common.util.concurrent.FutureCallback;
import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.Server;
import de.btobastian.sdcf4j.*;
import de.btobastian.sdcf4j.handler.JavacordHandler;

import java.util.Collection;

public class RaidBot {

    private DiscordAPI api;

    public RaidBot(DiscordAPI api)
    {
        this.api = api;
    }

    /***
     * Enables the Raid Bot and puts it online.
     */
    public void Start()
    {
        api.connect(new FutureCallback<DiscordAPI>() {
            public void onSuccess(DiscordAPI discordAPI) {
                System.out.println("Kiin is online!");
                Data._singleton.init();
                EnableCommands();
            }

            public void onFailure(Throwable throwable) {
                throwable.printStackTrace();
            }
        });
    }

    private void EnableCommands()
    {
        CommandHandler cmdHandler = new JavacordHandler(api);
        cmdHandler.registerCommand(new RaidCommands());
        cmdHandler.registerCommand(new AdminCommands());
    }
}
