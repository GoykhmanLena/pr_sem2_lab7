package ru.lenok.server.commands;

import ru.lenok.common.CommandRequest;
import ru.lenok.common.CommandResponse;
import ru.lenok.common.commands.AbstractCommand;
import ru.lenok.server.services.OfferService;

import java.sql.SQLException;

import static ru.lenok.server.commands.CommandName.show;

public class MakeOfferCommand extends AbstractCommand {
    OfferService offerService;

    public MakeOfferCommand(OfferService offerService) {
        super(show.getBehavior(), "Сделать предложение об обмене товара на LabWork");
        this.offerService = offerService;
    }

    private CommandResponse execute(String labWorkIdStr, String productIdStr, Long userId) throws SQLException {
        long labWorkId = Long.parseLong(labWorkIdStr);
        long productId = Long.parseLong(productIdStr);
        offerService.createOffer(labWorkId, productId, userId);
        return new CommandResponse("Предложение добалено успешно");
    }

    @Override
    public CommandResponse execute(CommandRequest req) throws Exception {
        return execute(req.getCommandWithArgument().getArgument1(), req.getCommandWithArgument().getArgument2(), req.getUser().getId());
    }
}
