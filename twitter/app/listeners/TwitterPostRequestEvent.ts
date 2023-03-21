import { ListenerEvent } from "./ListenerEvent";

export interface TwitterPostRequestEvent extends ListenerEvent {
	subject: string;
	data: {
		url: string;
	};
}
