package com.kinfante.kiinbot;

import de.btobastian.javacord.entities.User;
import de.btobastian.javacord.entities.permissions.Role;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import de.btobastian.sdcf4j.CommandHandler;

import java.util.Collection;

public class AdminCommands implements CommandExecutor {

    @Command(aliases = {"!au*"}, description = "Update data for Kiin.")
    public String onAdminUpdateCommand(String command, String[] args, User user)
    {
        String msg = "";
        Collection<Role> roles = user.getRoles(Data._singleton.getServer());
        if(roles.contains(Data._singleton.getAdminRole()))
        {
            Data._singleton.update();
            msg = "Pokemon raid list has been successfully updated!";
        }

        return msg;
    }
}
