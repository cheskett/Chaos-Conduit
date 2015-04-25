package org.chaosconduit;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Cameron on 4/25/2015.
 */
public class ViewId {

    private static ViewId INSTANCE = new ViewId();

    private AtomicInteger seq;

    private ViewId() {
        seq = new AtomicInteger(0);
    }

    public int getUniqueId() {
        return seq.incrementAndGet();
    }

    public static ViewId getInstance() {
        return INSTANCE;
    }
}
