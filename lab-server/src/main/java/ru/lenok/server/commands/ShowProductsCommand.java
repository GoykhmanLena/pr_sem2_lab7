package ru.lenok.server.commands;

import ru.lenok.common.CommandRequest;
import ru.lenok.common.CommandResponse;
import ru.lenok.common.Product;
import ru.lenok.common.auth.LoginResponse;
import ru.lenok.common.commands.AbstractCommand;
import ru.lenok.server.collection.LabWorkService;
import ru.lenok.server.services.ProductService;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static ru.lenok.server.commands.CommandName.show;

public class ShowProductsCommand extends AbstractCommand {
    ProductService productService;

    public ShowProductsCommand(ProductService productService) {
        super(show.getBehavior(), "вывести список товаров пользователя");
        this.productService = productService;
    }

    private CommandResponse execute(Long userId) throws SQLException {
        List<Product> products = productService.getUserProducts(userId);
        return new CommandResponse(
                products.stream()
                .map(Object::toString)
                .collect(Collectors.joining("\n"))
        );
    }

    @Override
    public CommandResponse execute(CommandRequest req) throws Exception {
        return execute(req.getUser().getId());
    }
}
