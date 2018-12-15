package com.profitera.util;

import oracle.toplink.queryframework.CursoredStream;

/**
 * This uses a TopLink Object stream in a forward-only manner,
 * reading it much like an enumeration/iterator. It takes
 * care of the stream synchronization for you as long as you use
 * the next() method to get the next object from the stream.
 * @author jamison
 *
 */
public abstract class TopLinkStreamThread extends Thread {
    protected CursoredStream stream;
    public TopLinkStreamThread(CursoredStream stream) {
        this.stream = stream;
    }

    /**
     * This method does the synchronization on the stream for you.<br/>
     * Having a hasNext method is pointless since this in multithreaded,
     * just ask for one and see if it is null, if it is the stream is finito! 
     * @return
     */
    protected Object next() {
        Object o = null;
        synchronized (stream) {
            if (!stream.atEnd())
                o = stream.read();
            stream.releasePrevious();
        }
        return o;
    }

}
