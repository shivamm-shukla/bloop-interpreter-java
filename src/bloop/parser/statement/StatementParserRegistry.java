package bloop.parser.statement;

import bloop.exceptions.BloopParseException;
import bloop.token.Token;
import bloop.token.TokenType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public final class StatementParserRegistry {

    private final Map<TokenType, StatementParser> registry = new HashMap<>();

    public StatementParserRegistry(List<StatementParser> registeredParsers) {
        for (StatementParser parser : registeredParsers) {
            registry.put(parser.triggerToken(), parser);
        }
    }


    public StatementParser resolve(Token triggerToken) {
        StatementParser handler = registry.get(triggerToken.type());
        if (handler == null) {
            throw new BloopParseException(
                    "Unexpected token '" + triggerToken.value() +
                            "' — not a valid start of a statement",
                    triggerToken.line());
        }
        return handler;
    }

    // True if a parser is registered for the given token type
    public boolean canHandle(TokenType type) {
        return registry.containsKey(type);
    }
}
