package ru.lenok.server.commands;

import ru.lenok.common.CommandRequest;
import ru.lenok.common.CommandResponse;
import ru.lenok.common.FullOffer;
import ru.lenok.common.commands.AbstractCommand;
import ru.lenok.server.services.OfferService;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import static ru.lenok.server.commands.CommandName.show;

public class ShowIncomingOffersCommand extends AbstractCommand {
    OfferService offerService;

    public ShowIncomingOffersCommand(OfferService offerService) {
        super(show.getBehavior(), "вывести список сделанных пользователю предложений");
        this.offerService = offerService;
    }


    private CommandResponse execute(Long userId) throws SQLException {
        List<FullOffer> answer = offerService.getIncomingOffers(userId);
        return new CommandResponse(
                answer.stream()
                        .map(Object::toString)
                        .collect(Collectors.joining("\n"))
        );
    }

    @Override
    public CommandResponse execute(CommandRequest req) throws Exception {
        return execute(req.getUser().getId());
    }
}
