package com.icode.icodebe.common;

public class Constants {
    public static final String STORAGE_ROOT_DIRECTORY = System.getProperty("user.home") + "\\icode";

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
}
