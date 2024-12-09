package io.github.sashirestela.easybuilder.support;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TemplateProcessor1 {

    /**
     * Processes a template string with variable replacements, supporting multiple elseif and nested
     * directives
     * 
     * @param template The input template string
     * @param context  A map of variables and their values
     * @return Processed string with variables replaced
     */
    public static String process(String template, Map<String, Object> context) {
        // First, handle variable replacements
        String processedTemplate = replaceVariables(template, context);

        // Then handle nested control structures
        processedTemplate = processNestedDirectives(processedTemplate, context);

        return processedTemplate;
    }

    public static String process(Path path, Map<String, Object> context) throws IOException {
        String template = new String(Files.readAllBytes(path));
        return process(template, context);
    }

    /**
     * Replaces ${variable} placeholders with their corresponding values
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
     * Processes nested directives by recursively handling conditionals and loops
     */
    private static String processNestedDirectives(String template, Map<String, Object> context) {
        // First, process conditionals (which may contain nested elements)
        String processedTemplate = processConditionals(template, context);

        // Then process loops (which may also contain nested elements)
        processedTemplate = processLoops(processedTemplate, context);

        return processedTemplate;
    }

    /**
     * Processes conditional blocks with support for multiple elseif
     */
    private static String processConditionals(String template, Map<String, Object> context) {
        Pattern conditionalPattern = Pattern.compile(
                "\\#\\{if\\((.*?)\\)}(.*?)" + // Initial if block
                        "((?:\\#\\{elseif\\((.*?)\\)}(.*?))*)" + // Multiple elseif blocks
                        "(?:\\#\\{else\\}(.*?))?" + // Optional else block
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
     * Evaluates multi-conditional logic with multiple elseif blocks
     */
    private static String evaluateMultiConditional(String mainCondition, String mainBlock, String elseIfBlocks,
            String elseBlock, Map<String, Object> context) {
        // Check main if condition
        if (evaluateCondition(mainCondition, context)) {
            return process(mainBlock, context);
        }

        // Check multiple elseif conditions
        Pattern elseIfPattern = Pattern.compile("\\#\\{elseif\\((.*?)\\)\\}(.*?)(?=\\#\\{|$)");
        Matcher elseIfMatcher = elseIfPattern.matcher(elseIfBlocks);

        while (elseIfMatcher.find()) {
            String elseIfCondition = elseIfMatcher.group(1);
            String elseIfBlock = elseIfMatcher.group(2);

            if (evaluateCondition(elseIfCondition, context)) {
                return process(elseIfBlock, context);
            }
        }

        // Return else block if no conditions met
        return process(elseBlock, context);
    }

    /**
     * Processes loop blocks with support for nested directives and optional separator
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
     * Evaluates and processes loop iterations with nested processing and optional separator
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
     * Evaluates a single condition
     */
    private static boolean evaluateCondition(String condition, Map<String, Object> context) {
        if (condition == null || condition.trim().isEmpty())
            return false;

        // Evaluate using expression parsing
        String value = evaluateExpression(condition.trim(), context);

        // Convert result to boolean
        return Boolean.parseBoolean(value) || (!value.isEmpty() && !"false".equalsIgnoreCase(value));
    }

    private static String evaluateExpression(String expression, Map<String, Object> context) {
        try {
            // Handle method calls
            if (expression.contains("(")) {
                return evaluateMethodCall(expression, context);
            }
            // Handle direct variable replacement
            if (context.containsKey(expression)) {
                return Objects.toString(context.get(expression), "");
            }
            return ""; // Default to empty if not found
        } catch (Exception e) {
            return ""; // Fallback for errors
        }
    }

    private static String evaluateMethodCall(String expression, Map<String, Object> context) throws Exception {
        Pattern methodPattern = Pattern.compile("(\\w+)\\.(\\w+)\\((.*?)\\)");
        Matcher matcher = methodPattern.matcher(expression);

        if (!matcher.matches())
            return "";

        String objectName = matcher.group(1);
        String methodName = matcher.group(2);
        String[] params = (matcher.group(3).isEmpty() ? new String[0] : matcher.group(3).split(",", 0));

        // Resolve object from context
        Object object = context.get(objectName);
        if (object == null)
            return "";

        // Resolve method and invoke it
        Class<?> clazz = object.getClass();
        Method method = findMethod(clazz, methodName, params.length);
        if (method == null)
            return "";

        Object[] parsedParams = parseParameters(params, context);
        Object result = method.invoke(object, parsedParams);
        return Objects.toString(result, "");
    }

    private static Method findMethod(Class<?> clazz, String methodName, int paramCount) {
        for (Method method : clazz.getMethods()) {
            if (method.getName().equals(methodName) && method.getParameterCount() == paramCount) {
                return method;
            }
        }
        return null;
    }

    private static Object[] parseParameters(String[] params, Map<String, Object> context) {
        Object[] parsedParams = new Object[params.length];
        for (int i = 0; i < params.length; i++) {
            String param = params[i].trim();
            parsedParams[i] = context.containsKey(param) ? context.get(param) : param;
        }
        return parsedParams;
    }

}
