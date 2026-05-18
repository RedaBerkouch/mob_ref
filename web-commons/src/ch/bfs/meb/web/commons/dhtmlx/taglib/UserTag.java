package ch.bfs.meb.web.commons.dhtmlx.taglib;

import java.io.IOException;

import org.springframework.security.core.context.SecurityContextHolder;

import ch.bfs.meb.security.MebUser;
import lombok.extern.slf4j.Slf4j;

/**
 * Tag printing out current logged in user
 */
@Slf4j
public class UserTag extends DhtmlxTagBase {

    @Override
    public void doTag() throws DhtmlxTagException {
        try {
            MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            pageContext.getOut().print(user != null ? user.getEmail() : "N/A");
        } catch (IOException e) {
            throw new DhtmlxTagException(e);
        }
    }
}
