package bloop.parser.statement;

import bloop.instruction.Instruction;
import bloop.parser.cursor.TokenCursor;
import bloop.token.TokenType;


public interface StatementParser {

    TokenType triggerToken();

    Instruction parse(TokenCursor cursor, BlockParser blockParser);
}
