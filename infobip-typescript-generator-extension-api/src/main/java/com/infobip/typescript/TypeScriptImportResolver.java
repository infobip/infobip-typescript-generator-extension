package com.infobip.typescript;

import java.util.List;

public interface TypeScriptImportResolver {

    List<String> resolve(String typeScript);
}
