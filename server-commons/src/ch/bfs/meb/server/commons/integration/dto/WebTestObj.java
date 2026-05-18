package ch.bfs.meb.server.commons.integration.dto;

import java.util.ArrayList;
import java.util.List;

public class WebTestObj {
    public Long getLong() {
        return 0L;
    }

    public void setLong(Long l) {}

    public String getString() {
        return "";
    }

    public void setString(String s) {}

    public List<String> getListString() {
        return new ArrayList<String>();
    }

    public void setListString(List<String> list) {}

    public List<Long> getListLong() {
        return new ArrayList<Long>();
    }

    public void setListInt(List<Integer> list) {}

    public List<WebTestInnerObj> getListInnerObj() {
        return new ArrayList<WebTestInnerObj>();
    }

    public void setListInnerObj2(List<WebTestInnerObj> innerObj) {}
}
