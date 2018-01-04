package com.ld.autoupdatedemo.bean;

/**
 * Created by BillTian on 2017/12/11.
 */

public class UpdateBean {
    public String newVision;
    public String content;
    public String url;

    public String getNewVision() {
        return newVision;
    }

    public void setNewVision(String newVision) {
        this.newVision = newVision;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "版本{" +
                "newVision='" + newVision + '\'' +
                ", content='" + content + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
