package invaders.singleton;

public class Singleton {
    private static Singleton instance = null;
    private String easyLevel;
    private String mediumLevel;
    private String hardLevel;

    private Singleton() {
        loadLevels();
    }

    /**
     *
     * @return
     */
    public static Singleton getInstance() {
        if (instance == null) {
            instance = new Singleton();
        }
        return instance;
    }

    private void loadLevels() {
        ClassLoader classLoader = getClass().getClassLoader();

        String easyFile = classLoader.getResource("config_easy.json").getFile();
        easyLevel = easyFile;

        String mediumFile = classLoader.getResource("config_medium.json").getFile();
        mediumLevel = mediumFile;

        String hardFile = classLoader.getResource("config_hard.json").getFile();
        hardLevel = hardFile;

    }

    public String getEasyLevel() {
        return easyLevel;
    }

    public String getMediumLevel() {
        return mediumLevel;
    }

    public String getHardLevel() {
        return hardLevel;
    }
}
