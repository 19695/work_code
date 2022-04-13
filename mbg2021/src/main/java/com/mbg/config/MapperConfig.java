package com.mbg.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class MapperConfig {

    // 实体类名
    public static String simpleName = "";

    // mapper 的命名空间
    public static String nameSpace = "";

    // 数据库表名
    public static String tableName = "";

    // 限定性类名
    public static String typeName = "";

    // mapper 文件名称后缀
    public static String suffix = "-mapping.xml";

    // 是否输出到控制台，false 输出到文件
    public static boolean output2Console = true;

    // mapper 文件生成路径
    public static String mapperPath = "src/main/java/com/mbg/mapper/";

    static {
        InputStream resourceAsStream = MapperConfig.class.getClassLoader().getResourceAsStream("config.properties");
        Properties properties = new Properties();
        try {
            properties.load(resourceAsStream);
            String mapperNameSpace = properties.getProperty("mappper.nameSpace");
            String generatorEntityName = properties.getProperty("generator.entityName");
            String mapperSuffix = properties.getProperty("mapper.suffix");
            String mapperOutput2Console = properties.getProperty("mapper.output2Console");
            String path = properties.getProperty("mapper.path");

            MapperConfig.simpleName = isBlank(mapperNameSpace) ? generatorEntityName : mapperNameSpace;
            MapperConfig.nameSpace = "MNS_" + simpleName;
            MapperConfig.tableName = properties.getProperty("generator.tableName");
            MapperConfig.typeName = properties.getProperty("generator.entityPath") + "." + generatorEntityName;
            MapperConfig.suffix = isBlank(mapperSuffix) ? suffix : mapperSuffix;
            MapperConfig.output2Console = isBlank(mapperOutput2Console) ? output2Console : Boolean.parseBoolean(mapperOutput2Console);
            MapperConfig.mapperPath = isBlank(path) ? mapperPath : path;
            mkPath();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void mkPath() throws IOException {
        String target_classes = MapperConfig.class.getClassLoader().getResource("").getPath();
        File project = new File(target_classes).getParentFile().getParentFile();
        String currentPath = project.getAbsolutePath();
        File file = new File(currentPath, mapperPath);
        file.mkdirs();
        MapperConfig.mapperPath = file.getCanonicalPath();
    }

    private static boolean isBlank(String target) {
        return target == null || "".equals(target);
    }
}
