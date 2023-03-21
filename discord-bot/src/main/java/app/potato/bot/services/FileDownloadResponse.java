package app.potato.bot.services;

import java.util.Optional;

public
record FileDownloadResponse(
        Optional<FileDownloadMetadata> metadata,
        boolean success,
        String message

)
{
    public
    record FileDownloadMetadata(
            String mimeType,
            Integer size,
            String fileName,
            String fileExtension,
            String key
    )
    {
        public
        String getFileNameWithExtension() {
            return this.fileName + "." + this.fileExtension;
        }
    }
}
