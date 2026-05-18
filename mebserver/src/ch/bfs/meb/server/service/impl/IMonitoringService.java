package ch.bfs.meb.server.service.impl;

public interface IMonitoringService {
    Boolean checkIdmService();

    Boolean checkSasService();

    Boolean checkMetastatService();

    Boolean checkBurService();

    Boolean checkDatabase();
}
