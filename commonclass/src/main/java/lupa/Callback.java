package lupa;

import io.netty.buffer.ByteBuf;

public interface Callback {
    void call(String msg);
}
