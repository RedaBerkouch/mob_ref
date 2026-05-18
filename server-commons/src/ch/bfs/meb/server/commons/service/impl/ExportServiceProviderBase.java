package ch.bfs.meb.server.commons.service.impl;

import java.util.HashMap;
import java.util.List;

import ch.bfs.meb.security.idm.IIdmUserService;
import ch.bfs.meb.security.idm.User;
import ch.bfs.meb.server.commons.codes.ICodegroupManager;
import ch.bfs.meb.server.commons.i18n.IServerLocalizationManager;
import lombok.Setter;

public abstract class ExportServiceProviderBase implements IExportServiceProvider {

    public static final int MEB_RESPONSE_NO_PLAUSIERRORS = 10;
    public static final int MEB_RESPONSE_WITH_PLAUSIERRORS = 11;

    protected static final String APPLICATION_TITLE = "application.title";

    @Setter
    protected ExportUsersFactory exportUsersFactory;
    @Setter
    protected ExportInitStatusFactory exportInitStatusFactory;
    @Setter
    protected IIdmUserService idmService;
    @Setter
    protected IServerLocalizationManager localizationManager;
    @Setter
    protected ICodegroupManager codegroupManager;

    /* helper methods for subclasses */

    protected void addAll(HashMap<String, ExportUser> userMap, List<User> userList, long role) {
        for (User user : userList) {
            if (!userMap.containsKey(user.getUsername().toLowerCase())) {
                userMap.put(user.getUsername().toLowerCase(), new ExportUser(user, role));
            }
        }
    }
}
