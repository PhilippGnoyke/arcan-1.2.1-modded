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
            if (exTimeLoggerEvent.hasBeenStarted())
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
            if (exTimeLoggerEvent.hasBeenStarted())
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

    public void subtractEventFromEvent(ETLE.Event eventMain, ETLE.Event toBeSubtracted)
    {
        ExTimeLoggerEvent event = events.get(eventMain);
        if(events.containsKey(toBeSubtracted))
        {
            ExTimeLoggerEvent eventToBeSubtracted = events.get(toBeSubtracted);
            event.subtractTime(eventToBeSubtracted.getDurationInNanoSecs());
        }
    }

    public void subtractEventsFromEvent(ETLE.Event eventMain, ETLE.Event[] toBeSubtracted)
    {
        for (ETLE.Event eventToBeSubtracted : toBeSubtracted)
        {
            subtractEventFromEvent(eventMain, eventToBeSubtracted);
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

        private boolean hasBeenStarted()
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

        private void subtractTime(long toSubtract)
        {
            cumulatedDuration -= toSubtract;
        }

        public String getEventDescription()
        {
            return ETLE.getText(event);
        }

        private long getDurationInNanoSecs()
        {
            return cumulatedDuration;
        }

        public long getDurationInMilliSecs()
        {
            return getDurationInNanoSecs() / 1000000;
        }

        public int getEventCount()
        {
            return eventCount;
        }
    }


}
