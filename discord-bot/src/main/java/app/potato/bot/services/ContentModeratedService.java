package app.potato.bot.services;

import app.potato.bot.services.ContentModerationService.ContentModerationData;
import app.potato.bot.services.ContentModerationService.ContentModerationResponse;
import app.potato.bot.services.FileDownloadResponse.FileDownloadMetadata;
import app.potato.bot.utils.ChannelUtil;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.IOException;
import java.util.Optional;

import static app.potato.bot.services.ContentModerationService.requestImageContentModeration;

public
class ContentModeratedService {
    static
    Optional<ContentModerationResponse> getContentModerationData( MessageReceivedEvent event,
                                                                  boolean isNsfwContent,
                                                                  FileDownloadMetadata fileDownloadMetadata,
                                                                  byte[] imageBytes )
    throws IOException, InterruptedException
    {
        boolean isNsfwChannel = ChannelUtil.isNsfwChannel( event );
        return ( !isNsfwContent && !isNsfwChannel )
               ? requestImageContentModeration( event,
                                                fileDownloadMetadata.mimeType(),
                                                imageBytes )
               : Optional.empty();
    }

    public
    record ModeratedContent(
            FileDownloadMetadata metadata,
            ContentModerationData moderationData,
            byte[] imageData
    ) {}
}
