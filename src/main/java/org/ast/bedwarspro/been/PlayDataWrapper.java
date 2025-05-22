package org.ast.bedwarspro.been;

import java.util.Map;

public class PlayDataWrapper {
    private Map<String, PlayerStats> data;

    public Map<String, PlayerStats> getData() {
        return data;
    }

    public void setData(Map<String, PlayerStats> data) {
        this.data = data;
    }
}
