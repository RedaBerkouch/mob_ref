package ch.bfs.meb.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.bfs.meb.logback.MonitorLayout;

public class LoggerAdvice {
    private final static Logger LOGGER = LoggerFactory.getLogger(LoggerAdvice.class);

    public Object handleAround(ProceedingJoinPoint call) throws Throwable {
        long start = System.currentTimeMillis();
        StringBuilder sb = new StringBuilder();
        try {
            if (LOGGER.isDebugEnabled()) {
                sb.append(call.getSignature().getDeclaringTypeName());
                sb.append(".");
                sb.append(call.getSignature().getName());
                sb.append("(");

                Object[] args = call.getArgs();
                for (int i = 0; i < args.length; i++) {
                    if (i > 0)
                        sb.append(",");

                    Object object = args[i];
                    if (object == null)
                        sb.append("NULL");
                    else
                        sb.append(object);
                }

                sb.append(")");

                LOGGER.debug("start: " + sb.toString());
            }

            return call.proceed();
        } finally {
            long end = System.currentTimeMillis() - start;
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("end: " + end + "ms " + sb.toString());
            }

            String simpleClassName = call.getSignature().getDeclaringType().getSimpleName();
            if (simpleClassName.endsWith("Provider"))
                simpleClassName = simpleClassName.substring(0, simpleClassName.length() - 8);
            LOGGER.info(MonitorLayout.SERVICE_MARKER, "{}.{} [{}]", new Object[] { simpleClassName, call.getSignature().getName(), end });
        }
    }
}