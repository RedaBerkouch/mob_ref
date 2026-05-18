package ch.bfs.meb.configuration;

/**
 * Interface for the web configuration of the modules SDA, SDL, SSP and SBG
 */
public interface IWebModuleConfiguration extends IWebConfiguration {
    String getModuleServerURL();

    void setModuleServerURL(String url);
}