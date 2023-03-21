import { request, RequestOptions } from "https";
import { userAgent } from "../constants.config";

export function createHttpRequest(url: URL, options?: RequestOptions) {
	return request(url, {
		headers: { Accept: "*/*", Host: url.host, "User-Agent": userAgent },
		...options,
	});
}
