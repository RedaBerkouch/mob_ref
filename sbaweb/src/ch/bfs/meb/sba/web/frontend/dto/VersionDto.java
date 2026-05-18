package ch.bfs.meb.sba.web.frontend.dto;

public class VersionDto {
    private String major;
    private String minor;
    private String full;

    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }

    public String getMinor() { return minor; }
    public void setMinor(String minor) { this.minor = minor; }

    public String getFull() { return full; }
    public void setFull(String full) { this.full = full; }
}
