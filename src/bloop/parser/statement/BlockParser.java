package bloop.parser.statement;

import bloop.instruction.Instruction;
import java.util.List;

@FunctionalInterface
public interface BlockParser {
    List<Instruction> parseBlock();
}
