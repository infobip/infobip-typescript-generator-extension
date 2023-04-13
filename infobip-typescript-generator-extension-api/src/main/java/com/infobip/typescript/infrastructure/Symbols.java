package com.infobip.typescript.infrastructure;

import java.util.Optional;

import cz.habarta.typescript.generator.compiler.Symbol;
import cz.habarta.typescript.generator.compiler.SymbolTable;

public class Symbols {

    public static Symbol resolve(SymbolTable symbolTable, Class<?> type) {
        return Optional.ofNullable(symbolTable.getSymbolIfImported(type))
                       .orElseGet(() -> symbolTable.getSymbol(type));
    }

}
