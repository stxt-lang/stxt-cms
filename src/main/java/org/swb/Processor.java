package org.swb;

import java.util.Map;
import java.util.Properties;

public interface Processor
{
    public void init(String name, Properties config) throws Exception;
    public void execute(Map<String, Object> context) throws Exception;
}
