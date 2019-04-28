package com.tencent.shadow.core.pluginmanager.installplugin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class PluginConfig {

    /**
     * 配置json文件的格式版本号
     */
    public int version;
    /**
     * 配置json文件的格式兼容版本号
     */
    public int[] compact_version;
    /**
     * 标识一次插件发布的id
     */
    public String UUID;
    /**
     * 标识一次插件发布的id，可以使用自定义格式描述版本信息
     */
    public String UUID_NickName;
    /**
     * pluginLoaderAPk 文件信息
     */
    public FileInfo pluginLoader;
    /**
     * runtime 文件信息
     */
    public FileInfo runTime;
    /**
     * 业务插件 key: partKey value:文件信息
     */
    public Map<String, PluginFileInfo> plugins = new HashMap<>();
    /**
     * 插件的存储目录
     */
    public File storageDir;

    static class FileInfo {
        File file;
        String hash;

        FileInfo(File file, String hash) {
            this.file = file;
            this.hash = hash;
        }
    }

    static class PluginFileInfo extends FileInfo {
        final String[] dependsOn;
        final String[] hostWhiteList;
        final String businessName;

        PluginFileInfo(String businessName, FileInfo fileInfo, String[] dependsOn, String[] hostWhiteList) {
            this(businessName, fileInfo.file, fileInfo.hash, dependsOn, hostWhiteList);
        }

        PluginFileInfo(String businessName, File file, String hash, String[] dependsOn, String[] hostWhiteList) {
            super(file, hash);
            this.businessName = businessName;
            this.dependsOn = dependsOn;
            this.hostWhiteList = hostWhiteList;
        }
    }


    public static PluginConfig parseFromJson(String json, File storageDir) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        PluginConfig pluginConfig = new PluginConfig();
        pluginConfig.version = jsonObject.getInt("version");
        JSONArray compact_version_json = jsonObject.optJSONArray("compact_version");
        if (compact_version_json != null && compact_version_json.length() > 0) {
            pluginConfig.compact_version = new int[compact_version_json.length()];
            for (int i = 0; i < compact_version_json.length(); i++) {
                pluginConfig.compact_version[i] = compact_version_json.getInt(i);
            }
        }
        //todo #27 json的版本检查和不兼容检查
        pluginConfig.UUID = jsonObject.getString("UUID");
        pluginConfig.UUID_NickName = jsonObject.getString("UUID_NickName");

        JSONObject loaderJson = jsonObject.optJSONObject("pluginLoader");
        if (loaderJson != null) {
            pluginConfig.pluginLoader = getFileInfo(loaderJson, storageDir);
        }

        JSONObject runtimeJson = jsonObject.optJSONObject("runtime");
        if (runtimeJson != null) {
            pluginConfig.runTime = getFileInfo(runtimeJson, storageDir);
        }

        JSONArray pluginArray = jsonObject.optJSONArray("plugins");
        if (pluginArray != null && pluginArray.length() > 0) {
            for (int i = 0; i < pluginArray.length(); i++) {
                JSONObject plugin = pluginArray.getJSONObject(i);
                String partKey = plugin.getString("partKey");
                pluginConfig.plugins.put(partKey, getPluginFileInfo(plugin, storageDir));
            }
        }

        pluginConfig.storageDir = storageDir;
        return pluginConfig;
    }

    private static FileInfo getFileInfo(JSONObject jsonObject, File storageDir) throws JSONException {
        String name = jsonObject.getString("apkName");
        String hash = jsonObject.getString("hash");
        return new FileInfo(new File(storageDir, name), hash);
    }

    private static PluginFileInfo getPluginFileInfo(JSONObject jsonObject, File storageDir) throws JSONException {
        String businessName = jsonObject.getString("businessName");
        FileInfo fileInfo = getFileInfo(jsonObject, storageDir);
        String[] dependsOn = getArrayStringByName(jsonObject, "dependsOn");
        String[] hostWhiteList = getArrayStringByName(jsonObject, "hostWhiteList");
        return new PluginFileInfo(businessName, fileInfo, dependsOn, hostWhiteList);
    }

    private static String[] getArrayStringByName(JSONObject jsonObject, String name) throws JSONException {
        JSONArray jsonArray = jsonObject.optJSONArray(name);
        String[] dependsOn;
        if (jsonArray != null) {
            dependsOn = new String[jsonArray.length()];
            for (int i = 0; i < jsonArray.length(); i++) {
                dependsOn[i] = jsonArray.getString(i);
            }
        } else {
            dependsOn = new String[]{};
        }
        return dependsOn;
    }
}
