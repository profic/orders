package orders;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import java.util.HashMap;
import java.util.Map;

@Aspect
public class LogExecutionTime {
    public static Map<String, Long> methodExecutions = new HashMap<>();

    //    @Pointcut("execution(orders.HeapContainer.*")
//    @Pointcut("* orders.HeapContainer.*(..)")
    public void myPointCut() {}

//    @Around("execution(public * orders.HeapContainer.*(..))")
    public Object logTimeMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.nanoTime();

        Object retVal = joinPoint.proceed();

        long total = System.nanoTime() - start;

//        logExecutionTime(joinPoint, total);
        return retVal;
    }

    private void logExecutionTime(ProceedingJoinPoint joinPoint, long total) {
        String className  = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String key        = className + "." + methodName;

        methodExecutions.compute(
                key,
                (k, oldValue) -> oldValue == null ? total : (oldValue + total) / 2
        );
    }
}