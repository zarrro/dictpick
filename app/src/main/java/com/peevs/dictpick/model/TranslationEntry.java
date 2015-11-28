package com.peevs.dictpick.model;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by zarrro on 28.11.15.
 */
public class TranslationEntry {
    private final AtomicLong atomicId;
    private final Text srcText;
    private final Text targetText;

    public TranslationEntry(long id, Text srcText, Text targetText) {
        this.atomicId = new AtomicLong(id);
        this.srcText = srcText;
        this.targetText = targetText;
    }

    /**
     * @return - id of the translation so it can be tracked in statistics.
     */
    public long getId() {
        return atomicId.get();
    }

    public void setId(long id) {
        atomicId.set(id);
    }

    public Text getSrcText() {
        return srcText;
    }

    public Text getTargetText() {
        return targetText;
    }

    @Override
    public String toString() {
        return "TranslationEntry{" +
                "id=" + atomicId.get() +
                ", srcText='" + srcText + '\'' +
                '}';
    }
}
