package org.ast.bedwarspro.mannger;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.entity.Player;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class AuthManager {
    private final File authFile;
    private final Gson gson = new Gson();
    private final Map<String, String> userData = new HashMap<>();

    public AuthManager(File dataFolder) {
        this.authFile = new File(dataFolder, "auth.json");
        loadUserData();
    }

    private void loadUserData() {
        if (!authFile.exists()) {
            return;
        }
        try (Reader reader = new FileReader(authFile)) {
            Type type = new TypeToken<Map<String, String>>() {}.getType();
            Map<String, String> data = gson.fromJson(reader, type);
            if (data != null) {
                userData.putAll(data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveUserData() {
        try (Writer writer = new FileWriter(authFile)) {
            gson.toJson(userData, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean register(String username, String password) {
        if (userData.containsKey(username)) {
            return false; // 用户名已存在
        }
        userData.put(username, password);
        saveUserData();
        return true;
    }

    public boolean login(String username, String password) {
        return userData.containsKey(username) && userData.get(username).equals(password);
    }

    public boolean isRegistered(String username) {
        return userData.containsKey(username);
    }
}