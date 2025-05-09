package ru.lenok.server.commands;

import ru.lenok.common.CommandRequest;
import ru.lenok.common.CommandResponse;
import ru.lenok.common.commands.AbstractCommand;
import ru.lenok.server.services.ProductService;

import java.sql.SQLException;

import static ru.lenok.server.commands.CommandName.show;

public class RegisterProductCommand extends AbstractCommand {
    ProductService productService;

    public RegisterProductCommand(ProductService productService) {
        super(show.getBehavior(), "зарегестрировать новый товар");
        this.productService = productService;
    }

    private CommandResponse execute(Long ownerId, String productName) throws SQLException {
        productService.registerProduct(productName, ownerId);
        return new CommandResponse("Продукт успешно добавлен: " + productName);
    }

    @Override
    public CommandResponse execute(CommandRequest req) throws Exception {
        return execute(req.getUser().getId(), req.getCommandWithArgument().getArgument1());
    }
}
