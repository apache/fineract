package org.apache.fineract.command.persistence.mapping;

import javax.annotation.processing.Generated;
import org.apache.fineract.command.core.Command;
import org.apache.fineract.command.persistence.domain.CommandEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-19T12:02:20-0500",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.7 (Oracle Corporation)"
)
@Component
public class CommandMapperImpl implements CommandMapper {

    private final CommandJsonMapper commandJsonMapper;

    @Autowired
    public CommandMapperImpl(CommandJsonMapper commandJsonMapper) {

        this.commandJsonMapper = commandJsonMapper;
    }

    @Override
    public CommandEntity map(Command source) {
        if ( source == null ) {
            return null;
        }

        CommandEntity commandEntity = new CommandEntity();

        commandEntity.setCommandId( source.getId() );
        commandEntity.setCreatedAt( source.getCreatedAt() );
        commandEntity.setTenantId( source.getTenantId() );
        commandEntity.setUsername( source.getUsername() );
        commandEntity.setPayload( commandJsonMapper.map( source.getPayload() ) );

        return commandEntity;
    }

    @Override
    public Command map(CommandEntity source) {
        if ( source == null ) {
            return null;
        }

        Command command = new Command();

        command.setCreatedAt( source.getCreatedAt() );
        command.setTenantId( source.getTenantId() );
        command.setUsername( source.getUsername() );
        command.setPayload( commandJsonMapper.map( source.getPayload() ) );

        return command;
    }
}
