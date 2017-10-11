package com.kinfante.kiinbot;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.Javacord;
import de.btobastian.javacord.entities.Server;
import de.btobastian.javacord.entities.permissions.Role;
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.FileReader;
import java.sql.Time;
import java.util.*;

public class Data {

    protected static Data _singleton;

    private JSONArray raidPokemonList;
    private JSONObject pokemonList;
    private JSONObject pokeTypesList;
    private JSONObject pokemonTypeEmojis;
    private Role adminRole;
    private Role instinctRole;
    private Role mysticRole;
    private Role valorRole;
    private Server server;
    private DiscordAPI api;

    //region Getters/Setters
    public Server getServer() {
        return server;
    }

    public Role getAdminRole(){
        return adminRole;
    }
    //endregion

    public Data()
    {
        if(_singleton == null)
        {
            _singleton = this;
        }
    }

    /**
     * Called by the BotController constructor
     * Initialized the api object.
     * Gets the bot token from the auth.json file and authenticates to discord with it.
     * We need authentication before we turn on the bot
     */
    public void initApi()
    {
        JSONObject jsonObj = getJSON("data\\configs\\auth.json");
        String botToken = jsonObj.get("bot-token").toString();
        api = Javacord.getApi(botToken, true);
        api.setGame("Pokemon GO");
    }

    /**
     * Runs on the start of the bot
     * Grabs all of the necessary data from the JSON files.
     */
    void init()
    {
        //Get the server object
        Collection<Server> collS = api.getServers();
        Server[] servers = collS.toArray(new Server[collS.size()]);
        server = servers[0];

        getRoleData();
        getTypesData();
        getTypeEmojis();
        update();
    }

    DiscordAPI getApi()
    {
        return api;
    }

    /**
     * Parse the pokemon.json and types.json
     */
    void update()
    {
        getRaidPokemonData();
    }

    /**
     * Called from RaidCommands
     * @param args arguments in the command
     * @return String of PokemonName, "" if it doesn't exist
     */
    String getPokemonName(String[] args)
    {
        String name = args[0].toLowerCase();

        //Automatically return if is an egg
        if(name.equalsIgnoreCase("egg3") || name.equalsIgnoreCase("egg4") || name.equalsIgnoreCase("egg5"))
        {
            return args[0].toLowerCase();
        }

        //Check if it's a single worded pokemon
        if(raidPokemonList.contains(name))
        {
            return capitalize(name);
        }

        if(args.length > 1)
        {
            //Check if it's a two worded name
            name = args[0].toLowerCase() + " " + args[1].toLowerCase();
            if(raidPokemonList.contains(name))
            {
                return capitalize(name);
            }
        }

        return "";
    }

    /**
     * Capitalize the first character of each string value separated by a space.
     * @param text String value we are capitalizing
     * @return the capitalized string
     */
    private static String capitalize(String text)
    {
        String[] toks = text.split(" ");

        for(int i = 0; i < toks.length; i++)
        {
            String tempStr = toks[i];
            toks[i] = tempStr.substring(0,1).toUpperCase() + tempStr.substring(1,tempStr.length());
        }

        String retStr = String.join(" ", toks);
        return retStr;
    }

    /**
     * Get the time a number of minutes ahead from now.
     *
     * @param minutes number of minutes raid will remain up
     * @return time value added to current time.
     */
    static Time getExpireTime(int minutes)
    {
        Calendar cal = Calendar.getInstance();
        try
        {
            cal.add(Calendar.MINUTE, minutes);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            cal = null;
        }

        long longTime = cal.getTime().getTime();

        return new Time(longTime);
    }

    //region  Data Retrieval from JSON files.
    private void getRoleData()
    {
        JSONObject jsonObj = getJSON("data\\configs\\appdata.json");
        jsonObj = (JSONObject)jsonObj.get("role_names");

        //Role objects
        Collection<Role> collR = server.getRoles();
        Role[] roles = collR.toArray(new Role[collR.size()]);

        for(int i = 0; i < roles.length; i++)
        {
            String roleName = roles[i].getName();

            if(roleName.equalsIgnoreCase(jsonObj.get("admin").toString()))
            {
                adminRole = roles[i];
                continue;
            }

            if(roleName.equalsIgnoreCase(jsonObj.get("instinct").toString().toLowerCase()))
            {
                instinctRole = roles[i];
                continue;
            }

            if(roleName.equalsIgnoreCase(jsonObj.get("valor").toString().toLowerCase()))
            {
                valorRole = roles[i];
                continue;
            }

            if(roleName.equalsIgnoreCase(jsonObj.get("mystic").toString().toLowerCase()))
            {
                mysticRole = roles[i];
                continue;
            }
        }
    }

    private void getTypesData()
    {
        pokeTypesList = getJSON("data\\pokemondata\\types.json");
    }

    private void getTypeEmojis()
    {
        JSONObject jsonObj = getJSON("data\\configs\\appdata.json");
        pokemonTypeEmojis  = (JSONObject)jsonObj.get("icons");
    }

    /**
     * Read pokemon.json file for the raids pokemon and adds that list to raidPokemonList array
     */
    private void getRaidPokemonData()
    {
        JSONObject jsonObj = getJSON("data\\pokemondata\\pokemon.json");
        raidPokemonList = (JSONArray)jsonObj.get("raids");
        pokemonList = (JSONObject)jsonObj.get("pokemon_list");
    }

    public String getPokemonTypeString(JSONArray arr)
    {
        StringBuilder sb = new StringBuilder("");
        for(int i = 0; i < arr.size(); i++)
        {
            String s = pokemonTypeEmojis.get(arr.get(i)).toString() + " ";
            sb.append(s);
        }
        return sb.toString().trim();
    }

    /**
     * Find the weakness types given an array of types.
     * @param arr JSONArray of the pokemon types
     * @return string value of the type icon emojis
     */
    String getPokemonWeaknessesString(JSONArray arr)
    {
        StringBuilder sb = new StringBuilder("");
        HashMap<String, Integer> typesAndValues = new HashMap<String, Integer>();

        //Loop through each pokemon type
        for(int i = 0; i < arr.size(); i++)
        {
            String typeName = arr.get(i).toString();

            //get the key names under each type
            JSONObject set = (JSONObject)pokeTypesList.get(typeName);
            Set<String> keySet =  set.keySet();

            //Get the values for each key
            Iterator<String> it = keySet.iterator();
            while(it.hasNext())
            {
                String keyName = it.next();
                if(typesAndValues.keySet().contains(keyName))
                {
                    //Add the values together and add the new values to the list
                    int val = Integer.parseInt(typesAndValues.get(keyName).toString());
                    val += Integer.parseInt(set.get(keyName).toString());
                    typesAndValues.replace(keyName, val);
                }
                else
                {
                    int val = Integer.parseInt(set.get(keyName).toString());
                    //Simply add the new  KeyValuePair to the list
                    typesAndValues.put(keyName, val);
                }
            }
        }
        JSONArray resultsArr = new JSONArray();
        Iterator<String> resultKeys = typesAndValues.keySet().iterator();
        while(resultKeys.hasNext()) {
            String key = resultKeys.next();
            if (typesAndValues.get(key) > 0)
                resultsArr.add(key);
        }

        sb.append(getPokemonTypeString(resultsArr));
        return sb.toString().trim();
    }

    JSONObject getPokemonDataByName(String name)
    {
        return (JSONObject)pokemonList.get(name.toLowerCase());
    }

    /**
     * Grabs configuration data like API Tokens and sensitive information from auth.json file
     * @return JSONObject of the json file.
     */
    private JSONObject getJSON(String path)
    {
        /*ClassLoader cl = getClass().getClassLoader();
        InputStream stream = cl.getResourceAsStream(path);*/
        JSONObject jsonObj = null;
        try
        {
            JSONParser parser = new JSONParser();
            jsonObj = (JSONObject)parser.parse(new FileReader(path));
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        return jsonObj;
    }
    //endregion
}
