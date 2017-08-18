package com.kinfante.kiinbot;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.Javacord;
import de.btobastian.javacord.entities.Server;
import de.btobastian.javacord.entities.permissions.Role;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Scanner;

public class Data {

    protected static Data _singleton;

    protected String[] raidPokemonList;
    protected Role adminRole;
    protected Role instinctRole;
    protected Role mysticRole;
    protected Role valorRole;
    protected Server server;
    private DiscordAPI api;
    private String botToken;

    public Data()
    {
        if(_singleton == null)
        {
            _singleton = this;
        }
    }

    /**
     * Only runs on the start of the bot
     */
    public void init()
    {
        //Get the server object
        Collection<Server> collS = api.getServers();
        Server[] servers = collS.toArray(new Server[collS.size()]);
        Data._singleton.server = servers[0];

        getRoleData();
        getRaidPokemonData();
    }

    /**
     * Initialized the api object.
     * Gets the bot token from the auth.json file and authenticates to discord with it.
     */
    public void initApi()
    {
        JSONObject jsonObj = getJSON("configs/auth.json");
        botToken = jsonObj.get("bot-token").toString();
        api = Javacord.getApi(botToken, true);
        api.setGame("Pokemon GO");
    }

    /**
     * Parse the pokemon.json and types.json
     */
    public void update()
    {

    }


    //****************  Data Retrieval from JSON files.
    private void getRoleData()
    {
        //Role objects
        Collection<Role> collR = server.getRoles();
        Role[] roles = collR.toArray(new Role[collR.size()]);

        for(int i = 0; i < roles.length; i++)
        {
            switch(roles[i].getName().toLowerCase())
            {
                case("admin"): {
                    adminRole = roles[i]; break;}
                case("instinct"): {
                    instinctRole = roles[i]; break;}
                case("valor"): {
                    valorRole = roles[i]; break;}
                case("mystic"): {
                    mysticRole = roles[i]; break;}
            }
        }
    }

    private void getRaidPokemonData()
    {
        JSONObject jsonObj = getJSON("data/pokemon.json");
        Iterator x = jsonObj.keys();

        while(x.hasNext())
        {
            System.out.println(x.next().toString());
        }
    }

    public DiscordAPI getAPI()
    {
        return api;
    }

    /**
     * Grabs configuration data like API Tokens and sensitive information from auth.json file
     * @return JSONObject of the json file.
     */
    protected JSONObject getJSON(String path)
    {
        ClassLoader cl = getClass().getClassLoader();
        InputStream stream = cl.getResourceAsStream(path);
        String fileContents = getFileContents(stream);
        JSONObject jsonObj = new JSONObject(fileContents);
        return jsonObj;
    }

    /***
     * Read given input stream
     * @param stream the given input stream
     * @return all of the contents of the file as a String
     */
    private String getFileContents(InputStream stream)
    {
        StringBuilder contents = new StringBuilder("");

        try
        {
            Scanner scanner = new Scanner(stream);

            while(scanner.hasNextLine())
            {
                contents.append(scanner.nextLine());
            }
            scanner.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return contents.toString();
    }
}
