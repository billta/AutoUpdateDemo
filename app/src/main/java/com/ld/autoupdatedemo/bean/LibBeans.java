package com.ld.autoupdatedemo.bean;

import java.util.ArrayList;

/**
 * Created by BillTian on 2017/12/11.
 */

public class LibBeans<T> {
    public int recordsTotal; //几条数
    public ArrayList<T> data;

    @Override
    public String toString() {
        return "LibBeans{" +
                "recordsTotal=" + recordsTotal +
                ", data=" + data +
                '}';
    }
}
