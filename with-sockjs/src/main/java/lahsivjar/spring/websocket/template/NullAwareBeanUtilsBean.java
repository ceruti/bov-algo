package lahsivjar.spring.websocket.template;

import lahsivjar.spring.websocket.template.model.Event;
import lahsivjar.spring.websocket.template.model.Market;
import lahsivjar.spring.websocket.template.model.Outcome;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

@Component
public class NullAwareBeanUtilsBean extends BeanUtilsBean {

    private static final Set<Class<?>> primitiveTypes = new HashSet<Class<?>>(
            Arrays.asList(Boolean.class,
                    Character.class,
                    Byte.class,
                    Short.class,
                    Integer.class,
                    Long.class,
                    Float.class,
                    Double.class,
                    Void.class,
                    String.class,
                    Date.class));

    @Override
    public void copyProperty(Object dest, String name, Object value)
            throws IllegalAccessException, InvocationTargetException {
        if (value == null)
            return;

        if (primitiveTypes.contains(value.getClass())) {
            super.copyProperty(dest, name, value);
        } else {
            // don't try to handle non primitives...
        }
    }

    public void updateEvent(Event toUpdate, Event source) throws InvocationTargetException, IllegalAccessException {
        this.copyProperties(toUpdate, source);
        // NOTE: we won't bother to copy competitors because these shouldn't change
        copyMarkets(toUpdate, source);
    }

    private void copyMarkets(Event toUpdate, Event source) {
        if (toUpdate.getMarkets() == null && source.getMarkets() != null) {
            toUpdate.setMarkets(source.getMarkets());
        } else if (toUpdate.getMarkets() != null && source.getMarkets() != null) {
            source.getMarkets().values().forEach(sourceMarket -> {
                if (!toUpdate.getMarkets().containsKey(sourceMarket.getId())) {
                    toUpdate.getMarkets().put(sourceMarket.getId(), sourceMarket);
                } else {
                    Market targetMarket = toUpdate.getMarkets().get(sourceMarket.getId());
                    try {
                        copyProperties(targetMarket, sourceMarket);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    copyOutcomes(targetMarket, sourceMarket.getOutcomes());
                }
            });
        }

    }

    private void copyOutcomes(Market targetMarket, Map<String, Outcome> sourceOutcomes) {
        if (sourceOutcomes == null) {
            return;
        }
        else if (targetMarket.getOutcomes() == null) {
            targetMarket.setOutcomes(sourceOutcomes);
        }
        else {
            sourceOutcomes.values().forEach(sourceOutcome -> {
                if (!targetMarket.getOutcomes().containsKey(sourceOutcome.getId())) {
                    targetMarket.getOutcomes().put(sourceOutcome.getId(), sourceOutcome);
                } else {
                    Outcome targetOutcome = targetMarket.getOutcomes().get(sourceOutcome.getId());
                    try {
                        copyProperties(targetOutcome, sourceOutcome);
                        copyProperties(targetOutcome.getPrice(), sourceOutcome.getPrice());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }


}
