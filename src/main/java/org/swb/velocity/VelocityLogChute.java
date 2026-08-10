package org.swb.velocity;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogChute;

public class VelocityLogChute implements LogChute
{
    private static final Logger log = Logger.getLogger(VelocityLogChute.class.getCanonicalName());
    
    @Override
    public void init(RuntimeServices rs) throws Exception
    {
    }

    @Override
    public boolean isLevelEnabled(int level)
    {
        return level >= LogChute.ERROR_ID;
    }

    @Override
    public void log(int level, String msg)
    {
        if (level>=LogChute.ERROR_ID) log.severe(msg);
    }

    @Override
    public void log(int level, String msg, Throwable exc)
    {
        if (level>=LogChute.ERROR_ID) log.log(Level.SEVERE, msg, exc);
    }

}
