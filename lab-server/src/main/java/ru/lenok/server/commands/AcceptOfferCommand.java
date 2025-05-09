package ru.lenok.server.commands;

import ru.lenok.common.CommandRequest;
import ru.lenok.common.CommandResponse;
import ru.lenok.common.commands.AbstractCommand;
import ru.lenok.server.services.OfferService;

import java.sql.SQLException;

import static ru.lenok.server.commands.CommandName.show;

public class AcceptOfferCommand extends AbstractCommand {
    OfferService offerService;

    public AcceptOfferCommand(OfferService offerService) {
        super(show.getBehavior(), "принять предложение");
        this.offerService = offerService;
    }

    private CommandResponse execute(String argument1, Long userId) throws SQLException {
        long offerId = Long.parseLong(argument1);
        offerService.acceptOffer(offerId, userId);
        return new CommandResponse("");
    }

    @Override
    public CommandResponse execute(CommandRequest req) throws Exception {
        return execute(req.getCommandWithArgument().getArgument1(), req.getUser().getId());
    }
}
