package it.unimib.disco.essere.main;


import java.util.*;

//Modded
public class ExTimeLogger
{
    private SortedMap<ETLE.Event, ExTimeLoggerEvent> events;


    public ExTimeLogger()
    {
        events = new TreeMap<>();
    }

    public void logEventStart(ETLE.Event event)
    {
        if (events.containsKey(event))
        {
            ExTimeLoggerEvent exTimeLoggerEvent = events.get(event);
            if (exTimeLoggerEvent.getStarted())
            {
                throw new IllegalStateException("Started the same event twice");
            }
            else
            {
                exTimeLoggerEvent.restart();
            }
        }
        else
        {
            events.put(event, new ExTimeLoggerEvent(event, ETLE.getParent(event)));
        }
    }


    public void logEventEnd(ETLE.Event event)
    {
        if (events.containsKey(event))
        {
            ExTimeLoggerEvent exTimeLoggerEvent = events.get(event);
            if (exTimeLoggerEvent.getStarted())
            {
                exTimeLoggerEvent.finish();
            }
            else
            {
                throw new IllegalStateException("The event was not restarted!");
            }
        }
        else
        {
            throw new IllegalStateException("The event was not started even once!");
        }
    }

    public Collection<ExTimeLoggerEvent> getEvents()
    {
        return events.values();
    }

    public static class ExTimeLoggerEvent
    {
        private final ETLE.Event event;
        private final ETLE.Event parent;
        private long timestampStart;
        private long cumulatedDuration;
        private int eventCount;
        private boolean started;

        private ExTimeLoggerEvent(ETLE.Event event, ETLE.Event parent)
        {
            this.event = event;
            this.parent = parent;
            this.timestampStart = System.nanoTime();
            this.started = true;
        }

        private boolean getStarted()
        {
            return started;
        }

        private void restart()
        {
            timestampStart = System.nanoTime();
            started = true;
        }

        private void finish()
        {
            cumulatedDuration += (System.nanoTime() - timestampStart);
            eventCount++;
            started = false;
        }

        public int getEventId()
        {
            return event.ordinal();
        }

        public int getParentId()
        {
            final int NO_PARENT = -1;
            return parent == null ? NO_PARENT : parent.ordinal();
        }

        public String getEventDescription()
        {
            return ETLE.getText(event);
        }

        public long getDurationInMilliSecs()
        {
            return cumulatedDuration / 1000000;
        }

        public int getEventCount()
        {
            return eventCount;
        }
    }


}
