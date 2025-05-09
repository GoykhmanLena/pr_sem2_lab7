package ru.lenok.server.commands;

import ru.lenok.common.commands.AbstractCommand;
import ru.lenok.common.commands.CommandBehavior;
import ru.lenok.common.commands.Executable;
import ru.lenok.server.collection.LabWorkService;
import ru.lenok.server.services.OfferService;
import ru.lenok.server.services.ProductService;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.lenok.server.commands.CommandName.*;

public class CommandRegistry {
    public Map<CommandName, Executable> commands = new HashMap<>();
    public Collection<CommandName> commandDefinitions;
    public Map<String, CommandBehavior> clientCommandDefinitions;

    public CommandRegistry(LabWorkService labWorkService, ProductService productService, OfferService offerService, IHistoryProvider historyProvider) {
        commands.put(insert, wrap(new InsertToCollectionCommand(labWorkService)));
        commands.put(exit, wrap(new ExitFromProgramCommand()));
        commands.put(show, wrap(new ShowCollectionCommand(labWorkService)));
        commands.put(save, wrap(new SaveToFileCommand(labWorkService)));
        commands.put(remove_key, wrap(new RemoveByKeyFromCollectionCommand(labWorkService)));
        commands.put(update_id, wrap(new UpdateByIdInCollectionCommand(labWorkService)));
        commands.put(print_ascending, wrap(new PrintAscendingCommand(labWorkService)));
        commands.put(remove_greater, wrap(new RemoveGreaterFromCollectionCommand(labWorkService)));
        commands.put(replace_if_greater, wrap(new ReplaceIfGreaterInCollectionCommand(labWorkService)));
        commands.put(filter_contains_description, wrap(new FilterContainsDescriptionCommand(labWorkService)));
        commands.put(filter_starts_with_name, wrap(new FilterStartsWithNameCommand(labWorkService)));
        commands.put(help, wrap(new HelpCommand(this)));
        commands.put(info, wrap(new InfoAboutCollectionCommand(labWorkService)));
        commands.put(clear, wrap(new ClearCollectionCommand(labWorkService)));
        commands.put(execute_script, wrap(new ExecuteScriptCommand(labWorkService)));
        commands.put(history, wrap(new HistoryCommand(historyProvider)));
        commands.put(make_offer, wrap(new MakeOfferCommand(offerService)));
        commands.put(show_products, wrap(new ShowProductsCommand(productService)));
        commands.put(show_incoming_offers, wrap(new ShowIncomingOffersCommand(offerService)));
        commands.put(show_outgoing_offers, wrap(new ShowOutgoingOffersCommand(offerService)));
        commands.put(accept_offer, wrap(new AcceptOfferCommand(offerService)));
        commands.put(register_product, wrap(new RegisterProductCommand(productService)));

        commandDefinitions = commands.keySet();

        clientCommandDefinitions = commands.keySet().stream()
                .filter(key -> key != save)
                .collect(Collectors.toMap(commandName -> commandName.name(), commandName -> commandName.getBehavior()));
    }

    private Executable wrap(AbstractCommand command) {
        return new CommandWithLoggingExecuteTime(command);
    }
    public Map<String, CommandBehavior> getClientCommandDefinitions() {
        return clientCommandDefinitions;
    }

    public Executable getCommand(CommandName commandName) throws IllegalArgumentException {
        return commands.get(commandName);
    }

    public String getCommandDescription(CommandName commandName) {
        Executable command = getCommand(commandName);
        return commandName.name() + ": " + command.getDescription();
    }

    public String getCommandDescription(String commandNameStr) {
        CommandName commandName = valueOf(commandNameStr);
        return getCommandDescription(commandName);
    }

}
