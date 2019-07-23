package com.github.icovn.aspect;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.CodeSignature;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class MethodInfoAspect {

    private static final String MSG_PROMPT_AUTHEN = "Enter password: ";
    private static final String MSG_INPUT_FAIL = "Error while getting password";
    private static final String MSG_AUTHEN_FAILED = "Password is not correct";
    private static final String AUTHEN_CODE = "123";

    @Before("execution(* com.github.icovn.aop..*(..))")
    public void restrict(JoinPoint joinPoint) throws NoLoginException {
        Log.info(MSG_PROMPT_AUTHEN);
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {
            if (!AUTHEN_CODE.equals(reader.readLine())) {
                throw new NoLoginException(MSG_AUTHEN_FAILED);
            }
        } catch (IOException e) {
            Log.err(e);
            throw new NoLoginException(MSG_INPUT_FAIL);
        }
    }

    @Around("execution(* com.github.icovn.aop..*(..))")
    public Object logDuration(ProceedingJoinPoint joinPoint) throws Throwable {
        
        String methodFullname = joinPoint.getSignature().toString();

        long start = System.currentTimeMillis();
        Object proceed = joinPoint.proceed();
        long duration = System.currentTimeMillis() - start;
        Log.info("{");
        Log.info(String.format("  \"name\": %s", methodFullname));
        Log.info("  \"arguments\": [");
        
        CodeSignature codeSignature = (CodeSignature) joinPoint.getSignature();
        String[] paramNames = codeSignature.getParameterNames();
        Object[] methodArgs = joinPoint.getArgs();
        for (int i = 0; i < methodArgs.length; i++) {
            Object arg = methodArgs[i];
            Log.info("  {");
            Log.info(String.format("      \"name\": %s,", paramNames[i]));
            Log.info(String.format("      \"type\": %s,", arg.getClass().getSimpleName()));
            Log.info(String.format("      \"value\": %s", arg.toString()));
            if (i < methodArgs.length - 1)
                Log.info("  },");
            else
                Log.info("  }");
        }
        Log.info("  ]");
        Log.info(String.format("  \"duration\": %dms", duration));
        Log.info(String.format("  \"output\": %s", proceed.toString()));
        Log.info("}");

        return proceed;
    }

    private class NoLoginException extends Exception {
        public NoLoginException(String message) {
            super(message);
        }
        private static final long serialVersionUID = -5109567031982097224L;
    }
}
