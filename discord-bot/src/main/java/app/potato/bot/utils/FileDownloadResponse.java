package app.potato.bot.utils;

import java.util.Optional;

public
record FileDownloadResponse(
        Optional<FileDownloadMetadata> metadata,
        boolean success,
        String message,
        String key

)
{
    public
    record FileDownloadMetadata(
            String mimeType,
            Integer size,
            String fileName,
            String fileExtension
    )
    {
        public
        String getFileNameWithExtension() {
            return this.fileName + "." + this.fileExtension;
        }
    }
}
