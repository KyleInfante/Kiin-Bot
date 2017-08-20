package com.kinfante.kiinbot;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.Javacord;
import de.btobastian.javacord.entities.Server;
import de.btobastian.javacord.entities.permissions.Role;
import org.json.JSONArray;
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
     * Runs on the start of the bot
     */
    public void init()
    {
        //Get the server object
        Collection<Server> collS = api.getServers();
        Server[] servers = collS.toArray(new Server[collS.size()]);
        server = servers[0];

        getRoleData();
        update();
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

    public DiscordAPI getApi()
    {
        return api;
    }

    /**
     * Parse the pokemon.json and types.json
     */
    public void update()
    {
        getRaidPokemonData();
    }

    //****************  Data Retrieval from JSON files.
    private void getRoleData()
    {
        JSONObject jsonObj = getJSON("configs/appdata.json");
        jsonObj = jsonObj.getJSONObject("role_names");

        //Role objects
        Collection<Role> collR = server.getRoles();
        Role[] roles = collR.toArray(new Role[collR.size()]);

        for(int i = 0; i < roles.length; i++)
        {
            String roleName = roles[i].getName().toLowerCase();

            if(roleName == jsonObj.get("admin").toString().toLowerCase())
            {
                adminRole = roles[i];
                continue;
            }

            if(roleName == jsonObj.get("instinct").toString().toLowerCase())
            {
                instinctRole = roles[i];
                continue;
            }

            if(roleName == jsonObj.get("valor").toString().toLowerCase())
            {
                valorRole = roles[i];
                continue;
            }

            if(roleName == jsonObj.get("mystic").toString().toLowerCase())
            {
                mysticRole = roles[i];
                continue;
            }
        }
    }

    /**
     * Read pokemon.json file for the raids pokemon and adds that list to raidPokemonList array
     */
    private void getRaidPokemonData()
    {
        JSONObject jsonObj = getJSON("pokemondata/pokemon.json");
        JSONArray arr = jsonObj.getJSONArray("raids");
        raidPokemonList = new String[arr.length()];

        for(int i = 0 ; i < arr.length(); i++)
        {
            raidPokemonList[i] = arr.getString(i);
        }
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
