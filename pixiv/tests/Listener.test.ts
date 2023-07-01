import { Msg, StringCodec } from 'nats';
import { describe, expect, it, vi } from 'vitest';
import NatsClient from '../app/NatsClient';
import { Listener } from '../app/events/Listener';

const subject = 'testSubject';

class MockListener extends Listener<any> {
  async onMessage(msg: Msg, _data: any): Promise<void> {
    msg.respond('HELLO');
  }
}

describe('request/reply message bus', () => {
  it('should be subscribed on listen', async () => {
    const client = await NatsClient.create();
    const nc = client.connection;
    const subSpy = vi.spyOn(nc, 'subscribe');

    const listener = new MockListener(subject);
    listener.listen(nc);

    expect(subSpy).toBeCalled();
  });

  it('should reply when request is made', async () => {
    const subClient = await NatsClient.create();
    const reqClient = await NatsClient.create();
    const sc = StringCodec();

    const listener = new MockListener(subject);
    listener.listen(subClient.connection);

    const spyOnMessage = vi.spyOn(listener, 'onMessage');
    const spyRequest = vi.spyOn(reqClient.connection, 'request');

    const reply = await reqClient.connection.request(
      subject,
      sc.encode(JSON.stringify({ test: 'data32' })),
      {
        timeout: 1_000 * 5,
      },
    );

    expect(spyRequest).toHaveBeenCalled();
    expect(spyOnMessage).toHaveBeenCalled();
    expect(reply.string()).toEqual('HELLO');
  });
});
