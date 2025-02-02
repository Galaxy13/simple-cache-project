package com.galaxy13.network.netty.auth;

import java.lang.ref.WeakReference;
import java.util.Objects;

public class WeakTokenReference extends WeakReference<String> {
    public WeakTokenReference(String referent) {
        super(referent);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (obj instanceof WeakTokenReference) {
            WeakReference<String> other = (WeakTokenReference) obj;
            return Objects.equals(this.get(), other.get());
        }
        return false;
    }

    @Override
    public int hashCode() {
        String referent = this.get();
        return referent != null ? referent.hashCode() : 0;
    }
}
