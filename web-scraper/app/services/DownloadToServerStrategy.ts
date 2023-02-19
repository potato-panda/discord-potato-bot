import { DownloadStrategy } from './DowloadStrategy';
import { DownloadService } from './DownloadService';

export class DownloadToServerStrategy implements DownloadStrategy {
    constructor(readonly location: string,readonly service = new DownloadService) {

    }
    
}