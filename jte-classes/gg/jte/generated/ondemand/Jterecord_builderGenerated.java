package gg.jte.generated.ondemand;
import io.github.sashirestela.easybuilder.model.Model;
@SuppressWarnings("unchecked")
public final class Jterecord_builderGenerated {
	public static final String JTE_NAME = "record_builder.jte";
	public static final int[] JTE_LINE_INFO = {0,0,1,1,1,1,2,3,5,5,6,6,8,8,9,9,10,10,12,12,14,14,17,17,18,18,19,19,20,20,20,20,21,21,24,24,28,28,29,29,33,33,33,33,34,34,34,34,35,35,36,36,37,37,37,37,38,38,43,43,44,44,45,45,46,46,46,46,46,46,46,46,47,47,47,47,51,51,54,54,55,55,56,56,56,56,59,59,59,1,1,1,1};
	public static void render(gg.jte.TemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, Model model) {
		jteOutput.writeContent("\n");
		jteOutput.writeContent("\n\n");
		var processingEnv = model.getProcessingEnvironment();
		jteOutput.writeContent("\n");
		var recordElement = model.getTypeElement();
		jteOutput.writeContent("\n\n");
		var packageName = processingEnv.getElementUtils().getPackageOf(recordElement).getQualifiedName().toString();
		jteOutput.writeContent("\n");
		var recordName = recordElement.getSimpleName().toString();
		jteOutput.writeContent("\n");
		var builderName = recordName + "Builder";
		jteOutput.writeContent("\n\npackage ");
		jteOutput.writeUserContent(packageName);
		jteOutput.writeContent(";\n\npublic class ");
		jteOutput.writeUserContent(builderName);
		jteOutput.writeContent(" {\n\n    // Fields\n    ");
		for (var recComp : recordElement.getRecordComponents()) {
			jteOutput.writeContent("\n    ");
			var recCompName = recComp.getSimpleName().toString();
			jteOutput.writeContent("\n    ");
			var recCompType = recComp.asType().toString();
			jteOutput.writeContent("\n    private ");
			jteOutput.writeUserContent(recCompType);
			jteOutput.writeContent(" ");
			jteOutput.writeUserContent(recCompName);
			jteOutput.writeContent(";\n    ");
		}
		jteOutput.writeContent("\n\n    // Private constructor\n    private ");
		jteOutput.writeUserContent(builderName);
		jteOutput.writeContent("() {\n    }\n\n    // Static method to create a new builder\n    public static ");
		jteOutput.writeUserContent(builderName);
		jteOutput.writeContent(" new() {\n        return new ");
		jteOutput.writeUserContent(builderName);
		jteOutput.writeContent("();\n    }\n\n    // Static method to create a new builder from a record\n    public static ");
		jteOutput.writeUserContent(builderName);
		jteOutput.writeContent(" from(");
		jteOutput.writeUserContent(recordName);
		jteOutput.writeContent(" other) {\n        ");
		jteOutput.writeUserContent(builderName);
		jteOutput.writeContent(" one = new ");
		jteOutput.writeUserContent(builderName);
		jteOutput.writeContent("();\n        ");
		for (var recComp : recordElement.getRecordComponents()) {
			jteOutput.writeContent("\n        ");
			var recCompName = recComp.getSimpleName().toString();
			jteOutput.writeContent("\n        one.");
			jteOutput.writeUserContent(recCompName);
			jteOutput.writeContent(" = other.");
			jteOutput.writeUserContent(recCompName);
			jteOutput.writeContent("();\n        ");
		}
		jteOutput.writeContent("\n        return one;\n    }\n\n    // Accesors\n    ");
		for (var recComp : recordElement.getRecordComponents()) {
			jteOutput.writeContent("\n    ");
			var recCompName = recComp.getSimpleName().toString();
			jteOutput.writeContent("\n    ");
			var recCompType = recComp.asType().toString();
			jteOutput.writeContent("\n    public ");
			jteOutput.writeUserContent(builderName);
			jteOutput.writeContent(" ");
			jteOutput.writeUserContent(recCompName);
			jteOutput.writeContent("(");
			jteOutput.writeUserContent(recCompType);
			jteOutput.writeContent(" ");
			jteOutput.writeUserContent(recCompName);
			jteOutput.writeContent(") {\n        this.");
			jteOutput.writeUserContent(recCompName);
			jteOutput.writeContent(" = ");
			jteOutput.writeUserContent(recCompName);
			jteOutput.writeContent(";\n        return this;\n    }\n\n    ");
		}
		jteOutput.writeContent("\n\n    // Build method\n    public ");
		jteOutput.writeUserContent(recordName);
		jteOutput.writeContent(" build() {\n        ");
		var recCompNameList = recordElement.getRecordComponents().stream().map(rc -> rc.getSimpleName().toString()).collect(java.util.stream.Collectors.joining(", "));
		jteOutput.writeContent("\n        return new ");
		jteOutput.writeUserContent(recordName);
		jteOutput.writeContent("(");
		jteOutput.writeUserContent(recCompNameList);
		jteOutput.writeContent(");\n    }\n}\n");
	}
	public static void renderMap(gg.jte.TemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, java.util.Map<String, Object> params) {
		Model model = (Model)params.get("model");
		render(jteOutput, jteHtmlInterceptor, model);
	}
}
