package com.icode.icodebe.common;

public class Constants {
    public static final String AUTH_TOKEN_TYPE = "Bearer ";

    public static final String STORAGE_ROOT_DIRECTORY;

    public static final String FILE_TEMPLATE = "<html lang=\"en\">\n" +
            "  <head>\n" +
            "    <meta charset=\"UTF-8\" />\n" +
            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />\n" +
            "    <style>%s</style>\n" +
            "  </head>\n" +
            "  <body>\n" +
            "    %s<div><script>%s</script></div>\n" +
            "  </body>\n" +
            "</html>\n";

    static {
        final var OS = System.getProperty("os.name").toLowerCase();

        if (isUnix(OS)) {
            STORAGE_ROOT_DIRECTORY = "/data/icode";
        } else {
            STORAGE_ROOT_DIRECTORY = System.getProperty("user.home") + "/icode";
        }
    }

    private static boolean isUnix(String OS) {
        return (OS.contains("nix") || OS.contains("nux") || OS.contains("aix"));
    }
}
