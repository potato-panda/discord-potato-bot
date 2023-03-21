import { ListenerEvent } from './ListenerEvent';

export interface PixivImageRequestEvent extends ListenerEvent {
  data: {
    url: string;
  };
}
