package app.potato.bot.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import io.nats.client.Message;

import java.io.IOException;

public final
class NatsUtil {
    public static
    byte[] getObjectBytes( Object o ) throws JsonProcessingException
    {
        ObjectWriter objectWriter
                = new ObjectMapper().registerModule( new Jdk8Module() ).writer()
                                    .withDefaultPrettyPrinter();
        return objectWriter.writeValueAsBytes( o );
    }

    public static
    <T> T getMessageObject( Message message,
                            Class<T> valueType ) throws IOException
    {
        return new ObjectMapper().registerModule( new Jdk8Module() )
                                 .readValue( message.getData(),
                                             valueType );
    }
}
