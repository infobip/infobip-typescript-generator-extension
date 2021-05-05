package com.infobip.typescript.showcase;

import com.infobip.typescript.GenerateTypescript;
import com.infobip.typescript.showcase.hierarchy.HierarchyTypeScriptFileGenerator;
import com.infobip.typescript.showcase.simple.BasicTypeScriptFileGenerator;

@GenerateTypescript(generator = HierarchyTypeScriptFileGenerator.class)
public class HierarchyTypeScriptFileGeneratorConfiguration {
}
