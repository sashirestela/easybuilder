package io.github.sashirestela.easybuilder.support;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TemplateProcessor {

    /**
     * Processes a template string with advanced variable replacements and nested directives
     * 
     * @param template The input template string
     * @param context  A map of variables and their values
     * @return Processed string with variables replaced and directives resolved
     */
    public static String process(String template, Map<String, Object> context) {
        String processedTemplate = template;
        String previousTemplate;

        do {
            previousTemplate = processedTemplate;
            
            // Replace variables first
            processedTemplate = replaceVariables(processedTemplate, context);
            
            // Resolve nested directives recursively
            processedTemplate = processNestedDirectives(processedTemplate, context);
        } while (!processedTemplate.equals(previousTemplate));

        return processedTemplate;
    }

    public static String process(Path path, Map<String, Object> context) throws IOException {
        String template = new String(Files.readAllBytes(path));
        return process(template, context);
    }

    /**
     * Enhanced nested directive processing with recursive resolution
     */
    private static String processNestedDirectives(String template, Map<String, Object> context) {
        // Process inner loops first (more specific patterns)
        String processedTemplate = processLoops(template, context);
        
        // Then process conditionals
        processedTemplate = processConditionals(processedTemplate, context);

        return processedTemplate;
    }

    /**
     * Replaces ${variable} placeholders with more robust handling
     */
    private static String replaceVariables(String template, Map<String, Object> context) {
        Pattern varPattern = Pattern.compile("\\$\\{(.*?)\\}");
        Matcher matcher = varPattern.matcher(template);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String expression = matcher.group(1).trim();
            String replacement = evaluateExpression(expression, context);
            if (!replacement.isEmpty()) {
                matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
            }
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * Enhanced conditional processing with more flexible nested handling
     */
    private static String processConditionals(String template, Map<String, Object> context) {
        Pattern conditionalPattern = Pattern.compile(
                "\\#\\{if\\((.*?)\\)}(.*?)" + 
                "((?:\\#\\{elseif\\((.*?)\\)}(.*?))*)" + 
                "(?:\\#\\{else\\}(.*?))?" + 
                "\\#\\{endif\\}",
                Pattern.DOTALL);

        Matcher matcher = conditionalPattern.matcher(template);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String mainCondition = matcher.group(1);
            String mainBlock = matcher.group(2) != null ? matcher.group(2) : "";
            String elseIfBlocks = matcher.group(3) != null ? matcher.group(3) : "";
            String elseBlock = matcher.group(6) != null ? matcher.group(6) : "";

            String replacement = evaluateMultiConditional(
                    mainCondition, mainBlock,
                    elseIfBlocks, elseBlock,
                    context);

            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * Enhanced loop processing with more robust nested directive handling
     */
    private static String processLoops(String template, Map<String, Object> context) {
        Pattern loopPattern = Pattern.compile(
                "\\#\\{for\\((.*?) in (.*?)(?:,\\s*(.+?))?\\)\\}(.*?)(?:\\#\\{elsefor\\}(.*?))?\\#\\{endfor\\}",
                Pattern.DOTALL);

        Matcher matcher = loopPattern.matcher(template);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String loopVar = matcher.group(1).trim();
            String collectionVar = matcher.group(2).trim();
            String separator = matcher.group(3) != null ? matcher.group(3).trim() : null;
            String loopBody = matcher.group(4);
            String elseBody = matcher.group(5);
            separator = separator != null ? separator.substring(1, separator.length() - 1) : null;

            String replacement = evaluateLoop(loopVar, collectionVar, separator, loopBody, elseBody, context);

            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * Evaluates multi-conditional logic with enhanced flexibility
     */
    private static String evaluateMultiConditional(String mainCondition, String mainBlock, 
            String elseIfBlocks, String elseBlock, Map<String, Object> context) {
        // Recursively process nested directives in blocks
        mainBlock = process(mainBlock, context);
        elseBlock = process(elseBlock, context);

        // Check main if condition
        if (evaluateCondition(mainCondition, context)) {
            return mainBlock;
        }

        // Check multiple elseif conditions
        Pattern elseIfPattern = Pattern.compile("\\#\\{elseif\\((.*?)\\)\\}(.*?)(?=\\#\\{|$)");
        Matcher elseIfMatcher = elseIfPattern.matcher(elseIfBlocks);

        while (elseIfMatcher.find()) {
            String elseIfCondition = elseIfMatcher.group(1);
            String elseIfBlock = elseIfMatcher.group(2);

            // Recursively process nested directives in elseif block
            elseIfBlock = process(elseIfBlock, context);

            if (evaluateCondition(elseIfCondition, context)) {
                return elseIfBlock;
            }
        }

        // Return else block if no conditions met
        return elseBlock;
    }

    /**
     * Enhanced loop evaluation with more comprehensive processing
     */
    private static String evaluateLoop(String loopVar, String collectionVar, String separator,
            String loopBody, String elseBody, Map<String, Object> context) {
        Object collection = context.get(collectionVar);

        if (collection == null)
            return process(elseBody, context);

        StringBuilder output = new StringBuilder();

        if (collection instanceof List) {
            List<?> list = (List<?>) collection;
            if (list.isEmpty())
                return process(elseBody, context);

            for (int i = 0; i < list.size(); i++) {
                Object item = list.get(i);
                Map<String, Object> loopContext = new HashMap<>(context);
                loopContext.put(loopVar, item);

                // Add index and size info to the context
                loopContext.put(loopVar + "_index", i);
                loopContext.put(loopVar + "_size", list.size());

                // Recursively process nested directives in loop body
                String processedBody = process(loopBody, loopContext);
                output.append(processedBody);

                // Add separator if it's not the last iteration
                if (separator != null && i < list.size() - 1) {
                    output.append(process(separator, context));
                }
            }
        } else if (collection instanceof Object[]) {
            Object[] array = (Object[]) collection;
            if (array.length == 0)
                return process(elseBody, context);

            for (int i = 0; i < array.length; i++) {
                Object item = array[i];
                Map<String, Object> loopContext = new HashMap<>(context);
                loopContext.put(loopVar, item);

                // Add index and size info to the context
                loopContext.put(loopVar + "_index", i);
                loopContext.put(loopVar + "_size", array.length);

                // Recursively process nested directives in loop body
                String processedBody = process(loopBody, loopContext);
                output.append(processedBody);

                // Add separator if it's not the last iteration
                if (separator != null && i < array.length - 1) {
                    output.append(process(separator, context));
                }
            }
        } else {
            return process(elseBody, context);
        }

        return output.toString();
    }

    /**
     * Condition evaluation with more flexible boolean conversion
     */
    private static boolean evaluateCondition(String condition, Map<String, Object> context) {
        if (condition == null || condition.trim().isEmpty())
            return false;

        String value = evaluateExpression(condition.trim(), context);

        // More robust boolean conversion
        return Boolean.parseBoolean(value) || 
               (!value.isEmpty() && 
                !"false".equalsIgnoreCase(value) && 
                !"0".equals(value));
    }

    /**
     * Enhanced expression evaluation with improved method call support
     */
    private static String evaluateExpression(String expression, Map<String, Object> context) {
        try {
            // Handle method calls with dot notation
            if (expression.contains(".")) {
                return evaluateMethodCall(expression, context);
            }
            
            // Handle direct variable replacement
            if (context.containsKey(expression)) {
                return Objects.toString(context.get(expression), "");
            }
            
            return expression; // Return original expression if no match
        } catch (Exception e) {
            return ""; // Fallback for errors
        }
    }

    /**
     * More robust method call evaluation
     */
    private static String evaluateMethodCall(String expression, Map<String, Object> context) throws Exception {
        Pattern methodPattern = Pattern.compile("(.*?)\\.([^(]+)\\((.*?)\\)");
        Matcher matcher = methodPattern.matcher(expression);

        if (!matcher.matches())
            return expression;

        String objectExpression = matcher.group(1);
        String methodName = matcher.group(2);
        String[] paramExpressions = matcher.group(3).isEmpty() ? 
            new String[0] : matcher.group(3).split("\\s*,\\s*");

        // Resolve the object, potentially nested
        Object object = resolveNestedObject(objectExpression, context);
        if (object == null)
            return "";

        // Resolve method and prepare parameters
        List<Object> resolvedParams = new ArrayList<>();
        for (String paramExp : paramExpressions) {
            resolvedParams.add(evaluateExpression(paramExp, context));
        }

        // Find and invoke method
        Method method = findBestMatchingMethod(object.getClass(), methodName, resolvedParams);
        if (method == null)
            return "";

        Object result = method.invoke(object, resolvedParams.toArray());
        return Objects.toString(result, "");
    }

    /**
     * Resolves nested object references (e.g., "user.address.street")
     */
    private static Object resolveNestedObject(String expression, Map<String, Object> context) {
        String[] parts = expression.split("\\.");
        Object current = context.get(parts[0]);
        
        for (int i = 1; i < parts.length && current != null; i++) {
            try {
                Method getter = current.getClass().getMethod("get" + 
                    Character.toUpperCase(parts[i].charAt(0)) + parts[i].substring(1));
                current = getter.invoke(current);
            } catch (Exception e) {
                return null;
            }
        }
        
        return current;
    }

    /**
     * Finds the best matching method based on parameter types
     */
    private static Method findBestMatchingMethod(Class<?> clazz, String methodName, List<Object> params) {
        Method method = null;
        try {
            if (params.isEmpty()) {
                method = clazz.getMethod(methodName);
            } else {
                method = clazz.getMethod(methodName, (Class<?>[]) params.stream().map(p -> p.getClass()).toArray());
            }
            return method;
        } catch (Exception e) {
        }
        return method;
    }
}