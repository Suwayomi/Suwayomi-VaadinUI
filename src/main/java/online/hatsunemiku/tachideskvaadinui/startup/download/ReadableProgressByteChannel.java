/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.startup.download;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.function.IntConsumer;

public class ReadableProgressByteChannel implements ReadableByteChannel {

  private final ReadableByteChannel channel;
  private final IntConsumer onRead;

  private int read = 0;

  public ReadableProgressByteChannel(ReadableByteChannel channel, IntConsumer onRead) {
    this.channel = channel;
    this.onRead = onRead;
  }

  @Override
  public int read(ByteBuffer dst) throws IOException {
    int nRead = channel.read(dst);
    notifyBytesRead(nRead);
    return nRead;
  }

  protected void notifyBytesRead(int nRead) {
    if (nRead > 0) {
      read += nRead;
      onRead.accept(read);
    }
  }

  @Override
  public boolean isOpen() {
    return channel.isOpen();
  }

  @Override
  public void close() throws IOException {
    channel.close();
  }
}
