package com.github.icovn.aspect;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.CodeSignature;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class MethodInfoAspect {

    private static final String PASSWORD_IS_NOT_CORRECT = "Password is not correct";
    private static final String AUTHEN_CODE = "123";

    @Before("execution(* com.github.icovn.aop..*(..))")
    public void restrict(JoinPoint joinPoint) throws NoLoginException {
        Log.info("Enter password: ");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {
            if (!AUTHEN_CODE.equals(reader.readLine())) {
                throw new NoLoginException(PASSWORD_IS_NOT_CORRECT);
            }
        } catch (IOException e) {
            Log.err(e);
            throw new NoLoginException("Error while getting password");
        }
    }

    @Before("execution(* com.github.icovn.aop..*(..))")
    public void logInput(JoinPoint joinPoint) {

        CodeSignature codeSignature = (CodeSignature) joinPoint.getSignature();
        String[] paramNames = codeSignature.getParameterNames();
        Object[] methodArgs = joinPoint.getArgs();
        for (int i = 0; i < methodArgs.length; i++) {
            Object arg = methodArgs[i];
            Log.info("Argument", String.format("#%s:", (i + 1)), 
                    arg.getClass().getSimpleName(), "-", 
                    paramNames[i], "-", 
                    arg.toString());
        }
    }

    @Around("execution(* com.github.icovn.aop..*.*(..))")
    public Object logDuration(ProceedingJoinPoint joinPoint) throws Throwable {

        long start = System.currentTimeMillis();
        Object proceed = joinPoint.proceed();
        long duration = System.currentTimeMillis() - start;

        Log.info(joinPoint.getSignature().toString(), "duration", String.valueOf(duration), "ms");
        return proceed;
    }

    @AfterReturning(pointcut = "execution(* com.github.icovn.aop..*(..))", returning = "retVal")
    public void logOutput(JoinPoint joinPoint, Object retVal) {

        Log.info("Output:", retVal.getClass().getSimpleName(), "-", retVal.toString());
    }

    private class NoLoginException extends Exception {
        public NoLoginException(String message) {
            super(message);
        }
        private static final long serialVersionUID = -5109567031982097224L;
    }
}
