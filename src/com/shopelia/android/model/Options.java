package com.shopelia.android.model;

import java.util.ArrayList;

public class Options extends ArrayList<Option> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public Options() {
        super();
    }

    public Options(ArrayList<Option> options) {
        super();
        for (Option option : options) {
            add(option);
        }
    }

    @Override
    public boolean add(Option object) {

        return super.add(object);
    }

}
