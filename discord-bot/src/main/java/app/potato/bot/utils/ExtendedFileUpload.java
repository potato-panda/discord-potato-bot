package app.potato.bot.utils;

import net.dv8tion.jda.api.utils.FileUpload;

import static app.potato.bot.services.FileDownloadResponse.FileDownloadMetadata;

public
record ExtendedFileUpload(
        FileDownloadMetadata metadata,
        FileUpload fileUpload
) {}
